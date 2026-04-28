package nl.abnamro.amrotv.feature.movies.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.TmdbApiService
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

private const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"

@Module
@InstallIn(SingletonComponent::class)
internal object DataNetworkModule {

    @Provides
    @Singleton
    internal fun provideTmdbApiService(retrofitBuilder: Retrofit.Builder): TmdbApiService =
        retrofitBuilder
            .baseUrl(TMDB_BASE_URL)
            .build()
            .create()
}
