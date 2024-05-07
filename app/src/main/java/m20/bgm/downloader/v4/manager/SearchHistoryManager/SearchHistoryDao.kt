package m20.bgm.downloader.v4.manager.SearchHistoryManager

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SearchHistoryDao {
    @get:Query("SELECT * FROM search_history ORDER BY timestamp DESC")
    val all: List<SearchHistory>

    @Insert
    fun insert(searchHistory: SearchHistory)

    @Query("DELETE FROM search_history")
    fun deleteAll()
}