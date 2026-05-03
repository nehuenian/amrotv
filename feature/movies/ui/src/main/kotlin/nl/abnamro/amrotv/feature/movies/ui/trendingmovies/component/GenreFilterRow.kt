package nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import nl.abnamro.amrotv.core.ui.preview.PreviewLightDark
import nl.abnamro.amrotv.core.ui.theme.AmroTvDimensions
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.presentation.api.model.GenrePresentationModel
import nl.abnamro.amrotv.feature.movies.ui.R
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.preview.GenreFilterRowPreviewProvider
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.preview.GenreFilterRowPreviewState

@Composable
fun GenreFilterRow(
    genres: ImmutableList<GenrePresentationModel>,
    selectedGenreId: Int?,
    onGenreSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
) {
    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(AmroTvDimensions.spacingSmall),
    ) {
        Spacer(modifier = Modifier)
        FilterChip(
            selected = selectedGenreId == null,
            onClick = { onGenreSelect(null) },
            label = { Text(stringResource(R.string.genre_filter_all)) },
            shape = CircleShape,
            colors =
                FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
        )
        genres.forEach { genre ->
            FilterChip(
                selected = selectedGenreId == genre.id,
                onClick = { onGenreSelect(genre.id) },
                label = { Text(genre.name) },
                shape = CircleShape,
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        }
        Spacer(modifier = Modifier)
    }
}

@PreviewLightDark
@Composable
private fun GenreFilterRowPreview(
    @PreviewParameter(GenreFilterRowPreviewProvider::class) state: GenreFilterRowPreviewState
) {
    AmroTvTheme {
        GenreFilterRow(
            genres = state.genres,
            selectedGenreId = state.selectedGenreId,
            onGenreSelect = {},
        )
    }
}
