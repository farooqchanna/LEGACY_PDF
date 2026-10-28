package com.litepdf.reader.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.litepdf.reader.R;
import com.litepdf.reader.adapters.PdfGridAdapter;
import com.litepdf.reader.adapters.PdfListAdapter;
import com.litepdf.reader.adapters.RecentPdfAdapter;
import com.litepdf.reader.database.DatabaseHelper;
import com.litepdf.reader.database.RecentFileDao;
import com.litepdf.reader.dialogs.FileInfoDialog;
import com.litepdf.reader.dialogs.SortDialog;
import com.litepdf.reader.models.PdfFile;
import com.litepdf.reader.models.RecentFile;
import com.litepdf.reader.utils.PdfScanner;
import com.litepdf.reader.utils.PreferenceManager;
import com.litepdf.reader.utils.SortUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        PdfGridAdapter.OnPdfClickListener,
        PdfListAdapter.OnPdfClickListener,
        RecentPdfAdapter.OnRecentClickListener {

    private static final int REQUEST_STORAGE_PERMISSION = 101;
    public static final int REQUEST_CODE_SELECT_FOLDER = 102;
    public static final String EXTRA_FILE_PATH = "extra_file_path";

    private PreferenceManager prefManager;
    private RecentFileDao recentFileDao;
    private PdfScanner pdfScanner;

    private View layoutRecentSection;
    private RecyclerView recyclerRecent;
    private RecentPdfAdapter recentAdapter;

    private RecyclerView recyclerAllPdfs;
    private PdfGridAdapter gridAdapter;
    private PdfListAdapter listAdapter;

    private TextView txtCurrentFolder;
    private View btnClearFolder;
    private ProgressBar progressScan;
    private TextView txtPdfCount;
    private View layoutEmptyState;
    private TextView txtEmptyMessage;

    private final List<PdfFile> allPdfsList = new ArrayList<PdfFile>();
    private final List<PdfFile> displayedPdfsList = new ArrayList<PdfFile>();

    private int currentViewMode;
    private int currentSortMode;
    private String currentFolderPath;
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefManager = PreferenceManager.getInstance(this);
        recentFileDao = new RecentFileDao(DatabaseHelper.getInstance(this));
        pdfScanner = new PdfScanner(this);

        currentViewMode = prefManager.getViewMode();
        currentSortMode = prefManager.getSortMode();
        currentFolderPath = prefManager.getLastSelectedFolder();

        initViews();
        setupRecentRecycler();
        setupAllPdfsRecycler();
        setupFolderBar();

        checkPermissionsAndScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentFiles();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pdfScanner != null) {
            pdfScanner.cancel();
        }
    }

    private void initViews() {
        layoutRecentSection = findViewById(R.id.layout_recent_section);
        recyclerRecent = (RecyclerView) findViewById(R.id.recycler_recent);
        recyclerAllPdfs = (RecyclerView) findViewById(R.id.recycler_all_pdfs);
        txtCurrentFolder = (TextView) findViewById(R.id.txt_current_folder);
        btnClearFolder = findViewById(R.id.btn_clear_folder);
        progressScan = (ProgressBar) findViewById(R.id.progress_scan);
        txtPdfCount = (TextView) findViewById(R.id.txt_pdf_count);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        txtEmptyMessage = (TextView) findViewById(R.id.txt_empty_message);
    }

    private void setupRecentRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerRecent.setLayoutManager(layoutManager);
        recentAdapter = new RecentPdfAdapter(this, new ArrayList<RecentFile>());
        recentAdapter.setOnRecentClickListener(this);
        recyclerRecent.setAdapter(recentAdapter);
    }

    private void setupAllPdfsRecycler() {
        gridAdapter = new PdfGridAdapter(this, displayedPdfsList);
        gridAdapter.setOnPdfClickListener(this);

        listAdapter = new PdfListAdapter(this, displayedPdfsList);
        listAdapter.setOnPdfClickListener(this);

        applyViewModeLayout();
    }

    private void setupFolderBar() {
        findViewById(R.id.layout_folder_picker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FolderPickerActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SELECT_FOLDER);
            }
        });

        btnClearFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFolderPath = null;
                prefManager.setLastSelectedFolder(null);
                updateFolderBarDisplay();
                startScan();
            }
        });

        updateFolderBarDisplay();
    }

    private void updateFolderBarDisplay() {
        if (currentFolderPath != null && !currentFolderPath.isEmpty()) {
            File folder = new File(currentFolderPath);
            txtCurrentFolder.setText("📁 " + folder.getName());
            btnClearFolder.setVisibility(View.VISIBLE);
        } else {
            txtCurrentFolder.setText(R.string.btn_select_folder);
            btnClearFolder.setVisibility(View.GONE);
        }
    }

    private void applyViewModeLayout() {
        if (currentViewMode == PreferenceManager.VIEW_MODE_GRID) {
            recyclerAllPdfs.setLayoutManager(new GridLayoutManager(this, 3));
            recyclerAllPdfs.setAdapter(gridAdapter);
        } else {
            recyclerAllPdfs.setLayoutManager(new LinearLayoutManager(this));
            recyclerAllPdfs.setAdapter(listAdapter);
        }
    }

    private void checkPermissionsAndScan() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
                return;
            }
        }
        startScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan();
            } else {
                Toast.makeText(this, R.string.error_permission_needed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startScan() {
        progressScan.setVisibility(View.VISIBLE);

        PdfScanner.ScanListener scanListener = new PdfScanner.ScanListener() {
            @Override
            public void onScanStarted() {
                progressScan.setVisibility(View.VISIBLE);
            }

            @Override
            public void onScanProgress(int foundCount) {
                txtPdfCount.setText(getString(R.string.file_count_format, foundCount));
            }

            @Override
            public void onScanCompleted(List<PdfFile> pdfFiles) {
                progressScan.setVisibility(View.GONE);
                allPdfsList.clear();
                if (pdfFiles != null) {
                    allPdfsList.addAll(pdfFiles);
                }
                filterAndSortList();
            }
        };

        if (currentFolderPath != null && !currentFolderPath.isEmpty()) {
            File folder = new File(currentFolderPath);
            if (folder.exists() && folder.isDirectory()) {
                pdfScanner.scanFolder(folder, scanListener);
                return;
            }
        }
        pdfScanner.scanAllPdfs(scanListener);
    }

    private void loadRecentFiles() {
        recentFileDao.removeDeletedFiles();
        List<RecentFile> recents = recentFileDao.getRecent(10);
        if (recents != null && !recents.isEmpty()) {
            layoutRecentSection.setVisibility(View.VISIBLE);
            recentAdapter.setItems(recents);
        } else {
            layoutRecentSection.setVisibility(View.GONE);
        }
    }

    private void filterAndSortList() {
        displayedPdfsList.clear();

        if (currentSearchQuery.isEmpty()) {
            displayedPdfsList.addAll(allPdfsList);
        } else {
            String queryLower = currentSearchQuery.toLowerCase(Locale.getDefault());
            for (PdfFile file : allPdfsList) {
                if (file.getName().toLowerCase(Locale.getDefault()).contains(queryLower)) {
                    displayedPdfsList.add(file);
                }
            }
        }

        SortUtils.sort(displayedPdfsList, currentSortMode);

        gridAdapter.notifyDataSetChanged();
        listAdapter.notifyDataSetChanged();

        txtPdfCount.setText(getString(R.string.file_count_format, displayedPdfsList.size()));

        if (displayedPdfsList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            if (!currentSearchQuery.isEmpty()) {
                txtEmptyMessage.setText(R.string.empty_no_results);
            } else if (currentFolderPath != null) {
                txtEmptyMessage.setText(R.string.empty_folder_no_pdfs);
            } else {
                txtEmptyMessage.setText(R.string.empty_no_pdfs);
            }
        } else {
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Configure SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.search_hint));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    currentSearchQuery = query != null ? query.trim() : "";
                    filterAndSortList();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    currentSearchQuery = newText != null ? newText.trim() : "";
                    filterAndSortList();
                    return true;
                }
            });
        }

        updateViewModeMenuIcon(menu.findItem(R.id.action_view_mode));

        return true;
    }

    private void updateViewModeMenuIcon(MenuItem item) {
        if (item == null) return;
        if (currentViewMode == PreferenceManager.VIEW_MODE_GRID) {
            item.setIcon(android.R.drawable.ic_menu_agenda);
            item.setTitle(R.string.view_list);
        } else {
            item.setIcon(android.R.drawable.ic_dialog_dialer);
            item.setTitle(R.string.view_grid);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_view_mode) {
            currentViewMode = (currentViewMode == PreferenceManager.VIEW_MODE_GRID)
                    ? PreferenceManager.VIEW_MODE_LIST
                    : PreferenceManager.VIEW_MODE_GRID;
            prefManager.setViewMode(currentViewMode);
            applyViewModeLayout();
            updateViewModeMenuIcon(item);
            return true;
        }

        if (id == R.id.action_sort) {
            SortDialog.show(this, currentSortMode, new SortDialog.OnSortSelectedListener() {
                @Override
                public void onSortSelected(int sortMode) {
                    currentSortMode = sortMode;
                    prefManager.setSortMode(sortMode);
                    filterAndSortList();
                }
            });
            return true;
        }

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPdfClick(PdfFile pdfFile) {
        openPdf(pdfFile.getPath());
    }

    @Override
    public void onPdfLongClick(PdfFile pdfFile) {
        FileInfoDialog.show(this, pdfFile);
    }

    @Override
    public void onRecentClick(RecentFile recentFile) {
        openPdf(recentFile.getFilePath());
    }

    private void openPdf(String filePath) {
        if (filePath == null) return;
        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(this, R.string.error_file_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_FOLDER && resultCode == RESULT_OK && data != null) {
            String selectedFolder = data.getStringExtra(FolderPickerActivity.EXTRA_SELECTED_FOLDER);
            if (selectedFolder != null) {
                currentFolderPath = selectedFolder;
                prefManager.setLastSelectedFolder(selectedFolder);
                updateFolderBarDisplay();
                startScan();
            }
        }
    }
}
