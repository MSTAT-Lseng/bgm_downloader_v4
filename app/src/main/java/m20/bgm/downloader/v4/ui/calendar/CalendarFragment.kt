package m20.bgm.downloader.v4.ui.calendar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import m20.bgm.downloader.v4.R
import m20.bgm.downloader.v4.SearchActivity
import m20.bgm.downloader.v4.calendar.CalendarJSON
import m20.bgm.downloader.v4.component.SettingsComponent
import m20.bgm.downloader.v4.manager.SettingsManager
import org.xutils.x
import java.util.Calendar

class MyFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_calendar, container, false)

        val contentView = view.findViewById<LinearLayout>(R.id.content)
        val tabNumber: Int = requireArguments().getInt(ARG_TAB_NUMBER)

        val settingsManager = SettingsManager(activity)
        val json_data = settingsManager.getValue("calendar_json", "")

        val gson = Gson()
        val calendarJSONS = gson.fromJson(json_data, Array<CalendarJSON>::class.java)
        val releasedPosition = tabNumber - 1
        val releasedSize = calendarJSONS[releasedPosition].items?.size
        try {
            for (i in 0 until releasedSize!!) {
                val json = calendarJSONS[releasedPosition].items?.get(i)
                var title = json?.name_cn
                if (title == "") {
                    title = json?.name
                }
                var summary = ""
                try {
                    summary = json?.name.toString()
                } catch (_: Exception) {
                }
                var picture: String? = ""
                try {
                    picture = json?.images?.common
                } catch (ignored: Exception) {
                }
                val layoutInflater = LayoutInflater.from(activity)
                val listView = layoutInflater.inflate(R.layout.dashboard_item, null)
                (listView.findViewById<View>(R.id.title) as TextView).text = title // 设置名称
                (listView.findViewById<View>(R.id.subtitle) as TextView).text = summary // 设置名称
                x.image().bind(listView.findViewById(R.id.picture), picture) // 设置图像
                listView.findViewById<View>(R.id.content)
                    .setOnClickListener {
                        // 处理搜索提交事件，执行搜索操作
                        startActivity(
                            Intent(activity, SearchActivity::class.java).putExtra(
                                "keyword",
                                title
                            )
                        )
                    }
                // 添加到总容器
                contentView.addView(listView)
            }
        } catch (e: NullPointerException) {
            Log.e("NullPointerException", e.toString())
        }

        return view
    }

    companion object {
        private const val ARG_TAB_NUMBER = "tab_number"
        fun newInstance(tabNumber: Int): MyFragment {
            val fragment = MyFragment()
            val args = Bundle()
            args.putInt(ARG_TAB_NUMBER, tabNumber)
            fragment.setArguments(args)
            return fragment
        }
    }
}