package m20.bgm.downloader.v4.manager

import android.content.Context
import android.content.SharedPreferences

class FavoritesManager(context: Context?) {
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveFavoriteKeyword(keyword: String?) {
        val count = favoriteCount
        val editor = sharedPreferences.edit()
        editor.putString(KEY_FAVORITE_KEYWORD + count, keyword)
        editor.putInt(KEY_FAVORITE_COUNT, count + 1)
        editor.apply()
    }

    val favoriteKeyword: String?
        get() = sharedPreferences.getString(KEY_FAVORITE_KEYWORD, "")
    val allFavoriteKeywords: List<String?>
        get() {
            val count = favoriteCount
            val favoriteKeywords: MutableList<String?> = ArrayList()
            for (i in 0 until count) {
                val keyword = sharedPreferences.getString(KEY_FAVORITE_KEYWORD + i, "")
                if (!keyword!!.isEmpty()) {
                    favoriteKeywords.add(keyword)
                }
            }
            return favoriteKeywords
        }

    fun removeFavoriteKeyword(keyword: String?) {
        val count = favoriteCount
        var indexToRemove = -1 // 记录要删除的条目的索引
        for (i in 0 until count) {
            val storedKeyword = sharedPreferences.getString(KEY_FAVORITE_KEYWORD + i, "")
            if (storedKeyword == keyword) {
                indexToRemove = i
                break
            }
        }
        if (indexToRemove != -1) {
            val editor = sharedPreferences.edit()
            editor.remove(KEY_FAVORITE_KEYWORD + indexToRemove)

            // 更新索引
            for (i in indexToRemove + 1 until count) {
                val keywordToShift = sharedPreferences.getString(KEY_FAVORITE_KEYWORD + i, "")
                editor.putString(KEY_FAVORITE_KEYWORD + (i - 1), keywordToShift)
                editor.remove(KEY_FAVORITE_KEYWORD + i)
            }
            updateFavoriteCount(count - 1)
            editor.apply()
        }
    }

    fun isFavoriteKeywordExists(keyword: String?): Boolean {
        val count = favoriteCount
        for (i in 0 until count) {
            val storedKeyword = sharedPreferences.getString(KEY_FAVORITE_KEYWORD + i, "")
            if (storedKeyword == keyword) {
                return true
            }
        }
        return false
    }

    private fun updateFavoriteCount(count: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_FAVORITE_COUNT, count)
        editor.apply()
    }

    private val favoriteCount: Int
        private get() = sharedPreferences.getInt(KEY_FAVORITE_COUNT, 0)

    companion object {
        private const val PREF_NAME = "MyPrefs"
        private const val KEY_FAVORITE_KEYWORD = "favoriteKeyword"
        private const val KEY_FAVORITE_COUNT = "favoriteCount"
    }
}