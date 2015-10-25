package com.zxsc.zxmusic.model;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/18
 * * * * * * * * * * * * * * * * * * * * * * *
 **/

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

/**
 * displayName：在SD卡上显示的名字
 * title：音频文件内部的名字
 */
@Table(name = "song")
public class SongInfo {

    @Id(column = "_id")
    private int _id;
    @Column(column = "id")
    private int id;
    @Column(column = "size")
    private long size;
    @Column(column = "path")
    private String path;
    @Column(column = "title")
    private String title;
    @Column(column = "artist")
    private String artist;
    @Column(column = "displayName")
    private String displayName;
    @Column(column = "duration")
    private long duration;
    @Column(column = "album")
    private String album;
    @Column(column = "album_id")
    private long album_id;
    @Column(column = "album_pic_path")
    private String album_pic_path;
    @Column(column = "progress")
    private int progress;

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getAlbum_pic_path() {
        return album_pic_path;
    }

    public void setAlbum_pic_path(String album_pic_path) {
        this.album_pic_path = album_pic_path;
    }


    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(long album_id) {
        this.album_id = album_id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SongInfo)) {
            return false;
        }
        SongInfo info = (SongInfo) o;
        return this.id == info.id;
    }

}
