package com.github.yeeun_yun97.toy.imagekeyboard.service

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.github.yeeun_yun97.toy.imagekeyboard.R
import com.github.yeeun_yun97.toy.imagekeyboard.service.utils.SupportUtil
import com.github.yeeun_yun97.toy.imagekeyboard.service.utils.Util
import com.github.yeeun_yun97.toy.imagekeyboard.data.NaverSearchRepository


class MyKeyboard : InputMethodService() {
    private val repo = NaverSearchRepository.getInstance()


    override fun onCreate() {
        super.onCreate()
        repo.loadImages()
    }

    /**
     * 인풋 뷰를 생성하기
     */
    override fun onCreateInputView(): View {
        // root layout 생성
        val keyboardLayout =
            layoutInflater.inflate(R.layout.keyboard_layout, null) as RelativeLayout
        val imageContainer = keyboardLayout.findViewById(R.id.imageContainer) as LinearLayout

        val it = repo.imageLiveData.value!!
        Log.i("MIMI", "Size ${it.size}")
//        Toast.makeText(this@MyKeyboard, "size: ${it.size}", Toast.LENGTH_SHORT).show()
        var imageContainerColumn = layoutInflater.inflate(
            R.layout.image_container_column,
            imageContainer,
            false
        ) as LinearLayout
        currentInputEditorInfo
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
            imgButton.setOnClickListener { view ->
                Util.commitImage(view,currentInputConnection,currentInputEditorInfo)
            }
            imageContainerColumn.addView(imgButton)

            if ((i % rowCount) == 0) {
                imageContainer.addView(imageContainerColumn)
            }
        }

        return keyboardLayout;
    }

    /**
     * 인풋뷰를 시작할 때 -> MIME type 검증
     */
    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        val pngSupported = SupportUtil.isCommitContentSupported(
            this,
            currentInputConnection,
            info,
            "image/png",
            packageManager,
            currentInputBinding
        );

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

}