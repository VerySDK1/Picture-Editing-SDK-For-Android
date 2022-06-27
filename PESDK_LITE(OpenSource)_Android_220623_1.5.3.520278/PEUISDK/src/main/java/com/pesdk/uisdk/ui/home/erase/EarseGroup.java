package com.pesdk.uisdk.ui.home.erase;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.pesdk.uisdk.widget.doodle.DoodleView;
import com.pesdk.widget.ZoomView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 处理消除笔界面下的touch分发
 */
public class EarseGroup extends FrameLayout {
    public EarseGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private static final String TAG = "EarseGroup";

    private boolean bDouble = false;

    private ZoomView mZoomView;

    int nCount = 0;
    private boolean bCancleTouch = false;

    private void reset() {
        bDouble = false;
        mZoomView = null;
        nCount = 0;
        bCancleTouch = false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
//        Log.e(TAG, "dispatchTouchEvent: " + nCount + " " + ev);
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {//每次触摸时清理
            reset();
        }
        // nCount<(2+1)  +1 是为了容错 . （如果是双指第2次就能明确收到是双指的指令 ACTION_POINTER_DOWN(1), 若第2次还未收到双指指令，则不再监听双指手指,全部响应为单指触摸）
        if (nCount < 3 && (ev.getPointerCount() >= 2 || bDouble)) { //接收到双指事件后，本次触摸的事件全部交由zoomview处理
            if (null == mZoomView && getChildCount() == 2) {
                View view = getChildAt(0);
                if (view instanceof ZoomView) {
                    mZoomView = (ZoomView) view;
                    bDouble = true;
                }

                view = getChildAt(1);
                if (view instanceof DoodleView) {
                    DoodleView tmp = (DoodleView) view;
                    if (!bCancleTouch) {
                        bCancleTouch = true;
                        tmp.cancleTouch(); //切换为双指后,清理touch_down时的point
                    }
                }
            }
            if (null != mZoomView) {
                mZoomView.dispatchTouchEvent(ev);
                return false;
            } else {
                return super.dispatchTouchEvent(ev);
            }
        } else {
            nCount++;
            return super.dispatchTouchEvent(ev);
        }
    }
}
