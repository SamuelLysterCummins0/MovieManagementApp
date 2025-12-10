package org.wit.moviemanager.models

interface MovieStore {
    fun findAll(): ArrayList<MovieModel>
    fun create(movie: MovieModel)
    fun update(movie: MovieModel)
    fun delete(movie: MovieModel)
}