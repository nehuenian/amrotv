package nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import nl.abnamro.amrotv.core.ui.theme.AmroTvColors
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesIntent
import nl.abnamro.amrotv.feature.movies.ui.R
import androidx.compose.ui.tooling.preview.PreviewParameter
import nl.abnamro.amrotv.core.ui.preview.LightDarkPreview
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.preview.SortControlPreviewProvider
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.preview.SortControlPreviewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrendingMoviesTopBar(
    weekRangeLabel: String,
    selectedSortOption: SortOption,
    selectedSortOrder: SortOrder,
    showSortSheet: Boolean,
    onIntent: (TrendingMoviesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(stringResource(R.string.trending_movies_title))
                Text(
                    text = weekRangeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = AmroTvColors.OnPrimarySubtle,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        actions = {
            SortControl(
                sortOption = selectedSortOption,
                sortOrder = selectedSortOrder,
                showSheet = showSortSheet,
                onShowSheetChange = { visible -> onIntent(TrendingMoviesIntent.SetSortSheetVisible(visible)) },
                onSortOptionSelected = { option -> onIntent(TrendingMoviesIntent.ChangeSortOption(option)) },
                onSortOrderSelected = { order -> onIntent(TrendingMoviesIntent.SelectSortOrder(order)) },
            )
        },
    )
}

@LightDarkPreview
@Composable
private fun TrendingMoviesTopBarPreview(
    @PreviewParameter(SortControlPreviewProvider::class) state: SortControlPreviewState,
) {
    AmroTvTheme {
        TrendingMoviesTopBar(
            weekRangeLabel = "Jan 1 – Jan 7",
            selectedSortOption = state.option,
            selectedSortOrder = state.order,
            showSortSheet = false,
            onIntent = {},
        )
    }
}
