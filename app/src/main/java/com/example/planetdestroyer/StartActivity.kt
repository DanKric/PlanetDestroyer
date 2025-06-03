package com.example.planetdestroyer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val toggleSpeed = findViewById<ToggleButton>(R.id.start_TGL_speed)
        val isFast: () -> Boolean = { toggleSpeed.isChecked }

        val buttonMode = findViewById<Button>(R.id.start_BTN_buttons)
        val sensorMode = findViewById<Button>(R.id.start_BTN_sensor)

        buttonMode.setOnClickListener {
            launchGame(sensorMode = false, fastMode = isFast())
        }

        sensorMode.setOnClickListener {
            launchGame(sensorMode = true, fastMode = isFast())
        }
    }

    private fun launchGame(sensorMode: Boolean, fastMode: Boolean) {
        val intent = Intent(this, MainGameActivity::class.java)
        intent.putExtra("SENSOR_MODE", sensorMode)
        intent.putExtra("FAST_MODE", fastMode)
        startActivity(intent)
    }
}
