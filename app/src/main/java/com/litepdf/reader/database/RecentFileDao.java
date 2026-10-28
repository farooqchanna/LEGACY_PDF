package com.litepdf.reader.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.litepdf.reader.models.RecentFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for recent files.
 * Handles all CRUD operations on the recent_files table.
 * Automatically limits entries to MAX_RECENT_FILES (20).
 */
public class RecentFileDao {

    private static final int MAX_RECENT_FILES = 20;

    private final DatabaseHelper dbHelper;

    public RecentFileDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Inserts a new recent file entry, or updates it if the file path already exists.
     * Automatically trims the table to MAX_RECENT_FILES entries.
     *
     * @return the row ID of the inserted/updated entry
     */
    public long insertOrUpdate(RecentFile recentFile) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if entry already exists for this file path
        Cursor cursor = db.query(DatabaseHelper.TABLE_RECENT_FILES,
                new String[]{DatabaseHelper.COL_RECENT_ID},
                DatabaseHelper.COL_RECENT_FILE_PATH + " = ?",
                new String[]{recentFile.getFilePath()},
                null, null, null);

        long id;
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_RECENT_FILE_PATH, recentFile.getFilePath());
        values.put(DatabaseHelper.COL_RECENT_FILE_NAME, recentFile.getFileName());
        values.put(DatabaseHelper.COL_RECENT_FILE_SIZE, recentFile.getFileSize());
        values.put(DatabaseHelper.COL_RECENT_LAST_OPENED, recentFile.getLastOpened());
        values.put(DatabaseHelper.COL_RECENT_LAST_PAGE, recentFile.getLastPage());

        if (cursor.moveToFirst()) {
            // Update existing entry
            long existingId = cursor.getLong(0);
            db.update(DatabaseHelper.TABLE_RECENT_FILES, values,
                    DatabaseHelper.COL_RECENT_ID + " = ?",
                    new String[]{String.valueOf(existingId)});
            id = existingId;
        } else {
            // Insert new entry
            id = db.insert(DatabaseHelper.TABLE_RECENT_FILES, null, values);
        }
        cursor.close();

        // Trim to MAX_RECENT_FILES
        trimToLimit(db);

        recentFile.setId(id);
        return id;
    }

    /**
     * Updates only the last page for a given file path.
     * Call this when the user leaves the viewer to save their reading position.
     */
    public void updateLastPage(String filePath, int lastPage) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_RECENT_LAST_PAGE, lastPage);
        values.put(DatabaseHelper.COL_RECENT_LAST_OPENED, System.currentTimeMillis());

        db.update(DatabaseHelper.TABLE_RECENT_FILES, values,
                DatabaseHelper.COL_RECENT_FILE_PATH + " = ?",
                new String[]{filePath});
    }

    /**
     * Returns the most recent files, ordered by last opened date (newest first).
     *
     * @param limit max number of results (defaults to MAX_RECENT_FILES)
     */
    public List<RecentFile> getRecent(int limit) {
        List<RecentFile> recentFiles = new ArrayList<RecentFile>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_RECENT_FILES,
                null, null, null, null, null,
                DatabaseHelper.COL_RECENT_LAST_OPENED + " DESC",
                String.valueOf(limit));

        if (cursor.moveToFirst()) {
            do {
                recentFiles.add(cursorToRecentFile(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return recentFiles;
    }

    /**
     * Returns all recent files (up to MAX_RECENT_FILES).
     */
    public List<RecentFile> getRecent() {
        return getRecent(MAX_RECENT_FILES);
    }

    /**
     * Gets a specific recent file by its path.
     * Useful for checking last reading position.
     *
     * @return the RecentFile, or null if not found
     */
    public RecentFile getByFilePath(String filePath) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_RECENT_FILES,
                null,
                DatabaseHelper.COL_RECENT_FILE_PATH + " = ?",
                new String[]{filePath},
                null, null, null);

        RecentFile recentFile = null;
        if (cursor.moveToFirst()) {
            recentFile = cursorToRecentFile(cursor);
        }
        cursor.close();
        return recentFile;
    }

    /**
     * Deletes a specific recent file entry.
     */
    public void delete(String filePath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_RECENT_FILES,
                DatabaseHelper.COL_RECENT_FILE_PATH + " = ?",
                new String[]{filePath});
    }

    /**
     * Clears all recent file entries.
     */
    public void clearAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_RECENT_FILES, null, null);
    }

    /**
     * Returns the total count of recent files.
     */
    public int getCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_RECENT_FILES,
                new String[]{"COUNT(*)"},
                null, null, null, null, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * Removes entries that reference files which no longer exist on disk.
     */
    public void removeDeletedFiles() {
        List<RecentFile> allRecent = getRecent();
        for (RecentFile rf : allRecent) {
            java.io.File file = new java.io.File(rf.getFilePath());
            if (!file.exists()) {
                delete(rf.getFilePath());
            }
        }
    }

    /**
     * Trims the table to keep only the most recent MAX_RECENT_FILES entries.
     */
    private void trimToLimit(SQLiteDatabase db) {
        // Delete entries beyond the limit (keep newest MAX_RECENT_FILES)
        db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_RECENT_FILES +
                " WHERE " + DatabaseHelper.COL_RECENT_ID + " NOT IN (" +
                "SELECT " + DatabaseHelper.COL_RECENT_ID +
                " FROM " + DatabaseHelper.TABLE_RECENT_FILES +
                " ORDER BY " + DatabaseHelper.COL_RECENT_LAST_OPENED + " DESC" +
                " LIMIT " + MAX_RECENT_FILES + ")");
    }

    /**
     * Maps a cursor row to a RecentFile object.
     */
    private RecentFile cursorToRecentFile(Cursor cursor) {
        RecentFile recentFile = new RecentFile();
        recentFile.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COL_RECENT_ID)));
        recentFile.setFilePath(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_RECENT_FILE_PATH)));
        recentFile.setFileName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_RECENT_FILE_NAME)));
        recentFile.setFileSize(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COL_RECENT_FILE_SIZE)));
        recentFile.setLastOpened(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COL_RECENT_LAST_OPENED)));
        recentFile.setLastPage(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_RECENT_LAST_PAGE)));
        return recentFile;
    }
}
