package org.wit.moviemanager.adapters

import android.view.LayoutInflater
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
            binding.movieDirector.text = movie.director
            binding.movieGenre.text = movie.genre
            binding.movieYear.text = movie.releaseYear
            binding.movieRating.text = "Rating: ${movie.rating}/10"
            binding.movieCinema.text = "Cinema: ${movie.cinema}"
            binding.root.setOnClickListener { listener.onMovieClick(movie) }
        }
    }
}