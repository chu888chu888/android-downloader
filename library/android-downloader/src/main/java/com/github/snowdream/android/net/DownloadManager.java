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

import android.content.Context;

import com.github.snowdream.android.net.dao.ISql;
import com.github.snowdream.android.net.dao.ISqlImpl;

import java.sql.SQLException;

/**
 * @author snowdream <yanghui1986527@gmail.com>
 * @date Sep 29, 2013
 * @version v1.0
 */
public class DownloadManager {
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

            if(listener != null){
                task.start(listener);
            }
            
            ret = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return ret;
    };

    public static void pause(DownloadTask task) {
    };

    public static void delete(DownloadTask task) {
    };

    public static void deleteforever(DownloadTask task) {
    };

    public static void cancel() {
    };

    public static void cancel(DownloadTask task) {
    };
}
