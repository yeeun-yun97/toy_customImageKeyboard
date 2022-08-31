package com.github.yeeun_yun97.toy.imagekeyboard

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.ClipDescription
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.lifecycle.*
import com.bumptech.glide.Glide
import com.github.yeeun_yun97.toy.imagekeyboard.data.ImageItem
import com.github.yeeun_yun97.toy.imagekeyboard.data.ImageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class MyKeyboard : InputMethodService() {
    private val service = ImageService.getInstance()
    private val _imageLiveData: MutableLiveData<List<ImageItem>> = MutableLiveData(listOf())
    private val TAG = "ImageKeyboard"
    private val MIME_TYPE_PNG = "image/png"

    private fun loadImages() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getImages()
            if (response.isSuccessful) {
                val items = response.body()!!.items
                _imageLiveData.postValue(items)
                Log.d("RERE", "success, ${items}")
            } else {
                Log.d("RERE", "failed, ${response.code()}")
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        loadImages()
    }

    /**
     * 인풋 뷰를 생성하기
     */
    override fun onCreateInputView(): View {
        // root layout 생성
        val keyboardLayout =
            layoutInflater.inflate(R.layout.keyboard_layout, null) as RelativeLayout
        val imageContainer = keyboardLayout.findViewById(R.id.imageContainer) as LinearLayout

        val it = _imageLiveData.value!!
        Log.i("MIMI", "Size ${it.size}")
//        Toast.makeText(this@MyKeyboard, "size: ${it.size}", Toast.LENGTH_SHORT).show()
        var imageContainerColumn = layoutInflater.inflate(
            R.layout.image_container_column,
            imageContainer,
            false
        ) as LinearLayout

        val rowCount = 3

        for (i in it.indices) {
            if ((i % rowCount) == 0) {
                imageContainerColumn = layoutInflater.inflate(
                    R.layout.image_container_column,
                    imageContainer,
                    false
                ) as LinearLayout
            }

            // Creating button
            val imgButton = layoutInflater.inflate(
                R.layout.image_button,
                imageContainerColumn,
                false
            ) as ImageButton
            imgButton.setTag(it[i].thumbnail)
            Glide.with(this)
                .load(it[i].thumbnail)
                .centerCrop()
                .into(imgButton)
            imgButton.setOnClickListener(this::commitImage)
            imageContainerColumn.addView(imgButton)

            if ((i % rowCount) == 0) {
                imageContainer.addView(imageContainerColumn)
            }
        }

        return keyboardLayout;
    }

    private fun commitImage(view: View) {
//        Toast.makeText(this, view.tag.toString(), Toast.LENGTH_SHORT).show()
        mSaveMediaToStorage(url = view.tag.toString())

    }

    private fun mSaveMediaToStorage(url: String) {
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
                    this@MyKeyboard.contentResolver?.also { resolver ->
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

    @SuppressLint("Range")
    fun getImageContentUri(context: Context, imageFile: File): Uri? {
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


    private fun doCommitContent(file:File) {
        val fileCheck = file.absolutePath
        val uri_file = getImageContentUri(this, file)!!
        val contentUriCheck = uri_file.toString()

        val description = ClipDescription("image/png", arrayOf(MIME_TYPE_PNG))

        // error here, contentUri must have content scheme
        val inputContentInfoCompat = InputContentInfoCompat(
            uri_file, description, null
        )

        InputConnectionCompat.commitContent(
            currentInputConnection,
            currentInputEditorInfo,
            inputContentInfoCompat,
            INPUT_CONTENT_GRANT_READ_URI_PERMISSION,
            null
        )



    }

    /**
     * 인풋뷰를 시작할 때 -> MIME type 검증
     */
    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        val pngSupported = isCommitContentSupported(info, MIME_TYPE_PNG);

        if (!pngSupported) {
//            Toast.makeText(
//                getApplicationContext(),
//                "Images not supported here. Please change to another keyboard.",
//                Toast.LENGTH_SHORT
//            ).show();
        }
    }

    /**
     * Disable Full ScreenMode
     */
    override fun onEvaluateFullscreenMode() = false


    /**
     * 컨텐츠 타입이 맞는지 확인하는 함수
     */
    private fun isCommitContentSupported(
        editorInfo: EditorInfo?, mimeType: String
    ): Boolean {
        if (editorInfo == null) {
            return false
        }
        val ic = currentInputConnection ?: return false
        if (!validatePackageName(editorInfo)) {
            return false
        }
        val supportedMimeTypes = EditorInfoCompat.getContentMimeTypes(editorInfo)
        println(editorInfo)
        for (supportedMimeType in supportedMimeTypes) {
            if (ClipDescription.compareMimeTypes(mimeType, supportedMimeType)) {
                return true
            }
        }
        return false
    }

    private fun validatePackageName(editorInfo: EditorInfo?): Boolean {
        if (editorInfo == null) {
            return false
        }
        val packageName = editorInfo.packageName ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true
        }
        val inputBinding = currentInputBinding
        if (inputBinding == null) {
            // Due to b.android.com/225029, it is possible that getCurrentInputBinding() returns
            // null even after onStartInputView() is called.
            // TODO: Come up with a way to work around this bug....
            Log.e(
                TAG, "inputBinding should not be null here. "
                        + "You are likely to be hitting b.android.com/225029"
            )
            return false
        }
        val packageUid = inputBinding.uid
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val appOpsManager = getSystemService(APP_OPS_SERVICE) as AppOpsManager
            try {
                appOpsManager.checkPackage(packageUid, packageName)
            } catch (e: Exception) {
                return false
            }
            return true
        }
        val packageManager = packageManager
        val possiblePackageNames = packageManager.getPackagesForUid(packageUid)
        for (possiblePackageName in possiblePackageNames!!) {
            if (packageName == possiblePackageName) {
                return true
            }
        }
        return false
    }


}