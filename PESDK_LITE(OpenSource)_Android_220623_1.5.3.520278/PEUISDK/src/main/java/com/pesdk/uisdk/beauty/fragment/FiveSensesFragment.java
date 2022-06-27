package com.pesdk.uisdk.beauty.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.beauty.widget.ExtSeekBar3;
import com.pesdk.uisdk.beauty.listener.OnBeautyListener;
import com.pesdk.uisdk.beauty.bean.BeautyFaceInfo;
import com.pesdk.uisdk.fragment.BaseFragment;

import androidx.annotation.NonNull;


/**
 * 五官
 */
public class FiveSensesFragment extends BaseFragment {

    public static FiveSensesFragment newInstance() {
        return new FiveSensesFragment();
    }

    //比例
    private static final float LEFT_VALUE = 0.5f;

    //美颜
    private OnBeautyListener mBeautyListener;
    //数据接口
    private OnFiveSensesListener mFiveListener;

    /**
     * 菜单布局
     */
    private RelativeLayout mRlFaceShape;
    private RelativeLayout mRlChin;
    private RelativeLayout mRlForehead;
    private RelativeLayout mRlNose;
    private RelativeLayout mRlSmile;
    private RelativeLayout mRlEye;
    private RelativeLayout mRlMouth;

    /**
     * 轴
     */
    private ExtSeekBar3 mBarFaceShape;
    private ExtSeekBar3 mBarChinW;
    private ExtSeekBar3 mBarChinH;
    private ExtSeekBar3 mBarForehead;
    private ExtSeekBar3 mBarNoseWidth;
    private ExtSeekBar3 mBarNoseHeight;
    private ExtSeekBar3 mBarSmile;
    private ExtSeekBar3 mBarEyeTilt;
    private ExtSeekBar3 mBarEyeDistance;
    private ExtSeekBar3 mBarEyeWidth;
    private ExtSeekBar3 mBarEyeHeight;
    private ExtSeekBar3 mBarMouthUpper;
    private ExtSeekBar3 mBarMouthLower;
    private ExtSeekBar3 mBarMouthWidth;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_beauty_wuguan, container, false);
        return mRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //确定
        $(R.id.btn_sure).setOnClickListener(v -> onBackPressed());


        //菜单
        ((RadioGroup) $(R.id.rg_menu)).setOnCheckedChangeListener((group, checkedId) -> onClickMenu(checkedId));

        //布局
        mRlFaceShape = $(R.id.rl_face_shape);
        mRlChin = $(R.id.rl_chin);
        mRlForehead = $(R.id.rl_forehead);
        mRlNose = $(R.id.rl_nose);
        mRlSmile = $(R.id.rl_smile);
        mRlEye = $(R.id.rl_eye);
        mRlMouth = $(R.id.rl_mouth);

        //轴
        mBarFaceShape = $(R.id.bar_face_shape);
        mBarChinW = $(R.id.bar_chin_w);
        mBarChinH = $(R.id.bar_chin_h);
        mBarForehead = $(R.id.bar_forehead);
        mBarNoseWidth = $(R.id.bar_nose_w);
        mBarNoseHeight = $(R.id.bar_nose_h);
        mBarSmile = $(R.id.bar_smile);
        mBarEyeTilt = $(R.id.eye_tilt);
        mBarEyeDistance = $(R.id.eye_distance);
        mBarEyeWidth = $(R.id.eye_size_w);
        mBarEyeHeight = $(R.id.eye_size_h);
        mBarMouthUpper = $(R.id.mouth_upper);
        mBarMouthLower = $(R.id.mouth_lower);
        mBarMouthWidth = $(R.id.mouth_width);

        mBarFaceShape.setLeftValue(LEFT_VALUE);
        mBarChinW.setLeftValue(LEFT_VALUE);
        mBarChinH.setLeftValue(LEFT_VALUE);
        mBarForehead.setLeftValue(LEFT_VALUE);
        mBarNoseWidth.setLeftValue(LEFT_VALUE);
        mBarNoseHeight.setLeftValue(LEFT_VALUE);
        mBarSmile.setLeftValue(LEFT_VALUE);
        mBarEyeTilt.setLeftValue(LEFT_VALUE);
        mBarEyeDistance.setLeftValue(LEFT_VALUE);
        mBarEyeWidth.setLeftValue(LEFT_VALUE);
        mBarEyeHeight.setLeftValue(LEFT_VALUE);
        mBarMouthUpper.setLeftValue(LEFT_VALUE);
        mBarMouthLower.setLeftValue(LEFT_VALUE);
        mBarMouthWidth.setLeftValue(LEFT_VALUE);

        mBarFaceShape.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBeautyFaceInfo != null) {
                    float v = LEFT_VALUE * seekBar.getMax();
                    float value;
                    if (progress > v) {
                        value = (progress - v) / (seekBar.getMax() - v);
                    } else {
                        value = (progress - v) / v;
                    }
                    mBeautyFaceInfo.setFaceLift(value);
                    modifyParameter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBarChinW.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBeautyFaceInfo != null) {
                    float v = LEFT_VALUE * seekBar.getMax();
                    float value;
                    if (progress > v) {
                        value = (progress - v) / (seekBar.getMax() - v);
                    } else {
                        value = (progress - v) / v;
                    }
                    mBeautyFaceInfo.setChinWidth(value);
                    modifyParameter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBarChinH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBeautyFaceInfo != null) {
                    float v = LEFT_VALUE * seekBar.getMax();
                    float value;
                    if (progress > v) {
                        value = (progress - v) / (seekBar.getMax() - v);
                    } else {
                        value = (progress - v) / v;
                    }
                    mBeautyFaceInfo.setChinHeight(value);
                    modifyParameter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBarForehead.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBeautyFaceInfo != null) {
                    float v = LEFT_VALUE * seekBar.getMax();
                    float value;
                    if (progress > v) {
                        value = (progress - v) / (seekBar.getMax() - v);
                    } else {
                        value = (progress - v) / v;
                    }
                    mBeautyFaceInfo.setForehead(value);
                    modifyParameter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBarNoseWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBeautyFaceInfo != null) {
                    float v = LEFT_VALUE * seekBar.getMax();
                    float value;
                    if (progress > v) {
                        value = (progress - v) / (seekBar.getMax() - v);
                    } else {
                        value = (progress - v) / v;
                    }
                    mBeautyFaceInfo.setNoseWidth(value);
                    modifyParameter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBarNoseHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBeautyFaceInfo != null) {
                    float v = LEFT_VALUE * seekBar.getMax();
                    float value;
                    if (progress > v) {
                        value = (progress - v) / (seekBar.getMax() - v);
                    } else {
                        value = (progress - v) / v;
                    }
                    mBeautyFaceInfo.setNoseHeight(value);
                    modifyParameter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBarSmile.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBeautyFaceInfo != null) {
                    float v = LEFT_VALUE * seekBar.getMax();
                    float value;
                    if (progress > v) {
                        value = (progress - v) / (seekBar.getMax() - v);
                    } else {
                        value = (progress - v) / v;
                    }
                    mBeautyFaceInfo.setSmile(value);
                    modifyParameter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBarEyeTilt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBeautyFaceInfo != null) {
                    float v = LEFT_VALUE * seekBar.getMax();
                    float value;
                    if (progress > v) {
                        value = (progress - v) / (seekBar.getMax() - v);
                    } else {
                        value = (progress - v) / v;
                    }
                    mBeautyFaceInfo.setEyeTilt(value);
                    modifyParameter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBarEyeDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBeautyFaceInfo != null) {
                    float v = LEFT_VALUE * seekBar.getMax();
                    float value;
                    if (progress > v) {
                        value = (progress - v) / (seekBar.getMax() - v);
                    } else {
                        value = (progress - v) / v;
                    }
                    mBeautyFaceInfo.setEyeDistance(value);
                    modifyParameter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBarEyeWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBeautyFaceInfo != null) {
                    float v = LEFT_VALUE * seekBar.getMax();
                    float value;
                    if (progress > v) {
                        value = (progress - v) / (seekBar.getMax() - v);
                    } else {
                        value = (progress - v) / v;
                    }
                    mBeautyFaceInfo.setEyeWidth(value);
                    modifyParameter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBarEyeHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBeautyFaceInfo != null) {
                    float v = LEFT_VALUE * seekBar.getMax();
                    float value;
                    if (progress > v) {
                        value = (progress - v) / (seekBar.getMax() - v);
                    } else {
                        value = (progress - v) / v;
                    }
                    mBeautyFaceInfo.setEyeHeight(value);
                    modifyParameter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBarMouthUpper.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBeautyFaceInfo != null) {
                    float v = LEFT_VALUE * seekBar.getMax();
                    float value;
                    if (progress > v) {
                        value = (progress - v) / (seekBar.getMax() - v);
                    } else {
                        value = (progress - v) / v;
                    }
                    mBeautyFaceInfo.setMouthUpper(value);
                    modifyParameter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBarMouthLower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBeautyFaceInfo != null) {
                    float v = LEFT_VALUE * seekBar.getMax();
                    float value;
                    if (progress > v) {
                        value = (progress - v) / (seekBar.getMax() - v);
                    } else {
                        value = (progress - v) / v;
                    }
                    mBeautyFaceInfo.setMouthLower(value);
                    modifyParameter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBarMouthWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBeautyFaceInfo != null) {
                    float v = LEFT_VALUE * seekBar.getMax();
                    float value;
                    if (progress > v) {
                        value = (progress - v) / (seekBar.getMax() - v);
                    } else {
                        value = (progress - v) / v;
                    }
                    mBeautyFaceInfo.setMouthWidth(value);
                    modifyParameter();
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
                mBeautyFaceInfo.resetFiveSenses();
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
            mBeautyFaceInfo.resetFiveSenses();
            recover();
            modifyParameter();
        });

        //恢复
        recover();
    }


    /**
     * 点击菜单
     */
    private void onClickMenu(int id) {

        //菜单
        mRlFaceShape.setVisibility(View.GONE);
        mRlChin.setVisibility(View.GONE);
        mRlForehead.setVisibility(View.GONE);
        mRlNose.setVisibility(View.GONE);
        mRlSmile.setVisibility(View.GONE);
        mRlEye.setVisibility(View.GONE);
        mRlMouth.setVisibility(View.GONE);
        if (id == R.id.btn_face_shape) {
            mRlFaceShape.setVisibility(View.VISIBLE);
        } else if (id == R.id.btn_chin) {
            mRlChin.setVisibility(View.VISIBLE);
        } else if (id == R.id.btn_forehead) {
            mRlForehead.setVisibility(View.VISIBLE);
        } else if (id == R.id.btn_nose) {
            mRlNose.setVisibility(View.VISIBLE);
        } else if (id == R.id.btn_smile) {
            mRlSmile.setVisibility(View.VISIBLE);
        } else if (id == R.id.btn_eye) {
            mRlEye.setVisibility(View.VISIBLE);
        } else if (id == R.id.btn_mouth) {
            mRlMouth.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 换脸
     */
    private void switchFace() {
        if (mFiveListener != null) {
            mFiveListener.onSwitchFace();
        }
    }

    /**
     * 修改参数
     */
    private void modifyParameter() {
        if (mFiveListener != null) {
            mFiveListener.onChange();
        }
    }


    /**
     * 恢复
     */
    public void recover() {
        if (mFiveListener == null) {
            return;
        }
        mBeautyFaceInfo = mFiveListener.getFace();
        if (mBarChinH == null || mBeautyFaceInfo == null) {
            return;
        }
        float v = LEFT_VALUE * 100;
        //脸型
        if (mBeautyFaceInfo.getFaceLift() >= 0) {
            mBarFaceShape.setProgress((int) (mBeautyFaceInfo.getFaceLift() * (mBarFaceShape.getMax() - v) + v));
        } else {
            mBarFaceShape.setProgress((int) (mBeautyFaceInfo.getFaceLift() * v + v));
        }
        //下巴
        if (mBeautyFaceInfo.getChinWidth() >= 0) {
            mBarChinW.setProgress((int) (mBeautyFaceInfo.getChinWidth() * (mBarChinW.getMax() - v) + v));
        } else {
            mBarChinW.setProgress((int) (mBeautyFaceInfo.getChinWidth() * v + v));
        }
        if (mBeautyFaceInfo.getChinHeight() >= 0) {
            mBarChinH.setProgress((int) (mBeautyFaceInfo.getChinHeight() * (mBarChinH.getMax() - v) + v));
        } else {
            mBarChinH.setProgress((int) (mBeautyFaceInfo.getChinHeight() * v + v));
        }
        //额头
        if (mBeautyFaceInfo.getForehead() >= 0) {
            mBarForehead.setProgress((int) (mBeautyFaceInfo.getForehead() * (mBarForehead.getMax() - v) + v));
        } else {
            mBarForehead.setProgress((int) (mBeautyFaceInfo.getForehead() * v + v));
        }
        //鼻子
        if (mBeautyFaceInfo.getNoseWidth() >= 0) {
            mBarNoseWidth.setProgress((int) (mBeautyFaceInfo.getNoseWidth() * (mBarNoseWidth.getMax() - v) + v));
        } else {
            mBarNoseWidth.setProgress((int) (mBeautyFaceInfo.getNoseWidth() * v + v));
        }
        if (mBeautyFaceInfo.getNoseHeight() >= 0) {
            mBarNoseHeight.setProgress((int) (mBeautyFaceInfo.getNoseHeight() * (mBarNoseHeight.getMax() - v) + v));
        } else {
            mBarNoseHeight.setProgress((int) (mBeautyFaceInfo.getNoseHeight() * v + v));
        }
        //微信
        if (mBeautyFaceInfo.getSmile() >= 0) {
            mBarSmile.setProgress((int) (mBeautyFaceInfo.getSmile() * (mBarSmile.getMax() - v) + v));
        } else {
            mBarSmile.setProgress((int) (mBeautyFaceInfo.getSmile() * v + v));
        }
        //眼睛
        if (mBeautyFaceInfo.getEyeTilt() >= 0) {
            mBarEyeTilt.setProgress((int) (mBeautyFaceInfo.getEyeTilt() * (mBarEyeTilt.getMax() - v) + v));
        } else {
            mBarEyeTilt.setProgress((int) (mBeautyFaceInfo.getEyeTilt() * v + v));
        }
        if (mBeautyFaceInfo.getEyeDistance() >= 0) {
            mBarEyeDistance.setProgress((int) (mBeautyFaceInfo.getEyeDistance() * (mBarEyeDistance.getMax() - v) + v));
        } else {
            mBarEyeDistance.setProgress((int) (mBeautyFaceInfo.getEyeDistance() * v + v));
        }
        if (mBeautyFaceInfo.getEyeWidth() >= 0) {
            mBarEyeWidth.setProgress((int) (mBeautyFaceInfo.getEyeWidth() * (mBarEyeWidth.getMax() - v) + v));
        } else {
            mBarEyeWidth.setProgress((int) (mBeautyFaceInfo.getEyeWidth() * v + v));
        }
        if (mBeautyFaceInfo.getEyeHeight() >= 0) {
            mBarEyeHeight.setProgress((int) (mBeautyFaceInfo.getEyeHeight() * (mBarEyeHeight.getMax() - v) + v));
        } else {
            mBarEyeHeight.setProgress((int) (mBeautyFaceInfo.getEyeHeight() * v + v));
        }
        //嘴巴
        if (mBeautyFaceInfo.getMouthUpper() >= 0) {
            mBarMouthUpper.setProgress((int) (mBeautyFaceInfo.getMouthUpper() * (mBarMouthUpper.getMax() - v) + v));
        } else {
            mBarMouthUpper.setProgress((int) (mBeautyFaceInfo.getMouthUpper() * v + v));
        }
        if (mBeautyFaceInfo.getMouthLower() >= 0) {
            mBarMouthLower.setProgress((int) (mBeautyFaceInfo.getMouthLower() * (mBarMouthLower.getMax() - v) + v));
        } else {
            mBarMouthLower.setProgress((int) (mBeautyFaceInfo.getMouthLower() * v + v));
        }
        if (mBeautyFaceInfo.getMouthWidth() >= 0) {
            mBarMouthWidth.setProgress((int) (mBeautyFaceInfo.getMouthWidth() * (mBarMouthWidth.getMax() - v) + v));
        } else {
            mBarMouthWidth.setProgress((int) (mBeautyFaceInfo.getMouthWidth() * v + v));
        }
        mSwitchFace.setVisibility(mFiveListener.getFaceNum() <= 1 ? View.GONE : View.VISIBLE);
    }

    /**
     * 回调
     */
    public void setFiveListener(OnFiveSensesListener fiveListener) {
        mFiveListener = fiveListener;
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

    public interface OnFiveSensesListener {

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
