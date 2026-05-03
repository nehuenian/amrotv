package nl.abnamro.amrotv.feature.movies.ui.moviedetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import nl.abnamro.amrotv.core.ui.preview.PreviewLightDark
import nl.abnamro.amrotv.core.ui.theme.AmroTvDimensions
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MovieDetailPresentationModel
import nl.abnamro.amrotv.feature.movies.ui.R
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.component.preview.MovieDetailPresentationModelPreviewProvider

@Composable
internal fun MovieDetailStats(detail: MovieDetailPresentationModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AmroTvDimensions.spacingExtraSmall),
    ) {
        LabeledInfo(
            label = stringResource(R.string.movie_rating_label),
            value =
                stringResource(
                    R.string.movie_rating_value_format,
                    detail.formattedRating,
                    pluralStringResource(
                        R.plurals.movie_votes_count,
                        detail.voteCount,
                        detail.voteCount,
                    ),
                ),
        )
        detail.runtimeInMinutes?.let {
            LabeledInfo(
                label = stringResource(R.string.movie_runtime_label),
                value = stringResource(R.string.movie_runtime_value_format, it),
            )
        }
        detail.releaseYear?.let {
            LabeledInfo(
                label = stringResource(R.string.movie_release_date_label),
                // TODO: Replace with i18n date formatting library to localise release date display
                value = it,
            )
        }
        LabeledInfo(label = stringResource(R.string.movie_status_label), value = detail.status)
    }
}

@PreviewLightDark
@Composable
private fun MovieDetailStatsPreview(
    @PreviewParameter(MovieDetailPresentationModelPreviewProvider::class)
    detail: MovieDetailPresentationModel
) {
    AmroTvTheme { MovieDetailStats(detail = detail) }
}
