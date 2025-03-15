package com.d4rk.android.libs.apptoolkit.ui.screens.help.domain.usecases

import android.app.Application
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.ui.screens.help.domain.model.ui.UiHelpQuestion
import kotlinx.coroutines.flow.Flow
import com.d4rk.android.libs.apptoolkit.core.utils.extensions.toError
import kotlinx.coroutines.flow.flow

class GetFAQsUseCase(private val application : Application) {
    operator fun invoke() : Flow<DataState<List<UiHelpQuestion> , Errors>> = flow {
        runCatching {
            listOf(
                R.string.question_1 to R.string.summary_preference_faq_1 ,
                R.string.question_2 to R.string.summary_preference_faq_2 ,
                R.string.question_3 to R.string.summary_preference_faq_3 ,
                R.string.question_4 to R.string.summary_preference_faq_4 ,
                R.string.question_5 to R.string.summary_preference_faq_5 ,
                R.string.question_6 to R.string.summary_preference_faq_6 ,
                R.string.question_7 to R.string.summary_preference_faq_7 ,
                R.string.question_8 to R.string.summary_preference_faq_8 ,
                R.string.question_9 to R.string.summary_preference_faq_9
            ).map { (questionRes , summaryRes) ->
                UiHelpQuestion(
                    question = application.getString(questionRes) , answer = application.getString(summaryRes)
                )
            }.filter { faq ->
                faq.question.isNotBlank() && faq.answer.isNotBlank()
            }
        }.onSuccess { faqList ->
            emit(DataState.Success(data = faqList))
        }.onFailure { throwable ->
            emit(DataState.Error(error = throwable.toError(default = Errors.UseCase.FAILED_TO_LOAD_FAQS)))
        }
    }
}