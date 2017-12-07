package com.github.gulzar1996.socialvideocache;

import android.app.Activity;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.github.gulzar1996.socialvideocache.utils.SharedPreferenceUtils;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by gulza on 12/7/2017.
 */

public class VideoDownloadAndPlayService {
    private static LocalFileStreamingServer server;
    private static final String IP_OF_SERVER = "127.0.0.1";
    public static String ID;
    private static final String CACHE_PATH = Environment.getExternalStorageDirectory().getPath() + "/SocialCopsVideo/";
    public static VideoDownloaderPR mVideoDownloader;
    private VideoDownloadAndPlayService(LocalFileStreamingServer server)
    {
        this.server = server;
    }


    /**Before Downloading the video we must check the database if the file has already completed downloading
     *If the file has completed downloading we must give the proxy link to play it
     * If the file needs to be downloaded we must download and play simultaneously
     */

    public static VideoDownloadAndPlayService startServer(final Activity activity, String videoUrl, final VideoStreamInterface callback)
    {

        //Get uid from video
        ID = getUID(videoUrl);
        //If the video file is in cache then we dont have to download
        if(!checkFileinCache(activity)){
            mVideoDownloader = new VideoDownloaderPR(activity,videoUrl,ID);
            mVideoDownloader.init();
        }
        server = new LocalFileStreamingServer(getFilefromCache(),checkFileinCache(activity),activity,ID);

        server.setSupportPlayWhileDownloading(true);
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                server.init(IP_OF_SERVER);
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        server.start();
                        callback.onServerStart(server.getFileUrl());
                    }
                });
            }
        }).start();

        return new VideoDownloadAndPlayService(server);
    }

    private static boolean checkFileinCache(Activity activity) {
        boolean isFileCached = SharedPreferenceUtils.getInstance(activity).getBoolanValue(ID,false);
        return isFileCached;
    }

    //Generates md5 HASH for video url
    private static String getUID(String plaintext)
    {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.reset();
        m.update(plaintext.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        String hashtext = bigInt.toString(16);
// Now we need to zero pad it if you actually want the full 32 chars.
        while(hashtext.length() < 32 ){
            hashtext = "0"+hashtext;
        }
        return hashtext;
    }

    public static File getFilefromCache() {
        return new File(CACHE_PATH+ID);
    }

    public void start(){
        server.start();
    }

    public void stop(){
        server.stop();
    }

    public static interface VideoStreamInterface{
        public void onServerStart(String videoStreamUrl);
    }

}
