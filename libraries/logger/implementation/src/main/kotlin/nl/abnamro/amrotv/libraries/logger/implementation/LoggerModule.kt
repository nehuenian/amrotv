package nl.abnamro.amrotv.libraries.logger.implementation

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import nl.abnamro.amrotv.libraries.logger.api.Logger
import javax.inject.Singleton

/**
 * Hilt module that wires [Logger] to [CompositeLoggerImpl] and seeds
 * the [LogDataSource] set with [TimberLogDataSource].
 *
 * To add a new log sink (Datadog, Firebase Crashlytics, etc.), contribute
 * another `@Binds @IntoSet` binding here — no changes required at call sites.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LoggerModule {

    @Binds
    @Singleton
    internal abstract fun bindLogger(impl: CompositeLoggerImpl): Logger

    @Binds
    @IntoSet
    internal abstract fun bindTimberLogDataSource(impl: TimberLogDataSource): LogDataSource
}
