package com.weixin.xplayer;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ListView songList;
    ArrayList<File> songFiles;
    ArrayList<File> songFilesRandom;
    ArrayList<String> arrayList;
    ImageButton btn_play;
    int currentPlaying;
    final MediaPlayer mediaPlayer  = new MediaPlayer();
    boolean isRandom = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        btn_play = (ImageButton)findViewById(R.id.button_play);
        currentPlaying = -1;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int dp = (int) (displayMetrics.widthPixels * 160 / displayMetrics.densityDpi);
                Snackbar.make(view, "shuffle playing"+dp, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                long seed = System.nanoTime();
                Collections.shuffle(songFiles,new Random(seed));

            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                btn_play.setImageResource(R.drawable.ic_pause_black_48dp);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (currentPlaying < songFiles.size()-1)
                    currentPlaying++;
                else currentPlaying = 0;
            }
        });

        songList = (ListView)findViewById(R.id.songList);
        songFiles= new ArrayList<File>();
        songFilesRandom = new ArrayList<File>(songFiles);
        arrayList = new ArrayList<String>();

        File musicFolder = null;
        musicFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File[] p = musicFolder.listFiles();
        getSong(p);
        ArrayAdapter adapter = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_list_item_1,arrayList);
        songList.setAdapter(adapter);




        songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {

                    if (mediaPlayer.isPlaying()) {
                       System.out.println("++++++++++++++++++++++ is playing");
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }else {
                        mediaPlayer.reset();
                    }
                    String path = songFiles.get(position).getPath();
                    currentPlaying = position;
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepareAsync();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


    }
    private void getSong(File[] f){
        if (f !=null){
            int i = 0;
            for (i=0; i<f.length;i++){
                getSong(f[i].listFiles());
            }
            for (int j = 0 ; j < f.length;j++) {
                if (f[j].getName().contains("mp3")) {
                    songFiles.add(f[j]);
                    arrayList.add(f[j].getName());

                }
            }
        }
    }
    public void onClickPlay(View v) throws IOException {
        if (mediaPlayer.getDuration()<20) {
            System.out.println("++++++++++++++++"+"in the duration lt 20 " + mediaPlayer.getDuration());
            mediaPlayer.reset();
            currentPlaying = 0;
            String path = songFiles.get(0).getPath();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync();
        }
        else if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            btn_play.setImageResource(R.drawable.ic_pause_black_48dp);
        }
        else {
            mediaPlayer.pause();
            btn_play.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        }
    }
    public void onClickSkipNext(View v) throws Exception{
        if (currentPlaying < songFiles.size()-1)
            currentPlaying++;
        else currentPlaying = 0;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            String path = songFiles.get(currentPlaying).getPath();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync();


        }else {
            mediaPlayer.reset();
            String path = songFiles.get(currentPlaying).getPath();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync();

        }

    }
    public void onClickSkipPrevious(View v) throws IOException {
        if (currentPlaying <= 0)
            currentPlaying=(songFiles.size()-1);
        else currentPlaying--;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            String path = songFiles.get(currentPlaying).getPath();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync();
            //comment

        }else {
            mediaPlayer.reset();
            String path = songFiles.get(currentPlaying).getPath();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync();
        }
    }
    public void onClickCardView(View v){
        Intent i = new Intent(getBaseContext(),PlayingView.class);

        startActivity(i);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }asd

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
