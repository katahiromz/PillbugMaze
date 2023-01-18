package com.katahiromz.pillbugmaze

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.katahiromz.pillbugmaze.BuildConfig
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.utils.MDUtil.getStringArray
import com.katahiromz.pillbugmaze.R

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
        if (dialog != null)
            dialog?.show()
    }

    // JavaScriptのalert関数をラップする。
    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        // MaterialDialogを使用して実装する。
        val title = getResString(R.string.app_name)
        dialog = MaterialDialog(activity).show {
            title(text = title)
            message(text = message)
            positiveButton(text = getResString(R.string.ok)) {
                dialog = null
            }
            cancelable(false)
            cancelOnTouchOutside(false)
            lifecycleOwner(activity)
        }
        result?.confirm()
        return true
    }

    // JavaScriptのconfirm関数をラップする。
    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        // MaterialDialogを使用して実装する。
        val title = getResString(R.string.app_name)
        dialog = MaterialDialog(activity).show {
            title(text = title)
            message(text = message)
            positiveButton(text = getResString(R.string.ok)) {
                result?.confirm()
                dialog = null
            }
            negativeButton(text = getResString(R.string.cancel)) {
                result?.cancel()
                dialog = null
            }
            cancelable(false)
            cancelOnTouchOutside(false)
            lifecycleOwner(activity)
        }
        return true
    }

    // JavaScriptのprompt関数をラップする。
    @SuppressLint("CheckResult")
    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        // MaterialDialogを使用して実装する。
        val title = getResString(R.string.app_name)
        var inputtedText: String? = null
        dialog = MaterialDialog(activity).show {
            title(text = title)
            message(text = message)
            input(hint = getResString(R.string.prompt_hint), prefill = defaultValue) { _, text ->
                inputtedText = text.toString()
            }
            positiveButton(text = getResString(R.string.ok)) {
                result?.confirm(inputtedText ?: "")
                dialog = null
            }
            negativeButton(text = getResString(R.string.cancel)) {
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
