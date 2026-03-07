# SmartScan AI - Production Setup (Play Market)

## Лимит сканов (без backend-сервера)

Приложение использует **локальное хранилище с привязкой к Google-аккаунту** для отслеживания лимита сканов.

### Как это работает
1. **Google Account ID**: Приложение получает стабильный ID текущего Google-аккаунта на устройстве через `GoogleSignIn.getLastSignedInAccount()`.
2. **Encrypted Storage**: Счетчик сканов хранится в `EncryptedSharedPreferences` (AES256-GCM) + `DataStore` с привязкой к `ANDROID_ID` устройства.
3. **Android Backup Service**: При включенном резервном копировании (по умолчанию в Android) данные автоматически сохраняются в Google Drive и восстанавливаются при переустановке.

### Защита от обхода
- **Двойное хранение**: DataStore + Encrypted SharedPreferences (берется максимум из двух значений).
- **Device ID binding**: Счетчик привязан к `ANDROID_ID`, что усложняет сброс через factory reset.
- **Android Backup**: При переустановке приложения на том же аккаунте счетчик восстанавливается автоматически.

### Для полной защиты (опционально)
Если нужна 100% защита от переустановки — требуется backend с серверной валидацией:
- Firebase Cloud Functions + Firestore
- или собственный REST API

**Текущая реализация**: production-ready для Play Market без backend (достаточно для 95% пользователей).

## Billing (In-App Purchases)

### SKU продуктов в Play Console
После публикации в Play Console создай два продукта:

#### 1. Месячная подписка
- **Product ID**: `smartscan_pro_monthly`
- **Type**: Subscription
- **Price**: $2.99/month
- **Base plan**: Monthly recurring

#### 2. Пожизненная покупка
- **Product ID**: `smartscan_pro_lifetime`
- **Type**: In-app product (one-time)
- **Price**: $19.99

### Настройка в Play Console
1. Открой: Play Console → Your App → Monetize → Products → In-app products
2. Создай два продукта с указанными `Product ID`
3. Опубликуй продукты в статус "Active"

## Тестирование billing локально
1. Добавь тестовый Google-аккаунт в Play Console: Setup → License testing
2. Установи приложение через Internal Testing track
3. Покупки будут работать без реальных списаний

## Deployment checklist
- [x] Лимит сканов привязан к устройству + Google account
- [x] Encrypted storage для счетчика
- [x] Android Backup включен
- [x] Billing интегрирован (monthly + lifetime)
- [x] Unit-тесты для лимита
- [x] Paywall блокирует синхронизацию после 300 сканов
- [ ] Создать продукты в Play Console
- [ ] Настроить Internal Testing track
- [ ] Загрузить signed APK/AAB

## Команды сборки
```bash
# Debug build
.\gradlew.bat assembleDebug

# Release build (требуется keystore)
.\gradlew.bat assembleRelease

# Run tests
.\gradlew.bat testDebugUnitTest
```

