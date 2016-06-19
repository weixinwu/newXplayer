package com.weixin.xplayer;

import java.io.File;

/**
 * Created by Weixin on 2016-05-29.
 */
public class MusicFile {

    private String name;
    private String path;
    private File file;
    private String title;
    private String artist;
    public MusicFile(String name, String path, File file){
        this.name = name;
        this.path = path;
        this.file = file;
    }

    public void setTitle(String title){
        this.title = title;
    }
    public String getName() {
        return name;
    }
    public void setArtist(String artist){
        if (artist!=null)
            this.artist = artist;
        else
            this.artist = "";
    }
    public String getPath(){
        return path;
    }
    public String getSongTitle(){
        if (artist.equals(""))
            return title;
        else
            return title+" - "+artist;
    }
}
