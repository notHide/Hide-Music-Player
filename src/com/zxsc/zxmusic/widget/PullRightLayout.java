package com.zxsc.zxmusic.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;
import com.zxsc.zxmusic.utils.CommonUtils;

import java.util.ArrayList;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/18
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class PullRightLayout extends FrameLayout {

    public static final int STATE_COLLAPSE = 1;
    public static final int STATE_SCROLLING = 2;
    public static final int STATE_EXPAND = 3;

    public static ArrayList<PullRightLayout> sSelfs = new ArrayList<>();

    private ViewGroup mAboveView;
    private ViewGroup mBehindView;

    private int[] mScreenSize;
    private int mHeight;
    private int mMaxRange;
    private int mCurRange;
    private int mCurState = STATE_COLLAPSE;
    private float mTouchX;
    private int mThreshold;
    private float mLastTouchX;
    private float mLastRange;
    private Scroller mScroller;
    private View.OnClickListener onClickListener;


    public PullRightLayout(Context context) {
        super(context);
        init();
    }


    public PullRightLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullRightLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mScreenSize = CommonUtils.getScreenSize(getContext());
        mMaxRange = mScreenSize[0] / 5;
        mThreshold = mScreenSize[0] / 12;
        mScroller = new Scroller(getContext(), new LinearInterpolator());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBehindView = (ViewGroup) getChildAt(0);
        mAboveView = (ViewGroup) getChildAt(1);
        if (onClickListener != null) {
            mAboveView.setOnClickListener(onClickListener);
            onClickListener = null;
        }
        sSelfs.add(this);

        mBehindView.setLayoutParams(new LayoutParams(mMaxRange, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        sSelfs.remove(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (!mScroller.isFinished()) return true;

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = mLastTouchX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                mLastTouchX = ev.getX();
                float range = Math.abs(ev.getX() - mTouchX);
                if (ev.getX() - mTouchX < 0
                        && range >= mThreshold) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                disallowParent(false);
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mScroller.isFinished()) return true;


        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = mLastTouchX = event.getX();
                if (mCurState == STATE_EXPAND) {
                    if (touchBehind(event)) {
                        return true;
                    }
                    collapse();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCurState == STATE_EXPAND) {
                    collapse();
                    return true;
                }
                float range = Math.abs(event.getX() - mTouchX);
                if (event.getX() - mTouchX < 0
                        && range >= mThreshold
                        && mCurState != STATE_SCROLLING) {
                    mLastTouchX = event.getX();
                    mCurState = STATE_SCROLLING;
                    return false;
                }
                if (mCurState != STATE_SCROLLING) break;
                disallowParent(true);
                mLastRange = mLastTouchX - event.getX();
                mCurRange += mLastRange;
                mCurRange = clamp(mCurRange, 0, mMaxRange);
                mLastTouchX = event.getX();
                requestLayout();
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                disallowParent(false);
                if (mCurState == STATE_SCROLLING) {
                    if (mLastRange > 0) {
                        expand();
                    } else {
                        collapse();
                    }
                    return false;
                }
                if (mCurState == STATE_EXPAND) {
                    return touchBehind(event);
                }

                break;
        }

        return super.onTouchEvent(event);
    }

    private int clamp(int cur, int min, int max) {
        return Math.max(min, Math.min(cur, max));
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mHeight = bottom - top;
        int width = right - left;
        mAboveView.layout(left - mCurRange, 0, right - mCurRange, mHeight);
        mBehindView.layout(width - mCurRange, 0, width, mHeight);

    }

    public void disallowParent(boolean b) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(b);
        }
    }

    public void expand() {
        for (PullRightLayout pullRightLayout : sSelfs) {
            if (pullRightLayout.getState() == STATE_EXPAND) {
                pullRightLayout.collapse();
            }
        }

        if (!mScroller.isFinished()) return;
        startScroll(mMaxRange - mCurRange);
    }

    public static boolean collapseAll() {
        boolean collapsed = false;
        for (PullRightLayout pullRightLayout : sSelfs) {
            if (pullRightLayout != null && pullRightLayout.getState() == STATE_EXPAND) {
                pullRightLayout.collapse();
                collapsed = true;
            }
        }
        return collapsed;


    }

    public void collapse() {
        if (!mScroller.isFinished()) return;
        startScroll(-mCurRange);
    }

    private void startScroll(int dx) {
        mScroller.startScroll(mCurRange, 0, dx, 0, 100);
        requestLayout();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mCurRange = mScroller.getCurrX();
            requestLayout();

            if (mCurRange == 0) {
                mCurState = STATE_COLLAPSE;
                mScroller.forceFinished(true);
            } else if (mCurRange == mMaxRange) {
                mCurState = STATE_EXPAND;
                mScroller.forceFinished(true);
            }
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        if (mAboveView != null) {
            mAboveView.setOnClickListener(l);
        } else {
            onClickListener = l;
        }
    }

    public boolean touchBehind(MotionEvent event) {


        if (mCurState != STATE_EXPAND) return false;

        return !(event.getX() < mBehindView.getLeft()
                || event.getX() > mBehindView.getRight()
                || event.getY() < mBehindView.getTop()
                || event.getY() > mBehindView.getBottom());
    }

    public int getState() {
        return mCurState;
    }


}

