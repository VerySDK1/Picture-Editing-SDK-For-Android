package com.vesdk.camera.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.vecore.base.lib.ui.Rotatable;
import com.vecore.base.lib.ui.RotateImageView;

/**
 * A view that contains camera zoom control which could adjust the zoom in/out
 * if the camera supports zooming.
 */
public abstract class ZoomControlLayout extends RelativeLayout implements Rotatable {
    // The states of zoom button.
    public static final int ZOOM_IN = 0;
    public static final int ZOOM_OUT = 1;
    public static final int ZOOM_STOP = 2;

    @SuppressWarnings("unused")
    private static final String TAG = "ZoomControl";
    private static final int ZOOMING_INTERVAL = 1000; // milliseconds

    protected ImageView mZoomIn;
    protected ImageView mZoomOut;
    protected ImageView mZoomSlider;
    protected int mOrientation;
    private Handler mHandler;

    public interface OnZoomChangedListener {
        void onZoomValueChanged(int index); // only for immediate zoom

        void onZoomStateChanged(int state); // only for smooth zoom
    }

    // The interface OnZoomIndexChangedListener is used to inform the
    // ZoomIndexBar about the zoom index change. The index position is between
    // 0 (the index is zero) and 1.0 (the index is mZoomMax).
    public interface OnZoomIndexChangedListener {
        void onZoomIndexChanged(double indexPosition);
    }

    protected int mZoomMax, mZoomIndex;
    private boolean mSmoothZoomSupported;
    private OnZoomChangedListener mListener;
    // private OnZoomIndexChangedListener mIndexListener;

    // protected OnIndicatorEventListener mOnIndicatorEventListener;
    private int mState;
    private int mStep;

    protected final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            performZoom(mState, false);
        }
    };

    public ZoomControlLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandler = new Handler();
    }

    public void startZoomControl() {
        mZoomSlider.setPressed(true);
        setZoomIndex(mZoomIndex); // Update the zoom index bar.
    }

    protected ImageView addImageView(Context context, int iconResourceId) {
        ImageView image = new RotateImageView(context);
        image.setImageResource(iconResourceId);
        addView(image);
        return image;
    }

    public void closeZoomControl() {
        mZoomSlider.setPressed(false);
        stopZooming();
        if (!mSmoothZoomSupported) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    public void setZoomMax(int zoomMax) {
        mZoomMax = zoomMax;
        requestLayout();
    }

    public void setOnZoomChangeListener(OnZoomChangedListener listener) {
        mListener = listener;
    }

    public void setZoomIndex(int index) {
        if (index < 0 || index > mZoomMax) {
            throw new IllegalArgumentException("Invalid zoom value:" + index);
        }
        mZoomIndex = index;
        invalidate();
    }

    public void setSmoothZoomSupported(boolean smoothZoomSupported) {
        mSmoothZoomSupported = smoothZoomSupported;
    }

    private boolean zoomIn() {
        return (mZoomIndex == mZoomMax) ? false : changeZoomIndex(mZoomIndex
                + mStep);
    }

    private boolean zoomOut() {
        return (mZoomIndex == 0) ? false : changeZoomIndex(mZoomIndex - mStep);
    }

    protected void setZoomStep(int step) {
        mStep = step;
    }

    private void stopZooming() {
        if (mSmoothZoomSupported) {
            if (mListener != null) {
                mListener.onZoomStateChanged(ZOOM_STOP);
            }
        }
    }

    // Called from ZoomControlWheel to change the zoom level.
    // TODO: merge the zoom control for both platforms.
    protected void performZoom(int state) {
        performZoom(state, true);
    }

    private void performZoom(int state, boolean fromUser) {
        if ((mState == state) && fromUser) {
            return;
        }
        if (fromUser) {
            mHandler.removeCallbacks(mRunnable);
        }
        mState = state;
        switch (state) {
            case ZOOM_IN:
                zoomIn();
                break;
            case ZOOM_OUT:
                zoomOut();
                break;
            case ZOOM_STOP:
                stopZooming();
                break;
            default:
                break;
        }
        if (!mSmoothZoomSupported) {
            // Repeat the zoom action on tablet as the user is still holding
            // the zoom slider.
            mHandler.postDelayed(mRunnable, ZOOMING_INTERVAL / mZoomMax);
        }
    }

    // Called from ZoomControlBar to change the zoom level.
    protected void performZoom(double zoomPercentage) {
        int index = (int) (mZoomMax * zoomPercentage);
        if (mZoomIndex == index) {
            return;
        }
        changeZoomIndex(index);
    }

    private boolean changeZoomIndex(int index) {
        if (mListener != null) {
            if (mSmoothZoomSupported) {
                int zoomType = (index < mZoomIndex) ? ZOOM_OUT : ZOOM_IN;
                if (((zoomType == ZOOM_IN) && (mZoomIndex != mZoomMax))
                        || ((zoomType == ZOOM_OUT) && (mZoomIndex != 0))) {
                    mListener.onZoomStateChanged(zoomType);
                }
            } else {
                if (index > mZoomMax) {
                    index = mZoomMax;
                }
                if (index < 0) {
                    index = 0;
                }
                mListener.onZoomValueChanged(index);
                mZoomIndex = index;
            }
        }
        return true;
    }

    @Override
    public void setOrientation(int orientation) {
        mOrientation = orientation;
        int count = getChildCount();
        for (int i = 0; i < count; ++i) {
            View view = getChildAt(i);
            if (view instanceof RotateImageView) {
                ((RotateImageView) view).setOrientation(orientation);
            }
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void setActivated(boolean activated) {
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            super.setActivated(activated);
            mZoomIn.setActivated(activated);
            mZoomOut.setActivated(activated);
        }
    }
}
