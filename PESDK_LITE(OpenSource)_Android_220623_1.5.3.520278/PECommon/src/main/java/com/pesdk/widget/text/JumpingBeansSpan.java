package com.pesdk.widget.text;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.text.TextPaint;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

final class JumpingBeansSpan extends SuperscriptSpan implements ValueAnimator.AnimatorUpdateListener {

    private final WeakReference<TextView> textView;
    private final int delay;
    private final int loopDuration;
    private final float animatedRange;
    private int shift;
    private ValueAnimator jumpAnimator;

    public JumpingBeansSpan(@NonNull TextView textView,
                            @IntRange(from = 1) int loopDuration,
                            @IntRange(from = 0) int position,
                            @IntRange(from = 0) int waveCharOffset,
                            @FloatRange(from = 0, to = 1, fromInclusive = false) float animatedRange) {
        this.textView = new WeakReference<>(textView);
        this.delay = waveCharOffset * position;
        this.loopDuration = loopDuration;
        this.animatedRange = animatedRange;
    }

    @Override
    public void updateMeasureState(TextPaint tp) {
        initIfNecessary(tp.ascent());
        tp.baselineShift = shift;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        initIfNecessary(tp.ascent());
        tp.baselineShift = shift;
    }

    private void initIfNecessary(float ascent) {
        if (jumpAnimator != null) {
            return;
        }

        this.shift = 0;
        int maxShift = (int) ascent / 2;
        jumpAnimator = ValueAnimator.ofInt(0, maxShift);
        jumpAnimator
                .setDuration(loopDuration)
                .setStartDelay(delay);
        jumpAnimator.setInterpolator(new JumpInterpolator(animatedRange));
        jumpAnimator.setRepeatCount(ValueAnimator.INFINITE);
        jumpAnimator.setRepeatMode(ValueAnimator.RESTART);
        jumpAnimator.addUpdateListener(this);
        jumpAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        // No need for synchronization as this always runs on main thread anyway
        TextView v = textView.get();
        if (v != null) {
            updateAnimationFor(animation, v);
        } else {
            cleanupAndComplainAboutUserBeingAFool();
        }
    }

    private void updateAnimationFor(ValueAnimator animation, TextView v) {
        if (isAttachedToHierarchy(v)) {
            shift = (int) animation.getAnimatedValue();
            v.invalidate();
        }
    }

    private static boolean isAttachedToHierarchy(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return v.isAttachedToWindow();
        }
        return v.getParent() != null;   // Best-effort fallback (without adding support-v4 just for this...)
    }

    private void cleanupAndComplainAboutUserBeingAFool() {
        // The textview has been destroyed and teardown() hasn't been called
        teardown();
        Log.w("JumpingBeans", "!!! Remember to call JumpingBeans.stopJumping() when appropriate !!!");
    }

    public void teardown() {
        if (jumpAnimator != null) {
            jumpAnimator.cancel();
            jumpAnimator.removeAllListeners();
        }
        if (textView.get() != null) {
            textView.clear();
        }
    }

    private static class JumpInterpolator implements TimeInterpolator {

        private final float animRange;

        public JumpInterpolator(float animatedRange) {
            animRange = Math.abs(animatedRange);
        }

        @Override
        public float getInterpolation(float input) {
            // We want to map the [0, PI] sine range onto [0, animRange]
            if (input > animRange) {
                return 0f;
            }
            double radians = (input / animRange) * Math.PI;
            return (float) Math.sin(radians);
        }

    }

}
