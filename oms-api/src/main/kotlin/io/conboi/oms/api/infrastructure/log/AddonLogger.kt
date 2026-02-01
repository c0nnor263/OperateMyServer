package io.conboi.oms.api.infrastructure.log

/**
 * Logging interface for OMS addons.
 *
 * [AddonLogger] provides a standardized, policy-controlled logging API
 * for addons running within the OMS environment.
 *
 * Implementations are supplied by OMS and may apply logging policies,
 * formatting rules, and output routing based on the active runtime profile.
 */
interface AddonLogger {

    /**
     * Logs an informational message.
     *
     * @param msg the log message template
     * @param args optional message arguments
     */
    fun info(msg: String, vararg args: Any?)

    /**
     * Logs a warning message.
     *
     * @param msg the log message template
     * @param args optional message arguments
     */
    fun warn(msg: String, vararg args: Any?)

    /**
     * Logs an error message.
     *
     * @param msg the log message template
     * @param args optional message arguments
     */
    fun error(msg: String, vararg args: Any?)

    /**
     * Logs a debug-level message.
     *
     * Debug output may be suppressed depending on
     * the active logging policy or runtime profile.
     *
     * @param msg the log message template
     * @param args optional message arguments
     */
    fun debug(msg: String, vararg args: Any?)
}
