package nl.abnamro.amrotv.feature.movies.ui.trendingmovies

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import nl.abnamro.amrotv.core.mvi.AmroTvViewModel
import nl.abnamro.amrotv.core.ui.component.AmroTvEmptyView
import nl.abnamro.amrotv.core.ui.component.AmroTvErrorView
import nl.abnamro.amrotv.core.ui.component.AmroTvLoadingView
import nl.abnamro.amrotv.core.ui.preview.LightDarkPreview
import nl.abnamro.amrotv.core.ui.theme.AmroTvDimensions
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError
import nl.abnamro.amrotv.feature.movies.presentation.api.model.GenrePresentationModel
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MoviePresentationModel
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesEffect
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesIntent
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesState
import nl.abnamro.amrotv.feature.movies.ui.R
import nl.abnamro.amrotv.feature.movies.ui.theme.MoviesDimensions
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.FeaturedMovieBanner
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.GenreFilterRow
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.InlineErrorBanner
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.MovieCard
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.TrendingMoviesTopBar
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.preview.TrendingMoviesStateProvider

@Composable
fun TrendingMoviesScreen(
    onNavigateToMovieDetail: (movieId: Int) -> Unit,
    viewModel: AmroTvViewModel<TrendingMoviesState, TrendingMoviesIntent, TrendingMoviesEffect>,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnNavigateToMovieDetail by rememberUpdatedState(onNavigateToMovieDetail)

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TrendingMoviesEffect.NavigateToMovieDetail ->
                    currentOnNavigateToMovieDetail(effect.movieId)
            }
        }
    }

    TrendingMoviesContent(
        state = state,
        onIntent = viewModel::handleIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingMoviesContent(
    state: TrendingMoviesState,
    onIntent: (TrendingMoviesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TrendingMoviesTopBar(
                weekRangeLabel = state.weekRangeLabel,
                selectedSortOption = state.selectedSortOption,
                selectedSortOrder = state.selectedSortOrder,
                showSortSheet = state.showSortSheet,
                onIntent = onIntent,
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading && state.movies.isEmpty() ->
                AmroTvLoadingView(modifier = Modifier.fillMaxSize().padding(innerPadding))

            state.errors.isNotEmpty() && state.movies.isEmpty() ->
                AmroTvErrorView(
                    message = stringResource(R.string.error_movies_failed),
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    onRetry = { onIntent(TrendingMoviesIntent.LoadMovies) },
                )

            state.movies.isEmpty() ->
                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    GenreFilterRow(
                        genres = state.genres,
                        selectedGenreId = state.selectedGenreId,
                        onGenreSelected = { onIntent(TrendingMoviesIntent.FilterByGenre(it)) },
                    )
                    AmroTvEmptyView(
                        title = stringResource(R.string.empty_no_movies_found),
                        subtitle = stringResource(R.string.empty_no_movies_found_subtitle),
                        modifier = Modifier.weight(1f),
                    )
                }

            else ->
                TrendingMoviesMovieList(
                    movies = state.movies,
                    genres = state.genres,
                    selectedGenreId = state.selectedGenreId,
                    errors = state.errors,
                    onIntent = onIntent,
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                )
        }
    }
}

@Composable
private fun TrendingMoviesMovieList(
    movies: ImmutableList<MoviePresentationModel>,
    genres: ImmutableList<GenrePresentationModel>,
    selectedGenreId: Int?,
    errors: ImmutableList<MovieError>,
    onIntent: (TrendingMoviesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val featuredMovie = movies.firstOrNull()
    val gridState = rememberLazyGridState()
    val genreFilterScrollState: ScrollState = rememberScrollState()
    // Tracks whether the scrollable GenreFilterRow item is currently visible in the grid.
    // When it scrolls out of view, the sticky GenreFilterRow overlay (below) takes over
    // so the genre filter remains accessible without scrolling back up.
    val isGenreFilterVisible by remember(gridState) {
        derivedStateOf {
            gridState.layoutInfo.visibleItemsInfo.any { it.key == "genre_filter" }
        }
    }
    val bleedModifier = remember {
        Modifier.layout { measurable, constraints ->
            val bleedPx = AmroTvDimensions.spacingMedium.roundToPx()
            val expandedWidth = constraints.maxWidth + bleedPx * 2
            val placeable = measurable.measure(
                constraints.copy(minWidth = expandedWidth, maxWidth = expandedWidth),
            )
            layout(constraints.maxWidth, placeable.height) {
                placeable.place(-bleedPx, 0)
            }
        }
    }
    Box(modifier = modifier) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize = MoviesDimensions.movieCardMinWidth),
            contentPadding = PaddingValues(
                start = AmroTvDimensions.spacingMedium,
                end = AmroTvDimensions.spacingMedium,
                bottom = AmroTvDimensions.spacingMedium,
            ),
            horizontalArrangement = Arrangement.spacedBy(AmroTvDimensions.spacingSmall),
            verticalArrangement = Arrangement.spacedBy(AmroTvDimensions.spacingSmall),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (featuredMovie != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FeaturedMovieBanner(
                        movie = featuredMovie,
                        onMovieClick = { id -> onIntent(TrendingMoviesIntent.OpenMovieDetail(id)) },
                        modifier = bleedModifier.graphicsLayer {
                            val bannerInfo = gridState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == 0 }
                            alpha = if (bannerInfo == null) {
                                0f
                            } else {
                                val visibleHeight = (bannerInfo.size.height + bannerInfo.offset.y).coerceAtLeast(0)
                                (visibleHeight.toFloat() / bannerInfo.size.height).coerceIn(0f, 1f)
                            }
                        },
                    )
                }
            }
            // Primary (scrollable) GenreFilterRow — hidden by the sticky overlay once it scrolls out of view.
            item(key = "genre_filter", span = { GridItemSpan(maxLineSpan) }) {
                GenreFilterRow(
                    genres = genres,
                    selectedGenreId = selectedGenreId,
                    onGenreSelected = { id -> onIntent(TrendingMoviesIntent.FilterByGenre(id)) },
                    scrollState = genreFilterScrollState,
                    modifier = bleedModifier.padding(vertical = AmroTvDimensions.spacingSmall),
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                InlineErrorBanner(
                    errors = errors,
                    onRetry = { onIntent(TrendingMoviesIntent.LoadMovies) },
                )
            }
            items(movies.drop(1), key = { it.id }) { movie ->
                MovieCard(movie = movie, onMovieClick = { id -> onIntent(TrendingMoviesIntent.OpenMovieDetail(id)) })
            }
        }
        // Sticky GenreFilterRow — shown only when the scrollable one has scrolled out of view,
        // keeping genre filtering accessible at all scroll positions.
        if (!isGenreFilterVisible) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = AmroTvDimensions.elevationSmall,
                modifier = Modifier.fillMaxWidth(),
            ) {
                GenreFilterRow(
                    genres = genres,
                    selectedGenreId = selectedGenreId,
                    onGenreSelected = { id -> onIntent(TrendingMoviesIntent.FilterByGenre(id)) },
                    scrollState = genreFilterScrollState,
                    modifier = Modifier.padding(vertical = AmroTvDimensions.spacingSmall),
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun TrendingMoviesContentPreview(
    @PreviewParameter(TrendingMoviesStateProvider::class) state: TrendingMoviesState,
) {
    AmroTvTheme {
        TrendingMoviesContent(
            state = state,
            onIntent = {},
        )
    }
}

