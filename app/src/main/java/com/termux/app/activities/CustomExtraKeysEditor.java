package com.termux.app.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.termux.shared.termux.extrakeys.ExtraKeysConstants;
import com.termux.shared.termux.extrakeys.ExtraKeysInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Visual editor for Termux's extra-key rows. */
public final class CustomExtraKeysEditor {
    private static final String[][] PALETTE = {
        {"ESC", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12"},
        {"`", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "BKSP", "HOME"},
        {"TAB", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "]", "\\", "PGUP"},
        {"CAPS", "A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'", "ENTER", "PGDN"},
        {"SHIFT", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "/", "SHIFT", "END"},
        {"CTRL", "FN", "ALT", "SPACE", "ALT", "CTRL", "LEFT", "DOWN", "UP", "RIGHT"}
    };

    public interface SaveListener { void onSave(String json); }
    private CustomExtraKeysEditor() {}

    public static void show(Context context, String current, SaveListener listener) {
        ArrayList<ArrayList<String>> rows = parseRows(current);
        int[] selectedRow = {0};
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(context, 16), dp(context, 8), dp(context, 16), dp(context, 8));
        ScrollView scroll = new ScrollView(context);
        scroll.addView(content);
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("Atur tombol keyboard Termux")
            .setView(scroll)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton("Simpan", (d, which) -> listener.onSave(toJson(rows)))
            .create();
        final int panel = Color.rgb(54, 54, 57);
        final int keyBg = Color.rgb(74, 74, 78);
        final int textColor = Color.rgb(238, 238, 242);

        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            int previousScroll = scroll.getScrollY();
            content.removeAllViews();
            TextView instructions = new TextView(context);
            instructions.setTextColor(textColor);
            instructions.setText("Pilih baris, lalu ketuk tombol pada keyboard untuk menambahkannya. Ketuk tombol di baris untuk mengatur posisi atau menghapusnya. Maksimal 3 baris.");
            instructions.setPadding(0, 0, 0, dp(context, 10));
            content.addView(instructions);

            for (int r = 0; r < rows.size(); r++) {
                final int rowIndex = r;
                LinearLayout rowCard = new LinearLayout(context);
                rowCard.setOrientation(LinearLayout.VERTICAL);
                rowCard.setPadding(dp(context, 8), dp(context, 6), dp(context, 8), dp(context, 6));
                rowCard.setBackgroundColor(selectedRow[0] == r ? Color.rgb(78, 78, 83) : Color.TRANSPARENT);
                TextView rowTitle = new TextView(context);
                rowTitle.setTextColor(textColor);
                rowTitle.setText((r + 1) + ".  " + (selectedRow[0] == r ? "dipilih" : "ketuk untuk memilih"));
                rowTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                rowTitle.setOnClickListener(v -> { selectedRow[0] = rowIndex; render[0].run(); });
                rowCard.addView(rowTitle);
                HorizontalScrollView rowScroll = new HorizontalScrollView(context);
                LinearLayout keyLine = new LinearLayout(context);
                keyLine.setGravity(Gravity.CENTER_VERTICAL);
                for (int i = 0; i < rows.get(r).size(); i++) {
                    final int keyIndex = i;
                    MaterialButton key = new MaterialButton(context);
                    key.setText(displayItem(rows.get(r).get(i)));
                    key.setTextColor(textColor);
                    key.setBackgroundTintList(android.content.res.ColorStateList.valueOf(keyBg));
                    key.setRippleColor(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
                    key.setTextSize(12);
                    key.setMinWidth(dp(context, 42));
                    key.setMinimumWidth(dp(context, 42));
                    key.setInsetTop(0); key.setInsetBottom(0);
                    key.setOnClickListener(v -> showKeyActions(context, rows, rowIndex, keyIndex, render[0]));
                    keyLine.addView(key);
                }
                rowScroll.addView(keyLine);
                rowCard.addView(rowScroll);
                rowCard.setOnClickListener(v -> { selectedRow[0] = rowIndex; render[0].run(); });
                content.addView(rowCard);
            }

            Button addRow = new Button(context);
            addRow.setTextColor(textColor);
            addRow.setBackgroundTintList(android.content.res.ColorStateList.valueOf(keyBg));
            addRow.setText(rows.size() < 3 ? "+ Tambah baris" : "Maksimal 3 baris");
            addRow.setEnabled(rows.size() < 3);
            addRow.setOnClickListener(v -> {
                if (rows.size() < 3) { rows.add(new ArrayList<>()); selectedRow[0] = rows.size() - 1; render[0].run(); }
            });
            content.addView(addRow);

            TextView paletteTitle = new TextView(context);
            paletteTitle.setTextColor(textColor);
            paletteTitle.setText("KEYBOARD  ·  ketuk untuk menambah ke baris " + (selectedRow[0] + 1));
            paletteTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            paletteTitle.setPadding(0, dp(context, 14), 0, dp(context, 4));
            content.addView(paletteTitle);
            for (String[] paletteRow : PALETTE) {
                HorizontalScrollView paletteScroll = new HorizontalScrollView(context);
                paletteScroll.setHorizontalScrollBarEnabled(false);
                LinearLayout keys = new LinearLayout(context);
                keys.setGravity(Gravity.CENTER_VERTICAL);
                for (String value : paletteRow) {
                    MaterialButton key = new MaterialButton(context);
                    key.setText(value);
                    key.setContentDescription("Tambahkan tombol " + value + " ke baris terpilih");
                    key.setTextColor(textColor);
                    key.setBackgroundTintList(android.content.res.ColorStateList.valueOf(keyBg));
                    key.setRippleColor(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
                    key.setTextSize(10);
                    key.setMinWidth(dp(context, 48)); key.setMinimumWidth(dp(context, 48));
                    key.setInsetTop(0); key.setInsetBottom(0);
                    key.setOnClickListener(v -> { rows.get(selectedRow[0]).add(value); render[0].run(); });
                    LinearLayout.LayoutParams keyParams = new LinearLayout.LayoutParams(dp(context, 58), dp(context, 44));
                    keyParams.setMargins(dp(context, 2), dp(context, 1), dp(context, 2), dp(context, 1));
                    keys.addView(key, keyParams);
                }
                paletteScroll.addView(keys);
                content.addView(paletteScroll);
            }
            Button customKey = new Button(context);
            customKey.setTextColor(textColor);
            customKey.setBackgroundTintList(android.content.res.ColorStateList.valueOf(keyBg));
            customKey.setText("+ Tombol / macro khusus");
            customKey.setOnClickListener(v -> {
                LinearLayout fields = new LinearLayout(context);
                fields.setPadding(dp(context, 20), 0, dp(context, 20), 0);
                fields.setOrientation(LinearLayout.VERTICAL);
                EditText label = new EditText(context);
                label.setSingleLine(true);
                label.setHint("Label tombol, contoh: C-q");
                EditText binding = new EditText(context);
                binding.setSingleLine(true);
                binding.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                binding.setHint("Tombol / urutan, contoh: CTRL C q");
                fields.addView(label);
                fields.addView(binding);
                new AlertDialog.Builder(context).setTitle("Bind tombol Termux").setView(fields)
                    .setMessage("Gunakan nama key Termux dipisah spasi. Contoh CTRL C q mengirim Ctrl+C lalu q.")
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton("Tambah", (d, w) -> {
                        String shown = label.getText().toString().trim();
                        String sequence = binding.getText().toString().trim();
                        if (shown.isEmpty()) shown = sequence;
                        if (!shown.isEmpty() && !sequence.isEmpty()) {
                            try {
                                JSONObject macro = new JSONObject();
                                macro.put("macro", sequence);
                                macro.put("display", shown);
                                rows.get(selectedRow[0]).add(macro.toString());
                                render[0].run();
                            } catch (JSONException ignored) { }
                        }
                    }).show();
            });
            content.addView(customKey);
            TextView holdNote = new TextView(context);
            holdNote.setTextColor(textColor);
            holdNote.setText("CTRL / ALT / SHIFT: ketuk untuk aktifkan; tekan lama untuk mengunci. Saat aktif, warnanya kuning.");
            holdNote.setPadding(0, dp(context, 10), 0, 0);
            content.addView(holdNote);
            scroll.post(() -> scroll.scrollTo(0, previousScroll));
        };
        render[0].run();
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(textColor);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textColor);
        scroll.setBackgroundColor(panel);
        content.setBackgroundColor(panel);
        dialog.getWindow().setLayout((int)(context.getResources().getDisplayMetrics().widthPixels * 0.94f), (int)(context.getResources().getDisplayMetrics().heightPixels * 0.82f));
    }

    private static void showKeyActions(Context context, ArrayList<ArrayList<String>> rows, int row, int index, Runnable render) {
        String[] actions = {"Ganti dengan tombol…", "Buat shortcut / macro…", "Geser ke kiri", "Geser ke kanan", "Hapus"};
        new AlertDialog.Builder(context).setTitle(displayItem(rows.get(row).get(index))).setItems(actions, (dialog, which) -> {
            ArrayList<String> keys = rows.get(row);
            if (which == 0) showKeyPicker(context, rows, row, index, false, render);
            else if (which == 1) showKeyPicker(context, rows, row, index, true, render);
            else if (which == 2 && index > 0) { String value = keys.remove(index); keys.add(index - 1, value); render.run(); }
            else if (which == 3 && index < keys.size() - 1) { String value = keys.remove(index); keys.add(index + 1, value); render.run(); }
            else if (which == 4) { keys.remove(index); render.run(); }
        }).show();
    }

    /** A full, scrollable desktop-keyboard picker for replacing a key or composing a macro. */
    private static void showKeyPicker(Context context, ArrayList<ArrayList<String>> rows, int row, int index,
                                      boolean macro, Runnable render) {
        int dark = Color.rgb(54, 54, 57), keyColor = Color.rgb(76, 76, 81), selectedColor = Color.rgb(89, 113, 139);
        int foreground = Color.rgb(242, 242, 246);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(context, 12), dp(context, 8), dp(context, 12), dp(context, 8));
        ScrollView scroll = new ScrollView(context);
        LinearLayout keyboard = new LinearLayout(context);
        keyboard.setOrientation(LinearLayout.VERTICAL);
        ArrayList<String> selected = new ArrayList<>();
        AlertDialog[] pickerRef = {null};
        for (String[] keyRow : PALETTE) {
            HorizontalScrollView horizontal = new HorizontalScrollView(context);
            horizontal.setHorizontalScrollBarEnabled(false);
            LinearLayout line = new LinearLayout(context);
            for (String value : keyRow) {
                MaterialButton key = new MaterialButton(context);
                key.setText(value);
                key.setContentDescription("Pilih tombol " + value);
                key.setTextSize(11);
                key.setTextColor(foreground);
                key.setMinWidth(dp(context, 50)); key.setMinimumWidth(dp(context, 50));
                key.setInsetTop(0); key.setInsetBottom(0);
                key.setBackgroundTintList(android.content.res.ColorStateList.valueOf(keyColor));
                key.setRippleColor(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(context, 60), dp(context, 48));
                params.setMargins(dp(context, 2), dp(context, 2), dp(context, 2), dp(context, 2));
                key.setOnClickListener(v -> {
                    if (!macro) {
                        rows.get(row).set(index, value);
                        render.run();
                        if (pickerRef[0] != null) pickerRef[0].dismiss();
                    } else if (selected.contains(value)) {
                        selected.remove(value);
                        key.setBackgroundTintList(android.content.res.ColorStateList.valueOf(keyColor));
                    } else {
                        selected.add(value);
                        key.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedColor));
                    }
                });
                line.addView(key, params);
            }
            horizontal.addView(line);
            keyboard.addView(horizontal);
        }
        scroll.addView(keyboard);
        layout.addView(scroll, new LinearLayout.LayoutParams(-1, dp(context, 340)));
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
            .setTitle(macro ? "Pilih urutan shortcut" : "Pilih tombol pengganti")
            .setView(layout)
            .setNegativeButton("Batal", null);
        if (macro) {
            EditText name = new EditText(context);
            name.setSingleLine(true);
            name.setTextColor(foreground);
            name.setHintTextColor(Color.LTGRAY);
            name.setHint("Nama tombol, misalnya Paste");
            name.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.LTGRAY));
            layout.addView(name, new LinearLayout.LayoutParams(-1, dp(context, 56)));
            builder.setMessage("Pilih beberapa tombol berurutan, misalnya CTRL lalu V.")
                .setPositiveButton("Buat shortcut", (d, w) -> {
                    if (selected.isEmpty()) return;
                    String sequence = android.text.TextUtils.join(" ", selected);
                    String label = name.getText().toString().trim();
                    if (label.isEmpty()) label = sequence;
                    try {
                        JSONObject item = new JSONObject(); item.put("macro", sequence); item.put("display", label);
                        rows.get(row).set(index, item.toString()); render.run();
                    } catch (JSONException ignored) { }
                });
        }
        AlertDialog picker = builder.create();
        pickerRef[0] = picker;
        picker.setOnShowListener(d -> {
            if (picker.getWindow() != null) picker.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            picker.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(foreground);
            if (picker.getButton(AlertDialog.BUTTON_POSITIVE) != null) picker.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(foreground);
            layout.setBackgroundColor(dark);
        });
        picker.show();
    }

    private static ArrayList<ArrayList<String>> parseRows(String source) {
        try {
            ExtraKeysInfo info = new ExtraKeysInfo(source, "default", ExtraKeysConstants.CONTROL_CHARS_ALIASES);
            ArrayList<ArrayList<String>> rows = new ArrayList<>();
            for (com.termux.shared.termux.extrakeys.ExtraKeyButton[] inputRow : info.getMatrix()) {
                ArrayList<String> row = new ArrayList<>();
                for (com.termux.shared.termux.extrakeys.ExtraKeyButton key : inputRow) {
                    if (key.isMacro()) {
                        JSONObject macro = new JSONObject();
                        macro.put("macro", key.getKey());
                        macro.put("display", key.getDisplay());
                        row.add(macro.toString());
                    } else row.add(key.getKey());
                }
                rows.add(row);
            }
            return rows;
        } catch (JSONException e) {
            ArrayList<ArrayList<String>> rows = new ArrayList<>();
            rows.add(new ArrayList<>(Arrays.asList("ESC", "CTRL", "ALT", "LEFT", "DOWN", "RIGHT", "PGDN")));
            return rows;
        }
    }

    private static String toJson(List<ArrayList<String>> rows) {
        JSONArray matrix = new JSONArray();
        for (ArrayList<String> row : rows) {
            JSONArray keys = new JSONArray();
            for (String key : row) {
                if (key.startsWith("{")) {
                    try { keys.put(new JSONObject(key)); }
                    catch (JSONException e) { keys.put(key); }
                } else keys.put(key);
            }
            matrix.put(keys);
        }
        return matrix.toString();
    }

    private static String displayItem(String item) {
        if (item.startsWith("{")) {
            try {
                JSONObject macro = new JSONObject(item);
                return macro.optString("display", macro.optString("macro", item));
            } catch (JSONException ignored) { }
        }
        return displayKey(item);
    }

    private static String displayKey(String key) {
        switch (key) {
            case "LEFT": return "←";
            case "RIGHT": return "→";
            case "UP": return "↑";
            case "DOWN": return "↓";
            case "BKSP": return "⌫";
            case "SPACE": return "␣";
            default: return key;
        }
    }

    private static int dp(Context context, int value) {
        return (int)(value * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
