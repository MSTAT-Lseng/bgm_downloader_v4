package m20.bgm.downloader.v4.dashboard.dmxq

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import m20.bgm.downloader.v4.R
import m20.bgm.downloader.v4.component.SearchComponent.addAnthology
import m20.bgm.downloader.v4.component.URLComponent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.regex.Pattern

object DmxqAnthology {
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
        val listItems = data.split("<div class=\"module-tab-item tab-item\"".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val ListItem: MutableList<String> = ArrayList()
        for (i in 1 until listItems.size) {
            val text = listItems[i].replace("(?s).*<span>(.*?)</span>.*".toRegex(), "$1")
            ListItem.add(text)
        }
        val ListChild: MutableList<List<String>> = ArrayList()
        val ListLink: MutableList<List<String>> = ArrayList()
        val childItems =
            data.split("<div class=\"module-play-list-content  module-play-list-base\">".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
        for (i in 1 until childItems.size) {
            val episodeNames: MutableList<String> = ArrayList()
            // 使用正则表达式提取集数名称
            val pattern = Pattern.compile("<span>(.*?)</span>")
            val matcher = pattern.matcher(childItems[i])
            while (matcher.find()) {
                val episodeName = matcher.group(1)
                if (episodeName != null) {
                    episodeNames.add(episodeName)
                }
            }
            val episodeLinks: MutableList<String> = ArrayList()
            // 使用正则表达式提取链接
            val pattern2 = Pattern.compile("href=\"(.*?)\"")
            val matcher2 = pattern2.matcher(childItems[i])
            while (matcher2.find()) {
                val episodeLink = matcher2.group(1)
                episodeLinks.add(DmxqSearch.domain + episodeLink)
            }
            ListChild.add(episodeNames)
            ListLink.add(episodeLinks)
        }
        addAnthology(
            activity,
            activity.findViewById<LinearLayout>(R.id.content),
            ListItem,
            ListChild,
            ListLink
        )
    }
}
