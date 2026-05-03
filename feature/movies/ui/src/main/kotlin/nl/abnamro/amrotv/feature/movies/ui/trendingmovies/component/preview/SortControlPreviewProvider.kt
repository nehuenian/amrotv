package nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder

internal data class SortControlPreviewState(val option: SortOption, val order: SortOrder)

internal class SortControlPreviewProvider : PreviewParameterProvider<SortControlPreviewState> {
    override val values =
        sequenceOf(
            SortControlPreviewState(SortOption.POPULARITY, SortOrder.DESC),
            SortControlPreviewState(SortOption.RELEASE_DATE, SortOrder.ASC),
            SortControlPreviewState(SortOption.TITLE, SortOrder.DESC),
        )
}
