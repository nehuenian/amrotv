package nl.abnamro.amrotv.feature.movies.ui.trendingmovies

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import nl.abnamro.amrotv.core.buildconfig.BuildConfigProvider
import nl.abnamro.amrotv.core.testing.robot.withRobot
import nl.abnamro.amrotv.core.ui.theme.AmroTvTheme
import nl.abnamro.amrotv.feature.movies.presentation.implementation.moviedetail.MovieDetailViewModel
import nl.abnamro.amrotv.feature.movies.presentation.implementation.trendingmovies.TrendingMoviesViewModel
import nl.abnamro.amrotv.feature.movies.ui.E2ETestData
import nl.abnamro.amrotv.feature.movies.ui.MoviesE2ETestActivity
import nl.abnamro.amrotv.feature.movies.ui.mock.MockWebServerRule
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.MovieDetailScreen
import nl.abnamro.amrotv.feature.movies.ui.moviedetail.robots.MovieDetailRobot
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.robots.TrendingMoviesRobot
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Singleton

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MoviesFlowE2ETest {

    @get:Rule(order = 0)
    val mockServerRule = MockWebServerRule()

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MoviesE2ETestActivity>()

    @Module
    @InstallIn(SingletonComponent::class)
    object TestBuildConfigModule {

        @Provides
        @Singleton
        fun provideBuildConfigProvider(): BuildConfigProvider = object : BuildConfigProvider {
            override val tmdbReadAccessToken: String get() = ""
            override val isDebug: Boolean get() = true
        }
    }

    @Before
    fun setUp() {
        composeRule.setContent {
            var currentScreen: AppScreen by remember { mutableStateOf(AppScreen.TrendingMovies) }
            AmroTvTheme {
                when (val screen = currentScreen) {
                    is AppScreen.TrendingMovies -> TrendingMoviesScreen(
                        onNavigateToMovieDetail = { movieId ->
                            currentScreen = AppScreen.MovieDetail(movieId)
                        },
                        viewModel = hiltViewModel<TrendingMoviesViewModel>(),
                    )

                    is AppScreen.MovieDetail -> MovieDetailScreen(
                        navigateBack = { currentScreen = AppScreen.TrendingMovies },
                        viewModel = hiltViewModel<MovieDetailViewModel, MovieDetailViewModel.Factory>(
                            creationCallback = { factory -> factory.create(movieId = screen.movieId) },
                        ),
                    )
                }
            }
        }
    }

    // GIVEN the movies screen is open WHEN movies are loaded THEN at least one movie card is visible
    @Test
    fun givenMoviesScreenOpen_whenMoviesLoaded_thenAtLeastOneMovieCardVisible() {
        withRobot(TrendingMoviesRobot(composeRule)) {
            verify {
                onMoviesLoaded {
                    trendingMoviesTitleVisible()
                    genreFilterAllChipVisible()
                    featuredBannerVisible()
                    atLeastOneMovieVisible()
                    releasedTagVisible()
                }
            }
        }
    }

    // GIVEN movies are loaded WHEN tapping the first movie card THEN the detail screen shows a title and an IMDb link
    @Test
    fun givenMoviesLoaded_whenTapFirstMovieCard_thenDetailShowsTitleAndImdbLink() {
        withRobot(TrendingMoviesRobot(composeRule)) {
            execute {
                sortByReleaseDateAscending()
                onMoviesLoaded {
                    clickFirstFeaturedMovie()
                }
            }
        }
        withRobot(MovieDetailRobot(composeRule)) {
            verify {
                onDetailLoaded {
                    backButtonVisible()
                    ratingLabelVisible()
                    imdbLinkVisible()
                    titleVisible(E2ETestData.HOPPERS_TITLE)
                    overviewVisible()
                    statusLabelVisible()
                }
            }
        }
    }

    // GIVEN the movie detail screen is open WHEN navigating back THEN the movies list is restored
    @Test
    fun givenDetailScreenOpen_whenNavigateBack_thenMoviesListRestored() {
        withRobot(TrendingMoviesRobot(composeRule)) {
            execute {
                sortByReleaseDateAscending()
                onMoviesLoaded {
                    clickFirstFeaturedMovie()
                }
            }
        }
        withRobot(MovieDetailRobot(composeRule)) {
            execute {
                onDetailLoaded {
                    tapBack()
                }
            }
        }
        withRobot(TrendingMoviesRobot(composeRule)) {
            verify {
                onMoviesLoaded {
                    trendingMoviesTitleVisible()
                    atLeastOneMovieVisible()
                }
            }
        }
    }

    // GIVEN movies are loaded WHEN filtering by Science Fiction THEN only SciFi movies are visible
    @Test
    fun givenMoviesLoaded_whenFilterBySciFi_thenOnlySciFiMoviesVisible() {
        withRobot(TrendingMoviesRobot(composeRule)) {
            execute {
                onMoviesLoaded {
                    filterByGenre(E2ETestData.SCIENCE_FICTION_GENRE_NAME)
                }
            }
            verify {
                onMoviesLoaded {
                    atLeastOneMovieVisible()
                    movieVisible(E2ETestData.PROJECT_HAIL_MARY_TITLE)
                    // Drama/non-SciFi movies must be absent after SciFi filter
                    movieNotVisible(E2ETestData.THE_DEVIL_WEARS_PRADA_2_TITLE)
                    movieNotVisible(E2ETestData.MICHAEL_TITLE)
                }
            }
        }
    }

    // GIVEN movies are loaded WHEN sorting by release date ascending THEN the oldest movie is featured
    @Test
    fun givenMoviesLoaded_whenSortByReleaseDateAscending_thenOldestMovieIsFeatured() {
        withRobot(TrendingMoviesRobot(composeRule)) {
            execute {
                sortByReleaseDateAscending()
            }
            verify {
                onMoviesLoaded {
                    // Hoppers (2020-01-01) is oldest — must appear as featured banner
                    movieVisible(E2ETestData.HOPPERS_TITLE)
                    // Highest-popularity movie is no longer featured but still in the list
                    movieVisible(E2ETestData.APEX_TITLE)
                }
            }
        }
    }

    // GIVEN movies are loaded in default sort WHEN scrolling down THEN a low-popularity movie becomes visible
    @Test
    fun givenMoviesLoaded_whenScrollingDown_thenLowPopularityMovieBecomesVisible() {
        withRobot(TrendingMoviesRobot(composeRule)) {
            execute {
                onMoviesLoaded {
                    // Project Hail Mary has the lowest popularity and is last in default (Popularity DESC) sort
                    scrollToMovie(E2ETestData.LAST_IN_DEFAULT_SORT_TITLE)
                }
            }
            verify {
                movieVisibleNow(E2ETestData.LAST_IN_DEFAULT_SORT_TITLE)
            }
        }
    }

    private sealed interface AppScreen {
        data object TrendingMovies : AppScreen
        data class MovieDetail(val movieId: Int) : AppScreen
    }
}
