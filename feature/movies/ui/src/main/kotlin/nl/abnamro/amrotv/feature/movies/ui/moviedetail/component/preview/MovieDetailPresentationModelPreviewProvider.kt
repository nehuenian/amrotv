package nl.abnamro.amrotv.feature.movies.ui.moviedetail.component.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MovieDetailPresentationModel
import nl.abnamro.amrotv.feature.movies.ui.preview.UIPreviewData

internal class MovieDetailPresentationModelPreviewProvider :
    PreviewParameterProvider<MovieDetailPresentationModel> {
    override val values =
        sequenceOf(UIPreviewData.MovieDetails.darkKnight, UIPreviewData.MovieDetails.interstellar)
}
