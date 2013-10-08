
package com.github.snowdream.android.net;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.webkit.URLUtil;

import com.github.snowdream.android.net.dao.ISql;
import com.github.snowdream.android.net.dao.ISqlImpl;
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
import java.sql.SQLException;

public class AsycDownloadTask extends AsyncTask<DownloadTask, Integer, DownloadTask> {
    private static final InternalHandler sHandler = new InternalHandler();

    private static final int MESSAGE_POST_ERROR = 0x1;
    private static final int MESSAGE_POST_SUCCESS = 0x2;

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
                        new AsyncTaskResult(this, null,
                                DownloadException.DOWNLOAD_TASK_NOT_VALID)).sendToTarget();
            }

            return null;
        }

        DownloadTask task = tasks[0];

        if (task == null || task.getUrl() == null || !URLUtil.isValidUrl(task.getUrl())) {
            if (!isCancelled()) {
                sHandler.obtainMessage(
                        MESSAGE_POST_ERROR,
                        new AsyncTaskResult(this, task,
                                DownloadException.DOWNLOAD_TASK_NOT_VALID)).sendToTarget();
            }

            return null;
        }

        String path = task.getPath();
        File file = new File(path);

        InputStream in = null;
        RandomAccessFile out = null;
        HttpURLConnection connection = null;

        try {
            long range = 0;
            long size = task.getSize();
            long curSize = 0;
            String contentType = "";

            File dir = file.getParentFile();

            if (!dir.exists()) {
                dir.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

            range = file.length();
            curSize = range;

            if (!file.canWrite()) {
                if (!isCancelled()) {
                    sHandler.obtainMessage(
                            MESSAGE_POST_ERROR,
                            new AsyncTaskResult(this, task,
                                    DownloadException.DOWNLOAD_TASK_NOT_VALID)).sendToTarget();
                }

                return null;
            }

            URL url = new URL(task.getUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Snowdream Mobile");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestMethod("GET");

            if (range > 0) {
                connection.setRequestProperty("Range", "bytes=" + range +
                        "-");
            }
            // String acceptRanges = connection.getHeaderField("Accept-Ranges");

            
            size = connection.getContentLength();
            contentType = connection.getContentType();

            if (task.getSize() != 0 && file.length() == task.getSize()) {
                Log.i("The file has already been downloaded!");
                return task;
            }

            if (range == 0) {
                task.setSize(size);
                task.setStartTime(System.currentTimeMillis());
                task.setMimeType(contentType);
                SaveDownloadTask(task, task.getStatus());
            }

            out = new RandomAccessFile(file, "rw");
            out.seek(range);

            in = new BufferedInputStream(connection.getInputStream());

            byte[] buffer = new byte[1024];
            int nRead = 0;
            while ((nRead = in.read(buffer, 0, 1024)) > 0)
            {
                while (task.getStatus() == DownloadStatus.STATUS_PAUSED) {
                    Thread.sleep(500);
                }

                out.write(buffer, 0, nRead);

                curSize += nRead;
                Log.i("cur size:" + (curSize) + "total size:" + (size));

                if (isCancelled())
                    break;
                
                if ((task.getStatus() != DownloadStatus.STATUS_RUNNING) && (task.getStatus() != DownloadStatus.STATUS_PAUSED)) {
                    break;
                }
            }
            
            if (file.length() != 0 && file.length() == size) {
                if (!isCancelled()) {
                    sHandler.obtainMessage(
                            MESSAGE_POST_SUCCESS,
                            new AsyncTaskResult(this, task, -1)).sendToTarget();
                }
            }
        } catch (MalformedURLException e) {
            if (!isCancelled()) {
                sHandler.obtainMessage(
                        MESSAGE_POST_ERROR,
                        new AsyncTaskResult(this, task,
                                DownloadException.DOWNLOAD_TASK_NOT_VALID)).sendToTarget();
            }
            e.printStackTrace();
        } catch (ProtocolException e) {
            if (!isCancelled()) {
                sHandler.obtainMessage(
                        MESSAGE_POST_ERROR,
                        new AsyncTaskResult(this, task,
                                DownloadException.DOWNLOAD_TASK_NOT_VALID)).sendToTarget();
            }
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            if (!isCancelled()) {
                sHandler.obtainMessage(
                        MESSAGE_POST_ERROR,
                        new AsyncTaskResult(this, task,
                                DownloadException.DOWNLOAD_TASK_NOT_VALID)).sendToTarget();
            }
            e.printStackTrace();
        } catch (IOException e) {
            if (!isCancelled()) {
                sHandler.obtainMessage(
                        MESSAGE_POST_ERROR,
                        new AsyncTaskResult(this, task,
                                DownloadException.DOWNLOAD_TASK_NOT_VALID)).sendToTarget();
            }
            e.printStackTrace();
        } catch (InterruptedException e) {
            if (!isCancelled()) {
                sHandler.obtainMessage(
                        MESSAGE_POST_ERROR,
                        new AsyncTaskResult(this, task,
                                DownloadException.DOWNLOAD_TASK_NOT_VALID)).sendToTarget();
            }
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
    private void OnError(DownloadTask task, Integer code) {
        SaveDownloadTask(task, DownloadStatus.STATUS_FAILED);

        if (listener != null) {
            listener.onError(new DownloadException(code));
        }
    }

    /**
     * finish
     * 
     * @param mData
     */
    private void OnFinish(DownloadTask task) {
        SaveDownloadTask(task, DownloadStatus.STATUS_FINISHED);

        if (listener != null) {
            listener.onSuccess(task);
        }
    } 
    
    
    /**
     * Update the status of the DownloadTask,and save it to the sqlite
     * 
     * @param task
     * @param status
     */
    private void SaveDownloadTask(DownloadTask task, int status) {
        Context context = task.getContext();

        if (context == null) {
            return;
        }

        task.setStatus(status);

        ISql iSql = new ISqlImpl(context);

        try {
            iSql.updateDownloadTask(task);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static class InternalHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            AsyncTaskResult result = (AsyncTaskResult) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_ERROR:
                    ((AsycDownloadTask) result.mTask).OnError(result.mDownloadTask, result.mData);
                    break;
                case MESSAGE_POST_SUCCESS:
                    ((AsycDownloadTask) result.mTask).OnFinish(result.mDownloadTask);
                    break;
                default:
                    break;
            }
        }
    }

    private static class AsyncTaskResult {
        @SuppressWarnings("rawtypes")
        final AsyncTask mTask;
        final Integer mData;
        final DownloadTask mDownloadTask;

        AsyncTaskResult(@SuppressWarnings("rawtypes")
        AsyncTask task, DownloadTask downloadtask, Integer data) {
            mTask = task;
            mData = data;
            mDownloadTask = downloadtask;
        }
    }
}
