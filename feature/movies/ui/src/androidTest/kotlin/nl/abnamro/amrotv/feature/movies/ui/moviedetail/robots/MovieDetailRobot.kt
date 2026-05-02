package nl.abnamro.amrotv.feature.movies.ui.moviedetail.robots

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import nl.abnamro.amrotv.core.testing.robot.Robot
import nl.abnamro.amrotv.core.testing.robot.RobotActionScope
import nl.abnamro.amrotv.core.testing.robot.RobotVerificationScope
import nl.abnamro.amrotv.feature.movies.ui.E2ETestData

class MovieDetailRobot(
    private val rule: ComposeContentTestRule,
) : Robot<MovieDetailRobot.Actions, MovieDetailRobot.Verifications> {

    interface Actions : RobotActionScope {
        fun onDetailLoaded(block: DetailLoadedActions.() -> Unit)
    }

    interface DetailLoadedActions {
        fun tapBack()
    }

    interface Verifications : RobotVerificationScope {
        fun loadingVisible()
        fun errorVisible()
        fun onDetailLoaded(block: DetailLoadedVerifications.() -> Unit)
    }

    interface DetailLoadedVerifications {
        fun backButtonVisible()
        fun ratingLabelVisible()
        fun titleVisible(title: String)
        fun imdbLinkVisible()
        fun overviewVisible()
        fun statusLabelVisible()
    }

    override fun actionScope(): Actions = ActionsImpl(rule)

    override fun verificationScope(): Verifications = VerificationsImpl(rule)

    private class ActionsImpl(private val rule: ComposeContentTestRule) : Actions {

        override fun onDetailLoaded(block: DetailLoadedActions.() -> Unit) {
            rule.waitForDetailContent()
            object : DetailLoadedActions {
                override fun tapBack() {
                    rule.onNodeWithContentDescription(E2ETestData.NAVIGATE_BACK_DESCRIPTION)
                        .performClick()
                }
            }.block()
        }
    }

    private class VerificationsImpl(private val rule: ComposeContentTestRule) : Verifications {

        override fun loadingVisible() {
            rule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
                .assertIsDisplayed()
        }

        override fun errorVisible() {
            rule.onNodeWithContentDescription("Error").assertIsDisplayed()
        }

        override fun onDetailLoaded(block: DetailLoadedVerifications.() -> Unit) {
            rule.waitForDetailContent()
            object : DetailLoadedVerifications {
                override fun backButtonVisible() {
                    rule.onNodeWithContentDescription(E2ETestData.NAVIGATE_BACK_DESCRIPTION)
                        .assertIsDisplayed()
                }

                override fun ratingLabelVisible() {
                    rule.onNode(hasText(E2ETestData.RATING_LABEL, substring = true))
                        .assertIsDisplayed()
                }

                override fun titleVisible(title: String) {
                    rule.onNodeWithText(title).assertIsDisplayed()
                }

                override fun imdbLinkVisible() {
                    rule.onNodeWithText(E2ETestData.VIEW_ON_IMDB_LABEL).assertIsDisplayed()
                }

                override fun overviewVisible() {
                    rule.onNodeWithContentDescription(E2ETestData.MOVIE_OVERVIEW_CONTENT_DESC)
                        .assertExists()
                }

                override fun statusLabelVisible() {
                    rule.onNode(hasText(E2ETestData.STATUS_LABEL, substring = true))
                        .assertExists()
                }
            }.block()
        }
    }

    companion object {
        private fun ComposeContentTestRule.waitForDetailContent(timeoutMs: Long = 15_000) {
            waitUntil(timeoutMillis = timeoutMs) {
                onAllNodes(
                    hasContentDescription(E2ETestData.MOVIE_OVERVIEW_CONTENT_DESC)
                ).fetchSemanticsNodes().isNotEmpty()
            }
        }
    }
}
