package m20.bgm.downloader.v4.ui.home

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.AdapterView
import android.widget.AdapterView.INVISIBLE
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import m20.bgm.downloader.v4.BrowserActivity
import m20.bgm.downloader.v4.CalendarActivity
import m20.bgm.downloader.v4.R
import m20.bgm.downloader.v4.SearchActivity
import m20.bgm.downloader.v4.SettingsActivity
import m20.bgm.downloader.v4.calendar.CalendarJSON
import m20.bgm.downloader.v4.component.SettingsComponent
import m20.bgm.downloader.v4.component.URLComponent
import m20.bgm.downloader.v4.databinding.FragmentHomeBinding
import m20.bgm.downloader.v4.manager.FavoritesManager
import m20.bgm.downloader.v4.manager.SearchHistoryManager.SearchHistoryManager
import m20.bgm.downloader.v4.manager.SettingsManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.xutils.x
import java.io.IOException
import java.util.Calendar
import kotlin.math.ceil


class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private var favoritesManager: FavoritesManager? = null
    private var collectionAdapter: ArrayAdapter<String?>? = null
    private var animeCalendarData: String? = null
    private var searchView: SearchView? = null
    private var fragmentView: View? = null
    private var flexboxLayoutPointerWidth = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        val root: View = binding!!.root
        searchView = root.findViewById(R.id.search)
        searchView?.setOnQueryTextListener(onQueryTextListener) // 设置搜索提交事件
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favoritesManager = FavoritesManager(activity)
        fragmentView = view

        // 绑定设置按钮
        val settingsButton = view.findViewById<ImageView>(R.id.settings)
        settingsButton.setOnClickListener {
            startActivity(
                Intent(
                    activity, SettingsActivity::class.java
                )
            )
        }

        view.findViewById<LinearLayout>(R.id.image_search_anime).setOnClickListener {
            selectImageSearchAnime()
        }

        // 加载今日上映、收藏条目
        loadCollection(view)
        loadReleasedToday(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        loadCollection(fragmentView)
    }

    private fun selectImageSearchAnime() {
        val sites = arrayOf("trace.moe", "ai.animedb.cn")
        val builder = activity?.let { MaterialAlertDialogBuilder(it) }
        builder?.setTitle(getText(R.string.image_search_anime))
        builder?.setItems(sites) { _: DialogInterface?, which: Int ->
            when (which) {
                0 -> startActivity(
                    Intent(
                        activity, BrowserActivity::class.java
                    ).putExtra("url", "https://trace.moe/").putExtra("title", getText(R.string.image_search_anime))
                )
                1 -> startActivity(
                    Intent(
                        activity, BrowserActivity::class.java
                    ).putExtra("url", "https://ai.animedb.cn/").putExtra("title", getText(R.string.image_search_anime))
                )
            }
        }
        builder?.show()
    }

    private fun loadCollection(view: View?) {
        val collectionGrid = requireView().findViewById<GridView>(R.id.collection)
        val collectionTitle = view?.findViewById<TextView>(R.id.collection_title)
        val items = favoritesManager?.allFavoriteKeywords ?: return

        try {
            if (items.isNotEmpty()) {
                collectionTitle?.visibility = VISIBLE
                collectionGrid.visibility = VISIBLE
                val adapter = ArrayAdapter(requireActivity(), R.layout.collection_item, items)
                collectionGrid.adapter = adapter
                collectionAdapter = adapter
                collectionGrid.onItemLongClickListener = onItemLongClickListener
                collectionGrid.onItemClickListener = collectionItemClickListener
                freshCollectionViewHeight(collectionGrid, items.size)
            } else {
                collectionTitle?.visibility = View.GONE
                collectionGrid.visibility = View.GONE
            }
        } catch (e: NullPointerException) {
            Log.e("NullPointerException", e.toString())
        }
    }

    private var onItemLongClickListener = OnItemLongClickListener { parent, view, position, id ->
        val collectText = (view.findViewById<View>(android.R.id.text1) as TextView).text as String
        favoritesManager?.removeFavoriteKeyword(collectText)
        loadCollection(fragmentView)
        true
    }
    private var onQueryTextListener: SearchView.OnQueryTextListener =
        object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // 处理搜索提交事件，执行搜索操作
                startActivity(
                    Intent(activity, SearchActivity::class.java).putExtra(
                        "keyword",
                        query
                    )
                )
                val settingsManager = SettingsManager(activity)
                if (settingsManager.getValue(
                        SettingsComponent.SETTINGS_SAVE_SEARCH_HISTORY_KEY,
                        SettingsComponent.SETTINGS_SAVE_SEARCH_HISTORY_DEFAULT_VALUE
                    ) == SettingsComponent.SETTINGS_SAVE_SEARCH_HISTORY_ENABLED_VALUE
                ) {
                    saveSearchHistory(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // 处理搜索关键字变化事件，更新搜索结果列表等操作
                return true
            }
        }

    private fun saveSearchHistory(query: String) {
        val thread = Thread { // 保存搜索历史纪录
            val searchHistoryManager = SearchHistoryManager(activity)
            searchHistoryManager.saveSearchHistory(query)
        }
        // 启动线程
        thread.start()
    }

    private fun freshCollectionViewHeight(view: GridView, length: Int) {
        val line = ceil(length.toDouble() / 3).toInt()
        val height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            (line * 60).toFloat(),
            resources.displayMetrics
        ).toInt()
        val layoutParams = view.layoutParams
        layoutParams.height = height
        view.layoutParams = layoutParams
    }

    private var collectionItemClickListener =
        AdapterView.OnItemClickListener { _, _, position, _ ->
            searchView?.setQuery(
                collectionAdapter?.getItem(position),
                true
            )
        }

    private fun loadReleasedToday(view: View) {
        if (animeCalendarData == null) {
            loadReleasedTodayData(view)
        } else {
            requireActivity().runOnUiThread {
                try {
                    loadReleasedTodayList(view)
                    loadReleasedTodayFade(view)
                } catch (e: JsonSyntaxException) {
                    Log.e("JsonSyntaxException", e.toString())
                } catch (e: NullPointerException) {
                    Log.e("NullPointerException", e.toString())
                }
            }
        }
    }

    private fun loadReleasedTodayFade(view: View) {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = 150
        fadeOut.fillAfter = true
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 150
        fadeIn.fillAfter = true
        val progress = view.findViewById<ProgressBar>(R.id.progress)
        val releasedTitle = view.findViewById<TextView>(R.id.released_today_title)
        val releasedView = view.findViewById<LinearLayout>(R.id.released_today)
        progress.startAnimation(fadeOut)
        progress.postDelayed({
            progress.visibility = View.GONE
            releasedTitle.startAnimation(fadeIn)
            releasedTitle.visibility = View.VISIBLE
            releasedView.startAnimation(fadeIn)
            releasedView.visibility = View.VISIBLE
        }, 150)
    }

    private fun loadReleasedTodayList(view: View) {
        val gson = Gson()
        val calendarJSONS = gson.fromJson(animeCalendarData, Array<CalendarJSON>::class.java)
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
        var releasedPosition = 0
        when (dayOfWeek) {
            1 -> releasedPosition = 5
            2 -> releasedPosition = 6
            3 -> {}
            4 -> releasedPosition = 1
            5 -> releasedPosition = 2
            6 -> releasedPosition = 3
            7 -> releasedPosition = 4
        }
        val flexboxLayout = view.findViewById<FlexboxLayout>(R.id.released_today_flex)
        val releasedSize = calendarJSONS[releasedPosition].items?.size
        try {
            for (i in 0 until releasedSize!!) {
                val json = calendarJSONS[releasedPosition].items?.get(i)
                var title = json?.name_cn
                if (title == "") {
                    title = json?.name
                }
                var picture: String? = ""
                try {
                    picture = json?.images?.common
                } catch (ignored: Exception) {
                }
                val layoutInflater = LayoutInflater.from(activity)
                val listView = layoutInflater.inflate(R.layout.released_today_item, null)
                (listView.findViewById<View>(R.id.title) as TextView).text = title // 设置名称
                x.image().bind(listView.findViewById(R.id.picture), picture) // 设置图像
                val finalTitle = title
                listView.findViewById<View>(R.id.content)
                    .setOnClickListener { searchView?.setQuery(finalTitle, true) }
                // 添加到总容器
                flexboxLayout.addView(listView)
            }
        } catch (e: NullPointerException) {
            Log.e("NullPointerException", e.toString())
        }

        // fab 按钮的显示
        view.findViewById<LinearLayout>(R.id.anime_calendar).visibility = VISIBLE
        view.findViewById<LinearLayout>(R.id.image_search_anime).visibility = VISIBLE
        view.findViewById<LinearLayout>(R.id.anime_calendar).setOnClickListener {
            val settingsManager = SettingsManager(activity)
            settingsManager.saveValue("calendar_json", animeCalendarData)
            startActivity(Intent(activity, CalendarActivity::class.java))
        }

        // 处理flexboxLayout在最后一行的布局问题。
        // 步骤 1: 获取 FlexboxLayout 中的子 View 数量
        val childCount = flexboxLayout.childCount
        // 步骤 2: 获取 FlexboxLayout 的宽度
        var flexboxWidth = flexboxLayoutPointerWidth
        if (flexboxLayoutPointerWidth == 0) {
            flexboxWidth = view.findViewById<View>(R.id.released_today_flex_point).width
            flexboxLayoutPointerWidth = flexboxWidth
        }
        // 步骤 3: 获取子 View 的宽度并计算第一行的条目数量
        var totalWidth = 0
        var numberOfItemsInFirstRow = 0

        for (i in 0 until childCount) {
            val child = flexboxLayout.getChildAt(i)
            // 测量子 View 的宽度
            child.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val childWidth = child.measuredWidth
            // 累加子 View 的宽度
            totalWidth += childWidth
            // 如果累加的宽度超过了 FlexboxLayout 的宽度，则跳出循环
            if (totalWidth > flexboxWidth) {
                break
            }
            // 增加第一行的条目数量
            numberOfItemsInFirstRow++
        }
        // 如果 numberOfItemsInFirstRow 大于 releasedSize，那就是一行能装下，不必处理。
        if (numberOfItemsInFirstRow < releasedSize!!) {
            try {
                val needReplaceNumber =
                    mathNextDivisible(releasedSize, numberOfItemsInFirstRow) - releasedSize
                if (needReplaceNumber > 0) {
                    for (i in 0 until needReplaceNumber) {
                        // 添加占位的条目
                        val layoutInflater = LayoutInflater.from(activity)
                        val listView = layoutInflater.inflate(R.layout.released_today_item, null)
                        listView.visibility = INVISIBLE
                        flexboxLayout.addView(listView)
                    }
                }
            } catch (_: ArithmeticException) {
            }

        }
    }

    private fun mathNextDivisible(dividend: Int, divisor: Int): Int {
        val remainder = dividend % divisor
        return if (remainder == 0) {
            dividend // If the remainder is 0, the dividend is already divisible
        } else {
            dividend + (divisor - remainder) // Add the difference between divisor and remainder to dividend
        }
    }

    private fun loadReleasedTodayData(view: View) {
        val okHttpClient = OkHttpClient()
        val request = Request.Builder().url(URLComponent.bgmCalendarUrl).removeHeader("User-Agent")
            .addHeader("User-Agent", URLComponent.commonUA).build()
        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    (view.findViewById<View>(R.id.progress) as ProgressBar).visibility = View.GONE
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val data = response.body()?.string()
                animeCalendarData = data
                try {
                    loadReleasedToday(view)
                } catch (e: NullPointerException) {
                    Log.e("NullPointerException", e.toString())
                } catch (e: IllegalStateException) {
                    Log.e("IllegalStateException", e.toString())
                }
            }
        })
    }
}
