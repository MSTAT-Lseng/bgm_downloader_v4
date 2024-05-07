package m20.bgm.downloader.v4

import android.app.Application
import com.umeng.commonsdk.UMConfigure
import m20.bgm.downloader.v4.component.ConfigComponent
import org.xutils.x

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        x.Ext.init(this)

        // SDK预初始化函数不会采集设备信息，也不会向友盟后台上报数据。
        // preInit预初始化函数耗时极少，不会影响App首次冷启动用户体验
        UMConfigure.preInit(this, ConfigComponent.UMENG_APP_KEY, ConfigComponent.UMENG_CHANNEL)
    }
}
