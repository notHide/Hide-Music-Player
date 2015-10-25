package com.zxsc.zxmusic.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import com.zxsc.zxmusic.R;
import com.zxsc.zxmusic.manager.ThemeManager;
import com.zxsc.zxmusic.utils.CommonUtils;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/16
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class TabLayout extends LinearLayout
        implements View.OnClickListener, ThemeManager.IThemeListener {

    private int mCurTab = 1;
    private int mHeight = 4;
    private Paint mIndicatorPaint;
    private Scroller mScroller;
    private Rect mCurRect;
    private ITabClickListener iTabClickListener;

    public TabLayout(Context context) {
        super(context);
        init();
    }

    public TabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mIndicatorPaint = new Paint();
        mIndicatorPaint.setColor(ThemeManager.with(getContext()).getCurrentColor());
        mScroller = new Scroller(getContext(), new DecelerateInterpolator());
        ThemeManager.with(getContext()).registerListener(this);
    }

    public void addTab(String tabText) {
        TextView textView = new TextView(getContext());
        textView.setText(tabText);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.title_text_size));
        textView.setTextColor(getTextColor());
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(this);
        LayoutParams params = new LayoutParams(0, (int) getResources().getDimension(R.dimen.title_height));
        params.weight = 1;
        addView(textView, params);
    }


    private ColorStateList getTextColor() {
        return new ColorStateList(new int[][]{
                new int[]{android.R.attr.state_selected}
                , new int[]{android.R.attr.state_pressed}
                , new int[0],
        }, new int[]{ThemeManager.with(getContext()).getCurrentColor()
                , ThemeManager.with(getContext()).getCurrentColor()
                , getResources().getColor(R.color.midnight_blue)});
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawLine(0, 0, getWidth(), 0, CommonUtils.getDividerPaint(getContext()));

        for (int i = 0; i < getChildCount() - 1; i++) {
            View child = getChildAt(i);
            int startX = child.getRight();
            int dividerHeight = child.getHeight() / 2;
            int startY = (child.getHeight() - dividerHeight) / 2;
            int stopY = startY + dividerHeight;
            canvas.drawLine(startX, startY, startX, stopY, CommonUtils.getDividerPaint(getContext()));
        }

        canvas.drawRect(mCurRect, mIndicatorPaint);


    }

    public void setCheckedNoAnim(int index) {

        getChildAt(mCurTab).setSelected(false);
        getChildAt(index).setSelected(true);
        mCurRect = getIndicatorRect(index);
        mCurTab = index;
        if (iTabClickListener != null) {
            iTabClickListener.onTabClick(index);
        }
        invalidate();
    }

    public void setChecked(int index) {
        onClick(getChildAt(index));
    }

    private int calculateSlideX(int targetIndex) {
        Rect rect2 = getIndicatorRect(targetIndex);
        return rect2.left - mCurRect.left;
    }

    private Rect getIndicatorRect(int index) {
        View child = getChildAt(index);
        int width = child.getWidth() / 2;
        int left = (child.getWidth() - width) / 2 + child.getLeft();
        int right = left + width;
        int bottom = (int) CommonUtils.dpToPx(getContext(), mHeight);
        return new Rect(left, 0, right, bottom);
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < getChildCount(); i++) {
            if (v != getChildAt(i)) {
                getChildAt(i).setSelected(false);
            } else {
                if (mCurTab == i) break;
                getChildAt(i).setSelected(true);
                mScroller.startScroll(mCurRect.left, 0, calculateSlideX(i), 0, 150);
                mCurTab = i;
                if (iTabClickListener != null) {
                    iTabClickListener.onTabClick(i);
                }
                invalidate();
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int width = mCurRect.right - mCurRect.left;
            mCurRect.left = mScroller.getCurrX();
            mCurRect.right = mCurRect.left + width;
            invalidate();
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SaveState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SaveState ss = (SaveState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mCurTab = ss.curTab;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SaveState ss = new SaveState(parcelable);
        ss.curTab = this.mCurTab;
        return ss;
    }

    public void setOnTabClickListener(ITabClickListener iTabClickListener) {
        this.iTabClickListener = iTabClickListener;
    }

    @Override
    public void onThemeChange(int color) {
        mIndicatorPaint.setColor(color);
        for (int i = 0; i < getChildCount(); i++) {
            TextView tv = (TextView) getChildAt(i);
            tv.setTextColor(getTextColor());
        }
        invalidate();
    }

    public interface ITabClickListener {
        void onTabClick(int index);
    }

    public int getCurrentTab() {
        return mCurTab;
    }

    private static class SaveState extends BaseSavedState {

        int curTab;

        public SaveState(Parcel source) {
            super(source);
            this.curTab = source.readInt();
        }

        public SaveState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.curTab);
        }

        public static final Parcelable.Creator<SaveState> CREATOR =
                new Parcelable.Creator<SaveState>() {
                    public SaveState createFromParcel(Parcel in) {
                        return new SaveState(in);
                    }

                    public SaveState[] newArray(int size) {
                        return new SaveState[size];
                    }
                };
    }


}
