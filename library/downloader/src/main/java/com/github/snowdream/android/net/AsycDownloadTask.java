package com.github.snowdream.android.net;

import com.github.snowdream.android.net.DownloadListener;
import com.github.snowdream.android.net.DownloadTask;
import com.github.snowdream.android.util.Log;
import com.github.snowdream.android.util.concurrent.AsyncTask;
import com.github.snowdream.android.util.concurrent.TaskListener;

import java.net.URL;

public class AsycDownloadTask extends AsyncTask<URL, Integer, DownloadTask> {
    public AsycDownloadTask(DownloadListener<Integer, DownloadTask> listener) {
        super(listener);
     }
    
    /**
     * TODO 
     * if error occurs,carry it out.
     * 
     * if (listener != null) {
     *    listener.onError(new Throwable());
     * }
     */
    protected DownloadTask doInBackground(URL... urls) {
         int count = urls.length;
         long totalSize = 0;
         for (int i = 0; i < count; i++) {
             totalSize += 10;
             publishProgress((int) ((i / (float) count) * 100));
             // Escape early if cancel() is called
             if (isCancelled()) break;
             try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
         }
         return totalSize;
     }
}
