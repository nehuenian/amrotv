package nl.abnamro.amrotv.feature.movies.presentation.api

/**
 * Represents a categorised error that can occur in the movies feature.
 *
 * The UI layer maps each entry to an Android string resource, keeping hardcoded
 * strings out of the domain and presentation layers and enabling proper localisation.
 */
enum class MovieError {
    /** Failed to load the list of trending movies from the remote source. */
    MOVIES_LOAD_FAILED,

    /** Failed to load the genre catalogue. Movies may still be shown without genre labels. */
    GENRES_LOAD_FAILED,

    /** Failed to load the detail for the requested movie. */
    MOVIE_DETAIL_LOAD_FAILED,
}
