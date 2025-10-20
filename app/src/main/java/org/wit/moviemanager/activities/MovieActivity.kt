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
    var edit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarAdd.title = title
        setSupportActionBar(binding.toolbarAdd)

        app = application as MainApp

        i("Movie Activity started...")

        if (intent.hasExtra("movie_edit")) {
            edit = true
            movie = intent.extras?.getParcelable("movie_edit")!!
            binding.movieTitle.setText(movie.title)
            binding.movieDirector.setText(movie.director)
            binding.movieGenre.setText(movie.genre)
            binding.movieYear.setText(movie.releaseYear)
            binding.movieRating.setText(movie.rating)
            binding.movieCinema.setText(movie.cinema)
            binding.movieDescription.setText(movie.description)
            binding.btnAdd.text = "Update Movie"
        }

        binding.btnAdd.setOnClickListener() {
            movie.title = binding.movieTitle.text.toString()
            movie.director = binding.movieDirector.text.toString()
            movie.genre = binding.movieGenre.text.toString()
            movie.releaseYear = binding.movieYear.text.toString()
            movie.rating = binding.movieRating.text.toString()
            movie.cinema = binding.movieCinema.text.toString()
            movie.description = binding.movieDescription.text.toString()

            if (movie.title.isNotEmpty()) {
                if (edit) {
                    val foundMovie = app.movies.find { m -> m.id == movie.id }
                    if (foundMovie != null) {
                        foundMovie.title = movie.title
                        foundMovie.director = movie.director
                        foundMovie.genre = movie.genre
                        foundMovie.releaseYear = movie.releaseYear
                        foundMovie.rating = movie.rating
                        foundMovie.cinema = movie.cinema
                        foundMovie.description = movie.description
                        i("Movie updated: $foundMovie")
                    }
                } else {
                    movie.id = app.lastId++
                    app.movies.add(movie.copy())
                    i("Movie created: $movie")
                }

                for (i in app.movies.indices) {
                    i("Movie[$i]:${app.movies[i]}")
                }
                setResult(RESULT_OK)
                finish()
            } else {
                Snackbar
                    .make(it, "Please Enter a movie title", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_movie, menu)
        if (edit) {
            menu.findItem(R.id.item_delete)?.isVisible = true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_delete -> {
                app.movies.remove(movie)
                i("Movie deleted: $movie")
                setResult(RESULT_OK)
                finish()
            }

            R.id.item_cancel -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}