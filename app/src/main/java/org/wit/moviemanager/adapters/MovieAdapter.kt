package org.wit.moviemanager.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import org.wit.moviemanager.R
import org.wit.moviemanager.databinding.CardMovieBinding
import org.wit.moviemanager.models.MovieModel

interface MovieListener {
    fun onMovieClick(movie: MovieModel)
}

class MovieAdapter(
    private var movies: List<MovieModel>,
    private val listener: MovieListener,
    private var isEditMode: Boolean = false
) : RecyclerView.Adapter<MovieAdapter.MainHolder>() {

    private var expandedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val movie = movies[holder.adapterPosition]
        val isExpanded = holder.adapterPosition == expandedPosition
        holder.bind(movie, listener, isEditMode, isExpanded) { pos ->
            val previousExpandedPosition = expandedPosition
            expandedPosition = if (expandedPosition == pos) -1 else pos

            if (previousExpandedPosition != -1) {
                notifyItemChanged(previousExpandedPosition)
            }
            if (expandedPosition != -1) {
                notifyItemChanged(expandedPosition)
            }
        }
    }

    override fun getItemCount(): Int = movies.size

    override fun onViewRecycled(holder: MainHolder) {
        super.onViewRecycled(holder)
        holder.onRecycled()
    }

    fun updateEditMode(editMode: Boolean) {
        isEditMode = editMode
        expandedPosition = -1
        notifyDataSetChanged()
    }

    class MainHolder(private val binding: CardMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var googleMap: GoogleMap? = null
        private var mapInitialized = false

        fun bind(
            movie: MovieModel,
            listener: MovieListener,
            isEditMode: Boolean,
            isExpanded: Boolean,
            onExpandClick: (Int) -> Unit
        ) {
            binding.movieTitle.text = movie.title

            if (isEditMode) {
                binding.root.setCardBackgroundColor(
                    binding.root.context.resources.getColor(R.color.colorEditMode, null)
                )
            } else {
                binding.root.setCardBackgroundColor(
                    binding.root.context.resources.getColor(R.color.colorBackground, null)
                )
            }

            Picasso.get()
                .load(movie.image)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(binding.movieImage)

            if (movie.isFavorite) {
                binding.movieFavoriteIcon.visibility = View.VISIBLE
                binding.movieWatchlistIcon.visibility = View.GONE
            } else if (movie.isWatchlist) {
                binding.movieFavoriteIcon.visibility = View.GONE
                binding.movieWatchlistIcon.visibility = View.VISIBLE
            } else {
                binding.movieFavoriteIcon.visibility = View.GONE
                binding.movieWatchlistIcon.visibility = View.GONE
            }

            if (movie.director.isNotEmpty()) {
                binding.movieDirector.text = movie.director
                binding.movieDirector.visibility = View.VISIBLE
            } else {
                binding.movieDirector.visibility = View.GONE
            }

            if (movie.genre.isNotEmpty() && movie.genre != "Select Genre") {
                binding.movieGenre.text = movie.genre
                binding.movieGenre.visibility = View.VISIBLE
            } else {
                binding.movieGenre.visibility = View.GONE
            }

            if (movie.releaseYear.isNotEmpty()) {
                binding.movieYear.text = movie.releaseYear
                binding.movieYear.visibility = View.VISIBLE
            } else {
                binding.movieYear.visibility = View.GONE
            }

            if (movie.rating.isNotEmpty() && !movie.isWatchlist) {
                binding.movieRating.text = "⭐ ${movie.rating}/10"
                binding.movieRating.visibility = View.VISIBLE
            } else {
                binding.movieRating.visibility = View.GONE
            }

            if (movie.cinema.isNotEmpty()) {
                binding.movieCinema.text = "📍 ${movie.cinema}"
                binding.movieCinema.visibility = View.VISIBLE
            } else {
                binding.movieCinema.visibility = View.GONE
            }

            if (movie.description.isNotEmpty()) {
                binding.movieDescription.text = movie.description
                binding.movieDescription.visibility = View.VISIBLE
            } else {
                binding.movieDescription.visibility = View.GONE
            }

            if (movie.cinemaAddress.isNotEmpty()) {
                binding.movieCinemaAddress.text = movie.cinemaAddress
                binding.addressSection.visibility = View.VISIBLE
            } else {
                binding.addressSection.visibility = View.GONE
            }

            if (isExpanded) {
                binding.expandableContent.visibility = View.VISIBLE
                binding.expandIndicator.text = "▲ Tap to collapse"

                // Setup map only when expanded
                if (movie.lat != 0.0 && movie.lng != 0.0) {
                    binding.mapSection.visibility = View.VISIBLE
                    setupMap(movie)
                } else {
                    binding.mapSection.visibility = View.GONE
                }
            } else {
                binding.expandableContent.visibility = View.GONE
                binding.expandIndicator.text = "▼ Tap for more details"

                cleanupMap()
            }

            if (isEditMode) {
                binding.root.setOnClickListener {
                    listener.onMovieClick(movie)
                }
            } else {
                binding.mainContent.setOnClickListener {
                    onExpandClick(adapterPosition)
                }
            }

            if (isEditMode) {
                binding.root.setOnClickListener {
                    listener.onMovieClick(movie)
                }
                binding.mainContent.setOnClickListener {
                    listener.onMovieClick(movie)
                }
            } else {
                binding.root.setOnClickListener(null)
                binding.mainContent.setOnClickListener {
                    onExpandClick(adapterPosition)
                }
            }
        }

        private fun setupMap(movie: MovieModel) {
            if (!mapInitialized) {
                try {
                    MapsInitializer.initialize(binding.root.context)
                    binding.movieMapView.onCreate(null)
                    binding.movieMapView.onResume()
                    mapInitialized = true

                    binding.movieMapView.getMapAsync { map ->
                        googleMap = map
                        map.uiSettings.setAllGesturesEnabled(false)

                        val location = LatLng(movie.lat, movie.lng)
                        map.addMarker(
                            MarkerOptions()
                                .position(location)
                                .title(movie.cinema)
                        )
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.mapSection.visibility = View.GONE
                }
            }
        }

        private fun cleanupMap() {
            if (mapInitialized) {
                try {
                    googleMap?.clear()
                    googleMap = null
                    binding.movieMapView.onPause()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun onRecycled() {
            if (mapInitialized) {
                try {
                    googleMap?.clear()
                    googleMap = null
                    binding.movieMapView.onPause()
                    binding.movieMapView.onDestroy()
                    mapInitialized = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}