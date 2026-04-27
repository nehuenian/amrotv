package nl.abnamro.amrotv.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import nl.abnamro.amrotv.core.ui.R
import nl.abnamro.amrotv.core.ui.theme.AmroTvDimensions
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme

/**
 * Empty state view component for displaying empty states in the AMRO TV app.
 *
 * Shows an empty state icon, title, and optional subtitle.
 *
 * @param title Title text for empty state
 * @param modifier Modifier for styling
 * @param subtitle Optional subtitle text for empty state
 */
@Composable
fun AmroTvEmptyView(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = stringResource(R.string.core_ui_empty_icon_content_desc),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(AmroTvDimensions.emptyStateIconSize),
        )

        Spacer(modifier = Modifier.height(AmroTvDimensions.spacingMedium))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(AmroTvDimensions.spacingSmall))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(horizontal = AmroTvDimensions.spacingMedium),
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
private fun AmroTvEmptyViewLightPreview() {
    AmroTvTheme(darkTheme = false) {
        AmroTvEmptyView(title = "No movies found")
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
private fun AmroTvEmptyViewDarkPreview() {
    AmroTvTheme(darkTheme = true) {
        AmroTvEmptyView(title = "No movies found")
    }
}

@Preview(showBackground = true, name = "Light Mode - With Subtitle")
@Composable
private fun AmroTvEmptyViewWithSubtitleLightPreview() {
    AmroTvTheme(darkTheme = false) {
        AmroTvEmptyView(
            title = "No movies found",
            subtitle = "Try adjusting your filters or check back later.",
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode - With Subtitle")
@Composable
private fun AmroTvEmptyViewWithSubtitleDarkPreview() {
    AmroTvTheme(darkTheme = true) {
        AmroTvEmptyView(
            title = "No movies found",
            subtitle = "Try adjusting your filters or check back later.",
        )
    }
}
