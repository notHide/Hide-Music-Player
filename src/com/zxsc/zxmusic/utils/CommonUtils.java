package com.zxsc.zxmusic.utils;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Environment;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import com.zxsc.zxmusic.R;
import com.zxsc.zxmusic.manager.ThemeManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/16
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class CommonUtils {

    private static Paint sPaint;
    private static int[] sScreenSize;
    private static LayoutTransition mLayoutTransition;

    public static float dpToPx(Context ctx, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics());
    }

    public static Paint getDividerPaint(Context ctx) {
        if (sPaint == null) {
            sPaint = new Paint();
            sPaint.setColor(ctx.getResources().getColor(R.color.divider));
            sPaint.setStrokeWidth(dpToPx(ctx, 1.5f));
        }
        return sPaint;
    }

    public static int[] getScreenSize(Context ctx) {
        if (sScreenSize == null) {
            DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
            sScreenSize = new int[]{metrics.widthPixels, metrics.heightPixels};
        }
        return sScreenSize;
    }

    public static void setThemeBg(Context ctx, View view, int drawableId) {

        ColorDrawable pressDrawable = new ColorDrawable(ThemeManager.with(ctx).getCurrentColor());

        StateListDrawable sDrawable = new StateListDrawable();
        sDrawable.addState(new int[]{android.R.attr.state_pressed}, pressDrawable);
        sDrawable.addState(new int[]{}, ctx.getResources().getDrawable(drawableId));

        view.setBackground(sDrawable);
    }

    public static String imageToLocal(Bitmap bitmap, String name) {
        String path = null;
        try {
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                return null;
            }
            String filePath = Environment.getExternalStorageDirectory().getPath() + "/cache/";
            File file = new File(filePath, name);
            if (file.exists()) {
                path = file.getAbsolutePath();
                return path;
            } else {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();

            OutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }

            path = file.getAbsolutePath();
        } catch (Exception e) {
        }
        return path;
    }

    public static Bitmap toCircleBitmap(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int r = width < height ? width : height;

        Bitmap buffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(buffer);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        RectF rect = new RectF(0, 0, r, r);

        canvas.drawCircle(r / 2, r / 2, r / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);
        bitmap.recycle();
        return buffer;
    }

    public static String getFileSize(String path) {
        File file = new File(path);
        return getFileSize(file.length());
    }

    public static String getFileSize(long size) {
        StringBuilder sBuffer = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#.00");

        if (size < 1024) {
            sBuffer.append(size);
            sBuffer.append("B");
        } else if (size < 1024 * 1024) { //不足1mb，显示kb
            sBuffer.append(df.format(size / 1024.0));
            sBuffer.append("KB");
        } else if (size < 1024 * 1024 * 1024) {//不足1G，显示M
            sBuffer.append(df.format(size / 1024.0 / 1024.0));
            sBuffer.append("M");
        } else {//否则显示G
            sBuffer.append(df.format(size / 1024.0 / 1024.0 / 1024.0));
            sBuffer.append("G");
        }
        return sBuffer.toString();
    }

    public static String durationToString(long duration) {
        int secondAll = (int) (duration / 1000);
        int minute = secondAll / 60;
        int second = secondAll % 60;
        return String.format("%1$d分%2$d秒", minute, second);
    }

    public static String durationToString2(long duration) {
        int secondAll = (int) (duration / 1000);
        int minute = secondAll / 60;
        int second = secondAll % 60;
        return String.format("%02d:%02d ", minute, second);
    }

    public static int[] getViewSizeFromSpec(View view, int widthSpec, int heightSpec) {
        int minWidth = View.MeasureSpec.getSize(widthSpec) + view.getPaddingLeft() + view.getPaddingRight();
        int width = ViewCompat.resolveSizeAndState(minWidth, widthSpec, 1);

        int minHeight = View.MeasureSpec.getSize(heightSpec) + view.getPaddingBottom() + view.getPaddingTop();
        int height = ViewCompat.resolveSizeAndState(minHeight, heightSpec, 0);
        return new int[]{width, height};
    }

    public static Bitmap scaleBitmap(Context ctx, String bitmapPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bitmapPath, options);

        int resultWidth = (int) CommonUtils.dpToPx(ctx, 100);
        int max = options.outWidth > options.outHeight ? options.outWidth : options.outHeight;
        float ratio = max * 1.0f / resultWidth;
        if (ratio < 1.0f) ratio = 1.0f;

        options.inSampleSize = Math.round(ratio);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(bitmapPath, options);
    }


}
