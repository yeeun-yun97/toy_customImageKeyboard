package com.github.yeeun_yun97.toy.imagekeyboard.service.utils

import android.annotation.SuppressLint
import android.content.ClipDescription
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class Util {
    companion object {
        fun commitImage(
            view: View, currentInputConnection: InputConnection,
            currentInputEditorInfo: EditorInfo,
        ) {
            mSaveMediaToStorage(
                currentInputConnection,
                currentInputEditorInfo,
                context = view.context,
                url = view.tag.toString()
            )
        }

        private fun mSaveMediaToStorage(
            currentInputConnection: InputConnection,
            currentInputEditorInfo: EditorInfo, context: Context,
            url: String
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val url = URL(url)
                val connection: HttpURLConnection?
                try {
                    connection = url.openConnection() as HttpURLConnection
                    connection.connect()
                    val inputStream: InputStream = connection.inputStream
                    val bufferedInputStream = BufferedInputStream(inputStream)
                    val bitmap = BitmapFactory.decodeStream(bufferedInputStream)

                    val filename = "temp.jpg"

                    File(
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES
                        ), filename
                    ).delete()
                    var fos: OutputStream? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        context.contentResolver?.also { resolver ->
                            val contentValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                                put(
                                    MediaStore.MediaColumns.RELATIVE_PATH,
                                    Environment.DIRECTORY_PICTURES
                                )
                            }
                            val imageUri: Uri? =
                                resolver.insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    contentValues
                                )
                            fos = imageUri?.let { resolver.openOutputStream(it) }
                        }
                    } else {
                        val imagesDir =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        val image = File(imagesDir, filename)
                        fos = FileOutputStream(image)
                    }
                    fos?.use {
                        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, it)
                        launch(Dispatchers.Main) {
//                        Toast.makeText(this@MyKeyboard, "Saved to Gallery", Toast.LENGTH_SHORT)
//                            .show()
                            doCommitContent(
                                currentInputConnection,
                                currentInputEditorInfo,
                                context,
                                File(
                                    Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_PICTURES
                                    ), filename
                                )
                            )
                        }
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
//                Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
                }
            }
        }

        private fun doCommitContent(
            currentInputConnection: InputConnection,
            currentInputEditorInfo: EditorInfo,
            context: Context,
            file: File
        ) {
            val file = getImageContentUri(context, file)!!
            val description = ClipDescription("image/png", arrayOf("image/png"))

            val inputContentInfoCompat = InputContentInfoCompat(
                file, description, null
            )

            InputConnectionCompat.commitContent(
                currentInputConnection,
                currentInputEditorInfo,
                inputContentInfoCompat,
                InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION,
                null
            )
        }

        @SuppressLint("Range")
        private fun getImageContentUri(context: Context, imageFile: File): Uri? {
            val filePath: String = imageFile.getAbsolutePath()
            val cursor: Cursor? = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.Media._ID),
                MediaStore.Images.Media.DATA + "=? ", arrayOf(filePath), null
            )
            return if (cursor != null && cursor.moveToFirst()) {
                val id: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                cursor.close()
                Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id)
            } else {
                if (imageFile.exists()) {
                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.DATA, filePath)
                    context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                    )
                } else {
                    null
                }
            }
        }
    }
}