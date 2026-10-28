# 📘 PDF Reader for Old Android — Complete Blueprint

> **Project Name:** LitePDF Reader  
> **Status:** ✅ Core Implementation Complete — Steps 1 through 16 Complete!  

---

## Table of Contents

1. [Requirements](#1-requirements)
2. [Tech Stack & Dependencies](#2-tech-stack--dependencies)
3. [Project Structure](#3-project-structure)
4. [UI/UX Design Specification](#4-uiux-design-specification)
5. [Feature Specifications](#5-feature-specifications)
6. [Database Schema](#6-database-schema)
7. [Permissions](#7-permissions)
8. [Implementation Steps (Ordered)](#8-implementation-steps-ordered)
9. [Testing Checklist](#9-testing-checklist)

---

## 1. Requirements

### 1.1 Platform Requirements

| Item | Value |
|---|---|
| Min SDK | API 14 (Android 4.0 Ice Cream Sandwich) |
| Target SDK | API 22 (Android 5.1 Lollipop) |
| Language | Java |
| UI Theme | Holo Light |
| License | **Private / Closed Source** |
| Build System | Gradle |
| APK Size Target | Under 10MB |

### 1.2 Hardware Constraints (Design For)

| Constraint | Assumption |
|---|---|
| RAM | 512MB – 1GB |
| CPU | Single/dual-core, ~1GHz |
| Screen | 480×800 to 720×1280 |
| Storage | Limited (8–16GB total) |
| GPU | Basic, no heavy rendering |

### 1.3 Functional Requirements

| # | Requirement | Priority |
|---|---|---|
| FR-01 | Open and render PDF files | Must Have |
| FR-02 | Continuous vertical scroll mode | Must Have |
| FR-03 | Page-by-page swipe mode | Must Have |
| FR-04 | User can switch between scroll modes | Must Have |
| FR-05 | Pinch-to-zoom and double-tap zoom | Must Have |
| FR-06 | Go-to-page dialog | Must Have |
| FR-07 | Page number indicator (e.g., 3/45) | Must Have |
| FR-08 | Auto-hide toolbar (tap to toggle) | Must Have |
| FR-09 | Light mode (default) | Must Have |
| FR-10 | Dark mode (color inversion on PDF) | Must Have |
| FR-11 | Toggle button for light/dark | Must Have |
| FR-12 | Home screen: Recent files + All PDFs | Must Have |
| FR-13 | Grid view with thumbnails | Must Have |
| FR-14 | List view | Must Have |
| FR-15 | Toggle between grid/list view | Must Have |
| FR-16 | Folder picker → show all PDFs in folder + subfolders | Must Have |
| FR-17 | Sort by name, date, size | Must Have |
| FR-18 | Search PDF files by name | Must Have |
| FR-19 | Bookmarks (save & resume position) | Must Have |
| FR-20 | Table of Contents / Outline support | Must Have |
| FR-21 | Remember last reading position per file | Must Have |
| FR-22 | Open PDFs from external file managers (intent filter) | Must Have |
| FR-23 | Recent files section on home screen | Must Have |

---

## 2. Tech Stack & Dependencies

### 2.1 Core Dependencies

| Dependency | Purpose | License | Notes |
|---|---|---|---|
| **AndroidPdfViewer (barteksc)** | PDF rendering using PDFium | Apache 2.0 ✅ | Supports API 14+, private code safe |
| **PdfiumAndroid** | Native PDF engine (bundled with above) | Apache 2.0 ✅ | Fast, low memory |
| **SQLite** | Local database for bookmarks, recents | Built-in | No extra dependency |
| **Android Support Library v7** | Backward compatibility | Apache 2.0 ✅ | For ActionBar, RecyclerView etc. |
| **RecyclerView (support-v7)** | Efficient lists/grids | Apache 2.0 ✅ | For file browser |
| **DiskLruCache** | Thumbnail caching | Apache 2.0 ✅ | Lightweight cache |

---

## 3. Project Structure

```
com.litepdf.reader/
│
├── activities/
│   ├── MainActivity.java          # Home screen (recent + all PDFs)
│   ├── PdfViewerActivity.java     # PDF reading screen
│   ├── FolderPickerActivity.java  # Folder selection screen
│   └── SettingsActivity.java      # App settings
│
├── adapters/
│   ├── PdfGridAdapter.java        # Grid view adapter for PDF files
│   ├── PdfListAdapter.java        # List view adapter for PDF files
│   └── RecentPdfAdapter.java      # Recent files horizontal row adapter
│
├── database/
│   ├── DatabaseHelper.java        # SQLite helper
│   ├── BookmarkDao.java           # Bookmark CRUD operations
│   └── RecentFileDao.java         # Recent files CRUD operations
│
├── models/
│   ├── PdfFile.java               # PDF file data model
│   ├── Bookmark.java              # Bookmark data model
│   └── RecentFile.java            # Recent file data model
│
├── utils/
│   ├── PdfScanner.java            # Scan device/folders for PDF files
│   ├── ThumbnailCache.java        # Generate & cache PDF thumbnails (RGB_565)
│   ├── FileUtils.java             # File path & size utilities
│   ├── PreferenceManager.java     # SharedPreferences wrapper
│   └── SortUtils.java             # Sorting logic (name/date/size)
│
└── dialogs/
    ├── GoToPageDialog.java        # Go-to-page input dialog
    ├── SortDialog.java            # Sort options dialog
    ├── BookmarksDialog.java       # Bookmarks list & jump dialog
    ├── FileInfoDialog.java        # File info popup dialog
    └── OutlineDialog.java         # Table of contents dialog
```

---

## 8. Implementation Steps (Ordered)

### Step 1: Project Setup ✅ COMPLETE
- [x] Create new Android project with package `com.litepdf.reader`
- [x] Set `minSdkVersion 14`, `targetSdkVersion 22`, `compileSdkVersion 28`
- [x] Configure `build.gradle` with all dependencies
- [x] Add AndroidPdfViewer library: `com.github.barteksc:android-pdf-viewer:2.8.2`
- [x] Add RecyclerView support library: `com.android.support:recyclerview-v7:28.0.0`
- [x] Add AppCompat: `com.android.support:appcompat-v7:28.0.0`
- [x] Set Holo Light theme in `styles.xml`
- [x] Add storage permissions in `AndroidManifest.xml`
- [x] Verify project builds successfully

### Step 2: Create Data Models ✅ COMPLETE
- [x] Create `PdfFile.java` — fields: path, name, size, dateModified, pageCount
- [x] Create `Bookmark.java` — fields: id, filePath, pageNumber, label, createdAt
- [x] Create `RecentFile.java` — fields: id, filePath, fileName, fileSize, lastOpened, lastPage
- [x] Add constructors, getters, setters to all models

### Step 3: Database Layer ✅ COMPLETE
- [x] Create `DatabaseHelper.java` extending SQLiteOpenHelper
- [x] Create `bookmarks` table with schema from Section 6
- [x] Create `recent_files` table with schema from Section 6
- [x] Create `BookmarkDao.java` — insert, delete, getByFile, getAll
- [x] Create `RecentFileDao.java` — insertOrUpdate, getRecent(limit 20), delete, clearAll
- [x] Test all CRUD operations work correctly

### Step 4: Utility Classes ✅ COMPLETE
- [x] Create `FileUtils.java` — formatFileSize(), formatDate(), getFileName(), getFileExtension()
- [x] Create `SortUtils.java` — sort by name/date/size with ascending/descending direction
- [x] Create `PreferenceManager.java` — save/load: viewMode, sortMode, darkMode, scrollMode, readingPositions
- [x] Create `PdfScanner.java` — scanAllPdfs(), scanFolder(path), recursive subfolder scan using AsyncTask

### Step 5: Thumbnail System ✅ COMPLETE
- [x] Create `ThumbnailCache.java`
- [x] Implement disk cache directory setup (`/cache/thumbnails/`)
- [x] Implement thumbnail generation from PdfiumCore (page 0, 150×200px, RGB_565)
- [x] Implement async loading with callback interface
- [x] Implement cache key generation (file path hash + last modified)
- [x] Implement cache invalidation (stale check by file modified date)
- [x] Create `ic_pdf_placeholder.xml` drawable for loading state

### Step 6: Resource Files (Colors, Strings, Dimensions, Styles) ✅ COMPLETE
- [x] Create `colors.xml` with full color palette from Section 4.1 & 4.2
- [x] Create `strings.xml` with all UI text strings
- [x] Create `dimens.xml` with spacing, text sizes, thumbnail dimensions
- [x] Create `styles.xml` with Holo Light theme and custom styles
- [x] Create `arrays.xml` with sort options

### Step 7: Home Screen — Layouts ✅ COMPLETE
- [x] Create `activity_main.xml` — full home screen with search bar, folder button, recent section, all PDFs section
- [x] Create `item_pdf_grid.xml` — grid item: thumbnail image + file name text
- [x] Create `item_pdf_list.xml` — list item: small thumbnail + name + size + date
- [x] Create `item_pdf_recent.xml` — horizontal scroll item for recent files
- [x] Create `menu_main.xml` — search, grid/list toggle, sort action items

### Step 8: Home Screen — Adapters & Logic ✅ COMPLETE
- [x] Create `PdfGridAdapter.java` with RecyclerView.Adapter + ViewHolder
- [x] Create `PdfListAdapter.java` with RecyclerView.Adapter + ViewHolder
- [x] Create `RecentPdfAdapter.java` for horizontal recent files
- [x] Integrate thumbnail loading into all adapters
- [x] Create `MainActivity.java`:
  - [x] On launch → scan all PDFs in background
  - [x] Show progress indicator during scan
  - [x] Populate recent files from database (horizontal RecyclerView)
  - [x] Populate all PDFs from scan (vertical RecyclerView, grid default)
  - [x] Handle grid/list toggle → swap layout manager + adapter
  - [x] Handle sort button → show SortDialog → re-sort list
  - [x] Handle search → filter displayed list in real-time
  - [x] Handle PDF tap → launch PdfViewerActivity with file path
  - [x] Handle PDF long-press → show file info dialog

### Step 9: Sort & Search Dialogs ✅ COMPLETE
- [x] Create `SortDialog.java` — single choice dialog for 6 sort options
- [x] Implement SearchView in action bar with real-time filtering
- [x] Connect sort selection to PreferenceManager (persist choice)

### Step 10: Folder Picker ✅ COMPLETE
- [x] Create `activity_folder_picker.xml` — folder tree list layout
- [x] Create `item_folder.xml` — folder icon + folder name
- [x] Create `FolderPickerActivity.java`:
  - [x] Show root directories (Internal Storage + SD Card if available)
  - [x] Allow navigation into subfolders
  - [x] "Select This Folder" button
  - [x] Return selected folder path via Intent result
- [x] In `MainActivity`: receive folder path → scan that folder recursively → update PDF list

### Step 11: PDF Viewer — Layout ✅ COMPLETE
- [x] Create `activity_pdf_viewer.xml`:
  - [x] PDFView (fills entire screen)
  - [x] Overlay top bar: back button, file name, dark mode toggle, TOC button, bookmark button, overflow menu
  - [x] Overlay bottom bar: SeekBar (page slider) + "Page X / Y" text
- [x] Create `menu_viewer.xml` — overflow menu: scroll mode, go to page, bookmarks list, file info

### Step 12: PDF Viewer — Core Rendering ✅ COMPLETE
- [x] Create `PdfViewerActivity.java`:
  - [x] Receive file path from intent (direct launch or external app)
  - [x] Load PDF into PDFView using AndroidPdfViewer
  - [x] Set default scroll mode from preferences
  - [x] Implement page change listener → update page indicator text + SeekBar position
  - [x] Implement SeekBar listener → jump to page on drag
  - [x] Implement tap listener → toggle top/bottom bar visibility with animation
  - [x] Pinch-to-zoom (built into PDFView library)
  - [x] Restore last reading position from PreferenceManager
  - [x] Save reading position on onPause() and back press
  - [x] Add file to recent_files database on open

### Step 13: PDF Viewer — Features ✅ COMPLETE
- [x] Implement dark mode toggle (PDFView nightMode + theme colors)
- [x] Implement scroll mode toggle (Vertical continuous ↔ Page swipe)
- [x] Create `GoToPageDialog.java`
- [x] Implement bookmark toggle & `BookmarksDialog.java`
- [x] Implement Table of Contents & `OutlineDialog.java`
- [x] Implement file info & `FileInfoDialog.java`

### Step 14: Intent Filter (Open from External Apps) ✅ COMPLETE
- [x] Add intent filter in `AndroidManifest.xml` (`ACTION_VIEW`, `application/pdf`)
- [x] Handle `file://` URI and copy `content://` URI to cache in `PdfViewerActivity`

### Step 15: Settings Screen ✅ COMPLETE
- [x] Create `activity_settings.xml`
- [x] Create `SettingsActivity.java` (manage view mode, scroll mode, clear recents, clear cache)

### Step 16: Polish & Launcher Icon ✅ COMPLETE
- [x] Create `ic_launcher.xml` vector/layer-list icon
- [x] Update manifest to use `@drawable/ic_launcher`
- [x] Provide clean fallback handling for missing, corrupted, or deleted files

---

## 9. Testing Checklist

### 9.1 Functional Tests

| # | Test Case | Expected Result |
|---|---|---|
| T-01 | Open app first time | Shows loading → scans → displays PDFs |
| T-02 | Tap a PDF | Opens in viewer at page 1 |
| T-03 | Re-open same PDF | Opens at last reading position |
| T-04 | Pinch to zoom | Smooth zoom in/out |
| T-05 | Double tap | Toggle fit-width / zoom |
| T-06 | Tap center of viewer | Toggle toolbars |
| T-07 | Drag page slider | Jump to page |
| T-08 | Go to Page dialog | Enter page → jump correctly |
| T-09 | Toggle dark mode | PDF colors invert |
| T-10 | Toggle scroll mode | Switch between scroll/swipe |
| T-11 | Add bookmark | Bookmark icon filled, saved |
| T-12 | View bookmarks list | Shows all bookmarks for file |
| T-13 | Tap bookmark in list | Jumps to correct page |
| T-14 | View Table of Contents | Shows outline if available |
| T-15 | Switch grid/list view | Layout changes correctly |
| T-16 | Sort by name/date/size | Files reorder correctly |
| T-17 | Search by name | Filters list in real-time |
| T-18 | Select folder | Shows only PDFs in that folder tree |
| T-19 | Open PDF from file manager | App opens and loads the PDF |
| T-20 | Open corrupted PDF | Shows error message, doesn't crash |
| T-21 | No PDFs on device | Shows "No PDFs found" message |
| T-22 | Large PDF (500 pages) | Opens without crash, scrolls smoothly |
| T-23 | App backgrounded | Reading position saved |
| T-24 | Clear recent files | Recent section empties |
| T-25 | Rotate device | Layout adjusts, position preserved |
