package com.d4rk.android.libs.apptoolkit.app.help.domain.usecases

import android.app.Application
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.help.domain.model.ui.UiHelpQuestion
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.RepositoryWithoutParam
import com.d4rk.android.libs.apptoolkit.core.utils.extensions.toError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetFAQsUseCase(private val application : Application) : RepositoryWithoutParam<Flow<DataState<List<UiHelpQuestion> , Errors>>> {

    override suspend operator fun invoke() : Flow<DataState<List<UiHelpQuestion> , Errors>> = flow {
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
            ).map { (questionRes : Int , summaryRes : Int) ->
                UiHelpQuestion(
                    question = application.getString(questionRes) , answer = application.getString(summaryRes)
                )
            }.filter { faq : UiHelpQuestion ->
                faq.question.isNotBlank() && faq.answer.isNotBlank()
            }
        }.onSuccess { faqList : List<UiHelpQuestion> ->
            emit(value = DataState.Success(data = faqList))
        }.onFailure { throwable : Throwable ->
            emit(value = DataState.Error(error = throwable.toError(default = Errors.UseCase.FAILED_TO_LOAD_FAQS)))
        }
    }
}