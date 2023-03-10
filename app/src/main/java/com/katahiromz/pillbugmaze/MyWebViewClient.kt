package com.katahiromz.pillbugmaze

import android.webkit.*

class MyWebViewClient(val listener: Listener) : WebViewClient() {
    interface Listener {
        // TODO: リスナインターフェイスを追加
        fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?)
        fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?,
                                errorResponse: WebResourceResponse?)
        fun onPageFinished(view: WebView?, url: String?)
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        // TODO: URLをオーバーライドすべきならtrueを返す
        return false
    }

    //
    // イベント処理。
    // TODO: 必要なら処理を追加する。
    //
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?,
                                 error: WebResourceError?)
    {
        super.onReceivedError(view, request, error)
        listener.onReceivedError(view, request, error)
    }

    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?,
                                     errorResponse: WebResourceResponse?)
    {
        super.onReceivedHttpError(view, request, errorResponse)
        listener.onReceivedHttpError(view, request, errorResponse)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        listener.onPageFinished(view, url)
    }
}