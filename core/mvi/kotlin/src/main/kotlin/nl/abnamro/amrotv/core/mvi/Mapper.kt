package nl.abnamro.amrotv.core.mvi

/**
 * Base contract for all single-input, single-output mappers in the application.
 *
 * Every mapper class in the project must implement this interface to ensure a uniform mapping API.
 * Do not use extension functions for mapping — create an injectable class that implements [Mapper]
 * instead.
 *
 * @param I the input type to map from.
 * @param O the output type to map to.
 */
interface Mapper<I, O> {

    /**
     * Maps [input] to an instance of [O].
     *
     * @param input the object to transform.
     * @return the mapped output.
     */
    fun map(input: I): O
}
