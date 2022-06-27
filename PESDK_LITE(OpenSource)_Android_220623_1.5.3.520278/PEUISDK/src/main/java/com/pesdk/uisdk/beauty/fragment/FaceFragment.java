package com.pesdk.uisdk.beauty.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.beauty.listener.OnBeautyListener;
import com.pesdk.uisdk.beauty.bean.BeautyFaceInfo;
import com.pesdk.uisdk.fragment.BaseFragment;
import com.pesdk.uisdk.widget.ExtSeekBar2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * 人脸
 */
public class FaceFragment extends BaseFragment {

    public static FaceFragment newInstance() {
        return new FaceFragment();
    }

    //美颜
    private OnBeautyListener mBeautyListener;
    //数据接口
    private OnFaceListener mFaceListener;

    private ExtSeekBar2 mSeekBar2;

    /**
     * 选择人脸
     */
    private ImageView mSwitchFace;

    /**
     * 还原
     */
    private final BeautyFaceInfo mTempFaceInfo = new BeautyFaceInfo(0, 1, null, null);

    /**
     * 当前编辑的人脸
     */
    private BeautyFaceInfo mBeautyFaceInfo;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mBeautyListener = (OnBeautyListener) context;
    }

    @Override
    public void onCancelClick() {

    }

    @Override
    public void onSureClick() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_beauty_face, container, false);
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
                if (fromUser && mBeautyFaceInfo != null && mFaceListener != null) {
                    int current = mFaceListener.getCurrent();
                    if (current == 0) {
                        mBeautyFaceInfo.setValueEyes(progress * 1.0f / seekBar.getMax());
                        mFaceListener.onChange();
                    } else if (current == 1) {
                        mBeautyFaceInfo.setValueFace(progress * 1.0f / seekBar.getMax());
                        mFaceListener.onChange();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //切换人脸
        mSwitchFace = $(R.id.switch_face);
        mSwitchFace.setOnClickListener(v -> switchFace());

        //对比
        $(R.id.contrast).setOnTouchListener((View v, MotionEvent event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mTempFaceInfo.setFaceInfo(mBeautyFaceInfo);
                mBeautyFaceInfo.resetFace();
                recover();
                modifyParameter();
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mBeautyFaceInfo.setFaceInfo(mTempFaceInfo);
                recover();
                modifyParameter();
            }
            return true;
        });

        //还原
        $(R.id.reduction).setOnClickListener(v -> {
            mBeautyFaceInfo.resetFace();
            recover();
            modifyParameter();
        });

        //恢复
        recover();
    }

    /**
     * 换脸
     */
    private void switchFace() {
        if (mFaceListener != null) {
            mFaceListener.onSwitchFace();
        }
    }

    /**
     * 修改参数
     */
    private void modifyParameter() {
        if (mFaceListener != null) {
            mFaceListener.onChange();
        }
    }


    /**
     * 恢复
     */
    public void recover() {
        if (mFaceListener == null) {
            return;
        }
        mBeautyFaceInfo = mFaceListener.getFace();
        if (mSeekBar2 == null || mBeautyFaceInfo == null) {
            return;
        }
        int current = mFaceListener.getCurrent();
        if (current == 0) {
            mSeekBar2.setProgress((int) (mBeautyFaceInfo.getValueEyes() * mSeekBar2.getMax()));
        } else if (current == 1) {
            mSeekBar2.setProgress((int) (mBeautyFaceInfo.getValueFace() * mSeekBar2.getMax()));
        }
        //标题
        ((TextView) $(R.id.tv_title)).setText(mFaceListener.getTitle());

        //人脸
        mSwitchFace.setVisibility(mFaceListener.getFaceNum() <= 1 ? View.GONE : View.VISIBLE);
    }

    /**
     * 回调
     */
    public void setFaceListener(OnFaceListener fiveListener) {
        mFaceListener = fiveListener;
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

    public interface OnFaceListener {

        /**
         * 标题
         */
        String getTitle();

        /**
         * 当前设置什么参数
         */
        int getCurrent();

        /**
         * 获取当前的人脸
         */
        BeautyFaceInfo getFace();

        /**
         * 改变
         */
        void onChange();

        /**
         * 人脸数量
         */
        int getFaceNum();

        /**
         * 切换
         */
        void onSwitchFace();

    }

}
