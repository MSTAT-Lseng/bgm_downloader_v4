package m20.bgm.downloader.v4.component

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import m20.bgm.downloader.v4.AnthologyPageActivity
import m20.bgm.downloader.v4.BrowserActivity
import m20.bgm.downloader.v4.R
import m20.bgm.downloader.v4.dashboard.acgrip.AcgRipSearch
import m20.bgm.downloader.v4.dashboard.agefans.AgeFansSearch
import m20.bgm.downloader.v4.dashboard.dmxq.DmxqSearch
import m20.bgm.downloader.v4.dashboard.libvio.LibvioSearch
import m20.bgm.downloader.v4.dashboard.mikan.MikanSearch
import m20.bgm.downloader.v4.dashboard.qimiqmi.QimiqimiSearch
import m20.bgm.downloader.v4.dashboard.ysjdm.YsjdmSearch
import m20.bgm.downloader.v4.manager.SettingsManager
import org.xutils.x
import java.io.UnsupportedEncodingException

object SearchComponent {
    // this class only support context is SearchActivity
    fun loadSearch(
        activity: Activity,
        keyword: String?,
        dashboard: String?,
        settingsManager: SettingsManager?,
        page: Int
    ) {
        val contentLinear = activity.findViewById<LinearLayout>(R.id.content)
        contentLinear.removeAllViews() // 删除原先的搜索结果
        activity.findViewById<View>(R.id.page_linear).visibility = View.GONE // 隐藏页数控件

        // 创建一个新的 ProgressBar
        val progressBar = ProgressBar(activity)

        // 设置 ProgressBar 的布局参数
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER
        layoutParams.topMargin =
            activity.resources.getDimension(R.dimen.margin_50dp).toInt() // 设置上边距为50dp
        layoutParams.bottomMargin =
            activity.resources.getDimension(R.dimen.margin_50dp).toInt() // 设置下边距为50dp
        progressBar.layoutParams = layoutParams

        // 将 ProgressBar 添加到 LinearLayout 中
        contentLinear.addView(progressBar)
        try {
            when (dashboard) {
                "acgrip" -> {
                    AcgRipSearch.search(activity, settingsManager, keyword, page)
                }
                "agefans" -> {
                    AgeFansSearch.search(activity, settingsManager, keyword, page)
                }
                "mikan" -> {
                    MikanSearch.search(activity, keyword)
                }
                "dmxq" -> {
                    DmxqSearch.search(activity, settingsManager, keyword, page)
                }
                "ysjdm" -> {
                    YsjdmSearch.search(activity, settingsManager, keyword, page)
                }
                "libvio" -> {
                    LibvioSearch.search(activity, settingsManager, keyword, page)
                }
                "qimiqimi" -> {
                    QimiqimiSearch.search(activity, settingsManager, keyword, page)
                }
                else -> {
                    Log.e("SearchComponent", "dashboard is null")
                }
            }
        } catch (e: UnsupportedEncodingException) {
            Log.e("UnsupportedEncodingException", e.toString())
        }
    }

    fun loadEmpty(activity: Activity, text: String?) {
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val textView = TextView(activity)
        textView.layoutParams = layoutParams
        textView.text = text
        textView.setPadding(35, 500, 35, 500)
        textView.gravity = Gravity.CENTER
        val linearLayout = activity.findViewById<LinearLayout>(R.id.content)
        linearLayout.removeAllViews()
        linearLayout.addView(textView)
    }

    fun addItem(
        activity: Activity,
        content: LinearLayout,
        settingsManager: SettingsManager?,
        image: String?,
        title: String?,
        subtitle: String?,
        link: String?
    ) {
        val layoutInflater = LayoutInflater.from(activity)
        val view = layoutInflater.inflate(R.layout.dashboard_item, null)
        (view.findViewById<View>(R.id.title) as TextView).text = title // 设置名称
        (view.findViewById<View>(R.id.subtitle) as TextView).text = subtitle // 设置描述
        x.image().bind(view.findViewById(R.id.picture), image) // 设置番剧图像
        view.findViewById<View>(R.id.content).setOnClickListener {
            activity.startActivity(
                Intent(activity, AnthologyPageActivity::class.java)
                    .putExtra("use_dashboard", settingsManager?.getValue("use_dashboard", ""))
                    .putExtra("url", link)
                    .putExtra("title", title)
                    .putExtra("image", image)
                    .putExtra("desc", subtitle)
            )
        }
        // 放置到总容器
        content.addView(view)
    }

    fun addItem(
        activity: Activity?,
        contentLinear: LinearLayout,
        title: String?,
        subtitle: String?,
        onClickListener: View.OnClickListener?
    ) {
        val layoutInflater = LayoutInflater.from(activity)
        val view = layoutInflater.inflate(R.layout.dashboard_simple_item, null)
        (view.findViewById<View>(R.id.title) as TextView).text = title // 设置名称
        (view.findViewById<View>(R.id.subtitle) as TextView).text = subtitle // 设置描述
        view.findViewById<View>(R.id.content).setOnClickListener(onClickListener)
        // 添加到总容器
        contentLinear.addView(view)
    }

    @JvmOverloads
    fun addAnthology(
        activity: Activity,
        content: LinearLayout,
        listItem: List<String?>,
        listChild: List<List<String?>>,
        listLink: List<List<String?>>,
        allowRedirect: Boolean = true
    ) {
        for (i in listItem.indices) {
            val textView = TextView(activity)
            textView.text = listItem[i]
            textView.setTextColor(ContextCompat.getColor(activity, R.color.textcolor))
            textView.textSize = 17f
            textView.setPadding(25, 25, 25, 15)
            content.addView(textView)
            val flexboxLayout = FlexboxLayout(activity)
            flexboxLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            flexboxLayout.flexDirection = FlexDirection.ROW
            flexboxLayout.justifyContent = JustifyContent.FLEX_START
            flexboxLayout.alignItems = AlignItems.CENTER
            flexboxLayout.flexWrap = FlexWrap.WRAP
            flexboxLayout.setPadding(25, 15, 25, 15)
            val typedValue = TypedValue()
            activity.theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                typedValue,
                true
            )
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            for (i2 in listChild[i].indices) {
                layoutParams.setMargins(10, 10, 20, 10)
                val cardView = CardView(activity)
                cardView.layoutParams = layoutParams
                val textView1 = TextView(activity)
                textView1.setPadding(40, 25, 40, 25)
                textView1.setTextColor(ContextCompat.getColor(activity, R.color.summary_textcolor))
                textView1.text = listChild[i][i2]
                textView1.setBackgroundResource(typedValue.resourceId)
                cardView.addView(textView1)
                cardView.setOnClickListener {
                    activity.startActivity(
                        Intent(activity, BrowserActivity::class.java).putExtra(
                            "url",
                            listLink[i][i2]
                        ).putExtra("title", activity.resources.getString(R.string.play_online))
                            .putExtra("allow_redirect", allowRedirect)
                    )
                }
                flexboxLayout.addView(cardView)
            }
            (activity.findViewById<View>(R.id.content) as LinearLayout).addView(flexboxLayout)
        }
    }

    fun showPage(activity: Activity, pageNumber: Int, page: Int): Spinner {
        val pageSpinner = activity.findViewById<Spinner>(R.id.page)
        // 创建页数数据源，例如 1 到总页数的数组
        val pageList: MutableList<Int> = ArrayList()
        for (i in 1..pageNumber) {
            pageList.add(i)
        }
        // 创建适配器并设置数据源
        val pageAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, pageList)
        pageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        pageSpinner.adapter = pageAdapter
        // 设置选中的页数
        pageSpinner.setSelection(page - 1) // 因为数组索引从0开始，所以减去1
        (activity.findViewById<View>(R.id.page_total) as TextView).text =
            "/ " + pageNumber + " " + activity.resources.getString(R.string.page_piker_end)
        activity.findViewById<View>(R.id.page_linear).visibility = View.VISIBLE
        return pageSpinner
    }
}
