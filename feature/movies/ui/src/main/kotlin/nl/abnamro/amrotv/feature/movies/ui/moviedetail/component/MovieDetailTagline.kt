package nl.abnamro.amrotv.feature.movies.ui.moviedetail.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import nl.abnamro.amrotv.core.ui.preview.PreviewLightDark
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.ui.R

@Composable
internal fun MovieDetailTagline(tagline: String, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.movie_tagline_format, tagline),
        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun MovieDetailTaglinePreview() {
    AmroTvTheme { MovieDetailTagline(tagline = "Why so serious?") }
}
