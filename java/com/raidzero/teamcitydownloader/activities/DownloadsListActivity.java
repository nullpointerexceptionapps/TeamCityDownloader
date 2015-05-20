package com.raidzero.teamcitydownloader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.adapters.DownloadedFileAdapter;
import com.raidzero.teamcitydownloader.data.DownloadedFile;
import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.DialogUtility;
import com.raidzero.teamcitydownloader.global.DownloadHelper;
import com.raidzero.teamcitydownloader.global.DownloadObserver;
import com.raidzero.teamcitydownloader.global.FileUtility;
import com.raidzero.teamcitydownloader.global.ThemeUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by posborn on 11/24/14.
 */
public class DownloadsListActivity extends ActionBarActivity implements
        AdapterView.OnItemClickListener, View.OnCreateContextMenuListener, DownloadObserver.DownloadObserverListener {
    private static final String tag = "DownloadsListActivity";

    private final ArrayList<DownloadedFile> loadedFiles = new ArrayList<DownloadedFile>();

    private ProgressBar progressBar;
    private ListView list;
    private TextView txtNoFiles;

    private String storagePath = null;

    private DownloadedFileAdapter adapter;
    private ActionBar actionBar;
    private AppHelper helper;
    private Intent launchIntent;

    private int notificationId = -1;

    private DownloadObserver downloadObserver;
    private DownloadHelper downloadHelper;

    private static final int CONTEXT_MENU_DELETE = 100;
    private static final int CONTEXT_MENU_CANCEL = 101;

    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemeUtility.setAppTheme(this);
        super.onCreate(savedInstanceState);

        launchIntent = getIntent();
        notificationId = launchIntent.getIntExtra("notificationId", -1);

        helper = (AppHelper) getApplicationContext();
        setContentView(R.layout.downloads_list);

        storagePath = helper.getStorageDirectory();

        downloadObserver = new DownloadObserver(this, storagePath);
        downloadHelper = helper.getDownloadHelper();

        progressBar = (ProgressBar) findViewById(R.id.loadProgress);
        list = (ListView) findViewById(android.R.id.list);
        txtNoFiles = (TextView) findViewById(R.id.noDownloadedFilesFound);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                int progressVisibility = bundle.getInt("progressVisibility");
                showProgress(progressVisibility);

                int filesLoaded = bundle.getInt("filesLoaded");
                if (filesLoaded > 0) {
                    txtNoFiles.setVisibility(View.GONE);
                    onDownloadedFilesLoaded();
                } else {
                    txtNoFiles.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        downloadObserver.startWatching();
        adapter = new DownloadedFileAdapter(this, loadedFiles, notificationId);
        list.setAdapter(adapter);

        list.setOnItemClickListener(this);
        list.setOnCreateContextMenuListener(this);

        loadFiles();
    }

    @Override
    public void onPause() {
        super.onPause();
        downloadObserver.stopWatching();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.downloaded_files, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.downloaded_files_clear) {
            boolean downloadsCanceled = false;

            for (DownloadedFile f : loadedFiles) {
                if (f.isDownloading()) {
                    helper.serviceCancelDownload(f.getFileName(), false);
                    downloadsCanceled = true;
                }
            }

            downloadHelper.clearCurrentRequests();

            int filesDeleted = FileUtility.deleteAllFiles(storagePath);

            helper.serviceRemoveAllNotifications();
            helper.refreshNavItems();

            String toastMsg = "";

            if (filesDeleted > 0) {
                toastMsg = String.format(getString(R.string.all_files_deleted), filesDeleted);

                if (downloadsCanceled) {
                    toastMsg += ", " + getString(R.string.all_downloads_canceled);
                }
            } else {
                toastMsg = getString(R.string.no_files_to_delete);
            }

            DialogUtility.makeToast(this, toastMsg);

            DownloadsListActivity.this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;

        int position = info.position;
        DownloadedFile selectedItem = loadedFiles.get(position);
        menu.setHeaderTitle(selectedItem.getFileName());

        // show cancel if downloading, delete otherwise
        if (selectedItem.isDownloading()) {
            menu.add(0, CONTEXT_MENU_CANCEL, 0, android.R.string.cancel);
        } else {
            menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.downloaded_file_delete);
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        DownloadedFile selectedFile = loadedFiles.get(info.position);

        int itemId = item.getItemId();

        switch (itemId) {
            case CONTEXT_MENU_CANCEL:
                selectedFile.setIsDownloading(false);
                helper.serviceCancelDownload(selectedFile.getFileName());
                deleteDownloadedFile(selectedFile);
                break;
            case CONTEXT_MENU_DELETE:
                deleteDownloadedFile(selectedFile);
                break;
        }

        return super.onContextItemSelected(item);
    }

    private void deleteDownloadedFile(DownloadedFile file) {
        FileUtility.deleteFile(file.getFilePath());

        if (file.isDownloading()) {
            helper.serviceCancelDownload(file.getFileName());
        }

        loadedFiles.remove(file);
        onDownloadedFilesLoaded();
        helper.refreshNavItems();

        if (loadedFiles.size() == 0) {
            finish();
        }
    }

    public void onDownloadedFilesLoaded() {
        // sort and prepare to bold if necessary
        Collections.sort(loadedFiles);

        // did we come from a notification click?
        if (notificationId > -1) {
            for (DownloadedFile f : loadedFiles) {
                f.setIsNew(helper.shouldBoldFile(f.getFileName(), notificationId));
            }
        }

        updateAdapter(loadedFiles);

        showProgress(View.GONE);
    }

    private void updateAdapter(final ArrayList<DownloadedFile> filesToDisplay) {
        final ArrayList<DownloadedFile> files = new ArrayList<DownloadedFile>(filesToDisplay);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                adapter.addAll(files);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void showProgress(final int visibility) {
        progressBar.setVisibility(visibility);

        if (visibility == View.VISIBLE) {
            list.setVisibility(View.GONE);
        } else {
            list.setVisibility(View.VISIBLE);
        }
    }

    private void loadFiles() {
        final Bundle bundle = new Bundle();
        loadedFiles.clear();

        Runnable loadFiles = new Runnable() {
            @Override
            public void run() {
                // show progress
                Message msg = handler.obtainMessage();
                bundle.putInt("progressVisibility", View.VISIBLE);
                msg.setData(bundle);
                handler.sendMessage(msg);

                File storage = new File(storagePath);
                File[] files = storage.listFiles();

                int filesLoaded = 0;

                if (files != null) {
                    for (File f : files) {
                        Debug.Log(tag, "Reading file " + f.getName() + "...");

                        final DownloadedFile newFile = new DownloadedFile(DownloadsListActivity.this, f);
                        if (helper.isFileDownloading(f.getName())) {
                            newFile.setIsDownloading(true);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadedFiles.add(newFile);
                            }
                        });

                        filesLoaded++;
                        Debug.Log(tag, "loaded file: " + f.toString());
                    }
                }

                msg = handler.obtainMessage();
                bundle.putInt("progressVisibility", View.GONE);
                bundle.putInt("filesLoaded", filesLoaded);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        };

        new Thread(loadFiles).start();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DownloadedFile file = loadedFiles.get(position);

        if (!file.isDownloading()) {
            helper.markFileSeen(file.getFileName(), notificationId);
            FileUtility.openFile(this, file.getFilePath());
        }
    }

    // handle back button -
    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(i, 0);
        super.onBackPressed();
    }

    @Override
    public void onFileCreated(String path) {
        setFileDownloading(path, true);
        updateAdapter(loadedFiles);
    }

    @Override
    public void onFileClosed(String path) {
        setFileDownloading(path, false);
        updateAdapter(loadedFiles);
    }

    private void setFileDownloading(String path, boolean downloading) {
        Debug.Log(tag, String.format("setFileDownloading(%s, %b)", path, downloading));
        for (DownloadedFile f : loadedFiles) {
            if (f.getFileName().equals(path)) {
                Debug.Log(tag, "downloading: " + downloading);
                f.setIsDownloading(downloading);

                if (!downloading) {
                    f.rename(path);
                    f.updateIcon();
                }

                break;
            }
        }
    }
}
