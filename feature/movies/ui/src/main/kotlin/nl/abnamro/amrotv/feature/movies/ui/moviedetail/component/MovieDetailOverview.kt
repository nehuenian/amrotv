package nl.abnamro.amrotv.feature.movies.ui.moviedetail.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import nl.abnamro.amrotv.core.ui.preview.PreviewLightDark
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.ui.R
import nl.abnamro.amrotv.feature.movies.ui.preview.UIPreviewData

@Composable
internal fun MovieDetailOverview(
    overview: String,
    modifier: Modifier = Modifier,
) {
    val overviewDescription = stringResource(R.string.movie_overview_section)
    Column(modifier = modifier.semantics { contentDescription = overviewDescription }) {
        var overviewExpanded by rememberSaveable { mutableStateOf(false) }
        var overviewOverflows by rememberSaveable { mutableStateOf(false) }
        Text(
            text = overview,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (overviewExpanded) Int.MAX_VALUE else 4,
            overflow = if (overviewExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
            modifier = Modifier.animateContentSize(),
            onTextLayout = { result ->
                if (!overviewExpanded) overviewOverflows = result.hasVisualOverflow
            },
        )
        if (overviewOverflows) {
            TextButton(
                onClick = { overviewExpanded = !overviewExpanded },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    if (overviewExpanded) stringResource(R.string.show_less)
                    else stringResource(R.string.show_more)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MovieDetailOverviewPreview() {
    AmroTvTheme { MovieDetailOverview(overview = UIPreviewData.MovieDetails.darkKnight.overview) }
}
