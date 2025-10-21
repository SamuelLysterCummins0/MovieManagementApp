package org.wit.moviemanager.main

import android.app.Application
import org.wit.moviemanager.models.MovieJSONStore
import org.wit.moviemanager.models.MovieModel
import timber.log.Timber
import timber.log.Timber.i

class MainApp : Application() {

    lateinit var movies : ArrayList<MovieModel>
    var lastId = 0L
    lateinit var store: MovieJSONStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        i("Movie Manager started")

        store = MovieJSONStore(applicationContext)
        movies = store.load()

        if (movies.isNotEmpty()) {
            lastId = movies.last().id + 1
        }

        i("Loaded ${movies.size} movies")
    }
}