package com.pesdk.uisdk.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import com.pesdk.uisdk.R;

public class CircleProgressBar extends View {
    private static final ColorFilter PRORESS_BACKGROUP_FILTER = new LightingColorFilter(
            Color.DKGRAY, 0x010101);
    private static final int MAX_LEVEL = 10000;
    private static final int ANIMATION_RESOLUTION = 200;

    private static int MAX_PROGRESS = 100;
    // private static String TAG = "CircleProgressBar";
    private int mAngle = -1;
    private RectF mArcRect;
    private int mProgress;
    private int mMax = MAX_PROGRESS;
    private Bitmap mMemBitmap;
    private Canvas mMemCanvas;
    private Paint mPaint;
    private boolean mIndeterminate;
    private Drawable mIndeterminateDrawable;
    private Drawable mProgressDrawable;
    private Drawable mCurrentDrawable;
    private boolean mShouldStartAnimationDrawable;
    private Transformation mTransformation;
    private AlphaAnimation mAnimation;
    private Interpolator mInterpolator;
    private boolean mInDrawing;
    private long mLastDrawTime;
    private boolean mNoInvalidate;

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            mNoInvalidate = true;
            TypedArray ta = context.obtainStyledAttributes(attrs,
                    R.styleable.pesdk_ExtCircleProgressBar);
            mProgressDrawable = ta
                    .getDrawable(R.styleable.pesdk_ExtCircleProgressBar_extprogressDrawable);
            if (mProgressDrawable != null) {
                setProgressDrawable(mProgressDrawable);
            }

            mIndeterminateDrawable = ta
                    .getDrawable(R.styleable.pesdk_ExtCircleProgressBar_extindetermiateDrawable);
            if (mIndeterminateDrawable != null) {
                if (mIndeterminateDrawable instanceof BitmapDrawable) {
                    mIndeterminateDrawable = new AnimatedRotateDrawable(
                            AnimatedRotateDrawable
                                    .defaultAnimatedRotateState(mIndeterminateDrawable),
                            getResources());
                }
                setIndeterminateDrawable(mIndeterminateDrawable);
            }

            this.mPaint = new Paint();
            this.mPaint.setStyle(Paint.Style.FILL);
            this.mPaint.setAntiAlias(true);
            this.mPaint.setColor(0);
            this.mPaint.setXfermode(new PorterDuffXfermode(
                    PorterDuff.Mode.CLEAR));

            setIndeterminate(ta.getBoolean(
                    R.styleable.pesdk_ExtCircleProgressBar_extindetermiate, false));
            mNoInvalidate = false;
            ta.recycle();
        }
    }

    public int getMax() {
        return this.mMax;
    }

    public int getProgress() {
        return this.mProgress;
    }

    public void setProgressDrawable(Drawable d) {
        if (d != null) {
            d.setCallback(this);
            requestLayout();
        }
        mProgressDrawable = d;
        if (!mIndeterminate) {
            mCurrentDrawable = d;
            postInvalidate();
        }
    }

    public void setIndeterminateDrawable(Drawable d) {
        if (d != null) {
            d.setCallback(this);
        }
        mIndeterminateDrawable = d;
        if (mIndeterminate) {
            mCurrentDrawable = d;
            postInvalidate();
        }
    }

    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable d = mCurrentDrawable;
        if (null != d) {
            canvas.save();
            canvas.translate(getPaddingLeft(), getPaddingTop());
            if (!mIndeterminate) {
                d.setColorFilter(PRORESS_BACKGROUP_FILTER);
                d.draw(canvas);
                d.clearColorFilter();
                if (null != mMemBitmap) {
                    this.mMemBitmap.eraseColor(0);
                }
                if (null != d) {
                    d.draw(mMemCanvas);
                }
                this.mMemCanvas.drawArc(this.mArcRect, 270 - this.mAngle,
                        this.mAngle, true, this.mPaint);
                canvas.drawBitmap(this.mMemBitmap, 0.0F, 0.0F, null);
            } else {
                long time = getDrawingTime();
                if (mAnimation != null) {
                    mAnimation.getTransformation(time, mTransformation);
                    float scale = mTransformation.getAlpha();
                    try {
                        mInDrawing = true;
                        d.setLevel((int) (scale * MAX_LEVEL));
                    } finally {
                        mInDrawing = false;
                    }
                    if (SystemClock.uptimeMillis() - mLastDrawTime >= ANIMATION_RESOLUTION) {
                        mLastDrawTime = SystemClock.uptimeMillis();
                        postInvalidateDelayed(ANIMATION_RESOLUTION);
                    }
                }
                d.draw(canvas);
            }
            canvas.restore();
            if (mShouldStartAnimationDrawable && d instanceof Animatable) {
                ((Animatable) d).start();
                mShouldStartAnimationDrawable = false;
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = mCurrentDrawable;
        if (d != null) {
            int dw = 0;
            int dh = 0;
            if (d != null) {
                dw = Math.max(50, d.getIntrinsicWidth());
                dh = Math.max(50, d.getIntrinsicHeight());
            }
            dw += getPaddingLeft() + getPaddingRight();
            dh += getPaddingTop() + getPaddingBottom();

            setMeasuredDimension(resolveSize(dw, widthMeasureSpec),
                    resolveSize(dh, heightMeasureSpec));
        }
    }

    public synchronized void setMax(int max) {
        if ((max > 0) && (this.mMax != max)) {
            this.mMax = max;
            setProgress(this.mProgress);
        }
    }

    public synchronized void setProgress(int progress) {
        this.mProgress = progress;
        if (this.mProgress > this.mMax)
            this.mProgress = this.mMax;
        else if (this.mProgress < 0) {
            this.mProgress = 0;
        }
        int nOffsetProgress = (this.mMax - this.mProgress);
        int i = 360 * nOffsetProgress / this.mMax;
        if (i != this.mAngle) {
            this.mAngle = i;
            invalidate();
        }
    }

    public synchronized void setIndeterminate(boolean indeterminate) {
        if (indeterminate != mIndeterminate) {
            mIndeterminate = indeterminate;

            if (indeterminate) {
                mCurrentDrawable = mIndeterminateDrawable;
                startAnimation();
            } else {
                mCurrentDrawable = mProgressDrawable;
                if (null != mMemBitmap) {
                    mMemBitmap.recycle();
                    mMemBitmap = null;
                }
                this.mMemBitmap = Bitmap.createBitmap(
                        mCurrentDrawable.getIntrinsicWidth(),
                        mCurrentDrawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
                this.mMemCanvas = new Canvas(this.mMemBitmap);
                this.mArcRect = new RectF(0.0F, 0.0F, mMemBitmap.getWidth(),
                        mMemBitmap.getHeight());
                stopAnimation();
            }
        }
    }

    @Override
    public void setVisibility(int v) {
        if (getVisibility() != v) {
            super.setVisibility(v);

            if (mIndeterminate) {
                // let's be nice with the UI thread
                if (v == GONE || v == INVISIBLE) {
                    stopAnimation();
                } else {
                    startAnimation();
                }
            }
        }
    }

    @Override
    public void postInvalidate() {
        if (!mNoInvalidate) {
            super.postInvalidate();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mIndeterminate) {
            startAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mIndeterminate) {
            stopAnimation();
        }
    }

    @Override
    public void invalidateDrawable(Drawable dr) {
        if (!mInDrawing) {
            if (verifyDrawable(dr)) {
                final Rect dirty = dr.getBounds();
                final int scrollX = getScrollX() + getPaddingLeft();
                final int scrollY = getScrollY() + getPaddingTop();

                invalidate(dirty.left + scrollX, dirty.top + scrollY,
                        dirty.right + scrollX, dirty.bottom + scrollY);
            } else {
                super.invalidateDrawable(dr);
            }
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        int[] state = getDrawableState();

        if (mProgressDrawable != null && mProgressDrawable.isStateful()) {
            mProgressDrawable.setState(state);
        }

        if (mIndeterminateDrawable != null
                && mIndeterminateDrawable.isStateful()) {
            mIndeterminateDrawable.setState(state);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mProgressDrawable || who == mIndeterminateDrawable
                || super.verifyDrawable(who);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // onDraw will translate the canvas so we draw starting at 0,0
        int right = w - getPaddingRight() - getPaddingLeft();
        int bottom = h - getPaddingBottom() - getPaddingTop();

        if (mIndeterminateDrawable != null) {
            mIndeterminateDrawable.setBounds(0, 0, right, bottom);
        }

        if (mProgressDrawable != null) {
            mProgressDrawable.setBounds(0, 0, right, bottom);
        }
    }

    void startAnimation() {
        if (getVisibility() != VISIBLE) {
            return;
        }

        if (mIndeterminateDrawable instanceof Animatable) {
            mShouldStartAnimationDrawable = true;
            invalidate();
        } else {
            if (mInterpolator == null) {
                mInterpolator = new LinearInterpolator();
            }
            if (mTransformation != null) {
                mTransformation.clear();
            }
            if (null != mAnimation) {
                mAnimation.reset();
                mAnimation = null;
            }
            mTransformation = new Transformation();
            mAnimation = new AlphaAnimation(0.0f, 1.0f);
            mAnimation.setRepeatMode(AlphaAnimation.RESTART);
            mAnimation.setRepeatCount(Animation.INFINITE);
            mAnimation.setDuration(4000);
            mAnimation.setInterpolator(mInterpolator);
            mAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);
            postInvalidate();
        }
    }

    private void stopAnimation() {
        mAnimation = null;
        mTransformation = null;
        if (mIndeterminateDrawable instanceof Animatable) {
            ((Animatable) mIndeterminateDrawable).stop();
            mShouldStartAnimationDrawable = false;
        }
    }

}
class AnimatedRotateDrawable extends Drawable implements Drawable.Callback,
        Runnable, Animatable {

    private AnimatedRotateState mState;
    private boolean mMutated;
    private float mCurrentDegrees;
    private float mIncrement;
    private boolean mRunning;
    private Callback mCallback = null;

    public AnimatedRotateDrawable(AnimatedRotateState rotateState, Resources res) {
        mState = new AnimatedRotateState(rotateState, this, res);
        init();
    }

    private void init() {
        final AnimatedRotateState state = mState;
        mIncrement = 360.0f / (float) state.mFramesCount;
        final Drawable drawable = state.mDrawable;
        if (drawable != null) {
            drawable.setFilterBitmap(true);
            if (drawable instanceof BitmapDrawable) {
                ((BitmapDrawable) drawable).setAntiAlias(true);
            }
        }
    }

    public void draw(Canvas canvas) {
        int saveCount = canvas.save();

        final AnimatedRotateState st = mState;
        final Drawable drawable = st.mDrawable;
        final Rect bounds = drawable.getBounds();

        int w = bounds.right - bounds.left;
        int h = bounds.bottom - bounds.top;

        float px = st.mPivotXRel ? (w * st.mPivotX) : st.mPivotX;
        float py = st.mPivotYRel ? (h * st.mPivotY) : st.mPivotY;

        canvas.rotate(mCurrentDegrees, px, py);

        drawable.draw(canvas);

        canvas.restoreToCount(saveCount);
    }

    public void start() {
        if (!mRunning) {
            mRunning = true;
            nextFrame();
        }
    }

    public void stop() {
        mRunning = false;
        unscheduleSelf(this);
    }

    public boolean isRunning() {
        return mRunning;
    }

    private void nextFrame() {
        unscheduleSelf(this);
        scheduleSelf(this, SystemClock.uptimeMillis() + mState.mFrameDuration);
    }

    public void run() {
        // amount
        // of time since the last frame drawn
        mCurrentDegrees += mIncrement;
        if (mCurrentDegrees > (360.0f - mIncrement)) {
            mCurrentDegrees = 0.0f;
        }
        invalidateSelf();
        nextFrame();
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        mState.mDrawable.setVisible(visible, restart);
        boolean changed = super.setVisible(visible, restart);
        if (visible) {
            if (changed || restart) {
                mCurrentDegrees = 0.0f;
                nextFrame();
            }
        } else {
            unscheduleSelf(this);
        }
        return changed;
    }

    /**
     * Returns the drawable rotated by this RotateDrawable.
     */
    public Drawable getDrawable() {
        return mState.mDrawable;
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations()
                | mState.mChangingConfigurations
                | mState.mDrawable.getChangingConfigurations();
    }

    public void setAlpha(int alpha) {
        mState.mDrawable.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mState.mDrawable.setColorFilter(cf);
    }

    public int getOpacity() {
        return mState.mDrawable.getOpacity();
    }

    public void invalidateDrawable(Drawable who) {
        if (mCallback != null) {
            mCallback.invalidateDrawable(this);
        }
    }

    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (mCallback != null) {
            mCallback.scheduleDrawable(this, what, when);
        }
    }

    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (mCallback != null) {
            mCallback.unscheduleDrawable(this, what);
        }
    }

    @Override
    public boolean getPadding(Rect padding) {
        return mState.mDrawable.getPadding(padding);
    }

    @Override
    public boolean isStateful() {
        return mState.mDrawable.isStateful();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mState.mDrawable.setBounds(bounds.left, bounds.top, bounds.right,
                bounds.bottom);
    }

    @Override
    public int getIntrinsicWidth() {
        return mState.mDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mState.mDrawable.getIntrinsicHeight();
    }

    @Override
    public ConstantState getConstantState() {
        if (mState.canConstantState()) {
            mState.mChangingConfigurations = super.getChangingConfigurations();
            return mState;
        }
        return null;
    }

    public static AnimatedRotateState defaultAnimatedRotateState(Drawable dw) {
        AnimatedRotateState rotateState = new AnimatedRotateState(null, null,
                null);
        rotateState.mDrawable = dw;
        rotateState.mPivotXRel = true;
        rotateState.mPivotX = 0.5f;
        rotateState.mPivotYRel = true;
        rotateState.mPivotY = 0.5f;
        rotateState.mFramesCount = 12;
        rotateState.mFrameDuration = 100;
        return rotateState;
    }


    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mState.mDrawable.mutate();
            mMutated = true;
        }
        return this;
    }

    final static class AnimatedRotateState extends Drawable.ConstantState {
        Drawable mDrawable;

        int mChangingConfigurations;

        boolean mPivotXRel;
        float mPivotX;
        boolean mPivotYRel;
        float mPivotY;
        int mFrameDuration;
        int mFramesCount;

        private boolean mCanConstantState;
        private boolean mCheckedConstantState;

        public AnimatedRotateState(AnimatedRotateState source,
                                   AnimatedRotateDrawable owner, Resources res) {
            if (source != null) {
                if (res != null) {
                    mDrawable = source.mDrawable.getConstantState()
                            .newDrawable(res);
                } else {
                    mDrawable = source.mDrawable.getConstantState()
                            .newDrawable();
                }
                mDrawable.setCallback(owner);
                mPivotXRel = source.mPivotXRel;
                mPivotX = source.mPivotX;
                mPivotYRel = source.mPivotYRel;
                mPivotY = source.mPivotY;
                mFramesCount = source.mFramesCount;
                mFrameDuration = source.mFrameDuration;
                mCanConstantState = mCheckedConstantState = true;
            }
        }

        @Override
        public Drawable newDrawable() {
            return new AnimatedRotateDrawable(this, null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new AnimatedRotateDrawable(this, res);
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }

        public boolean canConstantState() {
            if (!mCheckedConstantState) {
                mCanConstantState = mDrawable.getConstantState() != null;
                mCheckedConstantState = true;
            }

            return mCanConstantState;
        }
    }

}