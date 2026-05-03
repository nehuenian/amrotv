package nl.abnamro.amrotv.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import nl.abnamro.amrotv.core.buildconfig.BuildConfigProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindBuildConfigProvider(impl: BuildConfigProviderImpl): BuildConfigProvider
}
