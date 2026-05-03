package nl.abnamro.amrotv.feature.movies.ui.di

internal object MockServerHolder {
    @Volatile
    var url: String = "http://localhost:1/"
}
