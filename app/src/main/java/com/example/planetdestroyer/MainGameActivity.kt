package com.example.planetdestroyer

import android.util.Log
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.annotation.SuppressLint
import android.view.View
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


class MainGameActivity : AppCompatActivity(), SensorEventListener {

    private var gameOver = false

    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView

    private var distance = 0
    private lateinit var distanceLabel: TextView
    private lateinit var scoreLabel: TextView

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastTiltTime: Long = 0
    private val tiltCooldown: Long = 300 // ms
    private var isSensorMode: Boolean = false
    private var isFastMode: Boolean = false
    private var tiltSpeedMode: String = "NORMAL"



    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private val rows = 5
    private val cols = 5
    private val delay: Long by lazy { if (isFastMode) 550 else 850 }

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
    private var coinSound: MediaPlayer? = null


    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.game_ui) // ✅ This must be FIRST

        isSensorMode = intent.getBooleanExtra("SENSOR_MODE", false)
        isFastMode = intent.getBooleanExtra("FAST_MODE", false)

        scoreLabel = findViewById(R.id.game_LBL_score)
        coinSound = MediaPlayer.create(this, R.raw.coin_collect)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        explosionSound = MediaPlayer.create(this, R.raw.meteor_explosion)

        initViews()
        startGameLoop()
    }


    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        handler.removeCallbacks(runnable)

    }


    override fun onDestroy() {
        super.onDestroy()
        explosionSound?.release()
        explosionSound = null
        coinSound?.release()
        coinSound = null

    }


    private fun initViews() {
        heart1 = findViewById(R.id.game_IMG_heart1)
        heart2 = findViewById(R.id.game_IMG_heart2)
        heart3 = findViewById(R.id.game_IMG_heart3)
        scoreLabel = findViewById(R.id.game_LBL_score)

        val ids = arrayOf(
            arrayOf(
                R.id.game_IMG_meteor0,
                R.id.game_IMG_meteor1,
                R.id.game_IMG_meteor2,
                R.id.game_IMG_meteor15,
                R.id.game_IMG_earth_0
            ),
            arrayOf(
                R.id.game_IMG_meteor3,
                R.id.game_IMG_meteor4,
                R.id.game_IMG_meteor5,
                R.id.game_IMG_meteor16,
                R.id.game_IMG_earth_1
            ),
            arrayOf(
                R.id.game_IMG_meteor6,
                R.id.game_IMG_meteor7,
                R.id.game_IMG_meteor8,
                R.id.game_IMG_meteor17,
                R.id.game_IMG_earth_2
            ),
            arrayOf(
                R.id.game_IMG_meteor9,
                R.id.game_IMG_meteor10,
                R.id.game_IMG_meteor11,
                R.id.game_IMG_meteor18,
                R.id.game_IMG_earth_3
            ),
            arrayOf(
                R.id.game_IMG_meteor12,
                R.id.game_IMG_meteor13,
                R.id.game_IMG_meteor14,
                R.id.game_IMG_meteor19,
                R.id.game_IMG_earth_4
            )
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
        if (isSensorMode) {
            findViewById<ExtendedFloatingActionButton>(R.id.game_BTN_left).visibility = View.GONE
            findViewById<ExtendedFloatingActionButton>(R.id.game_BTN_right).visibility = View.GONE
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
            }
        }
        handler.postDelayed(runnable, delay)
    }

    private fun updateMeteorMatrix() {
        // Shift all meteors down
        for (i in rows - 1 downTo 1) {
            for (j in 0 until cols) {
                meteorMatrix[i][j] = meteorMatrix[i - 1][j]
            }
        }

        // Clear top row
        for (j in 0 until cols) {
            meteorMatrix[0][j] = 0
        }

        val row1IsEmpty = meteorMatrix[1].all { it == 0 }
        if (row1IsEmpty) {
            meteorSpawnCounter++
            val numMeteors = (2..4).random() // 2–4 meteors

            val availableCols = (0 until cols).shuffled().take(numMeteors)
            for (col in availableCols) {
                meteorMatrix[0][col] = 1 // meteor
            }

            val shouldSpawnCoin = meteorSpawnCounter % 2 == 0 && (0..2).random() == 0
            if (shouldSpawnCoin) {
                val emptyCols = (0 until cols).filter { meteorMatrix[0][it] == 0 }
                if (emptyCols.isNotEmpty()) {
                    val coinCol = emptyCols.random()
                    meteorMatrix[0][coinCol] = 2
                }
            }
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
        if (gameOver) return
        for (j in 0 until cols) {
            when (meteorMatrix[4][j]) {
                1 -> { // Meteor
                    if (j == currentLane) {
                        lives--
                        updateHeartsUI()
                        if (explosionSound?.isPlaying == true) {
                            explosionSound?.pause()
                            explosionSound?.seekTo(0)
                        }
                        explosionSound?.start()
                        vibrate()
                        val toast = Toast.makeText(this, "💥 Ouch! Lives left: $lives", Toast.LENGTH_SHORT)
                        toast.show()
                        Handler(Looper.getMainLooper()).postDelayed({ toast.cancel() }, 800)
                    }
                    meteorMatrix[4][j] = 0
                    meteorViews[4][j]?.setImageResource(0)
                    showCarAtLane(currentLane)
                }

                2 -> { // Coin
                    if (j == currentLane) {
                        coinsCollected++
                        val toast = Toast.makeText(
                            this,
                            "💰 Collected! coins: $coinsCollected",
                            Toast.LENGTH_SHORT
                        )
                        if (coinSound?.isPlaying == true) {
                            coinSound?.pause()
                            coinSound?.seekTo(0)
                        }
                        coinSound?.start()
                        toast.show()
                        Handler(Looper.getMainLooper()).postDelayed({ toast.cancel() }, 800)
                        meteorMatrix[4][j] = 0
                        meteorViews[4][j]?.setImageResource(0)
                        distance += coinsCollected
                        scoreLabel.text = "Score: $distance"
                        showCarAtLane(currentLane)
                    }
                }
            }
        }

        // ✅ Check if game is over AFTER all collisions are handled
        if (lives <= 0) {
            gameOver = true // ✅ This prevents repeated calls
            handler.removeCallbacks(runnable)
            sensorManager.unregisterListener(this)

            val intent = Intent(this, GameOverActivity::class.java)
            intent.putExtra("FINAL_SCORE", distance)
            Log.d("MainGameActivity", "GAME OVER TRIGGERED")

            startActivity(intent)
            finish() // Kill this activity properly
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

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val isEmulator = android.os.Build.FINGERPRINT.contains("generic")
            val tiltX = if (isEmulator) event.values[2] else event.values[0] // Left/Right
            val tiltZ = event.values[2] // Forward/Backward based on your test

            val now = System.currentTimeMillis()
            if (now - lastTiltTime > tiltCooldown) {

                // Left/Right movement (for sensor mode only)
                if (isSensorMode) {
                    if (tiltX > 3) {
                        moveCar(1)
                        lastTiltTime = now
                    } else if (tiltX < -3) {
                        moveCar(-1)
                        lastTiltTime = now
                    }
                }

                // Forward/Backward tilt controls speed (affects both modes)
                when {
                    tiltZ < -4 && tiltSpeedMode != "FAST" -> {
                        tiltSpeedMode = "FAST"
                        restartGameLoop(newDelay = 550)
                        Toast.makeText(this, "🐇 Fast Mode", Toast.LENGTH_SHORT).show()
                    }
                    tiltZ > 7 && tiltSpeedMode != "SLOW" -> {
                        tiltSpeedMode = "SLOW"
                        restartGameLoop(newDelay = 850)
                        Toast.makeText(this, "🐢 Slow Mode", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun restartGameLoop(newDelay: Long) {
        handler.removeCallbacks(runnable)
        runnable = object : Runnable {
            override fun run() {
                updateMeteorMatrix()
                updateMeteorViews()
                checkCollision()
                handler.postDelayed(this, newDelay)
                distance++
                scoreLabel.text = "Score: $distance"
            }
        }
        handler.postDelayed(runnable, newDelay)
    }



    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // not needed for this game
    }

}
