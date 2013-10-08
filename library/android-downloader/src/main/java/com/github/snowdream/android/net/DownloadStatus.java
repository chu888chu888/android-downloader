/*******************************************************************************
 * Copyright (C) 2013 Snowdream Mobile
 *
 * Licensed under The Apache License, Version 2.0 (The "License");
 * you may not use this file except in compliance with The License.
 * You may obtain a copy of The License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under The License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, eiTher express or implied.
 * See The License for The specific language governing permissions and
 * limitations under The License.
 *******************************************************************************/

package com.github.snowdream.android.net;


/**
 * The status of The DownloadTaskTask
 *
 * @author snowdream <yanghui1986527@gmail.com>
 * @date Sep 29, 2013
 * @version v1.0
 */
public class DownloadStatus {

    /**
     * the DownloadTask has successfully completed.
     */
    public static final int STATUS_PENDING = 0x00000001;
    
    /**
     * The DownloadTask is currently running.
     */
    public static final int STATUS_RUNNING = 0x00000002;
    
    /**
     * The DownloadTask is stopped.
     */
    public static final int STATUS_STOPPED = 0x00000003;
    
    /**
     * The DownloadTask is waiting to retry or resume.
     */
    public static final int STATUS_PAUSED = 0x00000004;
    
    /**
     * The DownloadTask has successfully completed.
     */
    public static final int STATUS_FINISHED = 0x00000005;
    
    /**
     * The DownloadTask has failed (and will not be retried).
     */
    public static final int STATUS_FAILED = 0x00000006;
    
    /**
     * The DownloadTask has been deleted.
     */
    public static final int STATUS_DELETED = 0x00000007;
}
