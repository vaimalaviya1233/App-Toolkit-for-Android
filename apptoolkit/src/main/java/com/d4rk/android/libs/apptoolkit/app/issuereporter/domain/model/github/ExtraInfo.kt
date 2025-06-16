package com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.github

import android.os.Bundle

class ExtraInfo {
    private val extraInfo: MutableMap<String, String> = LinkedHashMap()

    fun put(key: String, value: String) {
        extraInfo[key] = value
    }

    fun put(key: String, value: Boolean) { // FIXME: Function "put" is never used
        extraInfo[key] = value.toString()
    }

    fun put(key: String, value: Double) { // FIXME: Function "put" is never used
        extraInfo[key] = value.toString()
    }

    fun put(key: String, value: Float) { // FIXME: Function "put" is never used
        extraInfo[key] = value.toString()
    }

    fun put(key: String, value: Long) { // FIXME: Function "put" is never used
        extraInfo[key] = value.toString()
    }

    fun put(key: String, value: Int) { // FIXME: Function "put" is never used
        extraInfo[key] = value.toString()
    }

    fun put(key: String, value: Any?) { // FIXME: Function "put" is never used
        extraInfo[key] = value.toString()
    }

    fun putAll(extraInfo: ExtraInfo) { // FIXME: Function "putAll" is never used
        this.extraInfo.putAll(extraInfo.extraInfo)
    }

    fun remove(key: String) { // FIXME: Function "remove" is never used
        extraInfo.remove(key)
    }

    fun isEmpty(): Boolean = extraInfo.isEmpty() // FIXME: Function "isEmpty" is never used

    fun toMarkdown(): String {
        if (extraInfo.isEmpty()) return ""
        val output = StringBuilder()
        output.append(
            "Extra info:\n" +
                "---\n" +
                "<table>\n"
        )
        for (key in extraInfo.keys) {
            output.append("<tr><td>")
                .append(key)
                .append("</td><td>")
                .append(extraInfo[key])
                .append("</td></tr>\n")
        }
        output.append("</table>\n")
        return output.toString()
    }

    fun toBundle(): Bundle { // FIXME: Function "toBundle" is never used
        val bundle = Bundle(extraInfo.size)
        for (key in extraInfo.keys) {
            bundle.putString(key, extraInfo[key])
        }
        return bundle
    }

    companion object {
        fun fromBundle(bundle: Bundle?): ExtraInfo { // FIXME: Function "fromBundle" is never used
            val extraInfo = ExtraInfo()
            if (bundle == null || bundle.isEmpty) return extraInfo
            for (key in bundle.keySet()) {
                extraInfo.put(key, bundle.getString(key) ?: "")
            }
            return extraInfo
        }
    }
}
