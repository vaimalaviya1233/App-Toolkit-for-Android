package com.d4rk.android.libs.apptoolkit.app.help.domain.usecases

import com.d4rk.android.libs.apptoolkit.app.help.domain.data.model.UiHelpQuestion
import com.d4rk.android.libs.apptoolkit.app.help.domain.repository.HelpRepository
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.RepositoryWithoutParam
import kotlinx.coroutines.flow.Flow

class ObserveFaqUseCase(
    private val repository: HelpRepository
) : RepositoryWithoutParam<Flow<List<UiHelpQuestion>>> {
    override suspend operator fun invoke(): Flow<List<UiHelpQuestion>> = repository.observeFaq()
}
