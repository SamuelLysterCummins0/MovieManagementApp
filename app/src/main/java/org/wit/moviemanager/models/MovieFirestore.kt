package org.wit.moviemanager.models

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import timber.log.Timber.i
import java.util.Random

class MovieFirestore(private val context: Context) : MovieStore {

    private val db: FirebaseFirestore = Firebase.firestore
    private var movies = ArrayList<MovieModel>()

    init {
        db.firestoreSettings = firestoreSettings {
            isPersistenceEnabled = true
        }
        loadMovies()
    }

    private fun getUserCollection(): CollectionReference {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
        return db.collection("users").document(userId).collection("movies")
    }

    private fun loadMovies() {
        getUserCollection().get()
            .addOnSuccessListener { documents ->
                movies.clear()
                for (document in documents) {
                    val movie = document.toObject(MovieModel::class.java)
                    movie.id = document.id.toLongOrNull() ?: 0
                    movie.image = Uri.parse(movie.imageUrl)
                    movies.add(movie)
                }
                i("Movies loaded from Firestore: ${movies.size} movies")
            }
            .addOnFailureListener { exception ->
                Timber.e("Error loading movies: $exception")
            }
    }

    override fun findAll(): ArrayList<MovieModel> {
        return movies
    }

    fun reloadMovies() {
        loadMovies()
    }

    override fun create(movie: MovieModel) {
        movie.id = generateRandomId()
        movie.imageUrl = movie.image.toString()
        movies.add(movie)

        getUserCollection().document(movie.id.toString())
            .set(movie)
            .addOnSuccessListener {
                i("Movie synced to Firestore: ${movie.title}")
            }
            .addOnFailureListener { e ->
                Timber.e("Error syncing movie: $e")
            }
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
            foundMovie.cinemaAddress = movie.cinemaAddress
            foundMovie.description = movie.description
            foundMovie.isFavorite = movie.isFavorite
            foundMovie.isWatchlist = movie.isWatchlist
            foundMovie.image = movie.image
            foundMovie.lat = movie.lat
            foundMovie.lng = movie.lng
            foundMovie.zoom = movie.zoom
        }

        movie.imageUrl = movie.image.toString()

        getUserCollection().document(movie.id.toString())
            .set(movie)
            .addOnSuccessListener {
                i("Movie updated in Firestore: ${movie.title}")
            }
            .addOnFailureListener { e ->
                Timber.e("Error updating movie: $e")
            }
    }

    override fun delete(movie: MovieModel) {
        movies.remove(movie)

        getUserCollection().document(movie.id.toString())
            .delete()
            .addOnSuccessListener {
                i("Movie deleted from Firestore: ${movie.title}")
            }
            .addOnFailureListener { e ->
                Timber.e("Error deleting movie: $e")
            }
    }

    private fun generateRandomId(): Long {
        return Random().nextLong()
    }
}