package m20.bgm.downloader.v4

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.OrientationEventListener
import android.view.View
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebViewClient
import m20.bgm.downloader.v4.component.UIComponent
import m20.bgm.downloader.v4.databinding.ActivityBrowserBinding
import java.text.SimpleDateFormat
import java.util.Date

class BrowserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBrowserBinding
    private var agentWeb: AgentWeb? = null
    private var currentOrientation = 0
    private var defaultSystemUiVisibility = 0
    private var isAllowRedirect = true
    private var isLibvioCheck = false
    private var isHintedVideoCapture = false
    private val capturedVideoList = mutableListOf<Pair<String, String>>()
    private var videoMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentOrientation = resources.configuration.orientation
        val url = intent.getStringExtra(KEY_URL)
        val title = intent.getStringExtra(KEY_TITLE)
        isLibvioCheck = intent.getBooleanExtra(KEY_LIBVIO_COOKIE_CHECK, false)
        isAllowRedirect = intent.getBooleanExtra(KEY_ALLOW_REDIRECT, true)
        defaultSystemUiVisibility = window.decorView.systemUiVisibility

        supportActionBar?.title = title
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            UIComponent.setMaterial3ActionBar(this@BrowserActivity, this)
        }

        agentWeb = AgentWeb.with(this)
            .setAgentWebParent(binding.webview, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator()
            .setWebViewClient(webViewClient)
            .createAgentWeb()
            .ready()
            .go(url)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                val newOrientation = resources.configuration.orientation
                if (newOrientation != currentOrientation) {
                    currentOrientation = newOrientation
                    if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        setFullScreen()
                    } else {
                        setUnFullScreen()
                    }
                }
            }
        }
        orientationEventListener.enable()

        if (isLibvioCheck) {
            Toast.makeText(this, R.string.cookie_save_hint, Toast.LENGTH_SHORT).show()
        }
    }

    private val webViewClient = object : WebViewClient() {
        override fun shouldInterceptRequest(
            webView: WebView,
            webResourceRequest: WebResourceRequest
        ): WebResourceResponse? {
            val url = webResourceRequest.url.toString()
            return if (shouldInterceptUrl(url)) {
                WebResourceResponse("text/plain", "utf-8", null)
            } else {
                if (isVideoUrl(url) || url.contains("toutiaovod.com")) {
                    capturedVideoList.add(url to getCurrentTime())
                    if (!isHintedVideoCapture) {
                        isHintedVideoCapture = true
                        runOnUiThread {
                            videoMenuItem?.isVisible = true
                            Toast.makeText(
                                this@BrowserActivity,
                                R.string.captured_video_hint,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                super.shouldInterceptRequest(webView, webResourceRequest)
            }
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView,
            webResourceRequest: WebResourceRequest
        ): Boolean {
            return !isAllowRedirect || super.shouldOverrideUrlLoading(webView, webResourceRequest)
        }
    }

    // 屏蔽广告网站
    private val urls = listOf(
        "xn--jvrp4x1zyfta.net",
        "g01.xn--qrq171dxpq.com",
        "4vgyjja.cn:8005",
        "p.sda1.dev"
    )

    private fun shouldInterceptUrl(url: String): Boolean {
        return urls.any { url.contains(it) }
    }

    private fun isVideoUrl(url: String): Boolean {
        val videoExtensions =
            arrayOf(".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".m3u8", ".m3u", ".ts")
        return videoExtensions.any { url.endsWith(it, ignoreCase = true) }
    }

    private fun getCurrentTime(): String {
        val date = Date()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(date)
    }

    override fun onPause() {
        agentWeb?.webLifeCycle?.onPause()
        super.onPause()
    }

    override fun onResume() {
        agentWeb?.webLifeCycle?.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        agentWeb?.webLifeCycle?.onDestroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_browser, menu)
        videoMenuItem = menu.findItem(R.id.item_capture_video)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.item_open -> {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(agentWeb?.webCreator?.webView?.url.toString())
                )
                startActivity(intent)
                true
            }

            R.id.item_capture_video -> {
                val intent = Intent(this, VideoCapturedActivity::class.java)
                val bundle =
                    Bundle().apply { putSerializable(KEY_PAIR_LIST, ArrayList(capturedVideoList)) }
                intent.putExtra(KEY_BUNDLE, bundle)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setFullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.run {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            supportActionBar?.hide()
        }
    }

    private fun setUnFullScreen() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = defaultSystemUiVisibility
        if (!UIComponent.isNightMode(this)) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        supportActionBar?.show()
    }

    companion object {
        const val KEY_URL = "url"
        const val KEY_TITLE = "title"
        const val KEY_LIBVIO_COOKIE_CHECK = "libvio_cookie_check"
        const val KEY_ALLOW_REDIRECT = "allow_redirect"
        const val KEY_PAIR_LIST = "pairList"
        const val KEY_BUNDLE = "bundle"
    }
}
