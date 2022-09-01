package com.github.yeeun_yun97.toy.imagekeyboard.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val DATASTORE_PREF_KEY = "settings"
private val DATASTORE_KEY= stringPreferencesKey("key")

class DataStoreRepo private constructor(context: Context) {
    private val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(produceFile = {
            context.preferencesDataStoreFile(DATASTORE_PREF_KEY)
        })

    companion object {
        private lateinit var repo: DataStoreRepo
        fun getInstance(context: Context): DataStoreRepo {
            if (!this::repo.isInitialized) {
                repo = DataStoreRepo(context)
            }
            return repo
        }
    }

    fun useData(callback:(String)->Unit){
        CoroutineScope(Dispatchers.IO).launch {
            val data: String = dataStore.data
                .map { preferences ->
                    preferences[DATASTORE_KEY] ?: ""
                }.first()
            callback(data)
        }
    }

    fun setData(data:String){
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { settings ->
                settings[DATASTORE_KEY] = data
            }
        }
    }


}