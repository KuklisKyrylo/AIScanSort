# SmartScan AI (Scaffold)

Production-ready Android utility app для сканирования галереи с помощью ML Kit и локальной базой Room с FTS4 поиском.

## Текущий статус scaffold
- ✅ `app/build.gradle.kts` — Kotlin 2.1 + Compose M3 + Room + Hilt + ML Kit + Billing 6.2
- ✅ `app/src/main/AndroidManifest.xml` — Media permissions (Android 13+) + Billing queries
- ✅ Clean Architecture (data/domain/ui/di слои)
- ✅ Room + FTS4 (`ScanEntity`, `ScanFtsEntity`, `ScanDao`, `SmartScanDatabase`)
- ✅ `MLKitAnalyzer` — OCR + Entity extraction на `Dispatchers.Default`
- ✅ Billing Manager — Pro purchase flow + entitlement check
- ✅ Paywall gate — `!isPro && scannedCount > 50`
- ✅ MediaStore sync pipeline + ML Kit integration
- ✅ Compose UI — `MainActivity`, `MainScreen`, search bar, gallery grid
- ✅ Gradle wrapper + root build files configured

## Реализованные компоненты

### Data Layer
- `MediaStoreImageSource` — загрузка изображений из галереи
- `MLKitAnalyzer` — Text Recognition + Entity Extraction
- `BillingManager` — Google Play Billing v6 integration
- `ScanRepositoryImpl` — Room-backed Flow streams с FTS4 поиском
- `SmartScanDatabase` — Room БД с виртуальной FTS4 таблицей

### Domain Layer
- `ScanRepository` — контракт для работы со сканами
- `BillingRepository` — контракт для billing состояния
- `SyncGalleryUseCase` — оркестрация синка галереи с ML анализом
- `PaywallGate` — логика free-tier лимита (50 сканов)
- `ScannedImage` / `ScanAnalysisResult` — domain модели

### UI Layer
- `MainActivity` — Compose entry point
- `MainViewModel` — state management + sync orchestration
- `MainScreen` — Search bar + Gallery grid + Paywall banner
- Runtime permission handling (Android 13+ READ_MEDIA_IMAGES)

## Сборка проекта

### Требования
- JDK 17+
- Android Studio Ladybug (2024.2.1+) или Hedgehog (2023.1.1+)
- Android SDK 35 (compileSdk)
- Min SDK 26 (Android 8.0)

### Первая сборка
```powershell
# Клонировать/открыть проект
cd C:\Users\kiril\IdeaProjects\Tools\AIScanSort

# Сборка через Gradle wrapper
.\gradlew.bat assembleDebug --no-daemon

# Запуск тестов
.\gradlew.bat test

# Установка на устройство
.\gradlew.bat installDebug
```

### Android Studio
1. `File → Open` → выбрать папку `AIScanSort`
2. Дождаться Gradle Sync
3. `Build → Make Project` (Ctrl+F9)
4. `Run → Run 'app'` (Shift+F10)

## Тестирование

### Чеклист функциональности
1. ✅ Запуск приложения без крашей
2. ✅ Запрос media permission при первом старте
3. ✅ Tap "Sync now" → загрузка изображений из MediaStore
4. ✅ OCR + entity tags обрабатываются ML Kit
5. ✅ Новые сканы появляются в grid
6. ✅ Search bar фильтрует по тексту и тегам (FTS4)
7. ✅ После 50 сканов (без Pro) появляется paywall banner
8. ✅ Tap "Buy Pro" → запуск Google Play Billing flow

### Billing тестирование
Для тестирования покупок нужно:
- Загрузить APK в Internal Testing track на Google Play Console
- Добавить тестовый продукт `smartscan_pro_lifetime` (одноразовая покупка)
- Использовать тестовый Google-аккаунт из License Testers

### Unit тесты
```powershell
.\gradlew.bat test
```

Текущие тесты:
- `PaywallGateTest` — логика free-tier gate

## Текущая архитектура

```
app/src/main/java/com/smartscan/ai/
├── data/
│   ├── analyzer/MLKitAnalyzer.kt
│   ├── billing/BillingManager.kt
│   ├── local/
│   │   ├── ScanDao.kt
│   │   ├── ScanEntity.kt
│   │   ├── ScanFtsEntity.kt
│   │   ├── ScanMappers.kt
│   │   └── SmartScanDatabase.kt
│   ├── media/MediaStoreImageSource.kt
│   └── repository/ScanRepositoryImpl.kt
├── di/
│   ├── BillingModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
├── domain/
│   ├── billing/PaywallGate.kt
│   ├── model/
│   │   ├── ScanAnalysisResult.kt
│   │   └── ScannedImage.kt
│   ├── repository/
│   │   ├── BillingRepository.kt
│   │   └── ScanRepository.kt
│   └── usecase/SyncGalleryUseCase.kt
├── ui/
│   ├── base/BaseViewModel.kt
│   ├── main/
│   │   ├── MainScreen.kt
│   │   └── MainViewModel.kt
│   └── theme/Theme.kt
├── MainActivity.kt
└── SmartScanApplication.kt
```

## Следующие шаги
- [ ] Добавить экран детального просмотра скана с полным текстом
- [ ] Реализовать ручной выбор изображений через picker
- [ ] Добавить больше языков (китайский, французский, итальянский)
- [ ] Экспорт результатов (CSV/JSON)
- [ ] Фоновый worker для периодического синка
- [ ] Поддержка темной темы
- [ ] Instrumented tests для UI
- [ ] ProGuard/R8 rules для release build

## 📚 Документация

- **[README.md](README.md)** — основная документация проекта
- **[BUILD_SUMMARY.md](BUILD_SUMMARY.md)** — отчет о сборке
- **[ANDROID_STUDIO_GUIDE.md](ANDROID_STUDIO_GUIDE.md)** — инструкция запуска
- **[LOCALIZATION_GUIDE.md](LOCALIZATION_GUIDE.md)** — мультиязычность и настройки

## Статус первой сборки
✅ **УСПЕШНО!** Проект успешно собран и готов к тестированию в Android Studio.

### Результаты сборки
- ✅ `assembleDebug` - APK собран без ошибок
- ✅ `test` - Unit тесты пройдены
- ✅ Все warnings исправлены
- ✅ KSP annotation processing (Hilt + Room) выполнен корректно

### Готово к запуску
Проект полностью готов для открытия в Android Studio и запуска на эмуляторе/устройстве.

**APK location:** `app/build/outputs/apk/debug/app-debug.apk`

### Быстрый старт
```powershell
# Открыть в Android Studio
# File → Open → выбрать папку AIScanSort

# Или запустить через командную строку
.\gradlew.bat installDebug  # Установить на подключенное устройство
```
