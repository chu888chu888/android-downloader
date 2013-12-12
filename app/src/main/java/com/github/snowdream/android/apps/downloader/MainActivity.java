/*******************************************************************************
 * Copyright (C) 2013 Snowdream Mobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.github.snowdream.android.apps.downloader;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.snowdream.android.app.DownloadListener;
import com.github.snowdream.android.app.DownloadManager;
import com.github.snowdream.android.app.DownloadStatus;
import com.github.snowdream.android.app.DownloadTask;
import com.github.snowdream.android.util.Log;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author snowdream <yanghui1986527@gmail.com>
 * @version v1.0
 * @date Sep 29, 2013
 */
//@EActivity(R.layout.activity_main)
public class MainActivity extends ListActivity implements MenuAdapter.MenuListener {
    private MenuDrawer mDrawer;
    private MenuAdapter mAdapter;
    private ListView mList;
    private int mActivePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.setPath("/mnt/sdcard/snowdream/log","log","log");
        Log.setPolicy(Log.LOG_ERROR_TO_FILE);

        //setContentView(R.layout.activity_main);
        mDrawer = MenuDrawer.attach(this);
        mDrawer.setSlideDrawable(R.drawable.ic_drawer);
        mDrawer.setMenuSize(250);


        mDrawer.setDrawerIndicatorEnabled(true);

        List<Object> items = new ArrayList<Object>();
        items.add(new MenuItem("All", -1));
        items.add(new MenuItem("Downloading", -1));
        items.add(new MenuItem("Finished", -1));
        items.add(new MenuItem("Trash", -1));

        mList = new ListView(this);
        mAdapter = new MenuAdapter(this, items);
        mAdapter.setListener(this);
        mAdapter.setActivePosition(mActivePosition);

        mList.setAdapter(mAdapter);
        mDrawer.setMenuView(mList);
        mList.setOnItemClickListener(mItemClickListener);

        TextView content = new TextView(this);
        content.setText("This is a sample of an overlayed left drawer.");
        content.setGravity(Gravity.CENTER);
        mDrawer.setContentView(content);
        mDrawer.setSlideDrawable(R.drawable.ic_drawer);
        mDrawer.setDrawerIndicatorEnabled(true);

        List<String> items1;
        items1 = new ArrayList<String>();
        for (int i = 1; i <= 20; i++) {
            items1.add("MenuItem " + i);
        }

        List<DownloadTask> list = new ArrayList<DownloadTask>();

        DownloadTask task = new DownloadTask(this);
        task.setUrl("http://192.168.30.131:8080/snowdream/HDExplorer_0.1.3_signed.apk");
        task.setPath("/mnt/sdcard/HDExplorer_0.1.3_signed.apk");
        DownloadManager.add(task);
        list.add(task);

        DownloadTaskAdapter adapter = new DownloadTaskAdapter(this, list);
        setListAdapter(adapter);
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mActivePosition = position;
            mDrawer.setActiveView(view, position);
            mAdapter.setActivePosition(position);
            mDrawer.closeMenu();
        }
    };

    @Override
    public void setContentView(int layoutResID) {
        // This override is only needed when using MENU_DRAG_CONTENT.
        mDrawer.setContentView(layoutResID);
        onContentChanged();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        DownloadTask task = (DownloadTask) getListAdapter().getItem(position);

        if (task == null) {
            return;
        }

        switch (task.getStatus()) {
            case DownloadStatus.STATUS_PENDING:
            case DownloadStatus.STATUS_FAILED:
            case DownloadStatus.STATUS_STOPPED:
                DownloadManager.start(this,task, new DownloadListener<Integer, DownloadTask>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        Log.i("onStart");
                    }

                    @Override
                    public void onProgressUpdate(Integer... values) {
                        super.onProgressUpdate(values);
                        ((DownloadTaskAdapter)getListAdapter()).notifyDataSetChanged();
                        Log.i("onProgressUpdate");
                    }

                    @Override
                    public void onSuccess(DownloadTask downloadTask) {
                        super.onSuccess(downloadTask);
                        Log.i("onSuccess");
                    }

                    @Override
                    public void onCancelled() {
                        super.onCancelled();
                        Log.i("onCancelled");
                    }

                    @Override
                    public void onError(Throwable thr) {
                        super.onError(thr);
                        Log.i("onError");
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        Log.i("onFinish");
                    }
                });
                break;
            case DownloadStatus.STATUS_RUNNING:
                DownloadManager.stop(task);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                mDrawer.toggleMenu();
                return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        final int drawerState = mDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            mDrawer.closeMenu();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onActiveViewChanged(View v) {
        mDrawer.setActiveView(v, mActivePosition);
    }
}
