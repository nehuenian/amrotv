package nl.abnamro.amrotv.libraries.logger.implementation

import javax.inject.Inject
import nl.abnamro.amrotv.libraries.logger.api.LogLevel
import nl.abnamro.amrotv.libraries.logger.api.Logger

internal class CompositeLoggerImpl
@Inject
constructor(private val dataSources: Set<@JvmSuppressWildcards LogDataSource>) : Logger {

    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        dataSources.forEach {
            try {
                it.log(level, tag, message, throwable)
            } catch (_: Exception) {
                // Logging is best-effort; ignore sink failures so other data sources still receive
                // the log
                // entry.
            }
        }
    }
}
