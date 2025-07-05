import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Test

class TestConsentManagerHelper {
    @Test
    fun `updateConsent passes values to firebase`() {
        val analytics = mockk<FirebaseAnalytics>(relaxed = true)
        mockkObject(Firebase)
        every { Firebase.analytics } returns analytics
        justRun { analytics.setConsent(any()) }

        com.d4rk.android.libs.apptoolkit.core.utils.helpers.ConsentManagerHelper.updateConsent(
            analyticsGranted = true,
            adStorageGranted = false,
            adUserDataGranted = true,
            adPersonalizationGranted = false
        )

        verify {
            analytics.setConsent(match {
                it[FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE] == FirebaseAnalytics.ConsentStatus.GRANTED &&
                it[FirebaseAnalytics.ConsentType.AD_STORAGE] == FirebaseAnalytics.ConsentStatus.DENIED &&
                it[FirebaseAnalytics.ConsentType.AD_USER_DATA] == FirebaseAnalytics.ConsentStatus.GRANTED &&
                it[FirebaseAnalytics.ConsentType.AD_PERSONALIZATION] == FirebaseAnalytics.ConsentStatus.DENIED
            })
        }
    }
}
