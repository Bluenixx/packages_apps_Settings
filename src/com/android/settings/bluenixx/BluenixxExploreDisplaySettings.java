package com.android.settings.bluenixx;

import android.app.settings.SettingsEnums;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;


@SearchIndexable
public class BluenixxExploreDisplaySettings extends DashboardFragment {


    @Override
    public void onViewCreated(android.view.View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupDynamicBarPreference(
                "bluenixx_dynamic_bar_enabled",
                "bluenixx_dynamic_bar_enabled", true);
        setupDynamicBarPreference(
                "bluenixx_dynamic_bar_media",
                "bluenixx_dynamic_bar_media", true);
        setupDynamicBarPreference(
                "bluenixx_dynamic_bar_notifications",
                "bluenixx_dynamic_bar_notifications", true);
        setupDynamicBarPreference(
                "bluenixx_dynamic_bar_charging",
                "bluenixx_dynamic_bar_charging", true);
        setupSecureWindowsPreference();
    }



    private void setupSecureWindowsPreference() {
        SwitchPreferenceCompat preference =
                findPreference("bluenixx_disable_secure_windows");
        if (preference == null) return;

        preference.setChecked(Settings.Secure.getInt(
                getContentResolver(),
                Settings.Secure.DISABLE_SECURE_WINDOWS, 0) != 0);

        preference.setOnPreferenceChangeListener((pref, newValue) -> {
            if (!(newValue instanceof Boolean)) return false;

            boolean saved = Settings.Secure.putInt(
                    getContentResolver(),
                    Settings.Secure.DISABLE_SECURE_WINDOWS,
                    (Boolean) newValue ? 1 : 0);

            if (!saved) {
                Toast.makeText(getContext(),
                        "Não foi possível salvar a configuração",
                        Toast.LENGTH_SHORT).show();
            }
            return saved;
        });
    }

    private void setupDynamicBarPreference(
            String preferenceKey, String settingKey, boolean defaultValue) {
        SwitchPreferenceCompat preference = findPreference(preferenceKey);
        if (preference == null) return;

        preference.setChecked(Settings.System.getInt(
                getContentResolver(), settingKey, defaultValue ? 1 : 0) != 0);

        preference.setOnPreferenceChangeListener((pref, newValue) -> {
            if (!(newValue instanceof Boolean)) return false;

            boolean enabled = (Boolean) newValue;
            boolean saved = Settings.System.putInt(
                    getContentResolver(), settingKey, enabled ? 1 : 0);

            if (!saved) {
                Toast.makeText(
                        getContext(),
                        "Não foi possível salvar a configuração",
                        Toast.LENGTH_SHORT).show();
            }
            return saved;
        });
    }


    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.bluenixx_explore_display_settings;
    }

    @Override
    protected String getLogTag() {
        return "BluenixxExploreDisplay";
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.PAGE_UNKNOWN;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.bluenixx_explore_display_settings);
}
