package org.wit.moviemanager.main

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import org.wit.moviemanager.models.MovieFirestore
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

        store = MovieFirestore(applicationContext)
        Thread.sleep(1000)
        movies = store.findAll()

        i("Loaded ${movies.size} movies")
    }
}