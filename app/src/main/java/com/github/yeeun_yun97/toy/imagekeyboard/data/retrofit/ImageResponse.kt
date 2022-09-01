package com.github.yeeun_yun97.toy.imagekeyboard.data.retrofit

data class ImageItem(val title:String,val link:String, val thumbnail:String)

data class ImageResponse(
    val items:List<ImageItem>
)
