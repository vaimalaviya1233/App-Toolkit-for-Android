package com.d4rk.android.apps.apptoolkit.core.utils.constants.ads

import com.d4rk.android.apps.apptoolkit.BuildConfig
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ads.DebugAdsConstants

object AdsConstants {

    val APPS_LIST_BANNER_AD_UNIT_ID : String
        get() = if (BuildConfig.DEBUG) {
            DebugAdsConstants.BANNER_AD_UNIT_ID
        }
        else {
            "ca-app-pub-5294151573817700/7520919879"
        }

    val BOTTOM_NAV_BAR_FULL_BANNER_AD_UNIT_ID : String
        get() = if (BuildConfig.DEBUG) {
            DebugAdsConstants.BANNER_AD_UNIT_ID
        }
        else {
            "ca-app-pub-5294151573817700/6815073092"
        }

    val HELP_LARGE_BANNER_AD_UNIT_ID : String
        get() = if (BuildConfig.DEBUG) {
            DebugAdsConstants.BANNER_AD_UNIT_ID
        }
        else {
            "ca-app-pub-5294151573817700/4464279910"
        }

    val NO_DATA_MEDIUM_RECTANGLE_BANNER_AD_UNIT_ID : String
        get() = if (BuildConfig.DEBUG) {
            DebugAdsConstants.BANNER_AD_UNIT_ID
        }
        else {
            "ca-app-pub-5294151573817700/2564874881"
        }


    val SUPPORT_MEDIUM_RECTANGLE_BANNER_AD_UNIT_ID : String
        get() = if (BuildConfig.DEBUG) {
            DebugAdsConstants.BANNER_AD_UNIT_ID
        }
        else {
            "ca-app-pub-5294151573817700/8295675725"
        }

    val APP_OPEN_UNIT_ID : String
        get() = if (BuildConfig.DEBUG) {
            DebugAdsConstants.APP_OPEN_AD_UNIT_ID
        }
        else {
            "ca-app-pub-5294151573817700/8339177528"
        }

    val NATIVE_AD_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) {
            DebugAdsConstants.NATIVE_AD_UNIT_ID
        } else {
            "ca-app-pub-5294151573817700/5578142927"
        }
}