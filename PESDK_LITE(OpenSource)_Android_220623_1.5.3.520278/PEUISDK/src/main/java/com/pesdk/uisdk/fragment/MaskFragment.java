package com.pesdk.uisdk.fragment;

import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.MaskAdapter;
import com.pesdk.uisdk.bean.MaskItem;
import com.pesdk.uisdk.data.vm.MaskVM;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.fragment.mask.MaskRender;
import com.pesdk.uisdk.fragment.mask.MaskView;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.listener.OnItemClickListener;
import com.pesdk.uisdk.widget.ColorBar;
import com.vecore.models.MaskObject;
import com.vecore.models.PEImageObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 形状/填充 （蒙版）
 */
public class MaskFragment extends BaseFragment {
    //最大粗细和羽化
    private static final float MAX_THICKNESS = 1f;
    private static final float MAX_EMERGENCE = 1f;

    public static MaskFragment newInstance() {
        Bundle args = new Bundle();
        MaskFragment fragment = new MaskFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private MaskView mMaskView;
    private SeekBar mSbarFeather, mSbarBorderWidth;
    private RecyclerView maskType;
    private MaskAdapter mMaskAdapter;
    private LinearLayout llEmergence, llBorder;
    private ColorBar mColorBar;
    private TextView tvFeatherValue, tvBorderValue;
    private float mFeather = 0.0f; // 羽化度
    private float mEdgeSize = 0.0f; // 粗细
    private int mEdgeColor = 0; // 颜色
    private boolean isInverse = false; //是否反转 ture 反转 false 不反转
    private MaskObject mMaskObject;
    private CheckBox mExtTextView;

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
        mFlContainer.removeView(mMaskView);
        mMenuCallBack.onSure();
        mMaskView = null;
    }

    @Override
    public void onSureClick() {
        exit();
    }

    //容器 编辑时
    private FrameLayout mFlContainer;
    private PEImageObject mPEImage;

    public void setPEImage(PEImageObject object) {
        mPEImage = object;
    }

    private MaskVM mVM;
    private ImageHandlerListener mVideoHandlerListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mVideoHandlerListener = (ImageHandlerListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_mask, container, false);
        llBorder = $(R.id.llBorderWidth);
        mColorBar = $(R.id.colorBar);
        initView();
        mVM = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(MaskVM.class);
        mVM.getData().observe(getViewLifecycleOwner(), this::onDataResult);
        mVM.load();
        return mRoot;
    }


    private int getIndex(List<MaskItem> list, String mName) {
        if (TextUtils.isEmpty(mName)) {
            return 0;
        }
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (mName.equals(list.get(i).getName())) {
                return i;
            }
        }
        return 0;
    }

    private void onDataResult(List<MaskItem> maskItems) {
        if (null != maskItems) {
            maskItems.add(0, new MaskItem(getString(R.string.pesdk_none), R.drawable.pesdk_sub_flower_none));
        }
        mMaskAdapter.addAll(maskItems, getIndex(maskItems, mMaskObject.getName()));
        maskType.postDelayed(() -> {
            restoreUI(mMaskAdapter.getChecked());
        }, 50);
    }


    private Runnable postInverse = new Runnable() {
        @Override
        public void run() {
            isInverse = mExtTextView.isChecked();
            changeMask();
        }
    };

    private boolean recordStep = false; //false 未记录步骤;true 已记录步骤

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.pesdk_mask);
        mExtTextView = $(R.id.rb_mask);
        mExtTextView.setChecked(isInverse);
        mExtTextView.setOnClickListener(v -> {
            mExtTextView.post(postInverse);
        });

        recordStep = false;
        int feather = (int) (mMaskObject.getFeather() / MAX_EMERGENCE * mSbarFeather.getMax());
        mSbarFeather.setProgress(feather);
        tvFeatherValue.setText(Integer.toString(feather));

        int size = (int) (mMaskObject.getEdgeSize() / MAX_THICKNESS * mSbarBorderWidth.getMax());
        mSbarBorderWidth.setProgress(size);
        tvBorderValue.setText(Integer.toString(size));
    }

    /**
     * 记录Mask调整
     */
    private void recordMask() {
        if (!recordStep) {
            recordStep = true;
            mVideoHandlerListener.getParamHandler().onSaveAdjustStep(IMenu.pip);
        }
    }

    private void initView() {
        mMaskObject = mPEImage.getMaskObject();
        if (mMaskObject == null) {
            mMaskObject = new MaskObject();
            mMaskObject.setFeather(0.1f);//默认设置0.1f,防止默认无效果
        }
        mFeather = mMaskObject.getFeather();
        mEdgeSize = mMaskObject.getEdgeSize();
        isInverse = mMaskObject.isInvert();
        maskType = $(R.id.mask_type);
        llEmergence = $(R.id.llEmergence);
        tvFeatherValue = $(R.id.tvPosition);
        mMaskAdapter = new MaskAdapter(Glide.with(this));
        maskType.setAdapter(mMaskAdapter);
        mMaskAdapter.setOnItemClickListener((OnItemClickListener<MaskItem>) (position, item) -> {
            recordMask();
            mMaskObject.setName(item.getName());
            mMaskObject.setMaskId(item.getMaskId());
            onItemCheck(position);
        });
        mSbarFeather = $(R.id.sbarStrength);
        mSbarFeather.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    changeMask();
                    mFeather = progress * MAX_EMERGENCE / seekBar.getMax();
                    tvFeatherValue.setText("" + progress);
                    recordMask();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvFeatherValue.setText("" + seekBar.getProgress());
            }
        });
        tvBorderValue = $(R.id.tvBorderValue);
        mSbarBorderWidth = $(R.id.sbarBorderWidth);
        mSbarBorderWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    recordMask();
                    mEdgeSize = progress * MAX_THICKNESS / seekBar.getMax();
                    tvBorderValue.setText(progress + "");
                    changeMask();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvBorderValue.setText("" + seekBar.getProgress());
            }
        });
        mEdgeColor = mMaskObject.getEdgeColor();
        mColorBar.setColor(mEdgeColor);
        mColorBar.setCallback(new ColorBar.Callback() {
            @Override
            public void onNone() {

            }

            @Override
            public void onColor(int color) {
                mEdgeColor = color;
                changeMask();
            }
        });

    }


    private void setMenuUI(int position) {
        if (position == 0) {
            llEmergence.setVisibility(View.GONE);
            llBorder.setVisibility(View.GONE);
            mColorBar.setVisibility(View.GONE);
        } else {
            llEmergence.setVisibility(View.VISIBLE);
            String name = mMaskAdapter.getItem(position).getName();
            if (TextUtils.equals(name, "四边形") || TextUtils.equals(name, "矩形")) {
                llBorder.setVisibility(View.VISIBLE);
                mColorBar.setVisibility(View.VISIBLE);
            } else {
                llBorder.setVisibility(View.INVISIBLE);
                mColorBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 恢复上一次的蒙版状态
     */
    private void restoreUI(int position) {
        setMenuUI(position);
        initMaskView();
        onKeyFrameUI();
    }

    private void onItemCheck(int position) {
        setMenuUI(position);
        initMaskView();
        MaskObject.KeyFrame keyFrame = mMaskView.getKeyFrame();
        if (keyFrame != null) {
            if (mMaskObject != null) {
                keyFrame.setSize(mMaskObject.getSize());
            }
            setMask(keyFrame);
        } else {
            MaskObject.KeyFrame frame = new MaskObject.KeyFrame();
            frame.setAtTime(-1);
            setMask(frame);
        }
        onKeyFrameUI();
    }


    private void onKeyFrameUI() {
        MaskObject.KeyFrame frame = new MaskObject.KeyFrame();
        frame.setSize(mMaskObject.getSize())
                .setAngle(mMaskObject.getAngle())
                .setCornerRadius(mMaskObject.getCornerRadius())
                .setPointFList(mMaskObject.getPointFList())
                .setEdgeSize(mEdgeSize)
                .setEdgeColor(mEdgeColor)
                .setFeather(mFeather)
                .setCenter(mMaskObject.getCenter());
        mMaskView.setKeyframe(frame);
    }

    private void initMaskView() {
        FrameLayout container = mFlContainer;
        if (mMaskView == null) {
            container.removeAllViews();
            mMaskView = new MaskView(getContext(), null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout
                    .LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            mMaskView.setLayoutParams(params);
            mMaskView.setListener(new MaskView.OnMaskListener() {
                @Override
                public void onDown() {
                }

                @Override
                public void onChange(MaskObject.KeyFrame frame) {
                    setMask(frame);
                }

            });
            //添加
            container.addView(mMaskView);
        }
        //设置
        MaskItem item = mMaskAdapter.getItem(mMaskAdapter.getChecked());
        if (item != null) {
            MaskRender maskRender = item.getMaskRender();
            if (maskRender != null) {
                //显示位置、角度、大小
                RectF rectF = new RectF();
                int angle;
                if (mPEImage != null) {
                    RectF showRectF = mPEImage.getShowRectF();
                    angle = -mPEImage.getShowAngle();
                    rectF.set(showRectF);
                } else {
                    RectF showRectF = mPEImage.getShowRectF();
                    if (showRectF == null || showRectF.isEmpty()) {
                        float asp = mPEImage.getWidth() * 1.0f / mPEImage.getHeight();
                        float aspContainer = container.getWidth() * 1.0f / container.getHeight();
                        if (asp > aspContainer) {
                            float value = (1 - aspContainer / asp) / 2;
                            showRectF = new RectF(0, value, 1, 1 - value);
                        } else {
                            float value = (1 - asp / aspContainer) / 2;
                            showRectF = new RectF(value, 0, 1 - value, 1);
                        }
                    }
                    rectF.set(showRectF);
                    angle = -mPEImage.getShowAngle();
                }
                maskRender.init(rectF, new RectF(0, 0, container.getWidth(), container.getHeight()), angle);
            }
            mMaskView.setMaskRender(maskRender);
        }
    }


    private void changeMask() {
        MaskObject.KeyFrame keyFrame = new MaskObject.KeyFrame();
        keyFrame.setAtTime(-1);
        setMask(keyFrame);
    }

    /**
     * 设置mask
     */
    private void setMask(MaskObject.KeyFrame frame) {
        if (frame == null) {
            return;
        }

        //设置羽化、反转、颜色、边框
        frame.setInvert(isInverse)
                .setEdgeColor(mEdgeColor)
                .setEdgeSize(mEdgeSize)
                .setFeather(mFeather);
        //角度
        RectF rectF = mPEImage.getShowRectF();//保持与核心中的相对坐标一致
        if (rectF.isEmpty()) {
            rectF = new RectF(0, 0, 1, 1);
            mPEImage.setShowRectF(rectF);
        }
        apply(frame);
    }

    private void apply(MaskObject.KeyFrame frame) {
        mMaskObject.setFeather(frame.getFeather())
                .setEdgeSize(frame.getEdgeSize())
                .setEdgeColor(frame.getEdgeColor())
                .setPointFList(frame.getPointFList())
                .setInvert(frame.isInvert());
        if (frame.getAtTime() != -1) {
            mMaskObject.setAngle(frame.getAngle())
                    .setCenter(frame.getCenter())
                    .setSize(frame.getSize())
                    .setCornerRadius(frame.getCornerRadius());
        }
        mPEImage.setMaskObject(mMaskObject);
    }


    /**
     * 拖拽组件容器
     *
     * @param linearWords
     */
    public void setLinearWords(FrameLayout linearWords) {
        mFlContainer = linearWords;
    }
}
