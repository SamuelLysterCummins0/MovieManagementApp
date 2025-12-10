package org.wit.moviemanager.main

import android.app.Application
import org.wit.moviemanager.models.MovieJSONStore
import org.wit.moviemanager.models.MovieModel
import org.wit.moviemanager.models.MovieStore
import timber.log.Timber
import timber.log.Timber.i

class MainApp : Application() {

    lateinit var movies : ArrayList<MovieModel>
    lateinit var store: MovieStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        i("Movie Manager started")

        store = MovieJSONStore(applicationContext)
        movies = store.findAll()

        i("Loaded ${movies.size} movies")
    }
}