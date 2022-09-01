package com.github.yeeun_yun97.toy.imagekeyboard.service

import android.annotation.SuppressLint
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.yeeun_yun97.toy.imagekeyboard.R
import com.github.yeeun_yun97.toy.imagekeyboard.data.DataStoreRepo
import com.github.yeeun_yun97.toy.imagekeyboard.data.NaverSearchRepository
import com.github.yeeun_yun97.toy.imagekeyboard.data.SharedPrefRepo
import com.github.yeeun_yun97.toy.imagekeyboard.service.utils.SupportUtil
import com.github.yeeun_yun97.toy.imagekeyboard.service.utils.Util
import com.github.yeeun_yun97.toy.imagekeyboard.ui.MIMEListAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MyKeyboard : InputMethodService(), LifecycleOwner, LifecycleObserver {
    private val repo = NaverSearchRepository.getInstance()

    //Lifecycle Methods
    private var lifecycleRegistry = LifecycleRegistry(this)

    override fun onCreate() {
        super.onCreate()
        // setPrefRepo
        prefRepo = SharedPrefRepo.getInstance(applicationContext)
        storeRepo = DataStoreRepo.getInstance(applicationContext)

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
    private lateinit var progressBar: ProgressBar

    private lateinit var prefBtn: TextView
    private lateinit var dsButton: TextView
    private var targetPackageName = "com.github.yeeun_yun97.toy.imagekeyboard"


    private lateinit var prefRepo: SharedPrefRepo
    private lateinit var storeRepo: DataStoreRepo

    /**
     * 인풋 뷰를 생성하기
     */
    @SuppressLint("InflateParams")
    override fun onCreateInputView(): View {


        val layout = layoutInflater.inflate(R.layout.keyboard_my_layout, null)
        this.prefBtn = layout.findViewById(R.id.prefBtn)
        prefBtn.setOnClickListener {
            prefRepo.useData { Log.d("savedValues", "SharedPref - $it") }
        }
        this.dsButton = layout.findViewById(R.id.dsBtn)
        dsButton.setOnClickListener {
            storeRepo.useData { Log.d("savedValues", "DataStore - $it") }
        }
        this.chipGroup = layout.findViewById(R.id.chipGroup)
        this.progressBar = layout.findViewById(R.id.progressBar)
        val adapter = MIMEListAdapter { view ->
            Log.d("붙여넣기 시작", "클릭한 아이템 주소 = ${view!!.tag}")
            Log.d("붙여넣기 시작", "붙여넣을 앱 패키지명 = $targetPackageName")
            Util.commit(
                currentInputConnection,
                currentInputEditorInfo,
                view.context,
                view.tag.toString(),
                targetPackageName
            )
        }
        repo.loadImages()
        repo.imageLiveData.observeForever() { t ->
            Log.d("alert", "adapter set to ${t.toString()}")
            if (t.isNullOrEmpty()) progressBar.visibility = View.VISIBLE
            else progressBar.visibility = View.INVISIBLE
            adapter.setList(t)
        }

        val recyclerView: RecyclerView = layout.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
        recyclerView.adapter = adapter
        return layout
    }


    /**
     * 인풋뷰를 시작할 때 -> MIME type 검증
     */
    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        targetPackageName = info.packageName
        Log.d("타깃 앱의 패키지명", "패키지명 = ${info.packageName}")

        val supportedMimeTypes = EditorInfoCompat.getContentMimeTypes(info).toList()
        Log.d("칩 그룹", "설정할 것 $supportedMimeTypes")
        if (this::chipGroup.isInitialized) {
            chipGroup.removeAllViews() //이전에 추가한 칩 모두 제거
            if (supportedMimeTypes.isNullOrEmpty()) {
                val chip = Chip(chipGroup.context)
                chip.setText("NONE")
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
//                applicationContext,
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