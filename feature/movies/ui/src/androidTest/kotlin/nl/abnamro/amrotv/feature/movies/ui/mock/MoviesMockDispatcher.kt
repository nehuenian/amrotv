package nl.abnamro.amrotv.feature.movies.ui.mock

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import nl.abnamro.amrotv.feature.movies.ui.E2ETestData

internal class MoviesMockDispatcher : Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path.orEmpty()
        return when {
            path.startsWith("/3/trending/movie/") -> MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(MockFixtures.trendingMovies)
            path.startsWith("/3/genre/movie/list") -> MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(MockFixtures.genres)
            path.startsWith("/3/movie/${E2ETestData.HOPPERS_MOVIE_ID}") -> MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(MockFixtures.hoppersDetail)
            else -> MockResponse().setResponseCode(404)
        }
    }
}
