import android.app.Activity
import android.content.Context
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.data.core.ads.AdsCoreManager
import com.google.android.gms.ads.MobileAds
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Test

class TestAdsCoreManager {
    @Test
    fun `initializeAds triggers MobileAds`() {
        val context = mockk<Context>()
        val provider = mockk<BuildInfoProvider>()
        val manager = AdsCoreManager(context, provider)

        mockkStatic(MobileAds::class)
        justRun { MobileAds.initialize(context) }

        manager.initializeAds("id")
        verify { MobileAds.initialize(context) }
    }

    @Test
    fun `showAdIfAvailable before init does nothing`() {
        val context = mockk<Context>()
        val provider = mockk<BuildInfoProvider>()
        val manager = AdsCoreManager(context, provider)
        val activity = mockk<Activity>()

        manager.showAdIfAvailable(activity)
    }
}
