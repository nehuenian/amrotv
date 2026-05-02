package nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper

import nl.abnamro.amrotv.core.mvi.Mapper
import nl.abnamro.amrotv.feature.movies.domain.api.model.Genre
import nl.abnamro.amrotv.feature.movies.presentation.api.model.GenrePresentationModel
import javax.inject.Inject

class GenreDomainToPresentationMapper @Inject constructor() : Mapper<Genre, GenrePresentationModel> {

    override fun map(input: Genre) = GenrePresentationModel(
        id = input.id,
        name = input.name,
    )
}
