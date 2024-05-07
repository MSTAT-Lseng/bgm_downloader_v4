package m20.bgm.downloader.v4.manager.SearchHistoryManager

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SearchHistory::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
}