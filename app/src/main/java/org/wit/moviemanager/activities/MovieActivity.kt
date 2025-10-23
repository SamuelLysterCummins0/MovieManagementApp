package org.wit.moviemanager.activities

import android.app.DatePickerDialog
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
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

        val genres = resources.getStringArray(R.array.genre_options)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genres)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.movieGenre.adapter = spinnerAdapter

        binding.movieRating.minValue = 1
        binding.movieRating.maxValue = 10
        binding.movieRating.value = 5
        binding.movieRating.wrapSelectorWheel = false

        binding.movieYear.setOnClickListener {
            val year = binding.movieYear.text.toString().toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)

            DatePickerDialog(this,  { _, selectedYear, _, _ ->
                binding.movieYear.setText(selectedYear.toString())
                i("Release year selected: $selectedYear")
            }, year, 0, 1).show()
        }

        binding.btnFavorite.setOnClickListener {
            movie.isFavorite = !movie.isFavorite
            updateFavoriteButton()
            if (movie.isFavorite && movie.isWatchlist) {
                movie.isWatchlist = false
                updateWatchlistButton()
                showWatchedFields()
            }
            i("Favorite toggled: ${movie.isFavorite}")
        }

        binding.btnWatchlist.setOnClickListener {
            movie.isWatchlist = !movie.isWatchlist
            updateWatchlistButton()
            if (movie.isWatchlist) {
                if (movie.isFavorite) {
                    movie.isFavorite = false
                    updateFavoriteButton()
                }
                hideWatchedFields()
            } else {
                showWatchedFields()
            }
            i("Watchlist toggled: ${movie.isWatchlist}")
        }

        if (intent.hasExtra("movie_edit")) {
            edit = true
            movie = intent.extras?.getParcelable("movie_edit")!!
            binding.movieTitle.setText(movie.title)
            binding.movieDirector.setText(movie.director)

            val genrePosition = genres.indexOf(movie.genre)
            if (genrePosition >= 0) {
                binding.movieGenre.setSelection(genrePosition)
            }

            val ratingValue = movie.rating.toIntOrNull() ?: 5
            binding.movieRating.value = ratingValue

            binding.movieYear.setText(movie.releaseYear)
            binding.movieCinema.setText(movie.cinema)
            binding.movieDescription.setText(movie.description)
            binding.btnAdd.text = "Update Movie"

            updateFavoriteButton()
            updateWatchlistButton()

            if (movie.isWatchlist) {
                hideWatchedFields()
            }
        }

        binding.btnAdd.setOnClickListener() {
            movie.title = binding.movieTitle.text.toString()
            movie.director = binding.movieDirector.text.toString()
            movie.genre = binding.movieGenre.selectedItem.toString()
            movie.releaseYear = binding.movieYear.text.toString()
            movie.description = binding.movieDescription.text.toString()

            if (!movie.isWatchlist) {
                movie.rating = binding.movieRating.value.toString()
                movie.cinema = binding.movieCinema.text.toString()
            } else {
                movie.rating = ""
                movie.cinema = ""
            }

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
                        foundMovie.isFavorite = movie.isFavorite
                        foundMovie.isWatchlist = movie.isWatchlist
                        i("Movie updated: $foundMovie")
                    }
                } else {
                    movie.id = app.lastId++
                    app.movies.add(movie.copy())
                    i("Movie created: $movie")
                }

                app.store.save(app.movies)

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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun updateFavoriteButton() {
        if (movie.isFavorite) {
            binding.btnFavorite.text = "⭐ Favorited"
            binding.btnFavorite.setBackgroundColor(resources.getColor(R.color.colorAccent, null))
        } else {
            binding.btnFavorite.text = "⭐ Favorite"
            binding.btnFavorite.setBackgroundColor(resources.getColor(R.color.colorBackground, null))
        }
    }

    private fun updateWatchlistButton() {
        if (movie.isWatchlist) {
            binding.btnWatchlist.text = "📋 On Watchlist"
            binding.btnWatchlist.setBackgroundColor(resources.getColor(R.color.colorAccent, null))
        } else {
            binding.btnWatchlist.text = "📋 Watchlist"
            binding.btnWatchlist.setBackgroundColor(resources.getColor(R.color.colorBackground, null))
        }
    }

    private fun hideWatchedFields() {
        binding.ratingSection.visibility = View.GONE
        binding.cinemaSection.visibility = View.GONE
    }

    private fun showWatchedFields() {
        binding.ratingSection.visibility = View.VISIBLE
        binding.cinemaSection.visibility = View.VISIBLE
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
                app.store.save(app.movies)
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