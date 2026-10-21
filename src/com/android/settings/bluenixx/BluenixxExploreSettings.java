package com.android.settings.bluenixx;

import android.app.settings.SettingsEnums;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@SearchIndexable
public class BluenixxExploreSettings extends DashboardFragment {
    private static final String KEY_FP_ICON = "bluenixx_fp_icon_b64";
    private ActivityResultLauncher<String[]> mPicker;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        mPicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(), this::saveIcon);


    }

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
    }

    @Override
    public boolean onPreferenceTreeClick(Preference p) {
        String key = p.getKey();
        if ("bluenixx_fp_pick".equals(key)) {
            mPicker.launch(new String[]{"image/png"});
            return true;
        } else if ("bluenixx_fp_reset".equals(key)) {
            Settings.System.putString(getContentResolver(), KEY_FP_ICON, null);
            return true;
        }
        return super.onPreferenceTreeClick(p);
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

    private void saveIcon(Uri uri) {
        if (uri == null) return;

        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) {
                throw new java.io.IOException("Não foi possível abrir a imagem");
            }

            Bitmap src = BitmapFactory.decodeStream(in);
            if (src == null) {
                Toast.makeText(getContext(), "PNG inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bmp = Bitmap.createScaledBitmap(src, 128, 128, true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (!bmp.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                Toast.makeText(getContext(), "Falha ao processar PNG", Toast.LENGTH_SHORT).show();
                return;
            }

            String b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
            if (b64.length() > 16000) {
                Toast.makeText(getContext(), "Imagem muito complexa", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean saved = Settings.System.putString(
                    getContentResolver(), KEY_FP_ICON, b64);
            Toast.makeText(
                    getContext(),
                    saved ? "Ícone salvo" : "Falha ao salvar ícone",
                    Toast.LENGTH_SHORT
            ).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "PNG inválido", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.bluenixx_explore_settings;
    }

    @Override
    protected String getLogTag() {
        return "BluenixxExplore";
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.PAGE_UNKNOWN;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.bluenixx_explore_settings);
}
