# Seamstress — план работ

Состояние на 2026-11-14. Проект в разработке: схему БД можно менять свободно,
миграции не пишем, но ассет `app/src/main/assets/pre_seamstress` пересобираем при каждом
изменении сущностей.

## Уже сделано

- [x] `.gitignore`, `gradle/wrapper/*`, adaptive-иконки в репозитории (коммит `5ce2a91`).

## Этап 1. Исправления (стабильность)

Порядок — сверху вниз, каждый пункт самодостаточен.

1. **Ввод и двойное нажатие**
   `AddCustomerFragment.kt:46`, `UpdateCustomerFragment.kt:51`: парсить баланс через
   `toFloatOrNull() ?: 0f`, `inputType="numberDecimal"`, проверка пустого имени;
   блокировать кнопку «Сохранить» до ответа базы.
   *Готово, когда:* пустой баланс сохраняется как 0, двойной тап даёт один переход.

2. **Одноразовые события для навигации**
   Вынести подписку на результат из `onClick` (`AddCustomerFragment.kt:49`) в `onViewCreated`;
   `_newCustomerId` → `Channel`/`Event` вместо `MutableLiveData`.
   *Готово, когда:* поворот и возврат «назад» не повторяют переход.

3. **Жизненный цикл экранов**
   `_binding = null` из `onDestroy` → `onDestroyView` (`CustomersListFragment.kt:55`,
   `AddMeasurementFragment.kt:47`, `CardMeasurementFragment.kt:58`);
   все `observe(requireActivity())` → `viewLifecycleOwner`;
   ViewModel везде одного уровня (Activity-scoped, как в `AGENTS.md`).
   *Готово, когда:* нет `requireActivity()` в роли `LifecycleOwner`, поворот не плодит экраны.

4. **Безопасное чтение**
   Одиночные DAO-запросы → nullable (`CustomersDao.kt:13`, `MeasurementDao.kt:12`,
   `MetricDao.kt:13`); при отсутствии записи — тост и навигация назад; `postValue` → `value`.
   *Готово, когда:* открытие карточки удалённого клиента не падает.

5. **Удаление без сирот**
   Удаление клиента чистит замеры и `taken_measurements` (`@Transaction` в DAO);
   после save/delete — `popUpTo` в действиях `navigation.xml`.
   *Готово, когда:* back не ведёт на удалённое, в базе нет висячих строк.

6. **Карточка замера**
   JOIN с `metrics` вместо `metricId` (`TakenMeasuredViewHolder.kt:17`);
   FAB `fr_card_measurement_floatingActionButton` либо работает, либо убран.
   *Готово, когда:* в списке «Обхват груди — 92».

7. **Экран нового замера**
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
