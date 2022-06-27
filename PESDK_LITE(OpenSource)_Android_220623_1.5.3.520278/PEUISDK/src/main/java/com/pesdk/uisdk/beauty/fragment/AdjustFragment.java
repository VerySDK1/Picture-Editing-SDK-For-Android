package com.pesdk.uisdk.beauty.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.beauty.listener.OnBeautyListener;
import com.pesdk.uisdk.fragment.BaseFragment;
import com.pesdk.uisdk.widget.ExtSeekBar2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * 调节
 */
public class AdjustFragment extends BaseFragment {

    public static AdjustFragment newInstance() {
        return new AdjustFragment();
    }

    //美颜
    private OnBeautyListener mBeautyListener;
    //数据接口
    private OnAdjustListener mAdjustListener;

    private ExtSeekBar2 mSeekBar2;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mBeautyListener = (OnBeautyListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_beauty_adjust, container, false);
        return mRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //确定
        $(R.id.btn_sure).setOnClickListener(v -> onBackPressed());

        mSeekBar2 = $(R.id.seekbar);
        mSeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mAdjustListener != null) {
                    mAdjustListener.onChange(progress * 1.0f / seekBar.getMax());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //恢复
        recover();
    }

    @Override
    public void onCancelClick() {

    }

    @Override
    public void onSureClick() {

    }


    /**
     * 恢复
     */
    private void recover() {
        if (mSeekBar2 != null && mAdjustListener != null) {
            mSeekBar2.setProgress((int) (mAdjustListener.getDefault() * mSeekBar2.getMax()));

            //标题
            if (mAdjustListener != null) {
                ((TextView) $(R.id.tv_title)).setText(mAdjustListener.getTitle());
            }
        }
    }

    /**
     * 回调
     */
    public void setAdjustListener(OnAdjustListener adjustListener) {
        mAdjustListener = adjustListener;
    }

    @Override
    public int onBackPressed() {
        if (mBeautyListener != null) {
            mBeautyListener.onSure();
        }
        return 0;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            recover();
        }
    }

    public interface OnAdjustListener {

        /**
         * 改变 0~1
         */
        void onChange(float value);

        /**
         * 默认 0~1
         */
        float getDefault();

        /**
         * 标题
         */
        String getTitle();

    }

}
