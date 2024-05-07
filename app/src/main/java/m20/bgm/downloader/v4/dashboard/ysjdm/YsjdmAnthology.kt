package m20.bgm.downloader.v4.dashboard.ysjdm

import android.app.Activity
import android.view.View
import android.widget.Toast
import m20.bgm.downloader.v4.R
import m20.bgm.downloader.v4.component.SearchComponent
import m20.bgm.downloader.v4.component.URLComponent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.regex.Pattern

object YsjdmAnthology {
    fun loadAnthology(activity: Activity, url: String?) {
        val client = OkHttpClient()
        val request = url?.let {
            Request.Builder().url(it).removeHeader("User-Agent")
                .addHeader("User-Agent", URLComponent.commonUA).build()
        }
        val call = request?.let { client.newCall(it) }
        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        activity.resources.getText(R.string.load_failed) as String + e,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val data = response.body()!!.string()
                activity.runOnUiThread { anthology(activity, data) }
            }
        })
    }

    private fun anthology(activity: Activity, data: String) {
        activity.findViewById<View>(R.id.progress).visibility = View.GONE

        // 创建一个List来存储文本
        val ListItem: MutableList<String?> = ArrayList()
        ListItem.add("超清原画质播放+下载 文明弹幕 谢谢合作")
        val ListChild: MutableList<List<String?>> = ArrayList()
        val ListLink: MutableList<List<String?>> = ArrayList()
        var epList =
            data.substring(data.indexOf("<ul class=\"content_playlist list_scroll clearfix\">"))
        epList = epList.substring(0, epList.indexOf("</ul>"))
        val childList =
            epList.split("<ul class=\"content_playlist list_scroll clearfix\">".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
        for (i in 1 until childList.size) {
            val episodeNames: MutableList<String?> = ArrayList()

            // 使用正则表达式提取集数名称
            val pattern = Pattern.compile("<li><a[^>]*>(.*?)</a></li>")
            val matcher = pattern.matcher(childList[i])
            while (matcher.find()) {
                val episodeName = matcher.group(1)
                episodeNames.add(episodeName)
            }
            val episodeLinks: MutableList<String?> = ArrayList()
            // 使用正则表达式提取链接
            val pattern2 = Pattern.compile("href=\"(.*?)\"")
            val matcher2 = pattern2.matcher(childList[i])
            while (matcher2.find()) {
                val episodeLink = matcher2.group(1)
                episodeLinks.add(YsjdmSearch.domain + episodeLink)
            }
            ListChild.add(episodeNames)
            ListLink.add(episodeLinks)
        }
        SearchComponent.addAnthology(
            activity,
            activity.findViewById(R.id.content),
            ListItem,
            ListChild,
            ListLink,
            false
        )
    }
}
