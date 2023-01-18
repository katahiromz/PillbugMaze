package com.katahiromz.pillbugmaze

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

class MainRepository {

    // アプリ設定を保持するオブジェクト。
    companion object {
        // 設定ファイル名。
        private const val MainPrefFileKey = "MAIN_PREF_FILE"
        // データバージョン。
        private const val DATA_VERSION = 1 // TODO: 必要ならデータバージョンを加算する

        // TODO: 設定項目を追加
        var myFloat: Float = 0.0f
        var myInt: Int = 0

        // 共有設定を取得する。
        private fun getPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(MainPrefFileKey, Context.MODE_PRIVATE)
        }

        // 設定を読み込む。
        fun load(context: Context) {
            val prefs = getPreferences(context)
            // TODO: データバージョンのチェック。
            val data_version:Int = prefs.getInt("data_version", 0)
            // TODO: 設定項目を読み込む
            myFloat = prefs.getFloat("myFloat", 0.0f)
            myInt = prefs.getInt("myInt", 0)
        }

        // 設定を保存する。
        fun save(context: Context) {
            // TODO: 設定項目を書き込む
            getPreferences(context)
                    .edit()
                    .putInt("data_version", DATA_VERSION)
                    .putInt("myInt", myInt)
                    .putFloat("myFloat", myFloat)
                    .apply()
        }
   }
}