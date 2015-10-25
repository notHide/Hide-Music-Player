package com.zxsc.zxmusic.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import app.mosn.zdepthshadowlayout.ZDepthShadowLayout;
import com.zxsc.zxmusic.R;
import com.zxsc.zxmusic.manager.ThemeManager;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/16
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class TitleBar extends ZDepthShadowLayout
        implements ThemeManager.IThemeListener {

    private RelativeLayout mTitleBar;
    private ImageView mLeftView;
    private ImageView mRightView;
    private TextView mTitleText;

    public TitleBar(Context context) {
        super(context);
        init();
    }

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mTitleBar = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.title, null);
        mLeftView = (ImageView) mTitleBar.findViewById(R.id.title_left);
        mTitleText = (TextView) mTitleBar.findViewById(R.id.title_text);
        mRightView = (ImageView) mTitleBar.findViewById(R.id.title_right);
        int height = (int) getContext().getResources().getDimension(R.dimen.title_height);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        addView(mTitleBar, params);

        mLeftView.setVisibility(INVISIBLE);
        mRightView.setVisibility(INVISIBLE);

        mTitleBar.setBackgroundColor(ThemeManager.with(getContext()).getCurrentColor());
        mLeftView.setBackground(getContext().getResources().getDrawable(R.drawable.dark_selector));
        mRightView.setBackground(getContext().getResources().getDrawable(R.drawable.dark_selector));

        ThemeManager.with(getContext()).registerListener(this);
    }

    public void setOnBackListener(final ITitleBackListener listener) {
        mLeftView.setVisibility(View.VISIBLE);
        mLeftView.setImageResource(R.drawable.back_arrow);
        if (listener != null) {
            mLeftView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onTitleBackClick();
                }
            });
        }
    }

    public void setTitle(String text) {
        mTitleText.setText(text);
    }

    @Override
    public void onThemeChange(int color) {
        mTitleBar.setBackgroundColor(color);
    }

    public interface ITitleBackListener {
        void onTitleBackClick();
    }

}
