package m20.bgm.downloader.v4

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import m20.bgm.downloader.v4.component.UIComponent
import m20.bgm.downloader.v4.manager.SearchHistoryManager.SearchHistoryManager

class SearchHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_history)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        UIComponent.setMaterial3ActionBar(this, supportActionBar) // 设置标题栏样式
        val thread = Thread { searchHistory() }
        // 启动线程
        thread.start()
    }

    private fun searchHistory() {
        // 在新线程中执行数据库操作
        val searchHistoryManager = SearchHistoryManager(this@SearchHistoryActivity)
        val searchHistory = ArrayList<String?>()
        val allSearchHistory = searchHistoryManager.allSearchHistory
        for (i in allSearchHistory!!.indices) {
            searchHistory.add(allSearchHistory[i]?.searchText)
        }
        runOnUiThread { searchHistory(searchHistory) }
    }

    private fun searchHistory(searchHistory: ArrayList<String?>) {
        // 创建适配器
        val adapter = ArrayAdapter(
            this@SearchHistoryActivity,
            android.R.layout.simple_list_item_1,
            searchHistory
        )

        // 设置适配器
        val listView = findViewById<ListView>(R.id.list)
        listView.adapter = adapter

        // 处理列表项点击事件
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ -> // 处理点击事件逻辑
                val selectedItem = adapter.getItem(position)
                startActivity(
                    Intent(
                        this@SearchHistoryActivity,
                        SearchActivity::class.java
                    ).putExtra("keyword", selectedItem)
                )
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search_history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_delete -> {
                val thread = Thread { clearHistory() }
                // 启动线程
                thread.start()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun clearHistory() {
        // 在新线程中执行数据库操作
        val searchHistoryManager = SearchHistoryManager(this@SearchHistoryActivity)
        searchHistoryManager.clearSearchHistory()
        runOnUiThread {
            startActivity(Intent(this@SearchHistoryActivity, SearchHistoryActivity::class.java))
            finish()
        }
    }
}