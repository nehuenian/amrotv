package nl.abnamro.amrotv.feature.movies.ui.moviedetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import nl.abnamro.amrotv.core.mvi.AmroTvViewModel
import nl.abnamro.amrotv.core.ui.component.AmroTvErrorView
import nl.abnamro.amrotv.core.ui.component.AmroTvLoadingView
import nl.abnamro.amrotv.core.ui.preview.PreviewLightDark
import nl.abnamro.amrotv.core.ui.theme.AmroTvDimensions
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MovieDetailPresentationModel
import nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail.MovieDetailEffect
import nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail.MovieDetailIntent
import nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail.MovieDetailState
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.component.MovieDetailFinancials
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.component.MovieDetailGenres
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.component.MovieDetailHero
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.component.MovieDetailImdbButton
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.component.MovieDetailOverview
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.component.MovieDetailStats
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.component.MovieDetailTagline
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.component.MovieDetailTopBar
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.preview.MovieDetailStateProvider
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.InlineErrorBanner
import nl.abnamro.amrotv.feature.movies.ui.util.toStringResId

@Composable
fun MovieDetailScreen(
    navigateBack: () -> Unit,
    viewModel: AmroTvViewModel<MovieDetailState, MovieDetailIntent, MovieDetailEffect>,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentUriHandler by rememberUpdatedState(LocalUriHandler.current)
    val currentNavigateBack by rememberUpdatedState(navigateBack)

    BackHandler { viewModel.handleIntent(MovieDetailIntent.NavigateBack) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MovieDetailEffect.NavigateBack -> currentNavigateBack()
                is MovieDetailEffect.OpenUrl -> currentUriHandler.openUri(effect.url)
            }
        }
    }

    MovieDetailContent(state = state, onIntent = viewModel::handleIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailContent(
    state: MovieDetailState,
    onIntent: (MovieDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier, topBar = { MovieDetailTopBar(onIntent = onIntent) }) {
        innerPadding ->
        when {
            state.movieDetail == null && state.errors.isEmpty() ->
                AmroTvLoadingView(modifier = Modifier.fillMaxSize().padding(innerPadding))

            state.errors.isNotEmpty() && state.movieDetail == null ->
                AmroTvErrorView(
                    message = stringResource(state.errors.first().toStringResId()),
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    onRetry = { onIntent(MovieDetailIntent.Retry) },
                )

            state.movieDetail != null -> {
                val detail = requireNotNull(state.movieDetail)
                MovieDetailBody(
                    detail = detail,
                    errors = state.errors,
                    onIntent = onIntent,
                    // Only status bar inset: backdrop extends behind the transparent TopAppBar
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                )
            }
        }
    }
}

@Composable
private fun MovieDetailBody(
    detail: MovieDetailPresentationModel,
    errors: ImmutableList<MovieError>,
    onIntent: (MovieDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        MovieDetailHero(detail = detail)

        Surface(
            modifier = Modifier.fillMaxWidth().offset(y = -AmroTvDimensions.cornerRadiusMedium),
            shape =
                RoundedCornerShape(
                    topStart = AmroTvDimensions.cornerRadiusMedium,
                    topEnd = AmroTvDimensions.cornerRadiusMedium,
                ),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        horizontal = AmroTvDimensions.spacingMedium,
                        vertical = AmroTvDimensions.spacingMedium,
                    ),
                verticalArrangement = Arrangement.spacedBy(AmroTvDimensions.spacingMedium),
            ) {
                InlineErrorBanner(errors = errors, onRetry = { onIntent(MovieDetailIntent.Retry) })

                detail.tagline?.takeIf { it.isNotBlank() }?.let { MovieDetailTagline(tagline = it) }

                if (detail.genres.isNotEmpty()) {
                    MovieDetailGenres(genres = detail.genres)
                }

                MovieDetailOverview(overview = detail.overview)

                MovieDetailStats(detail = detail)

                MovieDetailFinancials(detail = detail)

                detail.imdbId
                    ?.takeIf { it.isNotBlank() }
                    ?.let {
                        MovieDetailImdbButton(
                            onClick = { onIntent(MovieDetailIntent.OpenImdb(it)) }
                        )
                    }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MovieDetailContentPreview(
    @PreviewParameter(MovieDetailStateProvider::class) state: MovieDetailState
) {
    AmroTvTheme { MovieDetailContent(state = state, onIntent = {}) }
}
