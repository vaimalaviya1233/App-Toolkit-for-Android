package com.d4rk.android.libs.apptoolkit.app.help.data

import android.content.Context
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.help.domain.data.model.UiHelpQuestion
import com.d4rk.android.libs.apptoolkit.app.help.domain.repository.HelpRepository
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DefaultHelpRepository(
    private val context: Context,
    private val dispatchers: DispatcherProvider
) : HelpRepository {

    override fun fetchFaq(): Flow<List<UiHelpQuestion>> = flow {
        val faq = listOf(
            R.string.question_1 to R.string.summary_preference_faq_1,
            R.string.question_2 to R.string.summary_preference_faq_2,
            R.string.question_3 to R.string.summary_preference_faq_3,
            R.string.question_4 to R.string.summary_preference_faq_4,
            R.string.question_5 to R.string.summary_preference_faq_5,
            R.string.question_6 to R.string.summary_preference_faq_6,
            R.string.question_7 to R.string.summary_preference_faq_7,
            R.string.question_8 to R.string.summary_preference_faq_8,
            R.string.question_9 to R.string.summary_preference_faq_9
        ).mapIndexed { index, (questionRes, answerRes) ->
            UiHelpQuestion(
                id = index,
                question = context.getString(questionRes),
                answer = context.getString(answerRes),
            )
        }.filter { it.question.isNotBlank() && it.answer.isNotBlank() }
        emit(faq)
    }.flowOn(dispatchers.io)
}