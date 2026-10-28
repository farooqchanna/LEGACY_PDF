package com.litepdf.reader.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.litepdf.reader.R;
import com.litepdf.reader.database.DatabaseHelper;
import com.litepdf.reader.database.RecentFileDao;
import com.litepdf.reader.utils.PreferenceManager;
import com.litepdf.reader.utils.ThumbnailCache;

public class SettingsActivity extends AppCompatActivity {

    private PreferenceManager prefManager;
    private RecentFileDao recentFileDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_settings);
        }

        prefManager = PreferenceManager.getInstance(this);
        recentFileDao = new RecentFileDao(DatabaseHelper.getInstance(this));

        initViewModeGroup();
        initScrollModeGroup();
        initClearButtons();
    }

    private void initViewModeGroup() {
        RadioGroup rgViewMode = (RadioGroup) findViewById(R.id.rg_view_mode);
        if (prefManager.getViewMode() == PreferenceManager.VIEW_MODE_GRID) {
            rgViewMode.check(R.id.rb_view_grid);
        } else {
            rgViewMode.check(R.id.rb_view_list);
        }

        rgViewMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_view_grid) {
                    prefManager.setViewMode(PreferenceManager.VIEW_MODE_GRID);
                } else if (checkedId == R.id.rb_view_list) {
                    prefManager.setViewMode(PreferenceManager.VIEW_MODE_LIST);
                }
            }
        });
    }

    private void initScrollModeGroup() {
        RadioGroup rgScrollMode = (RadioGroup) findViewById(R.id.rg_scroll_mode);
        if (prefManager.getScrollMode() == PreferenceManager.SCROLL_MODE_VERTICAL) {
            rgScrollMode.check(R.id.rb_scroll_vertical);
        } else {
            rgScrollMode.check(R.id.rb_scroll_page);
        }

        rgScrollMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_scroll_vertical) {
                    prefManager.setScrollMode(PreferenceManager.SCROLL_MODE_VERTICAL);
                } else if (checkedId == R.id.rb_scroll_page) {
                    prefManager.setScrollMode(PreferenceManager.SCROLL_MODE_PAGE);
                }
            }
        });
    }

    private void initClearButtons() {
        findViewById(R.id.btn_clear_recent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(R.string.settings_clear_recent)
                        .setMessage(R.string.settings_clear_confirm)
                        .setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                recentFileDao.clearAll();
                                Toast.makeText(SettingsActivity.this, R.string.settings_cleared, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();
            }
        });

        findViewById(R.id.btn_clear_cache).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(R.string.settings_clear_cache)
                        .setMessage(R.string.settings_clear_confirm)
                        .setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ThumbnailCache.getInstance(SettingsActivity.this).clearCache();
                                Toast.makeText(SettingsActivity.this, R.string.settings_cleared, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
