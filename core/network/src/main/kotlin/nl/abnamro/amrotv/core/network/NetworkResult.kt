package nl.abnamro.amrotv.core.network

/**
 * Represents the result of a network operation.
 *
 * Wrap all Retrofit responses in this sealed hierarchy before returning from a data source.
 *
 * @param T the type of the successful response body.
 */
sealed interface NetworkResult<out T> {

    data class Success<T>(val data: T) : NetworkResult<T>

    data class Error(val code: Int?, val message: String?) : NetworkResult<Nothing>

    data object Loading : NetworkResult<Nothing>
}
