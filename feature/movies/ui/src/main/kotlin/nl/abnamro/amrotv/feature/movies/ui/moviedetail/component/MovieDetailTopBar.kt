package nl.abnamro.amrotv.feature.movies.ui.moviedetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import nl.abnamro.amrotv.core.ui.theme.AmroTvColors
import nl.abnamro.amrotv.feature.movies.ui.R
import nl.abnamro.amrotv.core.ui.preview.LightDarkPreview
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.presentation.api.moviedetail.MovieDetailIntent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MovieDetailTopBar(
    onIntent: (MovieDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {},
        navigationIcon = {
            IconButton(
                onClick = { onIntent(MovieDetailIntent.NavigateBack) },
                modifier = Modifier
                    .background(
                        color = AmroTvColors.MediaNavIconBackground,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back_description),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AmroTvColors.MediaScrimTransparent,
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

@LightDarkPreview
@Composable
private fun MovieDetailTopBarPreview() {
    AmroTvTheme { MovieDetailTopBar(onIntent = {}) }
}
