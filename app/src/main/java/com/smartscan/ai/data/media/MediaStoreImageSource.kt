package com.smartscan.ai.data.media

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class MediaStoreImageSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun loadLatestImageUris(limit: Int): List<String> = withContext(Dispatchers.IO) {
        val uris = mutableListOf<String>()
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                Log.d("MediaStoreImageSource", "Cursor count: ${cursor.count}")
                while (cursor.moveToNext() && uris.size < limit) {
                    val id = cursor.getLong(idColumn)
                    val uri = ContentUris.withAppendedId(collection, id)
                    uris += uri.toString()
                    Log.d("MediaStoreImageSource", "Found image: $uri")
                }
            } ?: run {
                Log.w("MediaStoreImageSource", "Query returned null")
            }
        } catch (e: Exception) {
            Log.e("MediaStoreImageSource", "Error loading images: ${e.message}", e)
        }

        Log.d("MediaStoreImageSource", "Total images found: ${uris.size}")
        uris
    }

    suspend fun loadBitmap(uriString: String): Bitmap? = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        return@withContext runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        }.getOrNull()
    }

    suspend fun loadPhotoCreatedAtEpochMillis(uriString: String): Long? = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DATE_ADDED
                ),
                null,
                null,
                null
            )?.use { cursor: Cursor ->
                if (!cursor.moveToFirst()) return@use null

                val dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                val dateAddedIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)

                val dateTaken = if (dateTakenIndex >= 0 && !cursor.isNull(dateTakenIndex)) {
                    cursor.getLong(dateTakenIndex)
                } else {
                    0L
                }

                if (dateTaken > 0L) {
                    dateTaken
                } else {
                    val dateAddedSeconds = if (dateAddedIndex >= 0 && !cursor.isNull(dateAddedIndex)) {
                        cursor.getLong(dateAddedIndex)
                    } else {
                        0L
                    }
                    if (dateAddedSeconds > 0L) dateAddedSeconds * 1000L else null
                }
            }
        }.getOrNull()
    }
}
