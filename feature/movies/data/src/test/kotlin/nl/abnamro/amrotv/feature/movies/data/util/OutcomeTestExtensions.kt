package nl.abnamro.amrotv.feature.movies.data.util

import nl.abnamro.amrotv.core.domain.model.Outcome

internal fun <T> Outcome<T>.requireSuccess(): T = when (this) {
    is Outcome.Success -> data
    is Outcome.Error -> error("Expected Outcome.Success but got Outcome.Error: $cause")
}

internal fun <T> Outcome<T>.requireError(): Outcome.Error<T> = when (this) {
    is Outcome.Error -> this
    is Outcome.Success -> error("Expected Outcome.Error but got Outcome.Success")
}
