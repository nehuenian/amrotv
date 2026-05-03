package nl.abnamro.amrotv.core.domain.model

/**
 * Represents the outcome of a data-fetch operation.
 *
 * A [Success] result carries fresh or cached data. An [Error] result signals a failure and may
 * carry stale [Error.data] from a previous successful fetch, allowing the UI to remain functional
 * while surfacing the error to the user.
 */
sealed class Outcome<out T> {

    /**
     * The operation completed successfully.
     *
     * @property data the result value.
     */
    data class Success<out T>(val data: T) : Outcome<T>()

    /**
     * The operation failed.
     *
     * @property cause the exception that caused the failure.
     * @property data stale data from a previous successful fetch, or null if no cache exists yet.
     */
    data class Error<out T>(val cause: Throwable, val data: T? = null) : Outcome<T>()
}
