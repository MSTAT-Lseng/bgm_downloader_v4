package m20.bgm.downloader.v4

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.marginTop
import androidx.palette.graphics.Palette
import com.dylanc.longan.addNavigationBarHeightToMarginBottom
import com.makeramen.roundedimageview.RoundedImageView
import m20.bgm.downloader.v4.component.UIComponent
import m20.bgm.downloader.v4.dashboard.agefans.AgeFansAnthology
import m20.bgm.downloader.v4.dashboard.dmxq.DmxqAnthology
import m20.bgm.downloader.v4.dashboard.libvio.LibvioAnthology
import m20.bgm.downloader.v4.dashboard.qimiqmi.QimiqimiAnthology
import m20.bgm.downloader.v4.dashboard.ysjdm.YsjdmAnthology
import org.xutils.common.Callback
import org.xutils.common.Callback.CacheCallback
import org.xutils.x


class AnthologyPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anthology_page)

        val url = intent.getStringExtra("url") ?: ""
        val title = intent.getStringExtra("title") ?: ""
        val picture = intent.getStringExtra("image") ?: ""
        val description = intent.getStringExtra("desc") ?: ""
        val use_dashboard = intent.getStringExtra("use_dashboard") ?: ""

        findViewById<TextView>(R.id.title).text = title
        findViewById<TextView>(R.id.float_title).text = title
        findViewById<TextView>(R.id.subtitle).text = description
        window?.statusBarColor = ContextCompat.getColor(this, R.color.anthology_background)
        supportActionBar?.hide()

        UIComponent.edgeToEdge(this)
        val statusBarHeight = UIComponent.getStatusBarHeight(this)
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView)
        windowInsetsController?.isAppearanceLightStatusBars = false

        val topSpace1 = findViewById<LinearLayout>(R.id.top_space)
        val topSpace2 = findViewById<LinearLayout>(R.id.top_space_2)
        val contentLinear: LinearLayout = findViewById(R.id.content_container)
        val bottomSpace = findViewById<LinearLayout>(R.id.bottom_space)
        val floatFrame = findViewById<FrameLayout>(R.id.float_frame)

        topSpace1.minimumHeight = statusBarHeight
        topSpace2.minimumHeight = statusBarHeight

        val layoutParams = floatFrame.layoutParams
        layoutParams.height += statusBarHeight
        floatFrame.layoutParams = layoutParams

        val contentLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        contentLayoutParams.setMargins(0, contentLinear.marginTop + statusBarHeight, 0, 0)
        contentLinear.layoutParams = contentLayoutParams
        contentLinear.setPadding(
            contentLinear.paddingLeft,
            contentLinear.paddingTop,
            contentLinear.paddingRight,
            contentLinear.paddingBottom + statusBarHeight
        )

        bottomSpace.addNavigationBarHeightToMarginBottom()

        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.float_back).setOnClickListener { finish() }

        floatFrame.setOnClickListener {}

        val scrollView = findViewById<ScrollView>(R.id.scroll)
        scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            floatFrame.alpha =
                if (scrollY > oldScrollY && scrollY > 400) 1f else if (scrollY < oldScrollY && scrollY < 400) 0f else floatFrame.alpha
        }

        loadPicture(picture)

        when (use_dashboard) {
            "agefans" -> AgeFansAnthology.loadAnthology(this, url)
            "dmxq" -> DmxqAnthology.loadAnthology(this, url)
            "ysjdm" -> YsjdmAnthology.loadAnthology(this, url)
            "libvio" -> LibvioAnthology.loadAnthology(this, url)
            "qimiqimi" -> QimiqimiAnthology.loadAnthology(this, url)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadPicture(picture: String?) {
        x.image().loadDrawable(picture, null, object : CacheCallback<Drawable?> {
            override fun onCache(result: Drawable?): Boolean {
                if (result != null) {
                    drawBackground(result)
                }
                return false
            }

            override fun onSuccess(result: Drawable?) {
                if (result != null) {
                    drawBackground(result)
                }
            }

            override fun onError(ex: Throwable, isOnCallback: Boolean) {}
            override fun onCancelled(cex: Callback.CancelledException) {}
            override fun onFinished() {}
        })
    }

    private fun drawBackground(drawable: Drawable) {
        // 将 Drawable 转换为 Bitmap
        val bitmap = (drawable as BitmapDrawable).bitmap
        // 使用 Palette 从 Bitmap 中提取颜色
        Palette.from(bitmap).generate { palette ->
            // 获取深色主题颜色
            val darkColor = palette!!.getDarkMutedColor(0)
            (findViewById<View>(R.id.picture) as RoundedImageView).setImageDrawable(drawable)
            if (darkColor != 0) {
                (findViewById(R.id.float_background) as View).setBackgroundColor(darkColor)
                (findViewById<View>(R.id.background) as LinearLayout).setBackgroundColor(darkColor)
                window!!.statusBarColor = darkColor // 设置状态栏背景颜色
            }
        }
    }

}