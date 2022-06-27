package com.pesdk.uisdk.ui.home.erase;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.fragment.helper.StrokeHandler;
import com.pesdk.uisdk.widget.doodle.DoodleView;
import com.pesdk.uisdk.widget.doodle.bean.Mode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * 消除笔-用户自由绘制模式 操作面板  可设置画笔粗细 画笔颜色
 */
public class ErasePenFragment extends AbsBaseFragment {

    private DoodleView mDoodleView;
    private SeekBar mBar;
    private StrokeHandler mStrokeHandler;

    private IDemark mDemark;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IDemark) {
            mDemark = (IDemark) context;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_demark_layout, container, false);
        View ivGo = $(R.id.ivGo);
        ivGo.setVisibility(View.GONE);
        ivGo.setOnClickListener(v -> mDemark.doMask());
        return mRoot;
    }


    public void setDoodleView(DoodleView doodleView) {
        mDoodleView = doodleView;
        new Handler().postDelayed(initRunnable, 200);
    }

    private Runnable initRunnable = () -> initStroke();


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvTitle = $(R.id.tvBottomTitle);
        tvTitle.setText(R.string.pesdk_erase_pen);
        mBar = $(R.id.sbStrokeWdith);
        mBar.setProgress(80);
        $(R.id.ivCancel).setOnClickListener(v -> mDemark.onCancel());
        $(R.id.ivSure).setOnClickListener(v -> mDemark.onSure());

    }

    private void initStroke() {
        if (null != mBar && null != mDoodleView) {
            mStrokeHandler = new StrokeHandler(mBar, mDoodleView);
            mStrokeHandler.init();
            mDoodleView.setMode(Mode.DOODLE_MODE); //默认涂鸦
            mDoodleView.setPaintColor(Color.RED);
            mDoodleView.setAlpha(0.5f);
            mDoodleView.setEditable(true);
            mDoodleView.setCallBack(new DoodleView.DoodleCallback() {
                @Override
                public void onDrawStart() {
                    mCallback.startTouch();
                }

                @Override
                public void onDrawing() {
                    mCallback.moveTouch();
                }

                @Override
                public void onDrawComplete(boolean hand) {
                    mCallback.endTouch();
                    if (hand) {
                        mDemark.preMask();
                    }
                }

                @Override
                public void onRevertStateChanged(boolean canRevert) {

                }
            });
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private Callback mCallback;

    public static interface Callback {


        void startTouch();

        void moveTouch();

        void endTouch();


    }

    @Override
    public int onBackPressed() {
        if (null != mDemark && isVisible()) {
            mDemark.onSure();
        }
        return super.onBackPressed();
    }

    public interface IDemark {

        void onCancel();

        void onSure();

        /**
         * 准备生成去水印后的图片
         */
        void preMask();

        void doMask();


    }

}
