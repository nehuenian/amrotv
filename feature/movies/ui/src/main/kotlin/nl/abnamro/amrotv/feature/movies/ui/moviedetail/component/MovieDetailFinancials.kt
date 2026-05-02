package nl.abnamro.amrotv.feature.movies.ui.moviedetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import nl.abnamro.amrotv.core.ui.theme.AmroTvDimensions
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MovieDetailPresentationModel
import nl.abnamro.amrotv.feature.movies.ui.R
import androidx.compose.ui.tooling.preview.PreviewParameter
import nl.abnamro.amrotv.core.ui.preview.LightDarkPreview
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.component.preview.MovieDetailPresentationModelPreviewProvider

@Composable
internal fun MovieDetailFinancials(
    detail: MovieDetailPresentationModel,
    modifier: Modifier = Modifier,
) {
    if (detail.formattedBudget == null && detail.formattedRevenue == null) return
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AmroTvDimensions.spacingExtraSmall),
    ) {
        detail.formattedBudget?.let {
            LabeledInfo(
                label = stringResource(R.string.movie_budget_label),
                value = it,
            )
        }
        detail.formattedRevenue?.let {
            LabeledInfo(
                label = stringResource(R.string.movie_revenue_label),
                value = it,
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun MovieDetailFinancialsPreview(
    @PreviewParameter(MovieDetailPresentationModelPreviewProvider::class) detail: MovieDetailPresentationModel,
) {
    AmroTvTheme {
        MovieDetailFinancials(detail = detail)
    }
}