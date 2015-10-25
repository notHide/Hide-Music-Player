package com.zxsc.zxmusic.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.zxsc.zxmusic.MainActivity;
import com.zxsc.zxmusic.R;
import com.zxsc.zxmusic.widget.RevealLayout;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/16
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public abstract class BaseFragment extends Fragment {

    protected RevealLayout mView;
    private boolean isFirstVisible = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = new RevealLayout(getActivity());
        View view = inflateView(inflater, container);
        mView.addView(view);
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            ((MainActivity) getActivity()).onFragmentCreate(this);
        }
    }

    protected abstract View inflateView(LayoutInflater inflater, ViewGroup container);


    @Override
    public void onStart() {
        super.onStart();

        if (isFirstVisible) {
            isFirstVisible = false;
            start();
        }

    }

    protected void start() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                onLoading();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (getActivity() == null
                        || getActivity().isFinishing()
                        || isDetached()) {
                    return;
                }
                onLoadFinish();
            }
        }.execute();
    }

    public void showCircleReveal(final int x, final int y, final int duration) {
        if (mView == null) return;
        mView.post(new Runnable() {
            @Override
            public void run() {
                if (mView.isInitialed(x, y)) {
                    mView.show(x, y, duration);
                }

            }
        });
    }

    protected abstract void onLoading();

    protected abstract void onLoadFinish();

    protected void startActivity(Class<? extends Activity> cls) {
        Intent intent = new Intent(getActivity(), cls);
        getActivity().startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_bottom_in, R.anim.scale_out);
    }

}
