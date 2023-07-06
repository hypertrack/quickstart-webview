package com.hypertrack.quickstart.android.webview

import android.util.Log
import android.webkit.JavascriptInterface

class JsToJavaInterface {
    @JavascriptInterface
    fun helloWorld() {
        Log.e("quickstart-webview","Hello World!")
    }
}
