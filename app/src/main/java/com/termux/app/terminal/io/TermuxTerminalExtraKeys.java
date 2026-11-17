package com.termux.app.terminal.io;

import android.annotation.SuppressLint;
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
import com.termux.terminal.TerminalBuffer;
import com.termux.view.TerminalView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TermuxTerminalExtraKeys extends TerminalExtraKeys {

    private ExtraKeysInfo mExtraKeysInfo;

    final TermuxActivity mActivity;
    final TermuxTerminalViewClient mTermuxTerminalViewClient;
    final TermuxTerminalSessionActivityClient mTermuxTerminalSessionActivityClient;

    private static final String LOG_TAG = "TermuxTerminalExtraKeys";

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
            String extrakeys = (String) mActivity.getProperties().getInternalPropertyValue(TermuxPropertyConstants.KEY_EXTRA_KEYS, true);
            String extraKeysStyle = (String) mActivity.getProperties().getInternalPropertyValue(TermuxPropertyConstants.KEY_EXTRA_KEYS_STYLE, true);

            // Append built-in terminal actions in memory; do not modify the user's properties file.
            JSONArray keysMatrix = new JSONArray(extrakeys);
            JSONArray actionRow = new JSONArray();
            actionRow.put(new JSONObject().put("key", "LAST_RUN").put("display", "LAST RUN"));
            actionRow.put(new JSONObject().put("key", "COPY_RUN").put("display", "COPY RUN"));
            actionRow.put(new JSONObject().put("key", "SELECT_ALL").put("display", "SELECT ALL"));
            actionRow.put(new JSONObject().put("key", "COPY_ALL").put("display", "COPY ALL"));
            keysMatrix.put(actionRow);
            extrakeys = keysMatrix.toString();

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
                mExtraKeysInfo = new ExtraKeysInfo(TermuxPropertyConstants.DEFAULT_IVALUE_EXTRA_KEYS, TermuxPropertyConstants.DEFAULT_IVALUE_EXTRA_KEYS_STYLE, ExtraKeysConstants.CONTROL_CHARS_ALIASES);
            } catch (JSONException e2) {
                Logger.showToast(mActivity, "Can't create default extra keys",true);
                Logger.logStackTraceWithMessage(LOG_TAG, "Could create default extra keys: ", e);
                mExtraKeysInfo = null;
            }
        }
    }

    public ExtraKeysInfo getExtraKeysInfo() {
        return mExtraKeysInfo;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onTerminalExtraKeyButtonClick(View view, String key, boolean ctrlDown, boolean altDown, boolean shiftDown, boolean fnDown) {
        if ("KEYBOARD".equals(key)) {
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
        } else if ("LAST_RUN".equals(key) || "COPY_RUN".equals(key) ||
                   "SELECT_ALL".equals(key) || "COPY_ALL".equals(key)) {
            TerminalView terminalView = mTermuxTerminalViewClient.getActivity().getTerminalView();
            handleTerminalAction(terminalView, key);
        } else {
            super.onTerminalExtraKeyButtonClick(view, key, ctrlDown, altDown, shiftDown, fnDown);
        }
    }

    private void handleTerminalAction(TerminalView terminalView, String action) {
        if (terminalView == null || terminalView.mEmulator == null ||
            terminalView.mTermSession == null) return;

        TerminalBuffer screen = terminalView.mEmulator.getScreen();
        int firstRow = -screen.getActiveTranscriptRows();
        int lastRow = terminalView.mEmulator.mRows - 1;

        if ("SELECT_ALL".equals(action)) {
            terminalView.selectTextRange(firstRow, lastRow);
            return;
        }

        if ("COPY_ALL".equals(action)) {
            terminalView.mTermSession.onCopyTextToClipboard(screen.getTranscriptText());
            return;
        }

        int[] lastRun = findLastRunRange(terminalView, screen);
        if (lastRun == null) {
            Logger.showToast(mActivity, "Could not detect the last command from the shell prompt", true);
            return;
        }

        if ("LAST_RUN".equals(action)) {
            terminalView.selectTextRange(lastRun[0], lastRun[1]);
        } else {
            String text = screen.getSelectedText(0, lastRun[0],
                terminalView.mEmulator.mColumns - 1, lastRun[1]).trim();
            terminalView.mTermSession.onCopyTextToClipboard(text);
        }
    }

    /**
     * Locate the latest command line using common shell prompt markers, then
     * include its output up to the start of the next empty prompt.
     */
    private int[] findLastRunRange(TerminalView terminalView, TerminalBuffer screen) {
        int minRow = -screen.getActiveTranscriptRows();
        int endRow = Math.min(terminalView.mEmulator.mRows - 1,
            terminalView.mEmulator.getCursorRow());

        String currentLine = screen.getSelectedText(0, endRow,
            terminalView.mEmulator.mColumns - 1, endRow).trim();
        if (currentLine.matches(".*(?:\\$|#|%|❯|➜|>)\\s*$")) endRow--;

        for (int row = endRow; row >= minRow; row--) {
            String line = screen.getSelectedText(0, row,
                terminalView.mEmulator.mColumns - 1, row).trim();
            if (line.matches(".*(?:\\$|#|%|❯|➜|>)\\s+\\S.*")) {
                return new int[]{row, Math.max(row, endRow)};
            }
        }
        return null;
    }

}
