package nl.abnamro.amrotv.libraries.logger.api

/**
 * Abstraction for structured app-level logging.
 *
 * Inject [Logger] wherever logging is needed. Never use platform or third-party logging APIs
 * directly — all output is routed through registered log sinks.
 */
interface Logger {

    /**
     * Dispatches a log entry to all registered log sinks.
     *
     * @param level severity; controls filtering and routing in each sink.
     * @param tag identifies the log source (class name or feature area).
     * @param message human-readable description of the event being logged.
     * @param throwable optional exception associated with the entry; null when no exception is
     *   involved.
     */
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null)
}
