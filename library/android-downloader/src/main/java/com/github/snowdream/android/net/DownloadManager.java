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

import java.util.List;

/**
 * @author snowdream <yanghui1986527@gmail.com>
 * @date Sep 29, 2013
 * @version v1.0
 */
public class DownloadManager {
    public static void add(DownloadTask task) {
    };

    public static void add(List<DownloadTask> tasks) {
    };

    public static void start(DownloadListener listener) {
    };

    public static void start(DownloadTask task, DownloadListener listener) {
        if(task != null){
            task.start(listener);
        }
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
