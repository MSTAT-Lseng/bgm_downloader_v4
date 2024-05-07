package m20.bgm.downloader.v4.ui.calendar

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import m20.bgm.downloader.v4.R
import java.util.Locale

class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        // Return a different Fragment for each tab
        val fragment = MyFragment.newInstance(position + 1)
        return fragment
    }

    override fun getCount(): Int {
        // Return the number of tabs
        return 7
    }

    override fun getPageTitle(position: Int): CharSequence {
        // Return the title for each tab
        return when (position) {
            0 -> {
                if (isChineseSimplified()) {
                    "周一"
                } else {
                    "Mon"
                }
            }
            1 -> {
                if (isChineseSimplified()) {
                    "周二"
                } else {
                    "Tue"
                }
            }
            2 -> {
                if (isChineseSimplified()) {
                    "周三"
                } else {
                    "Wed"
                }
            }
            3 -> {
                if (isChineseSimplified()) {
                    "周四"
                } else {
                    "Thu"
                }
            }
            4 -> {
                if (isChineseSimplified()) {
                    "周五"
                } else {
                    "Fri"
                }
            }
            5 -> {
                if (isChineseSimplified()) {
                    "周六"
                } else {
                    "Sat"
                }
            }
            6 -> {
                if (isChineseSimplified()) {
                    "周日"
                } else {
                    "Sun"
                }
            }
            else -> {
                ""
            }
        }
    }

    private fun isChineseSimplified(): Boolean {
        val locale = Locale.getDefault()
        val language = locale.language
        val country = locale.country
        return language == "zh" && country == "CN"
    }

}
