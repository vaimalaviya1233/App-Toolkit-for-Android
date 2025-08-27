package com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.usecases

import com.d4rk.android.libs.apptoolkit.app.issuereporter.data.IssueReporterRepository
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.IssueReportResult
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.Report
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.github.GithubTarget
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.Repository
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.AppDispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

class SendIssueReportUseCase(
    private val repository: IssueReporterRepository,
    private val dispatchers: AppDispatchers,
) : Repository<SendIssueReportUseCase.Params, Result<IssueReportResult>> {

    data class Params(
        val report: Report,
        val target: GithubTarget,
        val token: String?
    )

    override suspend fun invoke(param: Params): Result<IssueReportResult> =
        withContext(dispatchers.io) {
            try {
                Result.success(repository.sendReport(param.report, param.target, param.token))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
}
