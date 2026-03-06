# SmartScan AI - Мультиязычность и Настройки

## ✅ Мультиязычность добавлена успешно!

**Дата завершения:** 2026-03-06  
**Поддерживаемые языки:** 4 (Английский, Русский, Испанский, Немецкий)  
**Способ хранения:** DataStore Preferences

---

## 🌍 Поддерживаемые языки

| Язык | Код | Отображение |
|------|-----|-------------|
| **English** | `en` | English |
| **Русский** | `ru` | Русский |
| **Español** | `es` | Español |
| **Deutsch** | `de` | Deutsch |

---

## 📱 Функции мультиязычности

### 1. **Автоматическое переключение язык при изменении настроек**
- Выбор языка в экране `Settings`
- Сохранение выбора в `DataStore Preferences`
- Мгновенное обновление всего UI при смене языка
- No app restart required!

### 2. **Локализованные строки**
Все текстовые элементы переведены на 4 языка:

#### Основной экран (Main)
- `searchHint` - подсказка поиска
- `syncNow` / `syncing` - кнопка синхронизации
- `grantPermission` - кнопка выдачи разрешения
- `permissionRequired` - сообщение о разрешении
- `paywallMessage` - сообщение о лимите
- `buyPro` - кнопка покупки Pro
- `noTextRecognized` - сообщение когда нет текста

#### Экран настроек (Settings)
- `settings` - заголовок
- `language` - раздел выбора языка
- `back` - кнопка назад

#### Сообщения синхронизации
- `syncingGallery` - статус синка
- `syncComplete` - успешное завершение
- `freeLimitReached` - достигнут лимит

---

## 🗂️ Архитектура мультиязычности

### Data Layer
```
data/preferences/
├── PreferencesManager.kt (DataStore интеграция)
```

### Domain Layer
```
domain/model/
├── AppLanguage.kt (enum с кодами языков)

ui/strings/
├── StringResources.kt (все переводы + getStrings())
```

### UI Layer
```
ui/settings/
├── SettingsScreen.kt (выбор языка в UI)
├── SettingsViewModel.kt (управление состоянием)

ui/main/
├── MainViewModel.kt (наблюдение за языком)
├── MainScreen.kt (использование локализованных строк)
```

### Navigation
```
ui/navigation/
├── Screen.kt (маршруты)
```

---

## 💾 DataStore Preferences

### Хранилище
- **Название:** `settings`
- **Ключ:** `app_language`
- **Значение:** Код языка (en, ru, es, de)
- **Место:** `/data/data/com.smartscan.ai/files/datastore/settings.preferences_pb`

### Инициализация
Происходит автоматически при первом запуске:
```kotlin
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
```

---

## 🔄 Поток данных языка

```
DataStore (Persistent)
    ↓
PreferencesManager.languageFlow (Flow<AppLanguage>)
    ↓
ViewModel.observeLanguage() (StateFlow)
    ↓
UI State (MainUiState, SettingsUiState)
    ↓
Composables (автоматический recompose)
```

---

## 📝 Использование в коде

### Получить текущий язык
```kotlin
// В ViewModel
val language by preferencesManager.languageFlow.collectAsState()

// Или из состояния UI
val strings = state.strings // StringResources
```

### Получить локализованные строки
```kotlin
val strings = getStrings(AppLanguage.RUSSIAN)
Text(strings.searchHint) // "Поиск текста и тегов"
```

### Изменить язык
```kotlin
viewModel.setLanguage(AppLanguage.SPANISH)
// Автоматически обновится весь UI
```

---

## 🎨 Интеграция в Compose

### Все элементы используют локализованные строки:

#### SearchTopBar
- `strings.searchHint` - placeholder
- `strings.syncNow` / `strings.syncing` - кнопка
- `strings.settings` - description иконки

#### PermissionBanner
- `strings.permissionRequired` - сообщение
- `strings.grantPermission` - кнопка

#### PaywallBanner
- `strings.paywallMessage.format(count)` - сообщение
- `strings.buyPro` - кнопка

#### SettingsScreen
- `strings.settings` - заголовок
- `strings.language` - раздел языков
- `strings.back` - кнопка назад

---

## 📁 Новые файлы добавлены

```
app/src/main/java/com/smartscan/ai/
├── data/
│   └── preferences/
│       └── PreferencesManager.kt                    (NEW)
├── domain/
│   ├── model/
│   │   └── AppLanguage.kt                          (NEW)
├── di/
└── ui/
    ├── main/
    │   └── MainScreen.kt                           (UPDATED)
    │   └── MainViewModel.kt                        (UPDATED)
    ├── navigation/
    │   └── Screen.kt                               (NEW)
    ├── settings/
    │   ├── SettingsScreen.kt                       (NEW)
    │   └── SettingsViewModel.kt                    (NEW)
    └── strings/
        └── StringResources.kt                      (NEW - все переводы)
```

---

## 🧪 Тестирование мультиязычности

### На эмуляторе/устройстве

1. **Запустить приложение**
   - Откроется на языке устройства (или English по умолчанию)

2. **Нажать иконку ⚙️ Settings**
   - Откроется экран настроек с выбором языка

3. **Выбрать язык**
   - Tap на нужный язык (English, Русский, Español, Deutsch)
   - UI автоматически переключится!

4. **Вернуться на main экран**
   - Все текст обновлен на выбранный язык
   - Нажать Back или вернуться навигацией

5. **Перезагрузить приложение**
   - Язык сохранится и будет загружен из DataStore

---

## 🔐 Безопасность и производительность

### DataStore (вместо SharedPreferences)
✅ Типобезопасен - compile-time safety  
✅ Асинхронный - не блокирует UI  
✅ Транзакционный - атомарные операции  
✅ Шифруемый - поддержка EncryptedSharedPreferences  

### Flow (вместо LiveData)
✅ Холодные потоки - не потребляют ресурсы пока не собраны  
✅ Корутин-native - работает с suspend  
✅ Compositional - легко комбинировать  

---

## 📊 Размер кода

- `StringResources.kt` - все переводы (4 языка) - ~250 строк
- `PreferencesManager.kt` - DataStore интеграция - ~35 строк
- `SettingsScreen.kt` + `SettingsViewModel.kt` - UI + логика - ~120 строк
- Обновления `MainScreen.kt`, `MainViewModel.kt` - использование строк - ~50 строк

**Итого:** ~455 строк кода для полной мультиязычности

---

## ✨ Особенности реализации

### 1. **Реактивность**
- UI автоматически обновляется при смене языка
- Нет необходимости перезагружать приложение
- Используется `collectAsStateWithLifecycle()` для lifecycle awareness

### 2. **Масштабируемость**
- Легко добавить новый язык - просто добавить еще один `StringResources` блок
- Централизованное управление строками в одном файле

### 3. **Тестируемость**
- `StringResources` - просто data class, легко мокировать
- `PreferencesManager` - можно подменять в DI для тестов
- `SettingsViewModel` - стандартный ViewModel

### 4. **Производительность**
- DataStore кэширует значения в памяти
- Flow ленивый - вычисляет только когда подписано
- Минимум переписываний в UI

---

## 🚀 Готово к использованию!

Приложение полностью поддерживает мультиязычность:
- ✅ 4 языка
- ✅ Сохранение выбора
- ✅ Мгновенное переключение
- ✅ Реактивный UI
- ✅ Clean Architecture
- ✅ Production-ready

**Можно открыть в Android Studio и запустить на эмуляторе!** 🎯

