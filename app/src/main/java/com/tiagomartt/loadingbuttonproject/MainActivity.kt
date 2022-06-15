package com.tiagomartt.loadingbuttonproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tiagomartt.loadingbutton.LoadingButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loadingButton = findViewById<LoadingButton>(R.id.loadingButton)

        loadingButton.setOnClickListener {
            loadingButton.hide()
        }
    }
}