package m20.bgm.downloader.v4.dashboard.dmxq

import android.app.Activity
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
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
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import kotlin.math.ceil

object DmxqSearch {
    var domain = "https://dmflm.com"

    @Throws(UnsupportedEncodingException::class)
    fun search(activity: Activity, settingsManager: SettingsManager?, keyword: String?, page: Int) {
        // 构建带有可变参数的链接，并进行URL编码
        val baseUrl = "$domain/vodsearch/" + URLEncoder.encode(
            keyword,
            "UTF-8"
        ) + "----------" + page + "---.html"
        val client = OkHttpClient()
        val request = Request.Builder().url(baseUrl).addHeader("Cookie", "searchneed=ok")
            .removeHeader("User-Agent").addHeader("User-Agent", URLComponent.commonUA).build()
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
        if (data.contains("没有找到与")) {
            SearchComponent.loadEmpty(
                activity,
                activity.resources.getText(R.string.no_search_results) as String
            )
        } else if (data.contains("请不要频繁操作，搜索时间间隔为3秒前")) {
            SearchComponent.loadEmpty(
                activity,
                activity.resources.getText(R.string.search_interval_too_short) as String
            )
        } else {
            contentLinear.removeAllViews()
            val list = data.split("<div class=\"module-card-item module-item\">".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            for (i in 1 until list.size) {
                val document = Jsoup.parse(list[i])
                val title = document.getElementsByTag("strong")[0].text()
                var subtitle = document.getElementsByClass("module-info-item-content")[0].text()
                subtitle += document.getElementsByClass("module-info-item-content")[1].text()
                var image =
                    list[i].substring(list[i].indexOf("data-original=\"") + "data-original=\"".length)
                image = image.substring(0, image.indexOf("\""))
                val link = domain + document.getElementsByTag("a")[0].attr("href")
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
        }
        var _resultsNumber =
            data.substring(data.indexOf("$('.mac_total').html('") + "$('.mac_total').html('".length)
        _resultsNumber = _resultsNumber.substring(0, _resultsNumber.indexOf("'"))
        val resultsNumber = _resultsNumber.toInt()
        if (resultsNumber > 16) {
            val pageNumber = ceil(resultsNumber.toDouble() / 16).toInt()
            val pageSpinner = SearchComponent.showPage(activity, pageNumber, page)
            pageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // 获取用户选择的新页数
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

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }
    }
}
