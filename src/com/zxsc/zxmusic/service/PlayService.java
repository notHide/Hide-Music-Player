package com.zxsc.zxmusic.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import com.zxsc.zxmusic.BaseActivity;
import com.zxsc.zxmusic.R;
import com.zxsc.zxmusic.db.SongDb;
import com.zxsc.zxmusic.manager.SongManager;
import com.zxsc.zxmusic.model.LastSong;
import com.zxsc.zxmusic.model.SongInfo;
import com.zxsc.zxmusic.other.Constants;
import com.zxsc.zxmusic.utils.NotificationUtils;
import com.zxsc.zxmusic.utils.SharedUtils;
import com.zxsc.zxmusic.utils.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/20
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class PlayService extends Service
        implements Runnable {

    private static final int STATE_PLAY = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSE = 3;
    private static final int STATE_STOP = 4;
    private static final int STATE_PLAY_FROM_USER = 5;

    private MyBinder mBinder = new MyBinder();
    private MediaPlayer mPlayer;
    private AudioManager mAudioManager;
    private ScheduledThreadPoolExecutor executor;
    private List<IMusicListener> listeners = new ArrayList<>();
    private Handler mHandler = new Handler();
    public boolean isPause;

    private BroadcastReceiver mStopRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "stop":
                    stop();
                    break;
                case "stopService":
                    stopSelf();
                    BaseActivity.closeApp();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class MyBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, NotificationUtils.getPlayNotification(this));
        IntentFilter filter = new IntentFilter("stopService");
        filter.addAction("stop");
        registerReceiver(mStopRecevier, filter);
        return super.onStartCommand(intent, flags, startId);
    }

    public void resume() {
        if (mPlayer == null) return;
        if (!mPlayer.isPlaying()) {
            mPlayer.start();
            isPause = false;
        }
    }

    public void seekTo(int duration) {
        if (mPlayer != null) {
            mPlayer.seekTo(duration);
            resume();
        }
    }

    public boolean isPlaying() {
        if (mPlayer == null) return false;
        return mPlayer.isPlaying();
    }

    public void play() {
        LastSong song = SongDb.getLastSong(this);
        if (song == null) return;
        play(song.getId());
    }

    public void play(int songId) {
        play(songId, false);
    }

    public void play(int songId, boolean fromUser) {
        if (songId < 0) return;
        SongInfo tmp = SongManager.with(this).getCurrentSong();
        if (isPause && tmp != null && songId == tmp.getId()) {
            resume();
            return;
        }
        isPause = false;
        if (mPlayer == null) mPlayer = new MediaPlayer();

        if (executor == null) {
            executor = new ScheduledThreadPoolExecutor(1);
            executor.scheduleAtFixedRate(this, 0, 500, TimeUnit.MILLISECONDS);
        }

        SongInfo lastSong = SongManager.with(this).getCurrentSong();
        if (lastSong != null) {
            lastSong.setProgress(0);
            SongDb.saveSong(this, lastSong);
        }
        SongManager.with(this).setCurrentSong(songId);
        SongInfo song = SongManager.with(this).getCurrentSong();

        int mode = SharedUtils.getInt(this, Constants.KEY_PLAY_MODE, SongManager.STATE_ALL);
        if (fromUser && SongManager.STATE_SINGLE == mode) {
            SongManager.with(this).singlePlayList();
        }

        mPlayer.reset();
        try {
            mPlayer.setDataSource(song.getPath());
            mPlayer.prepare();
            mPlayer.seekTo(song.getProgress());
            int result = mAudioManager.requestAudioFocus(audioFocusChangeListener
                    , AudioManager.STREAM_MUSIC
                    , AudioManager.AUDIOFOCUS_GAIN);
            if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED != result) {
                ToastUtils.show(this, getString(R.string.error_failed_request_focus));
                return;
            }

            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            playNext();
            return;
        }


        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                SongInfo song = SongManager.with(PlayService.this).getCurrentSong();
                if (song != null) {
                    song.setProgress(0);
                    SongDb.saveSong(PlayService.this, song);
                }
                playNext();

            }
        });

        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                playNext();
                return false;
            }
        });
        if (fromUser) {
            notifyMusicState(STATE_PLAY_FROM_USER);
        } else {
            notifyMusicState(STATE_PLAY);
        }
        SongDb.saveLastSong(this, song);
        sendBroadcast(new Intent("play_update"));
    }

    public void playNext() {
        int nextId = SongManager.with(this).getNextSongId();
        play(nextId);
    }

    public void playPrevious() {
        int previousId = SongManager.with(this).getPreviousSongId();
        play(previousId);
    }

    public void stop() {
        if (mPlayer == null) return;
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
            notifyMusicState(STATE_STOP);
        }

        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
    }

    public void pause() {
        if (mPlayer == null) return;
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            isPause = true;
            notifyMusicState(STATE_PAUSE);
        }
    }

    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            /**
             * AUDIOFOCUS_GAIN：获得音频焦点。
             * AUDIOFOCUS_LOSS：失去音频焦点，并且会持续很长时间。这是我们需要停止MediaPlayer的播放。
             * AUDIOFOCUS_LOSS_TRANSIENT
             * ：失去音频焦点，但并不会持续很长时间，需要暂停MediaPlayer的播放，等待重新获得音频焦点。
             * AUDIOFOCUS_REQUEST_GRANTED 永久获取媒体焦点（播放音乐）
             * AUDIOFOCUS_GAIN_TRANSIENT 暂时获取焦点 适用于短暂的音频
             * AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK 我们应用跟其他应用共用焦点
             * 我们播放的时候其他音频会降低音量
             */

            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (mPlayer != null) {
                        mPlayer.setVolume(0.5f, 0.5f);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mPlayer != null) {
                        mPlayer.setVolume(1.0f, 1.0f);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    stop();
                    break;
            }

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioManager.abandonAudioFocus(audioFocusChangeListener);
        stop();
        executor.shutdown();
        executor = null;
        stopForeground(true);
        unregisterReceiver(mStopRecevier);
    }

    @Override
    public void run() {
        if (mPlayer == null
                || !mPlayer.isPlaying()) {
            return;
        }

        final SongInfo song = SongManager.with(this).getCurrentSong();
        if (song == null) return;

        song.setProgress(mPlayer.getCurrentPosition());
        SongDb.saveSong(this, song);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyMusicState(STATE_PLAYING, song.getProgress(), song.getDuration());
            }
        });

    }

    public void addMusicListener(IMusicListener listener) {
        listeners.add(listener);
    }

    public void notifyMusicState(int state) {
        notifyMusicState(state, 0, 0);
    }

    public void notifyMusicState(int state, int progress, long max) {
        for (IMusicListener listener : listeners) {
            if (listener == null) continue;
            switch (state) {
                case STATE_PLAY:
                    listener.onMusicPlay(SongManager.with(this).getCurrentSong().getId());
                    break;
                case STATE_PLAY_FROM_USER:
                    listener.onMusicPlayByUser(SongManager.with(this).getCurrentSong().getId());
                    break;
                case STATE_PLAYING:
                    listener.onMusicPlaying(progress, max);
                    break;
                case STATE_PAUSE:
                    listener.onMusicPause();
                    break;
                case STATE_STOP:
                    listener.onMusicStop();
                    break;
            }
        }
    }


    public interface IMusicListener {
        void onMusicPlay(int songId);

        void onMusicPlayByUser(int songId);

        void onMusicPause();

        void onMusicStop();

        void onMusicPlaying(int progress, long max);
    }

}
