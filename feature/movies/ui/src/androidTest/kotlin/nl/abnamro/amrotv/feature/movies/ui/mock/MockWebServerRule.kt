package nl.abnamro.amrotv.feature.movies.ui.mock

import okhttp3.mockwebserver.MockWebServer
import nl.abnamro.amrotv.feature.movies.ui.di.MockServerHolder
import org.junit.rules.ExternalResource

class MockWebServerRule : ExternalResource() {
    val server = MockWebServer()

    override fun before() {
        server.dispatcher = MoviesMockDispatcher()
        server.start()
        MockServerHolder.url = server.url("/").toString()
    }

    override fun after() {
        server.shutdown()
    }
}
