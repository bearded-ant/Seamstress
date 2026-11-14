# Seamstress — архитектура по факту

Состояние кодовой базы на 2026-11-15. Документ фиксирует то, как приложение устроено
сейчас, включая отклонения от конвенций из `AGENTS.md`. Источник — исходники `app/src/main`.

## 1. Слои и зависимости

Односторонние, без DI и без общего скоупа:

```
MainActivity : AppCompatActivity
 └ NavHostFragment (R.id.fragmentContainerView) ← navigation.xml, старт customersListFragment

App : Application — пустой (db/repository закомментированы);
                    val Context.app существует, но не используется
ui/theme/SeamstressTheme — Compose Material3, lightColorScheme

ui/fragments/* ──создаёт через ViewModelProvider(this)──> viewmodel/* : AndroidViewModel
                      └─init: SeamstressDataBase.getDatabase(app) → DAO → Repository
              └──рисует Room-entity напрямую (DTO/маппинга нет)
```

Ключевое свойство: каждый экран получает **свой** экземпляр ViewModel, а ViewModel сам
открывает БД. Общих состояний и кэшей между экранами нет; данные перезапрашиваются
при каждом создании View.

## 2. Модель данных (Room, БД `seamstress_db`, version 2, exportSchema = false)

```
Customers(id: Long, name: String, phone: String?, img: Int, balance: Long)
Measurement(id, name, dateCreation: String, dressType: String, customerId: Long)
Metric(id, typeId, shortName, name, description: String?)
TakenMeasurement(id, metricId, measuredId, customerId, value: Float)
```

`SeamstressDataBase` — `@Database` с 4 DAO-аксессорами, синглтон `INSTANCE`
(`@Volatile` + `synchronized`), `createFromAsset("pre_seamstress")`,
`fallbackToDestructiveMigration(true)`.

`domain/dress/` — вне БД и вне UI: `Dress` (data class с `ArrayList`), `DressRepo`
(пустой интерфейс), `DressSources` (хардкод `R.drawable.jane/bp1…`, не используется).

## 3. Данные: репозитории и DAO

Репозиторий — тонкая обёртка над DAO, 1:1, без логики.

```
CustomersRepository          getAll: LiveData<List<Customers>>
                             getById / insert(→Long) / delete / update : suspend
MeasurementsRepository       getByCustomerId / getById / insert(→Long) / update / delete : suspend
MetricsRepository            getAll: LiveData<List<Metric>>, getById : suspend
TakenMeasurementsRepository  getTakenMeasById(measuredId) : suspend
```

DAO — интерфейсы с `@Query/@Insert/@Update/@Delete`; Room генерирует реализацию на KSP.
Списки клиентов и справочника мерок отдаются `LiveData`, остальное — `suspend`.

## 4. ViewModel

`AndroidViewModel` × 4, по одному на сущность; внутри `init` — БД → DAO → Repository.

```
SeamstressViewModel
  getAllCustomers: LiveData<List<Customers>>
  _selectByIdCustomerLiveData: MutableLiveData<Customers> (через postValue)
  Channel<Long> newCustomerId → Flow  — разовый навигационный сигнал после insert
  selectCustomerById / insert / delete / update

MeasurementsViewModel
  measurementLiveData: MutableLiveData<List<Measurement>> (по customerId)
  measurementByIdLiveData: MutableLiveData<Measurement> (по id)
  Channel<Long> measurementSaved → Flow — сигнал «замер сохранён»
  getMeasurementByCustomerId / getMeasurementById / insert / update / delete
  addMeasurement(customerId, name, dressType) — сам ставит дату dd.MM.yyyy и собирает Measurement

MetricViewModel
  getAllMetrics: LiveData<List<Metric>> (из репозитория), getMetricById → MutableLiveData

TakenMeasurementsViewModel
  measurementLiveData: MutableLiveData<List<TakenMeasurement>>
  getMeasurementByCustomerId(measurementId) — название врёт, аргумент это measured_id
```

Асинхронность: `viewModelScope.launch`; списки и одиночные объекты — `MutableLiveData`
+ `postValue`; одноразовые события навигации — `Channel(BUFFERED).receiveAsFlow()`.

## 5. UI: классы и наследование

Две параллельные ветки.

```
ViewBinding-ветка (XML)                              Compose-ветка
CustomersListFragment : Fragment,                    AddMeasurementFragment : Fragment
    CustomerClickListener, MenuProvider                  └ ComposeView → AddMeasurementScreen(saving, onSave, onCancel)
CustomerCardFragment : Fragment,                     SettingsFragment → SettingsScreen(onMetricsClick)
    MeasurementItemClickListener                     MetricsReferenceFragment → MetricsReferenceScreen(metrics)
AddCustomerFragment : Fragment                       состояние — remember / observeAsState;
UpdateCustomerFragment : Fragment                    composable-экраны stateless (только колбэки и данные)
CardMeasurementFragment : Fragment
```

RecyclerView — 3 комплекта «Adapter → ViewHolder → (ClickListener)»:

| Adapter | ViewHolder | Click |
|---|---|---|
| `CustomersRecyclerAdapter` | `CustomerRecyclerViewHolder` | `CustomerClickListener` (интерфейс) |
| `MeasurementRecyclerAdapter` | `MeasurementViewHolder` | `MeasurementItemClickListener` (интерфейс) |
| `TakenMeasuredRecyclerAdapter` | `TakenMeasuredViewHolder` | нет, только чтение |

Валидация — не классы, а top-level функции в `ui/fragments/customer/CustomerInput.kt`:
`parseBalance` (0…1 000 000 000, пусто → 0), `parsePhone` (ровно 11 цифр, пусто → ""),
константы `MAX_BALANCE`, `PHONE_DIGITS`.

Общие соглашения: ViewBinding в поле `_binding` + getter, обнуление в `onDestroyView`;
подписки и обработчики — в `onViewCreated`, наблюдение только через `viewLifecycleOwner`;
вся навигация — Safe Args.

## 6. Поток вызова

```
клик → Fragment → vm.method() → viewModelScope.launch → Repository → suspend DAO → Room
                              ↓
              LiveData (postValue) / Channel → Flow (событие «сделано»)
                              ↓
     observe(viewLifecycleOwner) → пересобрать Adapter / navigate / Toast / popBackStack
```

## 7. Граф экранов и переходы

```
                        [CustomersListFragment]  ★ старт
                          │ FAB                  │ меню «Настройки»
           ┌──────────────┴─────────┐            └──> [SettingsFragment]
           ▼                        ▼                        └──> [MetricsReferenceFragment]
[AddCustomerFragment]      [CustomerCardFragment *customerEntityId]
        │ insert → popUpTo(customersList)      │ FAB (карандаш) → [UpdateCustomerFragment *customerEntityId]
        └──> [CustomerCard]                   │ кнопка «замер» → [AddMeasurementFragment *customerId]
                                              └─ клик по замеру → [CardMeasurementFragment *measurementId]

[UpdateCustomerFragment]  save / cancel → [CustomerCard] (popUpTo customersList)
                          delete → [CustomersList]
[AddMeasurementFragment]  cancel → navigateUp
                          save → popBackStack (на предыдущий экран)
[CardMeasurementFragment] переходов нет
```

Аргументы передаются только `Long`: `customerEntityId`, `customerId`, `measurementId`;
сущности между экранами не передаются — карточка достаёт данные из БД по id.

Объявлены в `navigation.xml`, но нигде не вызываются:

- `action_customerCard_to_customersList`
- `action_addMeasurement_to_customerCard`
- `action_addMeasurement_to_cardMeasurement`
- `action_cardMeasurement_to_cardCustomer`

## 8. Состав экранов

| Экран | Технология | ViewModel | Состояние | Из чего состоит |
|---|---|---|---|---|
| `CustomersListFragment` | XML + ViewBinding + MenuProvider | SeamstressViewModel | `getAllCustomers: LiveData` | RecyclerView (vertical) + FAB + `menu_customers_list` |
| `CustomerCardFragment` | XML + ViewBinding | SeamstressViewModel + MeasurementsViewModel | две LiveData | `item_customer` (вложенный layout) + RecyclerView (horizontal) + FAB |
| `AddCustomerFragment` | XML + ViewBinding | SeamstressViewModel | `Channel<Long> newCustomerId` | 3 поля ввода, Save/Cancel, ошибки в TextInputLayout, блокировка Save |
| `UpdateCustomerFragment` | XML + ViewBinding | SeamstressViewModel | `customer: Customers?`, `customerId` | те же поля + Delete + AlertDialog |
| `AddMeasurementFragment` | Compose в ComposeView | MeasurementsViewModel | `saving: mutableStateOf` во фрагменте, поля — `remember` | OutlinedTextField ×2, Button / TextButton |
| `CardMeasurementFragment` | XML + ViewBinding | MeasurementsViewModel + TakenMeasurementsViewModel | две LiveData | TextView (name / type / date) + RecyclerView (vertical) |
| `SettingsFragment` | Compose | — | — | Column из `SettingsItem` |
| `MetricsReferenceFragment` | Compose + `runtime-livedata` | MetricViewModel | `observeAsState` | LazyColumn `Metric`: shortName / name / description |

Экран «Настройки» доступен только из меню списка клиентов; `CardMeasurementFragment` —
тупиковый лист (только назад системной кнопкой).

## 9. Отклонения и мёртвый код по факту

- ViewModel на фрагмент (`ViewModelProvider(this)`): у каждого экрана свой экземпляр и свой
  вызов `getDatabase`; при возврате назад ViewModel переживает пересоздание View, но данные
  перезапрашиваются в `onViewCreated`.
- Слой данных — прямой прокси к DAO; Room-сущности уходят в UI без маппинга. `Metric`
  не соединяется с `TakenMeasurement`: `TakenMeasuredViewHolder` печатает `metricId`
  в `helperText` вместо названия мерки (в коде помечено `//todo`).
- `Customers.img` игнорируется: во всех местах ставится `R.drawable.ic_baseline_add_a_photo_24`.
- `pre_seamstress` + `fallbackToDestructiveMigration(true)`: при смене версии база на
  устройстве пересоздаётся пустой.
- Мёртвое: `ui/fragments/notused/` (7 фрагментов, в графе отсутствуют), `newInstance()` во
  всех фрагментах (не вызываются), лэйауты `item_customer.xml` и
  `fragment_update_measurement.xml`, весь `domain/dress/`, `App`.
- Адаптеры списков пересоздаются на каждую эмиссию LiveData (без `ListAdapter`/`DiffUtil`).
- `AddCustomerFragment` после неудачной вставки не возвращает кнопку Save в активное состояние.
- `MeasurementDao.getMeasurementsById` объявлен как возвращающий non-null `Measurement`:
  отсутствующая строка даст NPE в `CardMeasurementFragment`.
