package nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import nl.abnamro.amrotv.core.ui.preview.PreviewLightDark
import nl.abnamro.amrotv.core.ui.theme.AmroTvDimensions
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError
import nl.abnamro.amrotv.feature.movies.ui.R
import nl.abnamro.amrotv.feature.movies.ui.theme.MoviesDimensions
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.preview.InlineErrorBannerPreviewProvider
import nl.abnamro.amrotv.feature.movies.ui.util.toStringResId

@Composable
internal fun InlineErrorBanner(
    errors: ImmutableList<MovieError>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = errors.isNotEmpty(), modifier = modifier) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier =
                    Modifier.padding(
                        horizontal = AmroTvDimensions.spacingMedium,
                        vertical = AmroTvDimensions.spacingSmall,
                    ),
                horizontalArrangement = Arrangement.spacedBy(AmroTvDimensions.spacingSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(MoviesDimensions.iconSizeSmall),
                )
                Column(modifier = Modifier.weight(1f)) {
                    errors.forEach { error ->
                        Text(
                            text = stringResource(error.toStringResId()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
                TextButton(onClick = onRetry) {
                    Text(
                        text = stringResource(R.string.error_retry),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun InlineErrorBannerPreview(
    @PreviewParameter(InlineErrorBannerPreviewProvider::class) errors: ImmutableList<MovieError>
) {
    AmroTvTheme { InlineErrorBanner(errors = errors, onRetry = {}) }
}
