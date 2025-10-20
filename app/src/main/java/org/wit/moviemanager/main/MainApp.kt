package org.wit.moviemanager.main

import android.app.Application
import org.wit.moviemanager.models.MovieModel
import timber.log.Timber
import timber.log.Timber.i

class MainApp : Application() {

    val movies = ArrayList<MovieModel>()
    var lastId = 0L

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        i("Movie Manager started")

    }
}