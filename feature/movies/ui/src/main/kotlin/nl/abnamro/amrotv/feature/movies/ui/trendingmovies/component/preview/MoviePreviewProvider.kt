package nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MoviePresentationModel
import nl.abnamro.amrotv.feature.movies.ui.preview.UIPreviewData

internal class MoviePreviewProvider : PreviewParameterProvider<MoviePresentationModel> {
    override val values =
        sequenceOf(
            UIPreviewData.Movies.darkKnight,
            UIPreviewData.Movies.inception,
            UIPreviewData.Movies.interstellar,
        )
}
