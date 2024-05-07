package m20.bgm.downloader.v4.dashboard.acgrip

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import m20.bgm.downloader.v4.BrowserActivity
import m20.bgm.downloader.v4.R
import m20.bgm.downloader.v4.component.DownloadComponent
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

object AcgRipSearch {
    private const val domain = "https://acgrip.art"

    @Throws(UnsupportedEncodingException::class)
    fun search(activity: Activity, settingsManager: SettingsManager?, keyword: String?, page: Int) {
        // 构建带有可变参数的链接，并进行URL编码
        val baseUrl = domain + "/page/" + page + "?term=" + URLEncoder.encode(keyword, "UTF-8")
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
        val list = data.split("<td class=\"date hidden-xs hidden-sm\">".toRegex())
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
                val group =
                    list[i].replace("(?s).*<a href=\"/user/\\d+\">(.*?)</a>.*".toRegex(), "$1")
                val days =
                    list[i].replace("(?s).*<time datetime=\".*?\">(.*?)</time>.*".toRegex(), "$1")
                val title = list[i].replace("(?s).*<a href=\"/t/\\d+\">(.*?)</a>.*".toRegex(), "$1")
                val size = list[i].replace("(?s).*<td class=\"size\">(.*?)</td>.*".toRegex(), "$1")
                val link = list[i].replace("(?s).*<a href=\"(.*?)\".*".toRegex(), "$1")
                SearchComponent.addItem(
                    activity,
                    contentLinear,
                    title,
                    group + activity.resources.getString(R.string.published_on_start) + days + activity.resources.getString(
                        R.string.published_on_end
                    ) + size
                ) { search(activity, title, link) }
            }
        }
        if (data.contains("上一页")) {
            var _pageList = data.substring(data.indexOf("<ul class=\"pagination pull-right\">"))
            _pageList =
                _pageList.substring(0, _pageList.indexOf("</li></ul>") + "</li></ul>".length)
            val pageList =
                _pageList.split("<li".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val doc = Jsoup.parse("<li" + pageList[pageList.size - 2])
            val pageNumber = doc.text().toInt()
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

    private fun search(activity: Activity, title: String, link: String) {
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
                        (domain + link).replace(".torrent", "")
                    ).putExtra("title", activity.resources.getString(R.string.acgrip))
                )
                dialog.dismiss()
            }
            .setPositiveButton(activity.resources.getText(R.string.download_torrent)) { dialog, _ ->
                DownloadComponent.downloadFile(
                    activity,
                    domain + link,
                    "$title.torrent",
                    domain + link
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
}
