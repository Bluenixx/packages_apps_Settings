/*
 * Copyright (C) 2025 crDroid Android Project
 * Copyright (C) 2026 Project Sakura
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.firmwareversion

import android.content.Context
import android.os.Build
import android.os.SystemProperties
import androidx.preference.Preference
import com.android.settings.R
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.preference.PreferenceBinding

class CiRCLEMaintainerPreference :
    PreferenceMetadata,
    PreferenceSummaryProvider,
    PreferenceAvailabilityProvider,
    PreferenceBinding {

    override val key: String
        get() = "maintainer"

    override val title: Int
        get() = R.string.maintainer

    override fun getSummary(context: Context): CharSequence {
        val deviceMaintainer = SystemProperties.get(
            ROM_MAINTAINER,
            context.getString(R.string.device_info_default)
        )
        return "$deviceMaintainer"
    }

    override fun isAvailable(context: Context) = true

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        // Match old XML: enableCopying="true" and default selectable
        preference.isCopyingEnabled = true
    }

    companion object {
        const val ROM_MAINTAINER = "ro.circle.maintainer"
    }
}
