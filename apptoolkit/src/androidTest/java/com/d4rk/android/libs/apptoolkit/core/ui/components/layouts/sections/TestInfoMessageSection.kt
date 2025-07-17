package com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.sections

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test

class TestInfoMessageSection {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun learnMoreInline_displayedInline() {
        composeTestRule.setContent {
            InfoMessageSection(
                message = "Message",
                learnMoreText = "Learn more",
                learnMoreUrl = "https://example.com",
                newLine = false
            )
        }

        composeTestRule.onNodeWithText("Message").assertIsDisplayed()
        composeTestRule.onNodeWithText("Learn more").assertIsDisplayed()
    }

    @Test
    fun learnMoreNewLine_displayedOnNewLine() {
        composeTestRule.setContent {
            InfoMessageSection(
                message = "Message",
                learnMoreText = "Learn more",
                learnMoreUrl = "https://example.com",
                newLine = true
            )
        }

        composeTestRule.onNodeWithText("Message").assertIsDisplayed()
        composeTestRule.onNodeWithText("Learn more").assertIsDisplayed()
    }
}
