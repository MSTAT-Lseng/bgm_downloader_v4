package m20.bgm.downloader.v4.component

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import m20.bgm.downloader.v4.R
import m20.bgm.downloader.v4.manager.SettingsManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class NoticeComponent(json: String?) {
    var title: String? = null
    var id = 0
    var content: String? = null
    var buttonText: String? = null
    var buttonLink: String? = null

    init {
        try {
            val jsonObject = JSONObject(json)
            title = jsonObject.getString("title")
            id = jsonObject.getInt("id")
            content = jsonObject.getString("content")
            buttonText = jsonObject.getString("buttonText")
            buttonLink = jsonObject.getString("buttonLink")
        } catch (e: JSONException) {
            Log.e("JSONException", e.toString())
        }
    }

    companion object {
        fun showNotice(activity: Activity, settingsManager: SettingsManager) {
            val client = OkHttpClient()
            val request = Request.Builder().url(URLComponent.noticeV4).removeHeader("User-Agent")
                .addHeader("User-Agent", URLComponent.commonUA).build()
            val call = client.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val data = response.body()!!.string()
                    activity.runOnUiThread { showNotice(activity, settingsManager, data) }
                }
            })
        }

        private fun showNotice(activity: Activity, settingsManager: SettingsManager, data: String) {
            val announcement = NoticeComponent(data)
            // 获取公告信息
            val title = announcement.title
            val id = announcement.id
            val content = announcement.content
            val buttonText = announcement.buttonText
            val buttonLink = announcement.buttonLink
            if (settingsManager.getValue("notice_$id", "") != "read") {
                val builder = MaterialAlertDialogBuilder(activity)
                builder.setTitle(title)
                builder.setMessage(content)
                builder.setPositiveButton(buttonText) { dialog, which ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(buttonLink))
                    activity.startActivity(intent)
                    settingsManager.saveValue("notice_$id", "read")
                }
                builder.setNegativeButton(activity.resources.getText(R.string.i_know)) { dialog, which ->
                    settingsManager.saveValue(
                        "notice_$id",
                        "read"
                    )
                }
                val dialog = builder.create()
                dialog.show()
            }
        }
    }
}