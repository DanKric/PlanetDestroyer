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
import android.media.MediaPlayer
import android.widget.TextView


class MainGameActivity : AppCompatActivity() {

    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView

    private var distance = 0
    private lateinit var distanceLabel: TextView
    private lateinit var scoreLabel: TextView



    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private val rows = 5
    private val cols = 5
    private val delay: Long = 700

    private var currentLane = 1 // Start in the center lane
    private var lives = 3
    private var score = 0
    private var meteorSpawnCounter = 0
    private var coinsCollected = 0


    private var meteorMatrix = Array(rows) { IntArray(cols) { 0 } } // 0 = empty, 1 = meteor
    private var meteorViews = Array(rows) { arrayOfNulls<ImageView>(cols) }

    private val meteorRes = R.drawable.comet
    private val earthRes = R.drawable.earth
    private var explosionSound: MediaPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        distanceLabel = findViewById(R.id.game_LBL_distance)
        scoreLabel = findViewById(R.id.game_LBL_score)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_ui)
        explosionSound = MediaPlayer.create(this, R.raw.meteor_explosion)
        initViews()
        startGameLoop()
    }

    private fun initViews() {
        heart1 = findViewById(R.id.game_IMG_heart1)
        heart2 = findViewById(R.id.game_IMG_heart2)
        heart3 = findViewById(R.id.game_IMG_heart3)
        scoreLabel = findViewById(R.id.game_LBL_score)
        distanceLabel = findViewById(R.id.game_LBL_distance)



        val ids = arrayOf(
            arrayOf(R.id.game_IMG_meteor0,  R.id.game_IMG_meteor1,  R.id.game_IMG_meteor2,  R.id.game_IMG_meteor15, R.id.game_IMG_earth_0),
            arrayOf(R.id.game_IMG_meteor3,  R.id.game_IMG_meteor4,  R.id.game_IMG_meteor5,  R.id.game_IMG_meteor16, R.id.game_IMG_earth_1),
            arrayOf(R.id.game_IMG_meteor6,  R.id.game_IMG_meteor7,  R.id.game_IMG_meteor8,  R.id.game_IMG_meteor17, R.id.game_IMG_earth_2),
            arrayOf(R.id.game_IMG_meteor9,  R.id.game_IMG_meteor10, R.id.game_IMG_meteor11, R.id.game_IMG_meteor18, R.id.game_IMG_earth_3),
            arrayOf(R.id.game_IMG_meteor12, R.id.game_IMG_meteor13, R.id.game_IMG_meteor14, R.id.game_IMG_meteor19, R.id.game_IMG_earth_4)
        )



        for (i in 0 until rows) {
            for (j in 0 until cols) {
                meteorViews[i][j] = findViewById(ids[j][i])
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
                distance++
                scoreLabel.text = "Score: $distance"
                distanceLabel.text = "Dis: $distance"


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
            meteorSpawnCounter++

            val shouldSpawnCoin = meteorSpawnCounter % 2 == 0 && (0..2).random() == 0 // 16% at random
            meteorMatrix[0][newCol] = if (shouldSpawnCoin) 2 else 1
        }
    }




    private fun updateMeteorViews() {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val img = meteorViews[i][j]
                if (i == rows - 1 && j == currentLane) {
                    // Show the player (Earth) at the last row in the current lane
                    img?.setImageResource(earthRes)
                } else {
                    when (meteorMatrix[i][j]) {
                        1 -> img?.setImageResource(meteorRes)       // Meteor
                        2 -> img?.setImageResource(R.drawable.coin) // Coin (add coin image to your drawable)
                        else -> img?.setImageResource(0)            // Empty
                    }
                }
            }
        }
        showCarAtLane(currentLane)
    }



    private fun checkCollision() {
        for (j in 0 until cols) {
            when (meteorMatrix[4][j]) {
                1 -> { // Meteor
                    if (j == currentLane) {
                        lives--
                        updateHeartsUI()
                        explosionSound?.start()
                        vibrate()
                        val toast = Toast.makeText(this, "💥 Ouch! Lives left: $lives", Toast.LENGTH_SHORT)
                        toast.show()
                        Handler(Looper.getMainLooper()).postDelayed({ toast.cancel() }, 800)
                    } else {
                    }
                    meteorMatrix[4][j] = 0
                    meteorViews[4][j]?.setImageResource(0)
                    showCarAtLane(currentLane)
                }

                2 -> { // Coin
                    if (j == currentLane) {
                        coinsCollected++
                        val toast = Toast.makeText(this, "💰 Collected! coins: $coinsCollected", Toast.LENGTH_SHORT)
                        toast.show()
                        Handler(Looper.getMainLooper()).postDelayed({ toast.cancel() }, 800)
                        meteorMatrix[4][j] = 0
                        meteorViews[4][j]?.setImageResource(0)
                        if (j == currentLane) {
                            coinsCollected++
                            distance += coinsCollected
                            scoreLabel.text = "Score: $distance"
                            distanceLabel.text = "Dis: $distance"
                        }
                        showCarAtLane(currentLane)
                    }
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
            meteorViews[4][j]?.setImageResource(if (j == lane) earthRes else 0)
        }
    }


    private fun moveCar(direction: Int) {
        currentLane += direction
        if (currentLane < 0) currentLane = 0
        if (currentLane > 4) currentLane = 4
        showCarAtLane(currentLane)
    }

    override fun onDestroy() {
        super.onDestroy()
        explosionSound?.release()
        explosionSound = null
    }

    private fun calculateMultiplier(): Double {
        return when {
            coinsCollected >= 10 -> coinsCollected * 1.05
            coinsCollected == 0 -> 1.0
            else -> {
                val base = 2.0 - 0.1 * (coinsCollected - 1)
                coinsCollected * base
            }
        }
    }


}

