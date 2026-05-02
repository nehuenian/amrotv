package nl.abnamro.amrotv.feature.movies.presentation.implementation.trendingmovies

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

// TODO: Replace with a proper i18n date/calendar library for locale-aware week range formatting
class WeekRangeLabelProviderImpl @Inject constructor() : WeekRangeLabelProvider {

    override fun currentWeekRangeLabel(nowMillis: Long): String {
        val cal =
            Calendar.getInstance().apply {
                timeInMillis = nowMillis
                firstDayOfWeek = Calendar.MONDAY
            }
        val offset =
            (cal.get(Calendar.DAY_OF_WEEK) - cal.firstDayOfWeek + DAYS_IN_WEEK) % DAYS_IN_WEEK
        cal.add(Calendar.DATE, -offset)
        val start = cal.time
        cal.add(Calendar.DATE, WEEK_DAYS_OFFSET)
        val end = cal.time
        val fmt = SimpleDateFormat("MMM d", Locale.ENGLISH)
        return "${fmt.format(start)} – ${fmt.format(end)}"
    }

    private companion object {
        const val DAYS_IN_WEEK = 7
        const val WEEK_DAYS_OFFSET = 6
    }
}
