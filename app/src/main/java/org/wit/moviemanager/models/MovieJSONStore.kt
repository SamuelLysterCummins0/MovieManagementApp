package org.wit.moviemanager.models

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import timber.log.Timber.i
import java.io.*

class MovieJSONStore(private val context: Context) {

    private val JSON_FILE = "movies.json"
    private val gsonBuilder: Gson = GsonBuilder().setPrettyPrinting().create()

    fun load(): ArrayList<MovieModel> {
            val file = File(context.filesDir, JSON_FILE)
            if (file.exists()) {
                val fileReader = FileReader(file)
                val listType = object : TypeToken<ArrayList<MovieModel>>() {}.type
                val movies: ArrayList<MovieModel> = gsonBuilder.fromJson(fileReader, listType)
                fileReader.close()
                i("Movies loaded from JSON: ${movies.size} movies")
                return movies
            }
        return ArrayList()
    }

    fun save(movies: ArrayList<MovieModel>) {
            val file = File(context.filesDir, JSON_FILE)
            val fileWriter = FileWriter(file)
            val json = gsonBuilder.toJson(movies)
            fileWriter.write(json)
            fileWriter.close()
            i("Movies saved to JSON: ${movies.size} movies")
    }
}
