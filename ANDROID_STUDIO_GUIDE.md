# Быстрый старт в Android Studio

## 1. Открыть проект
1. Запустить **Android Studio**
2. `File → Open`
3. Выбрать папку: `C:\Users\kiril\IdeaProjects\Tools\AIScanSort`
4. Дождаться **Gradle Sync** (~2-3 минуты)

## 2. Проверить конфигурацию
- **JDK:** Должен быть JDK 17 или выше
  - `File → Project Structure → SDK Location → JDK Location`
- **Android SDK:** API 35 должен быть установлен
  - `Tools → SDK Manager → Android 14.0 (API 35)`

## 3. Запустить приложение

### На эмуляторе
1. Создать эмулятор если его нет:
   - `Tools → Device Manager → Create Device`
   - Выбрать: **Pixel 6** или **Pixel 7**
   - System Image: **Android 13 (API 33)** или выше
2. Нажать **▶ Run 'app'** (Shift+F10)

### На реальном устройстве
1. Включить **Developer Options** на устройстве
2. Включить **USB Debugging**
3. Подключить устройство через USB
4. Разрешить отладку при появлении диалога
5. Нажать **▶ Run 'app'** (Shift+F10)

## 4. Первый запуск приложения

### Что произойдет:
1. Приложение запросит разрешение на доступ к фото
2. Нажмите **"Grant"** (Предоставить)
3. Автоматически запустится синхронизация галереи
4. Изображения будут обработаны через ML Kit (OCR)
5. Результаты появятся в grid-списке

### Основные функции:
- **Search bar** — поиск по распознанному тексту и тегам
- **Sync now** — ручной запуск синхронизации галереи
- **Buy Pro** — покупка Pro-версии (требует настройки в Play Console)

## 5. Тестирование функций

### Поиск (FTS4)
1. Добавить несколько изображений с текстом в галерею эмулятора
2. Нажать "Sync now"
3. Ввести текст в Search bar
4. Проверить фильтрацию результатов

### Paywall (Free limit 50)
1. Создать тестовую БД с 51+ записью (через debug injection)
2. Проверить появление красного баннера
3. Нажать "Buy Pro" → должен запуститься billing flow

### ML Kit OCR
1. Добавить изображение со снимком экрана (текст)
2. Нажать "Sync now"
3. Проверить что текст распознан в grid tile
4. Проверить что теги извлечены (email, phone, url и т.д.)

## 6. Просмотр логов

### Logcat фильтры:
```
package:com.smartscan.ai
```

### Ключевые теги для отладки:
- `MLKitAnalyzer` — OCR результаты
- `SyncGalleryUseCase` — статус синхронизации
- `BillingManager` — состояние billing
- `MainViewModel` — UI state changes

## 7. Запуск тестов

### Unit тесты
```bash
# Через Android Studio
Run → Run 'All Tests'

# Через терминал
.\gradlew.bat test
```

### Instrumented тесты (когда будут добавлены)
```bash
.\gradlew.bat connectedAndroidTest
```

## 8. Сборка release APK

⚠️ **Требуется keystore!**

```bash
.\gradlew.bat assembleRelease
```

APK будет создан в:
`app/build/outputs/apk/release/app-release-unsigned.apk`

## 9. Установка на устройство

```bash
# Debug версия
.\gradlew.bat installDebug

# Release версия (с keystore)
.\gradlew.bat installRelease
```

## 10. Частые проблемы

### "SDK location not found"
**Решение:** Создать `local.properties`:
```properties
sdk.dir=C\:\\Android
```

### "JDK version incompatible"
**Решение:** 
1. `File → Project Structure → SDK Location`
2. Выбрать JDK 17 или создать новый

### "Gradle sync failed"
**Решение:**
1. `File → Invalidate Caches / Restart`
2. Перезапустить Android Studio
3. Запустить `.\gradlew.bat --refresh-dependencies`

### "ML Kit model download failed"
**Решение:**
- Проверить интернет соединение
- ML Kit автоматически скачает модели при первом использовании
- Размер: ~15-20 MB

### "Billing unavailable"
**Решение:**
- Billing работает только на реальных устройствах
- Или на эмуляторе с Google Play Services
- Требуется настройка продукта в Play Console

---

## ✅ Готово!

Приложение должно успешно запуститься и показать основной экран с Search bar и пустым grid (или с результатами после синка).

**Следующие шаги:**
- Добавить тестовые изображения в галерею
- Протестировать OCR на разных типах текста
- Проверить работу поиска
- Настроить billing в Play Console для full-flow теста

**Удачи! 🚀**

