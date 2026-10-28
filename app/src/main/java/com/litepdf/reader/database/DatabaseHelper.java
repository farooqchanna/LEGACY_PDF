package com.litepdf.reader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite database helper for LitePDF Reader.
 * Manages creation and versioning of the local database.
 * 
 * Tables:
 * - bookmarks: stores user-created page bookmarks per PDF
 * - recent_files: stores recently opened PDFs (max 20)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "litepdf.db";
    private static final int DATABASE_VERSION = 1;

    // --- Bookmarks Table ---
    public static final String TABLE_BOOKMARKS = "bookmarks";
    public static final String COL_BOOKMARK_ID = "id";
    public static final String COL_BOOKMARK_FILE_PATH = "file_path";
    public static final String COL_BOOKMARK_PAGE_NUMBER = "page_number";
    public static final String COL_BOOKMARK_LABEL = "label";
    public static final String COL_BOOKMARK_CREATED_AT = "created_at";

    private static final String CREATE_TABLE_BOOKMARKS =
            "CREATE TABLE " + TABLE_BOOKMARKS + " (" +
                    COL_BOOKMARK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_BOOKMARK_FILE_PATH + " TEXT NOT NULL, " +
                    COL_BOOKMARK_PAGE_NUMBER + " INTEGER NOT NULL, " +
                    COL_BOOKMARK_LABEL + " TEXT DEFAULT '', " +
                    COL_BOOKMARK_CREATED_AT + " INTEGER NOT NULL" +
                    ")";

    private static final String CREATE_INDEX_BOOKMARKS_FILE =
            "CREATE INDEX idx_bookmarks_file ON " + TABLE_BOOKMARKS +
                    " (" + COL_BOOKMARK_FILE_PATH + ")";

    // --- Recent Files Table ---
    public static final String TABLE_RECENT_FILES = "recent_files";
    public static final String COL_RECENT_ID = "id";
    public static final String COL_RECENT_FILE_PATH = "file_path";
    public static final String COL_RECENT_FILE_NAME = "file_name";
    public static final String COL_RECENT_FILE_SIZE = "file_size";
    public static final String COL_RECENT_LAST_OPENED = "last_opened";
    public static final String COL_RECENT_LAST_PAGE = "last_page";

    private static final String CREATE_TABLE_RECENT_FILES =
            "CREATE TABLE " + TABLE_RECENT_FILES + " (" +
                    COL_RECENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_RECENT_FILE_PATH + " TEXT UNIQUE NOT NULL, " +
                    COL_RECENT_FILE_NAME + " TEXT NOT NULL, " +
                    COL_RECENT_FILE_SIZE + " INTEGER NOT NULL, " +
                    COL_RECENT_LAST_OPENED + " INTEGER NOT NULL, " +
                    COL_RECENT_LAST_PAGE + " INTEGER DEFAULT 0" +
                    ")";

    private static final String CREATE_INDEX_RECENT_OPENED =
            "CREATE INDEX idx_recent_opened ON " + TABLE_RECENT_FILES +
                    " (" + COL_RECENT_LAST_OPENED + " DESC)";

    // Singleton instance
    private static DatabaseHelper sInstance;

    /**
     * Returns a singleton instance of DatabaseHelper.
     * Use application context to prevent memory leaks.
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(CREATE_TABLE_BOOKMARKS);
        db.execSQL(CREATE_TABLE_RECENT_FILES);

        // Create indexes for fast queries
        db.execSQL(CREATE_INDEX_BOOKMARKS_FILE);
        db.execSQL(CREATE_INDEX_RECENT_OPENED);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For version 1, nothing to upgrade.
        // Future versions: use incremental ALTER TABLE statements here.
    }
}
