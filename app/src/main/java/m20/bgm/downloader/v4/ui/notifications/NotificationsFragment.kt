package m20.bgm.downloader.v4.ui.notifications

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import m20.bgm.downloader.v4.BrowserActivity
import m20.bgm.downloader.v4.R
import m20.bgm.downloader.v4.SearchHistoryActivity
import m20.bgm.downloader.v4.component.UpdateComponent
import m20.bgm.downloader.v4.databinding.FragmentNotificationsBinding
import m20.bgm.downloader.v4.manager.SettingsManager

class NotificationsFragment : Fragment() {
    private var binding: FragmentNotificationsBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(
            inflater,
            container,
            false
        )
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val settingsManager = SettingsManager(
            activity
        )
        try {
            val packageManager = activity?.packageManager
            val packageInfo = activity?.let {
                packageManager?.getPackageInfo(
                    it.packageName, 0
                )
            }
            val versionName = packageInfo?.versionName
            //val versionCode = packageInfo?.versionCode

            // 使用 versionName 和 versionCode 进行你想要的操作
            // 例如，将版本号显示在日志中
            (view.findViewById<View>(R.id.app_version) as TextView).text = "Version $versionName"
            view.findViewById<View>(R.id.update_log).setOnClickListener {
                try {
                    showUpdateLog(
                        versionName!!
                    )
                } catch (e: NullPointerException) {
                    Log.e("NullPointerException", e.toString())
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("PackageManager.NameNotFoundException", e.toString())
        }
        view.findViewById<View>(R.id.use_guide).setOnClickListener {
            startActivity(
                Intent(activity, BrowserActivity::class.java).putExtra(
                    "url",
                    "https://b.mstat.top/index.php/archives/750/"
                ).putExtra("title", resources.getString(R.string.use_guide))
            )
        }
        view.findViewById<View>(R.id.search_history).setOnClickListener {
            activity?.startActivity(
                Intent(activity, SearchHistoryActivity::class.java)
            )
        }
        (view.findViewById<View>(R.id.privacy) as TextView).setOnClickListener {
            startActivity(
                Intent(activity, BrowserActivity::class.java).putExtra(
                    "url",
                    "https://b.mstat.top/bgm_downloader/private/"
                ).putExtra("title", resources.getString(R.string.privacy))
            )
        }
        view.findViewById<View>(R.id.blog).setOnClickListener {
            startActivity(
                Intent(
                    activity,
                    BrowserActivity::class.java
                ).putExtra("url", "https://b.mstat.top/")
                    .putExtra("title", resources.getString(R.string.blog))
            )
        }
        view.findViewById<View>(R.id.bilibili).setOnClickListener {
            startActivity(
                Intent(activity, BrowserActivity::class.java).putExtra(
                    "url",
                    "https://space.bilibili.com/110941471"
                ).putExtra("title", resources.getString(R.string.bilibili))
            )
        }
        (view.findViewById<View>(R.id.check_update) as LinearLayout).setOnClickListener {
            Toast.makeText(
                activity,
                resources.getText(R.string.checking_update),
                Toast.LENGTH_SHORT
            ).show()
            UpdateComponent.checkUpdates(activity, true)
        }
        view.findViewById<View>(R.id.qqgroup)
            .setOnClickListener {
                startActivity(
                    Intent(
                        activity,
                        BrowserActivity::class.java
                    ).putExtra(
                        "url",
                        "https://b.mstat.top/index.php/archives/751/"
                    ).putExtra("title", getText(R.string.qq_group))
                )
            }


        view.findViewById<View>(R.id.email).setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("ms_tat@163.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "番剧下载邮件反馈")
            intent.putExtra(Intent.EXTRA_TEXT, "填写你的邮件正文...")

            startActivity(Intent.createChooser(intent, getText(R.string.select_email_application)))
        }

        view.findViewById<View>(R.id.donation).setOnClickListener {
            startActivity(
                Intent(activity, BrowserActivity::class.java).putExtra(
                    "url",
                    "https://b.mstat.top/index.php/archives/731/"
                ).putExtra("title", getText(R.string.donation))
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun showUpdateLog(version: String) {
        val builder = MaterialAlertDialogBuilder(
            requireContext()
        )
        // 设置对话框标题和消息
        builder.setTitle("$version Update Log")
            .setMessage("\n1. 适配了奇米奇米检索网站；\n2. 优化了细节。")
        // 设置对话框按钮
        builder.setPositiveButton(resources.getText(R.string.okay)) { dialog, _ ->
            // 点击确定按钮后执行的操作
            dialog.dismiss() // 关闭对话框
        }
        // 创建并显示对话框
        val dialog = builder.create()
        dialog.show()
    }
}
