package com.litepdf.reader.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user preferences such as view mode, sort order, dark mode,
 * scroll mode, and saved reading positions.
 */
public class PreferenceManager {

    private static final String PREF_NAME = "litepdf_prefs";

    public static final int VIEW_MODE_GRID = 0;
    public static final int VIEW_MODE_LIST = 1;

    public static final int SCROLL_MODE_VERTICAL = 0;
    public static final int SCROLL_MODE_PAGE = 1;

    private static final String KEY_VIEW_MODE = "view_mode";
    private static final String KEY_SORT_MODE = "sort_mode";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_SCROLL_MODE = "scroll_mode";
    private static final String KEY_LAST_FOLDER = "last_selected_folder";
    private static final String PREFIX_PAGE_POSITION = "page_pos_";

    private final SharedPreferences prefs;

    private static PreferenceManager sInstance;

    public static synchronized PreferenceManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceManager(context.getApplicationContext());
        }
        return sInstance;
    }

    private PreferenceManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getViewMode() {
        return prefs.getInt(KEY_VIEW_MODE, VIEW_MODE_GRID);
    }

    public void setViewMode(int viewMode) {
        prefs.edit().putInt(KEY_VIEW_MODE, viewMode).apply();
    }

    public int getSortMode() {
        return prefs.getInt(KEY_SORT_MODE, SortUtils.SORT_NAME_ASC);
    }

    public void setSortMode(int sortMode) {
        prefs.edit().putInt(KEY_SORT_MODE, sortMode).apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public int getScrollMode() {
        return prefs.getInt(KEY_SCROLL_MODE, SCROLL_MODE_VERTICAL);
    }

    public void setScrollMode(int scrollMode) {
        prefs.edit().putInt(KEY_SCROLL_MODE, scrollMode).apply();
    }

    public String getLastSelectedFolder() {
        return prefs.getString(KEY_LAST_FOLDER, null);
    }

    public void setLastSelectedFolder(String folderPath) {
        prefs.edit().putString(KEY_LAST_FOLDER, folderPath).apply();
    }

    public void saveReadingPosition(String filePath, int page) {
        if (filePath != null) {
            prefs.edit().putInt(PREFIX_PAGE_POSITION + filePath, page).apply();
        }
    }

    public int getReadingPosition(String filePath) {
        if (filePath == null) return 0;
        return prefs.getInt(PREFIX_PAGE_POSITION + filePath, 0);
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
