package org.wit.moviemanager.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import org.wit.moviemanager.R
import org.wit.moviemanager.databinding.ActivityMovieBinding
import org.wit.moviemanager.main.MainApp
import org.wit.moviemanager.models.Location
import org.wit.moviemanager.models.MovieModel
import timber.log.Timber.i

class MovieActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieBinding
    private lateinit var mapIntentLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageIntentLauncher: ActivityResultLauncher<PickVisualMediaRequest>
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
            binding.movieCinemaAddress.setText(movie.cinemaAddress)
            binding.btnAdd.text = "Update Movie"

            Picasso.get()
                .load(movie.image)
                .into(binding.movieImage)
            if (movie.image != Uri.EMPTY) {
                binding.chooseImage.setText("Change Movie Poster")
            }

            updateFavoriteButton()
            updateWatchlistButton()

            if (movie.isWatchlist) {
                hideWatchedFields()
            }
        }

        registerImagePickerCallback()
        registerMapCallback()

        binding.chooseImage.setOnClickListener {
            val request = PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                .build()
            imageIntentLauncher.launch(request)
        }

        binding.movieLocation.setOnClickListener {
            val location = Location(52.245696, -7.139102, 15f)
            if (movie.zoom != 0f) {
                location.lat = movie.lat
                location.lng = movie.lng
                location.zoom = movie.zoom
            }
            val launcherIntent = Intent(this, MapActivity::class.java)
                .putExtra("location", location)
                .putExtra("cinema", movie.cinema)
                .putExtra("cinemaAddress", movie.cinemaAddress)
            mapIntentLauncher.launch(launcherIntent)
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
                movie.cinemaAddress = binding.movieCinemaAddress.text.toString()
            } else {
                movie.rating = ""
                movie.cinema = ""
                movie.cinemaAddress = ""
            }

            if (movie.title.isNotEmpty()) {
                if (edit) {
                    app.store.update(movie)
                    i("Movie updated: $movie")
                } else {
                    app.store.create(movie.copy())
                    i("Movie created: $movie")
                }

                app.movies = app.store.findAll()

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
                app.store.delete(movie)
                app.movies = app.store.findAll()
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

    private fun registerMapCallback() {
        mapIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when (result.resultCode) {
                    RESULT_OK -> {
                        if (result.data != null) {
                            i("Got Location ${result.data.toString()}")
                            val location = result.data!!.extras?.getParcelable<Location>("location")!!
                            val cinema = result.data!!.extras?.getString("cinema") ?: ""
                            val cinemaAddress = result.data!!.extras?.getString("cinemaAddress") ?: ""

                            i("Location == $location")
                            i("Cinema == $cinema")
                            i("Address == $cinemaAddress")

                            movie.lat = location.lat
                            movie.lng = location.lng
                            movie.zoom = location.zoom

                            if (cinema.isNotEmpty()) {
                                binding.movieCinema.setText(cinema)
                                movie.cinema = cinema
                            }

                            if (cinemaAddress.isNotEmpty()) {
                                binding.movieCinemaAddress.setText(cinemaAddress)
                                movie.cinemaAddress = cinemaAddress
                            }
                        }
                    }
                    RESULT_CANCELED -> { }
                    else -> { }
                }
            }
    }

    private fun registerImagePickerCallback() {
        imageIntentLauncher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) {
            try {
                contentResolver.takePersistableUriPermission(
                    it!!,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                movie.image = it
                i("IMG :: ${movie.image}")
                Picasso.get()
                    .load(movie.image)
                    .into(binding.movieImage)
                binding.chooseImage.setText("Change Movie Poster")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}