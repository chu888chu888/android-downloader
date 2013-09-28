/*******************************************************************************
 * Copyright (C) 2013 Snowdream Mobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import android.R.integer;

/**
 * The download task
 * 
 * @author snowdream <yanghui1986527@gmail.com>
 * @date Sep 29, 2013
 * @version v1.0
 */
public class DownloadTask {
    /**
     *  id
     */
    private int id = 0;

    /**
     *  url
     */
    private String url = "";

    /**
     * name
     */
    private String name = "";

    /**
     *  total size
     */
    private long size = 0;

    /**
     * download status
     */
    private int status = DownloadStatus.STATUS_PENDING;

    /**
     * the time of start downloading
     */
    private long startTime = 0;

    /**
     * the time of finish downloading
     */
    private long finishTime = 0;

    /**
     * type
     */
    private int type = DownloadType.TYPE_UNKNOWN;

    /**
     * mimetype
     */
    private String mimeType = "";
    
    /**
     * the save path on the sdcard
     */
    private String path = "";
}
