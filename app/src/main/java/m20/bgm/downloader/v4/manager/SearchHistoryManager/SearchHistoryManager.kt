package m20.bgm.downloader.v4.manager.SearchHistoryManager

import android.content.Context
import androidx.room.Room

class SearchHistoryManager(context: Context?) {
    private val db: AppDatabase

    init {
        // 初始化数据库实例
        db = Room.databaseBuilder(
            context!!,
            AppDatabase::class.java, "search-history-db"
        ).build()
    }

    fun saveSearchHistory(searchText: String?) {
        val timestamp = System.currentTimeMillis()
        val searchHistory = SearchHistory(searchText, timestamp)
        db.searchHistoryDao().insert(searchHistory)
    }

    val allSearchHistory: List<SearchHistory?>?
        get() {
            // 获取所有的搜索历史记录
            return db.searchHistoryDao().all
        }

    fun clearSearchHistory() {
        // 清除所有的搜索历史记录
        db.searchHistoryDao().deleteAll()
    }
}
