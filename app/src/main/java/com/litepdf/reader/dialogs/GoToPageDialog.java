package com.litepdf.reader.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.litepdf.reader.R;

/**
 * Dialog to jump directly to a specific page number.
 */
public class GoToPageDialog {

    public interface OnPageSelectedListener {
        void onPageSelected(int pageIndex);
    }

    public static void show(final Context context, final int currentPageIndex, final int totalPages, final OnPageSelectedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.go_to_page);

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(context.getString(R.string.go_to_page_hint, totalPages));
        input.setText(String.valueOf(currentPageIndex + 1));
        input.setSelection(input.getText().length());

        int padding = (int) (16 * context.getResources().getDisplayMetrics().density);
        builder.setView(input);

        builder.setPositiveButton(R.string.btn_go, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString().trim();
                if (!value.isEmpty()) {
                    try {
                        int pageNumber = Integer.parseInt(value);
                        if (pageNumber >= 1 && pageNumber <= totalPages) {
                            if (listener != null) {
                                listener.onPageSelected(pageNumber - 1);
                            }
                        } else {
                            Toast.makeText(context, context.getString(R.string.go_to_page_hint, totalPages), Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        });

        builder.setNegativeButton(R.string.btn_cancel, null);
        builder.create().show();
    }
}
