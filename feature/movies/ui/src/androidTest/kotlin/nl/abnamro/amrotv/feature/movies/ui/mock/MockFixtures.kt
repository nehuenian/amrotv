package nl.abnamro.amrotv.feature.movies.ui.mock

internal object MockFixtures {
    val trendingMovies: String = readJson("mock/trending_movies.json")
    val genres: String = readJson("mock/genres.json")
    val hoppersDetail: String = readJson("mock/movie_detail_hoppers.json")

    private fun readJson(path: String): String =
        requireNotNull(MockFixtures::class.java.getResource("/$path")) {
            "Test fixture not found: /$path — check the file exists under androidTest/resources/"
        }.readText()
}
