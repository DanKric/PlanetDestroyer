package com.example.planetdestroyer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.planetdestroyer.HighScoresActivity

class GameOverActivity : AppCompatActivity() {


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private var finalScore: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        finalScore = intent.getIntExtra("FINAL_SCORE", 0)
        val scoreLabel = findViewById<TextView>(R.id.gameOver_LBL_score)
        val restartBtn = findViewById<Button>(R.id.gameOver_BTN_restart)
        val menuBtn = findViewById<Button>(R.id.gameOver_BTN_menu)
        val viewScoresBtn = findViewById<Button>(R.id.gameOver_BTN_view_scores)

        viewScoresBtn.setOnClickListener {
            val intent = Intent(this, HighScoresActivity::class.java)
            startActivity(intent)
        }

        scoreLabel.text = "Your Score: $finalScore"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            saveScoreWithLocation(finalScore)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

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



    private fun saveScoreWithLocation(score: Int) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location: Location? ->
                    val lat = location?.latitude
                    val lon = location?.longitude
                    FirebaseUtils.saveHighScore(score, lat, lon)
                }.addOnFailureListener {
                    FirebaseUtils.saveHighScore(score, null, null)
                }
            } else {
                FirebaseUtils.saveHighScore(score, null, null)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            FirebaseUtils.saveHighScore(score, null, null)
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveScoreWithLocation(finalScore)
        } else {
            FirebaseUtils.saveHighScore(finalScore, null, null)
        }
    }
}
