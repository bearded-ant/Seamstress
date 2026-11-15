# Seamstress — Android-приложение для учёта клиентов и мерок

## Стек
- AGP 9.5.1, Gradle 9.9.1 (wrapper), JDK 17+, Kotlin — встроенный в AGP (плагин `kotlin-android` не применяется).
- compileSdk/targetSdk 37, minSdk 26, Java 17.
- Jetpack: Navigation Compose 2.10.2 (типизированные маршруты), Room 2.8.5 (KSP), Lifecycle 2.11.0, Compose BOM 2025.09.00, kotlinx-serialization.
- Фрагментов, `navigation.xml`, Safe Args и XML-разметки рабочих экранов нет; ViewBinding включён только ради мёртвого `notused/`.
- Один модуль `:app`, applicationId `com.example.seamstress`, namespace задан только в `build.gradle` (в манифесте атрибута `package` нет).

## Сборка
- `./gradlew assembleDebug` (нужны Android SDK platform 37 и build-tools 36.0.0).
- Тестов нет; `androidTest`-зависимости не подключены.

## Структура
```
app/src/main/java/com/example/seamstress/
├── App.kt                  # Application; extension Context.app
├── data/                   # репозитории поверх DAO
├── domain/                 # Room-сущности и DAO + SeamstressDataBase
│   ├── customers/  dress/  measured/{measurements,metric,taken}/
├── ui/MainActivity.kt      # единственная Activity: setContent { SeamstressTheme { SeamstressNavHost() } }
├── ui/navigation/          # Routes.kt (@Serializable-маршруты) + CustomerGraph/MeasurementGraph/SettingsGraph + SeamstressNavHost
├── ui/screens/<feature>/   # stateless Compose-экраны: customer/, measurement/, settings/
├── ui/components/          # ScreenScaffold, observeLoaded, rememberToast, formatMeasure
├── ui/fragments/notused/   # заброшенные XML-экраны (одежда, материалы) — не трогать без запроса
└── viewmodel/              # AndroidViewModel, по одному на сущность
```

## Модель данных (Room, БД `seamstress_db`, version 3, exportSchema = false)
- `Customers(id, name, phone?, img, balance: Long)` — баланс в целых рублях, 0…1 000 000 000.
- `Measurement(id, name, date_creation, class_id, customer_id)` — обычная data-класс, не Parcelable; в граф передаётся только `measurementId: Long`, карточка достаёт замер из БД. Название класса — JOIN с `dress_classes`.
- `DressClass(id, name)` — класс изделия; `DressClassMetric(class_id, metric_id, position)` — метрики класса с порядком снятия.
- `Metric(id, type_id, short_name, name, description?)` — справочник метрик.
- `TakenMeasurement(id, metric_id, measured_id, customer_id, value: Float)` — пишется вместе с замером в `MeasurementDao.insertMeasurementWithTaken` (`@Transaction`).
- БД создаётся через `createFromAsset("pre_seamstress")` — предзаполненная база в `app/src/main/assets/pre_seamstress`. При изменении схемы нужна миграция И обновление ассета; сейчас версия поднята с `fallbackToDestructiveMigration(true)`, поэтому старая база на устройстве пересоздаётся пустой.

## Конвенции кода
- Экраны: stateless `*Screen.kt` в `ui/screens/` (данные и колбэки — параметрами), вызываются из `composable<Маршрут>` в `ui/navigation/`. Своё состояние экрана — `remember` в Composable.
- Навигация: `@Serializable`-маршруты в `ui/navigation/Routes.kt` и `navController.navigate(Маршрут)`; аргументы — только `Long`. Строковых маршрутов, Safe Args и `navigation.xml` нет.
- Каркас экрана — `ScreenScaffold` (Material3 `TopAppBar`, заголовок из `strings.xml`, стрелка «назад» при `onBack != null`); тема задаётся один раз в `MainActivity`.
- ViewModel — `viewModel()` внутри destination-композабла (свой экземпляр на точку возврата); подписки — `observeAsState`/`observeLoaded`, одноразовые события — `LaunchedEffect { flow.collect { … } }`, тосты — `rememberToast()`.
- `observeLoaded()` различает «ещё не пришло» и «нет записи»: комнатный `LiveData` без значения не эмитит, при `null` показывают «не найдено» и уходят назад.
- Формы клиента состояние в экране не держат: поля, ошибки и `saving` — в `CustomerFormState` внутри `SeamstressViewModel` (`formState`), валидация `parseBalance`/`parsePhone` — там же (`viewmodel/CustomerFormState.kt`), экран `CustomerFormScreen` общий для создания и правки.
- RecyclerView и адаптеров `*RecyclerAdapter`/`*ViewHolder`/`*ClickListener` больше нет — списки на Compose (`LazyColumn`/`LazyRow`).
- Репозитории — классы с suspend-методами, вызываются из `viewModelScope.launch`.
- UI-строки и подписи на русском.

## Замечания
- Плагин `kotlin-parcelize` не используется: с AGP 9 (встроенный Kotlin) `@Parcelize` на Room-сущности ломает KSP-обработку.
- Мёртвый код в `ui/fragments/notused/` и `XFragment.newInstance()` — не удалять без явной просьбы; из-за него в сборке остаются `viewBinding` и 7 `fragment_*.xml`.
