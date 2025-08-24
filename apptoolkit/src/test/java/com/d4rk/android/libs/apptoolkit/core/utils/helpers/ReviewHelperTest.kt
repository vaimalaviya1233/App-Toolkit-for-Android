package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.app.Activity
import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewHelperTest {

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `no coroutine launched when sessionCount less than 3`() = runTest {
        mockkObject(ReviewHelper)
        coEvery { ReviewHelper.launchReview(any()) } returns true
        every {
            ReviewHelper.launchInAppReviewIfEligible(
                any(), any(), any(), any(), any()
            )
        } answers { callOriginal() }

        ReviewHelper.launchInAppReviewIfEligible(
            activity = mockk(),
            sessionCount = 2,
            hasPromptedBefore = false,
            scope = this,
            onReviewLaunched = {},
        )

        coVerify(exactly = 0) { ReviewHelper.launchReview(any()) }
    }

    @Test
    fun `no coroutine launched when hasPromptedBefore true`() = runTest {
        mockkObject(ReviewHelper)
        coEvery { ReviewHelper.launchReview(any()) } returns true
        every {
            ReviewHelper.launchInAppReviewIfEligible(
                any(), any(), any(), any(), any()
            )
        } answers { callOriginal() }

        ReviewHelper.launchInAppReviewIfEligible(
            activity = mockk(),
            sessionCount = 3,
            hasPromptedBefore = true,
            scope = this,
            onReviewLaunched = {},
        )

        coVerify(exactly = 0) { ReviewHelper.launchReview(any()) }
    }

    @Test
    fun `onReviewLaunched invoked when launchReview returns true`() = runTest {
        val activity = mockk<Activity>()
        val context = mockk<Context>()
        every { activity.applicationContext } returns context
        mockkObject(ReviewHelper)
        coEvery { ReviewHelper.launchReview(activity) } returns true
        every {
            ReviewHelper.launchInAppReviewIfEligible(
                any(), any(), any(), any(), any()
            )
        } answers { callOriginal() }
        var launched = false

        ReviewHelper.launchInAppReviewIfEligible(
            activity = activity,
            sessionCount = 3,
            hasPromptedBefore = false,
            scope = this,
            onReviewLaunched = { launched = true }
        )
        runCurrent()

        assertTrue(launched)
        coVerify(exactly = 1) { ReviewHelper.launchReview(activity) }
    }

    @Test
    fun `onReviewLaunched not invoked when launchReview returns false`() = runTest {
        val activity = mockk<Activity>()
        val context = mockk<Context>()
        every { activity.applicationContext } returns context
        mockkObject(ReviewHelper)
        coEvery { ReviewHelper.launchReview(activity) } returns false
        every {
            ReviewHelper.launchInAppReviewIfEligible(
                any(), any(), any(), any(), any()
            )
        } answers { callOriginal() }
        var launched = false

        ReviewHelper.launchInAppReviewIfEligible(
            activity = activity,
            sessionCount = 3,
            hasPromptedBefore = false,
            scope = this,
            onReviewLaunched = { launched = true }
        )
        runCurrent()

        assertFalse(launched)
        coVerify(exactly = 1) { ReviewHelper.launchReview(activity) }
    }

    @Test
    fun `launchReview returns true when request and flow succeed`() = runTest {
        val activity = mockk<Activity>()
        val context = mockk<Context>()
        every { activity.applicationContext } returns context
        val reviewManager = mockk<ReviewManager>()
        val reviewInfo = mockk<ReviewInfo>()
        mockkStatic(ReviewManagerFactory::class)
        every { ReviewManagerFactory.create(activity) } returns reviewManager
        every { reviewManager.requestReviewFlow() } returns Tasks.forResult(reviewInfo)
        every { reviewManager.launchReviewFlow(activity, reviewInfo) } returns Tasks.forResult(null)

        val result = ReviewHelper.launchReview(activity)

        assertTrue(result)
    }

    @Test
    fun `launchReview returns false when requestReviewFlow fails`() = runTest {
        val activity = mockk<Activity>()
        val context = mockk<Context>()
        every { activity.applicationContext } returns context
        val reviewManager = mockk<ReviewManager>()
        mockkStatic(ReviewManagerFactory::class)
        every { ReviewManagerFactory.create(activity) } returns reviewManager
        every { reviewManager.requestReviewFlow() } returns Tasks.forException(Exception())

        val result = ReviewHelper.launchReview(activity)

        assertFalse(result)
    }

    @Test
    fun `launchReview returns false when launchReviewFlow fails`() = runTest {
        val activity = mockk<Activity>()
        val context = mockk<Context>()
        every { activity.applicationContext } returns context
        val reviewManager = mockk<ReviewManager>()
        val reviewInfo = mockk<ReviewInfo>()
        mockkStatic(ReviewManagerFactory::class)
        every { ReviewManagerFactory.create(activity) } returns reviewManager
        every { reviewManager.requestReviewFlow() } returns Tasks.forResult(reviewInfo)
        every { reviewManager.launchReviewFlow(activity, reviewInfo) } returns Tasks.forException(Exception())

        val result = ReviewHelper.launchReview(activity)

        assertFalse(result)
    }
}
