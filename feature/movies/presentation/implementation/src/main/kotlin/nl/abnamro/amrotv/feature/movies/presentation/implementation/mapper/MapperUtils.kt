package nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val releaseDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

// TODO: Replace with a locale-aware date formatter from an i18n library
internal fun formatReleaseDate(releaseDate: LocalDate?): String? =
    releaseDate?.format(releaseDateFormatter)

// TODO: Replace with a locale-aware date helper from an i18n library
internal fun isReleased(releaseDate: LocalDate?): Boolean =
    releaseDate != null && !releaseDate.isAfter(LocalDate.now())

// TODO: Replace with a locale-aware number formatter from an i18n library
internal fun formatRating(voteAverage: Double): String =
    String.format(Locale.US, "%.1f", voteAverage)
