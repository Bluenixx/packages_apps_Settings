package com.android.settings.bluenixx;

import android.app.ActivityManager;
import android.app.settings.SettingsEnums;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.content.Intent;
import java.io.File;
import java.io.FileOutputStream;
import androidx.core.content.FileProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.json.JSONObject;

@SearchIndexable
public class BluenixxExploreLockScreenSettings extends DashboardFragment {
    private static final String KEY_FP_ICON = "bluenixx_fp_icon_b64";
    private ActivityResultLauncher<String[]> mPicker;
    private ActivityResultLauncher<String[]> mFontPicker;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        mPicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(), this::saveIcon);
        mFontPicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(), this::saveClockFont);


    }

    @Override
    public void onViewCreated(android.view.View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference p) {
        String key = p.getKey();
        if ("bluenixx_fp_pick".equals(key)) {
            mPicker.launch(new String[]{"image/png"});
            return true;
        } else if ("bluenixx_clock_font_pick".equals(key)) {
            mFontPicker.launch(new String[]{"font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-opentype", "*/*"});
            return true;
        } else if ("bluenixx_clock_reset".equals(key)) {
            boolean saved = false;
            try {
                int userId = ActivityManager.getCurrentUser();
                String current = Settings.Secure.getStringForUser(
                        getContentResolver(),
                        Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_FACE,
                        userId);
                if (current == null || current.isBlank()) {
                    saved = true;
                } else {
                    JSONObject root = new JSONObject(current);
                    JSONObject metadata = root.optJSONObject("metadata");
                    if (metadata != null) {
                        metadata.remove("bluenixx_clock_font");
                        root.put("metadata", metadata);
                    }
                    saved = Settings.Secure.putStringForUser(
                            getContentResolver(),
                            Settings.Secure.LOCK_SCREEN_CUSTOM_CLOCK_FACE,
                            root.toString(),
                            userId);
                }
            } catch (Exception e) {
                android.util.Log.e(
                        "BluenixxExploreLockScreen",
                        "Falha ao redefinir fonte do relógio", e);
            }
            Toast.makeText(
                    getContext(),
                    saved ? "Fonte personalizada removida"
                            : "Não foi possível redefinir a fonte",
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        else if ("bluenixx_fp_reset".equals(key)) {
            Settings.System.putString(getContentResolver(), KEY_FP_ICON, null);
            return true;
        }
        return super.onPreferenceTreeClick(p);
    }

    private void saveClockFont(Uri uri) {
        if (uri == null) return;

        File cachedFont = new File(requireContext().getCacheDir(), "bluenixx_clock_font.ttf");
        File tempFont = new File(requireContext().getCacheDir(), "bluenixx_clock_font.tmp");

        try (InputStream in = getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(tempFont)) {
            if (in == null) {
                throw new java.io.IOException("Fonte indisponível");
            }

            byte[] buffer = new byte[8192];
            int read;
            long total = 0;
            while ((read = in.read(buffer)) != -1) {
                total += read;
                if (total > 20L * 1024 * 1024) {
                    throw new java.io.IOException("Fonte maior que 20 MB");
                }
                out.write(buffer, 0, read);
            }
        } catch (Exception e) {
            tempFont.delete();
            Toast.makeText(getContext(), "Não foi possível importar a fonte",
                    Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Typeface.createFromFile(tempFont);
            java.nio.file.Files.move(
                    tempFont.toPath(),
                    cachedFont.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            tempFont.delete();
            Toast.makeText(getContext(), "Arquivo inválido ou falha ao salvar a fonte",
                    Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Uri sharedUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".files",
                    cachedFont);
            Intent intent = new Intent(
                    "com.android.systemui.action.SET_BLUENIXX_CLOCK_FONT");
            intent.setPackage("com.android.systemui");
            intent.setData(sharedUri);
            intent.setClipData(android.content.ClipData.newUri(
                    requireContext().getContentResolver(), "clock-font", sharedUri));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            requireContext().sendBroadcast(intent);
            Toast.makeText(getContext(),
                    "Solicitação de importação enviada. Se a fonte não aparecer, verifique o log do SystemUI.",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Falha ao enviar a fonte",
                    Toast.LENGTH_LONG).show();
        }
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
        return R.xml.bluenixx_explore_lock_screen_settings;
    }

    @Override
    protected String getLogTag() {
        return "BluenixxExploreLockScreen";
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.PAGE_UNKNOWN;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.bluenixx_explore_lock_screen_settings);
}
