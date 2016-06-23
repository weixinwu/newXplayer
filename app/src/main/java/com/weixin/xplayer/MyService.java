package com.weixin.xplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;

public class MyService extends Service {
    private int PAUSE = 1;
    private int PLAY = 0;
    Notification.Builder nb ;
    PendingIntent pi ;
    Intent mainAct;
    NotificationManager nm;
    String song;
    ArrayList<MusicFile> songFiles;
    MediaPlayer mediaPlayer  = new MediaPlayer();
    private int currentPlaying;
    public MyService() {


    }
    public MyService(String s){
        song = s;
        currentPlaying= -1;
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                MainActivity.setBtn(PAUSE);

            }
        });



    }
    public void setSongFiles(ArrayList<MusicFile> songs){
        songFiles = songs;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nb = new Notification.Builder(getBaseContext());
        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.notification);

        nb.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_play_arrow_black_48dp))
                .setSmallIcon(R.drawable.ic_play_arrow_black_48dp)
                .setContentTitle("playing")
        .setContent(remoteViews);

        mainAct = new Intent(getBaseContext(),MainActivity.class);
        mainAct.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainAct.addCategory(Intent.CATEGORY_LAUNCHER);
        mainAct.setAction(Intent.ACTION_MAIN);
        pi = PendingIntent.getActivity(getBaseContext(),0,mainAct,PendingIntent.FLAG_UPDATE_CURRENT);
        nb.setContentIntent(pi);
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mediaPlayer.setWakeMode(getBaseContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //nm.notify(001,nb.build());
        startForeground(001,nb.build());
        System.out.println("service running ++++++");

        return START_STICKY;

    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public void onListViewSelected (String path){

        MainActivity.setBtn(PLAY);
        try {
            if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();

            }
            MainActivity.setBtn(1);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            System.out.println("mediaplayer is starting +++++"+path);
            mediaPlayer.prepareAsync();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void onPlayClicked(){
        if (songFiles.size() < 1){
            return ;
        }
        else if (mediaPlayer.getDuration()<60000) {
            System.out.println("--------"+"in the duration lt 20 " + mediaPlayer.getDuration());
            mediaPlayer.reset();
            int currentPlaying = 0;
            String path = songFiles.get(0).getPath();
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            MainActivity.setBtn(PAUSE);
            //btn_play.setImageResource(R.drawable.ic_pause_black_48dp);
        }
        else {
            mediaPlayer.pause();
            MainActivity.setBtn(PLAY);
            //btn_play.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        }

    }
    public void onSkipNextClicked() throws IOException {
        if (currentPlaying<songFiles.size()-1)
            currentPlaying++;
        else currentPlaying = 0;
        String path = songFiles.get(currentPlaying).getPath();


        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.reset();
        }else {
            mediaPlayer.reset();
        }
        mediaPlayer.setDataSource(path);
        mediaPlayer.prepareAsync();
    }
    public void onSkipPreviousClicked() throws IOException {
        if (currentPlaying<=0)
            currentPlaying = songFiles.size()-1;
        else currentPlaying--;
        String path = songFiles.get(currentPlaying).getPath();
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.reset();
        }else {
            mediaPlayer.reset();
        }
        mediaPlayer.setDataSource(path);
        mediaPlayer.prepareAsync();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.reset();
        mediaPlayer.release();
    }

    public void setCurrentPlaying(int currentPlaying){
        this.currentPlaying = currentPlaying;
    }
    //getter
    public MediaPlayer getMediaPlayer(){
        return  mediaPlayer;
    }
}
