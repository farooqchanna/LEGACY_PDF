package com.litepdf.reader.utils;

import com.litepdf.reader.models.PdfFile;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class to sort lists of PdfFile by name, date modified, or file size.
 */
public class SortUtils {

    public static final int SORT_NAME_ASC = 0;
    public static final int SORT_NAME_DESC = 1;
    public static final int SORT_DATE_NEWEST = 2;
    public static final int SORT_DATE_OLDEST = 3;
    public static final int SORT_SIZE_LARGEST = 4;
    public static final int SORT_SIZE_SMALLEST = 5;

    private SortUtils() {
        // Utility class
    }

    /**
     * Sorts the given list of PdfFiles in-place according to the sort mode.
     *
     * @param list     the list to sort
     * @param sortMode one of SORT_* constants
     */
    public static void sort(List<PdfFile> list, final int sortMode) {
        if (list == null || list.size() <= 1) {
            return;
        }

        Collections.sort(list, new Comparator<PdfFile>() {
            @Override
            public int compare(PdfFile f1, PdfFile f2) {
                if (f1 == null && f2 == null) return 0;
                if (f1 == null) return 1;
                if (f2 == null) return -1;

                switch (sortMode) {
                    case SORT_NAME_ASC:
                        return f1.getName().compareToIgnoreCase(f2.getName());

                    case SORT_NAME_DESC:
                        return f2.getName().compareToIgnoreCase(f1.getName());

                    case SORT_DATE_NEWEST:
                        // Long.compare is available in Java 7 / API 19+, but for API 14 compatibility:
                        if (f2.getDateModified() < f1.getDateModified()) return -1;
                        if (f2.getDateModified() > f1.getDateModified()) return 1;
                        return 0;

                    case SORT_DATE_OLDEST:
                        if (f1.getDateModified() < f2.getDateModified()) return -1;
                        if (f1.getDateModified() > f2.getDateModified()) return 1;
                        return 0;

                    case SORT_SIZE_LARGEST:
                        if (f2.getSize() < f1.getSize()) return -1;
                        if (f2.getSize() > f1.getSize()) return 1;
                        return 0;

                    case SORT_SIZE_SMALLEST:
                        if (f1.getSize() < f2.getSize()) return -1;
                        if (f1.getSize() > f2.getSize()) return 1;
                        return 0;

                    default:
                        return f1.getName().compareToIgnoreCase(f2.getName());
                }
            }
        });
    }
}
