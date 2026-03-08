# Индикация выбранной сортировки в меню Sort

## Обзор
Реализована визуальная индикация текущей выбранной сортировки в выпадающем меню Sort.

## Реализованные изменения

### 1. Кнопка Sort остается компактной

**Решение:**
- Кнопка продолжает показывать короткий текст "Sort" (strings.sortMenu)
- Сохраняет компактный размер и не ломает layout
- Использует стандартные цвета `secondaryContainer`

### 2. Выпадающее меню с визуальной индикацией

**Добавлено:**
- Иконка галочки (CheckCircle) рядом с активной опцией сортировки
- Иконка цвета `primary` для лучшей видимости
- Размер иконки: 20dp для оптимального размещения
- Отступ 8dp между иконкой и текстом

### 3. Технические детали

#### Измененные файлы:
- `app/src/main/java/com/smartscan/ai/ui/main/MainScreen.kt`

#### Добавленные импорты:
```kotlin
import androidx.compose.material.icons.filled.CheckCircle
```

#### Обновленная сигнатура SearchTopBar:
```kotlin
@Composable
private fun SearchTopBar(
    // ...existing parameters...
    sortOption: ScanSortOption,  // Новый параметр
    onSortOptionSelected: (ScanSortOption) -> Unit
)
```

#### Логика отображения кнопки:
```kotlin
FilledTonalButton(
    onClick = { showSortMenu = true },
    colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
) {
    Text(
        text = strings.sortMenu,  // Короткий текст "Sort"
        style = MaterialTheme.typography.labelLarge
    )
}
```

#### Логика выпадающего меню:
```kotlin
DropdownMenuItem(
    text = { 
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (sortOption == ScanSortOption.SYNC_DATE) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(strings.sortBySyncDate)
        }
    },
    onClick = {
        onSortOptionSelected(ScanSortOption.SYNC_DATE)
        showSortMenu = false
    }
)
```

## Преимущества

✅ **Компактный UI** - Кнопка не ломает layout, остается короткой
✅ **Улучшенная видимость** - Пользователь видит активную сортировку при открытии меню
✅ **Интуитивность** - Галочка в меню четко показывает активную опцию
✅ **Консистентность** - Использует системные цвета Material Design
✅ **Мультиязычность** - Работает со всеми языками приложения
✅ **Стандартный UX** - Следует паттернам Material Design

## Поведение

1. **При открытии экрана:**
   - Кнопка Sort показывает короткий текст "Sort"
   - Занимает минимум места

2. **При нажатии на кнопку Sort:**
   - Открывается выпадающее меню
   - Текущая опция отмечена галочкой (CheckCircle icon)
   - Пользователь сразу видит какая сортировка активна

3. **При выборе другой опции:**
   - Меню закрывается
   - Список пересортируется
   - Галочка переместится к новой опции при следующем открытии меню
   - Выбор сохраняется в preferences

## Цветовая схема

- **Кнопка Sort:** `secondaryContainer` / `onSecondaryContainer` (стандартная)
- **Иконка галочки:** `primary`
- **Остальные кнопки:** `secondaryContainer` / `onSecondaryContainer` (без изменений)

## Статус сборки

✅ Сборка успешна - нет ошибок
✅ Layout не сломан - кнопки остаются на своих местах
✅ Все функции работают корректно
✅ Готово к тестированию


