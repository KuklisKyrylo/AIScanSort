### Инструкция по замене иконки приложения

## ✅ SVG иконка создана
Файл: `app/src/main/ic_launcher-web.svg`

**Описание:**
- 🤖 Голова робота-помощника с анимированными глазами
- ✨ Градиентный фон (фиолетово-синий)
- 🔤 Крупный текст "AI" внизу
- 💫 Светящиеся эффекты и схемы

## 📋 Как применить иконку:

### Вариант 1: Через Android Studio (рекомендуется)
1. **Правый клик** на `app` в Project → `New` → `Image Asset`
2. Выбери **Icon Type**: `Launcher Icons (Adaptive and Legacy)`
3. **Foreground Layer**:
   - Path: `app/src/main/ic_launcher-web.svg`
   - Trim: `Yes`
   - Resize: `80%`
4. **Background Layer**:
   - Color: `#667eea`
5. Нажми **Next** → **Finish**

### Вариант 2: Онлайн генератор
1. Открой https://icon.kitchen/ или https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html
2. Загрузи `app/src/main/ic_launcher-web.svg`
3. Настрой:
   - Background: Gradient (#667eea → #764ba2)
   - Foreground: SVG (trim 10%, scale 80%)
4. Скачай ZIP
5. Распакуй в `app/src/main/res/` (перезапиши mipmap-* папки)

### Вариант 3: Вручную (если нужны кастомные размеры)
Используй ImageMagick или Inkscape:
```bash
# mdpi (48x48)
inkscape -w 48 -h 48 ic_launcher-web.svg -o res/mipmap-mdpi/ic_launcher.png

# hdpi (72x72)
inkscape -w 72 -h 72 ic_launcher-web.svg -o res/mipmap-hdpi/ic_launcher.png

# xhdpi (96x96)
inkscape -w 96 -h 96 ic_launcher-web.svg -o res/mipmap-xhdpi/ic_launcher.png

# xxhdpi (144x144)
inkscape -w 144 -h 144 ic_launcher-web.svg -o res/mipmap-xxhdpi/ic_launcher.png

# xxxhdpi (192x192)
inkscape -w 192 -h 192 ic_launcher-web.svg -o res/mipmap-xxxhdpi/ic_launcher.png
```

## 🎨 Дизайн иконки:
- **Концепция**: AI-помощник робот с "умными" глазами
- **Цвета**: Фиолетово-синий градиент (премиум-вид)
- **Эффекты**: Анимированное свечение (только в SVG, PNG статичны)
- **Текст**: Жирный "AI" для узнаваемости

## ⚡ Быстрая проверка:
После генерации иконок:
```bash
.\gradlew.bat assembleDebug --no-daemon
.\gradlew.bat installDebug --no-daemon
```

Новая иконка появится на устройстве!

