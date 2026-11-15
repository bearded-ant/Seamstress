# Seamstress — план работ

Состояние на 2026-11-14. Проект в разработке: схему БД можно менять свободно,
миграции не пишем, но ассет `app/src/main/assets/pre_seamstress` пересобираем при каждом
изменении сущностей.

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

## Этап 2. Улучшения

1. **Схема БД на вырост**
   `@ForeignKey` с `ON DELETE CASCADE` (замер → клиент, taken → замер/метрика),
   индексы по `customer_id`/`measured_id`/`metric_id`, деньги — `Long` в копейках вместо
   `Float`, `exportSchema = true` + папка `app/schemas` в git, обновлённый ассет
   (`wal_checkpoint(TRUNCATE)`, `journal_mode=DELETE`, `room_master_table`).
   *Критерий:* `PRAGMA foreign_key_check` пуст, схема Room в репозитории.

2. **Слой данных**
   Room-запросы → `Flow` вместо ручных `MutableLiveData`; репозитории только `suspend`
   с `Result`; отдельный ViewModel на экран вместо общего на четыре;
   `UiState` (загрузка / пусто / данные / ошибка).

3. **Навигация**
   Убрать `newInstance()`; явный back stack list → card → add/update/замер;
   после сохранения `popBackStack()`, после удаления `popUpTo(list, inclusive = true)`;
   пустые состояния для «нет клиентов» и «нет замеров».

4. **UI/UX**
   Формат денег (руб., 2 знака) и маска телефона; Material3 + `enableEdgeToEdge()`
   с обработкой инсетов (targetSdk 37 требует); все строки и nav-лейблы в `strings.xml`;
   `DiffUtil` в адаптерах; фото клиента через `PickVisualMedia` + копия в `filesDir`
   вместо `img: Int`.

5. **Приватность**
   `allowBackup=false` или `dataExtractionRules`, исключающие БД: сейчас имена и телефоны
   клиентов попадают в облачный бэкап.

6. **Качество**
   Тест на каскадное удаление с Room in-memory (`androidTest`), CI на `assembleDebug` +
   `lintDebug`, lint-конфиг с `abortOnError`; затем уборка `ui/fragments/notused/`,
   `domain/dress/*`, дубля `item_customer.xml` и jpeg в `drawable`.

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
