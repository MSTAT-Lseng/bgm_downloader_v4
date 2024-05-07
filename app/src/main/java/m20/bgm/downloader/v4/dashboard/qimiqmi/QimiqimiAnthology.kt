package m20.bgm.downloader.v4.dashboard.qimiqmi

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
import org.jsoup.Jsoup
import java.io.IOException

object QimiqimiAnthology {

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
        val content =
            data.replace("(?s).*<!--在线播放地址-->(.*?)<!--迅雷下载地址-->.*".toRegex(), "$1")
        val doc = Jsoup.parse(content)
        val listItems = doc.select("h2")
        val ListItem: MutableList<String> = ArrayList()
        for (item in listItems) {
            val text = item.text()
            ListItem.add(text)
        }
        val ListChild: MutableList<List<String>> = ArrayList()
        val ListLink: MutableList<List<String>> = ArrayList()
        val childItems =
            content.split("<div class=\"video_list fn-clear\">".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()

        childItems.drop(1).forEach { childItem ->
            val episodeNames =
                Regex(">([^<]+)</a>").findAll(childItem).map { it.groupValues[1] }.toList()
            val episodeLinks =
                Regex("href=\"([^\"]+)\"").findAll(childItem).map { QimiqimiSearch.domain + it.groupValues[1] }.toList()
            ListChild.add(episodeNames)
            ListLink.add(episodeLinks)
        }

        SearchComponent.addAnthology(
            activity,
            activity.findViewById(R.id.content),
            ListItem,
            ListChild,
            ListLink
        )
    }

}
