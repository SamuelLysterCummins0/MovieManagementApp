package org.wit.moviemanager.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.wit.moviemanager.R
import org.wit.moviemanager.adapters.MovieAdapter
import org.wit.moviemanager.adapters.MovieListener
import org.wit.moviemanager.databinding.ActivityMovieListBinding
import org.wit.moviemanager.main.MainApp
import org.wit.moviemanager.models.MovieModel
import timber.log.Timber.i

class MovieListActivity : AppCompatActivity(), MovieListener {

    lateinit var app: MainApp
    private lateinit var binding: ActivityMovieListBinding

    private var filteredMovies = ArrayList<MovieModel>()
    private var currentFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)

        app = application as MainApp

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager

        applyFilter(currentFilter)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.item_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchMovies(newText)
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add -> {
                val launcherIntent = Intent(this, MovieActivity::class.java)
                getResult.launch(launcherIntent)
            }
            R.id.filter_all -> {
                currentFilter = "all"
                applyFilter(currentFilter)
                i("Filter changed to: All Movies")
            }
            R.id.filter_favorites -> {
                currentFilter = "favorites"
                applyFilter(currentFilter)
                i("Filter changed to: Favorites")
            }
            R.id.filter_watchlist -> {
                currentFilter = "watchlist"
                applyFilter(currentFilter)
                i("Filter changed to: Watchlist")
            }
            R.id.item_statistics -> {
                val launcherIntent = Intent(this, StatisticsActivity::class.java)
                startActivity(launcherIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMovieClick(movie: MovieModel) {
        val launcherIntent = Intent(this, MovieActivity::class.java)
        launcherIntent.putExtra("movie_edit", movie)
        getResult.launch(launcherIntent)
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                applyFilter(currentFilter)
                i("Movie list updated")
            }
        }

    private fun applyFilter(filter: String) {
        filteredMovies.clear()

        when (filter) {
            "all" -> {
                filteredMovies.addAll(app.movies)
                binding.toolbar.title = "All Movies"
            }
            "favorites" -> {
                filteredMovies.addAll(app.movies.filter { it.isFavorite })
                binding.toolbar.title = "Favorites"
            }
            "watchlist" -> {
                filteredMovies.addAll(app.movies.filter { it.isWatchlist })
                binding.toolbar.title = "Watchlist"
            }
        }

        binding.recyclerView.adapter = MovieAdapter(filteredMovies, this)
        i("Showing $filter: ${filteredMovies.size} movies")
    }

    private fun searchMovies(query: String?) {
        val baseList = when (currentFilter) {
            "favorites" -> app.movies.filter { it.isFavorite }
            "watchlist" -> app.movies.filter { it.isWatchlist }
            else -> app.movies
        }

        filteredMovies.clear()

        if (query.isNullOrEmpty()) {
            filteredMovies.addAll(baseList)
            i("Showing all in current filter: ${filteredMovies.size}")
        } else {
            val searchQuery = query.lowercase()
            val results = baseList.filter { movie ->
                movie.title.lowercase().contains(searchQuery) ||
                        movie.director.lowercase().contains(searchQuery) ||
                        movie.genre.lowercase().contains(searchQuery)
            }
            filteredMovies.addAll(results)
            i("Search results for '$query': ${filteredMovies.size} movies found")
        }

        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        applyFilter(currentFilter)
    }
}