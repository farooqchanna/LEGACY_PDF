package com.litepdf.reader.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.litepdf.reader.R;
import com.litepdf.reader.database.BookmarkDao;
import com.litepdf.reader.models.Bookmark;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog displaying all bookmarks for the currently opened PDF file.
 */
public class BookmarksDialog {

    public interface OnBookmarkSelectedListener {
        void onBookmarkSelected(int pageIndex);
        void onBookmarkDeleted();
    }

    public static void show(final Context context, final String filePath, final BookmarkDao bookmarkDao, final OnBookmarkSelectedListener listener) {
        final List<Bookmark> bookmarks = bookmarkDao.getByFile(filePath);

        if (bookmarks.isEmpty()) {
            Toast.makeText(context, R.string.empty_no_bookmarks, Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> labels = new ArrayList<String>();
        for (Bookmark b : bookmarks) {
            labels.add("★ " + b.getLabel() + " (Page " + b.getDisplayPageNumber() + ")");
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_list_item_1, labels);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.bookmarks_list);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bookmark selected = bookmarks.get(which);
                if (listener != null) {
                    listener.onBookmarkSelected(selected.getPageNumber());
                }
            }
        });

        builder.setNegativeButton(R.string.btn_cancel, null);
        builder.create().show();
    }
}
