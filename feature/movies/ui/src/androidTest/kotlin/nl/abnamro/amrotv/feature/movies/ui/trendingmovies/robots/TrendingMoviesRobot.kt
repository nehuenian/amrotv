package nl.abnamro.amrotv.feature.movies.ui.trendingmovies.robots

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import nl.abnamro.amrotv.core.testing.robot.Robot
import nl.abnamro.amrotv.core.testing.robot.RobotActionScope
import nl.abnamro.amrotv.core.testing.robot.RobotVerificationScope
import nl.abnamro.amrotv.feature.movies.ui.E2ETestData
import nl.abnamro.amrotv.feature.movies.ui.trendingmovies.component.MovieCardSemantics
import org.junit.Assert.assertTrue

class TrendingMoviesRobot(
    private val rule: ComposeContentTestRule,
) : Robot<TrendingMoviesRobot.Actions, TrendingMoviesRobot.Verifications> {

    interface Actions : RobotActionScope {
        fun sortByReleaseDateAscending()
        fun onMoviesLoaded(block: MoviesLoadedActions.() -> Unit)
    }

    interface MoviesLoadedActions {
        fun filterByGenre(name: String)
        fun clickFirstFeaturedMovie()
        fun scrollToMovie(title: String)
    }

    interface Verifications : RobotVerificationScope {
        fun movieVisibleNow(title: String)
        fun onMoviesLoaded(block: MoviesLoadedVerifications.() -> Unit)
    }

    interface MoviesLoadedVerifications {
        fun trendingMoviesTitleVisible()
        fun genreFilterAllChipVisible()
        fun featuredBannerVisible()
        fun atLeastOneMovieVisible()
        fun movieVisible(title: String)
        fun movieNotVisible(title: String)
    }

    override fun actionScope(): Actions = ActionsImpl(rule)

    override fun verificationScope(): Verifications = VerificationsImpl(rule)

    private class ActionsImpl(private val rule: ComposeContentTestRule) : Actions {

        override fun sortByReleaseDateAscending() {
            rule.onNode(
                hasText(E2ETestData.SORT_DEFAULT_OPTION, substring = true).and(
                    hasClickAction()
                )
            )
                .performClick()
            rule.waitForSortSheet()
            rule.onNode(hasText(E2ETestData.SORT_ASCENDING)).performClick()
            rule.onNode(hasText(E2ETestData.SORT_RELEASE_DATE)).performClick()
        }

        override fun onMoviesLoaded(block: MoviesLoadedActions.() -> Unit) {
            rule.waitForMovieCards()
            object : MoviesLoadedActions {
                override fun filterByGenre(name: String) {
                    rule.onNode(
                        SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange)
                    ).performScrollToNode(hasText(name))
                    rule.onNode(hasText(name).and(hasClickAction())).performClick()
                }

                override fun clickFirstFeaturedMovie() {
                    rule.onAllNodes(
                        hasContentDescription(
                            E2ETestData.FEATURED_BANNER_CONTENT_DESC_PREFIX,
                            substring = true
                        )
                    ).get(0).performClick()
                }

                override fun scrollToMovie(title: String) {
                    rule.onNode(
                        SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
                    ).performScrollToNode(hasText(title))
                }
            }.block()
        }
    }

    private class VerificationsImpl(private val rule: ComposeContentTestRule) : Verifications {

        override fun movieVisibleNow(title: String) {
            rule.waitUntil(timeoutMillis = 5_000) {
                try {
                    rule.onNodeWithText(title).assertIsDisplayed()
                    true
                } catch (_: AssertionError) {
                    false
                }
            }
        }

        override fun onMoviesLoaded(block: MoviesLoadedVerifications.() -> Unit) {
            rule.waitForMovieCards()
            object : MoviesLoadedVerifications {
                override fun trendingMoviesTitleVisible() {
                    rule.onNodeWithText(E2ETestData.TRENDING_MOVIES_TITLE).assertIsDisplayed()
                }

                override fun genreFilterAllChipVisible() {
                    rule.onNode(hasText(E2ETestData.GENRE_FILTER_ALL).and(hasClickAction()))
                        .assertIsDisplayed()
                }

                override fun featuredBannerVisible() {
                    rule.onNode(
                        hasContentDescription(
                            E2ETestData.FEATURED_BANNER_CONTENT_DESC_PREFIX,
                            substring = true
                        )
                    ).assertIsDisplayed()
                }

                override fun atLeastOneMovieVisible() {
                    val nodes = rule.onAllNodes(hasTestTag(MovieCardSemantics.TEST_TAG))
                        .fetchSemanticsNodes()
                    assertTrue("Expected at least one movie card to be visible", nodes.isNotEmpty())
                }

                override fun movieVisible(title: String) {
                    rule.onNode(
                        SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
                    ).performScrollToNode(hasText(title))
                    rule.onNodeWithText(title).assertIsDisplayed()
                }

                override fun movieNotVisible(title: String) {
                    var nodeFound = false
                    try {
                        rule.onNode(
                            SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
                        ).performScrollToNode(hasText(title))
                        nodeFound = true
                    } catch (_: AssertionError) {
                        // performScrollToNode throws if the node is absent — expected when filtered out
                    }
                    if (nodeFound) throw AssertionError("Movie '$title' should not be in the list, but was found after scrolling")
                }
            }.block()
        }
    }

    companion object {
        private fun ComposeContentTestRule.waitForMovieCards(timeoutMs: Long = 15_000) {
            waitUntil(timeoutMillis = timeoutMs) {
                onAllNodes(hasTestTag(MovieCardSemantics.TEST_TAG)).fetchSemanticsNodes().isNotEmpty()
            }
        }

        private fun ComposeContentTestRule.waitForSortSheet(timeoutMs: Long = 5_000) {
            waitUntil(timeoutMillis = timeoutMs) {
                onAllNodesWithText(E2ETestData.SORT_RELEASE_DATE).fetchSemanticsNodes().isNotEmpty()
            }
        }
    }
}
