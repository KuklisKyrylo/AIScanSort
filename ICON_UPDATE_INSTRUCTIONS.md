# 🎨 Иконка обновлена! Инструкция по установке

## ✅ Что сделано:

1. **Обновлен векторный foreground** (`ic_launcher_foreground.xml`)
   - Улыбающийся робот с антенной
   - Светящиеся глаза
   - Красный badge "AI"
   - **Уменьшен размер**, чтобы влезал в круглую маску

2. **Обновлен gradient background** (#667eea → #764ba2)

3. **Пересобран проект** (BUILD SUCCESSFUL)

---

## 📱 Как установить и увидеть новую иконку:

### Вариант 1: Через Gradle (если эмулятор запущен)
```powershell
cd C:\Users\kiril\IdeaProjects\Tools\AIScanSort

# Удалить старую версию и очистить кэш
adb uninstall com.smartscan.ai
adb shell pm clear com.android.launcher3

# Установить новую версию
.\gradlew.bat installDebug --no-daemon

# Перезапустить launcher
adb shell am force-stop com.android.launcher3
adb shell monkey -p com.smartscan.ai -c android.intent.category.LAUNCHER 1
```

### Вариант 2: Вручную (если adb не работает)
1. Открой эмулятор
2. В Android Studio: **Run** → **Run 'app'**
3. После установки: **Long press** на Home → **Restart launcher**
4. Или просто **перезапусти эмулятор**

### Вариант 3: Установить APK вручную
```powershell
cd C:\Users\kiril\IdeaProjects\Tools\AIScanSort

# Скопировать APK на рабочий стол
copy app\build\outputs\apk\debug\app-debug.apk %USERPROFILE%\Desktop\SmartScanAI.apk
```
Затем перетащи `SmartScanAI.apk` в окно эмулятора.

---

## 🎯 Если иконка всё ещё не обновилась:

### Причина: Кэш лаунчера
Android кэширует иконки агрессивно. Нужно форсировать обновление:

```powershell
# 1. Удалить приложение
adb uninstall com.smartscan.ai

# 2. Очистить кэш launcher
adb shell pm clear com.android.launcher3

# 3. Перезагрузить эмулятор
adb reboot

# 4. После загрузки установить заново
.\gradlew.bat installDebug --no-daemon
```

### Альтернатива: Изменить applicationId (крайняя мера)
Если совсем ничего не помогает, временно измени `applicationId` в `app/build.gradle.kts`:
```kotlin
android {
    defaultConfig {
        applicationId = "com.smartscan.ai.v2"  // добавь .v2
    }
}
```
Пересобери и установи — лаунчер увидит это как новое приложение.

---

## 🖼️ Как выглядит новая иконка:

```
┌─────────────────┐
│  Gradient BG    │
│  Purple→Blue    │
│                 │
│   🤖  Robot     │
│   (  )  (  )    │ ← Светящиеся глаза
│      ◡          │ ← Улыбка
│   📡 Antenna    │ ← Антенна сверху
│                 │
│  ┌───────────┐  │
│  │    AI     │  │ ← Красный badge
│  └───────────┘  │
└─────────────────┘
```

**Размер подобран так**, чтобы робот не обрезался в круглой маске лаунчера.

---

## 📦 Файлы изменены:

- `app/src/main/res/drawable/ic_launcher_foreground.xml` ✅
- `app/src/main/res/drawable/ic_launcher_background.xml` ✅
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` ✅
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml` ✅

---

## ⚡ Быстрый тест (1 команда):

```powershell
cd C:\Users\kiril\IdeaProjects\Tools\AIScanSort; adb uninstall com.smartscan.ai; adb shell pm clear com.android.launcher3; .\gradlew.bat installDebug --no-daemon; adb shell am force-stop com.android.launcher3
```

После этого открой app drawer — должна быть **новая иконка с роботом и AI**!

---

🎉 **Иконка готова к тестированию!**

