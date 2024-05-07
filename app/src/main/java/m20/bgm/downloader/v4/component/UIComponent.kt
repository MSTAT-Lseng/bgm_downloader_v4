package m20.bgm.downloader.v4.component

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import com.google.android.material.elevation.SurfaceColors
import m20.bgm.downloader.v4.R

object UIComponent {
    private fun isSystemNightMode(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val nightModeFlags =
                context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        } else {
            val nightModeFlags =
                context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        }
    }

    fun isNightMode(context: Context): Boolean {
        return isSystemNightMode(context)
    }

    fun setLightStatusBar(activity: Activity) {
        if (!isNightMode(activity)) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    fun setMaterial3ActionBar(activity: Activity, actionBar: ActionBar?) {
        setLightStatusBar(activity) // 设置浅色状态栏
        // 设置 StatusBar 颜色
        activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.bar_background_color)
        // 设置 ActionBar 颜色
        actionBar!!.setBackgroundDrawable(ColorDrawable(activity.getColor(R.color.bar_background_color)))
        actionBar.elevation = 0f // 去除导航栏阴影
    }

    fun edgeToEdge(componentActivity: ComponentActivity) {
        val surfaceColor = SurfaceColors.SURFACE_0.getColor(componentActivity)
        val navigationBarColor = if (isEdgeToEdgeEnabled(componentActivity) == 2) {
            Color.TRANSPARENT
        } else {
            Color.TRANSPARENT
            surfaceColor.addAlpha(0xbf)
        }
        val navigationBarStyle = if (isNightMode(componentActivity)) {
            SystemBarStyle.dark(navigationBarColor)
        } else {
            SystemBarStyle.light(navigationBarColor, surfaceColor)
        }
        componentActivity.enableEdgeToEdge(navigationBarStyle = navigationBarStyle)
    }

    @SuppressLint("DiscouragedApi")
    fun isEdgeToEdgeEnabled(context: Context): Int {
        val resources = context.resources
        val resourceId =
            resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
        return if (resourceId > 0) {
            resources.getInteger(resourceId)
        } else {
            0
        }
    }

    private fun Int.addAlpha(alpha: Int): Int {
        return Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))
    }

    fun getStatusBarHeight(activity: Activity): Int {
        var result = 0
        val resourceId: Int = activity.getResources().getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId)
        }
        return result
    }


}
