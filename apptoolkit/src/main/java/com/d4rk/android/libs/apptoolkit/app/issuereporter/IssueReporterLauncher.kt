package com.heinrichreimersoftware.androidissuereporter

import android.content.Context
import android.content.Intent
import androidx.annotation.StyleRes
import com.heinrichreimersoftware.androidissuereporter.model.github.ExtraInfo
import com.heinrichreimersoftware.androidissuereporter.model.github.GithubTarget

class IssueReporterLauncher private constructor(
    private val targetUsername: String,
    private val targetRepository: String
) {
    @StyleRes
    private var theme: Int = 0
    private var guestToken: String? = null
    private var guestEmailRequired: Boolean = false
    private var minDescriptionLength: Int = 0
    private var extraInfo: ExtraInfo = ExtraInfo()
    private var homeAsUpEnabled: Boolean = true

    fun theme(@StyleRes theme: Int) = apply { this.theme = theme }
    fun guestToken(token: String?) = apply { guestToken = token }
    fun guestEmailRequired(required: Boolean) = apply { guestEmailRequired = required }
    fun minDescriptionLength(length: Int) = apply { minDescriptionLength = length }
    fun putExtraInfo(key: String, value: String) = apply { extraInfo.put(key, value) }
    fun homeAsUpEnabled(enabled: Boolean) = apply { homeAsUpEnabled = enabled }

    fun launch(context: Context) {
        val intent = Intent(context, IssueReporterActivity::class.java).apply {
            putExtra(Activity.EXTRA_TARGET_USERNAME, targetUsername)
            putExtra(Activity.EXTRA_TARGET_REPOSITORY, targetRepository)
            putExtra(Activity.EXTRA_THEME, theme)
            putExtra(Activity.EXTRA_GUEST_TOKEN, guestToken)
            putExtra(Activity.EXTRA_GUEST_EMAIL_REQUIRED, guestEmailRequired)
            putExtra(Activity.EXTRA_MIN_DESCRIPTION_LENGTH, minDescriptionLength)
            putExtra(Activity.EXTRA_EXTRA_INFO, extraInfo.toBundle())
            putExtra(Activity.EXTRA_HOME_AS_UP_ENABLED, homeAsUpEnabled)
        }
        context.startActivity(intent)
    }

    class Activity : IssueReporterActivity() {
        companion object {
            const val EXTRA_TARGET_USERNAME = "IssueReporterLauncher.Activity.EXTRA_TARGET_USERNAME"
            const val EXTRA_TARGET_REPOSITORY = "IssueReporterLauncher.Activity.EXTRA_TARGET_REPOSITORY"
            const val EXTRA_THEME = "IssueReporterLauncher.Activity.EXTRA_THEME"
            const val EXTRA_GUEST_TOKEN = "IssueReporterLauncher.Activity.EXTRA_GUEST_TOKEN"
            const val EXTRA_GUEST_EMAIL_REQUIRED = "IssueReporterLauncher.Activity.EXTRA_GUEST_EMAIL_REQUIRED"
            const val EXTRA_MIN_DESCRIPTION_LENGTH = "IssueReporterLauncher.Activity.EXTRA_MIN_DESCRIPTION_LENGTH"
            const val EXTRA_EXTRA_INFO = "IssueReporterLauncher.Activity.EXTRA_EXTRA_INFO"
            const val EXTRA_HOME_AS_UP_ENABLED = "IssueReporterLauncher.Activity.EXTRA_HOME_AS_UP_ENABLED"
        }
    }

    companion object {
        fun forTarget(username: String, repository: String): IssueReporterLauncher {
            return IssueReporterLauncher(username, repository)
        }

        fun forTarget(target: GithubTarget): IssueReporterLauncher {
            return IssueReporterLauncher(target.username, target.repository)
        }
    }
}
