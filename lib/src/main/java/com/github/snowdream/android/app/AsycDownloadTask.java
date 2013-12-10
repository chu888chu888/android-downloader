
package com.github.snowdream.android.app;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.URLUtil;
import com.github.snowdream.android.app.dao.ISql;
import com.github.snowdream.android.app.dao.ISqlImpl;
import com.github.snowdream.android.util.Log;
import com.github.snowdream.android.util.concurrent.AsyncTask;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;

public class AsycDownloadTask extends AsyncTask<DownloadTask, Integer, DownloadTask> {
    private static final InternalHandler sHandler = new InternalHandler();
    private static final int MESSAGE_POST_ERROR = 0x1;

    /**
     * http default
     */
    private static final int MODE_DEFAULT = 0x1;
    /**
     * http trunked
     */
    private static final int MODE_TRUNKED = 0x2;

    private int mode = MODE_DEFAULT;

    public AsycDownloadTask(DownloadListener<Integer, DownloadTask> listener) {
        super(listener);
    }

    /**
     * TODO if error occurs,carry it out. if (listener != null) {
     * listener.onError(new Throwable()); }
     */
    protected DownloadTask doInBackground(DownloadTask... tasks) {
        if (tasks.length <= 0) {
            Log.e("There is no DownloadTask.");
            return null;
        }

        DownloadTask task = tasks[0];

        if (task == null || task.getUrl() == null || !URLUtil.isValidUrl(task.getUrl())) {
            SendError(task, DownloadException.DOWNLOAD_TASK_NOT_VALID);

            Log.e("The task is not valid,or the url of the task is not valid.");
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

            if (!dir.exists() && !dir.mkdirs()) {
                SendError(task, DownloadException.DOWNLOAD_TASK_FAILED);
                Log.e("The directory of the file can not be created!");
                return null;
            }

            if (!file.exists() && !file.createNewFile() && !file.canWrite()) {
                SendError(task, DownloadException.DOWNLOAD_TASK_FAILED);
                Log.e("The file can not be created!");
                return null;
            }

            if (task.getStatus() == DownloadStatus.STATUS_FINISHED && task.getSize() == range) {
                Log.i("The DownloadTask has already been downloaded.");
                return task;
            }

            task.setStatus(DownloadStatus.STATUS_RUNNING);

            range = file.length();
            curSize = range;

            String urlString = task.getUrl();
            String cookies = null;
            while (true) {
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

                String tranfer_encoding = connection.getHeaderField("Transfer-Encoding");
                if (!TextUtils.isEmpty(tranfer_encoding)
                        && tranfer_encoding.equalsIgnoreCase("chunked")) {
                    mode = MODE_TRUNKED;
                    Log.i("HTTP MODE: TRUNKED");
                } else {
                    mode = MODE_DEFAULT;
                    Log.i("HTTP MODE: DEFAULT");
                }

                //http auto redirection
                //see: http://www.mkyong.com/java/java-httpurlconnection-follow-redirect-example/
                boolean redirect = false;
                boolean success = false;

                // normally, 3xx is redirect
                int status = connection.getResponseCode();
                Log.i("HTTP STATUS CODE: " + status);

                switch (status) {
                    case HttpURLConnection.HTTP_OK:
                    case HttpURLConnection.HTTP_PARTIAL:
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
                        SendError(task, DownloadException.DOWNLOAD_TASK_FAILED);

                        Log.e("Http Connection error. ");
                        return null;
                    }
                    Log.i("Successed to establish the http connection.Ready to download...");
                    break;
                }
            }

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
            int progress = -1;
            boolean isFinishDownloading = true;
            while ((nRead = in.read(buffer, 0, 1024)) > 0) {
                while (task.getStatus() == DownloadStatus.STATUS_PAUSED) {
                    Log.i("Pause the DownloadTask,Sleeping...");
                    Thread.sleep(500);
                }

                out.write(buffer, 0, nRead);

                curSize += nRead;

                if (size != 0) {
                    progress = (int) ((curSize * 100) / size);
                }

                publishProgress(progress);

                Log.i("cur size:" + (curSize) + "    total size:" + (size) + "    cur progress:" + (progress));

                if (isCancelled()) {
                    isFinishDownloading = false;
                    break;
                }

                if ((task.getStatus() != DownloadStatus.STATUS_RUNNING)
                        && (task.getStatus() != DownloadStatus.STATUS_PAUSED)) {
                    isFinishDownloading = false;
                    break;
                }
            }

            if (!isFinishDownloading) {
                Log.w("The DownloadTask has not been completely downloaded.");
                SaveDownloadTask(task, task.getStatus());
                return null;
            }

            //when the mode is MODE_TRUNKED,set the latest size.
            if (size == 0 && curSize != 0) {
                task.setSize(curSize);
            }

            range = file.length();
            size = task.getSize();
            Log.i("range: " + range + " size: " + size);

            if (range != 0 && range == size) {
                Log.i("The DownloadTask has been successfully downloaded.");
                task.setFinishTime(System.currentTimeMillis());
                SaveDownloadTask(task, DownloadStatus.STATUS_FINISHED);
                return task;
            } else {
                Log.i("The DownloadTask failed to downloaded.");
                SendError(task, DownloadException.DOWNLOAD_TASK_FAILED);
                return null;
            }
        } catch (MalformedURLException e) {
            SendError(task, DownloadException.DOWNLOAD_TASK_FAILED);

            e.printStackTrace();
        } catch (ProtocolException e) {
            SendError(task, DownloadException.DOWNLOAD_TASK_FAILED);

            e.printStackTrace();
        } catch (FileNotFoundException e) {
            SendError(task, DownloadException.DOWNLOAD_TASK_FAILED);

            e.printStackTrace();
        } catch (IOException e) {
            SendError(task, DownloadException.DOWNLOAD_TASK_FAILED);

            e.printStackTrace();
        } catch (InterruptedException e) {
            SendError(task, DownloadException.DOWNLOAD_TASK_FAILED);

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
        return null;
    }

    private void SendError(DownloadTask task, Integer code) {
        Log.e("Errors happen while downloading.");
        SaveDownloadTask(task, DownloadStatus.STATUS_FAILED);

        sHandler.obtainMessage(
                MESSAGE_POST_ERROR,
                new AsyncTaskResult(this, task,
                        code)).sendToTarget();
    }

    /**
     * throw error
     *
     * @param task task
     * @param code The code of the exception
     */
    private void OnError(DownloadTask task, Integer code) {
        if (listener != null) {
            listener.onError(new DownloadException(code));
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

            if (result == null || result.mTask == null || result.mTask.isCancelled()) {
                Log.i("The asyncTask is not valid or cancelled!");
                return;
            }

            switch (msg.what) {
                case MESSAGE_POST_ERROR:
                    ((AsycDownloadTask) result.mTask).OnError(result.mDownloadTask, result.mData);
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
