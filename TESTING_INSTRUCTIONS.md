# Инструкция по тестированию и диагностике русского OCR

## ✅ Что сделано:

1. **Автотесты** (`OcrTextSelectionTest.kt`) - проверяют логику выбора текста
   - ✅ Все тесты прошли успешно
   - ✅ Логика приоритета кириллицы работает правильно

2. **Улучшенное логирование** - теперь видно каждый шаг OCR
3. **Принудительный приоритет Tesseract** - если ML Kit = латиница, Tesseract = кириллица → берётся Tesseract

## 📋 Как протестировать:

### Шаг 1: Установить приложение
```powershell
cd C:\Users\kiril\IdeaProjects\Tools\AIScanSort
.\gradlew.bat installDebug --no-daemon
```

### Шаг 2: Очистить базу
- Удалить приложение и установить заново ИЛИ
- Settings → очистить данные

### Шаг 3: Включить интернет (для первого запуска)
- Tesseract должен скачать `rus.traineddata` (~2MB)
- Это происходит автоматически при первом сканировании

### Шаг 4: Открыть Logcat в Android Studio
Фильтры:
```
TesseractOcrFallback
MLKitAnalyzer
```

### Шаг 5: Запустить Sync now

### Шаг 6: Проверить логи

## 🔍 Что искать в логах:

### ✅ Успешный сценарий (Tesseract работает):
```
D/TesseractOcrFallback: === Tesseract OCR START ===
D/TesseractOcrFallback: Data path: /data/user/0/.../files/tesseract/
D/TesseractOcrFallback: Trained data ready
D/TesseractOcrFallback: TessBaseAPI initialized successfully
D/TesseractOcrFallback: Image set, starting recognition...
D/TesseractOcrFallback: Raw Tesseract result length: 156
D/TesseractOcrFallback: Raw Tesseract result preview: Аутентификация Пытаемся...
D/TesseractOcrFallback: Final result: SUCCESS (156 chars)
D/TesseractOcrFallback: === Tesseract OCR END ===

D/MLKitAnalyzer: === OCR ANALYSIS START ===
D/MLKitAnalyzer: ML Kit raw length: 189
D/MLKitAnalyzer: ML Kit sanitized: AyreHTuuKauua lblTaeMcA...
D/MLKitAnalyzer: Tesseract result: Аутентификация Пытаемся...
D/MLKitAnalyzer: Cyrillic count - ML Kit: 0, Tesseract: 78
D/MLKitAnalyzer: Forcing Tesseract: ML Kit has 0 Cyrillic, Tesseract has 78
D/MLKitAnalyzer: Final chosen text: Аутентификация Пытаемся...
D/MLKitAnalyzer: === OCR ANALYSIS END ===
```

### ❌ Проблемный сценарий 1 (Tesseract не инициализируется):
```
E/TesseractOcrFallback: Failed to init TessBaseAPI
D/MLKitAnalyzer: Tesseract result: NULL/FAILED
D/MLKitAnalyzer: Choosing ML Kit: Tesseract is empty/null
```
**Причина**: `rus.traineddata` не скачался или повреждён  
**Решение**: Проверить интернет, переустановить приложение

### ❌ Проблемный сценарий 2 (Tesseract падает с exception):
```
E/MLKitAnalyzer: Tesseract fallback exception: java.lang...
D/MLKitAnalyzer: Tesseract result: NULL/FAILED
```
**Причина**: Ошибка в Tesseract (например, OOM)  
**Решение**: Проверить стек-трейс exception

### ❌ Проблемный сценарий 3 (Tesseract возвращает пустой результат):
```
D/TesseractOcrFallback: Raw Tesseract result length: 0
D/TesseractOcrFallback: Final result: NULL/BLANK
```
**Причина**: Изображение слишком низкого качества или preprocessing не работает  
**Решение**: Улучшить preprocessing или использовать другие параметры Tesseract

## 🐛 Если проблема воспроизводится:

1. **Скопируй ВСЕ логи** из Logcat с тегами `TesseractOcrFallback` и `MLKitAnalyzer`
2. **Сделай скриншот** проблемного экрана detail
3. **Пришли мне** логи + скриншот

## 🔧 Временное решение (если Tesseract не работает):

Можно добавить принудительное правило в `choosePreferredText`:
```kotlin
// ВСЕГДА выбирать Tesseract если он вернул что-то
if (!tesseractText.isNullOrBlank()) {
    return tesseractText
}
```

Но это плохо для английского текста, поэтому нужно сначала понять ПОЧЕМУ Tesseract не работает.

