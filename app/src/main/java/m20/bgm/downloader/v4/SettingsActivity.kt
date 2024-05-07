package m20.bgm.downloader.v4

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import m20.bgm.downloader.v4.component.SettingsComponent
import m20.bgm.downloader.v4.component.UIComponent
import m20.bgm.downloader.v4.manager.SettingsManager

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        UIComponent.setMaterial3ActionBar(this, supportActionBar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish() // 处理向上导航按钮点击事件
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val settingsManager = SettingsManager(activity)
            val useDashboard = findPreference<ListPreference>("use_dashboard")
            val enabledSearchHistory =
                findPreference<SwitchPreferenceCompat>("enabled_search_history")
            val autoCheckUpdates = findPreference<SwitchPreferenceCompat>("auto_check_updates")
            assert(useDashboard != null)

            val dashboardValue = settingsManager.getValue("use_dashboard", "")
            useDashboard!!.value = when (dashboardValue) {
                "acgrip", "agefans", "mikan", "dmxq", "ysjdm", "libvio", "qimiqimi" -> dashboardValue
                else -> ""
            }
            assert(enabledSearchHistory != null)
            enabledSearchHistory!!.isChecked = settingsManager.getValue(
                SettingsComponent.SETTINGS_SAVE_SEARCH_HISTORY_KEY,
                SettingsComponent.SETTINGS_SAVE_SEARCH_HISTORY_DEFAULT_VALUE
            ) == SettingsComponent.SETTINGS_SAVE_SEARCH_HISTORY_ENABLED_VALUE
            assert(autoCheckUpdates != null)
            autoCheckUpdates!!.isChecked = settingsManager.getValue(
                SettingsComponent.SETTINGS_AUTO_CHECK_UPDATE_KEY,
                SettingsComponent.SETTINGS_AUTO_CHECK_UPDATE_DEFAULT_VALUE
            ) == SettingsComponent.SETTINGS_AUTO_CHECK_UPDATE_ENABLED_VALUE
            useDashboard.onPreferenceChangeListener = this
            enabledSearchHistory.onPreferenceChangeListener = this
            autoCheckUpdates.onPreferenceChangeListener = this
        }

        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            val settingsManager = SettingsManager(activity)
            val key = preference.key
            if (key == "enabled_search_history") {
                if (newValue.toString() == "true") {
                    settingsManager.saveValue(
                        SettingsComponent.SETTINGS_SAVE_SEARCH_HISTORY_KEY,
                        SettingsComponent.SETTINGS_SAVE_SEARCH_HISTORY_ENABLED_VALUE
                    )
                } else {
                    settingsManager.saveValue(
                        SettingsComponent.SETTINGS_SAVE_SEARCH_HISTORY_KEY,
                        SettingsComponent.SETTINGS_SAVE_SEARCH_HISTORY_DISABLED_VALUE
                    )
                }
            } else if (key == "auto_check_updates") {
                if (newValue.toString() == "true") {
                    settingsManager.saveValue(
                        SettingsComponent.SETTINGS_AUTO_CHECK_UPDATE_KEY,
                        SettingsComponent.SETTINGS_AUTO_CHECK_UPDATE_ENABLED_VALUE
                    )
                } else {
                    settingsManager.saveValue(
                        SettingsComponent.SETTINGS_AUTO_CHECK_UPDATE_KEY,
                        SettingsComponent.SETTINGS_AUTO_CHECK_UPDATE_DISABLED_VALUE
                    )
                }
            } else {
                settingsManager.saveValue("use_dashboard", newValue.toString())
            }
            return true
        }
    }
}