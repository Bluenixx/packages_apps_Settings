package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.widget.SliderPreference;

public class BluenixxBlurIntensityPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {
    private static final String KEY_INTENSITY = "bluenixx_blur_intensity";
    private static final int DEFAULT_INTENSITY = 50;

    public BluenixxBlurIntensityPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        SliderPreference slider = screen.findPreference(getPreferenceKey());
        if (slider != null) {
            slider.setMin(0);
            slider.setMax(100);
            slider.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof SliderPreference) {
            ((SliderPreference) preference).setValue(Settings.System.getInt(
                    mContext.getContentResolver(), KEY_INTENSITY, DEFAULT_INTENSITY));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = ((Number) newValue).intValue();
        return Settings.System.putInt(mContext.getContentResolver(), KEY_INTENSITY, value);
    }
}
