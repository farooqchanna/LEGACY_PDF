package com.litepdf.reader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.litepdf.reader.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FolderPickerActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_FOLDER = "extra_selected_folder";

    private TextView txtCurrentPath;
    private RecyclerView recyclerFolders;
    private TextView txtEmptyFolders;
    private FolderAdapter folderAdapter;

    private File currentDirectory;
    private final List<File> currentSubfolders = new ArrayList<File>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_picker);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_folder_picker);
        }

        txtCurrentPath = (TextView) findViewById(R.id.txt_current_path);
        recyclerFolders = (RecyclerView) findViewById(R.id.recycler_folders);
        txtEmptyFolders = (TextView) findViewById(R.id.txt_empty_folders);

        recyclerFolders.setLayoutManager(new LinearLayoutManager(this));
        folderAdapter = new FolderAdapter();
        recyclerFolders.setAdapter(folderAdapter);

        findViewById(R.id.btn_select_this_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDirectory != null) {
                    Intent data = new Intent();
                    data.putExtra(EXTRA_SELECTED_FOLDER, currentDirectory.getAbsolutePath());
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });

        // Start from external storage root
        File root = Environment.getExternalStorageDirectory();
        if (root == null || !root.exists()) {
            root = new File("/storage");
        }
        navigateTo(root);
    }

    private void navigateTo(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }

        currentDirectory = dir;
        txtCurrentPath.setText(dir.getAbsolutePath());

        File[] subdirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() && !file.getName().startsWith(".") && file.canRead();
            }
        });

        currentSubfolders.clear();
        if (subdirs != null && subdirs.length > 0) {
            List<File> list = Arrays.asList(subdirs);
            Collections.sort(list, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
            });
            currentSubfolders.addAll(list);
            txtEmptyFolders.setVisibility(View.GONE);
        } else {
            txtEmptyFolders.setVisibility(View.VISIBLE);
        }

        folderAdapter.notifyDataSetChanged();
        recyclerFolders.scrollToPosition(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleBackNavigation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!handleBackNavigation()) {
            super.onBackPressed();
        }
    }

    private boolean handleBackNavigation() {
        if (currentDirectory != null) {
            File parent = currentDirectory.getParentFile();
            if (parent != null && parent.canRead() && !parent.getAbsolutePath().equals("/")) {
                navigateTo(parent);
                return true;
            }
        }
        return false;
    }

    private class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(FolderPickerActivity.this).inflate(R.layout.item_folder, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final File folder = currentSubfolders.get(position);
            holder.txtFolderName.setText(folder.getName());
            holder.txtFolderPath.setText(folder.getAbsolutePath());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateTo(folder);
                }
            });
        }

        @Override
        public int getItemCount() {
            return currentSubfolders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView txtFolderName;
            final TextView txtFolderPath;

            ViewHolder(View itemView) {
                super(itemView);
                txtFolderName = (TextView) itemView.findViewById(R.id.txt_folder_name);
                txtFolderPath = (TextView) itemView.findViewById(R.id.txt_folder_path);
            }
        }
    }
}
