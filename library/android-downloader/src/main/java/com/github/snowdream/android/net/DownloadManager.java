/*******************************************************************************
 * Copyright (C) 2013 Snowdream Mobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License"){};
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

package com.github.snowdream.android.net;

import android.app.Activity;
import android.content.Context;

import com.github.snowdream.android.net.dao.ISql;
import com.github.snowdream.android.net.dao.ISqlImpl;
import com.github.snowdream.android.util.Log;

import java.io.File;
import java.sql.SQLException;

/**
 * @author snowdream <yanghui1986527@gmail.com>
 * @date Sep 29, 2013
 * @version v1.0
 */
public class DownloadManager {
    /**
     * Add Task 
     * 
     * @param task
     * @return  
     */
    public static boolean add(DownloadTask task) {
        boolean ret = false;

        if (task == null) {
            return ret;
        }

        Context context = task.getContext();

        if (context == null) {
            return ret;
        }

        ISql iSql = new ISqlImpl(context);

        try {
            iSql.addDownloadTask(task);

            ret = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    };

    // public static void add(List<DownloadTask> tasks) {
    // };
    //
    // public static void start(DownloadListener listener) {
    // };

    /**
     * Start Task
     * 
     * @param task
     * @param listener
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean start(DownloadTask task, DownloadListener listener) {
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
                add(task);
            } else if (!temptask.equals(task)) {
                task = temptask;
            }

            switch (task.getStatus()) {
                case DownloadStatus.STATUS_RUNNING:
                    Log.i("The Task is already Running.");
                    break;
                case DownloadStatus.STATUS_PAUSED:
                    resume(task, listener);
                    break;
                default:
                    if (listener != null) {
                        task.start(listener);
                    }
                    break;
            }

            ret = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    };

    /**
     * Pause Task<BR />
     * 
     * if the task status is not DownloadStatus.STATUS_PAUSED or DownloadStatus.STATUS_RUNNING,
     * then exceptions(DownloadException.OPERATION_NOT_VALID) will be thrown.
     * 
     * @param task
     * @param listener
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean pause(DownloadTask task, DownloadListener listener) {
        boolean ret = false;

        if (task == null) {
            return ret;
        }
        
        Context context = task.getContext();

        if (context == null) {
            return ret;
        }

        switch (task.getStatus()) {
            case DownloadStatus.STATUS_PAUSED:
                ret = true;
                Log.i("The Task is already Paused.");
                break;
            case DownloadStatus.STATUS_RUNNING:
                task.setStatus(DownloadStatus.STATUS_PAUSED);
                
                ISql iSql = new ISqlImpl(context);

                try {
                    iSql.updateDownloadTask(task);

                    ret = true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            default:
                OnError(context, listener, DownloadException.OPERATION_NOT_VALID);
                break;
        }

        return ret;
    };

    /**
     * Resume Task<BR />
     * 
     * if the task status is not DownloadStatus.STATUS_PAUSED or DownloadStatus.STATUS_RUNNING,
     * then errors(DownloadException.OPERATION_NOT_VALID) will be thrown.
     * 
     * @param task
     * @param listener
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean resume(DownloadTask task, DownloadListener listener) {
        boolean ret = false;

        if (task == null) {
            return ret;
        }
        
        Context context = task.getContext();

        if (context == null) {
            return ret;
        }
        
        switch (task.getStatus()) {
            case DownloadStatus.STATUS_RUNNING:
                ret = true;
                Log.i("The Task is already Running.");
                break;
            case DownloadStatus.STATUS_PAUSED:
                task.setStatus(DownloadStatus.STATUS_RUNNING);
                
                ISql iSql = new ISqlImpl(context);

                try {
                    iSql.updateDownloadTask(task);

                    ret = true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            default:
                OnError(context, listener, DownloadException.OPERATION_NOT_VALID);
                break;
        }

        return ret;
    };

    /**
     * Stop Task<BR />
     * 
     * if the task status is not DownloadStatus.STATUS_PAUSED,DownloadStatus.STATUS_STOPPED or DownloadStatus.STATUS_RUNNING,
     * then exceptions(DownloadException.OPERATION_NOT_VALID) will be thrown.
     * 
     * @param task
     * @param listener
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean stop(DownloadTask task, DownloadListener listener) {
        boolean ret = false;

        if (task == null) {
            return ret;
        }
        
        Context context = task.getContext();

        if (context == null) {
            return ret;
        }
        
        switch (task.getStatus()) {
            case DownloadStatus.STATUS_STOPPED:
                ret = true;
                Log.i("The Task is already Stopped.");
                break;
            case DownloadStatus.STATUS_PAUSED:
            case DownloadStatus.STATUS_RUNNING:
                task.setStatus(DownloadStatus.STATUS_STOPPED);
                
                ISql iSql = new ISqlImpl(context);

                try {
                    iSql.updateDownloadTask(task);

                    ret = true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            default:
                OnError(context, listener, DownloadException.OPERATION_NOT_VALID);
                break;
        }

        return ret;
    };

    /**
     * Delete Task <BR />
     * 
     * just set the task status to DownloadStatus.STATUS_DELETED
     * 
     * @param task
     * @param listener
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean delete(DownloadTask task, DownloadListener listener) {
        boolean ret = false;

        if (task == null) {
            return ret;
        }
        
        Context context = task.getContext();

        if (context == null) {
            return ret;
        }

        switch (task.getStatus()) {
            case DownloadStatus.STATUS_DELETED:
                ret = true;
                Log.i("The Task is already Deleted.");
                break;
            default:
                task.setStatus(DownloadStatus.STATUS_DELETED);
                
                ISql iSql = new ISqlImpl(context);

                try {
                    iSql.updateDownloadTask(task);

                    ret = true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
        }

        return ret;
    };

    /**
     * Delete Task <BR />
     * 
     * delete the task ,and the file of the task too.
     * 
     * @param task
     * @param listener
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean deleteforever(DownloadTask task, DownloadListener listener) {
        boolean ret = false;

        if (task == null) {
            return ret;
        }

        // delete the file
        if (delete(task, listener)) {
            File file = new File(task.getPath());

            if (file.exists()) {
                ret = file.delete();
            }
        }

        return ret;
    };

    // public static void cancel() {
    // };

    /**
     * Cancel Task <BR />
     * 
     * like @see {@link DownloadManager#stop(DownloadTask, DownloadListener)} ,
     * but no exceptions will be thrown.
     * 
     * @param task
     * @param listener
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean cancel(DownloadTask task, DownloadListener listener) {
        boolean ret = false;

        if (task == null) {
            return ret;
        }

        switch (task.getStatus()) {
            case DownloadStatus.STATUS_PAUSED:
            case DownloadStatus.STATUS_RUNNING:
                stop(task, listener);
                ret = true;
                break;
            default:
                ret = true;
                break;
        }

        return ret;
    };

    /**
     * throw error
     * 
     * @param context
     * @param listener
     * @param code
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
}
