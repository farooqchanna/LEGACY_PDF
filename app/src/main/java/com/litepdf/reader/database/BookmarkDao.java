package com.litepdf.reader.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.litepdf.reader.models.Bookmark;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for bookmarks.
 * Handles all CRUD operations on the bookmarks table.
 */
public class BookmarkDao {

    private final DatabaseHelper dbHelper;

    public BookmarkDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Inserts a new bookmark. If a bookmark already exists for the same
     * file and page, it is not duplicated.
     *
     * @return the row ID of the inserted bookmark, or -1 if it already exists
     */
    public long insert(Bookmark bookmark) {
        // Check for duplicate first
        if (exists(bookmark.getFilePath(), bookmark.getPageNumber())) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_BOOKMARK_FILE_PATH, bookmark.getFilePath());
        values.put(DatabaseHelper.COL_BOOKMARK_PAGE_NUMBER, bookmark.getPageNumber());
        values.put(DatabaseHelper.COL_BOOKMARK_LABEL, bookmark.getLabel());
        values.put(DatabaseHelper.COL_BOOKMARK_CREATED_AT, bookmark.getCreatedAt());

        long id = db.insert(DatabaseHelper.TABLE_BOOKMARKS, null, values);
        bookmark.setId(id);
        return id;
    }

    /**
     * Deletes a bookmark by its ID.
     */
    public void delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_BOOKMARKS,
                DatabaseHelper.COL_BOOKMARK_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * Deletes a bookmark by file path and page number.
     */
    public void delete(String filePath, int pageNumber) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_BOOKMARKS,
                DatabaseHelper.COL_BOOKMARK_FILE_PATH + " = ? AND " +
                        DatabaseHelper.COL_BOOKMARK_PAGE_NUMBER + " = ?",
                new String[]{filePath, String.valueOf(pageNumber)});
    }

    /**
     * Deletes all bookmarks for a specific file.
     */
    public void deleteByFile(String filePath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_BOOKMARKS,
                DatabaseHelper.COL_BOOKMARK_FILE_PATH + " = ?",
                new String[]{filePath});
    }

    /**
     * Checks if a bookmark exists for the given file and page.
     */
    public boolean exists(String filePath, int pageNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKMARKS,
                new String[]{DatabaseHelper.COL_BOOKMARK_ID},
                DatabaseHelper.COL_BOOKMARK_FILE_PATH + " = ? AND " +
                        DatabaseHelper.COL_BOOKMARK_PAGE_NUMBER + " = ?",
                new String[]{filePath, String.valueOf(pageNumber)},
                null, null, null);

        boolean found = cursor.getCount() > 0;
        cursor.close();
        return found;
    }

    /**
     * Returns all bookmarks for a specific PDF file, ordered by page number.
     */
    public List<Bookmark> getByFile(String filePath) {
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKMARKS,
                null,
                DatabaseHelper.COL_BOOKMARK_FILE_PATH + " = ?",
                new String[]{filePath},
                null, null,
                DatabaseHelper.COL_BOOKMARK_PAGE_NUMBER + " ASC");

        if (cursor.moveToFirst()) {
            do {
                bookmarks.add(cursorToBookmark(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookmarks;
    }

    /**
     * Returns all bookmarks across all files, ordered by creation date (newest first).
     */
    public List<Bookmark> getAll() {
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKMARKS,
                null, null, null, null, null,
                DatabaseHelper.COL_BOOKMARK_CREATED_AT + " DESC");

        if (cursor.moveToFirst()) {
            do {
                bookmarks.add(cursorToBookmark(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookmarks;
    }

    /**
     * Returns the count of bookmarks for a specific file.
     */
    public int getCountByFile(String filePath) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKMARKS,
                new String[]{"COUNT(*)"},
                DatabaseHelper.COL_BOOKMARK_FILE_PATH + " = ?",
                new String[]{filePath},
                null, null, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * Maps a cursor row to a Bookmark object.
     */
    private Bookmark cursorToBookmark(Cursor cursor) {
        Bookmark bookmark = new Bookmark();
        bookmark.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COL_BOOKMARK_ID)));
        bookmark.setFilePath(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_BOOKMARK_FILE_PATH)));
        bookmark.setPageNumber(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_BOOKMARK_PAGE_NUMBER)));
        bookmark.setLabel(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_BOOKMARK_LABEL)));
        bookmark.setCreatedAt(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COL_BOOKMARK_CREATED_AT)));
        return bookmark;
    }
}
