package com.pesdk.uisdk.fragment.mask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.vecore.models.MaskObject;

import androidx.annotation.Nullable;

/**
 * 蒙版控件
 */
public class MaskView extends View {

    //绘制
    private MaskRender mMaskRender;
    //超出时间范围隐藏
    private boolean mIsHide = false;

    public MaskView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置Render
     */
    public void setMaskRender(MaskRender maskRender) {
        mMaskRender = maskRender;
        invalidate();
    }

    /**
     * 返回
     */
    public MaskObject.KeyFrame getKeyFrame() {
        if (mMaskRender != null) {
            return mMaskRender.getKeyframe();
        }
        return null;
    }

    /**
     * 超出时间范围
     */
    public void setHide(boolean hide) {
        mIsHide = hide;
        invalidate();
    }

    /**
     * 设置当前的显示位置
     */
    public void setKeyframe(MaskObject.KeyFrame keyframe) {
        if (mMaskRender != null) {
            mMaskRender.setKeyframe(keyframe);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mMaskRender == null || mIsHide) {
            //不需要绘制
            return;
        }
        mMaskRender.onDraw(canvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mMaskRender == null || mListener == null || mIsHide) {
            return false;
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mListener.onDown();
        } else if (action == MotionEvent.ACTION_MOVE) {
            mListener.onChange(mMaskRender.getKeyframe());
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mListener.onChange(mMaskRender.getKeyframe());
        }
        boolean b = mMaskRender.onTouchEvent(event);
        invalidate();
        return b;
    }




    private OnMaskListener mListener;

    public void setListener(OnMaskListener listener) {
        mListener = listener;
    }

    public interface OnMaskListener {

        /**
         * 点击
         */
        void onDown();

        /**
         * 变化
         */
        void onChange(MaskObject.KeyFrame frame);

    }

}
