package com.zxsc.zxmusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.zxsc.zxmusic.utils.NotificationUtils;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/24
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class NotificationRecevier extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case "play_update":
                NotificationUtils.update(context);
                break;
            case "close":
                context.sendBroadcast(new Intent("stopService"));
                break;
        }
    }

}
