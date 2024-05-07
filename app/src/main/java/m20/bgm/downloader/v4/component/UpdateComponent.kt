package m20.bgm.downloader.v4.component

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import m20.bgm.downloader.v4.R
import m20.bgm.downloader.v4.update.UpdateInfo
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UpdateComponent(private val mContext: Context?, private val mListener: UpdateListener) {
    fun checkForUpdates() {
        CheckUpdatesTask().execute()
    }

    private fun parseJsonResponse(json: String) {
        val gson = Gson()
        val updateInfo = gson.fromJson(json, UpdateInfo::class.java)
        if (updateInfo != null) {
            try {
                val packageInfo = mContext!!.packageManager.getPackageInfo(
                    mContext.packageName, 0
                )
                val currentVersion = packageInfo.versionName
                if (updateInfo.version?.compareTo(currentVersion)!! > 0) {
                    mListener.onUpdateAvailable(updateInfo)
                } else {
                    mListener.onNoUpdateAvailable()
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, "Failed to get package info", e)
                mListener.onCheckError()
            }
        } else {
            mListener.onCheckError()
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class CheckUpdatesTask : AsyncTask<Void?, Void?, String?>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void?): String? {
            var connection: HttpURLConnection? = null
            var reader: BufferedReader? = null
            try {
                val url = URL(JSON_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", URLComponent.commonUA)
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val jsonResponse = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        jsonResponse.append(line)
                    }
                    return jsonResponse.toString()
                } else {
                    Log.e(TAG, "HTTP response code: $responseCode")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error checking for updates", e)
            } finally {
                connection?.disconnect()
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (e: IOException) {
                        Log.e(TAG, "Error closing reader", e)
                    }
                }
            }
            return null
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(jsonResponse: String?) {
            if (jsonResponse != null) {
                parseJsonResponse(jsonResponse)
            } else {
                mListener.onCheckError()
            }
        }
    }

    interface UpdateListener {
        fun onUpdateAvailable(updateInfo: UpdateInfo)
        fun onNoUpdateAvailable()
        fun onCheckError()
    }

    companion object {
        private const val TAG = "UpdateChecker"
        private const val JSON_URL: String = URLComponent.configV4 // 替换为您的 JSON 文件的 URL
        fun checkUpdates(
            activity: Activity?,
            hintNoUpdate: Boolean
        ) {
            val updateComponent = UpdateComponent(activity, object : UpdateListener {
                override fun onUpdateAvailable(updateInfo: UpdateInfo) {
                    // 处理有更新可用的情况
                    checkUpdates(activity, updateInfo)
                }

                override fun onNoUpdateAvailable() {
                    /* 处理没有可用更新的情况 */
                    if (hintNoUpdate) {
                        Toast.makeText(
                            activity,
                            activity!!.resources.getText(R.string.already_latest_version),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCheckError() {
                    // 处理检查更新时出现错误的情况
                    Toast.makeText(
                        activity,
                        activity!!.resources.getText(R.string.check_update_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            updateComponent.checkForUpdates()
        }

        private fun checkUpdates(activity: Activity?, updateInfo: UpdateInfo) {
            val version = updateInfo.version
            val changelog = updateInfo.changelog
            val downloadUrl = updateInfo.downloadUrl
            val forceUpdate = updateInfo.isForceUpdate

            // 弹出对话框提示用户更新
            val builder = MaterialAlertDialogBuilder(
                activity!!
            )
                .setCancelable(!forceUpdate)
            builder.setTitle(
                activity.resources.getText(R.string.find_new_version).toString() + " " + version
            )
            builder.setMessage(changelog)
            builder.setPositiveButton(activity.resources.getText(R.string.download_current)) { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                activity.startActivity(intent)
                if (forceUpdate) {
                    activity.finish()
                }
            }
            val dialog = builder.create()
            dialog.show()
        }
    }
}
