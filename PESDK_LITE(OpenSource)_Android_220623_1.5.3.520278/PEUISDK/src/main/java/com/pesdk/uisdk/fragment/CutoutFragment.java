package com.pesdk.uisdk.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.widget.CircleView;
import com.pesdk.uisdk.widget.ExtSeekBar2;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.vecore.utils.Log;

import androidx.annotation.Nullable;

/**
 * 抠像
 */
@Deprecated
public class CutoutFragment extends BaseFragment {

    public static CutoutFragment newInstance() {
        Bundle args = new Bundle();
        CutoutFragment fragment = new CutoutFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private boolean hasInitial = false;
    /**
     * 调节
     */
    private ExtSeekBar2 mSbDegreeUpper;
    private ExtSeekBar2 mSbDegreeLower;

    //抠像2
    private ExtSeekBar2 mSbDegreeUpper2;
    private ExtSeekBar2 mSbDegreeLower2;

    //抠图
    private RadioButton mCutout1, mCutout2;
    private boolean isCutout1 = true; // ture 为抠图1 ，false为抠图2

    private boolean isChangeUpperPos = false; //记录mSbDegreeUpper值是否改变；
    private boolean isChangeLowerPos = false; //记录mSbDegreeLower值是否改变；
    private boolean isChangeUpper2Pos = false; //记录mSbDegreeUpper2值是否改变；
    private boolean isChangeLower2Pos = false; //记录mSbDegreeLower2值是否改变；

    /**
     * 取消抠图
     */
    private TextView mTvCancel;
    /**
     * 颜色值
     */
    private CircleView mCvColor;
    private TextView mTvColor;
    private int mColor = 0, mOldColor = 0;
    private float mUpperValue = 0.5f, mOldUpperValue = 0.5f;
    private float mLowerValue = 0.5F, mOldLowerValue = 0;
    private int mode = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk__fragment_cutout, container, false);
        initView();
        return mRoot;
    }

    private void initView() {
        mSbDegreeUpper = $(R.id.sb_degree_upper1);
        mSbDegreeUpper.setProgressColor(getResources().getColor(R.color.pesdk_white));
        mSbDegreeLower = $(R.id.sb_degree_lower1);
        mSbDegreeLower.setIsProgressColor(false);
        mSbDegreeLower.setIsSpeedReverse(true);
        mSbDegreeLower.setBgPaintColor(getResources().getColor(R.color.pesdk_white));
        mSbDegreeLower.setProgressColor(getResources().getColor(R.color.pesdk_config_titlebar_bg));
        mSbDegreeLower.setPaintColor(getResources().getColor(R.color.pesdk_white));

        mSbDegreeUpper2 = $(R.id.sb_degree_upper2);
        mSbDegreeUpper2.setProgressColor(getResources().getColor(R.color.pesdk_white));
        mSbDegreeLower2 = $(R.id.sb_degree_lower2);
        mSbDegreeLower2.setProgressColor(getResources().getColor(R.color.pesdk_white));


        mCvColor = $(R.id.cv_color);
        mTvColor = $(R.id.tv_color);
        mCutout1 = $(R.id.btn_cutout1);
        mCutout2 = $(R.id.btn_cutout2);
        if (mode == 1) {
            mCutout1.post(() -> mCutout1.setChecked(true));
        } else {
            mCutout2.post(() -> {
                mCutout2.setChecked(true);
                setCutout2();
            });
        }

        mTvCancel = $(R.id.tv_cancel);

        ((TextView) $(R.id.tvBottomTitle)).setText(getString(R.string.pesdk_cutout));

        mSbDegreeUpper.setProgress((int) (mUpperValue * mSbDegreeUpper.getMax()));
        mSbDegreeUpper.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                isChangeUpperPos = true;
                mUpperValue = i / (seekBar.getMax() + 0.0f);
                if (b && mListener != null) {
                    mListener.change(isCutout1, mUpperValue, mLowerValue);
                }
                mTvCancel.setEnabled(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSbDegreeLower.setProgress((int) (mLowerValue * mSbDegreeLower.getMax()));
        mSbDegreeLower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                isChangeLowerPos = true;
                mLowerValue = i / (seekBar.getMax() + 0.0f);
                if (b && mListener != null) {
                    mListener.change(isCutout1, mUpperValue, mLowerValue);
                }
                mTvCancel.setEnabled(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSbDegreeUpper2.setProgress((int) (mUpperValue * mSbDegreeUpper2.getMax()));
        mSbDegreeUpper2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                isChangeUpper2Pos = true;
                mUpperValue = i / (seekBar.getMax() + 0.0f);
                if (b && mListener != null) {
                    mListener.change(isCutout1, mUpperValue, mLowerValue);
                }
                mTvCancel.setEnabled(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSbDegreeLower2.setProgress((int) (mLowerValue * mSbDegreeLower2.getMax()));
        mSbDegreeLower2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                isChangeLower2Pos = true;
                mLowerValue = i / (seekBar.getMax() + 0.0f);
                if (b && mListener != null) {
                    mListener.change(isCutout1, mUpperValue, mLowerValue);
                }
                mTvCancel.setEnabled(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mCutout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCutout1 = true;
                mSbDegreeLower.setVisibility(View.VISIBLE);
                mSbDegreeLower2.setVisibility(View.GONE);
                if (!isChangeUpperPos) {
                    mSbDegreeUpper.setProgress(50);
                }
                if (!isChangeLowerPos) {
                    mSbDegreeLower.setProgress(50);
                }
                mSbDegreeUpper.setVisibility(View.VISIBLE);
                mSbDegreeUpper2.setVisibility(View.GONE);
                if (mListener != null) {
                    if (mUpperValue != 0.5 || mLowerValue != 0.5) {
                        mListener.change(isCutout1, mUpperValue, mLowerValue);
                    } else {
                        mListener.change(isCutout1, 0, 0);
                    }

                }
            }
        });

        mCutout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCutout2();
            }
        });

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCutout1) {
                    mSbDegreeLower.setProgress(50);
                    mSbDegreeUpper.setProgress(50);
                } else {
                    mSbDegreeLower2.setProgress(50);
                    mSbDegreeUpper2.setProgress(50);
                }
                if (mListener != null) {
                    mListener.onCancel();
                }
                mTvCancel.setEnabled(false);
            }
        });

    }

    public int getColor() {
        return mColor;
    }

    public float getUpperValue() {
        return mUpperValue;
    }

    public float getLowerValue() {
        return mLowerValue;
    }

    public void setOldColor(int color) {
        this.mColor = color;
        this.mOldColor = color;
        setColor(color);
    }

    public void setOldValue(float value, float lower, int mode) {
        Log.e(TAG, "mode==>" + mode);
        this.mode = mode;
        this.mUpperValue = value;
        this.mOldUpperValue = value;
        if (mode == 1) {
            if (mUpperValue != 0.5) {
                isChangeUpperPos = true;
            }
            this.mLowerValue = lower;
            if (mLowerValue != 0.5) {
                isChangeLowerPos = true;
            }
        } else if (mode == 2) {
            if (mUpperValue != 0.5) {
                isChangeUpper2Pos = true;
            }
            this.mLowerValue = lower;
            if (mLowerValue != 0.5) {
                isChangeLower2Pos = true;
            }
        }

        this.mOldLowerValue = lower;
        if (mSbDegreeUpper != null) {
            mSbDegreeUpper.setProgress((int) (value * mSbDegreeUpper.getMax()));
            mSbDegreeLower.setProgress((int) (lower * mSbDegreeUpper.getMax()));
        }
        isCutout1 = true;

    }

    /**
     * 设置颜色
     *
     * @param color
     */
    public void setColor(int color) {
        mColor = color;
        if (mCvColor != null && mTvColor != null) {
            mCvColor.setColor(color);
            int red = (color & 0xff0000) >> 16;
            int green = (color & 0x00ff00) >> 8;
            int blue = (color & 0x0000ff);
            mTvColor.setText("R:" + red + "  G:" + green + "  B:" + blue);
        }
        if (mTvCancel != null) {
            mTvCancel.setEnabled(true);
        }
    }

    public void setHasInitial(boolean hasInitial) {
        this.hasInitial = hasInitial;
    }

    @Override
    public int onBackPressed() {
        if (mColor != mOldColor || mUpperValue != mOldUpperValue || mOldLowerValue != mLowerValue) {
            onShowAlert();
            return -1;
        }
        if (mListener != null) {
            mListener.onCancel(hasInitial, mOldColor, mOldUpperValue, mOldLowerValue);
        }
        return super.onBackPressed();
    }

    private void onShowAlert() {
        SysAlertDialog.createAlertDialog(mContext,
                mContext.getString(R.string.pesdk_dialog_tips),
                mContext.getString(R.string.pesdk_cancel_all_changed),
                mContext.getString(R.string.pesdk_cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, mContext.getString(R.string.pesdk_sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (mListener != null) {
                            mListener.onCancel(hasInitial, mOldColor, mOldUpperValue, mOldLowerValue);
                        }
                    }
                }, false, null).show();
    }

    private OnCutoutListener mListener;

    public void setListener(OnCutoutListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onCancelClick() {
        onBackPressed();
    }

    @Override
    public void onSureClick() {
        if (mListener != null) {
            mListener.onSure();
            if (isCutout1) {
                mSbDegreeLower2.setProgress(50);
                mSbDegreeUpper2.setProgress(50);
            } else {
                mSbDegreeLower.setProgress(50);
                mSbDegreeUpper.setProgress(50);
            }
        }
    }


    public interface OnCutoutListener {

        void onSure();

        void onCancel(boolean hasinit, int color, float upper, float lower);

        void change(boolean isCutout1, float upper, float lower);

        /**
         * 取消抠图
         */
        void onCancel();

    }

    private void setCutout2() {
        mSbDegreeLower.setVisibility(View.GONE);
        mSbDegreeLower2.setVisibility(View.VISIBLE);
        if (!isChangeUpper2Pos) {
            mSbDegreeUpper2.setProgress(50);
        }
        if (!isChangeLower2Pos) {
            mSbDegreeLower2.setProgress(50);
        }
        mSbDegreeUpper.setVisibility(View.GONE);
        mSbDegreeUpper2.setVisibility(View.VISIBLE);
        isCutout1 = false;
        if (mListener != null) {
            if (mUpperValue != 0.5 || mLowerValue != 0.5) {
                mListener.change(isCutout1, mUpperValue, mLowerValue);
            } else {
                mListener.change(isCutout1, 0, 0);
            }

        }
    }
}
