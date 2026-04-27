package nl.abnamro.amrotv.libraries.logger.implementation

import nl.abnamro.amrotv.libraries.logger.api.LogLevel

internal interface LogDataSource {

    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?)
}
