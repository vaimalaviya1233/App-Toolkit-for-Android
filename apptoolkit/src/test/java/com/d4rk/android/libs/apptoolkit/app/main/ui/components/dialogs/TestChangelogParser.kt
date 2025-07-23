package com.d4rk.android.libs.apptoolkit.app.main.ui.components.dialogs

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class TestChangelogParser {
    @Test
    fun `extractChangesForVersion returns section`() {
        val markdown = File("CHANGELOG.md").readText()
        val clazz = Class.forName("com.d4rk.android.libs.apptoolkit.app.main.ui.components.dialogs.ChangelogDialogKt")
        val method = clazz.getDeclaredMethod("extractChangesForVersion", String::class.java, String::class.java)
        method.isAccessible = true
        val section = method.invoke(null, markdown, "1.0.9") as String
        assertTrue(section.startsWith("# Version"))
        assertTrue(section.contains("1.0.9"))
    }
}
