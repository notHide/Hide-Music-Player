package com.zxsc.zxmusic.model;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/20
 * * * * * * * * * * * * * * * * * * * * * * *
 **/

@Table(name = "lastsong")
public class LastSong {
    @Id(column = "_id")
    private int _id;
    @Column(column = "id")
    private int id;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LastSong lastSong = (LastSong) o;

        return id == lastSong.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
