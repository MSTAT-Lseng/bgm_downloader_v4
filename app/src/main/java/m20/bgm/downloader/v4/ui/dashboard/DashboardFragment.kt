package m20.bgm.downloader.v4.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import m20.bgm.downloader.v4.BrowserActivity
import m20.bgm.downloader.v4.R
import m20.bgm.downloader.v4.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {
    private var binding: FragmentDashboardBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(
            inflater,
            container,
            false
        )
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListener(R.id.item_mikan, "https://mikanani.me/", R.string.mikan_project)
        setupClickListener(R.id.item_agefans, "https://www.agedm.org/", R.string.agefans)
        setupClickListener(R.id.item_acgrip, "https://acg.rip/", R.string.acgrip)
        setupClickListener(R.id.item_ysjdm, "https://www.lldm.net/", R.string.ysjdm)
        setupClickListener(R.id.item_dmxq, "https://dmflm.com/", R.string.dmxq)
        setupClickListener(R.id.item_libvio, "https://www.libvio.vip/", R.string.libvio)
        setupClickListener(R.id.item_qimiqimi, "http://www.qimiqimi.net/", R.string.qimiqimi)

    }

    private fun setupClickListener(viewId: Int, url: String, titleId: Int) {
        view?.findViewById<View>(viewId)?.setOnClickListener {
            startActivity(
                Intent(activity, BrowserActivity::class.java).putExtra(
                    "url",
                    url
                ).putExtra("title", resources.getString(titleId))
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}