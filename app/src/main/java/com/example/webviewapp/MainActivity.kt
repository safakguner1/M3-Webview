package com.example.webviewapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var sharedPreferences: SharedPreferences
    private var volumeClickCount = 0
    private val handler = Handler(Looper.getMainLooper())
    private val resetCounterRunnable = Runnable { volumeClickCount = 0 }

    private val PREFS_NAME = "WebViewConfig"
    private val KEY_URL = "saved_url"
    private val DEFAULT_URL = "http://10.11.0.141:8080/CaniasWebClient/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setupWebView()
        loadUrl()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        
        // Force Mobile User Agent
        val mobileUserAgent = "Mozilla/5.0 (Linux; Android 11; M3 SL20) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.210 Mobile Safari/537.36"
        settings.userAgentString = mobileUserAgent

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }
        }
    }

    private fun loadUrl() {
        val savedUrl = sharedPreferences.getString(KEY_URL, DEFAULT_URL)
        if (savedUrl != null) {
            webView.loadUrl(savedUrl)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            volumeClickCount++
            
            // 5 saniye içinde basılmazsa sayacı sıfırla
            handler.removeCallbacks(resetCounterRunnable)
            handler.postDelayed(resetCounterRunnable, 5000)

            if (volumeClickCount >= 10) {
                volumeClickCount = 0
                showSettingsDialog()
            }
            return true // Ses açma işlevini engelle (opsiyonel)
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("URL Ayarlarını Düzenle")
        
        val input = EditText(this)
        input.setText(sharedPreferences.getString(KEY_URL, DEFAULT_URL))
        builder.setView(input)

        builder.setPositiveButton("Kaydet") { dialog, _ ->
            val newUrl = input.text.toString()
            if (newUrl.isNotEmpty()) {
                sharedPreferences.edit().putString(KEY_URL, newUrl).apply()
                webView.loadUrl(newUrl)
                Toast.makeText(this, "URL Kaydedildi", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        
        builder.setNegativeButton("İptal") { dialog, _ -> 
            dialog.cancel() 
        }

        builder.show()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
