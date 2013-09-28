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

import android.app.Activity;
import android.os.Bundle;

/**
 * 
 *
 * @author snowdream <yanghui1986527@gmail.com>
 * @date Sep 29, 2013
 * @version v1.0
 */
public class HelloAndroidActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        DownloadTask task = new DownloadTask();
        task.setUrl("http://down.angeeks.com/c/d2/d10120/10120702.apk");
        task.setPath("/mnt/sdcard/10120702.apk");
        
        DownloadManager.start(task, new DownloadListener<Integer, DownloadTask>());
    }
}
