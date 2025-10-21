package org.wit.moviemanager.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.wit.moviemanager.databinding.CardMovieBinding
import org.wit.moviemanager.models.MovieModel

interface MovieListener {
    fun onMovieClick(movie: MovieModel)
}
class MovieAdapter(private var movies: List<MovieModel>, private val listener: MovieListener) :
    RecyclerView.Adapter<MovieAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardMovieBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val movie = movies[holder.adapterPosition]
        holder.bind(movie, listener)
    }

    override fun getItemCount(): Int = movies.size

    class MainHolder(private val binding : CardMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: MovieModel, listener: MovieListener) {
            binding.movieTitle.text = movie.title

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

            binding.movieRating.text = "Rating: ${movie.rating}/10"
            binding.movieRating.visibility = View.VISIBLE

            if (movie.cinema.isNotEmpty()) {
                binding.movieCinema.text = "Cinema: ${movie.cinema}"
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

            binding.root.setOnClickListener { listener.onMovieClick(movie) }
        }
    }
}