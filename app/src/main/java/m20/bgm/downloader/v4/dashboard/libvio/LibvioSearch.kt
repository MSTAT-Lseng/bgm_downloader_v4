package m20.bgm.downloader.v4.dashboard.libvio

import android.app.Activity
import android.content.Intent
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Toast
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebViewClient
import m20.bgm.downloader.v4.BrowserActivity
import m20.bgm.downloader.v4.R
import m20.bgm.downloader.v4.component.SearchComponent
import m20.bgm.downloader.v4.manager.SettingsManager
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.Jsoup
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object LibvioSearch {
    var domain = "https://www.libvio.vip"

    @Throws(UnsupportedEncodingException::class)
    fun search(activity: Activity, settingsManager: SettingsManager?, keyword: String?, page: Int) {
        // 构建带有可变参数的链接，并进行URL编码
        val baseUrl = "$domain/search/" + URLEncoder.encode(
            keyword,
            "UTF-8"
        ) + "----------" + page + "---.html"

        val agentwebLinear = LinearLayout(activity)
        AgentWeb.with(activity)
            .setAgentWebParent(agentwebLinear, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator()
            .setWebViewClient(settingsManager?.let { webViewClient(activity, it, page, keyword) })
            .createAgentWeb()
            .ready()
            .go(baseUrl)

    }

    private class webViewClient(
        private val activity: Activity,
        private val settingsManager: SettingsManager,
        private val page: Int,
        private val keyword: String?
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
                search(activity, p0, settingsManager, page, keyword!!)
            }
        }
    }

    private fun search(
        activity: Activity,
        p0: WebView?,
        settingsManager: SettingsManager,
        page: Int,
        keyword: String
    ) {
        val contentLinear = activity.findViewById<LinearLayout>(R.id.content)

        if (p0 != null) {
            if (p0.title?.endsWith("LIBVIO") == true) {

                // 获取WebView中HTML并转换
                p0.evaluateJavascript("""
                    (function() {
                        var content = document.getElementsByTagName('html')[0].innerHTML;
                        return '<html>' + content + '</html>';
                    })();""".trimIndent(), ValueCallback { result ->
                    search(
                        StringEscapeUtils.unescapeEcmaScript(result),
                        activity,
                        contentLinear,
                        settingsManager,
                        page,
                        keyword
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

    private fun search(
        data: String,
        activity: Activity,
        contentLinear: LinearLayout,
        settingsManager: SettingsManager,
        page: Int,
        keyword: String
    ) {
        val list = data.split("<div class=\"stui-vodlist__box\">".toRegex())
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
                val title = document.getElementsByClass("title text-overflow")[0].text()
                val subtitle = document.getElementsByClass("pic-text text-right")[0].text()
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
        if (data.contains("\">尾页</a></li>")) {
            var _pageNumber = data.substring(
                data.indexOf("---.html\">尾页</a></li>") - 3,
                data.indexOf("---.html\">尾页</a></li>")
            )
            _pageNumber = _pageNumber.replace("-".toRegex(), "")
            val pageNumber = _pageNumber.toInt()
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
                            settingsManager.getValue("use_dashboard", ""),
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
