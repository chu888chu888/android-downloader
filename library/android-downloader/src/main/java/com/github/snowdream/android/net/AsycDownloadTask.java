
package com.github.snowdream.android.net;

import android.os.Handler;
import android.os.Message;
import android.webkit.URLUtil;

import com.github.snowdream.android.util.Log;
import com.github.snowdream.android.util.concurrent.AsyncTask;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AsycDownloadTask extends AsyncTask<DownloadTask, Integer, DownloadTask> {
    OkHttpClient client = null;
    private static final InternalHandler sHandler = new InternalHandler();

    private static final int MESSAGE_POST_ERROR = 0x1;

    public AsycDownloadTask(DownloadListener<Integer, DownloadTask> listener) {
        super(listener);
        client = new OkHttpClient();
    }

    /**
     * TODO if error occurs,carry it out. if (listener != null) {
     * listener.onError(new Throwable()); }
     */
    protected DownloadTask doInBackground(DownloadTask... tasks) {
        if (tasks.length <= 0) {
            if (!isCancelled()) {
                sHandler.obtainMessage(
                        MESSAGE_POST_ERROR,
                        new AsyncTaskResult(this,
                                DownloadException.DOWNLOAD_TASK_NOT_VALID)).sendToTarget();
            }
        }

        DownloadTask task = tasks[0];

        if (task == null || task.getUrl() == null || !URLUtil.isValidUrl(task.getUrl())) {
            if (!isCancelled()) {
                sHandler.obtainMessage(
                        MESSAGE_POST_ERROR,
                        new AsyncTaskResult(this,
                                DownloadException.DOWNLOAD_TASK_NOT_VALID)).sendToTarget();
            }

            return task;
        }

        String path = task.getPath();
        File file = new File(path);

        if (!file.canWrite()) {
            if (!isCancelled()) {
                sHandler.obtainMessage(
                        MESSAGE_POST_ERROR,
                        new AsyncTaskResult(this,
                                DownloadException.DOWNLOAD_TASK_NOT_VALID)).sendToTarget();
            }

            return task;
        }

        try {
            InputStream in = null;
            RandomAccessFile out = null;
            long range = 0;
            
            if (!file.exists()) {
                file.createNewFile();
            }
            
            range = file.length();
            
            URL url = new URL(task.getUrl());
            HttpURLConnection connection = client.open(url);
            connection.setRequestProperty("User-Agent", "Snowdream Mobile");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestMethod("GET");

            if (range > 0) {
                connection.setRequestProperty("RANGE", "bytes=" + range + "-");
            }

            try {
                out = new RandomAccessFile(file, "rw");
                out.seek(range);

                in = connection.getInputStream();

                byte[] buffer = new byte[1024];
                int nRead = 0;
                while ((nRead = in.read(buffer, 0, 1024)) > 0)
                {
                    out.write(buffer, 0, nRead);
                    Log.i("cur size:"+(range + nRead));
                    
                    if (isCancelled())
                        break;
                }
            } finally {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }
                connection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return task;
    }

    /**
     * throw error
     * 
     * @param mData
     */
    private void OnError(Integer code) {
        if (listener != null) {
            listener.onError(new DownloadException(code));
        }
        
    }

    private static class InternalHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            AsyncTaskResult result = (AsyncTaskResult) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_ERROR:
                    ((AsycDownloadTask) result.mTask).OnError(result.mData[0]);
                    break;
                default:
                    break;
            }
        }
    }

    private static class AsyncTaskResult {
        @SuppressWarnings("rawtypes")
        final AsyncTask mTask;
        final Integer[] mData;

        AsyncTaskResult(@SuppressWarnings("rawtypes")
        AsyncTask task, Integer... data) {
            mTask = task;
            mData = data;
        }
    }
}
