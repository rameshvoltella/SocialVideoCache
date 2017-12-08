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
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;

public class MainActivity extends AppCompatActivity {



    VideoView simpleExoPlayerView;
    public int stopPosition ;
    VideoDownloadAndPlayService videoService;
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
        startProxy();
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
    }

    private void startProxy() {

        videoService = VideoDownloadAndPlayService.startServer(this, videoPath, new VideoDownloadAndPlayService.VideoStreamInterface()
        {
            @Override
            public void onServerStart(String proxyUrl)
            {
                simpleExoPlayerView.setVideoPath(proxyUrl);
                simpleExoPlayerView.start();
                simpleExoPlayerView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        progressBar.setVisibility(View.VISIBLE);
                        initialLoding.setVisibility(View.INVISIBLE);
                        simpleExoPlayerView.setVisibility(View.VISIBLE);

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
        });


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
    public void onDestroy() {
        super.onDestroy();
        simpleExoPlayerView.stopPlayback();
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
    private void stopProxy()
    {
        if(videoService != null){
            videoService.stop();
            videoService=null;
        }
    }


    @Override
    public void onStop()
    {
        super.onStop();
        stopProxy();
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
                        stopProxy();
                        startProxy();
                    }
                })
                .show();
    }

}
