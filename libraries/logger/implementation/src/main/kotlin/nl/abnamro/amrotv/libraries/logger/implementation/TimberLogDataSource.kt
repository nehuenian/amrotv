package nl.abnamro.amrotv.libraries.logger.implementation

import nl.abnamro.amrotv.libraries.logger.api.LogLevel
import timber.log.Timber
import javax.inject.Inject

internal class TimberLogDataSource @Inject constructor() : LogDataSource {

    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val tree = Timber.tag(tag)
        when (level) {
            LogLevel.DEBUG -> tree.d(throwable, message)
            LogLevel.INFO -> tree.i(throwable, message)
            LogLevel.WARN -> tree.w(throwable, message)
            LogLevel.ERROR -> tree.e(throwable, message)
        }
    }
}
