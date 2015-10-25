package com.zxsc.zxmusic.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.zxsc.zxmusic.R;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/9/8
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class ToastUtils {

    private static Toast mToast;
    private static TextView mTvContent;

    public static void show(Context ctx, String text) {
        show(ctx, text, false);
    }

    public static void show(Context ctx, String text, boolean isLong) {
        if (mToast == null) {
            mToast = new Toast(ctx.getApplicationContext());
            View view = LayoutInflater.from(ctx.getApplicationContext())
                    .inflate(R.layout.toast, null);
            mTvContent = (TextView) view.findViewById(R.id.tv_content);
            mToast.setView(view);
        }
        if (isLong) {
            mToast.setDuration(Toast.LENGTH_LONG);
        } else {
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mTvContent.setText(text);
        mToast.show();
    }

}
