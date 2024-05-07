package m20.bgm.downloader.v4.component

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

object DownloadComponent {
    fun downloadFile(context: Context, fileUrl: String?, fileName: String, description: String?) {
        val request = DownloadManager.Request(Uri.parse(fileUrl))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle(removeSpecialCharacters(fileName))
        request.setDescription(description)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            removeSpecialCharacters(fileName)
        )
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    private fun removeSpecialCharacters(str: String): String {
        // 使用正则表达式匹配除了字母、数字、空格、小数点、中文字符和方括号之外的所有字符
        val regex = "[^a-zA-Z0-9\\s.\\u4e00-\\u9fa5\\[\\]]"
        // 使用空字符串替换特殊符号
        return str.replace(regex.toRegex(), "")
    }
}
