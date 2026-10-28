package com.litepdf.reader.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.litepdf.reader.R;
import com.litepdf.reader.models.PdfFile;
import com.litepdf.reader.utils.FileUtils;

/**
 * Dialog displaying detailed file information for a PDF.
 */
public class FileInfoDialog {

    public static void show(Context context, PdfFile pdfFile) {
        if (pdfFile == null) return;

        StringBuilder message = new StringBuilder();
        message.append(context.getString(R.string.info_name, pdfFile.getName())).append("\n\n");
        message.append(context.getString(R.string.info_path, pdfFile.getPath())).append("\n\n");
        message.append(context.getString(R.string.info_size, FileUtils.formatFileSize(pdfFile.getSize()))).append("\n\n");
        message.append(context.getString(R.string.info_modified, FileUtils.formatDateTime(pdfFile.getDateModified())));

        new AlertDialog.Builder(context)
                .setTitle(R.string.file_info)
                .setMessage(message.toString())
                .setPositiveButton(R.string.btn_ok, null)
                .show();
    }
}
