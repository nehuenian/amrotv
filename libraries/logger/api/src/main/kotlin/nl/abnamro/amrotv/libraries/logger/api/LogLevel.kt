package nl.abnamro.amrotv.libraries.logger.api

/**
 * Severity levels used by [Logger.log] to control filtering and routing of log entries.
 */
enum class LogLevel {
    /** Fine-grained diagnostic information, useful during development. */
    DEBUG,

    /** General operational events confirming the application is working as expected. */
    INFO,

    /** Potentially harmful situations that do not stop execution. */
    WARN,

    /** Error events that may still allow the application to continue running. */
    ERROR,
}
