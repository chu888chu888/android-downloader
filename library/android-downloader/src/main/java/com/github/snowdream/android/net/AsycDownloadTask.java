
package com.github.snowdream.android.net;

import android.os.Handler;
import android.os.Message;
import android.webkit.URLUtil;

import com.github.snowdream.android.util.Log;
import com.github.snowdream.android.util.concurrent.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class AsycDownloadTask extends AsyncTask<DownloadTask, Integer, DownloadTask> {
    private static final InternalHandler sHandler = new InternalHandler();

    private static final int MESSAGE_POST_ERROR = 0x1;

    public AsycDownloadTask(DownloadListener<Integer, DownloadTask> listener) {
        super(listener);
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

        InputStream in = null;
        RandomAccessFile out = null;
        HttpURLConnection connection = null;

        try {
            long range = 0;
            long size = 0;
            long curSize = 0;

            if (!file.exists()) {
                file.createNewFile();
            }

            if (!file.canWrite()) {
                if (!isCancelled()) {
                    sHandler.obtainMessage(
                            MESSAGE_POST_ERROR,
                            new AsyncTaskResult(this,
                                    DownloadException.DOWNLOAD_TASK_NOT_VALID)).sendToTarget();
                }

                return task;
            }

            range = file.length();
            curSize = range;

            URL url = new URL(task.getUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Snowdream Mobile");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestMethod("GET");

            if (range > 0) {
                connection.setRequestProperty("Range", "bytes=" + range +
                        "-");
            }
            String acceptRanges = connection.getHeaderField("Accept-Ranges");

            size = connection.getContentLength();

            out = new RandomAccessFile(file, "rw");
            out.seek(range);

            in = new BufferedInputStream(connection.getInputStream());

            byte[] buffer = new byte[1024];
            int nRead = 0;
            while ((nRead = in.read(buffer, 0, 1024)) > 0)
            {
                /*
                 * while(paused) { Thread.sleep(500); }
                 */

                out.write(buffer, 0, nRead);

                curSize += nRead;
                Log.i("cur size:" + (curSize) + "total size:" + (size));

                if (isCancelled())
                    break;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (connection != null) {
                connection.disconnect();
            }
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
