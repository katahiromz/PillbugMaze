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
    // デバッグメッセージ出力。
    private fun logD(msg: String?, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.d("MainActivity", msg, tr)
        }
    }

    // トーストを表示。
    @JavascriptInterface
    fun showToast(text: String, isLong: Boolean = false) {
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
            // 「権限が必要」とトーストで表示。
            showToast(getString(R.string.needs_rights))
        }
    }

    //
    // イベント関連。
    //
    override fun onCreate(savedInstanceState: Bundle?) {
        logD("onCreate")

        // スプラッシュ画面を表示して切り替える。
        installSplashScreen()

        // 親に通知。
        super.onCreate(savedInstanceState)
        // ビューを設定。
        setContentView(R.layout.activity_main)
        // 画面上部のアクションバーを非表示にする。
        supportActionBar?.hide()

        // ウェブビュー初期化スレッド開始。
        webViewThread = WebViewThread(this)
        webViewThread?.start()

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

    // 開始。
    override fun onStart() {
        logD("onStart")
        super.onStart()
    }

    // 復帰。
    override fun onResume() {
        logD("onResume")
        super.onResume()
        MainRepository.load(this)
        webView?.onResume()
        chromeClient?.onResume()
    }

    // 一時停止。
    override fun onPause() {
        logD("onPause")
        super.onPause()
        MainRepository.save(this)
        chromeClient?.onPause()
        webView?.onPause()
    }

    // 停止。
    override fun onStop() {
        logD("onStop")
        super.onStop()
        chromeClient?.onPause()
        webView?.onPause()
    }

    // 破棄。
    override fun onDestroy() {
        logD("onDestroy")
        webView?.destroy()
        super.onDestroy()
    }

    //
    // WebView関連。
    //

    private var webView: WebView? = null
    private var chromeClient: MyWebChromeClient? = null
    private var webViewThread: WebViewThread? = null

    // WebViewを初期化する。
    @SuppressLint("JavascriptInterface")
    fun initWebView() {
        // WebViewを取得。
        webView = findViewById(R.id.web_view)
        webView?.post {
            // Web設定を初期化。
            initWebSettings()
        }
        webView?.post {
            // WebViewクライアントを作成。
            webView?.webViewClient = MyWebViewClient(object: MyWebViewClient.Listener {
                // TODO: リスナ関数を追加
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

            // Chromeクライアントを作成。
            chromeClient = MyWebChromeClient(this, object: MyWebChromeClient.Listener {
                // TODO: リスナの関数を追加。
            })
            webView?.webChromeClient = chromeClient

            // Javascriptインターフェースを指定。
            webView?.addJavascriptInterface(this, "AndroidNative")

            // URLを開く。
            webView?.loadUrl(getString(R.string.url))
        }
    }

    // Web設定。
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebSettings() {
        val settings = webView?.settings
        if (settings == null) return
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        val versionName = getVersionName()
        settings.userAgentString += "/AndroidNative/$versionName/"

        if (BuildConfig.DEBUG) {
            // デバッグ時にはキャッシュを無効に。
            settings?.cacheMode = WebSettings.LOAD_NO_CACHE
            // Webデバッグを有効に。
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }

    // バージョン名を取得。
    private fun getVersionName(): String {
        val appName: String = this.packageName
        val pm: PackageManager = this.packageManager
        val pi: PackageInfo = pm.getPackageInfo(appName, PackageManager.GET_META_DATA)
        return pi.versionName
    }

    // WebView初期化スレッド。
    class WebViewThread(private val activity: MainActivity) : Thread() {
        override fun run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE)
            activity.initWebView()
        }
    }
}