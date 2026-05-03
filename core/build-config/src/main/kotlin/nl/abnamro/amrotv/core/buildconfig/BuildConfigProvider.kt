package nl.abnamro.amrotv.core.buildconfig

/**
 * Typed access to build-time configuration values.
 *
 * Inject this interface instead of reading `BuildConfig` fields directly so that
 * call sites remain testable and the source of configuration can be swapped without
 * touching any consumer module.
 *
 * The `:app` module provides the implementation backed by `BuildConfig`, which is
 * compiled from `amrotv.properties` entries (never committed to source control).
 */
interface BuildConfigProvider {

    /**
     * TMDB v4 Read Access Token used as the Bearer credential on all API requests.
     */
    val tmdbReadAccessToken: String

    /**
     * Derived from the Gradle `debug` build type.
     *
     * Use this flag to gate verbose logging or diagnostic UI.
     */
    val isDebug: Boolean
}
