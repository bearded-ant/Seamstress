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
- [x] Поворот экрана отключён: `android:screenOrientation="portrait"` у `MainActivity`.
- [x] Баланс переведён на `Long` (целые рубли, лимит 1 000 000 000), колонка в ассете —
      `INTEGER`, телефоны в ассете приведены к 11 цифрам. БД поднята до `version = 2`
      с `fallbackToDestructiveMigration(true)`.

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
   *Готово, когда:* нет `requireActivity()` в роли `LifecycleOwner`, поворот не плодит экраны.

- [ ] **Безопасное чтение**
   Одиночные DAO-запросы → nullable (`CustomersDao.kt:13`, `MeasurementDao.kt:12`,
   `MetricDao.kt:13`); при отсутствии записи — тост и навигация назад; `postValue` → `value`.
   *Готово, когда:* открытие карточки удалённого клиента не падает.

- [ ] **Удаление без сирот**
   Удаление клиента чистит замеры и `taken_measurements` (`@Transaction` в DAO);
   после save/delete — `popUpTo` в действиях `navigation.xml`.
   *Готово, когда:* back не ведёт на удалённое, в базе нет висячих строк.

- [ ] **Карточка замера**
   JOIN с `metrics` вместо `metricId` (`TakenMeasuredViewHolder.kt:17`);
   FAB `fr_card_measurement_floatingActionButton` либо работает, либо убран.
   *Готово, когда:* в списке «Обхват груди — 92».

- [ ] **Экран нового замера**
   Связать кнопки (`fragment_add_measurement.xml`) с insert в `measurements` +
   `taken_measurements` одной транзакцией.
   *Готово, когда:* сохранённый замер появляется в карточке клиента.

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
