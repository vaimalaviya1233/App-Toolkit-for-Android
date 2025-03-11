package com.d4rk.android.libs.apptoolkit.data.interfaces

interface ErrorReporter {
    /**
     * Records a given exception with an optional custom message.
     */
    fun recordException(throwable: Throwable, message: String? = null)

    /**
     * Optional: if you want more flexible usage, you can define more
     * methods, e.g. setting custom keys, logging events, etc.
     */
    fun setCustomKey(key: String, value: String)
}