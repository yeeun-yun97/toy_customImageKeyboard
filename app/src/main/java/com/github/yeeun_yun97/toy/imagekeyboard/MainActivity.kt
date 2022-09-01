package com.github.yeeun_yun97.toy.imagekeyboard

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.github.yeeun_yun97.toy.imagekeyboard.data.DataStoreRepo
import com.github.yeeun_yun97.toy.imagekeyboard.data.SharedPrefRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    //view
    private lateinit var button: Button
    private lateinit var prefEditText: EditText
    private lateinit var dsEditText: EditText

    private lateinit var prefRepo: SharedPrefRepo
    private lateinit var dataStoreRepo: DataStoreRepo


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefRepo = SharedPrefRepo.getInstance(applicationContext)
        dataStoreRepo = DataStoreRepo.getInstance(applicationContext)

        //find View
        button = findViewById(R.id.button)
        prefEditText = findViewById(R.id.prefEditText)
        dsEditText = findViewById(R.id.dsEditText)


        // handle user click event
        button.setOnClickListener { onClick() }
    }

    override fun onResume() {
        super.onResume()
        this.prefRepo.useData(::showData)
        this.dataStoreRepo.useData(::showData)
    }

    private fun showData(data: String) {
        CoroutineScope(Dispatchers.IO).launch(Dispatchers.Main) {
            Toast.makeText(
                this@MainActivity,
                "onResume check pref - $data",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun onClick() {
        prefRepo.setData(prefEditText.text.toString())
        dataStoreRepo.setData(dsEditText.text.toString())
    }
}