
package com.github.snowdream.android.net;

import android.content.Context;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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

    private int mode = MODE_DEFAULT;

    /**
     * http default
     */
    private static final int MODE_DEFAULT = 0x1;

    /**
     * http trunked
     */
    private static final int MODE_TRUNKED = 0x2;

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

            // The download task has already downloaded
            if (task.getStatus() == DownloadStatus.STATUS_FINISHED && task.getSize() == range) {
                if (!isCancelled()) {
                    sHandler.obtainMessage(
                            MESSAGE_POST_SUCCESS,
                            new AsyncTaskResult(this, task, -1)).sendToTarget();
                }
                Log.i("The DownloadTask has already been downloaded.");
                return task;
            }

            task.setStatus(DownloadStatus.STATUS_RUNNING);

            String urlString = task.getUrl();
            String cookies = null;
            while(true){
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Snowdream Mobile");
                connection.setRequestProperty("Connection", "Keep-Alive");
                if (cookies != null && cookies != "") {
                    connection.setRequestProperty("Cookie", cookies);
                }
                connection.setRequestMethod("GET");

                if (range > 0) {
                    connection.setRequestProperty("Range", "bytes=" + range +
                            "-");
                }

                // String acceptRanges =
                // connection.getHeaderField("Accept-Ranges");
                String tranfer_encoding = connection.getHeaderField("Transfer-Encoding");
                if (!TextUtils.isEmpty(tranfer_encoding)
                        && tranfer_encoding.equalsIgnoreCase("chunked")) {
                    mode = MODE_TRUNKED;
                    Log.i("HTTP MODE: DEFAULT");
                } else {
                    mode = MODE_DEFAULT;
                    Log.i("HTTP MODE: TRUNKED");
                }

                boolean redirect = false;
                boolean success = false;

                // normally, 3xx is redirect
                int status = connection.getResponseCode();
                Log.i("HTTP STATUS CODE: " + status);
                
                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP
                            || status == HttpURLConnection.HTTP_MOVED_PERM
                            || status == HttpURLConnection.HTTP_SEE_OTHER)
                        redirect = true;
                }
                switch (status) {
                    case HttpURLConnection.HTTP_OK:
                        success = true;
                        break;
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                    case HttpURLConnection.HTTP_MOVED_PERM:
                    case HttpURLConnection.HTTP_SEE_OTHER:
                        redirect = true;
                        // get redirect url from "location" header field
                        urlString = connection.getHeaderField("Location");
                 
                        // get the cookie if need, for login
                        cookies = connection.getHeaderField("Set-Cookie");
                        
                        Log.i("Redirect Url : " + urlString);
                        break;
                    default:
                        success = false;
                        break;
                }

                
                if (!redirect) {
                    if (!success) {
                        if (!isCancelled()) {
                            sHandler.obtainMessage(
                                    MESSAGE_POST_ERROR,
                                    new AsyncTaskResult(this, task,
                                            DownloadException.DOWNLOAD_TASK_FAILED))
                                    .sendToTarget();
                        }
                        Log.e("Http Connection error. ");
                    }else {
                        Log.i("Successed to establish the http connection.Ready to download...");
                    }
                    
                    break;
                }
            };

            size = connection.getContentLength();
            contentType = connection.getContentType();

            if (range == 0) {
                task.setSize(size);
                task.setStartTime(System.currentTimeMillis());
                task.setMimeType(contentType);
                SaveDownloadTask(task, task.getStatus());
            }

            Log.i("DownloadTask " + task);

            out = new RandomAccessFile(file, "rw");
            out.seek(range);

            in = new BufferedInputStream(connection.getInputStream());

            byte[] buffer = new byte[1024];
            int nRead = 0;
            while ((nRead = in.read(buffer, 0, 1024)) > 0)
            {
                while (task.getStatus() == DownloadStatus.STATUS_PAUSED) {
                    Log.i("Pause the DownloadTask,Sleeping...");
                    Thread.sleep(500);
                }

                out.write(buffer, 0, nRead);

                curSize += nRead;
                Log.i("cur size:" + (curSize) + "    total size:" + (size));

                if (isCancelled())
                    break;

                if ((task.getStatus() != DownloadStatus.STATUS_RUNNING)
                        && (task.getStatus() != DownloadStatus.STATUS_PAUSED)) {
                    break;
                }
            }

            switch (mode) {
                case MODE_DEFAULT:
                    range = file.length();
                    if (range != 0 && range == size) {
                        task.setFinishTime(System.currentTimeMillis());
                        if (!isCancelled()) {
                            sHandler.obtainMessage(
                                    MESSAGE_POST_SUCCESS,
                                    new AsyncTaskResult(this, task, -1)).sendToTarget();
                        }
                        Log.i("The DownloadTask has been successfully downloaded.");
                    } else {
                        if (!isCancelled()) {
                            sHandler.obtainMessage(
                                    MESSAGE_POST_ERROR,
                                    new AsyncTaskResult(this, task,
                                            DownloadException.DOWNLOAD_TASK_FAILED))
                                    .sendToTarget();
                        }
                    }
                    break;
                case MODE_TRUNKED:
                    task.setSize(curSize);
                    task.setFinishTime(System.currentTimeMillis());
                    range = file.length();
                    size = task.getSize();
                    if (range != 0 && range == size) {
                        if (!isCancelled()) {
                            sHandler.obtainMessage(
                                    MESSAGE_POST_SUCCESS,
                                    new AsyncTaskResult(this, task, -1)).sendToTarget();
                        }
                        Log.i("The DownloadTask has been successfully downloaded.");
                    } else {
                        if (!isCancelled()) {
                            sHandler.obtainMessage(
                                    MESSAGE_POST_ERROR,
                                    new AsyncTaskResult(this, task,
                                            DownloadException.DOWNLOAD_TASK_FAILED))
                                    .sendToTarget();
                        }
                    }
                    break;
                default:
                    break;
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
        Log.e("Errors happen while downloading.");
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
