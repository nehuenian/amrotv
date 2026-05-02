package nl.abnamro.amrotv.feature.movies.presentation.implementation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import nl.abnamro.amrotv.feature.movies.presentation.implementation.trendingmovies.WeekRangeLabelProvider
import nl.abnamro.amrotv.feature.movies.presentation.implementation.trendingmovies.WeekRangeLabelProviderImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class PresentationModule {

    @Binds
    @Singleton
    abstract fun bindWeekRangeLabelProvider(
        impl: WeekRangeLabelProviderImpl
    ): WeekRangeLabelProvider
}
