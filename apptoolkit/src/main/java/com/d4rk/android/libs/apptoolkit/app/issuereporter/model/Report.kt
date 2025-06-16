package com.heinrichreimersoftware.androidissuereporter.model

import android.text.TextUtils
import com.heinrichreimersoftware.androidissuereporter.model.github.ExtraInfo

class Report(
    val title: String,
    private val description: String,
    private val deviceInfo: DeviceInfo,
    private val extraInfo: ExtraInfo,
    private val email: String?
) {
    fun getDescription(): String {
        val builder = StringBuilder()
        if (!email.isNullOrEmpty()) {
            builder.append("*Sent by [**")
                .append(email)
                .append("**](mailto:")
                .append(email)
                .append(")*")
                .append(PARAGRAPH_BREAK)
        }
        builder.append("Description:\n")
            .append(HORIZONTAL_RULE)
            .append(PARAGRAPH_BREAK)
            .append(description)
            .append(PARAGRAPH_BREAK)
            .append(deviceInfo.toMarkdown())
            .append(PARAGRAPH_BREAK)
            .append(extraInfo.toMarkdown())
        return builder.toString()
    }

    companion object {
        private const val PARAGRAPH_BREAK = "\n\n"
        private const val HORIZONTAL_RULE = "---"
    }
}
