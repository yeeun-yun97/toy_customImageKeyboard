package com.github.yeeun_yun97.toy.imagekeyboard.data

import android.content.Context

private const val SHARED_PREF_KEY = "tempKeyName"
private const val SHARED_KEY = "key"

class SharedPrefRepo private constructor(context: Context) {
    private val sharedPref =
        context.getSharedPreferences(
            SHARED_PREF_KEY,
            Context.MODE_PRIVATE
        )

    companion object {
        private lateinit var repo: SharedPrefRepo
        fun getInstance(context: Context): SharedPrefRepo {
            if (!this::repo.isInitialized)
                repo = SharedPrefRepo(context)
            return repo
        }
    }

    fun useData(callback: (String) -> Unit) {
        val data = sharedPref.getString(SHARED_KEY, "") ?: ""
        callback(data)
    }

    fun setData(value: String) {
        sharedPref.edit().apply() {
            this.putString(SHARED_KEY, value)
        }.apply()
    }


}