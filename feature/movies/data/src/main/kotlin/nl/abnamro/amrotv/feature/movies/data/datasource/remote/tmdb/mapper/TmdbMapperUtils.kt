package nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.mapper

import java.time.LocalDate
import java.time.format.DateTimeParseException

internal fun parseTmdbDate(dateString: String): LocalDate? =
    dateString.takeIf { it.isNotBlank() }?.let {
        try {
            LocalDate.parse(it)
        } catch (_: DateTimeParseException) {
            null
        }
    }
