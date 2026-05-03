package nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
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
fun MovieCard(movie: MoviePresentationModel, onMovieClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(onClick = { onMovieClick(movie.id) }, modifier = modifier.fillMaxWidth().testTag(MovieCardSemantics.TEST_TAG)) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(movie.posterUrl).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(MoviesDimensions.movieCardGradientHeight)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(AmroTvColors.MediaScrimTransparent, AmroTvColors.MediaScrimEnd)
                            )
                        )
            )
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(AmroTvDimensions.spacingSmall)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall.copy(shadow = AmroTvColors.OnMediaTextShadow),
                    color = AmroTvColors.OnMediaPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.movie_rating_format, movie.formattedRating),
                    style = MaterialTheme.typography.bodySmall.copy(shadow = AmroTvColors.OnMediaTextShadow),
                    color = AmroTvColors.OnMediaSecondary,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MovieCardPreview(@PreviewParameter(MoviePreviewProvider::class) movie: MoviePresentationModel) {
    AmroTvTheme { MovieCard(movie = movie, onMovieClick = {}) }
}
