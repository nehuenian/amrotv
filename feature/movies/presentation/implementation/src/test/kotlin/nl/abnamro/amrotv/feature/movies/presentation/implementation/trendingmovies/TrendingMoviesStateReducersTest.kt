package nl.abnamro.amrotv.feature.movies.presentation.implementation.trendingmovies

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.collections.immutable.persistentListOf
import nl.abnamro.amrotv.core.mvi.reduceWith
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOption
import nl.abnamro.amrotv.feature.movies.domain.api.model.SortOrder
import nl.abnamro.amrotv.feature.movies.presentation.api.MovieError
import nl.abnamro.amrotv.feature.movies.presentation.api.trendingmovies.TrendingMoviesState
import nl.abnamro.amrotv.feature.movies.presentation.implementation.PresentationMocks
import nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper.GenreDomainToPresentationMapper
import nl.abnamro.amrotv.feature.movies.presentation.implementation.mapper.MovieDomainToPresentationMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TrendingMoviesStateReducersTest {

  @MockK lateinit var movieDomainToPresentationMapper: MovieDomainToPresentationMapper

  @MockK lateinit var genreDomainToPresentationMapper: GenreDomainToPresentationMapper

  @MockK lateinit var weekRangeLabelProvider: WeekRangeLabelProvider

  private lateinit var stateReducers: TrendingMoviesStateReducers

  @BeforeEach
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    stateReducers =
        TrendingMoviesStateReducers(
            movieDomainToPresentationMapper = movieDomainToPresentationMapper,
            genreDomainToPresentationMapper = genreDomainToPresentationMapper,
            weekRangeLabelProvider = weekRangeLabelProvider,
        )
  }

  @Nested
  @DisplayName("GIVEN the week range label is available for the current week")
  inner class GivenWeekLabelForCurrentWeekIsAvailable {

    @BeforeEach
    fun setUp() {
      every { weekRangeLabelProvider.currentWeekRangeLabel(any()) } returns WEEK_LABEL
    }

    @Nested
    @DisplayName("WHEN the initial screen state is built")
    inner class WhenInitialScreenStateIsBuilt {

      @Test
      @DisplayName("THEN the current week range is shown on screen")
      fun currentWeekRangeIsShownOnScreen() {
        assertEquals(WEEK_LABEL, stateReducers.initialState().weekRangeLabel)
      }

      @Test
      @DisplayName("THEN the screen is not in a loading state")
      fun screenIsNotInALoadingState() {
        assertFalse(stateReducers.initialState().isLoading)
      }
    }
  }

  @Nested
  @DisplayName("GIVEN movies, genres, and the week range label are available")
  inner class GivenMoviesGenresAndWeekLabelAreAvailable {

    @BeforeEach
    fun setUp() {
      every { weekRangeLabelProvider.currentWeekRangeLabel(any()) } returns WEEK_LABEL
      every { movieDomainToPresentationMapper.map(PresentationMocks.Movies.action) } returns
          PresentationMocks.PresentationMovies.action
      every { movieDomainToPresentationMapper.map(PresentationMocks.Movies.comedy) } returns
          PresentationMocks.PresentationMovies.comedy
      every { genreDomainToPresentationMapper.map(PresentationMocks.Genres.action) } returns
          PresentationMocks.PresentationGenres.action
      every { genreDomainToPresentationMapper.map(PresentationMocks.Genres.comedy) } returns
          PresentationMocks.PresentationGenres.comedy
    }

    @Nested
    @DisplayName("WHEN movies and genres finish loading without errors")
    inner class WhenMoviesAndGenresFinishLoadingWithoutErrors {

      private val initialState = TrendingMoviesState(isLoading = true)

      @Test
      @DisplayName("THEN the loading indicator disappears")
      fun loadingIndicatorDisappears() {
        val result =
            initialState.reduceWith(
                stateReducers.contentLoaded(
                    movies = PresentationMocks.Movies.all,
                    genres = PresentationMocks.Genres.all,
                )
            )
        assertFalse(result.isLoading)
      }

      @Test
      @DisplayName("THEN the week range label is updated on screen")
      fun weekRangeLabelIsUpdatedOnScreen() {
        val result =
            initialState.reduceWith(
                stateReducers.contentLoaded(
                    movies = PresentationMocks.Movies.all,
                    genres = PresentationMocks.Genres.all,
                )
            )
        assertEquals(WEEK_LABEL, result.weekRangeLabel)
      }

      @Test
      @DisplayName("THEN movies are displayed on screen")
      fun moviesAreDisplayedOnScreen() {
        val result =
            initialState.reduceWith(
                stateReducers.contentLoaded(
                    movies = PresentationMocks.Movies.all,
                    genres = PresentationMocks.Genres.all,
                )
            )
        assertEquals(
            persistentListOf(
                PresentationMocks.PresentationMovies.action,
                PresentationMocks.PresentationMovies.comedy,
            ),
            result.movies,
        )
      }

      @Test
      @DisplayName("THEN genres are available for filtering")
      fun genresAreAvailableForFiltering() {
        val result =
            initialState.reduceWith(
                stateReducers.contentLoaded(
                    movies = PresentationMocks.Movies.all,
                    genres = PresentationMocks.Genres.all,
                )
            )
        assertEquals(
            persistentListOf(
                PresentationMocks.PresentationGenres.action,
                PresentationMocks.PresentationGenres.comedy,
            ),
            result.genres,
        )
      }

      @Test
      @DisplayName("THEN no errors are shown to the user")
      fun noErrorsAreShownToTheUser() {
        val result =
            initialState.reduceWith(
                stateReducers.contentLoaded(
                    movies = PresentationMocks.Movies.all,
                    genres = PresentationMocks.Genres.all,
                )
            )
        assertEquals(persistentListOf<MovieError>(), result.errors)
      }
    }

    @Nested
    @DisplayName("WHEN movies and genres finish loading but movies failed to fetch")
    inner class WhenMoviesAndGenresLoadButMoviesFailedToFetch {

      @Test
      @DisplayName("THEN the movies load failure error is shown to the user")
      fun moviesLoadFailureErrorIsShown() {
        val result =
            TrendingMoviesState()
                .reduceWith(
                    stateReducers.contentLoaded(
                        movies = PresentationMocks.Movies.all,
                        genres = PresentationMocks.Genres.all,
                        errors = listOf(MovieError.MOVIES_LOAD_FAILED),
                    )
                )
        assertEquals(persistentListOf(MovieError.MOVIES_LOAD_FAILED), result.errors)
      }
    }
  }

  @Nested
  @DisplayName("GIVEN movies are available for display")
  inner class GivenMoviesAreAvailableForDisplay {

    @BeforeEach
    fun setUp() {
      every { movieDomainToPresentationMapper.map(PresentationMocks.Movies.action) } returns
          PresentationMocks.PresentationMovies.action
      every { movieDomainToPresentationMapper.map(PresentationMocks.Movies.comedy) } returns
          PresentationMocks.PresentationMovies.comedy
    }

    @Nested
    @DisplayName("WHEN the user selects an action genre as a filter")
    inner class WhenUserSelectsAnActionGenreAsAFilter {

      @Test
      @DisplayName("THEN the action genre becomes the active filter")
      fun actionGenreBecomesTheActiveFilter() {
        val result =
            TrendingMoviesState()
                .reduceWith(
                    stateReducers.filterByGenre(
                        genreId = PresentationMocks.Movies.ACTION_GENRE_ID,
                        movies = PresentationMocks.Movies.all,
                    )
                )
        assertEquals(PresentationMocks.Movies.ACTION_GENRE_ID, result.selectedGenreId)
      }

      @Test
      @DisplayName("THEN the movie list is refreshed")
      fun movieListIsRefreshed() {
        val result =
            TrendingMoviesState()
                .reduceWith(
                    stateReducers.filterByGenre(
                        genreId = PresentationMocks.Movies.ACTION_GENRE_ID,
                        movies = PresentationMocks.Movies.all,
                    )
                )
        assertEquals(
            persistentListOf(
                PresentationMocks.PresentationMovies.action,
                PresentationMocks.PresentationMovies.comedy,
            ),
            result.movies,
        )
      }
    }

    @Nested
    @DisplayName("WHEN the user clears the active genre filter")
    inner class WhenUserClearsTheActiveGenreFilter {

      @Test
      @DisplayName("THEN no genre filter is active")
      fun noGenreFilterIsActive() {
        val stateWithGenre =
            TrendingMoviesState(selectedGenreId = PresentationMocks.Movies.ACTION_GENRE_ID)
        val result =
            stateWithGenre.reduceWith(
                stateReducers.filterByGenre(genreId = null, movies = PresentationMocks.Movies.all)
            )
        assertNull(result.selectedGenreId)
      }
    }

    @Nested
    @DisplayName("WHEN the user switches the sort option to title")
    inner class WhenUserSwitchesSortOptionToTitle {

      @Test
      @DisplayName("THEN title becomes the active sort option")
      fun titleBecomesTheActiveSortOption() {
        val result =
            TrendingMoviesState()
                .reduceWith(
                    stateReducers.changeSortOption(
                        sortOption = SortOption.TITLE,
                        movies = PresentationMocks.Movies.all,
                    )
                )
        assertEquals(SortOption.TITLE, result.selectedSortOption)
      }

      @Test
      @DisplayName("THEN the movie list is refreshed")
      fun movieListIsRefreshedAfterSortOptionChange() {
        val result =
            TrendingMoviesState()
                .reduceWith(
                    stateReducers.changeSortOption(
                        sortOption = SortOption.TITLE,
                        movies = PresentationMocks.Movies.all,
                    )
                )
        assertEquals(
            persistentListOf(
                PresentationMocks.PresentationMovies.action,
                PresentationMocks.PresentationMovies.comedy,
            ),
            result.movies,
        )
      }
    }

    @Nested
    @DisplayName("WHEN the user sets the sort order to ascending")
    inner class WhenUserSetsSortOrderToAscending {

      @Test
      @DisplayName("THEN ascending becomes the active sort order")
      fun ascendingBecomesTheActiveSortOrder() {
        val result =
            TrendingMoviesState()
                .reduceWith(
                    stateReducers.selectSortOrder(
                        sortOrder = SortOrder.ASC,
                        movies = PresentationMocks.Movies.all,
                    )
                )
        assertEquals(SortOrder.ASC, result.selectedSortOrder)
      }

      @Test
      @DisplayName("THEN the movie list is refreshed")
      fun movieListIsRefreshedAfterSortOrderChange() {
        val result =
            TrendingMoviesState()
                .reduceWith(
                    stateReducers.selectSortOrder(
                        sortOrder = SortOrder.ASC,
                        movies = PresentationMocks.Movies.all,
                    )
                )
        assertEquals(
            persistentListOf(
                PresentationMocks.PresentationMovies.action,
                PresentationMocks.PresentationMovies.comedy,
            ),
            result.movies,
        )
      }
    }
  }

  @Nested
  @DisplayName("GIVEN the screen has a previous load error")
  inner class GivenScreenHasAPreviousLoadError {

    private val stateWithErrors =
        TrendingMoviesState(
            isLoading = false,
            errors = persistentListOf(MovieError.MOVIES_LOAD_FAILED),
        )

    @Nested
    @DisplayName("WHEN movies start reloading")
    inner class WhenMoviesStartReloading {

      @Test
      @DisplayName("THEN the loading indicator appears")
      fun loadingIndicatorAppears() {
        assertTrue(stateWithErrors.reduceWith(stateReducers.loading()).isLoading)
      }

      @Test
      @DisplayName("THEN previous errors are dismissed")
      fun previousErrorsAreDismissed() {
        assertEquals(
            persistentListOf<MovieError>(),
            stateWithErrors.reduceWith(stateReducers.loading()).errors,
        )
      }
    }
  }

  @Nested
  @DisplayName("GIVEN the screen is in a loading state")
  inner class GivenScreenIsInALoadingState {

    private val loadingState = TrendingMoviesState(isLoading = true)

    @Nested
    @DisplayName("WHEN movies fail to load")
    inner class WhenMoviesFailToLoad {

      @Test
      @DisplayName("THEN the loading indicator disappears")
      fun loadingIndicatorDisappears() {
        assertFalse(
            loadingState
                .reduceWith(stateReducers.loadFailed(listOf(MovieError.MOVIES_LOAD_FAILED)))
                .isLoading
        )
      }

      @Test
      @DisplayName("THEN the movies load failure error is shown to the user")
      fun moviesLoadFailureErrorIsShown() {
        assertEquals(
            persistentListOf(MovieError.MOVIES_LOAD_FAILED),
            loadingState
                .reduceWith(stateReducers.loadFailed(listOf(MovieError.MOVIES_LOAD_FAILED)))
                .errors,
        )
      }
    }
  }

  @Nested
  @DisplayName("GIVEN the screen is in its initial state")
  inner class GivenScreenIsInItsInitialState {

    @Nested
    @DisplayName("WHEN the user opens the sort options sheet")
    inner class WhenUserOpensTheSortOptionsSheet {

      @Test
      @DisplayName("THEN the sort sheet becomes visible")
      fun sortSheetBecomesVisible() {
        assertTrue(
            TrendingMoviesState().reduceWith(stateReducers.sortSheetVisible(true)).showSortSheet
        )
      }
    }
  }

  @Nested
  @DisplayName("GIVEN the sort options sheet is open")
  inner class GivenSortOptionsSheetIsOpen {

    @Nested
    @DisplayName("WHEN the user closes the sort options sheet")
    inner class WhenUserClosesTheSortOptionsSheet {

      @Test
      @DisplayName("THEN the sort sheet is dismissed")
      fun sortSheetIsDismissed() {
        val openSheetState = TrendingMoviesState(showSortSheet = true)
        assertFalse(openSheetState.reduceWith(stateReducers.sortSheetVisible(false)).showSortSheet)
      }
    }
  }

  private companion object {
    const val WEEK_LABEL = "Apr 28 – May 4"
  }
}
