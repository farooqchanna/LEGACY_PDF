package com.litepdf.reader.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.litepdf.reader.models.PdfFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Background scanner for locating PDF files on the device storage or in a specific folder.
 * Compatible with Android 4.0+ (API 14).
 */
public class PdfScanner {

    public interface ScanListener {
        void onScanStarted();
        void onScanProgress(int foundCount);
        void onScanCompleted(List<PdfFile> pdfFiles);
    }

    private final Context context;
    private ScanTask currentTask;

    public PdfScanner(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Scans the default external storage (and SD card paths if accessible) for all PDF files.
     */
    public void scanAllPdfs(ScanListener listener) {
        cancel();
        currentTask = new ScanTask(null, listener);
        currentTask.execute();
    }

    /**
     * Scans a specific folder and all its subfolders recursively for PDF files.
     */
    public void scanFolder(File folder, ScanListener listener) {
        cancel();
        currentTask = new ScanTask(folder, listener);
        currentTask.execute();
    }

    public void cancel() {
        if (currentTask != null && currentTask.getStatus() != AsyncTask.Status.FINISHED) {
            currentTask.cancel(true);
            currentTask = null;
        }
    }

    private static class ScanTask extends AsyncTask<Void, Integer, List<PdfFile>> {
        private final File targetFolder;
        private final ScanListener listener;
        private final List<PdfFile> results = new ArrayList<PdfFile>();

        ScanTask(File targetFolder, ScanListener listener) {
            this.targetFolder = targetFolder;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            if (listener != null) {
                listener.onScanStarted();
            }
        }

        @Override
        protected List<PdfFile> doInBackground(Void... params) {
            if (targetFolder != null) {
                if (targetFolder.exists() && targetFolder.isDirectory()) {
                    scanRecursive(targetFolder);
                }
            } else {
                // Scan primary external storage
                File root = Environment.getExternalStorageDirectory();
                if (root != null && root.exists()) {
                    scanRecursive(root);
                }

                // Check secondary / external SD cards often found on Android 4.x
                String[] potentialRoots = new String[]{
                        "/storage/sdcard1",
                        "/storage/extSdCard",
                        "/storage/sdcard0/external_sd",
                        "/mnt/extSdCard",
                        "/mnt/sdcard/external_sd"
                };
                for (String p : potentialRoots) {
                    if (isCancelled()) break;
                    File extRoot = new File(p);
                    if (extRoot.exists() && extRoot.isDirectory() && !extRoot.equals(root)) {
                        scanRecursive(extRoot);
                    }
                }
            }
            return results;
        }

        private void scanRecursive(File dir) {
            if (dir == null || !dir.canRead() || isCancelled()) {
                return;
            }

            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }

            for (File file : files) {
                if (isCancelled()) return;

                // Skip hidden files/directories (starting with dot)
                if (file.getName().startsWith(".")) {
                    continue;
                }

                if (file.isDirectory()) {
                    // Skip typical Android system/cache directories to speed up scanning
                    String name = file.getName();
                    if (!"Android".equalsIgnoreCase(name) &&
                        !"lost.dir".equalsIgnoreCase(name) &&
                        !"data".equalsIgnoreCase(name)) {
                        scanRecursive(file);
                    }
                } else if (file.isFile()) {
                    if (FileUtils.isPdf(file)) {
                        results.add(new PdfFile(file));
                        if (results.size() % 10 == 0) {
                            publishProgress(results.size());
                        }
                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (listener != null && values != null && values.length > 0) {
                listener.onScanProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(List<PdfFile> pdfFiles) {
            if (listener != null) {
                listener.onScanCompleted(pdfFiles);
            }
        }
    }
}
