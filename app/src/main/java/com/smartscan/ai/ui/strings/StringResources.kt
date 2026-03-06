package com.smartscan.ai.ui.strings
import com.smartscan.ai.domain.model.AppLanguage
data class StringResources(
    val appName: String, val searchHint: String, val syncNow: String, val syncing: String,
    val grantPermission: String, val permissionRequired: String, val paywallMessage: String,
    val buyPro: String, val noTextRecognized: String, val settings: String, val language: String,
    val back: String, val syncingGallery: String, val syncComplete: String, val freeLimitReached: String,
    val imageDetails: String, val share: String, val delete: String, val confirmDelete: String,
    val deleteMessage: String, val cancel: String, val extractedText: String, val tags: String,
    val metadata: String, val status: String, val scannedAt: String, val uri: String, val imagePreview: String
)
fun getStrings(language: AppLanguage): StringResources = when (language) {
    AppLanguage.ENGLISH -> englishStrings
    AppLanguage.RUSSIAN -> russianStrings
    AppLanguage.SPANISH -> spanishStrings
    AppLanguage.GERMAN -> germanStrings
}
private val englishStrings = StringResources(
    "SmartScan AI", "Search text and tags", "Sync now", "Syncing...", "Grant",
    "Allow media access to scan gallery screenshots", "Free limit reached (%d/50). Unlock Pro to continue scanning.",
    "Buy Pro", "No text recognized", "Settings", "Language", "Back", "Syncing gallery...",
    "Synced %d, skipped %d.", "Synced %d, skipped %d. Free limit reached.", "Image Details", "Share",
    "Delete", "Delete Image?", "This image will be permanently deleted from the database.",
    "Cancel", "Extracted Text", "Tags", "Metadata", "Status", "Scanned at", "URI", "Image preview"
)
private val russianStrings = StringResources(
    "SmartScan AI", "Поиск текста и тегов", "Синхронизировать", "Синхронизация...", "Разрешить",
    "Разрешите доступ к медиафайлам для сканирования галереи", "Достигнут лимит бесплатной версии (%d/50). Разблокируйте Pro для продолжения.",
    "Купить Pro", "Текст не распознан", "Настройки", "Язык", "Назад", "Синхронизация галереи...",
    "Синхронизировано %d, пропущено %d.", "Синхронизировано %d, пропущено %d. Достигнут лимит.", "Детали изображения", "Поделиться",
    "Удалить", "Удалить изображение?", "Это изображение будет безвозвратно удалено из базы данных.",
    "Отмена", "Распознанный текст", "Теги", "Метаданные", "Статус", "Отсканировано", "URI", "Превью изображения"
)
private val spanishStrings = StringResources(
    "SmartScan AI", "Buscar texto y etiquetas", "Sincronizar ahora", "Sincronizando...", "Conceder",
    "Permite el acceso a los medios para escanear capturas de galería", "Límite gratuito alcanzado (%d/50). Desbloquea Pro para continuar escaneando.",
    "Comprar Pro", "No se reconoció texto", "Configuración", "Idioma", "Atrás", "Sincronizando galería...",
    "Sincronizado %d, omitido %d.", "Sincronizado %d, omitido %d. Límite alcanzado.", "Detalles de imagen", "Compartir",
    "Eliminar", "¿Eliminar imagen?", "Esta imagen se eliminará permanentemente de la base de datos.",
    "Cancelar", "Texto extraído", "Etiquetas", "Metadatos", "Estado", "Escaneado en", "URI", "Vista previa"
)
private val germanStrings = StringResources(
    "SmartScan AI", "Text und Tags suchen", "Jetzt synchronisieren", "Synchronisiere...", "Gewähren",
    "Erlaube Medienzugriff zum Scannen von Galerie-Screenshots", "Kostenloses Limit erreicht (%d/50). Schalte Pro frei, um weiterzuscannen.",
    "Pro kaufen", "Kein Text erkannt", "Einstellungen", "Sprache", "Zurück", "Galerie wird synchronisiert...",
    "Synchronisiert %d, übersprungen %d.", "Synchronisiert %d, übersprungen %d. Limit erreicht.", "Bilddetails", "Teilen",
    "Löschen", "Bild löschen?", "Dieses Bild wird dauerhaft aus der Datenbank gelöscht.",
    "Abbrechen", "Extrahierter Text", "Tags", "Metadaten", "Status", "Gescannt am", "URI", "Bildvorschau"
)