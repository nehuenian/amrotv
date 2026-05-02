package nl.abnamro.amrotv.feature.movies.presentation.implementation.trendingmovies

/**
 * Provides a human-readable label describing the current week's date range (e.g. "May 1 – May 7").
 */
interface WeekRangeLabelProvider {

    /**
     * Returns a formatted label for the current week's date range.
     *
     * @param nowMillis the timestamp in milliseconds representing "now";
     *   defaults to `System.currentTimeMillis()`.
     * @return a locale-formatted string such as "May 1 – May 7".
     */
    fun currentWeekRangeLabel(nowMillis: Long = System.currentTimeMillis()): String
}
