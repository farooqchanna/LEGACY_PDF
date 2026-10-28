package com.litepdf.reader.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.litepdf.reader.R;

/**
 * Dialog to select the sorting order of PDF files.
 */
public class SortDialog {

    public interface OnSortSelectedListener {
        void onSortSelected(int sortMode);
    }

    public static void show(Context context, int currentSortMode, final OnSortSelectedListener listener) {
        String[] options = context.getResources().getStringArray(R.array.sort_options);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.sort_title);
        builder.setSingleChoiceItems(options, currentSortMode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onSortSelected(which);
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, null);
        builder.create().show();
    }
}
