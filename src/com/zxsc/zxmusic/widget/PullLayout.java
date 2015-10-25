package com.zxsc.zxmusic.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
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
public class PullLayout extends FrameLayout {

    private static final int STATE_IDLE = 1;
    private static final int STATE_EXPAND = 2;
    private static final int STATE_COLLAPSE = 3;


    private ViewGroup mContentView;
    private ViewGroup mPullView;
    private View mContentChild;


    private int mPullMaxHeight;
    private int mCurPullRange;
    private int mTmpCurPullRange;
    private float mTouchY;
    private float mTouchX;
    private float mTmpTouchY;
    private Scroller mScroller;
    private ArrayList<IPullListener> listeners = new ArrayList<>();
    private float threshold;
    private int mCurState = STATE_IDLE;
    private boolean isChange;


    public PullLayout(Context context) {
        this(context, null);
    }

    public PullLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mScroller = new Scroller(context, new DecelerateInterpolator(2));
        threshold = CommonUtils.getScreenSize(context)[1] / 20;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mPullView = (ViewGroup) getChildAt(0);
        mContentView = (ViewGroup) getChildAt(1);
        mContentChild = mContentView.getChildAt(0);

        setPullViewHeight(0);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mPullMaxHeight = bottom - top;

        mContentView.layout(left, top + mCurPullRange, right, bottom);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTouchY = mTmpTouchY = ev.getY();
                mTouchX = ev.getX();
                if (mCurPullRange != 0 && mCurPullRange != mPullMaxHeight) {
                    mScroller.forceFinished(true);
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mContentChild instanceof AbsListView) {
                    AbsListView listView = (AbsListView) mContentChild;
                    View child = listView.getChildAt(0);
                    if (child != null) {
                        if (listView.getFirstVisiblePosition() != 0
                                || child.getTop() != 0) {
                            break;
                        }
                    }
                }

                if(Math.abs(mTouchX - ev.getX()) > threshold) {
                    return false;
                }

                if (mCurPullRange == 0) {
                    return ev.getY() - mTouchY > 0;
                } else if (mCurPullRange == mPullMaxHeight) {
                    return ev.getY() - mTouchY < 0;
                } else {
                    return true;
                }

        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mCurState = STATE_IDLE;
                mTmpCurPullRange = mCurPullRange;
                isChange = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                float curY = event.getY();
                if (Math.abs(curY - mTouchY) < threshold) {
                    mTmpTouchY = curY;
                    break;
                }
                float dy = curY - mTmpTouchY;
                if (dy < 0) {
                    mCurState = STATE_COLLAPSE;
                } else {
                    mCurState = STATE_EXPAND;
                }
                mCurPullRange += (int) (curY - mTmpTouchY) / 2;
                mTmpTouchY = curY;
                adjustCurPullRange();
                if (!isChange && mCurPullRange != mTmpCurPullRange) {
                    isChange = true;
                }
                if (!isChange) break;
                setPullViewHeight(mCurPullRange);
                requestLayout();
                notifyPullChange();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!isChange) {
                    if (mCurPullRange != 0 && mCurPullRange != mPullMaxHeight) {
                        collapse();
                    }
                    break;
                }
                switch (mCurState) {
                    case STATE_EXPAND:
                        expand();
                        break;
                    case STATE_COLLAPSE:
                        collapse();
                        break;
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    public void expand() {
        startScroll(mPullMaxHeight - mCurPullRange);
    }

    public void collapse() {
        startScroll(-mCurPullRange);
    }

    private void startScroll(int dx) {
        mScroller.startScroll(mCurPullRange, 0, dx, 0, 500);
        requestLayout();
    }

    private void adjustCurPullRange() {
        mCurPullRange = Math.max(0, Math.min(mCurPullRange, mPullMaxHeight));
    }

    private void setPullViewHeight(int height) {
        ViewGroup.LayoutParams params = mPullView.getLayoutParams();
        params.height = height;
        mPullView.setLayoutParams(params);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mCurPullRange = mScroller.getCurrX();
            setPullViewHeight(mCurPullRange);
            requestLayout();

            notifyPullChange();
            if (mCurPullRange == 0) {
                mScroller.forceFinished(true);
                notifyCollapse();
                mCurState = STATE_IDLE;
            } else if (mCurPullRange == mPullMaxHeight) {
                mScroller.forceFinished(true);
                notifyExpanded();
                mCurState = STATE_IDLE;
            }

        }
    }

    public void addOnPullListener(IPullListener listener) {
        listeners.add(listener);
    }

    private void notifyExpanded() {
        for (IPullListener listener : listeners) {
            if (listener != null) {
                listener.onExpanded();
            }
        }


    }

    private void notifyCollapse() {
        for (IPullListener listener : listeners) {
            if (listener != null) {
                listener.onCollapsed();
            }
        }
    }

    private void notifyPullChange() {
        for (IPullListener listener : listeners) {
            if (listener != null) {
                listener.onPullChange(mCurPullRange, mPullMaxHeight);
            }
        }
    }

    public interface IPullListener {
        void onExpanded();

        void onPullChange(int cur, int max);

        void onCollapsed();
    }
}

