package nl.abnamro.amrotv.feature.movies.ui.trendingmovies.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesState
import nl.abnamro.amrotv.feature.movies.ui.preview.UIPreviewData

internal class TrendingMoviesStateProvider : PreviewParameterProvider<TrendingMoviesState> {
    override val values = sequenceOf(
        TrendingMoviesState(isLoading = true),
        TrendingMoviesState(
            errors = persistentListOf(MovieError.MOVIES_LOAD_FAILED),
        ),
        TrendingMoviesState(
            movies = UIPreviewData.Movies.all,
            genres = UIPreviewData.Genres.all,
            selectedSortOption = SortOption.POPULARITY,
            selectedSortOrder = SortOrder.DESC,
        ),
        TrendingMoviesState(
            movies = UIPreviewData.Movies.all,
            genres = persistentListOf(),
            errors = persistentListOf(MovieError.GENRES_LOAD_FAILED),
            selectedSortOption = SortOption.POPULARITY,
            selectedSortOrder = SortOrder.DESC,
        ),
        TrendingMoviesState(
            movies = UIPreviewData.Movies.all,
            genres = persistentListOf(),
            errors = persistentListOf(MovieError.MOVIES_LOAD_FAILED, MovieError.GENRES_LOAD_FAILED),
            selectedSortOption = SortOption.POPULARITY,
            selectedSortOrder = SortOrder.DESC,
        ),
        TrendingMoviesState(
            movies = persistentListOf(),
            genres = UIPreviewData.Genres.all,
            selectedGenreId = UIPreviewData.Genres.action.id,
            errors = persistentListOf(),
            selectedSortOption = SortOption.POPULARITY,
            selectedSortOrder = SortOrder.DESC,
        ),
    )
}
