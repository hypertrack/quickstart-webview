package com.hypertrack.quickstart.android.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.hypertrack.quickstart.android.webview.sdkwebview.HyperTrackJsApiJava
import com.hypertrack.sdk.HyperTrack

class MainActivity : AppCompatActivity() {

    private var webView: WebView? = null
    private val PUBLISHABLE_KEY =
        "S9SNgYHyxL4abm6oFfGAKvoIGmOeZcqBcvIrBbRZ43dKuPYoJJsHSVEhdqB2VaZ2Yf1NR8l7gVIIQYG-gxkTUg"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)

        webView?.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            webChromeClient = WebChromeClient()
            addJavascriptInterface(
                HyperTrackJsApiJava(
                    HyperTrack.getInstance(PUBLISHABLE_KEY)
                ), HyperTrackJsApiJava.apiName
            )
        }

        if (savedInstanceState == null) {
            webView?.loadUrl("file:///android_asset/index.html")
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        webView?.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView?.restoreState(savedInstanceState)
    }

}
