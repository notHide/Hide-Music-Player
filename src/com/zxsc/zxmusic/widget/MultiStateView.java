package com.zxsc.zxmusic.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/22
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class MultiStateView extends ImageView
        implements View.OnClickListener {

    private SparseIntArray mStateMap;
    private ArrayList<Integer> mStateList;
    private int mCurState;
    private IStateChangeListener mListener;

    public MultiStateView(Context context) {
        super(context);
        init();
    }


    public MultiStateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiStateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mStateMap = new SparseIntArray();
        mStateList = new ArrayList<>();
        setOnClickListener(this);
    }

    public void addStateAndImage(final int state, final int resId) {
        post(new Runnable() {
            @Override
            public void run() {
                mStateMap.put(state, resId);
                mStateList.add(state);
                if (mStateList.size() == 1) {
                    mCurState = state;
                    show(false);
                }
            }
        });
    }

    public void show(final int state) {
        if (mCurState == state) return;
        post(new Runnable() {
            @Override
            public void run() {
                mCurState = state;
                show(false);
            }
        });
    }

    private void show(boolean shouldNotify) {

        int id = mStateMap.get(mCurState);
        if (id == 0) return;


        setImageResource(id);

        if (shouldNotify && mListener != null) {
            mListener.onStateChange(mCurState);
        }
    }

    public void setOnStateChangeListener(IStateChangeListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onClick(View v) {
        int size = mStateList.size();
        for (int i = 0; i < size; i++) {
            if (mCurState == mStateList.get(i)) {
                mCurState = mStateList.get((i + 1) % size);
                break;
            }
        }
        show(true);
    }


    public interface IStateChangeListener {
        void onStateChange(int state);
    }

}
