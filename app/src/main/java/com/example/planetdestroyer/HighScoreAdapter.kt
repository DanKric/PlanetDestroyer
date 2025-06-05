package com.example.planetdestroyer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng

class HighScoreAdapter(
    private val scores: List<Pair<Int, Pair<Double?, Double?>>>,
    private val onItemClick: (Int, LatLng?) -> Unit
) : RecyclerView.Adapter<HighScoreAdapter.ScoreViewHolder>() {

    class ScoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val scoreText: TextView = view.findViewById(R.id.item_score_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_high_score, parent, false)
        return ScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val (score, location) = scores[position]
        val (lat, lon) = location

        holder.scoreText.text = "Score: $score"
        holder.itemView.setOnClickListener {
            val latLng = if (lat != null && lon != null) LatLng(lat, lon) else null
            onItemClick(score, latLng)
        }
    }

    override fun getItemCount(): Int = scores.size
}
