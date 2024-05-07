package m20.bgm.downloader.v4.dashboard.agefans

import android.app.Activity
import android.util.Log
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

object AgeFansSearch {
    var domain = "https://www.agedm.org"
    @Throws(UnsupportedEncodingException::class)
    fun search(activity: Activity, settingsManager: SettingsManager?, keyword: String?, page: Int) {

        // 构建带有可变参数的链接，并进行URL编码
        val baseUrl =
            domain + "/search?query=" + URLEncoder.encode(keyword, "UTF-8") + "&page=" + page
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
        val list = data.split("<div class=\"card cata_video_item py-4\">".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        if (list.size < 2) {
            SearchComponent.loadEmpty(
                activity,
                activity.resources.getText(R.string.no_search_results) as String
            )
        } else {
            contentLinear.removeAllViews()
            for (i in 1 until list.size) {
                val document = Jsoup.parse(list[i])
                val title = document.getElementsByClass("card-title")[0].text()
                var subtitle = document.getElementsByClass("card-body")[0].text()
                subtitle =
                    subtitle.replace("资源详情 ", "").replace("在线播放", "").replace("$title ", "")
                var image =
                    list[i].substring(list[i].indexOf("data-original=\"") + "data-original=\"".length)
                image = image.substring(0, image.indexOf("\""))
                val link = document.getElementsByTag("a")[0].attr("href")
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

        // 页数加载
        var _resultsNumber =
            data.substring(data.indexOf("共 <span class=\"text-danger\">") + "共 <span class=\"text-danger\">".length)
        _resultsNumber = _resultsNumber.substring(0, _resultsNumber.indexOf("</span>"))
        val resultsNumber = _resultsNumber.toInt()
        if (resultsNumber > 24) {
            val pageNumber = ceil(resultsNumber.toDouble() / 24).toInt()
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

            Log.e("page_info", "multi page $pageNumber")
        } else {
            Log.e("page_info", "only one page.")
        }
    }
}
