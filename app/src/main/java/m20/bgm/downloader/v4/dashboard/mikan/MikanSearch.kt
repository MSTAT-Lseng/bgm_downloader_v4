package m20.bgm.downloader.v4.dashboard.mikan

import android.app.Activity
import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import m20.bgm.downloader.v4.BrowserActivity
import m20.bgm.downloader.v4.R
import m20.bgm.downloader.v4.component.DownloadComponent
import m20.bgm.downloader.v4.component.SearchComponent
import m20.bgm.downloader.v4.component.URLComponent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.xutils.x
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object MikanSearch {
    private const val domain = "https://mikanani.me"

    @Throws(UnsupportedEncodingException::class)
    fun search(activity: Activity, keyword: String?) {
        // 构建带有可变参数的链接，并进行URL编码
        val baseUrl = domain + "/Home/Search?searchstr=" + URLEncoder.encode(keyword, "UTF-8")
        val client = OkHttpClient()
        val request = Request.Builder().url(baseUrl).removeHeader("User-Agent")
            .addHeader("User-Agent", URLComponent.commonUA).build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    SearchComponent.loadEmpty(
                        activity,
                        activity.resources.getText(R.string.load_failed) as String + e
                    )
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val data = response.body()!!.string()
                activity.runOnUiThread { searchTask(activity, data) }
            }
        })
    }

    private fun searchTask(
        activity: Activity,
        data: String,
    ) {
        val contentLinear = activity.findViewById<LinearLayout>(R.id.content)
        if (data.contains("找不到对应结果")) {
            SearchComponent.loadEmpty(
                activity,
                activity.resources.getText(R.string.no_search_results) as String
            )
        } else {
            contentLinear.removeAllViews()
            searchCard(activity, contentLinear, data)
            val itemList = data.split("<tr class=\"js-search-results-row\"".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            for (i in 1 until itemList.size) {
                val item = itemList[i]
                val td = item.split("</td>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val name = Html.fromHtml(
                    item.replace(
                        "(?s).*class=\"magnet-link-wrap\">(.*?)</a>.*".toRegex(),
                        "$1"
                    )
                ).toString()
                val link = item.replace(
                    "(?s).*<a href=\"(.*?)\" target=\"_blank\" class=\"magnet-link-wrap\">.*".toRegex(),
                    "$1"
                )
                val size = td[1].replace("(?s).*<td>(.*?)".toRegex(), "$1")
                val date = td[2].replace("(?s).*<td>(.*?)".toRegex(), "$1")
                val torrent =
                    item.replace("(?s).*<td><a href=\"(.*?)\"><img src=\".*".toRegex(), "$1")
                SearchComponent.addItem(activity, contentLinear, name, "$size    $date") {
                    search(
                        activity,
                        name,
                        link,
                        torrent
                    )
                }
            }
        }
    }

    private fun search(activity: Activity, title: String, itemLink: String, torrentLink: String) {
        MaterialAlertDialogBuilder(
            activity,
            com.google.android.material.R.style.Theme_Material3_DayNight_Dialog
        )
            .setTitle(activity.resources.getText(R.string.select_action))
            .setMessage(activity.resources.getText(R.string.select_action_summary))
            .setNegativeButton(activity.resources.getText(R.string.view_info)) { dialog, _ -> // 用户选择了查看信息
                activity.startActivity(
                    Intent(activity, BrowserActivity::class.java).putExtra(
                        "url",
                        (domain + itemLink).replace(".torrent", "")
                    ).putExtra("title", activity.resources.getString(R.string.mikan_project))
                )
                dialog.dismiss()
            }
            .setPositiveButton(activity.resources.getText(R.string.download_torrent)) { dialog, which ->
                DownloadComponent.downloadFile(
                    activity,
                    domain + torrentLink,
                    "$title.torrent",
                    domain + torrentLink
                )
                val snackbar = Snackbar.make(
                    activity.window.decorView,
                    activity.resources.getString(R.string.torrent_download_is_starting),
                    Snackbar.LENGTH_SHORT
                )
                snackbar.show()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun searchCard(activity: Activity, contentLinear: LinearLayout, data: String) {
        if (data.contains("<ul class=\"list-inline an-ul\" style=\"margin-top:20px;\">")) {
            val content = data.replace(
                "(?s).*<ul class=\"list-inline an-ul\" style=\"margin-top:20px;\">(.*?)</ul>.*".toRegex(),
                "$1"
            )
            val flexboxLayout = FlexboxLayout(activity)
            flexboxLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            flexboxLayout.flexDirection = FlexDirection.ROW
            flexboxLayout.justifyContent = JustifyContent.FLEX_START
            flexboxLayout.alignItems = AlignItems.CENTER
            flexboxLayout.flexWrap = FlexWrap.WRAP
            val animeList =
                content.split("<li>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in 1 until animeList.size) {
                val picture = animeList[i].replace("(?s).*data-src=\"([^\"]*)\".*".toRegex(), "$1")
                val link = animeList[i].replace("(?s).*href=\"([^\"]*)\".*".toRegex(), "$1")
                val title = Html.fromHtml(
                    animeList[i].replace(
                        "(?s).*<div class=\"an-text\" title=\"([^\"]*)\".*".toRegex(),
                        "$1"
                    )
                ).toString()

                // 动态调用布局
                val cardView = LayoutInflater.from(activity)
                    .inflate(R.layout.dashboard_card, flexboxLayout, false)
                // 获取布局中的视图
                val imageView = cardView.findViewById<ImageView>(R.id.image)
                val descriptionTextView = cardView.findViewById<TextView>(R.id.title)
                // 设置图像和描述文本
                x.image().bind(imageView, domain + picture) // 设置番剧图像
                descriptionTextView.text = title
                cardView.findViewById<View>(R.id.content).setOnClickListener {
                    activity.startActivity(
                        Intent(activity, BrowserActivity::class.java).putExtra(
                            "url",
                            domain + link
                        ).putExtra("title", activity.resources.getString(R.string.mikan_project))
                    )
                }
                // 将卡片布局添加到父布局
                flexboxLayout.addView(cardView)
            }
            contentLinear.addView(flexboxLayout)
        }
    }
}
