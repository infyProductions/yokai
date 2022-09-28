package eu.kanade.tachiyomi.ui.setting

import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceScreen
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.extension.ExtensionUpdateJob
import eu.kanade.tachiyomi.util.preference.bindTo
import eu.kanade.tachiyomi.util.preference.infoPreference
import eu.kanade.tachiyomi.util.preference.onChange
import eu.kanade.tachiyomi.util.preference.preferenceCategory
import eu.kanade.tachiyomi.util.preference.requireAuthentication
import eu.kanade.tachiyomi.util.preference.summaryRes
import eu.kanade.tachiyomi.util.preference.switchPreference
import eu.kanade.tachiyomi.util.preference.titleRes
import eu.kanade.tachiyomi.util.system.AuthenticatorUtil.isAuthenticationSupported
import uy.kohesive.injekt.injectLazy

class SettingsBrowseController : SettingsController() {

    private val sourcePreferences: SourcePreferences by injectLazy()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = screen.apply {
        titleRes = R.string.browse

        preferenceCategory {
            titleRes = R.string.label_sources

            switchPreference {
                bindTo(sourcePreferences.duplicatePinnedSources())
                titleRes = R.string.pref_duplicate_pinned_sources
                summaryRes = R.string.pref_duplicate_pinned_sources_summary
            }
        }

        preferenceCategory {
            titleRes = R.string.label_extensions

            switchPreference {
                bindTo(preferences.automaticExtUpdates())
                titleRes = R.string.pref_enable_automatic_extension_updates

                onChange { newValue ->
                    val checked = newValue as Boolean
                    ExtensionUpdateJob.setupTask(activity!!, checked)
                    true
                }
            }
        }

        preferenceCategory {
            titleRes = R.string.action_global_search

            switchPreference {
                bindTo(sourcePreferences.searchPinnedSourcesOnly())
                titleRes = R.string.pref_search_pinned_sources_only
            }
        }

        preferenceCategory {
            titleRes = R.string.pref_category_nsfw_content

            switchPreference {
                bindTo(sourcePreferences.showNsfwSource())
                titleRes = R.string.pref_show_nsfw_source
                summaryRes = R.string.requires_app_restart

                if (context.isAuthenticationSupported() && activity != null) {
                    requireAuthentication(
                        activity as? FragmentActivity,
                        context.getString(R.string.pref_category_nsfw_content),
                        context.getString(R.string.confirm_lock_change),
                    )
                }
            }

            infoPreference(R.string.parental_controls_info)
        }
    }
}