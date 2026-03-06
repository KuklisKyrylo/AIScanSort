package com.smartscan.ai.data.analyzer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class TesseractOcrFallback @Inject constructor(
    @ApplicationContext private val context: Context
) {

    @Volatile
    private var disabledForSession: Boolean = false

    @Volatile
    private var modelsReady: Boolean = false

    suspend fun tryRecognizeRussian(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        if (disabledForSession) return@withContext null

        Log.d("TesseractOcrFallback", "=== Tesseract OCR START ===")
        runCatching {
            val tessDir = File(context.filesDir, "tesseract")
            if (!tessDir.exists()) tessDir.mkdirs()

            val dataPath = tessDir.absolutePath + File.separator
            val tessDataDir = File(dataPath, "tessdata")
            if (!tessDataDir.exists()) tessDataDir.mkdirs()

            if (!modelsReady) {
                val rusFile = ensureTrainedData(dataPath, "rus")
                val engFile = ensureTrainedData(dataPath, "eng")
                Log.d("TesseractOcrFallback", "rus.traineddata size=${rusFile.length()}")
                Log.d("TesseractOcrFallback", "eng.traineddata size=${engFile.length()}")
                modelsReady = true
            }

            val tessApi = TessBaseAPI()
            var lang = initWithFallback(tessApi, dataPath)
            if (lang == null) {
                val rusFile = File(tessDataDir, "rus.traineddata")
                val engFile = File(tessDataDir, "eng.traineddata")
                rusFile.delete()
                engFile.delete()
                ensureTrainedData(dataPath, "rus", forceRedownload = true)
                ensureTrainedData(dataPath, "eng", forceRedownload = true)
                lang = initWithFallback(tessApi, dataPath)
            }

            if (lang == null) {
                Log.e("TesseractOcrFallback", "Could not initialize Tesseract API with language=rus+eng/rus/eng")
                tessApi.end()
                disabledForSession = true
                return@runCatching null
            }

            val prepared = preprocessForOcr(bitmap)
            tessApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK)
            tessApi.setImage(prepared)

            val text = tessApi.utF8Text
            tessApi.clear()
            tessApi.end()
            prepared.recycle()

            val result = text?.trim().takeUnless { it.isNullOrBlank() }
            Log.d("TesseractOcrFallback", "Final: ${if (result != null) "SUCCESS (${result.length} chars)" else "NULL/BLANK"}")
            Log.d("TesseractOcrFallback", "=== Tesseract OCR END ===")
            result
        }.onFailure {
            Log.e("TesseractOcrFallback", "Exception: ${it.message}", it)
        }.getOrNull()
    }

    private fun preprocessForOcr(src: Bitmap): Bitmap {
        // Keep OCR input reasonably small and always output ARGB_8888 software bitmap.
        val maxSide = 1280
        val scale = (maxSide.toFloat() / maxOf(src.width, src.height)).coerceAtMost(1f)
        val w = (src.width * scale).toInt().coerceAtLeast(1)
        val h = (src.height * scale).toInt().coerceAtLeast(1)

        // Convert hardware bitmap to software bitmap first to avoid Canvas error.
        val softwareSrc = if (src.config == Bitmap.Config.HARDWARE) {
            src.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            src
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(softwareSrc, Rect(0, 0, softwareSrc.width, softwareSrc.height), Rect(0, 0, w, h), paint)

        if (softwareSrc != src) {
            softwareSrc.recycle()
        }

        return out
    }

    private fun ensureTrainedData(dataPath: String, lang: String, forceRedownload: Boolean = false): File {
        val tessDataDir = File(dataPath, "tessdata")
        if (!tessDataDir.exists()) tessDataDir.mkdirs()

        val trainedDataFile = File(tessDataDir, "$lang.traineddata")
        if (!forceRedownload && trainedDataFile.exists() && isValidTrainedData(trainedDataFile)) {
            return trainedDataFile
        }

        if (trainedDataFile.exists()) {
            trainedDataFile.delete()
        }

        val urls = listOf(
            "https://raw.githubusercontent.com/tesseract-ocr/tessdata/3.04.00/$lang.traineddata",
            "https://raw.githubusercontent.com/tesseract-ocr/tessdata/main/$lang.traineddata",
            "https://raw.githubusercontent.com/tesseract-ocr/tessdata_fast/main/$lang.traineddata"
        )

        var downloaded = false
        var lastError: String? = null
        for (urlStr in urls) {
            runCatching {
                val url = URL(urlStr)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    requestMethod = "GET"
                    doInput = true
                    instanceFollowRedirects = true
                }

                connection.connect()
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IllegalStateException("HTTP ${connection.responseCode}")
                }

                connection.inputStream.use { input ->
                    FileOutputStream(trainedDataFile).use { output ->
                        input.copyTo(output)
                    }
                }
                connection.disconnect()

                if (!isValidTrainedData(trainedDataFile)) {
                    throw IllegalStateException("invalid traineddata")
                }
                downloaded = true
            }.onFailure {
                lastError = "$urlStr -> ${it.message}"
                if (trainedDataFile.exists()) trainedDataFile.delete()
            }
            if (downloaded) break
        }

        if (!downloaded) {
            throw IllegalStateException("Failed to download $lang.traineddata: $lastError")
        }

        return trainedDataFile
    }

    private fun initWithFallback(tessApi: TessBaseAPI, dataPath: String): String? {
        if (tessApi.init(dataPath, "rus+eng")) return "rus+eng"
        if (tessApi.init(dataPath, "rus")) return "rus"
        if (tessApi.init(dataPath, "eng")) return "eng"
        return null
    }

    private fun isValidTrainedData(file: File): Boolean {
        // Reject tiny/HTML files and allow both legacy and modern traineddata sizes.
        if (!file.exists() || file.length() < 200_000L) return false
        return runCatching {
            RandomAccessFile(file, "r").use { raf ->
                val buf = ByteArray(16)
                raf.readFully(buf)
                val header = String(buf, Charsets.UTF_8)
                !header.startsWith("<") && !header.contains("html", ignoreCase = true)
            }
        }.getOrDefault(false)
    }
}
