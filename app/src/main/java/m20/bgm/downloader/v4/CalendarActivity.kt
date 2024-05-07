package m20.bgm.downloader.v4

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import m20.bgm.downloader.v4.component.UIComponent
import m20.bgm.downloader.v4.ui.calendar.MyPagerAdapter


class CalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        // Create an adapter for the ViewPager
        val pagerAdapter = MyPagerAdapter(supportFragmentManager)
        viewPager.adapter = pagerAdapter

        // Connect the TabLayout with the ViewPager
        tabLayout.setupWithViewPager(viewPager)

        supportActionBar?.title = getString(R.string.bgm_calendar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 启用向上导航按钮
        UIComponent.setMaterial3ActionBar(this, supportActionBar) // 设置 M3 样式标题栏

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish() // 处理向上导航按钮点击事件
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}