package com.litepdf.reader.models;

/**
 * Represents a recently opened PDF file.
 * Stored in SQLite database (recent_files table).
 * Limited to 20 most recent entries.
 */
public class RecentFile {

    private long id;
    private String filePath;
    private String fileName;
    private long fileSize;      // in bytes
    private long lastOpened;    // Unix timestamp in milliseconds
    private int lastPage;       // 0-indexed last viewed page

    public RecentFile() {
        this.lastPage = 0;
    }

    public RecentFile(String filePath, String fileName, long fileSize) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.lastOpened = System.currentTimeMillis();
        this.lastPage = 0;
    }

    public RecentFile(String filePath, String fileName, long fileSize, int lastPage) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.lastOpened = System.currentTimeMillis();
        this.lastPage = lastPage;
    }

    // --- Getters ---

    public long getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the file name without the .pdf extension.
     */
    public String getDisplayName() {
        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            return fileName.substring(0, fileName.length() - 4);
        }
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getLastOpened() {
        return lastOpened;
    }

    public int getLastPage() {
        return lastPage;
    }

    /**
     * Returns 1-indexed page number for display.
     */
    public int getDisplayLastPage() {
        return lastPage + 1;
    }

    // --- Setters ---

    public void setId(long id) {
        this.id = id;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setLastOpened(long lastOpened) {
        this.lastOpened = lastOpened;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RecentFile other = (RecentFile) obj;
        return filePath != null && filePath.equals(other.filePath);
    }

    @Override
    public int hashCode() {
        return filePath != null ? filePath.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "RecentFile{" +
                "fileName='" + fileName + '\'' +
                ", lastPage=" + getDisplayLastPage() +
                '}';
    }
}
