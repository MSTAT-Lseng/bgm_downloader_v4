package m20.bgm.downloader.v4

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import m20.bgm.downloader.v4.component.UIComponent


class VideoCapturedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_captured)

        // 接收 List 数据
        val bundle = intent.getBundleExtra("bundle")
        val VideoCapturedList = bundle?.getSerializable("pairList") as? ArrayList<Pair<String, String>> ?: arrayListOf()

        supportActionBar!!.title = getText(R.string.video_resources)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true) // 启用向上导航按钮
        UIComponent.setMaterial3ActionBar(this, supportActionBar) // 设置 M3 样式标题栏

        val contentView = findViewById<LinearLayout>(R.id.content)

        VideoCapturedList.forEach { (first, second) ->
            // 使用 first 和 second 访问条目的内容
            val layoutInflater = LayoutInflater.from(this)
            val view = layoutInflater.inflate(R.layout.dashboard_simple_item, null)
            val titleView = view.findViewById<TextView>(R.id.title)
            titleView.text = first // 设置名称
            titleView.maxLines = 2 // 设置行数
            titleView.ellipsize = TextUtils.TruncateAt.END // 设置省略号
            (view.findViewById<View>(R.id.subtitle) as TextView).text = "Capture Time: $second" // 设置描述
            view.findViewById<View>(R.id.content).setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_VIEW, Uri.parse(first)
                )
                startActivity(intent)
            }
            // 添加到总容器
            contentView.addView(view)
        }

    }
}