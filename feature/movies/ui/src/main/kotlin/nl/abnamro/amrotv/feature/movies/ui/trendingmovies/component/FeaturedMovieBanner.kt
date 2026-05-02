package nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import coil.compose.AsyncImage
import coil.request.ImageRequest
import nl.abnamro.amrotv.core.ui.preview.PreviewLightDark
import nl.abnamro.amrotv.core.ui.theme.AmroTvColors
import nl.abnamro.amrotv.core.ui.theme.AmroTvDimensions
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MoviePresentationModel
import nl.abnamro.amrotv.feature.movies.ui.R
import nl.abnamro.amrotv.feature.movies.ui.theme.MoviesDimensions
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.preview.MoviePreviewProvider

@Composable
fun FeaturedMovieBanner(
    movie: MoviePresentationModel,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().height(MoviesDimensions.featuredBannerHeight)) {
        BannerBackground(movie = movie)
        BannerContent(
            movie = movie,
            onMovieClick = onMovieClick,
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

@Composable
private fun BannerBackground(movie: MoviePresentationModel) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model =
                ImageRequest.Builder(context)
                    .data(movie.backdropUrl ?: movie.posterUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(
                                    0.0f to AmroTvColors.MediaScrimStart,
                                    0.5f to AmroTvColors.MediaScrimMid,
                                    1.0f to AmroTvColors.MediaScrimEnd,
                                )
                        )
                    )
        )
    }
}

@Composable
private fun BannerContent(
    movie: MoviePresentationModel,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier.padding(
                horizontal = AmroTvDimensions.spacingMedium,
                vertical = AmroTvDimensions.spacingMedium,
            ),
        verticalArrangement = Arrangement.spacedBy(AmroTvDimensions.spacingExtraSmall),
    ) {
        Text(
            text = movie.title,
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    shadow = AmroTvColors.OnMediaTextShadow
                ),
            color = AmroTvColors.OnMediaPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AmroTvDimensions.spacingMedium),
        ) {
            Text(
                text = stringResource(R.string.movie_rating_format, movie.formattedRating),
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        shadow = AmroTvColors.OnMediaTextShadow
                    ),
                color = AmroTvColors.OnMediaSecondary,
            )
            val movieContentDescription =
                stringResource(R.string.featured_more_info_description, movie.title)
            OutlinedButton(
                onClick = { onMovieClick(movie.id) },
                border =
                    BorderStroke(MoviesDimensions.borderWidthDefault, AmroTvColors.OnMediaPrimary),
                colors =
                    ButtonDefaults.outlinedButtonColors(contentColor = AmroTvColors.OnMediaPrimary),
                modifier =
                    Modifier.clearAndSetSemantics { contentDescription = movieContentDescription },
            ) {
                Text(stringResource(R.string.featured_more_info))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun FeaturedMovieBannerPreview(
    @PreviewParameter(MoviePreviewProvider::class) movie: MoviePresentationModel
) {
    AmroTvTheme { FeaturedMovieBanner(movie = movie, onMovieClick = {}) }
}
