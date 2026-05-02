package nl.abnamro.amrotv.feature.movies.ui.moviedetail.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import nl.abnamro.amrotv.feature.movies.ui.R
import nl.abnamro.amrotv.core.ui.preview.LightDarkPreview
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme

@Composable
internal fun MovieDetailImdbButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.view_on_imdb))
    }
}

@LightDarkPreview
@Composable
private fun MovieDetailImdbButtonPreview() {
    AmroTvTheme {
        MovieDetailImdbButton(onClick = {})
    }
}