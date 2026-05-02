package nl.abnamro.amrotv.feature.movies.ui.moviedetail.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import nl.abnamro.amrotv.feature.movies.ui.R
import nl.abnamro.amrotv.core.ui.preview.LightDarkPreview
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme

@Composable
internal fun LabeledInfo(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.label_value_format, label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@LightDarkPreview
@Composable
private fun LabeledInfoPreview() {
    AmroTvTheme {
        LabeledInfo(label = "Rating", value = "9.0 / 10 (30,455 votes)")
    }
}