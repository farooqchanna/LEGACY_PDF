package com.litepdf.reader.models;

/**
 * Represents a bookmarked page within a PDF file.
 * Stored in SQLite database (bookmarks table).
 */
public class Bookmark {

    private long id;
    private String filePath;
    private int pageNumber;     // 0-indexed page number
    private String label;       // e.g., "Page 5"
    private long createdAt;     // Unix timestamp in milliseconds

    public Bookmark() {
    }

    public Bookmark(String filePath, int pageNumber, String label) {
        this.filePath = filePath;
        this.pageNumber = pageNumber;
        this.label = label;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Convenience constructor: auto-generates label as "Page X".
     * Note: pageNumber is 0-indexed internally, but label shows 1-indexed.
     */
    public Bookmark(String filePath, int pageNumber) {
        this(filePath, pageNumber, "Page " + (pageNumber + 1));
    }

    // --- Getters ---

    public long getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Returns 1-indexed page number for display purposes.
     */
    public int getDisplayPageNumber() {
        return pageNumber + 1;
    }

    public String getLabel() {
        return label;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // --- Setters ---

    public void setId(long id) {
        this.id = id;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Bookmark other = (Bookmark) obj;
        return pageNumber == other.pageNumber
                && filePath != null && filePath.equals(other.filePath);
    }

    @Override
    public int hashCode() {
        int result = filePath != null ? filePath.hashCode() : 0;
        result = 31 * result + pageNumber;
        return result;
    }

    @Override
    public String toString() {
        return "Bookmark{" +
                "filePath='" + filePath + '\'' +
                ", page=" + getDisplayPageNumber() +
                ", label='" + label + '\'' +
                '}';
    }
}
