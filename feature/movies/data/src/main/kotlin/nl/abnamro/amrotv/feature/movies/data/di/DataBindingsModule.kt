package nl.abnamro.amrotv.feature.movies.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.abnamro.amrotv.feature.movies.data.datasource.local.LocalMovieDataSource
import nl.abnamro.amrotv.feature.movies.data.datasource.local.NoOpLocalMovieDataSource
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.RemoteMovieDataSource
import nl.abnamro.amrotv.feature.movies.data.datasource.remote.tmdb.TmdbMovieDataSource
import nl.abnamro.amrotv.feature.movies.data.repository.MovieRepositoryImpl
import nl.abnamro.amrotv.feature.movies.domain.api.repository.MovieRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataBindingsModule {

    @Binds
    @Singleton
    internal abstract fun bindRemoteMovieDataSource(
        impl: TmdbMovieDataSource,
    ): RemoteMovieDataSource

    @Binds
    @Singleton
    internal abstract fun bindLocalMovieDataSource(
        impl: NoOpLocalMovieDataSource,
    ): LocalMovieDataSource

    @Binds
    @Singleton
    internal abstract fun bindMovieRepository(
        impl: MovieRepositoryImpl,
    ): MovieRepository
}
