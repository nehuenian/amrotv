package nl.abnamro.amrotv.feature.movies.presentation.implementation.trendingmovies

import java.util.Calendar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WeekRangeLabelProviderImplTest {

    private lateinit var provider: WeekRangeLabelProvider

    @BeforeEach
    fun setUp() {
        provider = WeekRangeLabelProviderImpl()
    }

    @Nested
    @DisplayName("GIVEN a timestamp pointing to Wednesday May 7 2025")
    inner class GivenTimestampAtWednesdayMaySeventhTwentyTwentyFive {

        private var nowMillis: Long = 0L

        @BeforeEach
        fun setUp() {
            nowMillis =
                Calendar.getInstance()
                    .apply {
                        clear()
                        set(2025, Calendar.MAY, 7)
                    }
                    .timeInMillis
        }

        @Nested
        @DisplayName("WHEN asked for the week label for May 7 2025")
        inner class WhenAskedForWeekLabelForMay72025 {

            @Test
            @DisplayName("THEN the label covers May 5 to May 11")
            fun labelCoversMayFiveToMayEleven() {
                assertEquals("May 5 – May 11", provider.currentWeekRangeLabel(nowMillis))
            }
        }
    }
}
