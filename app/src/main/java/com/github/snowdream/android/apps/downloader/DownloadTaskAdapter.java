package com.github.snowdream.android.apps.downloader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.snowdream.android.app.DownloadTask;
import com.github.snowdream.android.util.Log;

import java.io.File;
import java.util.List;

public class DownloadTaskAdapter extends BaseAdapter {
    private Context mContext;
    private List<DownloadTask> mItems;

    public DownloadTaskAdapter(Context context, List<DownloadTask> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Object item = getItem(position);

        if (item == null || !(item instanceof DownloadTask)) {
            Log.e("Error");
            return null;
        }

        DownloadTask task = (DownloadTask) item;


        if (v == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.list_row_downloadtask, parent, false);
        }

        TextView title = (TextView) v.findViewById(R.id.title);
        TextView size = (TextView) v.findViewById(R.id.size);
        ProgressBar bar = (ProgressBar) v.findViewById(R.id.progress);

        long filesize = 0l;
        File file = new File(task.getPath());
        if (file.exists()) {
            filesize = file.length();
        }

        title.setText(task.getName());
        size.setText(filesize + "/" + task.getSize());

        int progress = 0;
        if (task.getSize() > 0) {
            progress = (int) (filesize * 100 / task.getSize());
        }

        bar.setProgress(progress);

        return v;
    }
}
