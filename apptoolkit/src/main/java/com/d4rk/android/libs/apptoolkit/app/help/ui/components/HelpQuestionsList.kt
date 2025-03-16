package com.d4rk.android.libs.apptoolkit.app.help.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.d4rk.android.libs.apptoolkit.app.help.domain.model.ui.UiHelpQuestion

@Composable
fun HelpQuestionsList(questions : List<UiHelpQuestion>) {
    val expandedStates : SnapshotStateMap<Int , Boolean> = remember { mutableStateMapOf() }

    Column {
        questions.forEachIndexed { index , question ->
            val isExpanded = expandedStates[index] ?: false
            QuestionCard(title = question.question , summary = question.answer , isExpanded = isExpanded , onToggleExpand = {
                expandedStates[index] = ! isExpanded
            })
        }
    }
}