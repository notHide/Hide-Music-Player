package com.zxsc.zxmusic.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import com.zxsc.zxmusic.R;
import com.zxsc.zxmusic.manager.ThemeManager;
import com.zxsc.zxmusic.utils.CommonUtils;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/21
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class SwitchButton extends ToggleButton
        implements CompoundButton.OnCheckedChangeListener {

    private static final int BORDER_WIDTH = 2;
    private static final long MOVEMENT_ANIMATION_DURATION_MS = 200;
    private static final int OPAQUE = 255;
    private static final float SELECTOR_RATIO = 0.85f;

    private final RectF backgroundRect = new RectF(0, 0, 0, 0);
    private final Point currentSelectorCenter = new Point(0, 0);
    private final Point disabledSelectorCenter = new Point(0, 0);
    private final Point enabledSelectorCenter = new Point(0, 0);
    private final Interpolator interpolator = new DecelerateInterpolator(1.0f);
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int selectorRadius;
    private int disableColor;
    private int enableColor;
    private boolean isInitial;

    private OnCheckedChangeListener mListener;

    public SwitchButton(Context context) {
        super(context);
        initialize();
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public SwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }


    private void initialize() {
        if (isInitial) return;

        setBackgroundColor(Color.argb(0, 0, 0, 0));
        super.setOnCheckedChangeListener(this);

        disableColor = getResources().getColor(R.color.clouds);
        enableColor = ThemeManager.with(getContext()).getCurrentColor();
        isInitial = true;
    }

    @Override
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int[] size = CommonUtils.getViewSizeFromSpec(this, widthMeasureSpec, heightMeasureSpec);
        int width = size[0];
        int height = size[1];

        selectorRadius = height / 2;
        enabledSelectorCenter.set(width - selectorRadius, height / 2);
        disabledSelectorCenter.set(selectorRadius, height / 2);
        if (isChecked()) {
            currentSelectorCenter.set(enabledSelectorCenter.x, enabledSelectorCenter.y);
        } else {
            currentSelectorCenter.set(disabledSelectorCenter.x, disabledSelectorCenter.y);
        }

        int borderPadding = BORDER_WIDTH / 2;
        backgroundRect.set(borderPadding, borderPadding, width - borderPadding,
                height - borderPadding);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        drawBorder(canvas);
        drawSelector(canvas);
    }

    //    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (mListener != null) {
            mListener.onCheckedChanged(buttonView, isChecked);
        }

        setEnabled(false);

        ObjectAnimator animator = ObjectAnimator.ofFloat(SwitchButton.this, "animationProgress", 0, 1);
        animator.setDuration(MOVEMENT_ANIMATION_DURATION_MS);

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private void drawBackground(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        if (isEnabled()) {
            paint.setColor(getBgColor());
        } else {
            if (isChecked()) {
                paint.setColor(evaluate(tmpAnimProgress, disableColor, enableColor));
            } else {
                paint.setColor(evaluate(tmpAnimProgress, enableColor, disableColor));
            }
        }
        canvas.drawRoundRect(backgroundRect, selectorRadius, selectorRadius, paint);
    }

    private int getBgColor() {
        return isChecked() ? enableColor : disableColor;
    }

    private void drawBorder(Canvas canvas) {
        paint.setStrokeWidth(BORDER_WIDTH);
        paint.setStyle(Paint.Style.STROKE);

        paint.setColor(getResources().getColor(R.color.clouds));
        canvas.drawRoundRect(backgroundRect, selectorRadius, selectorRadius, paint);
    }

    private void drawSelector(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.WHITE);
        paint.setAlpha(OPAQUE);
        canvas.drawCircle(currentSelectorCenter.x, currentSelectorCenter.y,
                (int) (selectorRadius * SELECTOR_RATIO), paint);
    }


    float tmpAnimProgress;

    public void setAnimationProgress(float animationProgress) {
        int left = disabledSelectorCenter.x;
        int right = enabledSelectorCenter.x;

        tmpAnimProgress = animationProgress;

        currentSelectorCenter.x = interpolate(animationProgress, left, right);
        if (!isChecked()) {
            currentSelectorCenter.x = getWidth() - currentSelectorCenter.x;
        }

        postInvalidate();
    }

    private int interpolate(float animationProgress, int left, int right) {
        return (int) (left + interpolator.getInterpolation(animationProgress) * (right - left));
    }

    public void setEnableColor(int enableColor) {
        this.enableColor = enableColor;
        postInvalidate();
    }

    private int evaluate(float fraction, Object startValue, int endValue) {
        int startInt = (int) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;
        int endA = (endValue >> 24) & 0xff;
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;
        return (startA + (int) (fraction * (endA - startA))) << 24
                | (startR + (int) (fraction * (endR - startR))) << 16
                | (startG + (int) (fraction * (endG - startG))) << 8
                | (startB + (int) (fraction * (endB - startB)));
    }


}
