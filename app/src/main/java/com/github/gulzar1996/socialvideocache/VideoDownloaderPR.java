package com.github.gulzar1996.socialvideocache;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.github.gulzar1996.socialvideocache.utils.SharedPreferenceUtils;

import java.io.Console;

/**
 * Created by gulza on 12/8/2017.
 */

public class VideoDownloaderPR {

    private Activity mActivity;
    public String mVideourl;
    public String mID;

    public static final int DATA_READY = 1;
    public static final int DATA_NOT_READY = 2;
    public static final int DATA_CONSUMED = 3;
    public static final int DATA_NOT_AVAILABLE = 4;

    static int fileLength = -1;
    public static int consumedb = 0;
    public static int dataStatus = -1;
    private static int readb = 0;

    public VideoDownloaderPR(Activity activity,String Videourl,String ID) {
        mActivity=activity;
        mVideourl=Videourl;
        mID=ID;
    }

    public static boolean isDataReady() {
        dataStatus = -1;
        boolean res = false;
        if (fileLength == readb) {
            dataStatus = DATA_CONSUMED;
            res = false;
        } else if (readb > consumedb) {
            dataStatus = DATA_READY;
            res = true;
        } else if (readb <= consumedb) {
            dataStatus = DATA_NOT_READY;
            res = false;
        } else if (fileLength == -1) {
            dataStatus = DATA_NOT_AVAILABLE;
            res = false;
        }
        return res;
    }

    public void init()
    {
        if (!isFileDownloaded()) {
            PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                    .setConnectTimeout(100000)
                    .setDatabaseEnabled(true)
                    .build();
            PRDownloader.initialize(mActivity, config);

//            Toast.makeText(mActivity, "Downloading .."+fileSize+" Kb", Toast.LENGTH_SHORT).show();
            PRDownloader.download(mVideourl, mActivity.getCacheDir().toString(), mID)
                    .build()
                    .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                        @Override
                        public void onStartOrResume() {

                        }
                    })
                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(Progress progress) {
                            readb = (int) progress.currentBytes;
                            fileLength = (int) progress.totalBytes;
                            Log.i("download", (readb / 1024) + "kb of " + (fileLength / 1024) + "kb");
                        }
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            SharedPreferenceUtils.getInstance(mActivity).setValue(mID, true);
                            Toast.makeText(mActivity, "Buffer Completed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Error error) {
                            Toast.makeText(mActivity, "Error " + error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else
            Toast.makeText(mActivity, "File is available offline", Toast.LENGTH_SHORT).show();
    }

    private boolean isFileDownloaded()
    {
        boolean isFileCached = SharedPreferenceUtils.getInstance(mActivity).getBoolanValue(mID,false);
        return isFileCached;
    }



}
