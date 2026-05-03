package nl.abnamro.amrotv.feature.movies.nav

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import nl.abnamro.amrotv.feature.movies.presentation.implementation.moviedetail.MovieDetailViewModel
import nl.abnamro.amrotv.feature.movies.presentation.implementation.trendingmovies.TrendingMoviesViewModel
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.MovieDetailScreen
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.TrendingMoviesScreen

fun EntryProviderScope<NavKey>.moviesEntry(backStack: NavBackStack<NavKey>) {
    entry<MoviesNavKey.TrendingMovies> {
        TrendingMoviesScreen(
            onNavigateToMovieDetail = { movieId ->
                backStack.add(MoviesNavKey.MovieDetail(movieId))
            },
            viewModel = hiltViewModel<TrendingMoviesViewModel>(),
        )
    }
    entry<MoviesNavKey.MovieDetail> { key ->
        MovieDetailScreen(
            navigateBack = { backStack.removeLastOrNull() },
            viewModel =
                hiltViewModel<MovieDetailViewModel, MovieDetailViewModel.Factory>(
                    creationCallback = { factory -> factory.create(movieId = key.movieId) }
                ),
        )
    }
}
