#android-downloader

##Introduction
android lib - downloader
a multi-thread downloader for android

##Preview
![preview.png](/docs/preview/preview.png "preview.png")

##System requirements
Android 2.2+

##Download
Download [the latest jar][1] or grab via Maven:

```xml
<dependency>
  <groupId>com.github.snowdream.android.app</groupId>
  <artifactId>downloader</artifactId>
  <version>1.0</version>
</dependency>
```

or Gradle:
```groovy
    compile 'com.github.snowdream.android.app:downloader:1.0'
```

##Usage
1.basic
```java
DownloadManager downloadManager = new DownloadManager(this);

DownloadTask task = new DownloadTask(this);
task.setUrl("https://github.com/snowdream/android-autoupdate/raw/master/docs/test/android-autoupdater-v0.0.2-release.apk");

downloadManager.add(task, listener); //Add the task
downloadManager.start(task, listener); //Start the task
downloadManager.stop(task, listener); //Stop the task if you exit your APP.

private DownloadListener listener = new DownloadListener<Integer, DownloadTask>() {
    /**
     * The download task has been added to the sqlite.
     * <p/>
     * operation of UI allowed.
     *
     * @param downloadTask the download task which has been added to the sqlite.
     */
    @Override
    public void onAdd(DownloadTask downloadTask) {
        super.onAdd(downloadTask);
        Log.i("onAdd()");
        list.add(downloadTask);
        Log.i(""+downloadTask);
        adapter.notifyDataSetChanged();
    }

    /**
     * The download task has been delete from the sqlite
     * <p/>
     * operation of UI allowed.
     *
     * @param downloadTask the download task which has been deleted to the sqlite.
     */
    @Override
    public void onDelete(DownloadTask downloadTask) {
        super.onDelete(downloadTask);
        Log.i("onDelete()");
    }

    /**
     * The download task is stop
     * <p/>
     * operation of UI allowed.
     *
     * @param downloadTask the download task which has been stopped.
     */
    @Override
    public void onStop(DownloadTask downloadTask) {
        super.onStop(downloadTask);
        Log.i("onStop()");
    }

    /**
     * Runs on the UI thread before doInBackground(Params...).
     */
    @Override
    public void onStart() {
        super.onStart();
        Log.i("onStart()");
    }

    /**
     * Runs on the UI thread after publishProgress(Progress...) is invoked. The
     * specified values are the values passed to publishProgress(Progress...).
     *
     * @param values The values indicating progress.
     */
    @Override
    public void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        ((DownloadTaskAdapter) getListAdapter()).notifyDataSetChanged();
        Log.i("onProgressUpdate");
    }

    /**
     * Runs on the UI thread after doInBackground(Params...). The specified
     * result is the value returned by doInBackground(Params...). This method
     * won't be invoked if the task was cancelled.
     *
     * @param downloadTask The result of the operation computed by
     *                     doInBackground(Params...).
     */
    @Override
    public void onSuccess(DownloadTask downloadTask) {
        super.onSuccess(downloadTask);
        Log.i("onSuccess()");
    }

    /**
     * Applications should preferably override onCancelled(Object). This method
     * is invoked by the default implementation of onCancelled(Object). Runs on
     * the UI thread after cancel(boolean) is invoked and
     * doInBackground(Object[]) has finished.
     */
    @Override
    public void onCancelled() {
        super.onCancelled();
        Log.i("onCancelled()");
    }

    @Override
    public void onError(Throwable thr) {
        super.onError(thr);
        Log.i("onError()");
    }

    /**
     * Runs on the UI thread after doInBackground(Params...) when the task is
     * finished or cancelled.
     */
    @Override
    public void onFinish() {
        super.onFinish();
        Log.i("onFinish()");
    }
};
```

2.advance  
You can set more properties of the DownloadTask.
```java
DownloadTask task = new DownloadTask(this);
task.setUrl("https://github.com/snowdream/android-autoupdate/raw/master/docs/test/android-autoupdater-v0.0.2-release.apk");
task.setName("taskname");
task.setSize(10240);
task.setPath("/mnt/sdcard/snowdream/android/downloader/android-autoupdater-v0.0.2-release.apk");
task.setId(1);
...
```

##License
```
Copyright (C) 2013 Snowdream Mobile <yanghui1986527@gmail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[1]:https://oss.sonatype.org/content/groups/public/com/github/snowdream/android/app/downloader/1.0/downloader-1.0.jar