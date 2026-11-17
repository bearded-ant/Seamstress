# Seamstress — план работ

Состояние на 2026-11-17. Этапы 1–4 закрыты. Схему БД менять можно свободно:
миграции не пишем (`fallbackToDestructiveMigration`), но `exportSchema = true` —
схемы в `app/schemas`, ассет `app/src/main/assets/pre_seamstress` пересобираем
при каждом изменении сущностей (сейчас Room `version = 6`).

## Уже сделано

- [x] `.gitignore`, `gradle/wrapper/*`, adaptive-иконки в репозитории (коммит `5ce2a91`).
- [x] Этап 1, п.1: проверка ввода и защита от двойного нажатия в формах клиента
      (`parseBalance`, `toFloatOrNull`, `inputType`, блокировка кнопки, пустой телефон → null).
- [x] Этап 1, п.2: `newCustomerId` — `Channel` + `Flow` вместо `MutableLiveData`,
      подписка в `onViewCreated` на `viewLifecycleOwner.lifecycleScope`.
- [x] Этап 1, п.3: ViewModel на каждый экран, `onDestroyView`, `viewLifecycleOwner`,
      обработчики вынесены из наблюдателей.
- [x] Настройки приложения: `SettingsFragment` + «Справочник мерок» на Compose.
- [x] Поворот экрана: сначала был зафиксирован `screenOrientation="portrait"`, шаг 6 этапа 3
      его снял (edge-to-edge, инсеты).
- [x] Баланс переведён на `Long` (целые рубли, лимит 1 000 000 000), колонка в ассете —
      `INTEGER`, телефоны в ассете приведены к 11 цифрам. БД поднята до `version = 2`
      с `fallbackToDestructiveMigration(true)`.
- [x] Этап 1, п.7: сохранение нового замера в БД.

## Этап 1. Исправления (стабильность)

Порядок — сверху вниз, каждый пункт самодостаточен.

- [x] **Ввод и двойное нажатие**
   Баланс — целое число рублей `0..1 000 000 000` (`Long`, колонка INTEGER), телефон — ровно
   11 цифр, имя не пустое; блокировка кнопки «Сохранить» до ответа базы. Строки полей
   ограничены цифровым вводом (`inputType="number"`, `maxLength`).
   *Готово, когда:* пустой баланс сохраняется как 0, двойной тап даёт один переход.

- [x] **Одноразовые события для навигации**
   Вынести подписку на результат из `onClick` (`AddCustomerFragment.kt:49`) в `onViewCreated`;
   `_newCustomerId` → `Channel`/`Event` вместо `MutableLiveData`.
   *Готово, когда:* поворот и возврат «назад» не повторяют переход.

- [x] **Жизненный цикл экранов**
   `_binding = null` перенесён в `onDestroyView` на всех экранах; подписки и обработчики —
   в `onViewCreated`, наблюдение только через `viewLifecycleOwner`. ViewModel — свой на каждый
   экран (`ViewModelProvider(this)`): Activity-scoped вариант из `AGENTS.md` отменён, потому что
   общий `selectByIdCustomerLiveData` отдавал в экран редактирования данные другого клиента.
   `AGENTS.md` приведён в соответствие.
   *Готово, когда:* нет `requireActivity()` в роли `LifecycleOwner`, поворот не плодит экраны.

- [x] **Безопасное чтение**
   Одиночные DAO-запросы → nullable (`CustomersDao.kt:13`, `MeasurementDao.kt:12`,
   `MetricDao.kt:13`); при отсутствии записи — тост и навигация назад; `postValue` → `value`.
   *Готово, когда:* открытие карточки удалённого клиента не падает.
   Сделано: `getCustomerById`: `Customers?`, `selectByIdCustomerLiveData`: `LiveData<Customers?>`,
   тост «Клиент не найден» + `popBackStack()` в карточке и в редактировании.

- [x] **Удаление без сирот**
   Удаление клиента чистит замеры и `taken_measurements` (`@Transaction` в DAO);
   после save/delete — `popUpTo` в действиях `navigation.xml`.
   *Готово, когда:* back не ведёт на удалённое, в базе нет висячих строк.
   Сделано: `CustomersDao.deleteCustomerWithData` (`@Transaction`), `popUpTo` у
   `action_updateCustomer_to_customersList`.

- [x] **Карточка замера**
   JOIN с `metrics` вместо `metricId` (`TakenMeasuredViewHolder.kt:17`);
   FAB `fr_card_measurement_floatingActionButton` либо работает, либо убран.
   *Готово, когда:* в списке «Обхват груди — 92».
   Сделано раньше: `CardMeasurementScreen` на Compose с JOIN, FAB ведёт в редактирование.

- [x] **Экран нового замера**
   Дата, название, класс изделия, значения по метрикам класса и доп. метрики из справочника;
   запись `measurements` + `taken_measurements` одной транзакцией (`insertMeasurementWithTaken`),
   кнопка блокируется, после записи — возврат в карточку клиента.
   *Готово, когда:* сохранённый замер появляется в карточке клиента.

- [x] **Классы изделий и мерки по архитектуре**
   `dress_classes`, `dress_class_metrics` (порядок), `measurements.class_id`;
   ассет пересобран, БД `version = 3`. Справочник классов и создание класса (Compose),
   создание метрики в справочнике, карточка замера на Compose с JOIN `metrics`.
   *Готово, когда:* замер хранит значения мерок, карточка показывает «краткое имя — значение».

## Этап 2. Улучшения — закрыт 2026-11-17

- [x] **Схема БД на вырост** (`7cd3389`)
   FK `ON DELETE CASCADE` и индексы: `measurements.customer_id`, `taken_measurements`
   (`measured_id`/`metric_id`/`customer_id`); у таблиц изделий — с этапа 4.
   `exportSchema = true`, схемы в `app/schemas` (`5.json`, `6.json`), ассет пересобран.
   Деньги — `Long` в целых рублях (пункт про копейки отменён). Сейчас Room `version = 6`.
   *Критерий выполнен:* `PRAGMA foreign_key_check` пуст, схемы Room в репозитории.

- [x] **Слой данных** (`29819ff`)
   Каркас метрик класса — Room `Flow` → `StateFlow` (`flatMapLatest`) в трёх ViewModel;
   клиент по id — Room `LiveData` вместо ручной `MutableLiveData`. Репозитории — suspend,
   ViewModel на каждый экран. «Загрузка / не найдено» — `observeLoaded`, пустота —
   пустые состояния экранов (отдельный `UiState`-класс не вводился). Обёртка `Result`
   не добавлена: UI ошибок её не потребляет. `formState` остаётся на `MutableLiveData` —
   это состояние формы, не Room-запрос.

- [x] **Навигация**
   Back stack: список → карточка → форма/замер/изделие; после сохранения `popBackStack()`,
   после удаления — `popUpTo`/replace. Пустые состояния: клиенты (`6cae32a`), замеры,
   изделия, наборы, классы, мерки. `newInstance()` исчез вместе с фрагментами (этап 3).

- [x] **UI/UX** (`9271c46`, `460a15b`)
   Маска телефона «+7 (998) 765-43-21», деньги «1 234 567 ₽» (`formatPhone`/`formatMoney`).
   Edge-to-edge и инсеты — этап 3, шаг 6. Строки в `strings.xml`. Фото клиента —
   `PickVisualMedia` + копия в `filesDir/customer_photos`, `Customers.img: String?` (путь),
   общий компонент `PhotoImage`. `DiffUtil` отменён — адаптеров нет.

- [x] **Приватность** (`c3fdfb1`)
   `dataExtractionRules` (API 31+) и `fullBackupContent` (26–30): БД, `dress_photos/` и
   `customer_photos/` исключены из облачного бэкапа и переноса устройства.

- [x] **Качество** (`c1eac2c`, `51c7609`)
   `CascadeDeleteTest` (androidTest, 4 теста: клиент/изделие/замер/набор) — компилируется,
   прогон требует устройства. CI `.github/workflows/android.yml`: `assembleDebug` +
   `lintDebug`. lint `abortOnError = true`. Чистка: `ui/fragments/notused/` (7 фрагментов),
   7 `fragment_*.xml`, мёртвые `domain/dress/{Dress,DressSources,DressRepo}.kt`, 6 jpeg,
   `viewBinding` удалён.

## Этап 3. Миграция UI на Jetpack Compose

Цель — декларативный UI и единый `NavHost`, без XML-разметки, ViewBinding, адаптеров
и Safe Args. Среды хватает: `compileSdk 37` + AGP 9.5.1 — это требования Compose 1.12+.
Миграция по экрану, проект собирается и работает на каждом шаге.

- [x] **Шаг 0. Инфраструктура**
  `androidx.compose:compose-bom:2025.09.00` (эпоха Kotlin 2.2.10, чтобы метаданные совпали),
  `ui`, `ui-tooling-preview`, `material3`, `runtime-livedata`, `ui-tooling` (debug);
  плагин `org.jetbrains.kotlin.plugin.compose:2.2.10` — ровно столько несёт AGP 9.5.1,
  `buildFeatures { compose = true }`. XML-экраны продолжают работать.
  *Готово, когда:* проект собирается — `assembleDebug` прошёл.

- [x] **Шаг 1. Пилот — Compose на настройках и замере**
  Шестерёнка в тулбаре списка клиентов → `SettingsScreen` → «Справочник мерок»
  (`MetricsReferenceScreen`, только чтение, данные из `MetricViewModel`). Инфраструктура
  обкатана: `ComposeView`, `SeamstressTheme` (Material3), меню через `MenuProvider`.
  Экран «Новый замер» в карточке клиента сведён к форме имени и типа изделия: справочник
  мерок к клиенту не относится. Сохранение — заглушка до этапа 1, п.7.
  Удалены `fragment_add_measurement.xml`, `item_metrics_list.xml`, `MetricRecyclerAdapter`,
  `MetricViewHolder`.
  *Готово, когда:* экран работает и навигация остаётся прежней, XML этого экрана удалён.

- [x] **Шаг 2. Список клиентов и карточка клиента**
  `LazyColumn` вместо `RecyclerView` + `CustomersRecyclerAdapter`/`CustomerRecyclerViewHolder`
  и `MeasurementRecyclerAdapter`/`MeasurementViewHolder`; удалить layout-файлы
  (`fragment_customers_list`, `fragment_card_customer`, `item_customer`, `item_list_customer`,
  `item_list_measurement`). `CustomersListScreen` и `CustomerCardScreen` — stateless,
  фрагменты только собирают `ComposeView` и навигацию; горизонтальный `LazyRow` замеров.
  *Готово, когда:* список и карточка полностью на Compose, адаптеры удалены.
  Сделано: 6 классов recycler'а и 5 layout'ов удалены, `assembleDebug` зелёный.

- [x] **Шаг 3. Формы «Новый клиент» и «Редактирование»**
  Текстовые поля с `error` и лимитами (11 цифр, баланс до 1 000 000 000), блокировка кнопки
  на время записи; `parseBalance`/`parsePhone` переезжают в ViewModel/UiState.
  *Готово, когда:* обе формы на Compose, XML удалён, валидация та же.
  Сделано: общий `CustomerFormScreen`, состояние формы — `CustomerFormState` в
  `SeamstressViewModel` (`formState`: поля, ошибки, `saving`), `parseBalance`/`parsePhone`
  и фильтры ввода — в `CustomerFormState.kt`; `saveCustomer(id)` пишет в БД, навигация — по
  событиям `newCustomerId`/`customerSaved`, ошибка записи разблокирует кнопку и показывает тост.
  Удалены `fragment_add_customer.xml`, `fragment_update_customer.xml`, `CustomerInput.kt`.

- [x] **Шаг 4. Карточка замера**
  Список мерок через `LazyColumn`; попутно JOIN с `metrics` (этап 1, п.6).
  *Готово, когда:* экран на Compose, `TakenMeasuredRecyclerAdapter` удалён.
  Сделано: `CardMeasurementScreen` (Card «дата · название · класс», `LazyColumn` «мерка —
  значение», пустое состояние, FAB «Редактировать»), JOIN в `TakenMeasurementDao`; адаптера
  в проекте нет. Добавлены `loaded` и обработка отсутствующего замера — тост «Замер не найден»
  и `popBackStack`, как в карточке клиента.

- [x] **Шаг 5. Схлопывание оболочки**
  `MainActivity` с `NavHost` Compose, типизированные маршруты вместо `navigation.xml`
  и Safe Args; удалить фрагменты, `Navigation`-fragment-зависимости, `viewBinding`,
  `fragment_*` layout'ы.
  *Готово, когда:* в проекте нет ни одного XML-layout'а и ни одного фрагмента.
  Сделано: `MainActivity` — `setContent { SeamstressTheme { SeamstressNavHost() } }`;
  `ui/navigation/Routes.kt` — `@Serializable`-маршруты, три `NavGraphBuilder`-расширения;
  `ui/screens/*` — stateless-экраны, каркас — `ScreenScaffold` (TopAppBar + «назад»),
  загрузка/не-найдено — `observeLoaded()`. Плагин Safe Args, `navigation.xml`,
  `menu_customers_list.xml`, `activity_main.xml`, `fragment_update_measurement.xml`,
  11 фрагментов и пакеты `ui/fragments/{customer,measurement,settings}` удалены;
  ViewModel — через `viewModel()`, событие `customerDeleted` для перехода после удаления.
  **Остаток:** `viewBinding = true` и 7 `fragment_*.xml` держатся только мёртвым
  `ui/fragments/notused/` — удалять его без явной просьбы нельзя.
  *(Снято при чистке этапа 2: `notused/`, layout'ы и `viewBinding` удалены.)*

- [x] **Шаг 6. Свобода вёрстки**
  Снять `android:screenOrientation="portrait"`, включить `enableEdgeToEdge()` и обработать
  инсеты (этап 2, п.4). После Compose обрезка формы не возвращается.
  *Готово, когда:* поворот и жесты работают, содержимое не уезжает под системные бары.
  Сделано: `portrait` снят, в манифесте `windowSoftInputMode="adjustResize"`;
  `MainActivity` зовёт `enableEdgeToEdge()` (`statusBarStyle = dark(TRANSPARENT)` — светлые
  иконки на фиолетовом тулбаре, навигационная панель — прозрачная с тёмными иконками).
  `ScreenScaffold` красит фон, `TopAppBar` уходит под статус-бар, содержимому даны
  `safeDrawing` (низ + горизонталь, для вырезов в повороте) и `imePadding()`.

**Что удаляется по итогу:** 20 файлов `res/layout`, 6 классов recycler'а, `navigation.xml`,
Safe Args, ViewBinding, `Fragment`-обвязка (`_binding`, `onDestroyView`, `viewLifecycleOwner`).

**Что в миграцию не входит:** схема и связи БД (этап 2, п.1), удаление с каскадом
(этап 1, п.5), фича сохранения замера, приватность бэкапа. Это отдельные работы, UI-тулкитом
не решаются.

**Сроки (один разработчик, знает Compose):** шаг 0–1 — 1 день, шаги 2–4 — 2–3 дня,
шаги 5–6 — 1 день. Итого 4–5 рабочих дней на паритет.

**Риски:** типизированная навигация вместо Safe Args (граф пишется заново), `TextField`
и клавиатура в формах, edge-to-edge. Смягчение — миграция по одному экрану и работающее
приложение после каждого шага.

## Этап 4. Изделия (Dress) — закрыт 2026-11-16

Модель зафиксирована по итогам обсуждения 2026-11-16 (доска user flow
`Qeh8kPXdTvayMTGsdD4I5s`, состав карточки — `EMWC2NorjgmYMKZ8NYPHSU` узел 2:3423).

### Модель данных

```
dress(id, name, class_id, customer_id, price INTEGER NULL, created_at TEXT)
dress_metric(id, dress_id, metric_id, value REAL)        -- значения изделия, всегда копия
dress_photo(id, dress_id, path TEXT)                     -- файлы в app storage, не drawable
dress_comment(id, dress_id, text TEXT, created_at TEXT)
standard_set(id, name TEXT)                              -- именной набор размеров («мужчина 54»)
standard_metric(id, set_id, metric_id, value REAL)       -- значения метрик внутри набора
```

Правила:
- Мерки **не привязаны к изделию ссылкой** — только копия значений; источник копии не помним.
- Наполнение `dress_metric` при создании/правке — три источника:
  1. снять новые прямо в форме изделия;
  2. взять готовый замер клиента → копировать его `taken_measurements`;
  3. общие мерки → выбрать именной набор `standard_set` → копировать его `standard_metric`.
- Каркас набора метрик — `dress_class_metrics` класса изделия (класс = базовый шаблон,
  перевыбирается); значения подставляются из выбранного источника.
- «Изделие без мерок» = пустой `dress_metric`.
- Несколько изделий по одним меркам = несколько независимых копий одного замера.
- `measurement` остаётся отдельной сущностью (замеры клиента), изделием не становится.

### Шаг 1. Room v4 — готово (`7ce4b01`, `76c7ec2`)
Шесть таблиц + DAO + репозитории + ViewModel (`DressViewModel`, `StandardSetViewModel`).
Транзакции: создание изделия вместе с metrics/photos/comments; удаление изделия каскадом.
Конвертация ассета `pre_seamstress`: каждый существующий `measurements` → `dress`
(то же имя/класс/клиент), его `taken_measurements` → `dress_metric`; версия БД 4,
`fallbackToDestructiveMigration` как сейчас. Удалить мёртвые `domain/dress/Dress.kt`
и `DressSources.kt` (используются только мёртвым `ui/fragments/notused/` — трогать
вместе с ним или оставить до чистки).
*Готово, когда:* схема компилируется KSP, ассет содержит изделия по числу замеров.
**Сделано:** сущности в `domain/dresses/` и `domain/standards/`, в ассете 6 изделий и
20 `dress_metric`; мёртвые `Dress.kt`/`DressSources.kt`/`DressRepo.kt` удалены при чистке этапа 2.

### Шаг 2. Общие мерки (справочник именных наборов) — готово (`76c7ec2`, мок `84054f9`)
`StandardMetricsScreen` в настройках: список именных наборов `standard_set`
(«мужчина 54», «женщина 48»…). `create`/`edit` — форма набора: имя + значения метрик
(метрики берутся из `MetricsReferenceScreen`, каркас — из класса или все). Удаление набора.
Форма изделия в пункте «общие мерки» выбирает набор и копирует его `standard_metric`.
На доске: Settings → StandardMetricsScreen (сплошная), StandardMetricsScreen → DressForm
(пунктир, выбор набора), MetricsReferenceScreen → StandardMetricsScreen (пунктир, определения метрик).
*Готово, когда:* набор создаётся/правится, значения копируются в форму изделия.
**Сделано:** `StandardMetricsScreen` (список) + `StandardSetFormScreen` (создание/правка,
каркас — класс или весь справочник, удаление с подтверждением); копирование набора в форму
изделия подключено в шаге 3. Мок в ассете: «Мужчина 54» (12 метрик), «Женщина 48» (18).

### Шаг 3. DressGraph и экраны — готово (`2a640d1`)
Маршруты (аргументы только Long): `DressCard(dressId)`, `DressForm(dressId?)` —
create/edit одним экраном (как CustomerFormScreen). Состав карточки по описанию:
название · фото-слайдер (загрузить/снять камерой/удалить) · связанные мерки · стоимость ·
комментарии (добавить/удалить) · FAB «Редактировать». Дата создания — служебное поле,
в форме не редактируется.
*Готово, когда:* цикл клиент → изделие → мерки проходит на устройстве.
**Сделано:** `DressCardScreen` (фото-слайдер `HorizontalPager`, загрузка `PickVisualMedia`,
камера `TakePicture` + `FileProvider`, комментарии, цена) и `DressFormScreen` (три источника
значений: вручную / замер клиента / именной набор); маршруты `DressCard`/`AddDress`/`UpdateDress`.
Фото: оригиналы без сжатия, даунсемплинг до 1600px при показе, без лимита числа и кэша превью.

### Шаг 4. Интеграция с карточкой клиента — готово (`ca71b43`)
Скролл изделий вместо скрытой заглушки `item_customer_btn_add_dress`: список
`dress` по `customer_id`, FAB/строка «новое изделие» → `DressForm(null)`.
Карточка клиента уже требует это по доске («новое изделие», «скролл изделий клиента»).
*Готово, когда:* заглушка удалена, переходы соответствуют доске.
**Сделано:** секция «Изделия» в карточке клиента (`LazyRow` плиток + «новое изделие»),
переходы по доске; файлы фото изделий удаляются при удалении изделия и клиента.

### Исправление: ассет `pre_seamstress` под Room v7 — готово (`879ab7e`)
Первый запуск падал: `IllegalStateException: Room cannot verify the data integrity`
(ожидался `7cb4d437…`, а в ассете был `348328e9…` от схемы v6). Проверка достигаемых
`onOpen` не перекрывается `fallbackToDestructiveMigration`.
*Сделано:* ассет пересобран через Room 2.8.5 по текущей схеме v7 и заполнен
справочниками и мок-данными (4 клиента, 6 изделий, 6 замеров, 20 мерок изделий,
2 стандартных набора). Проверено на эмуляторе: старт, список клиентов,
карточка клиента, карточка изделия — без падений. Код не менялся.
*Правило на будущее:* при смене схемы ассет надо пересобирать тем же Room-ом,
а не править SQL/хеш вручную.

### Открытые вопросы
- Цена: отдельное поле изделия или участвует в `customers.balance`? Пока — просто `price`. **Открыт.**
- Фото: максимум/сжатие, нужен ли кэш превью. **Закрыт в шаге 3** минимальным вариантом
  (оригиналы без сжатия, даунсемплинг при показе, без лимита и кэша).

## Осталось

- Прогнать `connectedDebugAndroidTest` на устройстве/эмуляторе (в среде разработки adb нет).
- Продуктовое решение: цена изделия — отдельное поле или участвует в `customers.balance`.
- `AGENTS.md` устарел (ViewBinding, `notused/`, версия БД 3) — обновить под текущее состояние.
