# Seamstress — Android-приложение для учёта клиентов и мерок

## Стек
- AGP 9.5.1, Gradle 9.9.1 (wrapper), JDK 17+, Kotlin — встроенный в AGP (плагин `kotlin-android` не применяется).
- compileSdk/targetSdk 37, minSdk 26, Java 17.
- Jetpack: Navigation 2.10.2 (Safe Args), Room 2.8.5 (KSP), Lifecycle 2.11.0, ViewBinding.
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
├── ui/MainActivity.kt      # единственная Activity, NavHost
├── ui/fragments/<feature>/ # customer/{list,card,add,update}, measurement/{add,card}
│   └── notused/            # заброшенные экраны (одежда, материалы) — не трогать без запроса
└── viewmodel/              # AndroidViewModel, по одному на сущность
app/src/main/res/navigation/navigation.xml  # граф навигации, старт — customersListFragment
```

## Модель данных (Room, БД `seamstress_db`, version 2, exportSchema = false)
- `Customers(id, name, phone?, img, balance: Long)` — баланс в целых рублях, 0…1 000 000 000.
- `Measurement(id, name, date_creation, dress_type, customer_id)` — обычная data-класс, не Parcelable; в граф передаётся только `measurementId: Long`, карточка достаёт замер из БД.
- `Metric(id, type_id, short_name, name, description?)` — справочник метрик.
- `TakenMeasurement(id, metric_id, measured_id, customer_id, value)`.
- БД создаётся через `createFromAsset("pre_seamstress")` — предзаполненная база в `app/src/main/assets/pre_seamstress`. При изменении схемы нужна миграция И обновление ассета; сейчас версия поднята с `fallbackToDestructiveMigration(true)`, поэтому старая база на устройстве пересоздаётся пустой.

## Конвенции кода
- ViewBinding: `Fragment<Name>Binding`, поле `_binding` + getter `binding`, обнуление в `onDestroyView`.
- Фрагменты получают ViewModel через `ViewModelProvider(this)[XViewModel::class.java]` — свой экземпляр на экран; наблюдение только через `viewLifecycleOwner`, подписки и обработчики — в `onViewCreated`.
- Навигация только через Safe Args (`XFragmentDirections.action...`); id аргументов — в `navigation.xml`.
- RecyclerView: отдельные классы `*RecyclerAdapter`, `*ViewHolder`, `*ClickListener` в подпакете `recycler/`.
- Репозитории — классы с suspend-методами, вызываются из `viewModelScope.launch`.
- UI-строки и подписи на русском.

## Замечания
- Плагин `kotlin-parcelize` не используется: с AGP 9 (встроенный Kotlin) `@Parcelize` на Room-сущности ломает KSP-обработку.
- Мёртвый код в `ui/fragments/notused/` и `XFragment.newInstance()` — не удалять без явной просьбы.
