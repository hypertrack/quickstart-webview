package com.hypertrack.quickstart.android.webview

import android.location.Location
import android.webkit.JavascriptInterface
import com.hypertrack.sdk.HyperTrack

object HyperTrackJsApi {

    const val apiName = "HyperTrack"
    private val hyperTrack by lazy {
        HyperTrack.getInstance("PasteYourPublishableKeyHere")
    }

    @JavascriptInterface
    fun getDeviceId(): String {
        return hyperTrack.deviceID
    }

    @JavascriptInterface
    fun setIsTracking(isTracking: Boolean) {
        if (isTracking) {
            hyperTrack.start()
        } else {
            hyperTrack.stop()
        }
    }

}
