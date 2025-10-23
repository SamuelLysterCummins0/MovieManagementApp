package org.wit.moviemanager.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.wit.moviemanager.databinding.ActivityStatisticsBinding
import org.wit.moviemanager.main.MainApp
import timber.log.Timber.i

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    lateinit var app: MainApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = "Statistics"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        app = application as MainApp

        i("Statistics Activity started...")

        calculateStatistics()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun calculateStatistics() {
        val movies = app.movies

        binding.totalMoviesValue.text = movies.size.toString()

        val favoriteCount = movies.count { it.isFavorite }
        binding.favoriteCountValue.text = favoriteCount.toString()

        val watchlistCount = movies.count { it.isWatchlist }
        binding.watchlistCountValue.text = watchlistCount.toString()

        if (movies.isNotEmpty()) {
            val ratings = movies.mapNotNull { it.rating.toIntOrNull() }
            if (ratings.isNotEmpty()) {
                val average = ratings.average()
                binding.averageRatingValue.text = String.format("%.1f", average)
            } else {
                binding.averageRatingValue.text = "N/A"
            }

            val genreCounts = movies
                .filter { it.genre.isNotEmpty() && it.genre != "Select Genre" }
                .groupingBy { it.genre }
                .eachCount()

            if (genreCounts.isNotEmpty()) {
                val mostCommonGenre = genreCounts.maxByOrNull { it.value }?.key
                binding.mostCommonGenreValue.text = mostCommonGenre ?: "N/A"
            } else {
                binding.mostCommonGenreValue.text = "N/A"
            }

            val cinemaCounts = movies
                .filter { it.cinema.isNotEmpty() }
                .groupingBy { it.cinema }
                .eachCount()

            if (cinemaCounts.isNotEmpty()) {
                val mostVisitedCinema = cinemaCounts.maxByOrNull { it.value }?.key
                binding.mostVisitedCinemaValue.text = mostVisitedCinema ?: "N/A"
            } else {
                binding.mostVisitedCinemaValue.text = "N/A"
            }
        } else {
            binding.averageRatingValue.text = "0"
            binding.mostCommonGenreValue.text = "N/A"
            binding.mostVisitedCinemaValue.text = "N/A"
        }

        i("Statistics calculated")
    }
}