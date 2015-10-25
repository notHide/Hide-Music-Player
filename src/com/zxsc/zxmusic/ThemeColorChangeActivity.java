package com.zxsc.zxmusic;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.zxsc.zxmusic.manager.ThemeManager;
import com.zxsc.zxmusic.utils.CommonUtils;
import com.zxsc.zxmusic.widget.TitleBar;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/19
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class ThemeColorChangeActivity extends BaseActivity
        implements TitleBar.ITitleBackListener, View.OnClickListener {

    private TitleBar mTitle;
    private LinearLayout mColorPanel;
    private ImageView mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_change);

        mTitle = (TitleBar) findViewById(R.id.title);
        mTitle.setTitle("主题颜色");
        mTitle.setOnBackListener(this);

        mPreview = (ImageView) findViewById(R.id.preview);
        mPreview.setBackgroundColor(ThemeManager.with(this).getCurrentColor());

        mColorPanel = (LinearLayout) findViewById(R.id.select_panel);
        for (int i = 0; i < ThemeManager.BACKGROUNDS.length; i++) {
            View view = new View(this);
            view.setBackgroundColor(ThemeManager.BACKGROUNDS[i]);
            view.setOnClickListener(this);

            int dp70 = (int) CommonUtils.dpToPx(this, 70);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp70, dp70);
            int margin = (int) CommonUtils.dpToPx(this, 10);
            params.setMargins(margin, margin, margin, margin);

            mColorPanel.addView(view, params);
        }


    }

    @Override
    public void onTitleBackClick() {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < mColorPanel.getChildCount(); i++) {
            if (v == mColorPanel.getChildAt(i)) {
                mPreview.setBackgroundColor(ThemeManager.BACKGROUNDS[i]);
                ThemeManager.with(this).saveColor(i);
                break;
            }
        }
    }

}
