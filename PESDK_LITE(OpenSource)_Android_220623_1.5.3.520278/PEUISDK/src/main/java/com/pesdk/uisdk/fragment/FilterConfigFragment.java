package com.pesdk.uisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.bean.model.IMediaParamImp;
import com.pesdk.uisdk.bean.model.UndoInfo;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.util.helper.FilterUtil;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.pesdk.uisdk.widget.ExtFilterSeekBar;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.vecore.models.PEImageObject;
import com.vecore.models.VisualFilterConfig;

import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 滤镜调色
 */
public class FilterConfigFragment extends BaseFragment implements View.OnClickListener {
    private float mBrightness = Float.NaN, mContrast = Float.NaN,
            mSaturation = Float.NaN, mSharpen = Float.NaN,
            mWhite = Float.NaN, mVignette = Float.NaN;

    private float mTint = Float.NaN;
    private float mHighlight = Float.NaN;
    private float mLightSensation = Float.NaN;
    private float mFade = Float.NaN;
    private float mGraininess = Float.NaN;
    private float mShadow = Float.NaN;
    private int vignettId = IMediaParamImp.NO_VIGNETTEDID;
    private float mTemperature = Float.NaN;


    public static FilterConfigFragment newInstance() {
        return new FilterConfigFragment();
    }

    private ExtFilterSeekBar mRange;
    private RadioGroup mRGGuangXiao, mRGColor, mRGAdjust;
    private RadioButton mBtnBrightness, mBtnContrast,
            mBtnSaturation, mBtnSharpen, mBtnTemperature,
            mBtnVignette, mBtnTone, mBtnHighlight, mBtnShadow, mBtnFade, mBtnLightSensation, mBtnGraininess;

    private final int brightness = 0, contrast = 1, saturation = 2,
            sharpen = 3, vignette = 5, temperature = 6, tone = 7,
            highlight = 8, shadow = 9, lightSensation = 10, fade = 11, graininess = 12, reset = 100;
    private int mStatus = reset;//当前选中的项目
    //数值显示、还原
    private TextView mTvProgress, mTvReset;
    //双击恢复
    private static int TIMEOUT = 400;//双击间四百毫秒延时
    private long mLastTime = 0;
    private boolean mHasChanged = false;//是否改变(与每个参数的默认值相比)
    private ImageHandlerListener mVideoHandlerListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mVideoHandlerListener = (ImageHandlerListener) context;
    }

    private IMediaParamImp mMediaParamImp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_fiter_config_layout, container, false);
        hasChange = false;
        mIsAdd = false;
        if (mFilterInfo == null) {
            mMediaParamImp = new IMediaParamImp();
            mFilterInfo = new FilterInfo(mMediaParamImp);
            if (mVideoHandlerListener.getParamHandler().getEditMode() == IMenu.pip) {//图层
                ((ImageOb) mPIPObject.getTag()).setAdjust(mFilterInfo);
            } else {//背景-调色
                mIsAdd = true;
            }
        } else {
            mMediaParamImp = mFilterInfo.getMediaParamImp();
        }
        initView();
        return mRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        $(R.id.adjust_guangxiao).setOnClickListener(v -> onGX());
        $(R.id.adjust_color).setOnClickListener(v -> onColor());
        $(R.id.adjust_adjust).setOnClickListener(v -> onAdjust());
    }

    private void onGX() {
        $(R.id.vgGuangxiao).setVisibility(View.VISIBLE);
        $(R.id.vgColor).setVisibility(View.GONE);
        $(R.id.vgAdjust).setVisibility(View.GONE);
    }

    private void onColor() {
        $(R.id.vgGuangxiao).setVisibility(View.GONE);
        $(R.id.vgColor).setVisibility(View.VISIBLE);
        $(R.id.vgAdjust).setVisibility(View.GONE);
    }

    private void onAdjust() {
        $(R.id.vgGuangxiao).setVisibility(View.GONE);
        $(R.id.vgColor).setVisibility(View.GONE);
        $(R.id.vgAdjust).setVisibility(View.VISIBLE);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (null != mMediaParamImp) {
            mBrightness = mMediaParamImp.getBrightness();
            mContrast = mMediaParamImp.getContrast();
            mSaturation = mMediaParamImp.getSaturation();
            mSharpen = mMediaParamImp.getSharpen();

            mVignette = mMediaParamImp.getVignette();
            mWhite = mMediaParamImp.getWhite();
            vignettId = mMediaParamImp.getVignetteId();

            mGraininess = mMediaParamImp.getGraininess();
            mLightSensation = mMediaParamImp.getLightSensation();
            mFade = mMediaParamImp.getFade();

            mHasChanged = !Float.isNaN(mBrightness) || !Float.isNaN(mContrast) || !Float.isNaN(mSaturation) || !Float.isNaN(mSharpen) || !Float.isNaN(mWhite) || !Float.isNaN(mVignette);
        }
        clearCheck();
        mStatus = reset;
        mRoot.postDelayed(() -> onClickImp(R.id.btn_brightness), 50);
    }

    @Override
    public void onCancelClick() {
        if (hasChange) {
            showAlert(new AlertCallback() {
                @Override
                public void cancel() {

                }

                @Override
                public void sure() {
                    if (mVideoHandlerListener.getParamHandler().getEditMode() == IMenu.pip) {
//                        mVideoHandlerListener.getParamHandler().onSaveStep(getString(R.string.pesdk_prompt_adjust_toning), IMenu.layer);
                    } else {
                        if (null != mFilterInfo) {  //删除新增|放弃编辑
                            mVideoHandlerListener.getParamHandler().deleteFilterInfo(mFilterInfo);
                            UndoInfo info = mVideoHandlerListener.getParamHandler().onDeleteStep();
                            if (null != info) {
                                mVideoHandlerListener.getParamHandler().setFilterList(info.getList());
                            }
                        }
                        mVideoHandlerListener.onChangeEffectFilter();
                    }
                    mMenuCallBack.onCancel();
                }
            });
        } else {
            mMenuCallBack.onCancel();
        }
    }

    @Override
    public void onSureClick() {
        mMenuCallBack.onSure();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnReset) {
            onResetClickImp();
        } else {
            onClickImp(id);
        }
    }

    private void onResetClickImp() {
        if (!mHasChanged) {
            return;
        }
        onResetDialog();
    }

    private void onClickImp(View view) {
        onClickImp(view.getId());
    }

    private void onClickImp(int viewId) {
        if (viewId == R.id.btn_brightness) {
            //亮度
            if (mStatus == brightness) {
                if (Float.isNaN(mBrightness) || mBrightness == 0) {
                    return;
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mBrightness = Float.NaN;
                    onConfigChange();
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_brightness));
                } else {
                    mLastTime = System.currentTimeMillis();
                    return;
                }
            }
            mLastTime = System.currentTimeMillis();
            mStatus = brightness;
            mRange.setDefaultValue(50);
            float fv = mRange.getMax() * (mBrightness - (-1.0f)) / 2.0f;
            if (Float.isNaN(fv)) {
                fv = mRange.getMax() / 2.0f;
                mRange.setChangedByHand(false);
            } else {
                mRange.setChangedByHand(true);
            }
            mRange.setProgress((int) fv);
        } else if (viewId == R.id.btn_contrast) {
            //对比度
            if (mStatus == contrast) {
                if (Float.isNaN(mContrast) || mContrast == 1.0) {
                    return;
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mContrast = Float.NaN;
                    onConfigChange();
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_contrast));
                } else {
                    mLastTime = System.currentTimeMillis();
                    return;
                }
            }
            mLastTime = System.currentTimeMillis();

            mStatus = contrast;
            mRange.setDefaultValue(25);
            if (Float.isNaN(mContrast)) {
                //    VisualFilterConfig中对比度默认为1.0f(区间：0~4.0f)
                mRange.setChangedByHand(false);
                mRange.setProgress((int) (mRange.getMax() * 1.0f / 4.0f));
            } else {
                mRange.setChangedByHand(true);
                mRange.setProgress((int) (mRange.getMax() * mContrast / 4.0f));
            }
        } else if (viewId == R.id.btn_saturation) {
            //饱和度
            if (mStatus == saturation) {
                if (Float.isNaN(mSaturation) || mSaturation == 1.0) {
                    return;
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mSaturation = Float.NaN;
                    onConfigChange();
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_saturation));
                } else {
                    mLastTime = System.currentTimeMillis();
                    return;
                }
            }
            mLastTime = System.currentTimeMillis();

            mStatus = saturation;
            mRange.setDefaultValue(50);
            if (Float.isNaN(mSaturation)) {
                //    VisualFilterConfig中饱和度默认为1.0f(区间：0~2.0f)
                mRange.setChangedByHand(false);
                mRange.setProgress((int) (mRange.getMax() * 1 / 2.0f));
            } else {
                mRange.setChangedByHand(true);
                mRange.setProgress((int) (mRange.getMax() * mSaturation / 2.0f));
            }
        } else if (viewId == R.id.btn_sharpen) {
            //锐化
            if (mStatus == sharpen) {
                if (Float.isNaN(mSharpen) || mSharpen == 0) {
                    return;
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mSharpen = Float.NaN;
                    onConfigChange();
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_sharpness));
                } else {
                    mLastTime = System.currentTimeMillis();
                    return;
                }
            }
            mLastTime = System.currentTimeMillis();

            mStatus = sharpen;
            mRange.setDefaultValue(0);
            if (Float.isNaN(mSharpen)) {
                mRange.setChangedByHand(false);
            } else {
                mRange.setChangedByHand(true);
            }
            mRange.setProgress((int) (mRange.getMax() * mSharpen));
        } else if (viewId == R.id.btn_temperature) {  //色温
            if (mStatus == temperature) {
                if (Float.isNaN(mTemperature) || mTemperature == 0) {
                    return;
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mTemperature = Float.NaN;
                    onConfigChange();
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_temperature));
                } else {
                    mLastTime = System.currentTimeMillis();
                    return;
                }
            }
            mLastTime = System.currentTimeMillis();
            mStatus = temperature;
            mRange.setDefaultValue(50);
            float fv = mRange.getMax() * (mTemperature - (-1.0f)) / 2.0f;
            if (Float.isNaN(fv)) {
                fv = mRange.getMax() / 2.0f;
                mRange.setChangedByHand(false);
            } else {
                mRange.setChangedByHand(true);
            }
            mRange.setProgress((int) fv);
        } else if (viewId == R.id.btn_vignette) { //暗角
            if (mStatus == vignette) {
                if (Float.isNaN(mVignette) || mVignette == 0) {
                    return;
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    vignettId = IMediaParamImp.NO_VIGNETTEDID;
                    mVignette = Float.NaN;
                    onConfigChange();
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_vignette));
                } else {
                    mLastTime = System.currentTimeMillis();
                    return;
                }
            }
            mLastTime = System.currentTimeMillis();

            mStatus = vignette;
            mRange.setDefaultValue(0);
            if (IMediaParamImp.NO_VIGNETTEDID != vignettId) {
                mRange.setChangedByHand(true);
            } else {
                mRange.setChangedByHand(false);
            }
            mRange.setProgress((int) (mRange.getMax() * mVignette));
        } else if (viewId == R.id.btn_shadow) { //阴影
            if (mStatus == shadow) {
                if (Float.isNaN(mShadow) || mShadow == 0) {
                    return;
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mShadow = Float.NaN;
                    onConfigChange();
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_shadow));
                } else {
                    mLastTime = System.currentTimeMillis();
                    return;
                }
            }
            mLastTime = System.currentTimeMillis();
            mStatus = shadow;
            mRange.setDefaultValue(0);
            mRange.setChangedByHand(!Float.isNaN(mShadow));
            mRange.setProgress((int) (mRange.getMax() * mShadow));
        } else if (viewId == R.id.btn_hightlight) {  //高光
            if (mStatus == highlight) {
                if (Float.isNaN(mHighlight) || mHighlight == 0) {
                    return;
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mHighlight = Float.NaN;
                    onConfigChange();
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_highlight));
                } else {
                    mLastTime = System.currentTimeMillis();
                    return;
                }
            }
            mLastTime = System.currentTimeMillis();
            mStatus = highlight;
            mRange.setDefaultValue(0);
            mRange.setChangedByHand(!Float.isNaN(mHighlight));
            mRange.setProgress((int) (mRange.getMax() * mHighlight));
        } else if (viewId == R.id.btn_guanggan) {  //光感
            if (mStatus == lightSensation) {
                if (Float.isNaN(mLightSensation) || mLightSensation == 0) {
                    return;
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mLightSensation = Float.NaN;
                    onConfigChange();
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_guanggan));
                } else {
                    mLastTime = System.currentTimeMillis();
                    return;
                }
            }
            mLastTime = System.currentTimeMillis();
            mStatus = lightSensation;
            mRange.setDefaultValue(50);
            float fv = mRange.getMax() * (mLightSensation - (-1.0f)) / 2.0f;
            if (Float.isNaN(fv)) {
                fv = mRange.getMax() / 2.0f;
                mRange.setChangedByHand(false);
            } else {
                mRange.setChangedByHand(true);
            }
            mRange.setProgress((int) fv);
        } else if (viewId == R.id.btn_hese) {  //褐色
            if (mStatus == fade) {
                if (Float.isNaN(mFade) || mFade == 0) {
                    return;
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mFade = Float.NaN;
                    onConfigChange();
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_hese));
                } else {
                    mLastTime = System.currentTimeMillis();
                    return;
                }
            }
            mLastTime = System.currentTimeMillis();
            mStatus = fade;
            mRange.setDefaultValue(0);
            mRange.setChangedByHand(!Float.isNaN(mFade));
            mRange.setProgress((int) (mRange.getMax() * mFade));
        } else if (viewId == R.id.btn_keli) {
            if (mStatus == graininess) {
                if (Float.isNaN(mGraininess) || mGraininess == 0) {
                    return;
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mGraininess = Float.NaN;
                    onConfigChange();
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_keli));
                } else {
                    mLastTime = System.currentTimeMillis();
                    return;
                }
            }
            mLastTime = System.currentTimeMillis();

            mStatus = graininess;
            mRange.setDefaultValue(0);
            mRange.setChangedByHand(!Float.isNaN(mGraininess));
            mRange.setProgress((int) (mRange.getMax() * mGraininess));
        } else if (viewId == R.id.btn_tone) {  //色调
            if (mStatus == tone) {
                if (Float.isNaN(mTint) || mTint == 0) {
                    return;
                }
                if (System.currentTimeMillis() - mLastTime < TIMEOUT) {
                    //恢复
                    mTint = Float.NaN;
                    onConfigChange();
                    onToast(getString(R.string.pesdk_toning_reset) + getString(R.string.pesdk_filter_tone));
                } else {
                    mLastTime = System.currentTimeMillis();
                    return;
                }
            }
            mLastTime = System.currentTimeMillis();

            mStatus = tone;
            mRange.setDefaultValue(50);
            float fv = mRange.getMax() * (mTint - (-1.0f)) / 2.0f;
            if (Float.isNaN(fv)) {
                fv = mRange.getMax() / 2.0f;
                mRange.setChangedByHand(false);
            } else {
                mRange.setChangedByHand(true);
            }
            mRange.setProgress((int) fv);
        }
    }


    private boolean hasChange = false;
    //是否新增
    private boolean mIsAdd = false;
    //滤镜
    private FilterInfo mFilterInfo;
    private PEImageObject mPIPObject;

    /**
     * 滤镜调节
     *
     * @param filterInfo 传入调色(不是滤镜)对应的filterInfo,
     */
    public void setFilterInfo(FilterInfo filterInfo) {
        mFilterInfo = filterInfo;
        mPIPObject = null;
    }

    /**
     * 画中画
     */
    public void setPIP(PEImageObject pip) {
        mFilterInfo = null;
        mPIPObject = pip;
        if (pip == null) {
            return;
        }
        initPIP();
    }

    /**
     * 画中画
     */
    private void initPIP() {
        ImageOb tmp = PEHelper.initImageOb(mPIPObject);
        mFilterInfo = tmp.getAdjust();
    }

    /**
     * 新的调色参数
     */
    private void onConfigChange() {
        if (mVideoHandlerListener.getParamHandler().getEditMode() == IMenu.pip) { //图层-调色
            //画中画模式
            if (mPIPObject == null) {
                return;
            }
            onTempSave();
            changeFilter(mPIPObject);
        } else {
            onTempSave();
            hasChange = true;
            mHasChanged = true;
            mVideoHandlerListener.onChangeEffectFilter();
        }
    }


    private boolean bSaved = false;
    @IMenu
    private final int type = IMenu.adjust;

    /**
     * 保存当前编辑的参数
     */
    private void onTempSave() {
        Log.e(TAG, "onTempSave: " + mIsAdd);
        if (mIsAdd) { //新增调色
            mIsAdd = false;
            mVideoHandlerListener.getParamHandler().addFilterInfo(mFilterInfo, type);
        }
        IMediaParamImp tmp = mMediaParamImp;
        if (null != tmp) {
            tmp.setBrightness(mBrightness);
            tmp.setContrast(mContrast);
            tmp.setSaturation(mSaturation);
            tmp.setSharpen(mSharpen);
            tmp.setWhite(mWhite);
            tmp.setVignette(mVignette);
            tmp.setVignetteId(vignettId);

            tmp.setTemperature(mTemperature);
            tmp.setTintValue(mTint);
            tmp.setHighlightsValue(mHighlight);
            tmp.setShadowsValue(mShadow);

            tmp.setGraininess(mGraininess);
            tmp.setLightSensation(mLightSensation);
            tmp.setFade(mFade);
        }
        Log.e(TAG, "onTempSave: "+tmp );
    }

    private void onResetImp() {
        //全部重置
        mBrightness = Float.NaN;
        mContrast = Float.NaN;
        mSaturation = Float.NaN;
        mSharpen = Float.NaN;
        mVignette = Float.NaN;
        mWhite = Float.NaN;
        mTemperature = Float.NaN;
        mTint = Float.NaN;
        mHighlight = Float.NaN;
        mShadow = Float.NaN;
        mLightSensation = Float.NaN;
        mFade = Float.NaN;
        mGraininess = Float.NaN;
        vignettId = IMediaParamImp.NO_VIGNETTEDID;
        initValue();
        onConfigChange();
    }

    //恢复
    private void initValue() {
        if (mStatus == brightness) {
            mStatus = reset;
            onClickImp(mBtnBrightness);
            mBtnBrightness.setChecked(true);
        } else if (mStatus == contrast) {
            mStatus = reset;
            onClickImp(mBtnContrast);
            mBtnContrast.setChecked(true);
        } else if (mStatus == saturation) {
            mStatus = reset;
            onClickImp(mBtnSaturation);
            mBtnSaturation.setChecked(true);
        } else if (mStatus == sharpen) {
            mStatus = reset;
            onClickImp(mBtnSharpen);
            mBtnSharpen.setChecked(true);
        } else if (mStatus == temperature) {
            mStatus = reset;
            onClickImp(mBtnTemperature);
            mBtnTemperature.setChecked(true);
        } else if (mStatus == tone) {
            mStatus = reset;
            onClickImp(mBtnTone);
            mBtnTone.setChecked(true);
        } else if (mStatus == vignette) {
            mStatus = reset;
            onClickImp(mBtnVignette);
            mBtnVignette.setChecked(true);
        } else if (mStatus == highlight) {
            mStatus = reset;
            onClickImp(mBtnHighlight);
            mBtnHighlight.setChecked(true);
        } else if (mStatus == shadow) {
            mStatus = reset;
            onClickImp(mBtnShadow);
            mBtnShadow.setChecked(true);
        } else if (mStatus == graininess) {
            mStatus = reset;
            onClick(mBtnGraininess);
            mBtnGraininess.setChecked(true);
        } else if (mStatus == lightSensation) {
            mStatus = reset;
            onClick(mBtnLightSensation);
            mBtnLightSensation.setChecked(true);
        } else if (mStatus == fade) {
            mStatus = reset;
            onClick(mBtnFade);
            mBtnFade.setChecked(true);
        }
    }

    private void onResetDialog() {
        String strMessage = getString(R.string.pesdk_toning_reset_msg);
        SysAlertDialog.showAlertDialog(getContext(), "", strMessage,
                getString(R.string.pesdk_cancel),
                (dialog, which) -> initValue(), getString(R.string.pesdk_sure),
                (dialog, which) -> {
                    onResetImp();
                    clearCheck();

                    mHasChanged = false;
                });
    }

    private void clearCheck() {
        mRGGuangXiao.clearCheck();
        mRGColor.clearCheck();
        mRGAdjust.clearCheck();
    }


    private void initView() {
        mTvProgress = $(R.id.tv_progress);
        mTvReset = $(R.id.btnReset);
        mRange = $(R.id.sbar_range);
        mRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int textid = R.string.pesdk_filter_brightness;
            float value = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    value = (1.0f * progress / seekBar.getMax());
                    textid = setFeaturesValue(value);
                    onConfigChange();
                }
                mTvProgress.setText(getText(textid) + " " + decimalFormat.format(value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mStatus == reset || mStatus == brightness) { //首次拖动,啥都未选中时
                    mStatus = brightness;
                    textid = R.string.pesdk_filter_brightness;
                    mBtnBrightness.setChecked(true);
                }
                if (mPIPObject != null) { //画中画-调色
                    if (!mIsAdd && !bSaved) { //调整调色
                        bSaved = true;
                        mVideoHandlerListener.getParamHandler().onSaveStep(getString(R.string.pesdk_prompt_adjust_toning), IMenu.pip);
                    }
                } else {//背景-调色
                    if (!mIsAdd && !bSaved) { //调整调色
                        bSaved = true;
                        mVideoHandlerListener.getParamHandler().editFilterInfo(mFilterInfo, type); //编辑调色
                    }
                }
                mRange.setChangedByHand(true);
                textid = setFeaturesValue(seekBar.getProgress() * 1.0f / seekBar.getMax());
                mTvProgress.setVisibility(View.VISIBLE);
                mTvProgress.setText(getText(textid) + " " + decimalFormat.format(value));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                value = (1.0f * seekBar.getProgress() / seekBar.getMax());
                textid = setFeaturesValue(value);
                onTempSave();
                mTvProgress.setVisibility(View.INVISIBLE);
            }
        });


        mRGGuangXiao = $(R.id.rgGuangxiao);
        mRGColor = $(R.id.rgColor);
        mRGAdjust = $(R.id.rgAdjust);

        //亮度......
        mBtnBrightness = $(R.id.btn_brightness);
        mBtnContrast = $(R.id.btn_contrast);
        mBtnSaturation = $(R.id.btn_saturation);
        mBtnSharpen = $(R.id.btn_sharpen);
        mBtnTemperature = $(R.id.btn_temperature);

        mBtnTone = $(R.id.btn_tone);
        mBtnVignette = $(R.id.btn_vignette);
        mBtnHighlight = $(R.id.btn_hightlight);
        mBtnShadow = $(R.id.btn_shadow);


        mBtnFade = $(R.id.btn_hese);
        mBtnLightSensation = $(R.id.btn_guanggan);
        mBtnGraininess = $(R.id.btn_keli);

        $(R.id.btnDiff).setOnTouchListener((v, event) -> {
            if (!mHasChanged) {
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                onDiffBegin();
                mTvProgress.setVisibility(View.VISIBLE);
                mTvProgress.setText(getText(R.string.pesdk_toning_diff_msg));
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                onDiffEnd();
                mTvProgress.setVisibility(View.INVISIBLE);
            }
            return true;
        });

        mBtnBrightness.setOnClickListener(this);
        mBtnContrast.setOnClickListener(this);
        mBtnShadow.setOnClickListener(this);
        mBtnSaturation.setOnClickListener(this);
        mBtnSharpen.setOnClickListener(this);
        mBtnTemperature.setOnClickListener(this);
        mBtnTone.setOnClickListener(this);
        mBtnVignette.setOnClickListener(this);
        mBtnHighlight.setOnClickListener(this);
        mBtnShadow.setOnClickListener(this);


        mBtnGraininess.setOnClickListener(this);
        mBtnFade.setOnClickListener(this);
        mBtnLightSensation.setOnClickListener(this);

        mTvReset.setOnClickListener(this);

    }

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.

    //返回当前选中的项目 返回当前的名字
    private int setFeaturesValue(float value) {
        if (mStatus == brightness) {
            mBrightness = 2 * value - 1.0f;
            return R.string.pesdk_filter_brightness;
        } else if (mStatus == contrast) {
            mContrast = value * 4;
            return R.string.pesdk_filter_contrast;
        } else if (mStatus == saturation) {
            mSaturation = value * 2;
            return R.string.pesdk_filter_saturation;
        } else if (mStatus == sharpen) {
            mSharpen = value;
            return R.string.pesdk_filter_sharpness;
        } else if (mStatus == temperature) {
            mTemperature = value * 2 - 1.0f;
            return R.string.pesdk_filter_temperature;
        } else if (mStatus == tone) {
            mTint = value * 2 - 1.0f;
            return R.string.pesdk_filter_tone;
        } else if (mStatus == vignette) {
            fixVignette(value);
            return R.string.pesdk_filter_vignette;
        } else if (mStatus == highlight) {
            mHighlight = value;
            return R.string.pesdk_filter_highlight;
        } else if (mStatus == shadow) {
            mShadow = value;
            return R.string.pesdk_filter_shadow;
        } else if (mStatus == graininess) {
            mGraininess = value;
            return R.string.pesdk_filter_keli;
        } else if (mStatus == lightSensation) {
            mLightSensation = value * 2 - 1.0f;
            return R.string.pesdk_filter_guanggan;
        } else if (mStatus == fade) {
            mFade = value;
            return R.string.pesdk_filter_hese;
        } else {
            return R.string.pesdk_filter_unknow;
        }
    }

    //暗角
    private void fixVignette(float value) {
        mVignette = value;
        if (value > 0 && value <= 1) {
            vignettId = VisualFilterConfig.FILTER_ID_VIGNETTE;
        } else {
            vignettId = IMediaParamImp.NO_VIGNETTEDID;
        }
    }

    private IMediaParamImp mbkDiff = null;


    /**
     * 设置滤镜
     */
    private void changeFilter(PEImageObject object) {
        FilterUtil.applyFilter(object);
    }

    /***
     * beginTouch
     */
    private void onDiffBegin() {
        mbkDiff = mMediaParamImp.copy();
        onResetImp();

    }

    /**
     * endTouch
     */
    private void onDiffEnd() {   //restore toning data
        if (null != mbkDiff) {
            mBrightness = mbkDiff.getBrightness();
            mContrast = mbkDiff.getContrast();
            mSaturation = mbkDiff.getSaturation();
            mSharpen = mbkDiff.getSharpen();
            mVignette = mbkDiff.getVignette();
            mTemperature = mbkDiff.getTemperature();
            mTint = mbkDiff.getTintValue();
            vignettId = mbkDiff.getVignetteId();
            mHighlight = mbkDiff.getHighlightsValue();
            mShadow = mbkDiff.getShadowsValue();

            mGraininess = mbkDiff.getGraininess();
            mLightSensation = mbkDiff.getLightSensation();
            mFade = mbkDiff.getFade();
        }
        initValue();
        onTempSave();
        if (mVideoHandlerListener.getParamHandler().getEditMode() == IMenu.pip) {
            changeFilter(mPIPObject);
        } else {
            mVideoHandlerListener.onChangeEffectFilter();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRoot = null;
    }
}
