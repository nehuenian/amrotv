package nl.abnamro.amrotv.di

import javax.inject.Inject
import nl.abnamro.amrotv.BuildConfig
import nl.abnamro.amrotv.core.buildconfig.BuildConfigProvider

class BuildConfigProviderImpl @Inject constructor() : BuildConfigProvider {
    override val tmdbReadAccessToken: String
        get() = BuildConfig.TMDB_READ_ACCESS_TOKEN

    override val isDebug: Boolean
        get() = BuildConfig.DEBUG
}
