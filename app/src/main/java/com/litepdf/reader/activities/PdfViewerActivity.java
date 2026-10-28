package com.litepdf.reader.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.litepdf.reader.R;
import com.litepdf.reader.database.BookmarkDao;
import com.litepdf.reader.database.DatabaseHelper;
import com.litepdf.reader.database.RecentFileDao;
import com.litepdf.reader.dialogs.BookmarksDialog;
import com.litepdf.reader.dialogs.FileInfoDialog;
import com.litepdf.reader.dialogs.GoToPageDialog;
import com.litepdf.reader.dialogs.OutlineDialog;
import com.litepdf.reader.models.Bookmark;
import com.litepdf.reader.models.PdfFile;
import com.litepdf.reader.models.RecentFile;
import com.litepdf.reader.utils.FileUtils;
import com.litepdf.reader.utils.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class PdfViewerActivity extends AppCompatActivity implements
        OnPageChangeListener,
        OnLoadCompleteListener,
        OnErrorListener,
        OnTapListener {

    private PDFView pdfView;
    private ProgressBar progressLoading;
    private LinearLayout layoutBottomBar;
    private SeekBar seekPage;
    private TextView txtPageIndicator;

    private PreferenceManager prefManager;
    private BookmarkDao bookmarkDao;
    private RecentFileDao recentFileDao;

    private String filePath;
    private int currentPage = 0;
    private int pageCount = 0;
    private boolean isDarkMode = false;
    private int scrollMode = PreferenceManager.SCROLL_MODE_VERTICAL;
    private boolean isBarsVisible = true;

    private MenuItem menuBookmark;
    private MenuItem menuDarkMode;
    private MenuItem menuScrollMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        prefManager = PreferenceManager.getInstance(this);
        bookmarkDao = new BookmarkDao(DatabaseHelper.getInstance(this));
        recentFileDao = new RecentFileDao(DatabaseHelper.getInstance(this));

        isDarkMode = prefManager.isDarkMode();
        scrollMode = prefManager.getScrollMode();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        resolveFilePath();

        if (filePath != null) {
            currentPage = prefManager.getReadingPosition(filePath);
            loadPdf();
        } else {
            Toast.makeText(this, R.string.error_open_pdf, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        pdfView = (PDFView) findViewById(R.id.pdfView);
        progressLoading = (ProgressBar) findViewById(R.id.progress_loading_pdf);
        layoutBottomBar = (LinearLayout) findViewById(R.id.layout_bottom_bar);
        seekPage = (SeekBar) findViewById(R.id.seek_page);
        txtPageIndicator = (TextView) findViewById(R.id.txt_page_indicator);

        seekPage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && pageCount > 0) {
                    pdfView.jumpTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void resolveFilePath() {
        Intent intent = getIntent();
        if (intent == null) return;

        if (intent.hasExtra(MainActivity.EXTRA_FILE_PATH)) {
            filePath = intent.getStringExtra(MainActivity.EXTRA_FILE_PATH);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null) {
                if ("file".equalsIgnoreCase(uri.getScheme())) {
                    filePath = uri.getPath();
                } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                    filePath = copyContentUriToTempFile(uri);
                }
            }
        }

        if (filePath != null) {
            File f = new File(filePath);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(f.getName());
            }
        }
    }

    private String copyContentUriToTempFile(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            if (in == null) return null;

            File tempFile = new File(getCacheDir(), "opened_document.pdf");
            FileOutputStream out = new FileOutputStream(tempFile);
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
            out.close();
            in.close();
            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private void loadPdf() {
        if (filePath == null) return;

        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(this, R.string.error_file_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressLoading.setVisibility(View.VISIBLE);

        boolean isSwipeHorizontal = (scrollMode == PreferenceManager.SCROLL_MODE_PAGE);

        pdfView.setBackgroundColor(isDarkMode ? Color.parseColor("#1A1A1A") : Color.parseColor("#F5F5F5"));

        pdfView.fromFile(file)
                .defaultPage(currentPage)
                .onPageChange(this)
                .onLoad(this)
                .onError(this)
                .onTap(this)
                .enableSwipe(true)
                .swipeHorizontal(isSwipeHorizontal)
                .enableDoubletap(true)
                .nightMode(isDarkMode)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        this.currentPage = page;
        this.pageCount = pageCount;

        seekPage.setMax(Math.max(0, pageCount - 1));
        seekPage.setProgress(page);

        txtPageIndicator.setText(getString(R.string.page_indicator_format, page + 1, pageCount));
        updateBookmarkIcon();
    }

    @Override
    public void loadComplete(int nbPages) {
        progressLoading.setVisibility(View.GONE);
        this.pageCount = nbPages;
        seekPage.setMax(Math.max(0, nbPages - 1));
        seekPage.setProgress(currentPage);
        txtPageIndicator.setText(getString(R.string.page_indicator_format, currentPage + 1, nbPages));

        // Save into recent files
        if (filePath != null) {
            File f = new File(filePath);
            RecentFile recent = new RecentFile(filePath, f.getName(), f.length(), currentPage);
            recentFileDao.insertOrUpdate(recent);
        }
    }

    @Override
    public void onError(Throwable t) {
        progressLoading.setVisibility(View.GONE);
        Toast.makeText(this, R.string.error_corrupted, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onTap(android.view.MotionEvent e) {
        toggleBars();
        return true;
    }

    private void toggleBars() {
        ActionBar actionBar = getSupportActionBar();
        if (isBarsVisible) {
            if (actionBar != null) actionBar.hide();
            layoutBottomBar.setVisibility(View.GONE);
            isBarsVisible = false;
        } else {
            if (actionBar != null) actionBar.show();
            layoutBottomBar.setVisibility(View.VISIBLE);
            isBarsVisible = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_viewer, menu);
        menuBookmark = menu.findItem(R.id.action_bookmark);
        menuDarkMode = menu.findItem(R.id.action_dark_mode);
        menuScrollMode = menu.findItem(R.id.action_scroll_mode);

        updateDarkModeMenu();
        updateScrollModeMenu();
        updateBookmarkIcon();
        return true;
    }

    private void updateDarkModeMenu() {
        if (menuDarkMode != null) {
            menuDarkMode.setTitle(isDarkMode ? R.string.dark_mode_off : R.string.dark_mode_on);
            menuDarkMode.setIcon(isDarkMode ? android.R.drawable.ic_menu_day : android.R.drawable.ic_menu_view);
        }
    }

    private void updateScrollModeMenu() {
        if (menuScrollMode != null) {
            boolean isPageMode = (scrollMode == PreferenceManager.SCROLL_MODE_PAGE);
            menuScrollMode.setTitle(isPageMode ? R.string.scroll_mode_vertical : R.string.scroll_mode_page);
        }
    }

    private void updateBookmarkIcon() {
        if (menuBookmark == null || filePath == null) return;
        boolean exists = bookmarkDao.exists(filePath, currentPage);
        menuBookmark.setIcon(exists ? android.R.drawable.star_on : android.R.drawable.star_off);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        if (id == R.id.action_dark_mode) {
            isDarkMode = !isDarkMode;
            prefManager.setDarkMode(isDarkMode);
            updateDarkModeMenu();
            loadPdf();
            return true;
        }

        if (id == R.id.action_toc) {
            OutlineDialog.show(this, pdfView.getTableOfContents(), new OutlineDialog.OnOutlineSelectedListener() {
                @Override
                public void onOutlineSelected(int pageIndex) {
                    if (pageIndex >= 0 && pageIndex < pageCount) {
                        pdfView.jumpTo(pageIndex);
                    }
                }
            });
            return true;
        }

        if (id == R.id.action_bookmark) {
            toggleCurrentBookmark();
            return true;
        }

        if (id == R.id.action_scroll_mode) {
            scrollMode = (scrollMode == PreferenceManager.SCROLL_MODE_VERTICAL)
                    ? PreferenceManager.SCROLL_MODE_PAGE
                    : PreferenceManager.SCROLL_MODE_VERTICAL;
            prefManager.setScrollMode(scrollMode);
            updateScrollModeMenu();
            loadPdf();
            return true;
        }

        if (id == R.id.action_go_to_page) {
            GoToPageDialog.show(this, currentPage, Math.max(1, pageCount), new GoToPageDialog.OnPageSelectedListener() {
                @Override
                public void onPageSelected(int pageIndex) {
                    pdfView.jumpTo(pageIndex);
                }
            });
            return true;
        }

        if (id == R.id.action_view_bookmarks) {
            BookmarksDialog.show(this, filePath, bookmarkDao, new BookmarksDialog.OnBookmarkSelectedListener() {
                @Override
                public void onBookmarkSelected(int pageIndex) {
                    pdfView.jumpTo(pageIndex);
                }

                @Override
                public void onBookmarkDeleted() {
                    updateBookmarkIcon();
                }
            });
            return true;
        }

        if (id == R.id.action_file_info) {
            if (filePath != null) {
                File f = new File(filePath);
                PdfFile pdfFile = new PdfFile(f);
                pdfFile.setPageCount(pageCount);
                FileInfoDialog.show(this, pdfFile);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleCurrentBookmark() {
        if (filePath == null) return;
        boolean exists = bookmarkDao.exists(filePath, currentPage);
        if (exists) {
            bookmarkDao.delete(filePath, currentPage);
            Toast.makeText(this, R.string.bookmark_removed, Toast.LENGTH_SHORT).show();
        } else {
            bookmarkDao.insert(new Bookmark(filePath, currentPage));
            Toast.makeText(this, R.string.bookmark_added, Toast.LENGTH_SHORT).show();
        }
        updateBookmarkIcon();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (filePath != null) {
            prefManager.saveReadingPosition(filePath, currentPage);
            recentFileDao.updateLastPage(filePath, currentPage);
        }
    }
}
