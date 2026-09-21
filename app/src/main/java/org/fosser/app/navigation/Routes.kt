package org.fosser.app.navigation

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val DETAILS = "details/{packageName}"
    const val SAVED = "saved"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val WEB_VIEW = "webview?url={url}"

    fun details(packageName: String) = "details/$packageName"

    fun webView(url: String) = "webview?url=${Uri.encode(url)}"
}
