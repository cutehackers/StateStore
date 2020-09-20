@file:Suppress("unused", "NOTHING_TO_INLINE")

package app.junhyounglee.logaware

import android.util.Log

/**
 * class Sample : LogAware {
 *   fun example() {
 *     d("Hello World")
 *   }
 * }
 *
 * Result
 *  "Sample: Hello World"
 */
interface LogAware {
    val logTag: String
        get() = determineLogTag(javaClass)
}

private fun determineLogTag(clazz: Class<*>): String = clazz.simpleName.run {
    if (length > 23) {
        substring(0..23)
    } else {
        this
    }
}

fun LogAware(clazz: Class<*>): LogAware = object : LogAware {
    override val logTag: String = determineLogTag(clazz)
}

inline fun <reified T : Any> LogAware(): LogAware = LogAware(T::class.java)

/**
 * Send a log message with the [Log.VERBOSE] severity.
 * Note that the log message will not be written if the current log level is above [Log.VERBOSE].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log.
 * @param thr an exception to log (optional).
 *
 * @see [Log.v].
 */
inline fun LogAware.v(message: String, thr: Throwable? = null) {
    if (Log.isLoggable(logTag, Log.VERBOSE)) {
        Log.v(logTag, message, thr)
    }
}
inline fun LogAware.v(message: Any?, thr: Throwable? = null) {
    if (Log.isLoggable(logTag, Log.VERBOSE)) {
        Log.v(logTag, message?.toString() ?: "null", thr)
    }
}

/**
 * Send a log message with the [Log.VERBOSE] severity.
 * Note that the log message will not be written if the current log level is above [Log.VERBOSE].
 * The default log level is [Log.INFO].
 *
 * @param message the high order function that returns message text to log.
 *
 * @see [Log.v].
 */
inline fun LogAware.v(message: () -> String) {
    if (Log.isLoggable(logTag, Log.VERBOSE)) {
        Log.v(logTag, message())
    }
}

/**
 * Send a log message with the [Log.VERBOSE] severity.
 * Note that the log message will not be written if the current log level is above [Log.VERBOSE].
 * The default log level is [Log.INFO].
 *
 * @param thr an exception to log.
 * @param message the high order function that returns message text to log.
 *
 * @see [Log.v].
 */
inline fun LogAware.v(thr: Throwable, message: () -> String) {
    if (Log.isLoggable(logTag, Log.VERBOSE)) {
        Log.v(logTag, message(), thr)
    }
}

/**
 * Send a log message with the [Log.DEBUG] severity.
 * Note that the log message will not be written if the current log level is above [Log.DEBUG].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log.
 * @param thr an exception to log (optional).
 *
 * @see [Log.d].
 */
inline fun LogAware.d(message: String, thr: Throwable? = null) {
    if (Log.isLoggable(logTag, Log.DEBUG)) {
        Log.d(logTag, message, thr)
    }
}
inline fun LogAware.d(message: Any?, thr: Throwable? = null) {
    if (Log.isLoggable(logTag, Log.DEBUG)) {
        Log.d(logTag, message?.toString() ?: "null", thr)
    }
}

/**
 * Send a log message with the [Log.DEBUG] severity.
 * Note that the log message will not be written if the current log level is above [Log.DEBUG].
 * The default log level is [Log.INFO].
 *
 * @param message the high order function that returns message text to log.
 *
 * @see [Log.d].
 */
inline fun LogAware.d(message: () -> String) {
    if (Log.isLoggable(logTag, Log.DEBUG)) {
        Log.d(logTag, message())
    }
}

/**
 * Send a log message with the [Log.DEBUG] severity.
 * Note that the log message will not be written if the current log level is above [Log.DEBUG].
 * The default log level is [Log.INFO].
 *
 * @param thr an exception to log.
 * @param message the high order function that returns message text to log.
 *
 * @see [Log.d].
 */
inline fun LogAware.d(thr: Throwable, message: () -> String) {
    if (Log.isLoggable(logTag, Log.DEBUG)) {
        Log.d(logTag, message(), thr)
    }
}

/**
 * Send a log message with the [Log.INFO] severity.
 * Note that the log message will not be written if the current log level is above [Log.INFO]
 *   (it is the default level).
 *
 * @param message the message text to log.
 * @param thr an exception to log (optional).
 *
 * @see [Log.i].
 */
inline fun LogAware.i(message: String, thr: Throwable? = null) {
    if (Log.isLoggable(logTag, Log.INFO)) {
        Log.i(logTag, message, thr)
    }
}
inline fun LogAware.i(message: Any?, thr: Throwable? = null) {
    if (Log.isLoggable(logTag, Log.INFO)) {
        Log.i(logTag, message?.toString() ?: "null", thr)
    }
}

/**
 * Send a log message with the [Log.INFO] severity.
 * Note that the log message will not be written if the current log level is above [Log.INFO]
 *   (it is the default level).
 *
 * @param message the high order function that returns message text to log.
 *
 * @see [Log.i].
 */
inline fun LogAware.i(message: () -> String) {
    if (Log.isLoggable(logTag, Log.INFO)) {
        Log.i(logTag, message())
    }
}

/**
 * Send a log message with the [Log.INFO] severity.
 * Note that the log message will not be written if the current log level is above [Log.INFO]
 *   (it is the default level).
 *
 * @param thr an exception to log.
 * @param message the high order function that returns message text to log.
 *
 * @see [Log.i].
 */
inline fun LogAware.i(thr: Throwable, message: () -> String) {
    if (Log.isLoggable(logTag, Log.INFO)) {
        Log.i(logTag, message(), thr)
    }
}

/**
 * Send a log message with the [Log.WARN] severity.
 * Note that the log message will not be written if the current log level is above [Log.WARN].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log.
 * @param thr an exception to log (optional).
 *
 * @see [Log.w].
 */
inline fun LogAware.w(message: String, thr: Throwable? = null) {
    if (Log.isLoggable(logTag, Log.WARN)) {
        Log.w(logTag, message, thr)
    }
}
inline fun LogAware.w(message: Any?, thr: Throwable? = null) {
    if (Log.isLoggable(logTag, Log.WARN)) {
        Log.w(logTag, message?.toString() ?: "null", thr)
    }
}

/**
 * Send a log message with the [Log.WARN] severity.
 * Note that the log message will not be written if the current log level is above [Log.WARN].
 * The default log level is [Log.INFO].
 *
 * @param message the high order function that returns message text to log.
 *
 * @see [Log.w].
 */
inline fun LogAware.w(message: () -> String) {
    if (Log.isLoggable(logTag, Log.WARN)) {
        Log.w(logTag, message())
    }
}

/**
 * Send a log message with the [Log.WARN] severity.
 * Note that the log message will not be written if the current log level is above [Log.WARN].
 * The default log level is [Log.INFO].
 *
 * @param thr an exception to log.
 * @param message the high order function that returns message text to log.
 *
 * @see [Log.w].
 */
inline fun LogAware.w(thr: Throwable, message: () -> String) {
    if (Log.isLoggable(logTag, Log.WARN)) {
        Log.w(logTag, message(), thr)
    }
}

/**
 * Send a log message with the [Log.ERROR] severity.
 * Note that the log message will not be written if the current log level is above [Log.ERROR].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log.
 * @param thr an exception to log (optional).
 *
 * @see [Log.e].
 */
inline fun LogAware.e(message: String, thr: Throwable? = null) {
    if (Log.isLoggable(logTag, Log.ERROR)) {
        Log.e(logTag, message, thr)
    }
}
inline fun LogAware.e(message: Any?, thr: Throwable? = null) {
    if (Log.isLoggable(logTag, Log.ERROR)) {
        Log.e(logTag, message?.toString() ?: "null", thr)
    }
}

/**
 * Send a log message with the [Log.ERROR] severity.
 * Note that the log message will not be written if the current log level is above [Log.ERROR].
 * The default log level is [Log.INFO].
 *
 * @param message the high order function that returns message text to log.
 *
 * @see [Log.e].
 */
inline fun LogAware.e(message: () -> String) {
    if (Log.isLoggable(logTag, Log.ERROR)) {
        Log.e(logTag, message())
    }
}

/**
 * Send a log message with the [Log.ERROR] severity.
 * Note that the log message will not be written if the current log level is above [Log.ERROR].
 * The default log level is [Log.INFO].
 *
 * @param thr an exception to log.
 * @param message the high order function that returns message text to log.
 *
 * @see [Log.e].
 */
inline fun LogAware.e(thr: Throwable, message: () -> String) {
    if (Log.isLoggable(logTag, Log.ERROR)) {
        Log.e(logTag, message(), thr)
    }
}

/**
 * Send a log message with the "What a Terrible Failure" severity.
 * Report an exception that should never happen.
 *
 * @param message the message text to log.
 * @param thr an exception to log (optional).
 *
 * @see [Log.wtf].
 */
inline fun LogAware.wtf(message: String, thr: Throwable? = null) {
    if (Log.isLoggable(logTag, Log.ERROR)) {
        Log.wtf(logTag, message, thr)
    }
}
inline fun LogAware.wtf(message: Any?, thr: Throwable? = null) {
    if (Log.isLoggable(logTag, Log.ERROR)) {
        Log.wtf(logTag, message?.toString() ?: "null", thr)
    }
}

/**
 * Send a log message with the "What a Terrible Failure" severity.
 * Report an exception that should never happen.
 *
 * @param message the high order function that returns message text to log.
 *
 * @see [Log.wtf].
 */
inline fun LogAware.wtf(message: () -> String) {
    if (Log.isLoggable(logTag, Log.ERROR)) {
        Log.wtf(logTag, message())
    }
}

/**
 * Send a log message with the "What a Terrible Failure" severity.
 * Report an exception that should never happen.
 *
 * @param thr an exception to log.
 * @param message the high order function that returns message text to log.
 *
 * @see [Log.wtf].
 */
inline fun LogAware.wtf(thr: Throwable, message: () -> String) {
    if (Log.isLoggable(logTag, Log.ERROR)) {
        Log.wtf(logTag, message(), thr)
    }
}