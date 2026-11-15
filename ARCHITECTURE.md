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

ui/fragments/* ──создаёт через ViewModelProvider(this)──> viewmodel/* : AndroidViewModel (4)
                      └─init: SeamstressDataBase.getDatabase(app) → DAO (5) → Repository (4)
              └──рисует Room-сущности и projection-строки напрямую (DTO/маппинга нет)
```

Ключевое свойство: каждый экран получает **свой** экземпляр ViewModel, а ViewModel сам
открывает БД. Общих состояний и кэшей между экранами нет; данные перезапрашиваются
при каждом создании View. Исключение — синглтон `SeamstressDataBase.INSTANCE`.

## 2. Модель данных (Room, БД `seamstress_db`, version 3, exportSchema = false)

Шесть сущностей:

```
Customers(id: Long, name: String, phone: String?, img: Int, balance: Long)   customers
DressClass(id: Long, name: String)                                           dress_classes
DressClassMetric(classId, metricId, position: Int)    PK(class_id, metric_id) dress_class_metrics
Measurement(id, name, date_creation: String, class_id, customer_id)          measurements
Metric(id, typeId, shortName, name, description: String?)                    metrics
TakenMeasurement(id, metricId, measuredId, customerId, value: Float)         taken_measurements
```

Дата замера — строка `dd.MM.yyyy` (форматируется на экране, не в БД). Баланс — целые рубли
(`Long`). Связи объявлены только в SQL-запросах: `@ForeignKey` и `ON DELETE CASCADE` нет,
индексов нет.

Три projection-класса (не таблицы), отдаются прямо в UI:

```
MeasurementRow(id, name, dateCreation, classId, className, customerId)  JOIN dress_classes
DressClassItem(id, name, metricsCount)                                  подзапрос count(*)
TakenMetricRow(metricId, value, shortName, metricName, position)        JOIN metrics + dress_class_metrics
```

`SeamstressDataBase` — `@Database` с 5 DAO-аксессорами, синглтон `INSTANCE`
(`@Volatile` + `synchronized`), `createFromAsset("pre_seamstress")`,
`fallbackToDestructiveMigration(true)`.

`domain/dress/` — смесь: `DressClass`, `DressClassMetric`, `DressClassItem` рабочие;
`Dress.kt`, `DressRepo.kt`, `DressSources.kt` — мёртвые (хардкод `R.drawable.jane/bp1…`).

## 3. Данные: репозитории и DAO

Репозиторий — тонкая обёртка над DAO, 1:1, без логики. Четыре репозитория на пять DAO:
`MeasurementsRepository` держит и `MeasurementDao`, и `TakenMeasurementDao`.

```
CustomersRepository          getAllCustomers: LiveData<List<Customers>>
                             getById / insert(→Long) / delete / update : suspend
MeasurementsRepository       getMeasurementsByCustomerId / getMeasurementById /
                             getTakenMeasurements : LiveData
                             insertMeasurementWithTaken / updateMeasurementWithTaken : suspend
MetricsRepository            getAllMetrics: LiveData<List<Metric>>
                             getMetricById / getMetricsByIds / insertMetric : suspend
DressClassesRepository       getAllClasses: LiveData<List<DressClassItem>>
                             getClassById / getMetricIds / insertClassWithMetrics : suspend
```

Транзакции — в DAO, не в репозитории:

- `MeasurementDao.insertMeasurementWithTaken` — вставка замера, затем мерок с его id;
- `MeasurementDao.updateMeasurementWithTaken` — update шапки, удаление мерок, повторная вставка;
- `DressClassDao.insertClassWithMetrics` — класс, затем `DressClassMetric` с `position` по порядку.

Списки и одиночные строки отдаются `LiveData` (Room перезапрашивает сам), запись — `suspend`.

## 4. ViewModel

`AndroidViewModel` × 4, по одному на сущность/справочник; внутри `init` — БД → DAO → Repository.

```
SeamstressViewModel
  getAllCustomers: LiveData<List<Customers>>
  _selectByIdCustomerLiveData: MutableLiveData<Customers> (через postValue)
  Channel<Long> newCustomerId → Flow — разовый навигационный сигнал после insert
  selectCustomerById / insertCustomer / deleteCustomer / updateCustomer

MeasurementsViewModel            (самая нагруженная: замеры + классы + метрики)
  allClasses: LiveData<List<DressClassItem>>, allMetrics: LiveData<List<Metric>>
  classMetricsLiveData: MutableLiveData<List<Metric>> (через postValue, из loadClassMetrics)
  measurementsByCustomer(id) / measurementById(id) / takenMeasurements(id): LiveData
  Channel<Long> measurementSaved / measurementUpdated → Flow
  addMeasurement(customerId, name, classId, dateCreation, values: List<Pair<Long, Float>>)
  updateMeasurement(measurementId, customerId, name, classId, dateCreation, values)

MetricViewModel
  getAllMetrics: LiveData<List<Metric>>
  Channel<Long> metricSaved → Flow
  insertMetric(shortName, name, description) — typeId всегда 1

DressClassViewModel
  getAllClasses: LiveData<List<DressClassItem>>
  Channel<Long> classSaved → Flow
  insertClass(name, metricIds)
```

Асинхронность: `viewModelScope.launch`; чтение — `LiveData` (+ `postValue` в двух местах),
одноразовые события навигации — `Channel(BUFFERED).receiveAsFlow()`.
Время (`dateCreation`) ставит экран, а не ViewModel.

## 5. UI: классы и наследование

Две параллельные ветки, 4 экрана на XML и 7 на Compose.

```
ViewBinding-ветка (XML)                              Compose-ветка (ComposeView)
CustomersListFragment : Fragment,                    AddMeasurementFragment → AddMeasurementScreen
    CustomerClickListener, MenuProvider              CardMeasurementFragment → CardMeasurementScreen
CustomerCardFragment : Fragment,                     UpdateMeasurementFragment → AddMeasurementScreen
    MeasurementItemClickListener                     SettingsFragment → SettingsScreen
AddCustomerFragment : Fragment                       MetricsReferenceFragment → MetricsReferenceScreen
UpdateCustomerFragment : Fragment                    ClassReferenceFragment → ClassReferenceScreen
                                                     CreateClassFragment → CreateClassScreen
```

Compose-экраны stateless: получают данные и колбэки, состояние — `remember`
внутри экрана; флаг `saving` — `mutableStateOf` в поле фрагмента; подписки на `LiveData` —
`observeAsState` в `setContent`.

RecyclerView — 2 комплекта «Adapter → ViewHolder → ClickListener»:

| Adapter | ViewHolder | Click |
|---|---|---|
| `CustomersRecyclerAdapter` | `CustomerRecyclerViewHolder` | `CustomerClickListener` (интерфейс) |
| `MeasurementRecyclerAdapter` | `MeasurementViewHolder` | `MeasurementItemClickListener` (интерфейс) |

Валидация — не классы, а top-level функции в `ui/fragments/customer/CustomerInput.kt`:
`parseBalance` (0…1 000 000 000, пусто → 0), `parsePhone` (ровно 11 цифр, пусто → ""),
константы `MAX_BALANCE`, `PHONE_DIGITS`.

Общие соглашения: ViewBinding в поле `_binding` + getter, обнуление в `onDestroyView`;
подписки и обработчики — в `onViewCreated`, наблюдение только через `viewLifecycleOwner`;
вся навигация — Safe Args.

## 6. Поток вызова

```
клик → Fragment / Composable → vm.method() → viewModelScope.launch → Repository → suspend DAO → Room
                              ↓
              LiveData (postValue) / Channel → Flow (событие «сделано»)
                              ↓
     observe(viewLifecycleOwner) / observeAsState → пересобрать Adapter / navigate / Toast / popBackStack
```

## 7. Граф экранов и переходы

```
                     [CustomersListFragment] ★ старт
                       │ FAB                      │ меню «Настройки»
        ┌──────────────┘                          └──> [SettingsFragment]
        ▼                                                   ├──> [MetricsReferenceFragment]
[AddCustomerFragment]                                       └──> [ClassReferenceFragment]
        │ insert → popUpTo(customersList)                          │ FAB «+»
        └──> [CustomerCardFragment *customerEntityId]              ▼
                   │ FAB (карандаш) ──> [UpdateCustomerFragment *customerEntityId]
                   │ «снять мерки» ───> [AddMeasurementFragment *customerId]
                   └ клик по замеру ──> [CardMeasurementFragment *measurementId]
                                              │ FAB «Редактировать»
                                              ▼
                                    [UpdateMeasurementFragment *measurementId]

[ClassReferenceFragment] ──FAB «+»──> [CreateClassFragment]
        ▲                                        │ save → popUpTo(classReference)
        └────────────────────────────────────────┘

[UpdateCustomerFragment]  Save / Cancel → [CustomerCard] (popUpTo customersList)
                          Delete → [CustomersList]
[AddMeasurementFragment]  Cancel → navigateUp;  Save → insert + popBackStack
[CardMeasurementFragment] переходов нет, кроме FAB «Редактировать»
[UpdateMeasurementFragment] Save → update + popBackStack;  Cancel → navigateUp
[MetricsReferenceFragment] FAB «+» открывает диалог CreateMetricDialog (не экран, не в графе)
```

Аргументы передаются только `Long`: `customerEntityId`, `customerId`, `measurementId`;
сущности между экранами не передаются — карточка достаёт данные из БД по id.
id назначений в `navigation.xml` не совпадают с именами классов: `customerFragment` —
это `CustomerCardFragment`, `metricsReferenceFragment` — `MetricsReferenceFragment`,
`classReferenceFragment` — `ClassReferenceFragment`.

Объявлены в `navigation.xml`, но нигде не вызываются:

- `action_customerCard_to_customersList`
- `action_addMeasurement_to_customerCard`
- `action_addMeasurement_to_cardMeasurement`
- `action_cardMeasurement_to_cardCustomer`
- `action_updateMeasurement_to_cardMeasurement`

## 8. Состав экранов

| Экран | Технология | ViewModel | Состояние | Из чего состоит |
|---|---|---|---|---|
| `CustomersListFragment` | XML + ViewBinding + MenuProvider | SeamstressViewModel | `getAllCustomers: LiveData` | RecyclerView (vertical) + FAB + `menu_customers_list` |
| `CustomerCardFragment` | XML + ViewBinding | SeamstressViewModel + MeasurementsViewModel | две LiveData | `item_customer` (include) + RecyclerView (horizontal) + FAB; кнопка «новое изделие» скрыта (`GONE`) |
| `AddCustomerFragment` | XML + ViewBinding | SeamstressViewModel | `Channel<Long> newCustomerId` | 3 поля ввода, Save/Cancel, ошибки в TextInputLayout, блокировка Save |
| `UpdateCustomerFragment` | XML + ViewBinding | SeamstressViewModel | `customer: Customers?`, `customerId` | те же поля + Save/Cancel/Delete + AlertDialog |
| `AddMeasurementFragment` | Compose в ComposeView | MeasurementsViewModel | `saving` в поле фрагмента, форма — `remember` | дата (DatePicker), название, класс (AlertDialog с RadioButton), значения по метрикам класса, доп. метрики из справочника |
| `CardMeasurementFragment` | Compose в ComposeView | MeasurementsViewModel | две LiveData + `measurement: MeasurementRow?` | Card «дата · название · класс» + LazyColumn «мерка — значение» + FAB «Редактировать» |
| `UpdateMeasurementFragment` | Compose в ComposeView | MeasurementsViewModel | две LiveData + `saving` | тот же `AddMeasurementScreen` с предзаполнением (`initialDate/Name/ClassId/Values`) |
| `SettingsFragment` | Compose | — | — | Column из двух `SettingsItem`: мерки, классы |
| `MetricsReferenceFragment` | Compose + `runtime-livedata` | MetricViewModel | `observeAsState` | LazyColumn `Metric`: shortName / name / description + FAB «+» → `CreateMetricDialog` |
| `ClassReferenceFragment` | Compose + `runtime-livedata` | DressClassViewModel | `observeAsState` | LazyColumn: класс + «N метрик» + FAB «+» |
| `CreateClassFragment` | Compose + `runtime-livedata` | DressClassViewModel + MetricViewModel | `saving` + `remember` | поле названия, чекбоксы всех метрик, выбранные с порядком ↑↓, Save/Cancel |

Не экран: `CreateMetricDialog` (`private @Composable` в `MetricsReferenceScreen.kt`) —
AlertDialog с полями shortName / name / description; Save активен, когда короткое и полное
имя не пустые; пишет через `MetricViewModel.insertMetric`, закрывается по `onDismiss`.

Экран «Настройки» доступен только из меню списка клиентов. Справочники — листы
(только назад системной кнопкой), кроме пары «классы → новый класс».

## 9. Отклонения и мёртвый код по факту

- ViewModel на фрагмент (`ViewModelProvider(this)`): у каждого экрана свой экземпляр и свой
  вызов `getDatabase`; при возврате назад ViewModel переживает пересоздание View, но данные
  перезапрашиваются в `onViewCreated`.
- Слой данных — прямой прокси к DAO; сущности и projection-строки уходят в UI без маппинга.
  JOIN с `metrics` сделан в `TakenMeasurementDao` (карточка печатает shortName / name / value),
  но `Metric` и `TakenMeasurement` по-прежнему не связаны на уровне Room.
- `Customers.img` игнорируется: во всех местах ставится `R.drawable.ic_baseline_add_a_photo_24`.
- `pre_seamstress` + `fallbackToDestructiveMigration(true)`: при смене версии база на
  устройстве пересоздаётся пустой.
- `CustomersDao.getCustomerById` возвращает non-null `Customers`: удалённый клиент даст
  исключение в `CustomerCardFragment` / `UpdateCustomerFragment`. `getMeasurementById` уже
  nullable, и карточка замера печатает `measurement_not_found`.
- Пустые поля мерок сохраняются как `0f` (`requiredIds.map { id -> id to (values[id].toMeasure() ?: 0f) }`):
  «не снимал» и «ноль» в БД неразличимы.
- `saving` живёт в поле фрагмента, а не в ViewModel, и не сбрасывается при ошибке записи:
  неудачная вставка оставляет кнопку Save заблокированной до пересоздания экрана.
- `AddCustomerFragment` после неудачной вставки тоже не возвращает кнопку Save в активное
  состояние; `UpdateCustomerFragment` показывает хардкод-тост `"success update"` и переходит
  сразу после вызова `updateCustomer`, не дожидаясь записи.
- `Metric.typeId` всегда `1` — типа метрики в UI нет.
- Мёртвое: `ui/fragments/notused/` (7 фрагментов и 7 их layout'ов, в графе отсутствуют),
  `newInstance()` во всех фрагментах (не вызываются, местами с неверным типом возврата),
  `fragment_update_measurement.xml` (экран давно на Compose), `domain/dress/Dress.kt`,
  `DressRepo.kt`, `DressSources.kt`, `App`, неиспользуемые `getMetricById` и `getClassById`.
- Адаптеры списков пересоздаются на каждую эмиссию LiveData (без `ListAdapter`/`DiffUtil`).

## 10. Сверка с доской архитектуры (`design/figma-arch/code.js`)

| Узел | По доске | В коде | Статус |
|---|---|---|---|
| `CustomersListFragment` | XML, RecyclerView + FAB, `menu_customers_list` | так же | совпадает |
| `AddCustomerFragment` | XML, имя/телефон/баланс, блокировка Save | так же | совпадает |
| `UpdateCustomerFragment` | XML, Save/Cancel → карточка, Delete → список | так же | совпадает |
| `CustomerCardFragment` | кнопка «новое изделие», скролл изделий клиента | кнопка скрыта (`GONE`), списка изделий нет | расхождение |
| `SettingsFragment` | `SettingsScreen(onMetricsClick)`, один пункт | два пункта: мерки + классы (`onClassesClick`) | расхождение |
| `MetricsReferenceFragment` | список + создание метрики | так же (+ диалог `CreateMetricDialog`) | совпадает |
| `ClassReferenceFragment` | Compose, `DressClassViewModel`, FAB «+» | так же | совпадает |
| `CreateClassFragment` | Compose, название + метрики с порядком | так же | совпадает |
| `AddMeasurementFragment` | дата, название, класс, метрики класса, доп. метрики | так же | совпадает |
| `CardMeasurementFragment` | Compose, `measurementById` + `takenMeasurements`, FAB «Редактировать» | так же | совпадает |
| `UpdateMeasurementFragment` | Compose, форма = `AddMeasurementScreen`, предзаполнение | так же | совпадает |
| `CreateMetricDialog` | AlertDialog в `MetricsReferenceScreen`, не экран | так же | совпадает |
| Переходы (17 связей) | 12 сплошных + 5 пунктирных | все действия и источники данных есть | совпадает |
| Мёртвые действия | 5 | те же 5 | совпадает |
| Слои: 11 экранов + диалог, 4 VM, 4 репозитория | 11 / 4 / 4 | 11 / 4 / 4 | совпадает |
