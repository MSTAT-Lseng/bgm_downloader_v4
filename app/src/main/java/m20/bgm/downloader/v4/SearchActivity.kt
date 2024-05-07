package m20.bgm.downloader.v4

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dylanc.longan.addNavigationBarHeightToMarginBottom
import com.google.android.material.snackbar.Snackbar
import m20.bgm.downloader.v4.component.SearchComponent
import m20.bgm.downloader.v4.component.UIComponent
import m20.bgm.downloader.v4.manager.FavoritesManager
import m20.bgm.downloader.v4.manager.SettingsManager

class SearchActivity : AppCompatActivity() {
    private lateinit var favoritesManager: FavoritesManager
    private lateinit var settingsManager: SettingsManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        favoritesManager = FavoritesManager(this)
        settingsManager = SettingsManager(this)
        keyword = intent.getStringExtra("keyword")
        val useDashboard = settingsManager?.getValue("use_dashboard", "")

        UIComponent.edgeToEdge(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 启用向上导航按钮
        UIComponent.setMaterial3ActionBar(this, supportActionBar) // 设置 M3 样式标题栏
        findViewById<LinearLayout>(R.id.top_space).minimumHeight = UIComponent.getStatusBarHeight(this) + supportActionBar!!.height;
        findViewById<LinearLayout>(R.id.bottom_space).addNavigationBarHeightToMarginBottom()

        // 获取使用的搜索源，如果未设置提示弹窗设置
        if (useDashboard == "") {
            Toast.makeText(this, R.string.select_dashboard_hint, Toast.LENGTH_SHORT).show()
            SearchComponent.loadEmpty(this, getText(R.string.select_dashboard_hint).toString())
        } else {
            startSearching(this)
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // 已经收藏的条目更改图标
        if (favoritesManager?.isFavoriteKeywordExists(keyword) == true) {
            menu.getItem(menu.size() - 1).setIcon(R.drawable.star_24px_fill)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return when (id) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.item_collect -> {
                val isKeywordCollected = favoritesManager?.isFavoriteKeywordExists(keyword) == true
                val messageRes = if (isKeywordCollected) R.string.cancel_collect_success else R.string.collect_success
                val iconRes = if (isKeywordCollected) R.drawable.star_24px else R.drawable.star_24px_fill

                val message = resources.getText(messageRes)
                Snackbar.make(window.decorView, message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                item.setIcon(iconRes)

                if (isKeywordCollected) favoritesManager?.removeFavoriteKeyword(keyword)
                else favoritesManager?.saveFavoriteKeyword(keyword)
                true
            }
            else -> {
                val dashboardKey = when (id) {
                    R.id.item_agefans -> "agefans"
                    R.id.item_dmxq -> "dmxq"
                    R.id.item_ysjdm -> "ysjdm"
                    R.id.item_libvio -> "libvio"
                    R.id.item_mikan -> "mikan"
                    R.id.item_acgrip -> "acgrip"
                    R.id.item_qimiqimi -> "qimiqimi"
                    else -> null
                }
                dashboardKey?.let {
                    settingsManager?.saveValue("use_dashboard", it)
                    startActivity(Intent(this, SearchActivity::class.java).putExtra("keyword", keyword))
                    finish()
                }
                true
            }
        }
    }

    private fun startSearching(activity: Activity) {
        SearchComponent.loadSearch(
            activity,
            keyword,
            settingsManager?.getValue("use_dashboard", ""),
            settingsManager,
            1
        )
    }

    companion object {
        private var keyword: String? = null
    }
}