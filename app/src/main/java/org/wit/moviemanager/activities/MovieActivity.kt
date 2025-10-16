package org.wit.moviemanager.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import org.wit.moviemanager.R
import org.wit.moviemanager.databinding.ActivityMovieBinding
import org.wit.moviemanager.main.MainApp
import org.wit.moviemanager.models.MovieModel
import timber.log.Timber.i

class MovieActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieBinding
    var movie = MovieModel()
    lateinit var app: MainApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarAdd.title = title
        setSupportActionBar(binding.toolbarAdd)

        app = application as MainApp

        i("Movie Activity started...")

        binding.btnAdd.setOnClickListener() {
            movie.title = binding.movieTitle.text.toString()
            movie.director = binding.movieDirector.text.toString()
            movie.genre = binding.movieGenre.text.toString()
            movie.releaseYear = binding.movieYear.text.toString()
            movie.rating = binding.movieRating.text.toString()
            movie.cinema = binding.movieCinema.text.toString()
            movie.description = binding.movieDescription.text.toString()

            if (movie.title.isNotEmpty()) {
                app.movies.add(movie.copy())
                i("add Button Pressed: $movie")
                for (i in app.movies.indices) {
                    i("Movie[$i]:${this.app.movies[i]}")
                }
                setResult(RESULT_OK)
                finish()
            }
            else {
                Snackbar
                    .make(it,"Please Enter a movie title", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_movie, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_cancel -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}