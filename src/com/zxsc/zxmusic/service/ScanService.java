package com.zxsc.zxmusic.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import com.zxsc.zxmusic.db.SongDb;
import com.zxsc.zxmusic.manager.SongManager;
import com.zxsc.zxmusic.model.SongInfo;
import com.zxsc.zxmusic.utils.CommonUtils;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/19
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class ScanService extends IntentService {

    public static final String ACTION_SCAN_FINISH = "action_scan_finish";
    public static boolean scaning;

    public ScanService() {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        scaning = true;
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        SongManager.with(this).clearSong();
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        int _id = 1;
        while (cursor.moveToNext()) {
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            if (isMusic == 0) continue;
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            if (size < 1024 * 1024) continue;
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            if (displayName.contains(".mp3")) {
                String[] displayNameArr = displayName.split(".mp3");
                displayName = displayNameArr[0].trim();
            }
            long duration = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            long albumid = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            String albumPath = getAlbumPicPath(path, displayName);

            SongInfo info = new SongInfo();
            info.set_id(_id++);
            info.setSize(size);
            info.setPath(path);
            info.setId(id);
            info.setTitle(title);
            info.setArtist(artist);
            info.setDisplayName(displayName);
            info.setDuration(duration);
            info.setAlbum(album);
            info.setAlbum_id(albumid);
            info.setAlbum_pic_path(albumPath);
            SongManager.with(this).addSong(info);
        }
        cursor.close();

        SongManager.with(this).sort();
        SongManager.with(this).initPlayList();
        SongDb.deleteAllSongInfo(this);
        SongManager.with(this).saveSongToDb();

        Intent scanIntent = new Intent(ACTION_SCAN_FINISH);
        LocalBroadcastManager.getInstance(this).sendBroadcast(scanIntent);
        scaning = false;
    }

    public String getAlbumPicPath(final String filePath, String fileName) {

        String path = null;
        Bitmap bitmap;
        //能够获取多媒体文件元数据的类
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath); //设置数据源
            byte[] embedPic = retriever.getEmbeddedPicture(); //得到字节型数据
            bitmap = BitmapFactory.decodeByteArray(embedPic, 0, embedPic.length); //转换为图片
            path = CommonUtils.imageToLocal(bitmap, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        return path;
    }

}
