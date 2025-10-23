package org.wit.moviemanager.models

import android.os.Parcelable
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
    var description: String = "",
    var isFavorite: Boolean = false,
    var isWatchlist: Boolean = false) : Parcelable
