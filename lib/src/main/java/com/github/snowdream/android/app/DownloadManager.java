/*
 * Copyright (C) 2013 Snowdream Mobile <yanghui1986527@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.snowdream.android.app;

import android.app.Activity;
import android.content.Context;
import com.github.snowdream.android.app.dao.ISql;
import com.github.snowdream.android.app.dao.ISqlImpl;
import com.github.snowdream.android.util.Log;

import java.io.File;
import java.sql.SQLException;

/**
 * @author snowdream <yanghui1986527@gmail.com>
 * @version v1.0
 * @date Sep 29, 2013
 */
public class DownloadManager {

    /**
     * throw error
     *
     * @param context  Context
     * @param listener DownloadListener
     * @param code     code
     */
    @SuppressWarnings("rawtypes")
    private static void OnError(Context context, final DownloadListener listener, final Integer code) {
        if (context == null || !(context instanceof Activity)) {
            Log.w("The context is null or invalid!");
            return;
        }

        ((Activity) context).runOnUiThread(new Runnable() {

            public void run() {
                if (listener != null) {
                    listener.onError(new DownloadException(code));
                }
            }
        });
    }

    /**
     * Add Task
     *
     * @param task DownloadTask
     * @return
     */
    public static boolean add(DownloadTask task, DownloadListener listener) {
        Log.i("Add Task");

        boolean ret = false;

        if (task == null) {
            return ret;
        }

        Context context = task.getContext();

        if (context == null) {
            return ret;
        }

        ISql iSql = new ISqlImpl(context);

        DownloadTask temptask = null;

        try {
            temptask = iSql.queryDownloadTask(task);

            if (temptask == null) {
                iSql.addDownloadTask(task);
                Log.i("The Task is stored in the sqlite.");
            } else {
                Log.i("The Task is already stored in the sqlite.");
            }

            ret = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Delete Task <BR />
     * just set the task status to DownloadStatus.STATUS_DELETED
     *
     * @param task DownloadTask
     * @return
     */
    public static boolean delete(DownloadTask task) {
        Log.i("Delete Task");

        boolean ret = false;

        if (task == null) {
            return ret;
        }

        DownloadListener listener = task.getListener();
        Context context = task.getContext();

        if (context == null) {
            OnError(context, listener, DownloadException.CONTEXT_NOT_VALID);
            return ret;
        }

        switch (task.getStatus()) {
            case DownloadStatus.STATUS_DELETED:
                ret = true;
                Log.i("The Task is already Deleted.");
                break;
            default:
                task.setStatus(DownloadStatus.STATUS_DELETED);
                break;
        }

        return ret;
    }

    /**
     * Delete Task <BR />
     * delete the task ,and the file of the task too.
     *
     * @param task DownloadTask
     * @return
     */
    public static boolean deleteforever(DownloadTask task) {
        Log.i("Delete Task forever");

        boolean ret = false;

        if (task == null) {
            return ret;
        }

        // delete the file
        if (delete(task)) {
            File file = new File(task.getPath());

            if (file.exists()) {
                ret = file.delete();
            }
        }

        return ret;
    }

    /**
     * Start Task
     *
     * @param task
     * @param listener
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean start(Context context, DownloadTask task, DownloadListener listener) {
        Log.i("Start Task");

        boolean ret = false;

        if (task == null) {
            OnError(context, listener, DownloadException.DOWNLOAD_TASK_NOT_VALID);
            return ret;
        }

        if (context != null) {
            task.setContext(context);
        }

        if (task.getContext() == null) {
            OnError(context, listener, DownloadException.CONTEXT_NOT_VALID);
            return ret;
        }

        ISql iSql = new ISqlImpl(context);

        DownloadTask temptask = null;

        try {
            temptask = iSql.queryDownloadTask(task);

            if (temptask == null) {
                add(task);
            } else if (!temptask.equals(task)) {
                task.setDownloadTask(temptask);
            }

            switch (task.getStatus()) {
                case DownloadStatus.STATUS_RUNNING:
                    Log.i("The Task is already Running.");
                    break;
                default:
                    if (listener != null) {
                        task.start(context, listener);
                    }
                    break;
            }

            ret = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Stop Task<BR />
     * if the task status is not
     * DownloadStatus.STATUS_PAUSED,DownloadStatus.STATUS_STOPPED or
     * DownloadStatus.STATUS_RUNNING, then
     * exceptions(DownloadException.OPERATION_NOT_VALID) will be thrown.
     *
     * @param task DownloadTask
     * @return
     */
    public static boolean stop(DownloadTask task) {
        Log.i("Stop Task");

        boolean ret = false;

        if (task == null) {
            return ret;
        }

        DownloadListener listener = task.getListener();
        Context context = task.getContext();

        if (context == null) {
            OnError(context, listener, DownloadException.CONTEXT_NOT_VALID);
            return ret;
        }

        switch (task.getStatus()) {
            case DownloadStatus.STATUS_STOPPED:
                ret = true;
                Log.i("The Task is already Stopped.");
                break;
            case DownloadStatus.STATUS_RUNNING:
                task.setStatus(DownloadStatus.STATUS_STOPPED);
                break;
            default:
                OnError(context, listener, DownloadException.OPERATION_NOT_VALID);
                break;
        }

        return ret;
    }
}
