package com.litepdf.reader.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.litepdf.reader.R;
import com.shockwave.pdfium.PdfDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog displaying Table of Contents (Outline) from PDF metadata.
 */
public class OutlineDialog {

    public interface OnOutlineSelectedListener {
        void onOutlineSelected(int pageIndex);
    }

    public static void show(Context context, List<PdfDocument.Bookmark> outline, final OnOutlineSelectedListener listener) {
        if (outline == null || outline.isEmpty()) {
            Toast.makeText(context, R.string.empty_no_toc, Toast.LENGTH_SHORT).show();
            return;
        }

        final List<PdfDocument.Bookmark> flatList = new ArrayList<PdfDocument.Bookmark>();
        final List<String> displayTitles = new ArrayList<String>();

        flattenOutline(outline, 0, flatList, displayTitles);

        if (displayTitles.isEmpty()) {
            Toast.makeText(context, R.string.empty_no_toc, Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_list_item_1, displayTitles);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.table_of_contents);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PdfDocument.Bookmark item = flatList.get(which);
                if (listener != null) {
                    listener.onOutlineSelected((int) item.getPageIdx());
                }
            }
        });

        builder.setNegativeButton(R.string.btn_cancel, null);
        builder.create().show();
    }

    private static void flattenOutline(List<PdfDocument.Bookmark> nodes, int depth, List<PdfDocument.Bookmark> flatList, List<String> displayTitles) {
        if (nodes == null) return;

        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("   ");
        }

        for (PdfDocument.Bookmark b : nodes) {
            flatList.add(b);
            displayTitles.add(indent.toString() + "• " + b.getTitle());
            if (b.hasChildren()) {
                flattenOutline(b.getChildren(), depth + 1, flatList, displayTitles);
            }
        }
    }
}
