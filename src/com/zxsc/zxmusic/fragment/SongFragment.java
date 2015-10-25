package com.zxsc.zxmusic.fragment;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;
import com.zxsc.zxmusic.MainActivity;
import com.zxsc.zxmusic.R;
import com.zxsc.zxmusic.db.SongDb;
import com.zxsc.zxmusic.manager.SongManager;
import com.zxsc.zxmusic.manager.ThemeManager;
import com.zxsc.zxmusic.model.LastSong;
import com.zxsc.zxmusic.model.SongInfo;
import com.zxsc.zxmusic.other.HideApplication;
import com.zxsc.zxmusic.service.PlayService;
import com.zxsc.zxmusic.service.ScanService;
import com.zxsc.zxmusic.utils.CommonUtils;
import com.zxsc.zxmusic.utils.ToastUtils;
import com.zxsc.zxmusic.widget.PullLayout;
import com.zxsc.zxmusic.widget.PullRightLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/16
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class SongFragment extends BaseFragment
        implements PullLayout.IPullListener, View.OnClickListener, ThemeManager.IThemeListener, PlayService.IMusicListener {


    private ListView mListView;
    private View mEmptyRoot;
    private TextView mStartScanView;
    private PullLayout mPullLayout;
    private ImageView mIvScan;
    private ViewGroup mFooterView;

    private boolean shouldStartAnim;
    private SongAdapter mSongAdapter;
    private ServiceConnection playConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayService.MyBinder binder = (PlayService.MyBinder) service;
            mPlayService = binder.getService();
            mPlayService.addMusicListener(SongFragment.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mSongAdapter == null) return;
            mSongAdapter.setPlaying(-1);
        }
    };
    private PlayService mPlayService;
    private PullLayout.IPullListener mListener;


    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        Intent playIntent = new Intent(getActivity(), PlayService.class);
        getActivity().startService(playIntent);
        getActivity().bindService(playIntent, playConn, Context.BIND_AUTO_CREATE);

        ThemeManager.with(getActivity()).registerListener(this);

        return inflater.inflate(R.layout.fragment_song, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unbindService(playConn);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        mListView = (ListView) mView.findViewById(R.id.lv);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    PullRightLayout.collapseAll();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        mStartScanView = (TextView) mView.findViewById(R.id.tv_start_scan);
        mStartScanView.setBackgroundColor(ThemeManager.with(getActivity()).getCurrentColor());
        mStartScanView.setOnClickListener(this);

        mPullLayout = (PullLayout) mView.findViewById(R.id.pull_layout);
        mPullLayout.addOnPullListener(this);
        if (mListener != null) {
            mPullLayout.addOnPullListener(mListener);
            mListener = null;
        }

        mIvScan = (ImageView) mView.findViewById(R.id.iv_scan);

        IntentFilter filter = new IntentFilter(ScanService.ACTION_SCAN_FINISH);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);

    }

    private View getEmptyView() {
        if (mEmptyRoot == null) {
            mEmptyRoot = mView.findViewById(R.id.empty_root);
            ImageView mIvEmpty = (ImageView) mView.findViewById(R.id.iv_empty);
            TextView mTvEmpty = (TextView) mView.findViewById(R.id.tv_empty);

            mIvEmpty.setImageResource(R.drawable.hand);
            mTvEmpty.setText("下拉扫描新歌曲");
        }
        return mEmptyRoot;
    }

    @Override
    protected void onLoading() {
        SongManager.with(getActivity()).fetchSongFromDb();
    }


    @Override
    protected void onLoadFinish() {
        updateList();
        LastSong lastSong = SongDb.getLastSong(getActivity());
        if (lastSong != null && getActivity() != null) {
            ((MainActivity) getActivity()).onMusicPlay(lastSong.getId());
        }
    }

    @Override
    public void onExpanded() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(200);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mStartScanView.setVisibility(View.VISIBLE);
                shouldStartAnim = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mStartScanView.startAnimation(alphaAnimation);
    }

    @Override
    public void onPullChange(int cur, int max) {
        if (shouldStartAnim) {
            shouldStartAnim = false;
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            alphaAnimation.setDuration(200);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mStartScanView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mStartScanView.startAnimation(alphaAnimation);
        }

    }

    @Override
    public void onCollapsed() {
    }

    @Override
    public void onClick(View v) {
        AnimationDrawable drawable = (AnimationDrawable) mIvScan.getBackground();
        if (drawable.isRunning()) {
            ToastUtils.show(getActivity(), "正在搜索中...");
        } else {
            drawable.start();
            Intent intent = new Intent(getActivity(), ScanService.class);
            intent.putExtras(new Bundle());
            SongManager.with(getActivity()).clearSong();
            updateList();
            getActivity().startService(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    @Override
    public void onThemeChange(int color) {
        mStartScanView.setBackgroundColor(color);
        if (mSongAdapter != null) {
            mSongAdapter.notifyDataSetChanged();
        }
    }

    private void updateList() {
        View footerView = getFooterView();
        if (mListView.getFooterViewsCount() == 0) {
            mListView.addFooterView(footerView);
        }
        if (mSongAdapter == null) {
            mSongAdapter = new SongAdapter();
            mListView.setEmptyView(getEmptyView());
            mListView.setAdapter(mSongAdapter);
        } else {
            mSongAdapter.notifyDataSetChanged();
        }
    }

    private View getFooterView() {
        if (mFooterView == null) {
            FrameLayout fl = new FrameLayout(getActivity());
            TextView tv = new TextView(getActivity());
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(0xFFaaaaaa);
            tv.setBackgroundColor(0xFFffffff);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , (int) CommonUtils.dpToPx(getActivity(), 50));
            fl.addView(tv, params);
            mFooterView = fl;
        }
        TextView tv = (TextView) mFooterView.getChildAt(0);
        tv.setText("共有" + SongManager.with(getActivity()).getSongSize() + "首歌");
        return mFooterView;
    }

    @Override
    public void onMusicPlay(int songId) {
        if (mSongAdapter == null) return;
        mSongAdapter.setPlaying(songId);

        if (getActivity() != null) {
            ((MainActivity) getActivity()).onMusicPlay(songId);
        }
    }

    @Override
    public void onMusicPlayByUser(int songId) {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).onMusicPlay(songId);
        }
    }

    @Override
    public void onMusicPause() {

    }

    @Override
    public void onMusicStop() {
        if (mSongAdapter == null) return;
        mSongAdapter.setPlaying(-1);
    }

    @Override
    public void onMusicPlaying(int progress, long max) {
        if (mSongAdapter != null
                && mSongAdapter.getPlayingId() == 0) {
            SongInfo song = SongManager.with(getActivity()).getCurrentSong();
            if (song != null) {
                mSongAdapter.setPlaying(song.getId());
            }
        }
    }

    private class SongAdapter extends BaseAdapter {

        private int playingId;
        private int animTime = 230;
        private ArrayList<PullRightLayout> convertViewList;


        public SongAdapter() {
            convertViewList = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return SongManager.with(getActivity()).getSongSize();
        }

        @Override
        public SongInfo getItem(int position) {
            return SongManager.with(getActivity()).getSongByIndex(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_song, null);
                holder.mTvName = (TextView) convertView.findViewById(R.id.tv_name);
                holder.mTvArtist = (TextView) convertView.findViewById(R.id.tv_artist);
                holder.mTvDuration = (TextView) convertView.findViewById(R.id.tv_duration);
                holder.aboveView = convertView.findViewById(R.id.item_root);
                holder.behindView = convertView.findViewById(R.id.behind);
                convertView.setTag(holder);
                convertViewList.add((PullRightLayout) convertView);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (HideApplication.showListAnim) {
                startAnim(convertView);
            }

            SongInfo info = SongManager.with(getActivity()).getSongByIndex(position);
            holder.mTvName.setText(info.getTitle());
            holder.mTvArtist.setText(info.getArtist() + " - " + info.getAlbum());
            holder.mTvDuration.setText(CommonUtils.durationToString(info.getDuration()) + " - " + CommonUtils.getFileSize(info.getPath()));

            setItemSelected(holder, info.getId() == playingId);

            convertView.setOnClickListener(new OnItemClick(position));
            holder.behindView.setOnClickListener(new OnBehindClick(position));

            return convertView;
        }

        private void startAnim(View convertView) {
            convertView.setPivotX(0.0f);
            convertView.setPivotY(1.0f);
            PropertyValuesHolder x = PropertyValuesHolder.ofFloat("scaleX", 0.0f, 1.0f);
            PropertyValuesHolder y = PropertyValuesHolder.ofFloat("scaleY", 0.0f, 1.0f);
            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(convertView, x, y);
            animator.setDuration(animTime);
            animator.start();
        }


        public List<PullRightLayout> getConvertViews() {
            return convertViewList;
        }

        private void setItemSelected(ViewHolder holder, boolean b) {

            if (b) {
                holder.aboveView.setBackgroundColor(ThemeManager.with(getActivity()).getCurrentColor());
                holder.mTvName.setTextColor(Color.WHITE);
                holder.mTvArtist.setTextColor(Color.WHITE);
                holder.mTvDuration.setTextColor(Color.WHITE);
            } else {
                CommonUtils.setThemeBg(getActivity(), holder.aboveView, R.drawable.item_bg_no_divider);
                holder.mTvName.setTextColor(getActivity().getResources().getColorStateList(R.color.black_normal_white_pressed));
                holder.mTvArtist.setTextColor(getActivity().getResources().getColorStateList(R.color.gray_normal_white_pressed));
                holder.mTvDuration.setTextColor(getActivity().getResources().getColorStateList(R.color.gray_normal_white_pressed));
            }
        }

        public void setPlaying(int songId) {
            playingId = songId;
            notifyDataSetChanged();
        }

        public int getPlayingId() {
            return playingId;
        }

        class ViewHolder {
            View aboveView;
            View behindView;
            TextView mTvName;
            TextView mTvArtist;
            TextView mTvDuration;
        }

        private class OnItemClick implements View.OnClickListener {

            private int position;

            public OnItemClick(int position) {
                this.position = position;
            }

            @Override
            public void onClick(final View v) {

                if (PullRightLayout.collapseAll()) return;

                for (PullRightLayout convertView : convertViewList) {
                    if (convertView == null) continue;

                    if (((ViewHolder) convertView.getTag()).aboveView == v) {
                        mPlayService.play(getItem(position).getId(), true);
                        playingId = getItem(position).getId();
                        setItemSelected((ViewHolder) convertView.getTag(), true);
                    } else {
                        setItemSelected((ViewHolder) convertView.getTag(), false);
                    }
                }


            }
        }

        private class OnBehindClick implements View.OnClickListener {

            private int position;

            public OnBehindClick(int position) {
                this.position = position;
            }

            @Override
            public void onClick(View v) {
                ToastUtils.show(getActivity(), "已删除：" + getItem(position).getTitle());
                SongManager.with(getActivity()).deleteSong(getItem(position).getId());
                PullRightLayout.collapseAll();
                updateList();
            }
        }
    }

    public void addOnPullListener(PullLayout.IPullListener listener) {
        if (mPullLayout == null) {
            this.mListener = listener;
            return;
        }
        mPullLayout.addOnPullListener(listener);
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AnimationDrawable drawable = (AnimationDrawable) mIvScan.getBackground();
            drawable.stop();
            ToastUtils.show(context, "找到" + SongManager.with(getActivity()).getSongSize() + "首歌");

            updateList();
        }
    };

}
