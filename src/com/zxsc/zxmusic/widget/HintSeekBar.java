package com.zxsc.zxmusic.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.*;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import com.zxsc.zxmusic.R;
import com.zxsc.zxmusic.utils.CommonUtils;

public class HintSeekBar extends SeekBar implements SeekBar.OnSeekBarChangeListener {

    private int mPopupWidth;
    private int mPopupStyle;
    public static final int POPUP_FIXED = 1;
    public static final int POPUP_FOLLOW = 0;

    private PopupWindow mPopup;
    private TextView mPopupTextView;

    private int[] positions;
    private int[] screenSizes;

    private OnSeekBarChangeListener mInternalListener;
    private OnSeekBarChangeListener mExternalListener;

    private OnSeekBarHintProgressChangeListener mProgressChangeListener;

    public interface OnSeekBarHintProgressChangeListener {
        String onHintTextChanged(HintSeekBar hintSeekBar, int progress);
    }

    public HintSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public HintSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public HintSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        setOnSeekBarChangeListener(this);


        mPopupWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        post(new Runnable() {
            @Override
            public void run() {
                positions = new int[2];
                screenSizes = CommonUtils.getScreenSize(getContext());
                getLocationOnScreen(positions);
            }
        });
        mPopupStyle = POPUP_FOLLOW;

        initHintPopup();
    }

    public void setPopupStyle(int style) {
        mPopupStyle = style;
    }

    public int getPopupStyle() {
        return mPopupStyle;
    }

    private void initHintPopup() {
        String popupText = null;

        if (mProgressChangeListener != null) {
            popupText = mProgressChangeListener.onHintTextChanged(this, getProgress());
        }

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View undoView = inflater.inflate(R.layout.toast, null);
        mPopupTextView = (TextView) undoView.findViewById(R.id.tv_content);
        mPopupTextView.setText(popupText != null ? popupText : String.valueOf(getProgress()));

        mPopup = new PopupWindow(undoView, mPopupWidth, ViewGroup.LayoutParams.WRAP_CONTENT, false);

        mPopup.setAnimationStyle(R.style.fade_animation);

    }

    private void showPopup() {

        if (getMax() == 0 || positions == null) return;
        if (mPopupStyle == POPUP_FOLLOW) {
//            mPopup.showAtLocation(this, Gravity.LEFT | Gravity.BOTTOM, (int) (this.getX() + (int) getXPosition(this)), (int) (this.getY() + mYLocationOffset + this.getHeight()));
            mPopup.showAtLocation(this, Gravity.LEFT | Gravity.BOTTOM
                    , getXPosition()
                    , getYPosition()
            );
        }
//        if (mPopupStyle == POPUP_FIXED) {
//            mPopup.showAtLocation(this, Gravity.CENTER | Gravity.BOTTOM, 0, (int) (this.getY() + mYLocationOffset + this.getHeight()));
//        }
    }

    private void hidePopup() {
        if (mPopup.isShowing()) {
            mPopup.dismiss();
        }
    }

    public void setHintView(View view) {
        //initHintPopup();
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        if (mInternalListener == null) {
            mInternalListener = l;
            super.setOnSeekBarChangeListener(l);
        } else {
            mExternalListener = l;
        }
    }

    public void setOnProgressChangeListener(OnSeekBarHintProgressChangeListener l) {
        mProgressChangeListener = l;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        String popupText = null;
        if (mProgressChangeListener != null) {
            popupText = mProgressChangeListener.onHintTextChanged(this, getProgress());
        }

        if (mExternalListener != null) {
            mExternalListener.onProgressChanged(seekBar, progress, b);
        }


        mPopupTextView.setText(popupText != null ? popupText : String.valueOf(progress));

//        if (mPopupStyle == POPUP_FOLLOW) {
//            mPopup.update((int) (this.getX() + (int) getXPosition(seekBar)), (int) (this.getY() + mYLocationOffset + this.getHeight()), -1, -1);
//        }
        View popupView = mPopup.getContentView();
        if (mPopupStyle == POPUP_FOLLOW) {
            mPopup.update(getXPosition(), getYPosition(), -1, -1);
        }

    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mExternalListener != null) {
            mExternalListener.onStartTrackingTouch(seekBar);
        }

        showPopup();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mExternalListener != null) {
            mExternalListener.onStopTrackingTouch(seekBar);
        }

        hidePopup();
    }


    private int getXPosition() {

        if (positions == null || getMax() == 0) return 0;
        int start = positions[0] + getThumbOffset();
        float progress = getProgress() * 1.0f / getMax();
        float progressWidth = (getWidth() - getThumbOffset() * 2) * progress;


        return (int) (start + progressWidth + CommonUtils.dpToPx(getContext(), 5));
    }

    private int getYPosition() {
        if (positions == null || getMax() == 0) return 0;

        return (int) (screenSizes[1] - positions[1] + CommonUtils.dpToPx(getContext(), 10));
    }
}
