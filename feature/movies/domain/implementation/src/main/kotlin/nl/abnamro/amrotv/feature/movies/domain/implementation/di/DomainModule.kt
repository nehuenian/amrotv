package nl.abnamro.amrotv.feature.movies.domain.implementation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.FilterAndSortMoviesUseCase
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetGenresUseCase
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetMovieDetailUseCase
import nl.abnamro.amrotv.feature.movies.domain.api.usecase.GetTrendingMoviesUseCase
import nl.abnamro.amrotv.feature.movies.domain.implementation.usecase.FilterAndSortMoviesUseCaseImpl
import nl.abnamro.amrotv.feature.movies.domain.implementation.usecase.GetGenresUseCaseImpl
import nl.abnamro.amrotv.feature.movies.domain.implementation.usecase.GetMovieDetailUseCaseImpl
import nl.abnamro.amrotv.feature.movies.domain.implementation.usecase.GetTrendingMoviesUseCaseImpl

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class DomainModule {

    @Binds
    @ViewModelScoped
    internal abstract fun bindGetTrendingMoviesUseCase(
        impl: GetTrendingMoviesUseCaseImpl,
    ): GetTrendingMoviesUseCase

    @Binds
    @ViewModelScoped
    internal abstract fun bindGetMovieDetailUseCase(
        impl: GetMovieDetailUseCaseImpl,
    ): GetMovieDetailUseCase

    @Binds
    @ViewModelScoped
    internal abstract fun bindGetGenresUseCase(
        impl: GetGenresUseCaseImpl,
    ): GetGenresUseCase

    @Binds
    @ViewModelScoped
    internal abstract fun bindFilterAndSortMoviesUseCase(
        impl: FilterAndSortMoviesUseCaseImpl,
    ): FilterAndSortMoviesUseCase
}
