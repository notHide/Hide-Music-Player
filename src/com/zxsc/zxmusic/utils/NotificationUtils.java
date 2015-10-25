package com.zxsc.zxmusic.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;
import com.zxsc.zxmusic.R;
import com.zxsc.zxmusic.SongDetailActivity;
import com.zxsc.zxmusic.db.SongDb;
import com.zxsc.zxmusic.manager.SongManager;
import com.zxsc.zxmusic.model.LastSong;
import com.zxsc.zxmusic.model.SongInfo;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/24
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class NotificationUtils {

    private static Notification mNotification;

    public static Notification getPlayNotification(Context ctx) {
        if (mNotification != null) return mNotification;

        RemoteViews remoteViews = new RemoteViews(ctx.getPackageName(), R.layout.notify_view);

        Intent buttoncloseIntent = new Intent("close");
        PendingIntent pendcloseButtonIntent = PendingIntent.getBroadcast(ctx, 0, buttoncloseIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.close, pendcloseButtonIntent);

//        Intent buttonplayIntent = new Intent("play");
//        PendingIntent pendplayButtonIntent = PendingIntent.getBroadcast(ctx, 0, buttonplayIntent, 0);
//        remoteViews.setOnClickPendingIntent(R.id.play, pendplayButtonIntent);

//        Intent buttonpauseIntent = new Intent("pause");
//        PendingIntent pendpauseButtonIntent = PendingIntent
//                .getBroadcast(ctx, 0, buttonpauseIntent,
//                        0);

//        Intent buttonnextIntent = new Intent("next");
//        PendingIntent pendnextButtonIntent = PendingIntent.getBroadcast(ctx, 0, buttonnextIntent, 0);
//        remoteViews.setOnClickPendingIntent(R.id.next, pendnextButtonIntent);
//
//        Intent buttonprewtIntent = new Intent("previous");
//        PendingIntent pendprewButtonIntent = PendingIntent.getBroadcast(ctx, 0, buttonprewtIntent, 0);
//        remoteViews.setOnClickPendingIntent(R.id.previous, pendprewButtonIntent);

        update(ctx, remoteViews);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(ctx, SongDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
        builder.setContentIntent(contentIntent)
                .setContent(remoteViews)
                .setTicker(ctx.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher);
        mNotification = builder.build();
        mNotification.bigContentView = remoteViews;
        return mNotification;
    }

    private static void update(Context ctx, RemoteViews remoteViews) {
        LastSong lastSong = SongDb.getLastSong(ctx);
        if (lastSong != null) {
            remoteViews.setViewVisibility(R.id.content_root, View.VISIBLE);
            SongInfo song = SongManager.with(ctx).getSongById(lastSong.getId());
            if (song == null) {
                remoteViews.setViewVisibility(R.id.content_root, View.GONE);
            } else {
                if (TextUtils.isEmpty(song.getAlbum_pic_path())) {
                    remoteViews.setImageViewResource(R.id.album, R.drawable.ic_launcher);
                } else {
                    remoteViews.setImageViewBitmap(R.id.album, CommonUtils.scaleBitmap(ctx, song.getAlbum_pic_path()));
                }
                remoteViews.setTextViewText(R.id.songName, song.getTitle());
                remoteViews.setTextViewText(R.id.artist, song.getArtist() + " - " + song.getAlbum());
            }
        } else {
            remoteViews.setViewVisibility(R.id.content_root, View.INVISIBLE);
        }
    }

    public static void update(final Context ctx) {
        if (mNotification == null) return;
        new AsyncTask<Void, Void, Void>() {

            long time;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                time = System.currentTimeMillis();
            }

            @Override
            protected Void doInBackground(Void... params) {
                update(ctx, mNotification.bigContentView);
                try {
                    long curTime = System.currentTimeMillis();
                    Thread.sleep(curTime - time > 2000 ? 0 : 2000 - curTime + time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                notifyUpdate(ctx);
            }
        }.execute();
    }

    private static void notifyUpdate(Context ctx) {
        if (mNotification == null) return;
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1, mNotification);
    }
}
