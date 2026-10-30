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

/**
 * Quick Settings appearance options for Bluenixx.
 */
@SearchIndexable
public class BluenixxExploreQuickSettings extends DashboardFragment {

    // Added by the Bluenixx ColorOS QS framework integration.
    private static final String QS_PANEL_STYLE = "qs_panel_style";
    private static final int QS_PANEL_STYLE_DEFAULT = 0;
    private static final int QS_PANEL_STYLE_COLOROS = 2;

    @Override
    public void onViewCreated(android.view.View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupColorOSPanelPreference();
    }

    private void setupColorOSPanelPreference() {
        SwitchPreferenceCompat preference =
                findPreference("bluenixx_qs_coloros_style");
        if (preference == null) return;

        preference.setChecked(Settings.System.getInt(
                getContentResolver(), QS_PANEL_STYLE, QS_PANEL_STYLE_DEFAULT)
                == QS_PANEL_STYLE_COLOROS);

        preference.setOnPreferenceChangeListener((Preference pref, Object newValue) -> {
            if (!(newValue instanceof Boolean)) return false;

            int style = (Boolean) newValue
                    ? QS_PANEL_STYLE_COLOROS : QS_PANEL_STYLE_DEFAULT;
            boolean saved = Settings.System.putInt(
                    getContentResolver(), QS_PANEL_STYLE, style);

            if (!saved) {
                Toast.makeText(
                        getContext(),
                        "Não foi possível salvar o estilo do Quick Settings",
                        Toast.LENGTH_SHORT).show();
            }
            return saved;
        });
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.bluenixx_explore_quick_settings;
    }

    @Override
    protected String getLogTag() {
        return "BluenixxExploreQuickSettings";
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.PAGE_UNKNOWN;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.bluenixx_explore_quick_settings);
}
