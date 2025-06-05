package com.example.planetdestroyer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planetdestroyer.databinding.ActivityHighScoresBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.content.Intent


class HighScoresActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHighScoresBinding
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighScoresBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonReturnToMenu.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
            finish()
        }

        setupRecyclerView()
        setupMap()
    }

    private fun setupRecyclerView() {
        binding.highScoresRecyclerView.layoutManager = LinearLayoutManager(this)

        FirebaseUtils.getTopScores { scoreList ->
            val adapter = HighScoreAdapter(scoreList) { score, latLng ->
                if (latLng != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                }
            }
            binding.highScoresRecyclerView.adapter = adapter
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
            FirebaseUtils.getTopScores { scores ->
                for ((score, location) in scores) {
                    val (lat, lon) = location
                    if (lat != null && lon != null) {
                        val position = LatLng(lat, lon)
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title("Score: $score")
                        )
                    }
                }
            }
        }
    }
}
