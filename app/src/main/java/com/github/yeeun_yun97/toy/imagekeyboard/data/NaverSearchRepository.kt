package com.github.yeeun_yun97.toy.imagekeyboard.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NaverSearchRepository private constructor() {
    private val service = ImageService.getInstance()
    private val _imageLiveData: MutableLiveData<List<ImageItem>> = MutableLiveData(listOf())
    val imageLiveData : LiveData<List<ImageItem>> get() = _imageLiveData

    companion object{
        private lateinit var repo : NaverSearchRepository
        fun getInstance(): NaverSearchRepository {
            if(!this::repo.isInitialized){
                repo = NaverSearchRepository()
            }
            return repo
        }
    }

    fun loadImages() {
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



}