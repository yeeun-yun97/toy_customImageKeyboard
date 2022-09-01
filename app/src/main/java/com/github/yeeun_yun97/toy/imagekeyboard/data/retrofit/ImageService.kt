package com.github.yeeun_yun97.toy.imagekeyboard.data.retrofit

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ImageService {
    companion object {
        private const val BASE_URL = "https://openapi.naver.com/"
        private lateinit var service: ImageService

        fun getInstance(): ImageService {
            if (!this::service.isInitialized) {
                service = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ImageService::class.java)
            }
            return service
        }
    }

    @Headers(
        "X-Naver-Client-Id:vC6PKe4D5hQ8DE9yEzXS",
        "X-Naver-Client-Secret:jCxquNgF1U"
    )
    @GET("v1/search/image")
    suspend fun getImages(
        @Query("query", encoded = true) query: String = "강아지",
        @Query("start", encoded = true) start: Int = 1,
        @Query("display", encoded = true) display: Int = 100,
    ): Response<ImageResponse>

}