package com.pesdk.uisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.vecore.models.PEImageObject;
import com.vecore.models.VisualFilterConfig;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 模糊度
 */
public class BlurryFragment extends BaseFragment {
    public static BlurryFragment newInstance() {

        Bundle args = new Bundle();

        BlurryFragment fragment = new BlurryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private boolean recordStep = false; //false 未记录步骤;true 已记录步骤
    private ImageHandlerListener mVideoHandlerListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mVideoHandlerListener = (ImageHandlerListener) context;
    }


    @Override
    public void onCancelClick() {
        if (recordStep) {
            showAlert(new AlertCallback() {
                @Override
                public void cancel() {

                }

                @Override
                public void sure() {
                    mVideoHandlerListener.getParamHandler().onUndo();
                    exit();
                }
            });
        } else {
            exit();
        }
    }

    private void exit() {
        mMenuCallBack.onSure();
    }

    @Override
    public void onSureClick() {
        exit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_blurry, container, false);
        TextView tvTitle = $(R.id.tvBottomTitle);
        tvTitle.setText(R.string.pesdk_blur);
        recordStep = false;
        initView();
        init();
        return mRoot;
    }

    /**
     * 记录Mask调整
     */
    private void recordStep() {
        if (!recordStep) {
            recordStep = true;
            mVideoHandlerListener.getParamHandler().onSaveStep(getString(R.string.pesdk_blur), nType);
        }
    }

    /**
     * 最大
     */
    public static final int MAX_VALUE = 1;

    private enum MODE {
        /**
         * 边缘
         */
        EDGES,
        /**
         * 高斯
         */
        GAUSSIAN,
        /**
         * 径向
         */
        RADIAL
    }

    /**
     * 当前菜单
     */
    private MODE mUiMode = MODE.EDGES;
    /**
     * 模糊
     */
    private LinearLayout mLlRadial;
    private LinearLayout mLlEdges;
    private LinearLayout mLlGaussian;
    /**
     * 高斯
     */
    private SeekBar mSbGaussian;
    /**
     * 边缘
     */
    private SeekBar mSbEdgesInnerRadius;
    private SeekBar mSbEdgesStrength;
    private SeekBar mSbEdgesCenterX;
    private SeekBar mSbEdgesCenterY;
    /**
     * 径向模糊
     */
    private SeekBar mSbStrength;
    private SeekBar mSbCenterX;
    private SeekBar mSbCenterY;
    /**
     * 菜单
     */
    private RadioGroup mRgMenu;

    /**
     * 媒体
     */
    private PEImageObject mMediaObject;
    @IMenu
    private int nType = IMenu.pip;

    public void setData(CollageInfo info) {
        mMediaObject = info.getImageObject();
        nType = IMenu.pip;
    }

    /**
     * 背景模糊
     */
    public void setBGData(PEImageObject info) {
        mMediaObject = info;
        nType = IMenu.canvas;
    }


    private void init() {
        resetData();
    }

    private void initView() {
        mLlRadial = $(R.id.ll_radial);
        mLlEdges = $(R.id.ll_edges);
        mLlGaussian = $(R.id.ll_gaussian);
        //高斯模糊
        mSbGaussian = $(R.id.sb_gaussian);
        mSbGaussian.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float value = progress * 1.0f / seekBar.getMax() * MAX_VALUE;
                    changeGaussianData(mMediaObject, value);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //边缘模糊
        mSbEdgesInnerRadius = $(R.id.sb_edges_innerRadius);
        mSbEdgesStrength = $(R.id.sb_edges_strength);
        mSbEdgesCenterX = $(R.id.sb_edges_center_x);
        mSbEdgesCenterY = $(R.id.sb_edges_center_y);
        mSbEdgesInnerRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float value = progress * 1.0f / seekBar.getMax() * MAX_VALUE;
                    changeEdgeData(mMediaObject, value, -1, -1, -1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSbEdgesStrength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float value = progress * 1.0f / seekBar.getMax() * MAX_VALUE;
                    changeEdgeData(mMediaObject, -1, value, -1, -1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSbEdgesCenterX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float value = progress * 1.0f / seekBar.getMax() * MAX_VALUE;
                    changeEdgeData(mMediaObject, -1, -1, value, -1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSbEdgesCenterY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float value = progress * 1.0f / seekBar.getMax() * MAX_VALUE;
                    changeEdgeData(mMediaObject, -1, -1, -1, value);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //径向
        mSbStrength = $(R.id.sb_radial_strength);
        mSbCenterX = $(R.id.sb_redial_center_x);
        mSbCenterY = $(R.id.sb_redial_center_y);
        mSbStrength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float value = progress * 1.0f / seekBar.getMax() * MAX_VALUE;
                    changeRadialData(mMediaObject, value, -1, -1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSbCenterX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float value = progress * 1.0f / seekBar.getMax() * MAX_VALUE;
                    changeRadialData(mMediaObject, -1, value, -1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSbCenterY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float value = progress * 1.0f / seekBar.getMax() * MAX_VALUE;
                    changeRadialData(mMediaObject, -1, -1, value);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //菜单
        mRgMenu = $(R.id.rgMenu);
        mRgMenu.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.btn_edges) {
                mUiMode = MODE.EDGES;
                float innerRadius = mSbEdgesInnerRadius.getProgress() * 1.0f / mSbEdgesInnerRadius.getMax();
                float strength = mSbEdgesStrength.getProgress() * 1.0f / mSbEdgesStrength.getMax();
                float centerX = mSbEdgesCenterX.getProgress() * 1.0f / mSbEdgesCenterX.getMax();
                float centerY = mSbEdgesCenterY.getProgress() * 1.0f / mSbEdgesCenterY.getMax();
                changeEdgeData(mMediaObject, innerRadius, strength, centerX, centerY);
            } else if (checkedId == R.id.btn_gaussian) {
                mUiMode = MODE.GAUSSIAN;
                changeGaussianData(mMediaObject, mSbGaussian.getProgress() * 1.0f / mSbGaussian.getMax());
            } else if (checkedId == R.id.btn_radial) {
                mUiMode = MODE.RADIAL;
                float strength = mSbStrength.getProgress() * 1.0f / mSbStrength.getMax();
                float centerX = mSbCenterX.getProgress() * 1.0f / mSbCenterX.getMax();
                float centerY = mSbCenterY.getProgress() * 1.0f / mSbCenterY.getMax();
                changeRadialData(mMediaObject, strength, centerX, centerY);
            }
            freshProgress(false);
        });
    }

    /**
     * 恢复数据
     */
    private void resetData() {
        //显示
        mRgMenu.post(() -> {
            VisualFilterConfig.BlurConfig blurConfig = mMediaObject.getBlurConfig();
            if (blurConfig != null) {
                if (blurConfig.getBlurType() == VisualFilterConfig.BlurConfig.BlurType.RADIAL) {
                    mUiMode = MODE.RADIAL;
                    mRgMenu.check(R.id.btn_radial);
                } else if (blurConfig.getBlurType() == VisualFilterConfig.BlurConfig.BlurType.EDGES) {
                    mUiMode = MODE.EDGES;
                    mRgMenu.check(R.id.btn_edges);
                } else {
                    mUiMode = MODE.GAUSSIAN;
                    mRgMenu.check(R.id.btn_gaussian);
                }
            } else {
                mUiMode = MODE.EDGES;
                mRgMenu.check(R.id.btn_edges);
            }
        });
        freshProgress(true);
    }

    /**
     * 清理设置
     */
    private void recoveryData() {
        if (mMediaObject != null) {
            mMediaObject.setBlurConfig(null);
        }
        freshProgress(false);
        if (mUiMode == MODE.GAUSSIAN) {
            mSbGaussian.setProgress(0);
        } else if (mUiMode == MODE.RADIAL) {
            mSbStrength.setProgress(0);
            mSbCenterX.setProgress(50);
            mSbCenterY.setProgress(50);
        } else if (mUiMode == MODE.EDGES) {
            mSbEdgesStrength.setProgress(0);
            mSbEdgesInnerRadius.setProgress(50);
            mSbEdgesCenterX.setProgress(50);
            mSbEdgesCenterY.setProgress(50);
        }
    }

    /**
     * 刷新进度
     */
    private void freshProgress(boolean recovery) {
        if (mLlEdges != null) {
            if (mMediaObject != null && recovery) {
                VisualFilterConfig.BlurConfig blurConfig = mMediaObject.getBlurConfig();
                if (blurConfig != null) {
                    VisualFilterConfig.BlurConfig.BlurType blurType = blurConfig.getBlurType();
                    if (blurType == VisualFilterConfig.BlurConfig.BlurType.GAUSSIAN) {
                        mSbGaussian.setProgress((int) (blurConfig.getIntensity() / MAX_VALUE * mSbGaussian.getMax()));
                    } else if (blurType == VisualFilterConfig.BlurConfig.BlurType.RADIAL) {
                        mSbStrength.setProgress((int) (blurConfig.getIntensity() / MAX_VALUE * mSbStrength.getMax()));
                        mSbCenterX.setProgress((int) (blurConfig.getCenterPointF().x / MAX_VALUE * mSbCenterX.getMax()));
                        mSbCenterY.setProgress((int) (blurConfig.getCenterPointF().y / MAX_VALUE * mSbCenterY.getMax()));
                    } else {
                        mSbEdgesStrength.setProgress((int) (blurConfig.getIntensity() / MAX_VALUE * mSbStrength.getMax()));
                        mSbEdgesInnerRadius.setProgress((int) (blurConfig.getInnerRadius() / MAX_VALUE * mSbEdgesInnerRadius.getMax()));
                        mSbEdgesCenterX.setProgress((int) (blurConfig.getCenterPointF().x / MAX_VALUE * mSbCenterX.getMax()));
                        mSbEdgesCenterY.setProgress((int) (blurConfig.getCenterPointF().y / MAX_VALUE * mSbCenterY.getMax()));
                    }
                } else {
                    /**
                     * 撤销后恢复数据*/
                    mSbGaussian.setProgress(0);

                    mSbStrength.setProgress(0);
                    mSbCenterX.setProgress(50);
                    mSbCenterY.setProgress(50);

                    mSbEdgesStrength.setProgress(0);
                    mSbEdgesInnerRadius.setProgress(50);
                    mSbEdgesCenterX.setProgress(50);
                    mSbEdgesCenterY.setProgress(50);

                }
            }
            if (mUiMode == MODE.EDGES) {
                mLlEdges.setVisibility(View.VISIBLE);
                mLlGaussian.setVisibility(View.GONE);
                mLlRadial.setVisibility(View.GONE);
            } else if (mUiMode == MODE.GAUSSIAN) {
                mLlEdges.setVisibility(View.GONE);
                mLlGaussian.setVisibility(View.VISIBLE);
                mLlRadial.setVisibility(View.GONE);
            } else if (mUiMode == MODE.RADIAL) {
                mLlEdges.setVisibility(View.GONE);
                mLlGaussian.setVisibility(View.GONE);
                mLlRadial.setVisibility(View.VISIBLE);
            } else {
                mLlEdges.setVisibility(View.VISIBLE);
                mLlGaussian.setVisibility(View.GONE);
                mLlRadial.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 高斯
     */
    private void changeGaussianData(PEImageObject mediaObject, float value) {
        if (mediaObject == null) {
            return;
        }
        recordStep();

        VisualFilterConfig.BlurConfig blurConfig = null;
        if (value <= 0) {
            mediaObject.setBlurConfig(null);
        } else {
            blurConfig = mediaObject.getBlurConfig();
            if (blurConfig == null || blurConfig.getBlurType() != VisualFilterConfig.BlurConfig.BlurType.GAUSSIAN) {
                blurConfig = new VisualFilterConfig.BlurConfig(VisualFilterConfig.BlurConfig.BlurType.GAUSSIAN);
            }
            blurConfig.setIntensity(value);
            mediaObject.setBlurConfig(blurConfig);
        }
    }

    /**
     * 径向模糊
     */
    private void changeRadialData(PEImageObject mediaObject, float strength, float centerX, float centerY) {
        if (mediaObject == null) {
            return;
        }
        recordStep();

        boolean delete = strength < 0 && centerX < 0 && centerY < 0;
        VisualFilterConfig.BlurConfig blurConfig = null;
        if (delete) {
            mediaObject.setBlurConfig(null);
        } else {
            blurConfig = mediaObject.getBlurConfig();
            if (blurConfig == null || blurConfig.getBlurType() != VisualFilterConfig.BlurConfig.BlurType.RADIAL) {
                blurConfig = new VisualFilterConfig.BlurConfig(VisualFilterConfig.BlurConfig.BlurType.RADIAL);
            }
            if (strength >= 0) {
                blurConfig.setIntensity(strength);
            }
            if (centerX >= 0) {
                blurConfig.setCenterPointF(centerX, blurConfig.getCenterPointF().y);
            }
            if (centerY >= 0) {
                blurConfig.setCenterPointF(blurConfig.getCenterPointF().x, centerY);
            }
            mediaObject.setBlurConfig(blurConfig);
        }
    }

    /**
     * 边缘模糊
     */
    private void changeEdgeData(PEImageObject mediaObject, float innerRadius, float strength, float centerX, float centerY) {
        if (mediaObject == null) {
            return;
        }
        recordStep();

        boolean delete = strength < 0 && innerRadius < 0 && centerX < 0 && centerY < 0;
        VisualFilterConfig.BlurConfig blurConfig = null;
        if (delete) {
            mediaObject.setBlurConfig(null);
        } else {
            blurConfig = mediaObject.getBlurConfig();
            if (blurConfig == null || blurConfig.getBlurType() != VisualFilterConfig.BlurConfig.BlurType.EDGES) {
                blurConfig = new VisualFilterConfig.BlurConfig(VisualFilterConfig.BlurConfig.BlurType.EDGES);
            }
            if (strength >= 0) {
                blurConfig.setIntensity(strength);
            }
            if (innerRadius >= 0) {
                blurConfig.setInnerRadius(innerRadius);
            }
            if (centerX >= 0) {
                blurConfig.setCenterPointF(centerX, blurConfig.getCenterPointF().y);
            }
            if (centerY >= 0) {
                blurConfig.setCenterPointF(blurConfig.getCenterPointF().x, centerY);
            }
            mediaObject.setBlurConfig(blurConfig);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            resetData();
        }
    }

    /**
     * 返回
     */
    @Override
    public int onBackPressed() {
        return super.onBackPressed();
    }
}
