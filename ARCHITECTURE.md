# Seamstress — архитектура по факту

Состояние кодовой базы на 2026-11-15. Документ фиксирует то, как приложение устроено
сейчас, включая отклонения от конвенций из `AGENTS.md`. Источник — исходники `app/src/main`.

## 1. Слои и зависимости

Односторонние, без DI и без общего скоупа:

```
MainActivity : AppCompatActivity
 └ setContent { SeamstressTheme { SeamstressNavHost() } } — navigation-compose, старт CustomersList

App : Application — пустой (db/repository закомментированы);
                    val Context.app существует, но не используется
ui/theme/SeamstressTheme — Compose Material3, lightColorScheme
ui/components — ScreenScaffold (тулбар), observeLoaded, rememberToast, formatMeasure
ui/navigation — @Serializable-маршруты и NavGraphBuilder-расширения (три графа + NavHost)
ui/screens/* — stateless-Compose экраны, ничего не знают о навигации
ui/fragments/notused — мёртвые XML-фрагменты, в графе не участвуют

ui/navigation/* ──viewModel()──> viewmodel/* : AndroidViewModel (4)
                      └─init: SeamstressDataBase.getDatabase(app) → DAO (5) → Repository (4)
              └──рисует Room-сущности и projection-строки напрямую (DTO/маппинга нет)
```

Ключевое свойство: `viewModel()` внутри `composable` создаёт **свой** экземпляр ViewModel
на точку возврата (NavBackStackEntry), а ViewModel сам открывает БД. Общих состояний
и кэшей между экранами нет; данные перезапрашиваются при каждом новом экземпляре.
Исключение — синглтон `SeamstressDataBase.INSTANCE`.

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
индексов нет; удаление клиента чистит его строки вручную — `CustomersDao.deleteCustomerWithData`
(`@Transaction`: `taken_measurements` → `measurements` → `customers`).

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
                             getCustomerById(→Customers?) / insert(→Long) / delete / update : suspend
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
- `DressClassDao.insertClassWithMetrics` — класс, затем `DressClassMetric` с `position` по порядку;
- `CustomersDao.deleteCustomerWithData` — мерки клиента, его замеры, затем сам клиент.

Списки и одиночные строки отдаются `LiveData` (Room перезапрашивает сам), запись — `suspend`.

## 4. ViewModel

`AndroidViewModel` × 4, по одному на сущность/справочник; внутри `init` — БД → DAO → Repository.

```
SeamstressViewModel
  getAllCustomers: LiveData<List<Customers>>
  formState: LiveData<CustomerFormState> — поля, ошибки, saving (одна форма на add и update)
  _selectByIdCustomerLiveData: MutableLiveData<Customers?> (через value, null — клиента нет)
  Channel<Long> newCustomerId → Flow — новый клиент записан, id для перехода
  Channel<Long> customerSaved → Flow — правка записана
  Channel<Unit> saveFailed → Flow — запись сорвалась, кнопка снова активна
  Channel<Unit> customerDeleted → Flow — удаление завершено
  onNameChange / onPhoneChange / onBalanceChange / fillForm
  saveCustomer(id = 0L) — валидация + insert или update, блокировка через saving
  selectCustomerById / deleteCustomer

MeasurementsViewModel            (самая нагруженная: замеры + классы + метрики)
  allClasses: LiveData<List<DressClassItem>>, allMetrics: LiveData<List<Metric>>
  classMetricsLiveData: MutableLiveData<List<Metric>> (через value, из loadClassMetrics)
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

Асинхронность: `viewModelScope.launch`; чтение — `LiveData` (запись только через `value`,
`postValue` в проекте нет), одноразовые события навигации — `Channel(BUFFERED).receiveAsFlow()`.
Время (`dateCreation`) ставит экран, а не ViewModel.

Валидация формы клиента — в ViewModel: `CustomerFormState.kt` держит `CustomerFormState`
и top-level `parseBalance` (0…1 000 000 000, пусто → 0), `parsePhone` (ровно 11 цифр, пусто → ""),
`filterPhone`/`filterBalance` (только цифры, 11/10 символов) и константы `MAX_BALANCE`.

## 5. UI: экраны и навигация

Фрагментов в рабочем коде нет; XML-фрагменты остались только в мёртвом `notused/`.

```
маршрут (ui/navigation/Routes.kt)   экран (ui/screens/*)         ViewModel
CustomersList                     → CustomersListScreen         SeamstressViewModel
CustomerCard(customerId)          → CustomerCardScreen          SeamstressViewModel + MeasurementsViewModel
AddCustomer                       → CustomerFormScreen          SeamstressViewModel
UpdateCustomer(customerId)        → CustomerFormScreen          SeamstressViewModel
AddMeasurement(customerId)        → AddMeasurementScreen        MeasurementsViewModel
CardMeasurement(measurementId)    → CardMeasurementScreen       MeasurementsViewModel
UpdateMeasurement(measurementId)  → AddMeasurementScreen        MeasurementsViewModel
Settings                          → SettingsScreen              —
MetricsReference                  → MetricsReferenceScreen      MetricViewModel
ClassReference                    → ClassReferenceScreen        DressClassViewModel
CreateClass                       → CreateClassScreen            DressClassViewModel + MetricViewModel
```

Маршруты — `@Serializable`-объекты и data-классы (только `Long`-аргументы), старт — `CustomersList`.
`SeamstressNavHost` собирает три `NavGraphBuilder`-расширения (`CustomerGraph`, `MeasurementGraph`,
`SettingsGraph`); `navigation.xml`, Safe Args и `menu_customers_list.xml` удалены, шестерёнка
переехала в `TopAppBar` списка клиентов.

Compose-экраны stateless: получают данные и колбэки, своё состояние — `remember`
внутри экрана; подписки на `LiveData` — `observeAsState`, одноразовые события —
`LaunchedEffect { viewModel.xxx.collect { … } }`. Каркас каждого экрана — `ScreenScaffold`
(`TopAppBar` с заголовком из `strings.xml`, стрелкой «назад» при `onBack != null`
и слотами действий). Тема — `SeamstressTheme` в `MainActivity`.

«Ещё не пришло» и «не найдено» различает `LiveData.observeLoaded()` (`ui/components`):
первое значение источника, включая `null`, помечается как `loaded`. Формы клиента состояние
не держат: поля, ошибки и `saving` приходят из `formState` в `SeamstressViewModel`.

RecyclerView в проекте не осталось: списки — `LazyColumn` (клиенты, мерки) и `LazyRow`
(замеры в карточке, плитки 150×150 dp), ключи — id сущностей.

Общие соглашения: навигация — только через `navController.navigate(Маршрут)`; подписки
и обработчики — внутри destination-композабла, чтобы отписка шла с композицией.

## 6. Поток вызова

```
клик → Composable → vm.method() → viewModelScope.launch → Repository → suspend DAO → Room
                              ↓
              LiveData (value) / Channel → Flow (событие «сделано»)
                              ↓
     observeAsState/observeLoaded / LaunchedEffect.collect → пересобрать список / navigate / Toast
```

## 7. Граф экранов и переходы

```
                     [CustomersList] ★ старт
                       │ FAB                      │ шестерёнка в тулбаре
        ┌──────────────┘                          └──> [Settings]
        ▼                                                   ├──> [MetricsReference]
[AddCustomer]                                               └──> [ClassReference]
        │ save → navigate(CustomerCard) { popUpTo<CustomersList> }   │ FAB «+»
        └──> [CustomerCard(customerId)]                             ▼
                   │ FAB (карандаш) ──> [UpdateCustomer(customerId)]     [CreateClass]
                   │ «снять мерки» ───> [AddMeasurement(customerId)]      │ save → navigate(ClassReference)
                   └ клик по замеру ──> [CardMeasurement(measurementId)]  │      { popUpTo<ClassReference>(inclusive) }
                                              │ FAB «Редактировать»
                                              ▼
                                    [UpdateMeasurement(measurementId)]

[UpdateCustomer]  Save → navigate(CustomerCard) { popUpTo<CustomersList> }
                  Cancel → popBackStack;  Delete → событие customerDeleted → navigate(CustomersList) { popUpTo(inclusive) }
[AddMeasurement]  Save → measurementSaved → popBackStack;  Cancel → popBackStack
[CardMeasurement] FAB «Редактировать» → [UpdateMeasurement]
[UpdateMeasurement] Save → measurementUpdated → popBackStack;  Cancel → popBackStack
[MetricsReference] FAB «+» открывает диалог CreateMetricDialog (не экран, не в графе)
```

Аргументы маршрутов — только `Long` (`customerId`, `measurementId`); сущности между экранами
не передаются — экран достаёт данные из БД по id.

Действий «объявлено, но не вызывается» больше нет: граф описан кодом, лишние маршруты
просто не объявляются.

## 8. Состав экранов

| Маршрут | Composable | ViewModel | Состояние | Из чего состоит |
|---|---|---|---|---|
| `CustomersList` | `CustomersListScreen` | SeamstressViewModel | `getAllCustomers` → `observeAsState` | `LazyColumn` карточек (фото · имя · телефон · баланс), FAB «+», шестерёнка в `TopAppBar` |
| `CustomerCard` | `CustomerCardScreen` | SeamstressViewModel + MeasurementsViewModel | `selectByIdCustomerLiveData` → `observeLoaded`, `measurementsByCustomer` → `observeAsState` | Card «фото · имя · телефон · баланс» + «снять мерки», `LazyRow` плиток замеров, FAB «Редактировать»; кнопки «новое изделие» нет |
| `AddCustomer` | `CustomerFormScreen` | SeamstressViewModel | `formState` → `observeAsState` | фото, поля имя/телефон/баланс с `error`, Save/Cancel, Save выключен при `saving`; переход — по `newCustomerId` |
| `UpdateCustomer` | `CustomerFormScreen` | SeamstressViewModel | `formState` + `selectByIdCustomerLiveData` → `fillForm` | тот же экран + Delete с AlertDialog; Save → `customerSaved` → карточка (`popUpTo<CustomersList>`), Delete → `customerDeleted` → список |
| `AddMeasurement` | `AddMeasurementScreen` | MeasurementsViewModel | `saving` в `remember`, форма — внутри экрана | дата (DatePicker), название, класс (AlertDialog с RadioButton), значения по метрикам класса, доп. метрики из справочника |
| `CardMeasurement` | `CardMeasurementScreen` | MeasurementsViewModel | `measurementById` → `observeLoaded`, `takenMeasurements` → `observeAsState` | Card «дата · название · класс» + LazyColumn «мерка — значение» + FAB «Редактировать» |
| `UpdateMeasurement` | `AddMeasurementScreen` | MeasurementsViewModel | две LiveData → `observeLoaded` + `saving` в `remember` | тот же экран с предзаполнением (`initialDate/Name/ClassId/Values`) |
| `Settings` | `SettingsScreen` | — | — | Column из двух `SettingsItem`: мерки, классы |
| `MetricsReference` | `MetricsReferenceScreen` | MetricViewModel | `observeAsState` | LazyColumn `Metric`: shortName / name / description + FAB «+» → `CreateMetricDialog` |
| `ClassReference` | `ClassReferenceScreen` | DressClassViewModel | `observeAsState` | LazyColumn: класс + «N метрик» + FAB «+» |
| `CreateClass` | `CreateClassScreen` | DressClassViewModel + MetricViewModel | `saving` + `remember` | поле названия, чекбоксы всех метрик, выбранные с порядком ↑↓, Save/Cancel |

Не экран: `CreateMetricDialog` (`private @Composable` в `MetricsReferenceScreen.kt`) —
AlertDialog с полями shortName / name / description; Save активен, когда короткое и полное
имя не пустые; пишет через `MetricViewModel.insertMetric`, закрывается по `onDismiss`.

Экран «Настройки» доступен только из тулбара списка клиентов. Справочники — листы
(только назад системной кнопкой), кроме пары «классы → новый класс».

## 9. Отклонения и мёртвый код по факту

- ViewModel создаётся `viewModel()` в destination-композабле: экземпляр на точку возврата,
  свой вызов `getDatabase`; при пересоздании экрана данные перезапрашиваются.
- Слой данных — прямой прокси к DAO; сущности и projection-строки уходят в UI без маппинга.
  JOIN с `metrics` сделан в `TakenMeasurementDao` (карточка печатает shortName / name / value),
  но `Metric` и `TakenMeasurement` по-прежнему не связаны на уровне Room.
- `Customers.img` игнорируется: во всех местах ставится `R.drawable.ic_baseline_add_a_photo_24`.
- `pre_seamstress` + `fallbackToDestructiveMigration(true)`: при смене версии база на
  устройстве пересоздаётся пустой.
- Удаление клиента — вручную в `@Transaction` (см. §2): каскада на уровне Room нет, при
  рассинхроне схемы и запроса сироты возможны только по новым таблицам.
- `getCustomerById` возвращает `Customers?`, `getMeasurementById` — `MeasurementRow?`:
  на отсутствующей записи все три карточки показывают тост и `popBackStack`:
  карточка клиента и редактирование — «Клиент не найден», карточка замера — «Замер не найден».
- Пустые поля мерок сохраняются как `0f` (`requiredIds.map { id -> id to (values[id].toMeasure() ?: 0f) }`):
  «не снимал» и «ноль» в БД неразличимы.
- `AddMeasurement`/`UpdateMeasurement`: `saving` живёт в `remember` destination-композабла,
  а не в ViewModel, и не сбрасывается при ошибке записи: неудачная вставка оставляет кнопку
  Save заблокированной до ухода с экрана. В формах клиента это не так — `saving` в `formState`,
  ошибка записи разблокирует кнопку и показывает тост «Не удалось сохранить».
- `Metric.typeId` всегда `1` — типа метрики в UI нет.
- `viewBinding = true` остался ради мёртвого `DimensListFragment`; в рабочем коде биндингов нет.
- Мёртвое: `ui/fragments/notused/` (7 фрагментов и 7 их layout'ов, вне графа), `newInstance()`
  в этих фрагментах, `domain/dress/Dress.kt`, `DressRepo.kt`, `DressSources.kt`,
  `App`, неиспользуемые `getMetricById` и `getClassById`.
- Адаптеров нет; `LazyColumn`/`LazyRow` получают список целиком и пересобираются на каждую
  эмиссию LiveData (без `DiffUtil`, ключи — id).

## 10. Сверка с доской архитектуры (`design/figma-arch/code.js`)

Доска называет узлы фрагментами (так было до миграции); в коде это Compose-экраны
и маршруты из §5–§8.

| Узел | По доске | В коде | Статус |
|---|---|---|---|
| `CustomersListFragment` | XML, RecyclerView + FAB, меню | Compose, `LazyColumn` + FAB, иконка в `TopAppBar` | расхождение (технология) |
| `AddCustomerFragment` | XML, имя/телефон/баланс, блокировка Save | Compose, `CustomerFormScreen` + `formState`, блокировка Save при `saving` | расхождение (технология) |
| `UpdateCustomerFragment` | XML, Save/Cancel → карточка, Delete → список | Compose, та же форма + Delete → список (`popUpTo`) | расхождение (технология) |
| `CustomerCardFragment` | кнопка «новое изделие», скролл изделий клиента | Compose-карточка, кнопки «новое изделие» и списка изделий нет | расхождение |
| `SettingsFragment` | `SettingsScreen(onMetricsClick)`, один пункт | два пункта: мерки + классы (`onClassesClick`) | расхождение |
| `MetricsReferenceFragment` | список + создание метрики | так же (+ диалог `CreateMetricDialog`) | совпадает |
| `ClassReferenceFragment` | Compose, `DressClassViewModel`, FAB «+» | так же | совпадает |
| `CreateClassFragment` | Compose, название + метрики с порядком | так же | совпадает |
| `AddMeasurementFragment` | дата, название, класс, метрики класса, доп. метрики | так же | совпадает |
| `CardMeasurementFragment` | Compose, `measurementById` + `takenMeasurements`, FAB «Редактировать» | так же | совпадает |
| `UpdateMeasurementFragment` | Compose, форма = `AddMeasurementScreen`, предзаполнение | так же | совпадает |
| `CreateMetricDialog` | AlertDialog в `MetricsReferenceScreen`, не экран | так же | совпадает |
| Переходы (17 связей) | 12 сплошных + 5 пунктирных | все переходы источников данных есть; лишних маршрутов нет | совпадает |
| Слои: 11 экранов + диалог, 4 VM, 4 репозитория | 11 / 4 / 4 | 11 / 4 / 4 | совпадает |
