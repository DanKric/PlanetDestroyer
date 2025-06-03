package com.example.planetdestroyer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.planetdestroyer.MainGameActivity
import com.example.planetdestroyer.R
import com.example.planetdestroyer.StartActivity

class GameOverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        val finalScore = intent.getIntExtra("FINAL_SCORE", 0)

        val scoreLabel = findViewById<TextView>(R.id.gameOver_LBL_score)
        val restartBtn = findViewById<Button>(R.id.gameOver_BTN_restart)
        val menuBtn = findViewById<Button>(R.id.gameOver_BTN_menu)

        scoreLabel.text = "Your Score: $finalScore"

        restartBtn.setOnClickListener {
            val intent = Intent(this, MainGameActivity::class.java)
            startActivity(intent)
            finish()
        }

        menuBtn.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
