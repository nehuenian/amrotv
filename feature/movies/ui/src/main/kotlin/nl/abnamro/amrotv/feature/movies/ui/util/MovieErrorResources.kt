package nl.abnamro.amrotv.feature.movies.ui.util

import androidx.annotation.StringRes
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError
import nl.abnamro.amrotv.feature.movies.ui.R

@StringRes
internal fun MovieError.toStringResId(): Int =
    when (this) {
        MovieError.MOVIES_LOAD_FAILED -> R.string.error_movies_failed
        MovieError.GENRES_LOAD_FAILED -> R.string.error_genres_failed
        MovieError.MOVIE_DETAIL_LOAD_FAILED -> R.string.error_movie_detail_failed
    }
