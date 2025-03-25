package com.d4rk.android.libs.apptoolkit.core.utils.constants.links

object AppLinks {

    // Play Store
    const val PLAY_STORE_MAIN = "https://play.google.com/"
    const val PLAY_STORE_APP = "${PLAY_STORE_MAIN}store/apps/details?id="
    const val PLAY_STORE_BETA = "${PLAY_STORE_MAIN}apps/testing/"
    const val PLAY_STORE_APP_REVIEWS_SUFFIX = "&showAllReviews=true"
    const val MARKET_APP_PAGE = "market://details?id="

    // Legal & Policy
    const val PRIVACY_POLICY = "https://sites.google.com/view/d4rk7355608/more/apps/privacy-policy"
    const val TERMS_OF_SERVICE = "https://sites.google.com/view/d4rk7355608/more/apps/terms-of-service"
    const val CODE_OF_CONDUCT = "https://sites.google.com/view/d4rk7355608/more/code-of-conduct"
    const val LEGAL_NOTICES = "https://sites.google.com/view/d4rk7355608/more/apps/legal-notices"
    const val GPL_V3 = "https://www.gnu.org/licenses/gpl-3.0"

    // GitHub
    private const val GITHUB_USER = "D4rK7355608"
    const val GITHUB_BASE = "https://github.com/$GITHUB_USER/"
    const val GITHUB_ISSUES_SUFFIX = "/issues/new"
    const val GITHUB_RAW = "https://raw.githubusercontent.com/$GITHUB_USER"
    fun githubChangelog(packageName : String) : String = "$GITHUB_BASE$packageName/blob/master/CHANGELOG.md"
}