package nl.abnamro.amrotv

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import nl.abnamro.amrotv.feature.movies.nav.MoviesNavKey
import nl.abnamro.amrotv.feature.movies.nav.moviesEntry

@Composable
fun AmroNavHost() {
    val backStack = rememberNavBackStack(MoviesNavKey.TrendingMovies)
    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider = entryProvider { moviesEntry(backStack) },
    )
}
