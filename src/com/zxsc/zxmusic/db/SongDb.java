package com.zxsc.zxmusic.db;

import android.content.Context;
import android.util.SparseArray;
import com.lidroid.xutils.DbUtils;
import com.zxsc.zxmusic.manager.SongManager;
import com.zxsc.zxmusic.model.LastSong;
import com.zxsc.zxmusic.model.SongInfo;

import java.util.List;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/19
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class SongDb {

    public static void saveSongInfos(Context ctx, List<SongInfo> songInfos) {
        try {
            DbUtils dbUtils = DbUtils.create(ctx);
            dbUtils.configAllowTransaction(true);
            dbUtils.saveAll(songInfos);
            dbUtils.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void saveSong(Context ctx, SongInfo song) {

        try {
            DbUtils dbUtils = DbUtils.create(ctx);
            dbUtils.update(song);
            dbUtils.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static SparseArray<SongInfo> getTotalSongInfo(Context ctx) {
        List<SongInfo> infoList = null;
        try {
            DbUtils dbUtils = DbUtils.create(ctx);
            infoList = dbUtils.findAll(SongInfo.class);
            dbUtils.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        SparseArray<SongInfo> infos = new SparseArray<>();
        if (infoList == null) {
            return infos;
        }

        for (SongInfo info : infoList) {
            infos.put(info.getId(), info);
        }
        return infos;
    }

    public static void deleteAllSongInfo(Context ctx) {
        try {
            DbUtils dbUtils = DbUtils.create(ctx);
            dbUtils.dropTable(SongInfo.class);
            dbUtils.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void deleteSongById(Context ctx, int id) {
        SongInfo info = SongManager.with(ctx).getSongById(id);
        if (info == null) return;
        try {
            DbUtils dbUtils = DbUtils.create(ctx);
            dbUtils.delete(info);
            dbUtils.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void saveLastSong(Context ctx, SongInfo song) {
        try {
            DbUtils dbUtils = DbUtils.create(ctx);
            dbUtils.dropTable(LastSong.class);
            LastSong lastSong = new LastSong();
            lastSong.setId(song.getId());
            dbUtils.save(lastSong);
            dbUtils.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized LastSong getLastSong(Context ctx) {
        LastSong song = null;
        try {
            DbUtils dbUtils = DbUtils.create(ctx);
            song = dbUtils.findFirst(LastSong.class);
            dbUtils.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return song;
    }

}
