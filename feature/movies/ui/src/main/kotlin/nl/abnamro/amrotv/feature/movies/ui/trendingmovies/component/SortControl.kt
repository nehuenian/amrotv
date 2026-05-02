package nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.coroutines.launch
import nl.abnamro.amrotv.core.ui.preview.PreviewLightDark
import nl.abnamro.amrotv.core.ui.theme.AmroTvDimensions
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder
import nl.abnamro.amrotv.feature.movies.ui.R
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.preview.SortControlPreviewProvider
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.preview.SortControlPreviewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortControl(
    sortOption: SortOption,
    sortOrder: SortOrder,
    showSheet: Boolean,
    onShowSheetChange: (Boolean) -> Unit,
    onSortOptionSelect: (SortOption) -> Unit,
    onSortOrderSelect: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    ElevatedAssistChip(
        onClick = { onShowSheetChange(true) },
        label = {
            Text(
                stringResource(
                    R.string.sort_chip_label,
                    sortOption.toDisplayName(),
                    sortOrder.toDisplayName(),
                )
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
            )
        },
        shape = CircleShape,
        modifier = modifier,
    )

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { onShowSheetChange(false) }, sheetState = sheetState) {
            SortSheetContent(
                sortOption = sortOption,
                sortOrder = sortOrder,
                onSortOptionSelect = { selected ->
                    onSortOptionSelect(selected)
                    coroutineScope.launch {
                        sheetState.hide()
                        onShowSheetChange(false)
                    }
                },
                onSortOrderSelect = onSortOrderSelect,
            )
        }
    }
}

@Composable
private fun SortSheetContent(
    sortOption: SortOption,
    sortOrder: SortOrder,
    onSortOptionSelect: (SortOption) -> Unit,
    onSortOrderSelect: (SortOrder) -> Unit,
) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = AmroTvDimensions.spacingMedium)
                .padding(bottom = AmroTvDimensions.spacingExtraLarge),
        verticalArrangement = Arrangement.spacedBy(AmroTvDimensions.spacingSmall),
    ) {
        Text(
            text = stringResource(R.string.sort_by_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AmroTvDimensions.spacingSmall),
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SortOption.entries.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = sortOption == option,
                    onClick = { onSortOptionSelect(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, SortOption.entries.size),
                    label = { Text(option.toDisplayName()) },
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = AmroTvDimensions.spacingSmall))
        Text(
            text = stringResource(R.string.sort_order_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SortOrder.entries.forEachIndexed { index, order ->
                SegmentedButton(
                    selected = sortOrder == order,
                    onClick = { onSortOrderSelect(order) },
                    shape = SegmentedButtonDefaults.itemShape(index, SortOrder.entries.size),
                    icon = {
                        Icon(
                            imageVector =
                                if (order == SortOrder.ASC) {
                                    Icons.Filled.ArrowUpward
                                } else {
                                    Icons.Filled.ArrowDownward
                                },
                            contentDescription = null,
                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                        )
                    },
                    label = { Text(order.toDisplayName()) },
                )
            }
        }
    }
}

@Composable
private fun SortOption.toDisplayName(): String =
    stringResource(
        when (this) {
            SortOption.POPULARITY -> R.string.sort_popularity
            SortOption.TITLE -> R.string.sort_title
            SortOption.RELEASE_DATE -> R.string.sort_release_date
        }
    )

@Composable
private fun SortOrder.toDisplayName(): String =
    stringResource(
        when (this) {
            SortOrder.ASC -> R.string.sort_ascending_description
            SortOrder.DESC -> R.string.sort_descending_description
        }
    )

@PreviewLightDark
@Composable
private fun SortControlPreview(
    @PreviewParameter(SortControlPreviewProvider::class) state: SortControlPreviewState
) {
    AmroTvTheme {
        SortControl(
            sortOption = state.option,
            sortOrder = state.order,
            showSheet = false,
            onShowSheetChange = {},
            onSortOptionSelect = {},
            onSortOrderSelect = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SortSheetContentPreview(
    @PreviewParameter(SortControlPreviewProvider::class) state: SortControlPreviewState
) {
    AmroTvTheme {
        SortSheetContent(
            sortOption = state.option,
            sortOrder = state.order,
            onSortOptionSelect = {},
            onSortOrderSelect = {},
        )
    }
}
