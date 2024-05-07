package m20.bgm.downloader.v4.update

import com.google.gson.annotations.SerializedName

class UpdateInfo {
    @SerializedName("version")
    val version: String? = null

    @SerializedName("changelog")
    val changelog: String? = null

    @SerializedName("download_url")
    val downloadUrl: String? = null

    @SerializedName("force_update")
    val isForceUpdate = false
}