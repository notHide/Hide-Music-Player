package com.zxsc.zxmusic;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.zxsc.zxmusic.db.SongDb;
import com.zxsc.zxmusic.manager.SongManager;
import com.zxsc.zxmusic.model.LastSong;
import com.zxsc.zxmusic.model.SongInfo;
import com.zxsc.zxmusic.other.Constants;
import com.zxsc.zxmusic.other.FastBlur;
import com.zxsc.zxmusic.service.PlayService;
import com.zxsc.zxmusic.utils.CommonUtils;
import com.zxsc.zxmusic.utils.SharedUtils;
import com.zxsc.zxmusic.utils.ToastUtils;
import com.zxsc.zxmusic.widget.HintSeekBar;
import com.zxsc.zxmusic.widget.MultiStateView;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/22
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class SongDetailActivity extends BaseActivity
        implements MultiStateView.IStateChangeListener, View.OnClickListener, PlayService.IMusicListener
        , HintSeekBar.OnSeekBarHintProgressChangeListener, SeekBar.OnSeekBarChangeListener {


    private ImageView mIvBg;
    private TextView mTvSongName;
    private TextView mTvArtist;
    private ImageView mIvAlbum;
    private MultiStateView mPlayModeView;
    private View mControllerRoot;
    private MultiStateView mPlayBtn;
    private TextView mTvCurTime;
    private TextView mTvTotalTime;
    private HintSeekBar mPlayBar;

    private PlayService mPlayService;
    private int mLastSongId;
    private int mCurSongId;
    private ArrayList<ObjectAnimator> mAnimList;
    private Bitmap mBgBitmap;
    private Bitmap mAlbumBitmap;


    private ServiceConnection mPlayConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayService.MyBinder binder = (PlayService.MyBinder) service;
            mPlayService = binder.getService();
            mPlayService.addMusicListener(SongDetailActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);

        mIvBg = (ImageView) findViewById(R.id.iv_bg);
        mTvSongName = (TextView) findViewById(R.id.tv_song_name);
        mTvArtist = (TextView) findViewById(R.id.tv_artist);
        mIvAlbum = (ImageView) findViewById(R.id.iv_album);
        mPlayModeView = (MultiStateView) findViewById(R.id.play_mode);
        mControllerRoot = findViewById(R.id.controller_root);
        mPlayBtn = (MultiStateView) findViewById(R.id.play);
        mTvCurTime = (TextView) findViewById(R.id.tv_curtime);
        mTvTotalTime = (TextView) findViewById(R.id.tv_totaltime);
        mPlayBar = (HintSeekBar) findViewById(R.id.sb_progress);
        mPlayBar.setOnSeekBarChangeListener(this);
        mPlayBar.setOnProgressChangeListener(this);

        bindService(new Intent(this, PlayService.class), mPlayConn, Context.BIND_AUTO_CREATE);

        findViewById(R.id.title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                mControllerRoot.setAlpha(0f);
                mAnimList = new ArrayList<>();
            }

            @Override
            protected Boolean doInBackground(Void... params) {

                LastSong lastSong = SongDb.getLastSong(SongDetailActivity.this);
                if (lastSong == null
                        || SongManager.with(SongDetailActivity.this).getSongById(lastSong.getId()) == null) {
                    finish();
                    return false;
                }
                mCurSongId = lastSong.getId();

                findViewById(R.id.play_previous).setOnClickListener(SongDetailActivity.this);
                findViewById(R.id.play_next).setOnClickListener(SongDetailActivity.this);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean b) {
                if (!b) return;
                init();
                initPlayMode();
                initPlayBtn();
                translateAnim(mControllerRoot);
                resetPlayState();
            }
        }.execute();
    }

    private void init() {
        initTitle();
        initBackGround();
        initAlbum();
    }

    private void initPlayBtn() {
        mPlayBtn.setOnStateChangeListener(this);
        mPlayBtn.addStateAndImage(SongManager.STATE_PAUSE, R.drawable.play);
        mPlayBtn.addStateAndImage(SongManager.STATE_PINGING, R.drawable.play_pause);
    }

    private void initPlayMode() {
        mPlayModeView.setOnStateChangeListener(this);
        mPlayModeView.addStateAndImage(SongManager.STATE_ALL, R.drawable.play_all);
        mPlayModeView.addStateAndImage(SongManager.STATE_RANDOM, R.drawable.play_random);
        mPlayModeView.addStateAndImage(SongManager.STATE_SINGLE, R.drawable.play_single);

        int state = SharedUtils.getInt(this, Constants.KEY_PLAY_MODE, SongManager.STATE_ALL);
        mPlayModeView.show(state);
    }

    private void initAlbum() {
        SongInfo song = SongManager.with(this).getCurrentSong();
        if (song == null) {
            song = SongManager.with(this).getSongById(mCurSongId);
        }
        if (song != null) {
            recycleBitmap(mIvAlbum, mAlbumBitmap);
            Bitmap bitmap = CommonUtils.scaleBitmap(this, song.getAlbum_pic_path());
            if (bitmap != null) {
                mAlbumBitmap = bitmap;
                mIvAlbum.setImageBitmap(mAlbumBitmap);
                alphaAnim(mIvAlbum, 400);
            } else {
                mIvAlbum.setImageBitmap(null);
            }
        }
    }

    private void initTitle() {
        SongInfo song = SongManager.with(this).getCurrentSong();
        SongInfo last = SongManager.with(this).getSongById(mCurSongId);
        mTvSongName.setText(song == null ? last.getTitle() + " " : song.getTitle() + " ");
        mTvArtist.setText(song == null ? last.getArtist() + " " + " - " + last.getAlbum() + " "
                : song.getArtist() + " " + " - " + song.getAlbum() + " ");
        alphaAnim(mTvSongName, 200);
        alphaAnim(mTvArtist, 400);

    }

    public void initBackGround() {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                Bitmap original = null;
                SongInfo song = SongManager.with(SongDetailActivity.this).getCurrentSong();
                if (song == null) {
                    song = SongManager.with(SongDetailActivity.this).getSongById(mCurSongId);
                }
                if (song != null
                        && !TextUtils.isEmpty(song.getAlbum_pic_path())) {
                    original = CommonUtils.scaleBitmap(SongDetailActivity.this, song.getAlbum_pic_path());
                }
                if (original == null) {
                    original = BitmapFactory.decodeResource(getResources(), R.drawable.default_bg);
                }
                Bitmap result = null;
                try {
                    result = FastBlur.doBlur(original, 50, false);
                } catch (Error e) {
                    e.printStackTrace();
                }
                original.recycle();
                return result;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                recycleBitmap(mIvBg, mBgBitmap);
                mBgBitmap = bitmap;
                mIvBg.setImageBitmap(mBgBitmap);
                alphaAnim(mIvBg, 0);
            }
        }.execute();
    }

    private void recycleBitmap(ImageView iv, Bitmap bitmap) {
        if (bitmap != null
                && !bitmap.isRecycled()) {
            iv.setImageBitmap(null);
            bitmap.recycle();
        }
    }

    private void alphaAnim(final View view, int delay) {
        alphaAnim(view, 1000, delay);
    }

    private void alphaAnim(final View view, int duration, int delay) {
        if (mLastSongId == mCurSongId) return;
        view.setAlpha(0.0f);
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1.0f);
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
        mAnimList.add(animator);
    }

    private void translateAnim(final View view) {
        alphaAnim(view, 200, 500);
        view.post(new Runnable() {
            @Override
            public void run() {
                view.setTranslationY(view.getHeight());
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f);
                animator.setDuration(250);
                animator.setStartDelay(500);
                animator.setInterpolator(new AccelerateInterpolator());
                animator.start();

            }
        });
    }

    @Override
    public void onStateChange(int state) {
        switch (state) {
            case SongManager.STATE_ALL:
                ToastUtils.show(this, "顺序播放");
                SharedUtils.saveInt(this, Constants.KEY_PLAY_MODE, state);
                SongManager.with(this).initPlayList();
                break;
            case SongManager.STATE_RANDOM:
                ToastUtils.show(this, "随机播放");
                SharedUtils.saveInt(this, Constants.KEY_PLAY_MODE, state);
                SongManager.with(this).initPlayList();
                break;
            case SongManager.STATE_SINGLE:
                ToastUtils.show(this, "单曲循环");
                SharedUtils.saveInt(this, Constants.KEY_PLAY_MODE, state);
                SongManager.with(this).initPlayList();
                break;
            case SongManager.STATE_PINGING:
                mPlayService.play();
                break;
            case SongManager.STATE_PAUSE:
                mPlayService.pause();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_previous:
                mPlayService.playPrevious();
                break;
            case R.id.play_next:
                mPlayService.playNext();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mPlayConn);
        recycleBitmap(mIvAlbum, mAlbumBitmap);
        recycleBitmap(mIvBg, mBgBitmap);
    }

    @Override
    public void onMusicPlay(int songId) {
        mLastSongId = mCurSongId;
        mCurSongId = songId;
        resetPlayState();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Iterator<ObjectAnimator> iterator = mAnimList.iterator();
                while (iterator.hasNext()) {
                    final ObjectAnimator next = iterator.next();
                    if (next != null && next.isRunning()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                next.cancel();
                            }
                        });
                    }
                    iterator.remove();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                init();
            }
        }.execute();


    }

    @Override
    public void onMusicPlayByUser(int songId) {
    }

    @Override
    public void onMusicPause() {
        mPlayBtn.show(SongManager.STATE_PAUSE);
    }

    @Override
    public void onMusicStop() {
        mPlayBtn.show(SongManager.STATE_PAUSE);

    }

    @Override
    public void onMusicPlaying(int progress, long max) {
        mPlayBtn.show(SongManager.STATE_PINGING);
        mTvCurTime.setText(CommonUtils.durationToString2(progress));
        mTvTotalTime.setText(CommonUtils.durationToString2(max));
        mPlayBar.setMax((int) max);
        mPlayBar.setProgress(progress);
    }

    public void resetPlayState() {
        mTvCurTime.setText(CommonUtils.durationToString2(0));
        mTvTotalTime.setText(CommonUtils.durationToString2(0));
        mPlayBar.setMax(0);
        mPlayBar.setProgress(0);
    }

    @Override
    public String onHintTextChanged(HintSeekBar hintSeekBar, int progress) {
        return CommonUtils.durationToString2(progress);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mPlayService.pause();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mPlayService.seekTo(seekBar.getProgress());

    }
}
