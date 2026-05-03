package nl.abnamro.amrotv.feature.movies.ui.moviedetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewParameter
import coil.compose.AsyncImage
import coil.request.ImageRequest
import nl.abnamro.amrotv.core.ui.preview.PreviewLightDark
import nl.abnamro.amrotv.core.ui.theme.AmroTvColors
import nl.abnamro.amrotv.core.ui.theme.AmroTvDimensions
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MovieDetailPresentationModel
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.component.preview.MovieDetailPresentationModelPreviewProvider
import nl.abnamro.amrotv.feature.movies.ui.theme.MoviesDimensions

@Composable
internal fun MovieDetailHero(detail: MovieDetailPresentationModel, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(MoviesDimensions.backdropImageHeight)) {
        AsyncImage(
            model =
                ImageRequest.Builder(context = LocalContext.current)
                    .data(detail.backdropUrl ?: detail.posterUrl)
                    .crossfade(true)
                    .build(),
            // Decorative: title and metadata are rendered as Text overlays above this image.
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(
                                    0.0f to AmroTvColors.MediaScrimDetailStart,
                                    0.45f to AmroTvColors.MediaScrimDetailMid,
                                    1.0f to AmroTvColors.MediaScrimEnd,
                                )
                        )
                    )
        )
        Column(
            modifier =
                Modifier.align(Alignment.BottomStart)
                    .padding(all = AmroTvDimensions.spacingMedium)
                    .padding(bottom = AmroTvDimensions.spacingSmall),
            verticalArrangement = Arrangement.spacedBy(AmroTvDimensions.spacingExtraSmall),
        ) {
            Text(
                text = detail.title,
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        shadow = AmroTvColors.OnMediaTextShadow
                    ),
                color = AmroTvColors.OnMediaPrimary,
                maxLines = 2,
            )
            detail.releaseDate?.let {
                Text(
                    text = it,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            shadow = AmroTvColors.OnMediaTextShadow
                        ),
                    color = AmroTvColors.OnMediaTertiary,
                    modifier = Modifier.padding(bottom = AmroTvDimensions.spacingSmall),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MovieDetailHeroPreview(
    @PreviewParameter(MovieDetailPresentationModelPreviewProvider::class)
    detail: MovieDetailPresentationModel
) {
    AmroTvTheme { MovieDetailHero(detail = detail) }
}
