package com.smartscan.ai.ui.strings
import com.smartscan.ai.domain.model.AppLanguage
data class StringResources(
    val appName: String, val searchHint: String, val syncNow: String, val syncing: String,
    val grantPermission: String, val permissionRequired: String, val paywallMessage: String,
    val buyPro: String, val noTextRecognized: String, val settings: String, val language: String,
    val back: String, val syncingGallery: String, val syncComplete: String, val freeLimitReached: String,
    val imageDetails: String, val share: String, val delete: String, val confirmDelete: String,
    val deleteMessage: String, val cancel: String, val extractedText: String, val tags: String,
    val metadata: String, val status: String, val scannedAt: String, val uri: String, val imagePreview: String,
    val trialUsage: String, val trialRemaining: String, val upgradeCta: String,
    val paywallTitle: String, val paywallSubtitle: String,
    val paywallFeatureUnlimited: String, val paywallFeatureNoWatermarks: String,
    val paywallFeatureFaster: String, val paywallFeatureCloud: String,
    val paywallMonthlyTitle: String, val paywallMonthlySubtitle: String,
    val paywallLifetimeTitle: String, val paywallLifetimeSubtitle: String,
    val paywallChooseLifetime: String, val paywallTryMonthly: String,
    val paywallTerms: String, val paywallPrivacy: String, val paywallRestore: String,
    val paywallPriceNote: String,
    val paywallMonthlyPeriod: String, val paywallLifetimePeriod: String,
    val paywallMonthlyFeatureA: String, val paywallMonthlyFeatureB: String,
    val paywallLifetimeFeatureA: String, val paywallLifetimeFeatureB: String,
    val showEmptyScans: String, val hideEmptyScans: String
)
fun getStrings(language: AppLanguage): StringResources = when (language) {
    AppLanguage.ENGLISH -> englishStrings
    AppLanguage.RUSSIAN -> russianStrings
    AppLanguage.SPANISH -> spanishStrings
    AppLanguage.GERMAN -> germanStrings
    AppLanguage.CHINESE -> chineseStrings
}
private val englishStrings = StringResources(
    "SmartScan AI", "Search text and tags", "Sync now", "Syncing...", "Grant",
    "Allow media access to scan gallery screenshots", "Free limit reached (%d/50). Unlock Pro to continue scanning.",
    "Buy Pro", "No text recognized", "Settings", "Language", "Back", "Syncing gallery...",
    "Synced %d, skipped %d.", "Synced %d, skipped %d. Free limit reached.", "Image Details", "Share",
    "Delete", "Delete Image?", "This image will be permanently deleted from the database.",
    "Cancel", "Extracted Text", "Tags", "Metadata", "Status", "Scanned at", "URI", "Image preview",
    "Trial: %d/30 scans used", "%d scans remaining", "Upgrade",
    "Upgrade to Premium", "Remove the 30-scan limit and scan unlimited photos with full ML Kit power",
    "Unlimited scans", "No watermarks", "Faster processing", "Cloud backup support",
    "Monthly", "Best for trying premium", "Lifetime (Best Value!)", "Save more, pay once forever",
    "Choose Lifetime", "Try Monthly", "Terms", "Privacy", "Restore",
    "Prices may vary by country",
    "/ month", "one-time", "Cancel anytime", "Auto-renews", "Unlimited updates", "Never pay again",
    "Show empty", "Hide empty"
)
private val russianStrings = StringResources(
    "SmartScan AI", "Поиск текста и тегов", "Синхронизировать", "Синхронизация...", "Разрешить",
    "Разрешите доступ к медиафайлам для сканирования галереи", "Достигнут лимит бесплатной версии (%d/50). Разблокируйте Pro для продолжения.",
    "Купить Pro", "Текст не распознан", "Настройки", "Язык", "Назад", "Синхронизация галереи...",
    "Синхронизировано %d, пропущено %d.", "Синхронизировано %d, пропущено %d. Достигнут лимит.", "Детали изображения", "Поделиться",
    "Удалить", "Удалить изображение?", "Это изображение будет безвозвратно удалено из базы данных.",
    "Отмена", "Распознанный текст", "Теги", "Метаданные", "Статус", "Отсканировано", "URI", "Превью изображения",
    "Пробный период: %d/30 сканов использовано", "Осталось %d сканов", "Обновить",
    "Перейти на Premium", "Снимите лимит в 30 сканов и сканируйте без ограничений с полной мощностью ML Kit",
    "Безлимитные сканы", "Без водяных знаков", "Быстрая обработка", "Поддержка облачного бэкапа",
    "Месячный", "Лучше для знакомства с Premium", "Пожизненный (Лучшая цена!)", "Сэкономьте больше, заплатите один раз",
    "Выбрать пожизненный", "Попробовать месячный", "Условия", "Конфиденциальность", "Восстановить",
    "Цены могут отличаться в зависимости от страны",
    "/ месяц", "разовая оплата", "Отмена в любой момент", "Автопродление", "Бессрочные обновления", "Больше не нужно платить",
    "Показать пустые", "Скрыть пустые"
)
private val spanishStrings = StringResources(
    "SmartScan AI", "Buscar texto y etiquetas", "Sincronizar ahora", "Sincronizando...", "Conceder",
    "Permite el acceso a los medios para escanear capturas de galería", "Límite gratuito alcanzado (%d/50). Desbloquea Pro para continuar escaneando.",
    "Comprar Pro", "No se reconoció texto", "Configuración", "Idioma", "Atrás", "Sincronizando galería...",
    "Sincronizado %d, omitido %d.", "Sincronizado %d, omitido %d. Límite alcanzado.", "Detalles de imagen", "Compartir",
    "Eliminar", "¿Eliminar imagen?", "Esta imagen se eliminará permanentemente de la base de datos.",
    "Cancelar", "Texto extraído", "Etiquetas", "Metadatos", "Estado", "Escaneado en", "URI", "Vista previa",
    "Prueba: %d/30 escaneos usados", "%d escaneos restantes", "Mejorar",
    "Mejorar a Premium", "Elimina el límite de 30 escaneos y escanea fotos ilimitadas con toda la potencia de ML Kit",
    "Escaneos ilimitados", "Sin marcas de agua", "Procesamiento más rápido", "Soporte de copia en la nube",
    "Mensual", "Ideal para probar premium", "De por vida (¡Mejor valor!)", "Ahorra más, paga una vez para siempre",
    "Elegir de por vida", "Probar mensual", "Términos", "Privacidad", "Restaurar",
    "Los precios pueden variar según el país",
    "/ mes", "pago único", "Cancela cuando quieras", "Renovación automática", "Actualizaciones ilimitadas", "Nunca pagues de nuevo",
    "Mostrar vacíos", "Ocultar vacíos"
)
private val germanStrings = StringResources(
    "SmartScan AI", "Text und Tags suchen", "Jetzt synchronisieren", "Synchronisiere...", "Gewähren",
    "Erlaube Medienzugriff zum Scannen von Galerie-Screenshots", "Kostenloses Limit erreicht (%d/50). Schalte Pro frei, um weiterzuscannen.",
    "Pro kaufen", "Kein Text erkannt", "Einstellungen", "Sprache", "Zurück", "Galerie wird synchronisiert...",
    "Synchronisiert %d, übersprungen %d.", "Synchronisiert %d, übersprungen %d. Limit erreicht.", "Bilddetails", "Teilen",
    "Löschen", "Bild löschen?", "Dieses Bild wird dauerhaft aus der Datenbank gelöscht.",
    "Abbrechen", "Extrahierter Text", "Tags", "Metadaten", "Status", "Gescannt am", "URI", "Bildvorschau",
    "Testphase: %d/30 Scans genutzt", "%d Scans verbleibend", "Upgrade",
    "Auf Premium upgraden", "Entferne das 30-Scan-Limit und scanne unbegrenzt Fotos mit voller ML-Kit-Leistung",
    "Unbegrenzte Scans", "Keine Wasserzeichen", "Schnellere Verarbeitung", "Cloud-Backup-Unterstützung",
    "Monatlich", "Ideal zum Ausprobieren von Premium", "Lebenslang (Bestes Angebot!)", "Mehr sparen, einmal zahlen",
    "Lebenslang wählen", "Monatlich testen", "AGB", "Datenschutz", "Wiederherstellen",
    "Preise können je nach Land variieren",
    "/ Monat", "einmalig", "Jederzeit kündbar", "Automatische Verlängerung", "Unbegrenzte Updates", "Nie wieder zahlen",
    "Leere anzeigen", "Leere ausblenden"
)
private val chineseStrings = StringResources(
    "SmartScan AI", "搜索文本和标签", "立即同步", "同步中...", "授权",
    "允许访问媒体以扫描图库截图", "已达到免费限制（%d/50）。解锁 Pro 以继续扫描。",
    "购买 Pro", "未识别到文本", "设置", "语言", "返回", "正在同步图库...",
    "已同步 %d，已跳过 %d。", "已同步 %d，已跳过 %d。已达到免费限制。", "图片详情", "分享",
    "删除", "删除图片？", "该图片将从本地数据库中永久删除。",
    "取消", "识别文本", "标签", "元数据", "状态", "扫描时间", "URI", "图片预览",
    "试用期：已使用 %d/30 次扫描", "剩余 %d 次扫描", "升级",
    "升级到 Premium", "移除 30 次扫描限制，使用完整 ML Kit 能力进行无限扫描",
    "无限扫描", "无水印", "更快处理", "云备份支持",
    "月付", "适合先体验 Premium", "终身版（最佳价值！）", "一次付费，永久使用",
    "选择终身版", "试用月付", "条款", "隐私", "恢复购买",
    "价格可能因地区而异",
    "/ 月", "一次性", "可随时取消", "自动续订", "无限更新", "无需再次付费",
    "显示空白", "隐藏空白"
)