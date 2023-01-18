package com.katahiromz.pillbugmaze

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import java.util.*
import com.katahiromz.pillbugmaze.BuildConfig
import com.katahiromz.pillbugmaze.R

class MainActivity : AppCompatActivity() {

    private var webView: WebView? = null
    private var chromeClient: MyWebChromeClient? = null
    private var webViewThread: WebViewThread? = null

    private fun logD(msg: String?, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.d("MainActivity", msg, tr)
        }
    }

    //
    // トースト。
    //
    private fun showToast(text: String, isLong: Boolean = false) {
        if (isLong) {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
    }

    //
    // 権限関連。
    //
    companion object {
        const val REQUEST_PERMISSIONS_VALUE = 1
    }
    private var PERMISSIONS = arrayOf(
        // TODO: 必要な権限を追加して下さい
        Manifest.permission.VIBRATE,
        //Manifest.permission.WRITE_EXTERNAL_STORAGE,
        //Manifest.permission.CAMERA
    )
    private val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
    ) {
        isGranted: Map<String?, Boolean?> ->
        if (isGranted.containsValue(false)) {
            showToast(getString(R.string.needs_rights))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        logD("onCreate")
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        if (false) {
            initWebView()
        } else {
            webViewThread = WebViewThread(this)
            webViewThread?.start()
        }
        // 権限を要求。
        for (perm in PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) {
                // granted
            } else {
                // need to request
                requestPermissionLauncher.launch(PERMISSIONS)
            }
        }
    }

    override fun onStart() {
        logD("onStart")
        super.onStart()
    }

    override fun onResume() {
        logD("onResume")
        super.onResume()
        webView?.onResume()
        chromeClient?.onResume()
    }

    override fun onPause() {
        logD("onPause")
        super.onPause()
        webView?.onPause()
    }

    override fun onStop() {
        logD("onStop")
        super.onStop()
        webView?.onPause()
    }

    override fun onDestroy() {
        logD("onDestroy")
        webView?.destroy()
        super.onDestroy()
    }

    fun initWebView() {
        webView = findViewById(R.id.web_view)
        webView?.post {
            webView?.setBackgroundColor(0)
            initWebSettings()
        }
        webView?.post {
            webView?.webViewClient = MyWebViewClient(object: MyWebViewClient.Listener {
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?,
                                             error: WebResourceError?)
                {
                    logD("onReceivedError")
                }

                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?,
                                                 errorResponse: WebResourceResponse?)
                {
                    logD("onReceivedHttpError")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    logD("onPageFinished")
                }
            })

            chromeClient = MyWebChromeClient(this, object: MyWebChromeClient.Listener {
            })
            webView?.webChromeClient = chromeClient
            webView?.addJavascriptInterface(chromeClient!!, "android")
            webView?.loadUrl(getString(R.string.url))
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebSettings() {
        val settings = webView?.settings
        settings?.javaScriptEnabled = true
        settings?.domStorageEnabled = true
        settings?.mediaPlaybackRequiresUserGesture = false
        if (BuildConfig.DEBUG) {
            settings?.cacheMode = WebSettings.LOAD_NO_CACHE
            WebView.setWebContentsDebuggingEnabled(true)
        }
        if (settings != null) {
            val versionName = getVersionName()
            updateUserAgent(settings, versionName)
        }
    }

    private fun updateUserAgent(settings: WebSettings, versionName: String) {
        var userAgent: String? = settings.userAgentString
        if (userAgent != null) {
            userAgent += "/Pillbug-native-app/$versionName/"
            settings.userAgentString = userAgent
        }
    }

    private fun getVersionName(): String {
        val appName: String = this.packageName
        val pm: PackageManager = this.packageManager
        val pi: PackageInfo = pm.getPackageInfo(appName, PackageManager.GET_META_DATA)
        return pi.versionName
    }

    class WebViewThread(private val activity: MainActivity) : Thread() {
        override fun run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE)
            activity.initWebView()
        }
    }
}