package com.github.yeeun_yun97.toy.imagekeyboard.service

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.yeeun_yun97.toy.imagekeyboard.R
import com.github.yeeun_yun97.toy.imagekeyboard.data.NaverSearchRepository
import com.github.yeeun_yun97.toy.imagekeyboard.service.utils.SupportUtil
import com.github.yeeun_yun97.toy.imagekeyboard.service.utils.Util
import com.github.yeeun_yun97.toy.imagekeyboard.ui.MIMEListAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class MyKeyboard : InputMethodService(), LifecycleOwner, LifecycleObserver {
    private val repo = NaverSearchRepository.getInstance()

    //Lifecycle Methods
    private var lifecycleRegistry = LifecycleRegistry(this)

    override fun onCreate() {
        super.onCreate()
        Log.d("LIFECYCLE", "onCreate")
        handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    private fun handleLifecycleEvent(event: Lifecycle.Event) =
        lifecycleRegistry.handleLifecycleEvent(event)


    override fun onDestroy() {
        super.onDestroy()
        Log.d("LIFECYCLE", "onDestroy")
        handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    private lateinit var chipGroup: ChipGroup
    private lateinit var progressBar : ProgressBar

    /**
     * 인풋 뷰를 생성하기
     */
    @SuppressLint("InflateParams")
    override fun onCreateInputView(): View {
        val layout = layoutInflater.inflate(R.layout.keyboard_my_layout, null)
        this.chipGroup = layout.findViewById(R.id.chipGroup)
        this.progressBar = layout.findViewById(R.id.progressBar)
        val adapter = MIMEListAdapter { view ->
            Log.d("~~~", "onClick -${view!!.tag}")
            Util.commitImage(view, currentInputConnection, currentInputEditorInfo)
        }
        repo.loadImages()
        repo.imageLiveData.observeForever() { t ->
            Log.d("alert", "adapter set to ${t.toString()}")
            if(t.isNullOrEmpty())progressBar.visibility = View.VISIBLE
            else progressBar.visibility = View.INVISIBLE
            adapter.setList(t)
        }

        val recyclerView: RecyclerView = layout.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
        recyclerView.adapter = adapter
        return layout


        // root layout 생성
        /*
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

        return keyboardLayout;*/
    }

    /**
     * 인풋뷰를 시작할 때 -> MIME type 검증
     */
    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {

        val supportedMimeTypes = EditorInfoCompat.getContentMimeTypes(info).toList()
        if (this::chipGroup.isInitialized) {
            chipGroup.removeAllViews()
            Log.d("칩 그룹", "설정할 것 ${supportedMimeTypes.toString()}")
            if (supportedMimeTypes.isNullOrEmpty()) {
                val chip = Chip(chipGroup.context)
                chip.setText("MIME 없음")
                chipGroup.addView(chip)
            } else
                for (mime in supportedMimeTypes) {
                    val chip = Chip(chipGroup.context)
                    chip.setText(mime)
                    chipGroup.addView(chip)
                }
        }


        val pngSupported = SupportUtil.isCommitContentSupported(
            this,
            currentInputConnection,
            info,
            "image/png",
            packageManager,
            currentInputBinding
        )

        if (!pngSupported) {
//            Toast.makeText(
//                getApplicationContext(),
//                "Images not supported here. Please change to another keyboard.",
//                Toast.LENGTH_SHORT
//            ).show()
        }
    }

    /**
     * Disable Full ScreenMode
     */
    override fun onEvaluateFullscreenMode() = false


}