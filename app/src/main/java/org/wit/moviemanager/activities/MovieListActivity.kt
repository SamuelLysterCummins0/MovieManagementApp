package org.wit.moviemanager.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import org.wit.moviemanager.R
import org.wit.moviemanager.adapters.MovieAdapter
import org.wit.moviemanager.adapters.MovieListener
import org.wit.moviemanager.databinding.ActivityMovieListBinding
import org.wit.moviemanager.main.MainApp
import org.wit.moviemanager.models.MovieModel
import timber.log.Timber.i

class MovieListActivity : AppCompatActivity(), MovieListener, NavigationView.OnNavigationItemSelectedListener {

    lateinit var app: MainApp
    private lateinit var binding: ActivityMovieListBinding

    private var filteredMovies = ArrayList<MovieModel>()
    private var currentFilter = "all"
    private var isEditMode = false
    private lateinit var adapter: MovieAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)

        app = application as MainApp

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })

        applyFilter(currentFilter)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_all_movies -> {
                currentFilter = "all"
                applyFilter(currentFilter)
            }
            R.id.nav_favorites -> {
                currentFilter = "favorites"
                applyFilter(currentFilter)
            }
            R.id.nav_watchlist -> {
                currentFilter = "watchlist"
                applyFilter(currentFilter)
            }
            R.id.nav_statistics -> {
                val intent = Intent(this, StatisticsActivity::class.java)
                startActivity(intent)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
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
            R.id.item_edit_mode -> {
                isEditMode = !isEditMode
                adapter.updateEditMode(isEditMode)

                if (isEditMode) {
                    Snackbar.make(binding.root, "Edit Mode: Tap movies to edit", Snackbar.LENGTH_SHORT).show()
                    item.setIcon(android.R.drawable.ic_menu_close_clear_cancel)
                } else {
                    Snackbar.make(binding.root, "Edit Mode: Off", Snackbar.LENGTH_SHORT).show()
                    item.setIcon(android.R.drawable.ic_menu_edit)
                }
                i("Edit mode toggled: $isEditMode")
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

        adapter = MovieAdapter(filteredMovies, this, isEditMode)
        binding.recyclerView.adapter = adapter
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