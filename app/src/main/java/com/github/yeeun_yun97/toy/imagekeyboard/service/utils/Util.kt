package com.github.yeeun_yun97.toy.imagekeyboard.service.utils

import android.annotation.SuppressLint
import android.content.ClipDescription
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class Util {
    companion object {
        fun commit(
            currentInputConnection: InputConnection,
            currentInputEditorInfo: EditorInfo, context: Context,
            url: String, packageName: String
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bitmap = Glide.with(context.applicationContext)
                        .asBitmap()
                        .load(url) // sample image
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal) // need placeholder to avoid issue like glide annotations
                        .error(android.R.drawable.stat_notify_error) // need error to avoid issue like glide annotations
                        .submit()
                        .get()

                    val fileName = "temp.png"
                    val imageFile = File(context.filesDir, fileName)

                    if (imageFile.exists()) {
                        imageFile.delete()
                    }
                    var outputStream: OutputStream? = FileOutputStream(imageFile)

//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        context.contentResolver?.also { resolver ->
//                            val contentValues = ContentValues().apply {
//                                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//                                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
//                                put(
//                                    MediaStore.MediaColumns.RELATIVE_PATH,
//                                    Environment.DIRECTORY_PICTURES
//                                )
//                            }
//                            val imageUri: Uri? =
//                                resolver.insert(
//                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                                    contentValues
//                                )
//                            outputStream = imageUri?.let { resolver.openOutputStream(it) }
//                        }
//                    }

                    if (outputStream == null) {
                        Log.d("오류", "outputStream이 널임")
                    } else {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream!!.close()
                        val savedImagePath = imageFile.absolutePath
                        if (imageFile.exists()) {
                            Log.d("파일 저장함", "경로 = $savedImagePath")

                            when (packageName) {
                                "com.kakao.talk" -> {
                                    Log.d("파일 공유하기", "카카오")
                                    doCommitKakao(context, imageFile,bitmap)
                                }
                                else -> {
                                    Log.d("파일 공유하기", "다른 앱")
                                    doCommitContent(
                                        currentInputConnection,
                                        currentInputEditorInfo,
                                        context,
                                        imageFile
                                    )
                                }
                            }
                        }

                    }
                } catch (e: Exception) {
                    Log.e("exception", e.message ?: "exception!!")
                    e.printStackTrace()
                }


            }
        }

        private fun doCommitKakao(context: Context, imgFile: File,bitmap:Bitmap) {
            val dataUri = FileProvider.getUriForFile(
                context.applicationContext,
                "com.github.yeeun_yun97.toy.imagekeyboard.inputcontent",
                imgFile
            )
            val intent = Intent(Intent.ACTION_SEND)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            intent.setType("text/plain");		// 고정 text
//                            intent.putExtra(Intent.EXTRA_TEXT, "");

            intent.type = "image/png"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra("image",bitmap)
            intent.putExtra(Intent.EXTRA_STREAM, dataUri)
            Log.d("인텐트 보내기 전에 확인", "uri = $dataUri")
//                            intent.`package` = "com.kakao.talk"
//                            context.startActivity(intent)
            val chooser = Intent.createChooser(intent, "공유")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(chooser)
        }

        private fun doCommitContent(
            currentInputConnection: InputConnection,
            currentInputEditorInfo: EditorInfo,
            context: Context,
            file: File
        ) {
            if(!file.exists()){
                Log.e("커밋 오류","파일이 존재하지 않음")
            }
            if(!file.canRead()){
                Log.e("커밋 오류","파일을 읽을 수 없음")
            }
            val file = FileProvider.getUriForFile(
                context.applicationContext,
                "com.github.yeeun_yun97.toy.imagekeyboard.inputcontent",
                file
            )
//            val file = getImageContentUri(context, file)!!
            val description = ClipDescription("image/png", arrayOf("image/png"))
            val inputContentInfoCompat = InputContentInfoCompat(file, description, null)

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