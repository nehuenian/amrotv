package nl.abnamro.amrotv.feature.movies.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation keys for the Movies feature.
 *
 * Each subtype represents a distinct destination in the Movies navigation graph. These keys are
 * used with Navigation 3's androidx.navigation3.runtime.NavDisplay to identify back-stack entries
 * and supply destination-specific data to entry builders.
 */
@Serializable
sealed interface MoviesNavKey : NavKey {

    /** Destination for the trending movies list screen. */
    @Serializable data object TrendingMovies : MoviesNavKey

    /**
     * Destination for the movie detail screen.
     *
     * @param movieId the TMDB identifier of the movie to display.
     */
    @Serializable data class MovieDetail(val movieId: Int) : MoviesNavKey
}
