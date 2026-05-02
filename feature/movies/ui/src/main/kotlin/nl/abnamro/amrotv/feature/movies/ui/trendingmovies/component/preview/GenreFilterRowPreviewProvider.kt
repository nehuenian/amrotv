package nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.ImmutableList
import nl.abnamro.amrotv.feature.movies.presentation.api.model.GenrePresentationModel
import nl.abnamro.amrotv.feature.movies.ui.preview.UIPreviewData

internal data class GenreFilterRowPreviewState(
    val genres: ImmutableList<GenrePresentationModel>,
    val selectedGenreId: Int?,
)

internal class GenreFilterRowPreviewProvider :
    PreviewParameterProvider<GenreFilterRowPreviewState> {
    override val values = sequenceOf(
        GenreFilterRowPreviewState(UIPreviewData.Genres.all, UIPreviewData.Genres.all.first().id),
        GenreFilterRowPreviewState(UIPreviewData.Genres.all, null),
    )
}
