package com.vesdk.camera.handler;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.vesdk.camera.widget.ZoomControlLayout;
import com.vecore.recorder.api.ICameraZoomHandler;

/**
 * 处理摄像头变焦
 *
 * @author abreal
 */
public class CameraZoomHandler implements ICameraZoomHandler {

    private int mZoomState = ZOOM_STOPPED;
    private boolean mSmoothZoomSupported = false;
    private boolean mIsZoomSupported = false;
    /**
     * 缩放
     */
    private int mZoomValue;
    private int mZoomMax;
    private int mTargetZoomValue;
    /**
     * v
     */
    private final ZoomControlLayout mZoomControl;
    private final ZoomListener mZoomListener = new ZoomListener();
    /**
     * 暂停
     */
    private boolean mPausing;
    private Camera mMainCamera;
    private final ScaleGestureDetector mScaleDetector;
    private boolean mHandleScale = false;
    private boolean mRecording;

    private int mSetIndex;

    /**
     * 构造函数
     */
    public CameraZoomHandler(Context ctx, ZoomControlLayout zoomCtrl) {
        mZoomControl = zoomCtrl;
        // Log.e(TAG, String.format("onScale scale:%.2f",scale));
        // 步进修正
        // int nZoomStateTmp = ZoomControl.ZOOM_STOP;
        // 放大
        // nZoomStateTmp = ZoomControl.ZOOM_IN;
        // nZoomStateTmp = ZoomControl.ZOOM_OUT;
        // Log.d(TAG, String.format("New value:%d,scale:%.3f", m_nSetIndex,
        // scale));
        ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                setCameraZoom(mSetIndex);
                mHandleScale = false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                mSetIndex = mZoomValue;
                mHandleScale = true;
                return mHandleScale;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scale = detector.getScaleFactor();
                // Log.e(TAG, String.format("onScale scale:%.2f",scale));
                if (Float.isNaN(scale) || Float.isInfinite(scale)) {
                    return true;
                }
                int nStepValue = 0; // 步进修正
                int nOldSetIndex = mSetIndex;
                // int nZoomStateTmp = ZoomControl.ZOOM_STOP;

                mSetIndex = Math.round(mSetIndex * scale);
                if (scale != 1.0f) {
                    if (scale > 1.0f) { // 放大
                        nStepValue = 2;
                        // nZoomStateTmp = ZoomControl.ZOOM_IN;
                    } else {
                        nStepValue = -2;
                        // nZoomStateTmp = ZoomControl.ZOOM_OUT;
                    }
                }

                mSetIndex += nStepValue;

                if (mSetIndex > mZoomMax) {
                    mSetIndex = mZoomMax;
                } else if (mSetIndex < 1) {
                    mSetIndex = 1;
                }
                if (Math.abs(nOldSetIndex - mSetIndex) > 10) {
                    mSetIndex = nOldSetIndex;
                }
                setCameraZoom(mSetIndex);
                // Log.d(TAG, String.format("New value:%d,scale:%.3f", m_nSetIndex,
                // scale));
                return true;
            }
        };
        mScaleDetector = new ScaleGestureDetector(ctx, mScaleGestureListener);
    }

    @Override
    public Camera getMainCamera() {
        return mMainCamera;
    }

    @Override
    public void setMainCamera(Camera mainCamera) {
        this.mMainCamera = mainCamera;
    }

    public boolean isPausing() {
        return mPausing;
    }

    public void setPausing(boolean pausing) {
        this.mPausing = pausing;
    }

    public void setRecording(boolean bRecording) {
        mRecording = bRecording;
    }

    @Override
    public int getZoomState() {
        return mZoomState;
    }

    @Override
    public void setZoomState(int zoomState) {
        this.mZoomState = zoomState;
    }

    @Override
    public int getZoomValue() {
        return mZoomValue;
    }

    @Override
    public void initializeZoom() {
        if (null != mMainCamera) {
            Parameters params = mMainCamera.getParameters();
            mSmoothZoomSupported = params.isSmoothZoomSupported();
            mIsZoomSupported = params.isZoomSupported();

            mZoomMax = params.getMaxZoom();
            // Currently we use immediate zoom for fast zooming to get better UX
            // and
            // there is no plan to take advantage of the smooth zoom.
            if (null != mZoomControl && mIsZoomSupported) {
                mZoomControl.setZoomMax(mZoomMax);
                mZoomControl.setZoomIndex(params.getZoom());
                mZoomControl.setSmoothZoomSupported(mSmoothZoomSupported);
                mZoomControl.setOnZoomChangeListener(new ZoomChangeListener());
            }
            mMainCamera.setZoomChangeListener(mZoomListener);
        }
    }

    /**
     * 响应手势
     */
    @Override
    public boolean onTouch(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        return mHandleScale;
    }

    private class ZoomChangeListener implements ZoomControlLayout.OnZoomChangedListener {
        // only for immediate zoom
        @Override
        public void onZoomValueChanged(int index) {
            CameraZoomHandler.this.onZoomValueChanged(index);
        }

        // only for smooth zoom
        @Override
        public void onZoomStateChanged(int state) {
            if (mPausing) {
                return;
            }

            if (state == ZoomControlLayout.ZOOM_IN) {
                CameraZoomHandler.this.onZoomValueChanged(mZoomMax);
            } else if (state == ZoomControlLayout.ZOOM_OUT) {
                CameraZoomHandler.this.onZoomValueChanged(0);
            } else {
                mTargetZoomValue = -1;
                if (mZoomState == ZOOM_START) {
                    mZoomState = ZOOM_STOPPING;
                    mMainCamera.stopSmoothZoom();
                }
            }
        }
    }

    private class ZoomListener implements Camera.OnZoomChangeListener {
        @Override
        public void onZoomChange(int value, boolean stopped, Camera camera) {
            mZoomValue = value;
            mZoomControl.setZoomIndex(value);
            setCameraZoom(value);
            if (stopped && mZoomState != ZOOM_STOPPED) {
                if (mTargetZoomValue != -1 && value != mTargetZoomValue) {
                    mMainCamera.startSmoothZoom(mTargetZoomValue);
                    mZoomState = ZOOM_START;
                } else {
                    mZoomState = ZOOM_STOPPED;
                }
            }
        }
    }

    private void onZoomValueChanged(int index) {
        // Not useful to change zoom value when the activity is paused.
        if (mPausing) {
            return;
        }

        if (mSmoothZoomSupported) {
            if (mTargetZoomValue != index && mZoomState != ZOOM_STOPPED) {
                mTargetZoomValue = index;
                if (mZoomState == ZOOM_START) {
                    mZoomState = ZOOM_STOPPING;
                    mMainCamera.stopSmoothZoom();
                }
            } else if (mZoomState == ZOOM_STOPPED && mZoomValue != index) {
                mTargetZoomValue = index;
                mMainCamera.startSmoothZoom(index);
                mZoomState = ZOOM_START;
            }
        } else {
            setCameraZoom(index);
        }
    }

    /**
     * 设置摄像头变焦值
     */
    private void setCameraZoom(int index) {
        try {
            Parameters param = mMainCamera.getParameters();
            if (mZoomValue != index
                    && mIsZoomSupported
                    && (param.isZoomSupported() || param
                    .isSmoothZoomSupported())) {
                mZoomValue = index;
                param.setZoom(mZoomValue);
                mMainCamera.setParameters(param);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
