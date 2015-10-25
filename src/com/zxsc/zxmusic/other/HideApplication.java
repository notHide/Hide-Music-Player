package com.zxsc.zxmusic.other;

import android.app.Application;
import com.zxsc.zxmusic.db.SongDb;
import com.zxsc.zxmusic.manager.SongManager;
import com.zxsc.zxmusic.model.LastSong;
import com.zxsc.zxmusic.utils.SharedUtils;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/21
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class HideApplication extends Application {

    public static boolean showListAnim;

    @Override
    public void onCreate() {
        super.onCreate();

        showListAnim = SharedUtils.getBoolean(this, Constants.KEY_SHOW_LIST_ANIM, true);

        LastSong lastSong = SongDb.getLastSong(this);
        if (lastSong != null) {
            SongManager.with(this).setCurrentSong(lastSong.getId());
        }
    }
}
