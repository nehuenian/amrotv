package nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError

internal class InlineErrorBannerPreviewProvider :
    PreviewParameterProvider<ImmutableList<MovieError>> {
    override val values = sequenceOf(
        persistentListOf(MovieError.GENRES_LOAD_FAILED),
        persistentListOf(MovieError.GENRES_LOAD_FAILED, MovieError.MOVIES_LOAD_FAILED),
    )
}
