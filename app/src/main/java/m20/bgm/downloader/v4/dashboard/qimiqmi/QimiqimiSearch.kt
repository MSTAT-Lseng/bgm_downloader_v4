package m20.bgm.downloader.v4.dashboard.qimiqmi

import android.app.Activity
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Toast
import m20.bgm.downloader.v4.R
import m20.bgm.downloader.v4.component.SearchComponent
import m20.bgm.downloader.v4.component.URLComponent
import m20.bgm.downloader.v4.manager.SettingsManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import java.net.URLEncoder
import kotlin.math.ceil

object QimiqimiSearch {
    var domain = "http://www.qimiqimi.net"

    fun search(activity: Activity, settingsManager: SettingsManager?, keyword: String?, page: Int) {
        val baseUrl = domain + "/vod/search/wd/" + URLEncoder.encode(
            keyword,
            "UTF-8"
        ) + "/page/" + page + ".html"
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
                activity.runOnUiThread { search(activity, settingsManager, data, keyword, page) }
            }
        })
    }

    private fun search(
        activity: Activity,
        settingsManager: SettingsManager?,
        data: String,
        keyword: String?,
        page: Int
    ) {
        val contentLinear = activity.findViewById<LinearLayout>(R.id.content)
        when {
            data.contains("<div class=\"text\">请不要频繁操作，搜索时间间隔为3秒</div>") ->
                SearchComponent.loadEmpty(
                    activity,
                    activity.resources.getText(R.string.search_interval_too_short) as String
                )

            data.contains("<h1>没有找到匹配数据</h1>") ->
                SearchComponent.loadEmpty(
                    activity,
                    activity.resources.getText(R.string.no_search_results) as String
                )

            else -> {
                contentLinear.removeAllViews()
                val list =
                    data.split("<div class=\"play-txt\">".toRegex()).filter { it.isNotEmpty() }
                list.drop(1).forEachIndexed { index, item ->
                    val parseItem = Jsoup.parse(item)
                    val title = parseItem.getElementsByTag("h2")[0].text()
                    val image = Jsoup.parse(data)
                        .getElementsByClass("play-img")[index].getElementsByTag("img")[0].attr("src")
                    val link = domain + parseItem.getElementsByTag("a")[0].attr("href")
                    val subtitle = parseItem.getElementsByTag("dl").joinToString("\n") { element ->
                        val dtText = element.getElementsByTag("dt").firstOrNull()?.text() ?: ""
                        val ddText = element.getElementsByTag("dd").firstOrNull()?.text() ?: ""
                        dtText + ddText
                    }

                    SearchComponent.addItem(
                        activity,
                        contentLinear,
                        settingsManager,
                        image,
                        title,
                        subtitle,
                        link
                    )
                }

                // 页数检查
                checkPage(data, page, activity, keyword, settingsManager)

            }
        }
    }

    private fun checkPage(
        data: String,
        page: Int,
        activity: Activity,
        keyword: String?,
        settingsManager: SettingsManager?
    ) {
        val macTotalStart = "\$('.mac_total').html('"
        val macTotalEnd = "');"
        val macTotal = data.substringAfter(macTotalStart).substringBefore(macTotalEnd)

        val totalPage = ceil(macTotal.toDouble() / 20).toInt()
        if (totalPage > 1) {
            val pageSpinner = SearchComponent.showPage(activity, totalPage, page)
            pageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedPage = parent?.getItemAtPosition(position) as Int
                    if (selectedPage != page) {
                        SearchComponent.loadSearch(
                            activity,
                            keyword,
                            settingsManager?.getValue("use_dashboard", ""),
                            settingsManager,
                            selectedPage
                        )
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

}
