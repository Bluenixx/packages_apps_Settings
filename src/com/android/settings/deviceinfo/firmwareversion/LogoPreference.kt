package com.android.settings.deviceinfo.firmwareversion

import android.content.Context
import androidx.preference.Preference
import com.android.settings.R
import com.android.settingslib.metadata.preferencesapi.preconditions.PreconditionStability
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.preference.PreferenceBinding

class LogoPreference :
    PreferenceMetadata,
    PreferenceAvailabilityProvider,
    PreferenceBinding {

    override val key: String
        get() = "circle_logo"

    override val purpose: Int
        get() = R.string.circle_logo_purpose

    // No title for this item
    override val title: Int
        get() = 0

    override fun getAvailabilityStability() = PreconditionStability.STABLE_UNTIL_APK_UPDATE

    override val availabilityDescription: String
        get() = "Always available"

    override fun isAvailable(context: Context) = true

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.layoutResource = R.layout.circle_logo
        preference.isSelectable = false
    }
}
