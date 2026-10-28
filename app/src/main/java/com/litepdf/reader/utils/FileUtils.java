package com.litepdf.reader.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for file-related operations.
 * Provides formatting helpers for file sizes, dates, and path manipulation.
 */
public class FileUtils {

    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    private static final SimpleDateFormat DATE_TIME_FORMAT =
            new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);

    private FileUtils() {
        // Utility class, no instantiation
    }

    /**
     * Formats a file size in bytes to a human-readable string.
     * Examples: "512 KB", "2.4 MB", "1.1 GB"
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 0) return "0 B";

        if (bytes < KB) {
            return bytes + " B";
        } else if (bytes < MB) {
            float kb = bytes / (float) KB;
            return formatNumber(kb) + " KB";
        } else if (bytes < GB) {
            float mb = bytes / (float) MB;
            return formatNumber(mb) + " MB";
        } else {
            float gb = bytes / (float) GB;
            return formatNumber(gb) + " GB";
        }
    }

    /**
     * Formats a Unix timestamp (milliseconds) to a date string.
     * Example: "Oct 15, 2026"
     */
    public static String formatDate(long timestamp) {
        if (timestamp <= 0) return "";
        return DATE_FORMAT.format(new Date(timestamp));
    }

    /**
     * Formats a Unix timestamp (milliseconds) to a date + time string.
     * Example: "Oct 15, 2026 14:30"
     */
    public static String formatDateTime(long timestamp) {
        if (timestamp <= 0) return "";
        return DATE_TIME_FORMAT.format(new Date(timestamp));
    }

    /**
     * Extracts the file name from a full file path.
     * Example: "/storage/Documents/test.pdf" → "test.pdf"
     */
    public static String getFileName(String path) {
        if (path == null || path.isEmpty()) return "";
        int lastSep = path.lastIndexOf(File.separatorChar);
        if (lastSep >= 0 && lastSep < path.length() - 1) {
            return path.substring(lastSep + 1);
        }
        return path;
    }

    /**
     * Extracts the file name without extension.
     * Example: "test.pdf" → "test"
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    /**
     * Gets the file extension in lowercase.
     * Example: "test.PDF" → "pdf"
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase(Locale.US);
        }
        return "";
    }

    /**
     * Checks if a file path points to a PDF file.
     */
    public static boolean isPdf(String path) {
        return "pdf".equals(getFileExtension(path));
    }

    /**
     * Checks if a file path points to a PDF file.
     */
    public static boolean isPdf(File file) {
        return file != null && !file.isDirectory() && isPdf(file.getName());
    }

    /**
     * Gets the parent directory path from a file path.
     * Example: "/storage/Documents/test.pdf" → "/storage/Documents"
     */
    public static String getParentPath(String path) {
        if (path == null || path.isEmpty()) return "";
        File file = new File(path);
        File parent = file.getParentFile();
        return parent != null ? parent.getAbsolutePath() : "";
    }

    /**
     * Returns a short display path (last 2 folder levels + file name).
     * Example: "/storage/emulated/0/Documents/Books/test.pdf" → "Books/test.pdf"
     */
    public static String getShortPath(String path) {
        if (path == null || path.isEmpty()) return "";
        String[] parts = path.split(File.separator.equals("\\") ? "\\\\" : File.separator);
        if (parts.length <= 2) return path;
        return parts[parts.length - 2] + File.separator + parts[parts.length - 1];
    }

    /**
     * Formats a number with at most 1 decimal place.
     * Examples: 2.0 → "2", 2.4 → "2.4", 2.45 → "2.5"
     */
    private static String formatNumber(float number) {
        if (number == (int) number) {
            return String.valueOf((int) number);
        }
        return String.format(Locale.US, "%.1f", number);
    }
}
