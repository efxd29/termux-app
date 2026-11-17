package com.termux.app.terminal.io;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.termux.app.TermuxActivity;
import com.termux.app.terminal.TermuxTerminalSessionActivityClient;
import com.termux.app.terminal.TermuxTerminalViewClient;
import com.termux.shared.logger.Logger;
import com.termux.shared.termux.extrakeys.ExtraKeysConstants;
import com.termux.shared.termux.extrakeys.ExtraKeysInfo;
import com.termux.shared.termux.settings.properties.TermuxPropertyConstants;
import com.termux.shared.termux.settings.properties.TermuxSharedProperties;
import com.termux.shared.termux.terminal.io.TerminalExtraKeys;
import com.termux.view.TerminalView;

import org.json.JSONArray;
import org.json.JSONException;

public class TermuxTerminalExtraKeys extends TerminalExtraKeys {

    private ExtraKeysInfo mExtraKeysInfo;

    final TermuxActivity mActivity;
    final TermuxTerminalViewClient mTermuxTerminalViewClient;
    final TermuxTerminalSessionActivityClient mTermuxTerminalSessionActivityClient;

    private static final String LOG_TAG = "TermuxTerminalExtraKeys";

    private static final String KEY_SELECT_LAST_EXECUTION = "LAST RUN";
    private static final String KEY_COPY_LAST_EXECUTION = "COPY RUN";
    private static final String KEY_SELECT_ALL_TRANSCRIPT = "SELECT ALL";
    private static final String KEY_COPY_ALL_TRANSCRIPT = "COPY ALL";

    public TermuxTerminalExtraKeys(TermuxActivity activity, @NonNull TerminalView terminalView,
                                   TermuxTerminalViewClient termuxTerminalViewClient,
                                   TermuxTerminalSessionActivityClient termuxTerminalSessionActivityClient) {
        super(terminalView);

        mActivity = activity;
        mTermuxTerminalViewClient = termuxTerminalViewClient;
        mTermuxTerminalSessionActivityClient = termuxTerminalSessionActivityClient;

        setExtraKeys();
    }


    /**
     * Set the terminal extra keys and style.
     */
    private void setExtraKeys() {
        mExtraKeysInfo = null;

        try {
            // The mMap stores the extra key and style string values while loading properties
            // Check {@link #getExtraKeysInternalPropertyValueFromValue(String)} and
            // {@link #getExtraKeysStyleInternalPropertyValueFromValue(String)}
            String extrakeys = appendCommandActionKeys((String) mActivity.getProperties().getInternalPropertyValue(TermuxPropertyConstants.KEY_EXTRA_KEYS, true));
            String extraKeysStyle = (String) mActivity.getProperties().getInternalPropertyValue(TermuxPropertyConstants.KEY_EXTRA_KEYS_STYLE, true);

            ExtraKeysConstants.ExtraKeyDisplayMap extraKeyDisplayMap = ExtraKeysInfo.getCharDisplayMapForStyle(extraKeysStyle);
            if (ExtraKeysConstants.EXTRA_KEY_DISPLAY_MAPS.DEFAULT_CHAR_DISPLAY.equals(extraKeyDisplayMap) && !TermuxPropertyConstants.DEFAULT_IVALUE_EXTRA_KEYS_STYLE.equals(extraKeysStyle)) {
                Logger.logError(TermuxSharedProperties.LOG_TAG, "The style \"" + extraKeysStyle + "\" for the key \"" + TermuxPropertyConstants.KEY_EXTRA_KEYS_STYLE + "\" is invalid. Using default style instead.");
                extraKeysStyle = TermuxPropertyConstants.DEFAULT_IVALUE_EXTRA_KEYS_STYLE;
            }

            mExtraKeysInfo = new ExtraKeysInfo(extrakeys, extraKeysStyle, ExtraKeysConstants.CONTROL_CHARS_ALIASES);
        } catch (JSONException e) {
            Logger.showToast(mActivity, "Could not load and set the \"" + TermuxPropertyConstants.KEY_EXTRA_KEYS + "\" property from the properties file: " + e.toString(), true);
            Logger.logStackTraceWithMessage(LOG_TAG, "Could not load and set the \"" + TermuxPropertyConstants.KEY_EXTRA_KEYS + "\" property from the properties file: ", e);

            try {
                mExtraKeysInfo = new ExtraKeysInfo(appendCommandActionKeys(TermuxPropertyConstants.DEFAULT_IVALUE_EXTRA_KEYS), TermuxPropertyConstants.DEFAULT_IVALUE_EXTRA_KEYS_STYLE, ExtraKeysConstants.CONTROL_CHARS_ALIASES);
            } catch (JSONException e2) {
                Logger.showToast(mActivity, "Can't create default extra keys",true);
                Logger.logStackTraceWithMessage(LOG_TAG, "Could create default extra keys: ", e);
                mExtraKeysInfo = null;
            }
        }
    }

    /**
     * Append a dedicated action row while preserving every user-configured extra key.
     */
    private String appendCommandActionKeys(String configuredKeys) {
        try {
            JSONArray rows = new JSONArray(configuredKeys);
            JSONArray actionRow = new JSONArray();
            actionRow.put(KEY_SELECT_LAST_EXECUTION);
            actionRow.put(KEY_COPY_LAST_EXECUTION);
            actionRow.put(KEY_SELECT_ALL_TRANSCRIPT);
            actionRow.put(KEY_COPY_ALL_TRANSCRIPT);
            rows.put(actionRow);
            return rows.toString();
        } catch (JSONException e) {
            Logger.logStackTraceWithMessage(LOG_TAG, "Could not append execution action keys: ", e);
            return configuredKeys;
        }
    }

    private int[] findLastExecutionRows(TerminalView terminalView) {
        if (terminalView == null || terminalView.mEmulator == null) return null;

        com.termux.terminal.TerminalBuffer buffer = terminalView.mEmulator.getScreen();
        int firstRow = -buffer.getActiveTranscriptRows();
        int lastRow = terminalView.mEmulator.mRows - 1;
        int columns = terminalView.mEmulator.mColumns;
        java.util.ArrayList<Integer> promptRows = new java.util.ArrayList<>();

        for (int row = firstRow; row <= lastRow; row++) {
            String line = buffer.getSelectedText(0, row, columns - 1, row, false, false).trim();
            if (isShellPromptLine(line)) promptRows.add(row);
        }

        if (promptRows.isEmpty()) return null;

        int startRow;
        int endRow;
        if (promptRows.size() >= 2) {
            startRow = promptRows.get(promptRows.size() - 2);
            endRow = promptRows.get(promptRows.size() - 1) - 1;
        } else {
            startRow = promptRows.get(promptRows.size() - 1);
            endRow = lastRow;
        }

        if (endRow < startRow) endRow = startRow;
        return new int[] { startRow, endRow };
    }

    /**
     * Detect the common Termux/bash prompt shapes without assuming one fixed username or path.
     * This is intentionally limited to prompt-like rows and is not a shell command parser.
     */
    private boolean isShellPromptLine(String line) {
        if (line == null || line.isEmpty()) return false;
        return line.matches("^(?:.*(?:~|/[^\\s]*)\\s*)[$#>]\\s*.*$")
            || line.matches("^[^\\s]+@[^\\s:]+:.*[$#]\\s*.*$");
    }

    private String getLastExecutionText(TerminalView terminalView, int[] rows) {
        if (rows == null || terminalView == null || terminalView.mEmulator == null) return null;
        int columns = terminalView.mEmulator.mColumns;
        return terminalView.mEmulator.getScreen().getSelectedText(0, rows[0], columns - 1, rows[1], false, false).trim();
    }

    private void selectLastExecution() {
        TerminalView terminalView = mActivity.getTerminalView();
        int[] rows = findLastExecutionRows(terminalView);
        if (rows == null) {
            mActivity.showToast("Could not detect the last command prompt", false);
            return;
        }
        terminalView.selectTextRange(0, rows[0], terminalView.mEmulator.mColumns - 1, rows[1]);
    }

    private void copyLastExecution() {
        TerminalView terminalView = mActivity.getTerminalView();
        int[] rows = findLastExecutionRows(terminalView);
        String text = getLastExecutionText(terminalView, rows);
        if (text == null || text.isEmpty()) {
            mActivity.showToast("No last execution text found", false);
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Termux last execution", text));
            mActivity.showToast("Last execution copied", false);
        }
    }

    private void selectAllTranscript() {
        TerminalView terminalView = mActivity.getTerminalView();
        if (terminalView == null || terminalView.mEmulator == null) return;
        com.termux.terminal.TerminalBuffer buffer = terminalView.mEmulator.getScreen();
        terminalView.selectTextRange(0, -buffer.getActiveTranscriptRows(),
            terminalView.mEmulator.mColumns - 1, terminalView.mEmulator.mRows - 1);
    }

    private void copyAllTranscript() {
        TerminalView terminalView = mActivity.getTerminalView();
        if (terminalView == null || terminalView.mEmulator == null) return;
        String text = terminalView.mEmulator.getScreen().getTranscriptText();
        ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Termux transcript", text));
            mActivity.showToast("Terminal transcript copied", false);
        }
    }

    public ExtraKeysInfo getExtraKeysInfo() {
        return mExtraKeysInfo;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onTerminalExtraKeyButtonClick(View view, String key, boolean ctrlDown, boolean altDown, boolean shiftDown, boolean fnDown) {
        if (KEY_SELECT_LAST_EXECUTION.equals(key)) {
            selectLastExecution();
        } else if (KEY_COPY_LAST_EXECUTION.equals(key)) {
            copyLastExecution();
        } else if (KEY_SELECT_ALL_TRANSCRIPT.equals(key)) {
            selectAllTranscript();
        } else if (KEY_COPY_ALL_TRANSCRIPT.equals(key)) {
            copyAllTranscript();
        } else if ("KEYBOARD".equals(key)) {
            if(mTermuxTerminalViewClient != null)
                mTermuxTerminalViewClient.onToggleSoftKeyboardRequest();
        } else if ("DRAWER".equals(key)) {
            DrawerLayout drawerLayout = mTermuxTerminalViewClient.getActivity().getDrawer();
            if (drawerLayout.isDrawerOpen(Gravity.LEFT))
                drawerLayout.closeDrawer(Gravity.LEFT);
            else
                drawerLayout.openDrawer(Gravity.LEFT);
        } else if ("PASTE".equals(key)) {
            if(mTermuxTerminalSessionActivityClient != null)
                mTermuxTerminalSessionActivityClient.onPasteTextFromClipboard(null);
        }  else if ("SCROLL".equals(key)) {
            TerminalView terminalView = mTermuxTerminalViewClient.getActivity().getTerminalView();
            if (terminalView != null && terminalView.mEmulator != null)
                terminalView.mEmulator.toggleAutoScrollDisabled();
        } else {
            super.onTerminalExtraKeyButtonClick(view, key, ctrlDown, altDown, shiftDown, fnDown);
        }
    }

}
