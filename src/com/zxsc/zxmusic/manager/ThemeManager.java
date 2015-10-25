package com.zxsc.zxmusic.manager;

import android.content.Context;
import android.graphics.Color;
import com.zxsc.zxmusic.utils.SharedUtils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/16
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class ThemeManager {

    public final static String KEY = "cur_color";

    private static ThemeManager self;
    private Context mContext;
    private ArrayList<IThemeListener> iThemeListeners;

    public static int BACKGROUNDS[] = {Color.rgb(26, 89, 154),
            Color.rgb(234, 84, 84), Color.rgb(240, 90, 154),
            Color.rgb(192, 80, 26), Color.rgb(148, 83, 237),
            Color.rgb(75, 104, 228), Color.rgb(44, 162, 249),
            Color.rgb(4, 188, 205), Color.rgb(242, 116, 77),
            Color.rgb(249, 169, 42), Color.rgb(105, 200, 78),
            Color.rgb(30, 186, 118), Color.rgb(31, 190, 158),
            Color.rgb(161, 161, 161), Color.rgb(214, 117, 213),
            Color.rgb(242, 106, 138), Color.rgb(211, 173, 114),
            Color.rgb(191, 199, 112), Color.rgb(120, 213, 214),
            Color.rgb(52, 145, 120)};

    private ThemeManager(Context context) {
        this.mContext = context;
    }

    public static ThemeManager with(Context ctx) {
        if (self == null) {
            self = new ThemeManager(ctx);
        } else {
            self.mContext = ctx;
        }

        return self;
    }

    public int getCurrentColor() {
        return SharedUtils.getInt(mContext, KEY, BACKGROUNDS[0]);
    }

    public void saveColor(int index) {
        SharedUtils.saveInt(mContext, KEY, BACKGROUNDS[index]);
        notifyThemeChange();
    }

    public void registerListener(IThemeListener listener) {
        if (iThemeListeners == null) {
            iThemeListeners = new ArrayList<>();
        }
        iThemeListeners.add(listener);
    }

    public void notifyThemeChange() {
        if (iThemeListeners == null) return;
        int curColor = getCurrentColor();
        Iterator<IThemeListener> iterator = iThemeListeners.iterator();
        while (iterator.hasNext()) {
            IThemeListener next = iterator.next();
            if (next == null) {
                iterator.remove();
            } else {
                next.onThemeChange(curColor);
            }
        }
    }

    public interface IThemeListener {
        void onThemeChange(int color);
    }

}
