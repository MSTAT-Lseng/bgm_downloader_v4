package m20.bgm.downloader.v4.dashboard.libvio

import android.app.Activity
import android.content.Intent
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.Toast
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebViewClient
import m20.bgm.downloader.v4.BrowserActivity
import m20.bgm.downloader.v4.R
import m20.bgm.downloader.v4.component.SearchComponent
import m20.bgm.downloader.v4.component.SearchComponent.addAnthology
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.Jsoup
import java.util.regex.Pattern

object LibvioAnthology {
    fun loadAnthology(activity: Activity, url: String?) {

        val agentwebLinear = LinearLayout(activity)
        AgentWeb.with(activity)
            .setAgentWebParent(agentwebLinear, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator()
            .setWebViewClient(webViewClient(activity))
            .createAgentWeb()
            .ready()
            .go(url)

    }

    private class webViewClient(
        private val activity: Activity
    ) : WebViewClient() {
        var err = false

        override fun onReceivedError(p0: WebView?, p1: WebResourceRequest?, p2: WebResourceError?) {
            super.onReceivedError(p0, p1, p2)
            err = true
            if (p2 != null) {
                SearchComponent.loadEmpty(
                    activity,
                    activity.resources.getText(R.string.load_failed) as String + p2.errorCode.toString()
                )
            }
        }

        override fun onPageFinished(p0: WebView?, p1: String?) {
            super.onPageFinished(p0, p1)
            // 错误检查
            if (!err) {
                anthology(activity, p0)
            }
        }
    }

    private fun anthology(
        activity: Activity,
        p0: WebView?,
    ) {
        if (p0 != null) {
            if (p0.title?.endsWith("LIBVIO") == true) {

                // 获取WebView中HTML并转换
                p0.evaluateJavascript("""
                    (function() {
                        var content = document.getElementsByTagName('html')[0].innerHTML;
                        return '<html>' + content + '</html>';
                    })();""".trimIndent(), ValueCallback { result ->
                    anthology(
                        activity,
                        StringEscapeUtils.unescapeEcmaScript(result)
                    )
                })

            } else if (p0.title?.contains("系统提示") == true) {
                // 频繁搜索
                SearchComponent.loadEmpty(
                    activity,
                    activity.resources.getText(R.string.search_interval_too_short) as String
                )
            } else {
                // 安全验证
                Toast.makeText(
                    activity,
                    activity.resources.getText(R.string.request_security_check),
                    Toast.LENGTH_SHORT
                ).show()
                activity.startActivity(
                    Intent(activity, BrowserActivity::class.java).putExtra(
                        "url",
                        p0.url
                    ).putExtra("title", "Check").putExtra("libvio_cookie_check", true)
                )
                activity.finish()
            }
        }
    }

    private fun anthology(activity: Activity, data: String) {
        activity.findViewById<View>(R.id.progress).visibility = View.GONE
        val listItems = data.split("<span class=\"more text-muted pull-right\">".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val ListItem: MutableList<String> = ArrayList()
        for (i in 1 until listItems.size) {
            val document = Jsoup.parse(listItems[i])
            val text = document.getElementsByClass("iconfont icon-iconfontplay2")[0].text()
            ListItem.add(text)
        }
        val ListChild: MutableList<List<String>> = ArrayList()
        val ListLink: MutableList<List<String>> = ArrayList()
        listItems.drop(1).forEach { listItem ->
            val episodeNames =
                Regex(">([^<]+)</a></li>").findAll(listItem).mapNotNull { it.groupValues[1] }
                    .toList()
            val episodeLinks = Regex("href=\"([^\"]+)\"").findAll(listItem)
                .map { LibvioSearch.domain + it.groupValues[1] }.toList()
            ListChild.add(episodeNames)
            ListLink.add(episodeLinks)
        }
        addAnthology(
            activity,
            activity.findViewById(R.id.content),
            ListItem,
            ListChild,
            ListLink
        )
    }

}
