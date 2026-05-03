package nl.abnamro.amrotv.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import nl.abnamro.amrotv.core.ui.theme.AmroTvDimensions
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme

/**
 * Loading view component for displaying loading states in the AMRO TV app.
 *
 * Shows a centered progress indicator with optional message.
 *
 * @param modifier Modifier for styling
 * @param message Optional message to display below the progress indicator
 */
@Composable
fun AmroTvLoadingView(modifier: Modifier = Modifier.fillMaxSize(), message: String? = null) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(AmroTvDimensions.loadingIndicatorSize),
        )

        if (message != null) {
            Spacer(modifier = Modifier.height(AmroTvDimensions.spacingMedium))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
private fun AmroTvLoadingViewLightPreview() {
    AmroTvTheme(darkTheme = false) { AmroTvLoadingView() }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
private fun AmroTvLoadingViewDarkPreview() {
    AmroTvTheme(darkTheme = true) { AmroTvLoadingView() }
}

@Preview(showBackground = true, name = "Light Mode - With Message")
@Composable
private fun AmroTvLoadingViewWithMessageLightPreview() {
    AmroTvTheme(darkTheme = false) { AmroTvLoadingView(message = "Loading movies...") }
}

@Preview(showBackground = true, name = "Dark Mode - With Message")
@Composable
private fun AmroTvLoadingViewWithMessageDarkPreview() {
    AmroTvTheme(darkTheme = true) { AmroTvLoadingView(message = "Loading movies...") }
}
