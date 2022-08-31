package com.github.yeeun_yun97.toy.imagekeyboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.yeeun_yun97.toy.imagekeyboard.data.ImageItem
import com.github.yeeun_yun97.toy.imagekeyboard.data.ImageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyViewModel : ViewModel() {
    private val service = ImageService.getInstance()
    private val _imageLiveData: MutableLiveData<List<ImageItem>> = MutableLiveData(listOf())
    val imageLiveData: LiveData<List<ImageItem>> get() = _imageLiveData

    fun loadImages() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getImages()
            if (response.isSuccessful) {
                _imageLiveData.postValue(response.body()!!.items)
            }
        }
    }


}