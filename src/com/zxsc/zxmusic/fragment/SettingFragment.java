package com.zxsc.zxmusic.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.zxsc.zxmusic.R;
import com.zxsc.zxmusic.ThemeColorChangeActivity;
import com.zxsc.zxmusic.manager.ThemeManager;
import com.zxsc.zxmusic.other.Constants;
import com.zxsc.zxmusic.other.HideApplication;
import com.zxsc.zxmusic.utils.CommonUtils;
import com.zxsc.zxmusic.utils.SharedUtils;
import com.zxsc.zxmusic.widget.SwitchButton;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/16
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class SettingFragment extends BaseFragment
        implements View.OnClickListener, ThemeManager.IThemeListener, CompoundButton.OnCheckedChangeListener {


    private TextView mThemeSetting;
    private SwitchButton mToggleListAnim;


    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        ThemeManager.with(getActivity()).registerListener(this);
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mThemeSetting = (TextView) mView.findViewById(R.id.setting_theme);
        mThemeSetting.setOnClickListener(this);

        mToggleListAnim = (SwitchButton) mView.findViewById(R.id.toggle_list_anim);
        mToggleListAnim.setOnCheckedChangeListener(this);
        if (HideApplication.showListAnim) {
            mToggleListAnim.setChecked(true);
        }

        setItemBg();
    }

    private void setItemBg() {
        CommonUtils.setThemeBg(getActivity(), mThemeSetting, R.drawable.item_bg_double_divider);
    }

    @Override
    protected void onLoading() {

    }

    @Override
    protected void onLoadFinish() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_theme:
                startActivity(ThemeColorChangeActivity.class);
                break;
        }
    }


    @Override
    public void onThemeChange(int color) {
        setItemBg();
        mToggleListAnim.setEnableColor(color);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mToggleListAnim) {
            HideApplication.showListAnim = isChecked;
            SharedUtils.saveBoolean(getActivity(), Constants.KEY_SHOW_LIST_ANIM, isChecked);
        }
    }
}
