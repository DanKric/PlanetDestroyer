package com.example.planetdestroyer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val startButton = findViewById<ExtendedFloatingActionButton>(R.id.start_FAB_button)
        startButton.setOnClickListener {
            val intent = Intent(this, MainGameActivity::class.java)
            startActivity(intent)
        }
    }
}