package com.pesdk.uisdk.ui.card.fragment;

import android.content.Context;
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
import com.pesdk.uisdk.widget.segment.SegmentView;
import com.vecore.models.PEScene;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * 用户自由绘制模式 操作面板  可设置画笔粗细 画笔颜色
 */
public class CardErasePenFragment extends AbsBaseFragment {

    private SegmentView mSegmentView;
    private SeekBar mBar;
    private StrokeHandler mStrokeHandler;

    public static CardErasePenFragment newInstance() {
        return new CardErasePenFragment();
    }

    public void setDemark(IDemark demark) {
        mDemark = demark;
    }

    private IDemark mDemark;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IDemark) {
            mDemark = (IDemark) context;
        }
    }

    public void setPaintColor(int paintColor) {
        mPaintColor = paintColor;
    }

    private int mPaintColor = PEScene.UNKNOWN_COLOR;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_demark_layout, container, false);
        View ivGo = $(R.id.ivGo);
        ivGo.setVisibility(View.GONE);
        ivGo.setOnClickListener(v -> mDemark.doMask());
        return mRoot;
    }


    public void setSegmentView(SegmentView segmentView) {
        mSegmentView = segmentView;
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
        if (null != mBar && null != mSegmentView) {
            mStrokeHandler = new StrokeHandler(mBar, mSegmentView);
            mStrokeHandler.init();
            //证件照
            mSegmentView.setMaskColor(mPaintColor);
            mSegmentView.setAlpha(1);
            mSegmentView.setIDCard(true);
        }
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

        void doMask();

    }

}
