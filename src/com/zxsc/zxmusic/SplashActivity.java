package com.zxsc.zxmusic;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.zxsc.zxmusic.manager.ShakeManager;
import com.zxsc.zxmusic.utils.ToastUtils;

public class SplashActivity extends BaseActivity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        ShakeManager.with(this).startShakeListener(new ShakeManager.ISensor() {
            @Override
            public void onSensorChange(float force) {
                if (force > 10) {
                    ShakeManager.with(SplashActivity.this).cancel();

                    jumpActivity();

                }
            }
        });
        ToastUtils.show(this, "『摇一摇』进入", true);

    }

    private void jumpActivity() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.shake);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                finish();
                startActivityNoAnim(MainActivity.class);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        findViewById(R.id.splash).startAnimation(animation);

    }


    @Override
    public void onBackPressed() {
        finish();
        startActivityNoAnim(MainActivity.class);
    }
}
