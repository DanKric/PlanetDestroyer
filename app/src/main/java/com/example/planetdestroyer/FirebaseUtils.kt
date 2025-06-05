package com.example.planetdestroyer

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object FirebaseUtils {

    private val db = FirebaseFirestore.getInstance()

    // Save a score only if it's in the top 10
    fun saveHighScore(score: Int, latitude: Double?, longitude: Double?) {
        db.collection("highscores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                val scores = result.documents.mapNotNull { it.getLong("score")?.toInt() }
                val minScore = scores.minOrNull() ?: 0

                if (scores.size < 10 || score > minScore) {
                    val data = hashMapOf(
                        "score" to score,
                        "timestamp" to System.currentTimeMillis(),
                        "lat" to latitude,
                        "lon" to longitude
                    )

                    db.collection("highscores")
                        .add(data)
                        .addOnSuccessListener {
                            Log.d("FirebaseUtils", "✅ Score $score saved to Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseUtils", "❌ Failed to save score", e)
                        }
                } else {
                    Log.d("FirebaseUtils", "⚠️ Score $score not high enough for top 10")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUtils", "❌ Failed to retrieve top scores", e)
            }
    }

    // Get top scores
    fun getTopScores(limit: Long = 10, callback: (List<Pair<Int, Pair<Double?, Double?>>>) -> Unit) {
        db.collection("highscores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener { result ->
                val scores = result.mapNotNull { doc ->
                    val score = doc.getLong("score")?.toInt()
                    val lat = doc.getDouble("lat")
                    val lon = doc.getDouble("lon")
                    if (score != null) Pair(score, Pair(lat, lon)) else null
                }
                callback(scores)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUtils", "Error fetching scores", e)
                callback(emptyList())
            }
    }
}
