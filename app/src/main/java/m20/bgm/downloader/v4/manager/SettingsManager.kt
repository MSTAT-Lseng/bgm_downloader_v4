package m20.bgm.downloader.v4.manager

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context?) {
    private val sharedPreferences: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        sharedPreferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    fun saveValue(key: String?, value: String?) {
        editor.putString(key, value)
        editor.apply()
    }

    fun getValue(key: String?, defaultValue: String?): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun removeValue(key: String?) {
        editor.remove(key)
        editor.apply()
    }

    fun clearAllValues() {
        editor.clear()
        editor.apply()
    }

    companion object {
        private const val PREF_NAME = "SettingsPref"
        private const val KEY_VALUE = "key_value"
    }
}
