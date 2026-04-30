package nl.abnamro.amrotv.feature.movies.ui.moviedetail.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import nl.abnamro.amrotv.core.ui.theme.AmroTvDimensions
import nl.abnamro.amrotv.feature.movies.presentation.api.model.GenrePresentationModel
import nl.abnamro.amrotv.core.ui.preview.LightDarkPreview
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.ui.preview.UIPreviewData

@Composable
internal fun MovieDetailGenres(
    genres: ImmutableList<GenrePresentationModel>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AmroTvDimensions.spacingSmall),
    ) {
        genres.forEach { genre ->
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = genre.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(
                        horizontal = AmroTvDimensions.spacingSmall,
                        vertical = AmroTvDimensions.spacingExtraSmall,
                    ),
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun MovieDetailGenresPreview() {
    AmroTvTheme {
        MovieDetailGenres(genres = UIPreviewData.Genres.all)
    }
}