package nl.abnamro.amrotv.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
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
 * Error view component for displaying error states in the AMRO TV app.
 *
 * Shows an error icon, message, and optional retry button.
 *
 * @param message Error message to display
 * @param modifier Modifier for styling
 * @param onRetry Optional callback when retry button is clicked
 */
@Composable
fun AmroTvErrorView(
    message: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(AmroTvDimensions.spacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = stringResource(R.string.core_ui_error_icon_content_desc),
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier
                .size(AmroTvDimensions.errorIconSize),
        )

        Spacer(modifier = Modifier.height(AmroTvDimensions.spacingMedium))

        Text(
            text = stringResource(R.string.core_ui_error_generic_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )

        Spacer(modifier = Modifier.height(AmroTvDimensions.spacingSmall))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(AmroTvDimensions.spacingMedium))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(0.75f),
            ) {
                Text(stringResource(R.string.core_ui_error_retry_button))
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
private fun AmroTvErrorViewLightPreview() {
    AmroTvTheme(darkTheme = false) {
        AmroTvErrorView(
            message = "Failed to load movies. Please check your internet connection.",
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
private fun AmroTvErrorViewDarkPreview() {
    AmroTvTheme(darkTheme = true) {
        AmroTvErrorView(
            message = "Failed to load movies. Please check your internet connection.",
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "Light Mode - No Retry")
@Composable
private fun AmroTvErrorViewNoRetryLightPreview() {
    AmroTvTheme(darkTheme = false) {
        AmroTvErrorView(
            message = "Failed to load movies. Please try again later.",
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode - No Retry")
@Composable
private fun AmroTvErrorViewNoRetryDarkPreview() {
    AmroTvTheme(darkTheme = true) {
        AmroTvErrorView(
            message = "Failed to load movies. Please try again later.",
        )
    }
}
