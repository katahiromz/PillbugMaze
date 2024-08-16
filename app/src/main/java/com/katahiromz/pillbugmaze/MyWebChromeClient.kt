package com.katahiromz.pillbugmaze

import android.text.InputType
import android.webkit.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber

const val MY_WEBVIEW_REQUEST_CODE_01 = 999
// TODO: Add more request

class MyWebChromeClient(private val activity: AppCompatActivity, private val listener: Listener) :
    WebChromeClient() {

    interface Listener {
        fun onChromePermissionRequest(permissions: Array<String>, requestCode: Int)
        fun onShowToast(text: String, typeOfToast: Int)
        fun onShowSnackbar(text: String, typeOfSnack: Int)
        fun onChromeProgressChanged(view: WebView?, newProgress: Int)
    }

    private fun getResString(resId: Int): String {
        return activity.getString(resId)
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        listener.onChromeProgressChanged(view, newProgress)
    }

    /////////////////////////////////////////////////////////////////////
    // Permissions-related

    override fun onPermissionRequest(request: PermissionRequest?) {
        // TODO: Add more request
        super.onPermissionRequest(request)
    }

    /////////////////////////////////////////////////////////////////////
    // JavaScript interface-related

    // Wrap JavaScript alert function
    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        // MaterialAlertDialogを使用して実装する。
        val title = getResString(R.string.app_name)
        val ok_text = getResString(R.string.ok)
        modalDialog = MaterialAlertDialogBuilder(activity!!)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(ok_text) { _, _ ->
                result?.confirm()
                modalDialog = null
            }
            .setCancelable(false)
            .create()
        modalDialog?.show()
        return true
    }

    // Wrap JavaScript confirm function
    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        // MaterialAlertDialogを使用して実装する。
        val title = getResString(R.string.app_name)
        val okText = getResString(R.string.ok)
        val cancelText = getResString(R.string.cancel)
        modalDialog = MaterialAlertDialogBuilder(activity!!)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(okText) { _, _ ->
                result?.confirm()
                modalDialog = null
            }
            .setNegativeButton(cancelText) { _, _ ->
                result?.cancel()
                modalDialog = null
            }
            .setCancelable(false)
            .create()
        modalDialog?.show()
        return true
    }

    private var modalDialog: AlertDialog? = null
    fun onResume() {
        if (modalDialog != null)
            modalDialog?.show()
    }

    // Wrap JavaScript prompt function
    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        val title = getResString(R.string.app_name)
        val okText = getResString(R.string.ok)
        val cancelText = getResString(R.string.cancel)
        val input = EditText(activity!!)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(if (defaultValue != null) defaultValue else "")
        modalDialog = MaterialAlertDialogBuilder(activity!!)
            .setTitle(title)
            .setMessage(message)
            .setView(input)
            .setPositiveButton(okText) { _, _ ->
                result?.confirm(input.text.toString())
                modalDialog = null
            }
            .setNegativeButton(cancelText) { _, _ ->
                result?.cancel()
                modalDialog = null
            }
            .setCancelable(false)
            .create()
        modalDialog?.show()
        return true
    }

    // JavaScriptのコンソール出力を制御する。
    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        if (BuildConfig.DEBUG) {
            if (consoleMessage != null) {
                val msg = consoleMessage.message()
                val line = consoleMessage.lineNumber()
                val src = consoleMessage.sourceId()
                Timber.d("console: $msg at Line $line of $src")
            }
        }
        return super.onConsoleMessage(consoleMessage)
    }
}
