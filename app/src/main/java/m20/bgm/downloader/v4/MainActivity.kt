package m20.bgm.downloader.v4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.umeng.commonsdk.UMConfigure
import m20.bgm.downloader.v4.component.ConfigComponent
import m20.bgm.downloader.v4.component.NoticeComponent
import m20.bgm.downloader.v4.component.SettingsComponent
import m20.bgm.downloader.v4.component.UIComponent
import m20.bgm.downloader.v4.component.UpdateComponent
import m20.bgm.downloader.v4.databinding.ActivityMainBinding
import m20.bgm.downloader.v4.manager.SettingsManager

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val appBarConfiguration = AppBarConfiguration.Builder(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
        ).build()

        val navController = findNavController(this, R.id.nav_host_fragment_activity_main)
        val settingsManager = SettingsManager(this)

        setupActionBarWithNavController(this, navController, appBarConfiguration)
        setupWithNavController(binding!!.navView, navController)
        supportActionBar?.hide() // 隐藏 Action Bar

        // 设置浅色状态栏和状态栏颜色
        UIComponent.setLightStatusBar(this)
        window.apply {
            statusBarColor = getColor(R.color.background_color)
            navigationBarColor = getColor(R.color.bar_background_color)
        }
        // 导航栏颜色设置
        if (!UIComponent.isNightMode(this)) {
            val windowInsetsController =
                ViewCompat.getWindowInsetsController(window.decorView)
            windowInsetsController?.isAppearanceLightNavigationBars = true
            windowInsetsController?.isAppearanceLightStatusBars = true
        }

        // 检查更新
        if (settingsManager.getValue(
                SettingsComponent.SETTINGS_AUTO_CHECK_UPDATE_KEY,
                SettingsComponent.SETTINGS_AUTO_CHECK_UPDATE_DEFAULT_VALUE
            ) == SettingsComponent.SETTINGS_AUTO_CHECK_UPDATE_ENABLED_VALUE
        ) {
            UpdateComponent.checkUpdates(this, false)
        }

        // 加载通知
        NoticeComponent.showNotice(this, settingsManager)

        // 加载友盟统计
        UMConfigure.init(
            this,
            ConfigComponent.UMENG_APP_KEY,
            ConfigComponent.UMENG_CHANNEL,
            UMConfigure.DEVICE_TYPE_PHONE,
            ""
        )
    }
}
