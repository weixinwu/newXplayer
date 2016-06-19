package com.weixin.xplayer;

import android.app.Activity;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import static java.lang.System.currentTimeMillis;

public class MainActivity extends AppCompatActivity {

    ListView songList;
    ArrayList<MusicFile> songFiles;
    ArrayList<File> songFilesRandom;
    ArrayList<String> arrayList;
    static ImageButton btn_play;
    Button btn_scan;
    SeekBar seekBar;
    int currentPlaying;
    //final MediaPlayer mediaPlayer  = new MediaPlayer();
    boolean isRandom = false;
    SharedPreferences sharedpreference;
    SharedPreferences.Editor editor ;
    final String  sharedPrefsSongName = "songName";
    final String sharedPrefsSongPath = "songPath";
    final String sharedPrefsSongCount = "sharedPrefsSongCount";
    final String sharedPrefsIsLoaded = "sharedPrefsIsLoaded";
    ProgressDialog progressdialog;
    MyService ms;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        btn_scan=(Button)findViewById(R.id.scanMusic);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        btn_play = (ImageButton)findViewById(R.id.button_play);
        btn_play.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        currentPlaying = -1;
        ms = new MyService("song");
        Intent foregroundService = new Intent(getBaseContext(),MyService.class);
        startService(foregroundService);

        sharedpreference = getSharedPreferences("musicInfo", Context.MODE_PRIVATE);

        editor = sharedpreference.edit();
        //editor.clear().commit();
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

//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                if (currentPlaying < songFiles.size()-1)
//                    currentPlaying++;
//                else currentPlaying = 0;
//            }
//        });

        songList = (ListView)findViewById(R.id.songList);
        songFiles= new ArrayList<MusicFile>();
        //songFilesRandom = new ArrayList<File>(songFiles);
        arrayList = new ArrayList<String>();
        songFiles.clear();
        arrayList.clear();
        //check if all the song is loaded previously
        if (!(sharedpreference.getBoolean(sharedPrefsIsLoaded,false))){
            btn_scan.setVisibility(View.VISIBLE);

        }else {
            btn_scan.setVisibility(View.INVISIBLE);
            File f = null;
            new scanForMusic().execute(f);
        }


        songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String path = songFiles.get(position).getPath();
                    ms.setCurrentPlaying(position);
                    ms.onListViewSelected(path);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }
    public void scanSongOnClick(View v){

        File musicFolder = null;
        musicFolder = Environment.getExternalStorageDirectory();
        File[] p = musicFolder.listFiles();
        new scanForMusic().execute(p);
    }
    private void getSong(File[] f){
        if (f !=null){
            int i = 0;
            for (i=0; i<f.length;i++){
                String fName = f[i].getName();
                if (!fName.substring(0,1).equals(".") && !fName.equals("backups")
                        && (!fName.equals("Android")) && (!fName.equals("DCIM")))
                    getSong(f[i].listFiles());


            }
            for (int j = 0 ; j < f.length;j++) {

                if (f[j].getName().contains("mp3")) {
                    String name = f[j].getName();
                    String songTitle;
                    if (name.substring(name.length() - 4, name.length()).equals(".mp3")) {
                        String mediaPath = f[j].getAbsolutePath();
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(mediaPath);
                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        if (title ==null || artist == null) {
                            title = name.substring(0, name.length() - 4);
                        }
                        mmr.release();
                        if (Integer.valueOf(duration) > 30000){
                            MusicFile musicFile = new MusicFile(f[j].getName(), f[j].getPath(), f[j]);
                            musicFile.setTitle(title);
                            musicFile.setArtist(artist);
                            songFiles.add(musicFile);
                            arrayList.add(musicFile.getSongTitle());
                        }
                    }
                }
            }

        }
    }

    private class scanForMusic extends AsyncTask<File,Void,Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressdialog = ProgressDialog.show(MainActivity.this,"Loading","");

        }

        @Override
        protected Void doInBackground(File... params) {

            if (!(sharedpreference.getBoolean(sharedPrefsIsLoaded,false))) {
                sharedpreference.edit().putBoolean(sharedPrefsIsLoaded,true).commit();
                getSong(params);
                int length = songFiles.size();
                editor.putInt(sharedPrefsSongCount, length);
                for (int i = 0; i < length; i++) {
                    editor.putString(sharedPrefsSongName + i, songFiles.get(i).getSongTitle());
                    editor.putString(sharedPrefsSongPath + i, songFiles.get(i).getPath());
                }
                editor.commit();

            }else {

                int length = sharedpreference.getInt(sharedPrefsSongCount,-1);
                if (length>0){
                    for (int i = 0 ; i < length; i++){
                        String name = sharedpreference.getString(sharedPrefsSongName+i,"not found");
                        String path = sharedpreference.getString(sharedPrefsSongPath+i,"not found");
                        File f = new File(sharedpreference.getString(sharedPrefsSongPath+i,"not found"));
                        songFiles.add(new MusicFile(name,path,f));
                        arrayList.add(name);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void Void) {
            ArrayAdapter adapter = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_list_item_1,arrayList);
            songList.setAdapter(adapter);
            songList.setVisibility(View.VISIBLE);
            btn_scan.setVisibility(View.INVISIBLE);
            progressdialog.dismiss();
            ms.setSongFiles(songFiles);
        }

    }

    public void onClickPlay(View v) throws IOException {
        ms.onPlayClicked();
    }
    public void onClickSkipNext(View v) throws Exception{
        ms.onSkipNextClicked();
    }
    public void onClickSkipPrevious(View v) throws IOException {
        ms.onSkipPreviousClicked();
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
    }

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


    public static void setBtn(int temp){
        if (temp==1)
            btn_play.setImageResource(R.drawable.ic_pause_black_48dp);
        else
            btn_play.setImageResource(R.drawable.ic_play_arrow_black_48dp);
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
