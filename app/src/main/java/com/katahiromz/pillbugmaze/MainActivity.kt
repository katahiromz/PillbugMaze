package com.katahiromz.pillbugmaze

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.snackbar.Snackbar
import java.util.*
import timber.log.Timber

/////////////////////////////////////////////////////////////////////
// Constants

// Toast types (for showToast)
const val SHORT_TOAST = 0
const val LONG_TOAST = 1

// Snack types (for showSnackbar)
const val SHORT_SNACK = 0
const val LONG_SNACK = 1
const val ACTION_SNACK_OK = 2
// TODO: Add more snack

class MainActivity : AppCompatActivity(), ValueCallback<String>,
        ActivityCompat.OnRequestPermissionsResultCallback {
    /////////////////////////////////////////////////////////////////////
    // Common
    /////////////////////////////////////////////////////////////////////

    // Use Timber for debugging log
    fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    // Display Toast (a messaging control)
    @JavascriptInterface
    fun showToast(text: String, typeOfToast: Int) {
        when (typeOfToast) {
            SHORT_TOAST -> {
                lastToast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
                lastToast?.show()
            }
            LONG_TOAST -> {
                lastToast = Toast.makeText(this, text, Toast.LENGTH_LONG)
                lastToast?.show()
            }
            else -> {
                require(false, { "typeOfToast: $typeOfToast" })
            }
        }
    }

    var lastToast: Toast? = null

    // Cancel Toast
    @JavascriptInterface
    fun cancelToast() {
        if (lastToast != null) {
            lastToast?.cancel()
            lastToast = null
        }
    }

    // Display Snackbar (another messaging control)
    @JavascriptInterface
    fun showSnackbar(text: String, typeOfSnack: Int) {
        val view = findViewById<View>(android.R.id.content)
        when (typeOfSnack) {
            SHORT_SNACK -> {
                lastSnackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
                lastSnackbar?.show()
            }
            LONG_SNACK -> {
                lastSnackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                lastSnackbar?.show()
            }
            ACTION_SNACK_OK -> {
                lastSnackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
                val buttonText = getString(R.string.ok)
                lastSnackbar?.setAction(buttonText) {
                    // TODO: Add action
                }
                lastSnackbar?.show()
            }
            // TODO: Add more Snack
            else -> {
                require(false, { "typeOfSnack: $typeOfSnack" })
            }
        }
    }

    var lastSnackbar: Snackbar? = null

    @JavascriptInterface
    fun cancelSnackbar() {
        if (lastSnackbar != null) {
            lastSnackbar?.dismiss()
            lastSnackbar = null
        }
    }

    /////////////////////////////////////////////////////////////////////
    // Permissions-related

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var grantedAll = true
        // Audio request
        if (requestCode == MY_WEBVIEW_REQUEST_CODE_01) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    grantedAll = false
                }
            }
            if (grantedAll) {
                // Permission has been granted.
                // TODO: Do something
                //webView?.reload()
            } else {
                // Permission request was denied.
                showSnackbar(getString(R.string.no_permissions), ACTION_SNACK_OK)
            }
        }
        // TODO: Add more request
    }

    /////////////////////////////////////////////////////////////////////
    // event handlers

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.i("onCreate")
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        // Initialize WebView
        initWebView()

        // Initialize Timber
        initTimber()
    }

    override fun onStart() {
        Timber.i("onStart")
        super.onStart()
    }

    override fun onResume() {
        Timber.i("onResume")
        super.onResume()
        webView?.onResume()
        chromeClient?.onResume()
    }

    override fun onPause() {
        Timber.i("onPause")
        super.onPause()
        webView?.onPause()
    }

    override fun onStop() {
        Timber.i("onStop")
        super.onStop()
        webView?.onPause()
    }

    override fun onDestroy() {
        Timber.i("onDestroy")
        webView?.destroy()
        super.onDestroy()
    }

    // ValueCallback<String>
    override fun onReceiveValue(value: String) {
        resultString = value
    }

    private var resultString = ""

    /////////////////////////////////////////////////////////////////////
    // WebView-related

    private var webView: WebView? = null
    private var chromeClient: MyWebChromeClient? = null

    private fun initWebView() {
        webView = findViewById(R.id.web_view)
        webView?.post {
            initWebSettings()
        }
        webView?.post {
            initWebViewClient()
        }
        webView?.post {
            initChromeClient()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebSettings() {
        webView?.setBackgroundColor(0)
        val settings = webView?.settings
        if (settings == null)
            return
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        if (BuildConfig.DEBUG) {
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            WebView.setWebContentsDebuggingEnabled(true)
        }
        val versionName = getVersionName()
        settings.userAgentString += "/KraKra-native-app/$versionName/"
    }

    private fun initWebViewClient() {
        webView?.webViewClient = MyWebViewClient(object : MyWebViewClient.Listener {
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?,
                                         error: WebResourceError?) {
                Timber.i("onReceivedError")
            }

            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?,
                                             errorResponse: WebResourceResponse?) {
                Timber.i("onReceivedHttpError")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Timber.i("onPageFinished")
            }
        })
    }

    private fun initChromeClient() {
        chromeClient = MyWebChromeClient(this, object : MyWebChromeClient.Listener {
            override fun onChromePermissionRequest(permissions: Array<String>, requestCode: Int) {
                requestPermissions(permissions, requestCode)
            }

            override fun onShowToast(text: String, typeOfToast: Int) {
                showToast(text, typeOfToast)
            }

            override fun onShowSnackbar(text: String, typeOfSnack: Int) {
                showSnackbar(text, typeOfSnack)
            }

            override fun onChromeProgressChanged(view: WebView?, newProgress: Int) {
                val bar: ProgressBar = findViewById(R.id.progressBar)
                bar.progress = newProgress
                if (newProgress == 100) {
                    bar.visibility = View.INVISIBLE
                }
            }
        })
        webView?.webChromeClient = chromeClient
        webView?.addJavascriptInterface(this, "AndroidNative")
        webView?.loadUrl(getString(R.string.url))
    }

    private fun getVersionName(): String {
        val appName: String = this.packageName
        val pm: PackageManager = this.packageManager
        val pi: PackageInfo = pm.getPackageInfo(appName, PackageManager.GET_META_DATA)
        return pi.versionName
    }
}