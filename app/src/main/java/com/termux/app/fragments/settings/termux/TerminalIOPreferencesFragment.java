package com.termux.app.fragments.settings.termux;

import android.content.Context;
import android.app.AlertDialog;
import android.widget.EditText;
import android.text.InputType;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.Preference;

import com.termux.R;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;
import com.termux.shared.termux.settings.properties.TermuxPropertyConstants;
import com.termux.shared.termux.extrakeys.ExtraKeysConstants;
import com.termux.shared.termux.extrakeys.ExtraKeysInfo;

import org.json.JSONException;

@Keep
public class TerminalIOPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setPreferenceDataStore(TerminalIOPreferencesDataStore.getInstance(context));

        setPreferencesFromResource(R.xml.termux_terminal_io_preferences, rootKey);
        Preference layoutPreference = findPreference("custom_extra_keys");
        if (layoutPreference != null) layoutPreference.setOnPreferenceClickListener(preference -> {
            Context ctx = getContext();
            if (ctx == null) return true;
            android.content.SharedPreferences custom = ctx.getSharedPreferences("terminal_ui", Context.MODE_PRIVATE);
            EditText input = new EditText(ctx);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            input.setMinLines(4);
            input.setText(custom.getString("extra_keys", TermuxPropertyConstants.DEFAULT_IVALUE_EXTRA_KEYS));
            new AlertDialog.Builder(ctx).setTitle("Layout tombol Termux (JSON)")
                .setMessage("Gunakan 1–3 baris. Contoh: [['ESC','CTRL'],['LEFT','RIGHT']]")
                .setView(input)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    try {
                        String layout = input.getText().toString();
                        ExtraKeysInfo parsed = new ExtraKeysInfo(layout, "default", ExtraKeysConstants.CONTROL_CHARS_ALIASES);
                        if (parsed.getMatrix().length < 1 || parsed.getMatrix().length > 3) throw new IllegalArgumentException("Jumlah baris harus 1 sampai 3.");
                        custom.edit().putString("extra_keys", layout).apply();
                    } catch (JSONException | IllegalArgumentException e) {
                        new AlertDialog.Builder(ctx).setMessage("Layout tidak valid. Pastikan JSON benar dan jumlah baris maksimal 3.")
                            .setPositiveButton(android.R.string.ok, null).show();
                    }
                }).show();
            return true;
        });
    }

}

class TerminalIOPreferencesDataStore extends PreferenceDataStore {

    private final Context mContext;
    private final TermuxAppSharedPreferences mPreferences;

    private static TerminalIOPreferencesDataStore mInstance;

    private TerminalIOPreferencesDataStore(Context context) {
        mContext = context;
        mPreferences = TermuxAppSharedPreferences.build(context, true);
    }

    public static synchronized TerminalIOPreferencesDataStore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TerminalIOPreferencesDataStore(context);
        }
        return mInstance;
    }



    @Override
    public void putBoolean(String key, boolean value) {
        if (mPreferences == null) return;
        if (key == null) return;

        switch (key) {
            case "soft_keyboard_enabled":
                    mPreferences.setSoftKeyboardEnabled(value);
                break;
            case "soft_keyboard_enabled_only_if_no_hardware":
                mPreferences.setSoftKeyboardEnabledOnlyIfNoHardware(value);
                break;
            case "light_mode":
                mContext.getSharedPreferences("terminal_ui", Context.MODE_PRIVATE).edit().putBoolean("light_mode", value).apply();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        if (mPreferences == null) return false;

        switch (key) {
            case "soft_keyboard_enabled":
                return mPreferences.isSoftKeyboardEnabled();
            case "soft_keyboard_enabled_only_if_no_hardware":
                return mPreferences.isSoftKeyboardEnabledOnlyIfNoHardware();
            case "light_mode":
                return mContext.getSharedPreferences("terminal_ui", Context.MODE_PRIVATE).getBoolean("light_mode", false);
            default:
                return false;
        }
    }

}
