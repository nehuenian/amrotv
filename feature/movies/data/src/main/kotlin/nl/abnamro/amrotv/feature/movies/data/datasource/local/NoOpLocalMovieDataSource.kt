package nl.abnamro.amrotv.feature.movies.data.datasource.local

import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.domain.api.model.Movie
import javax.inject.Inject

// TODO: Implement a real local data source using Room or DataStore for offline caching.
internal class NoOpLocalMovieDataSource @Inject constructor() : LocalMovieDataSource {
    override suspend fun saveMovies(movies: List<Movie>) = Unit
    override suspend fun getCachedMovies(): List<Movie> = emptyList()
    override suspend fun saveGenres(genres: List<Genre>) = Unit
    override suspend fun getCachedGenres(): List<Genre> = emptyList()
}
