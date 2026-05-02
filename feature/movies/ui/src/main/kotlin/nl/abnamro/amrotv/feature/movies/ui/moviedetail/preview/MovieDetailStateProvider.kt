package nl.abnamro.amrotv.feature.movies.ui.moviedetail.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError
import nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail.MovieDetailState
import nl.abnamro.amrotv.feature.movies.ui.preview.UIPreviewData

internal class MovieDetailStateProvider : PreviewParameterProvider<MovieDetailState> {
    override val values = sequenceOf(
        MovieDetailState(isLoading = true),
        MovieDetailState(
            errors = persistentListOf(MovieError.MOVIE_DETAIL_LOAD_FAILED),
        ),
        MovieDetailState(movieDetail = UIPreviewData.MovieDetails.darkKnight),
        MovieDetailState(movieDetail = UIPreviewData.MovieDetails.interstellar),
        MovieDetailState(
            movieDetail = UIPreviewData.MovieDetails.darkKnight,
            errors = persistentListOf(MovieError.MOVIE_DETAIL_LOAD_FAILED),
        ),
        MovieDetailState(
            movieDetail = UIPreviewData.MovieDetails.darkKnight,
            errors = persistentListOf(
                MovieError.MOVIE_DETAIL_LOAD_FAILED,
                MovieError.GENRES_LOAD_FAILED,
            ),
        ),
    )
}
