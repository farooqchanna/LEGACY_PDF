package com.litepdf.reader.models;

import java.io.File;

/**
 * Represents a PDF file found on the device.
 * Used in the home screen file browser (grid/list views).
 */
public class PdfFile implements Comparable<PdfFile> {

    private String path;
    private String name;
    private long size;          // in bytes
    private long dateModified;  // Unix timestamp in milliseconds
    private int pageCount;      // -1 if not yet determined

    public PdfFile() {
        this.pageCount = -1;
    }

    public PdfFile(String path, String name, long size, long dateModified) {
        this.path = path;
        this.name = name;
        this.size = size;
        this.dateModified = dateModified;
        this.pageCount = -1;
    }

    /**
     * Convenience constructor: builds PdfFile directly from a File object.
     */
    public PdfFile(File file) {
        this.path = file.getAbsolutePath();
        this.name = file.getName();
        this.size = file.length();
        this.dateModified = file.lastModified();
        this.pageCount = -1;
    }

    // --- Getters ---

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the file name without the .pdf extension.
     */
    public String getDisplayName() {
        if (name != null && name.toLowerCase().endsWith(".pdf")) {
            return name.substring(0, name.length() - 4);
        }
        return name;
    }

    public long getSize() {
        return size;
    }

    public long getDateModified() {
        return dateModified;
    }

    public int getPageCount() {
        return pageCount;
    }

    // --- Setters ---

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    /**
     * Checks if the underlying file still exists on disk.
     */
    public boolean exists() {
        return path != null && new File(path).exists();
    }

    /**
     * Default comparison: by name (case-insensitive).
     */
    @Override
    public int compareTo(PdfFile other) {
        if (other == null || other.name == null) return -1;
        if (this.name == null) return 1;
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PdfFile other = (PdfFile) obj;
        return path != null && path.equals(other.path);
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PdfFile{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", path='" + path + '\'' +
                '}';
    }
}
