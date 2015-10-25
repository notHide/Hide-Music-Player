package com.zxsc.zxmusic.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import app.mosn.zdepthshadowlayout.ZDepthShadowLayout;
import app.mosn.zdepthshadowlayout.utils.DisplayUtils;
import com.zxsc.zxmusic.R;
import com.zxsc.zxmusic.utils.CommonUtils;

public class FloatingButton extends ZDepthShadowLayout {

    protected int mButtonSizeDp = 60;
    protected int mIconSizeDp = 50;
    private Bitmap mIconBitmap;
    private View.OnClickListener mClickListener;

    public FloatingButton(Context context) {
        this(context, null);
    }

    public FloatingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    @Override
    protected void init(AttributeSet attrs, int defStyle) {
        super.init(attrs, defStyle);

        mAttrShape = SHAPE_OVAL;
        mAttrZDepth = 2;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        int buttonSize = DisplayUtils.convertDpToPx(getContext(), mButtonSizeDp);
        int iconSize = DisplayUtils.convertDpToPx(getContext(), mIconSizeDp);

        ImageView bgImage = new ImageView(getContext());
        bgImage.setLayoutParams(new LayoutParams(buttonSize, buttonSize));
        bgImage.setImageResource(R.drawable.drawable_circle);
        bgImage.setOnClickListener(mClickListener);
        addView(bgImage);


        LayoutParams iconLP = new LayoutParams(iconSize, iconSize);
        iconLP.gravity = Gravity.CENTER;

        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(iconLP);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
        addView(imageView);
    }

    public void setIcon(Bitmap bitmap) {
//        if (getChildCount() < 2) return;
//        View view = getChildAt(2);
//        if (!(view instanceof ImageView)) return;
        View view = getChildAt(2);
        ImageView iv = (ImageView) view;
        if (bitmap == null) {
            iv.setImageResource(R.drawable.ic_launcher);
        } else {
            iv.setImageBitmap(CommonUtils.toCircleBitmap(bitmap));
        }
        if (mIconBitmap != null
                && !mIconBitmap.isRecycled()) {
            mIconBitmap.recycle();
        }
        mIconBitmap = bitmap;
    }

    @Override
    public void setOnClickListener(View.OnClickListener clickListener) {
        this.mClickListener = clickListener;
        if (getChildCount() > 1) {
            getChildAt(0).setOnClickListener(clickListener);
        }
    }
}
