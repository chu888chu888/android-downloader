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


/**
 * @author snowdream <yanghui1986527@gmail.com>
 * @date Sep 29, 2013
 * @version v1.0
 */
public class DownloadException extends Exception {
    private int code = UNKNOWN;

    /** serialVersionUID **/
    private static final long serialVersionUID = -3804737584048978515L;

    public static final int UNKNOWN = 0;

    public static final int DOWNLOAD_TASK_NOT_VALID = 1;

    public DownloadException() {
    }

    public DownloadException(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
