package m20.bgm.downloader.v4.manager.SearchHistoryManager

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
class SearchHistory {
    // Setter方法
    // Getter方法
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var searchText: String? = null
    var timestamp: Long = 0

    // 无参构造函数
    constructor()

    // 带参构造函数
    @Ignore
    constructor(searchText: String?, timestamp: Long) {
        this.searchText = searchText
        this.timestamp = timestamp
    }
}