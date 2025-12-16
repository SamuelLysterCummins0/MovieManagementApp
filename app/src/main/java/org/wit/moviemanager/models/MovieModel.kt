package org.wit.moviemanager.models

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize
@Parcelize
data class MovieModel(
    var id: Long = 0,
    var title: String = "",
    var director: String = "",
    var genre: String = "",
    var releaseYear: String = "",
    var rating: String = "",
    var cinema: String = "",
    var cinemaAddress: String = "",
    var description: String = "",
    var isFavorite: Boolean = false,
    var isWatchlist: Boolean = false,
    @get:Exclude var image: Uri = Uri.EMPTY,
    var imageUrl: String = "",
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var zoom: Float = 0f) : Parcelable

@Parcelize
data class Location(
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var zoom: Float = 0f) : Parcelable
