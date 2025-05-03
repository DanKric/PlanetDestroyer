package com.example.planetdestroyer

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MainGameActivity : AppCompatActivity() {

    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private val rows = 4
    private val cols = 3
    private val delay: Long = 700

    private var currentLane = 1 // Start in the center lane
    private var lives = 3
    private var score = 0


    private var meteorMatrix = Array(rows) { IntArray(cols) { 0 } } // 0 = empty, 1 = meteor
    private var meteorViews = Array(rows) { arrayOfNulls<ImageView>(cols) }

    private val meteorRes = R.drawable.comet
    private val earthRes = R.drawable.earth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_ui)

        initViews()
        startGameLoop()
    }

    private fun initViews() {
        heart1 = findViewById(R.id.game_IMG_heart1)
        heart2 = findViewById(R.id.game_IMG_heart2)
        heart3 = findViewById(R.id.game_IMG_heart3)

        val ids = arrayOf(
            arrayOf(R.id.game_IMG_meteor0, R.id.game_IMG_meteor3, R.id.game_IMG_meteor6),
            arrayOf(R.id.game_IMG_meteor1, R.id.game_IMG_meteor4, R.id.game_IMG_meteor7),
            arrayOf(R.id.game_IMG_meteor2, R.id.game_IMG_meteor5, R.id.game_IMG_meteor8),
            arrayOf(R.id.game_IMG_earth_0, R.id.game_IMG_earth_1, R.id.game_IMG_earth_2)
        )

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                meteorViews[i][j] = findViewById(ids[i][j])
                meteorViews[i][j]?.setImageResource(0)
            }
        }

        showCarAtLane(currentLane)

        findViewById<ExtendedFloatingActionButton>(R.id.game_BTN_left).setOnClickListener {
            moveCar(-1)
        }

        findViewById<ExtendedFloatingActionButton>(R.id.game_BTN_right).setOnClickListener {
            moveCar(1)
        }
    }

    private fun startGameLoop() {
        runnable = object : Runnable {
            override fun run() {
                updateMeteorMatrix()
                updateMeteorViews()
                checkCollision()
                handler.postDelayed(this, delay)
            }
        }
        handler.postDelayed(runnable, delay)
    }

    private fun updateMeteorMatrix() {
        for (i in rows - 1 downTo 1) {
            for (j in 0 until cols) {
                meteorMatrix[i][j] = meteorMatrix[i - 1][j]
            }
        }

        for (j in 0 until cols) meteorMatrix[0][j] = 0

        val row1IsEmpty = meteorMatrix[1].all { it == 0 }
        if (row1IsEmpty) {
            val newCol = (0 until cols).random()
            meteorMatrix[0][newCol] = 1
        }
    }


    private fun updateMeteorViews() {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val img = meteorViews[i][j]
                if (i == rows - 1 && j == currentLane) {
                    img?.setImageResource(earthRes)
                } else if (meteorMatrix[i][j] == 1) {
                    img?.setImageResource(meteorRes)
                } else {
                    img?.setImageResource(0)
                }

            }
        }

        showCarAtLane(currentLane)
    }


    private fun checkCollision() {
        for (j in 0 until cols) {
            if (meteorMatrix[3][j] == 1) {
                if (j == currentLane) {
                    lives--
                    updateHeartsUI()
                    vibrate()

                    val toast = Toast.makeText(this, "💥 Ouch! Lives left: $lives", Toast.LENGTH_SHORT)
                    toast.show()
                    Handler(Looper.getMainLooper()).postDelayed({ toast.cancel() }, 600)
                } else {
                    score+=100
                    val toast = Toast.makeText(this, "✅ Safe! Score: $score", Toast.LENGTH_SHORT)
                    toast.show()
                    Handler(Looper.getMainLooper()).postDelayed({ toast.cancel() }, 600)
                }

                meteorMatrix[3][j] = 0
                if (j != currentLane) {
                    meteorViews[3][j]?.setImageResource(0)
                }
            }
        }
    }
    private fun updateHeartsUI() {
        when (lives) {
            2 -> heart3.setImageResource(0)
            1 -> heart2.setImageResource(0)
            0 -> heart1.setImageResource(0)
        }
    }


    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(300)
        }
    }

    private fun showCarAtLane(lane: Int) {
        for (j in 0 until cols) {
            meteorViews[3][j]?.setImageResource(if (j == lane) earthRes else 0)
        }
    }

    private fun moveCar(direction: Int) {
        currentLane += direction
        if (currentLane < 0) currentLane = 0
        if (currentLane > 2) currentLane = 2
        showCarAtLane(currentLane)
    }
}
