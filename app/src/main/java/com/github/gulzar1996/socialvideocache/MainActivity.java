package com.github.gulzar1996.socialvideocache;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;


import java.io.File;

public class MainActivity extends AppCompatActivity implements CacheListener {


    VideoView simpleExoPlayerView;
    public int stopPosition ;
    VideoDownloadAndPlayService videoService;
    HttpProxyCacheServer proxy;
    SeekBar progressBar;
    ProgressBar initialLoding;
    Button changeButton;
    private final VideoProgressUpdater updater = new VideoProgressUpdater();
    private String videoPath="https://socialcops.com/images/old/spec/home/header-img-background_video-1920-480.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        simpleExoPlayerView = findViewById(R.id.videoView);
        progressBar=findViewById(R.id.progressBar);
        changeButton=findViewById(R.id.changeButton);
        progressBar.setVisibility(View.INVISIBLE);
        initialLoding=findViewById(R.id.initialLoding);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            progressBar.setSecondaryProgressTintList(ColorStateList.valueOf(Color.WHITE));
        }
        checkCachedState();
        startVideo();

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               openDialog();
            }
        });
        //startProxy();
    }

    private void openDialog() {
        new MaterialDialog.Builder(this)
                .title("Change Video")
                .inputType(InputType.TYPE_TEXT_VARIATION_URI)
                .positiveText("Change")
                .alwaysCallInputCallback() // this forces the callback to be invoked with every input change
                .input(0, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            videoPath = input.toString();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            checkCachedState();
                            startVideo();
                    }
                })
                .show();
    }

    private void checkCachedState() {
        HttpProxyCacheServer proxy = App.getProxy(this);
        boolean fullyCached = proxy.isCached(videoPath);
        if (fullyCached) {
            progressBar.setSecondaryProgress(100);
            Toast.makeText(this, "Already Cached, Available Offline", Toast.LENGTH_SHORT).show();
        }
    }
    private void startVideo() {
        final HttpProxyCacheServer proxy = App.getProxy(this);
        proxy.registerCacheListener(this, videoPath);
        String proxyUrl = proxy.getProxyUrl(videoPath);
        progressBar.setVisibility(View.INVISIBLE);
        initialLoding.setVisibility(View.VISIBLE);
        simpleExoPlayerView.setVisibility(View.VISIBLE);
        Log.d("PROXYCACHE", "Use proxy url " + proxyUrl + " instead of original url " + videoPath);
        simpleExoPlayerView.setVideoPath(proxyUrl);


        simpleExoPlayerView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                progressBar.setVisibility(View.VISIBLE);
                initialLoding.setVisibility(View.INVISIBLE);
                simpleExoPlayerView.setVisibility(View.VISIBLE);
                simpleExoPlayerView.start();
                progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int videoPosition = simpleExoPlayerView.getDuration() * progressBar.getProgress() / 100;
                        simpleExoPlayerView.seekTo(videoPosition);
                        simpleExoPlayerView.start();
                    }
                });
            }
        });


    }


    private void startProxy() {
//
        videoService = VideoDownloadAndPlayService.startServer(this, videoPath, new VideoDownloadAndPlayService.VideoStreamInterface()
        {
            @Override
            public void onServerStart(String videoStreamUrl)
            {
                Toast.makeText(MainActivity.this, videoStreamUrl, Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    public void onStop()
    {
        super.onStop();
    }
    @Override
    public void onPause() {
        super.onPause();
        if (simpleExoPlayerView!=null && simpleExoPlayerView.isPlaying())
        {
            stopPosition=simpleExoPlayerView.getCurrentPosition();
            simpleExoPlayerView.pause();
        }
        updater.stop();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (simpleExoPlayerView!=null){
        simpleExoPlayerView.seekTo(stopPosition);
        simpleExoPlayerView.start();}
        updater.start();
    }
    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
        progressBar.setSecondaryProgress(percentsAvailable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        simpleExoPlayerView.stopPlayback();
        App.getProxy(this).unregisterCacheListener(this);
    }

    private final class VideoProgressUpdater extends Handler {

        public void start() {
            sendEmptyMessage(0);
        }

        public void stop() {
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message msg) {
            updateVideoProgress();
            sendEmptyMessageDelayed(0, 500);
        }
    }

    private void updateVideoProgress() {
        int videoProgress = simpleExoPlayerView.getCurrentPosition() * 100 / simpleExoPlayerView.getDuration();
        progressBar.setProgress(videoProgress);
    }


}
