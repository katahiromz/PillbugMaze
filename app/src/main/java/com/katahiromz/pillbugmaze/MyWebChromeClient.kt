package com.katahiromz.pillbugmaze

import android.util.Log
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MyWebChromeClient(private val activity: AppCompatActivity, private val listener: Listener) :
    WebChromeClient() {

    interface Listener {
        // TODO: リスナインターフェースを追加
    }

    // アクティビティからリソース文字列を取得。
    private fun getResString(resId: Int): String {
        return activity.getString(resId)
    }

    // 権限を許可する。
    override fun onPermissionRequest(request: PermissionRequest?) {
        request?.grant(request.resources)
    }

    // 表示中のダイアログ。
    private var dialog: MaterialDialog? = null

    fun onPause() {
        // TODO: 一時停止前の処理
    }
    fun onResume() {
        // TODO: 復帰後の処理
        if (dialog != null) {
            dialog?.show()
        }
    }

    // JavaScriptのalert関数をラップする。
    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        // MaterialAlertDialogを使用して実装する。
        val title = getResString(R.string.app_name)
        val ok_text = getResString(R.string.ok)
        MaterialAlertDialogBuilder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(ok_text) { _, _ ->
                result?.confirm()
            }
            .setCancelable(false)
            .show()
        return true
    }

    // JavaScriptのconfirm関数をラップする。
    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        // MaterialAlertDialogを使用して実装する。
        val title = getResString(R.string.app_name)
        val ok_text = getResString(R.string.ok)
        val cancel_text = getResString(R.string.cancel)
        MaterialAlertDialogBuilder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(ok_text) { _, _ ->
                result?.confirm()
            }
            .setNegativeButton(cancel_text) { _, _ ->
                result?.cancel()
            }
            .setCancelable(false)
            .show()
        return true
    }

    // JavaScriptのprompt関数をラップする。
    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        // MaterialDialogを使用して実装する。
        val title = getResString(R.string.app_name)
        val ok_text = getResString(R.string.ok)
        val cancel_text =  getResString(R.string.cancel)
        val hint_text = getResString(R.string.prompt_hint)
        var inputtedText: String? = null
        dialog = MaterialDialog(activity).show {
            title(text = title)
            message(text = message)
            input(hint = hint_text, prefill = defaultValue) { _, text ->
                inputtedText = text.toString()
            }
            positiveButton(text = ok_text) {
                result?.confirm(inputtedText ?: "")
                dialog = null
            }
            negativeButton(text = cancel_text) {
                result?.cancel()
                dialog = null
            }
            cancelable(false)
            cancelOnTouchOutside(false)
            lifecycleOwner(activity)
        }
        return true
    }

    // コンソールのメッセージを処理する。必要なければ消してもよい。
    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        if (consoleMessage != null) {
            val msg = consoleMessage.message()
            if (BuildConfig.DEBUG) {
                val line = consoleMessage.lineNumber()
                val src = consoleMessage.sourceId()
                Log.d("console", "$msg at Line $line of $src")
            }
        }
        return super.onConsoleMessage(consoleMessage)
    }
}
