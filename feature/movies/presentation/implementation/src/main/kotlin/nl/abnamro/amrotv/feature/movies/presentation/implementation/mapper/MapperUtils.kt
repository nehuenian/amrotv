package nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper

// TODO: Replace with a proper i18n date library for locale-aware year extraction
internal fun extractReleaseYear(releaseDate: String): String? =
    releaseDate.split("-").firstOrNull()?.takeIf { it.length == 4 }

// TODO: Replace with a locale-aware number formatter from an i18n library
internal fun formatRating(voteAverage: Double): String =
    String.format(java.util.Locale.US, "%.1f", voteAverage)
