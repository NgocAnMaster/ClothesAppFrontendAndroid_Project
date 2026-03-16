package com.master.myapplication2.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    fun saveBitmapToCache(context: Context, bitmap: Any?): Uri {
        val bmp = bitmap as? Bitmap ?: throw IllegalArgumentException("Invalid bitmap")

        val file = File(context.cacheDir, "captured_${System.currentTimeMillis()}.jpg")
        val fos = FileOutputStream(file)
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        fos.flush()
        fos.close()

        return FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
    }

    fun copyUriToCache(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream")

        val file = File(context.cacheDir, "tryon_${System.currentTimeMillis()}.jpg")

        file.outputStream().use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }

        return file
    }
}
