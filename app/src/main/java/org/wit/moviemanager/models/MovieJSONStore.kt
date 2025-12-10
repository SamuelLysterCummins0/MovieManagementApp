package org.wit.moviemanager.models

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.*
import java.lang.reflect.Type
import java.util.*

class MovieJSONStore(private val context: Context) : MovieStore {

    private val JSON_FILE = "movies.json"
    private val gsonBuilder: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Uri::class.java, UriParser())
        .create()
    private var movies = ArrayList<MovieModel>()

    init {
        if (exists(context, JSON_FILE)) {
            deserialize()
        }
    }

    override fun findAll(): ArrayList<MovieModel> {
        return movies
    }

    override fun create(movie: MovieModel) {
        movie.id = generateRandomId()
        movies.add(movie)
        serialize()
    }

    override fun update(movie: MovieModel) {
        val foundMovie = movies.find { it.id == movie.id }
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
            serialize()
        }
    }

    override fun delete(movie: MovieModel) {
        movies.remove(movie)
        serialize()
    }

    private fun serialize() {
        val file = File(context.filesDir, JSON_FILE)
        val fileWriter = FileWriter(file)
        val json = gsonBuilder.toJson(movies)
        fileWriter.write(json)
        fileWriter.close()
        Timber.i("Movies saved to JSON: ${movies.size} movies")
    }

    private fun deserialize() {
        val file = File(context.filesDir, JSON_FILE)
        val fileReader = FileReader(file)
        val listType = object : TypeToken<ArrayList<MovieModel>>() {}.type
        movies = gsonBuilder.fromJson(fileReader, listType)
        fileReader.close()
        Timber.i("Movies loaded from JSON: ${movies.size} movies")
    }

    private fun exists(context: Context, filename: String): Boolean {
        val file = context.getFileStreamPath(filename)
        return file.exists()
    }

    private fun generateRandomId(): Long {
        return Random().nextLong()
    }

    class UriParser : JsonDeserializer<Uri>, JsonSerializer<Uri> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Uri {
            return Uri.parse(json?.asString)
        }

        override fun serialize(
            src: Uri?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonPrimitive(src.toString())
        }
    }
}