package com.pesdk.uisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pesdk.uisdk.Interface.Ioff;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.fragment.main.MenuCallback;
import com.pesdk.uisdk.ui.home.EditActivity;
import com.vecore.models.PEImageObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 微调
 */
public class RectAdjustFragment extends BaseFragment {
    public static RectAdjustFragment newInstance() {
        Bundle args = new Bundle();
        RectAdjustFragment fragment = new RectAdjustFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Ioff mIFg;
    private MenuCallback mMenuCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = getContext();
        mMenuCallback = (MenuCallback) context;
    }


    public void setFg(Ioff iFg) {
        this.mIFg = iFg;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_rect_adjust_layout, container, false);
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.pesdk_rect_adjust);
        return mRoot;
    }

    private SeekBar mRotateBar;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //前景
        $(R.id.ivOffCenter).setOnClickListener(mfgListener);
        $(R.id.ivOffLeft).setOnClickListener(mfgListener);
        $(R.id.ivOffTop).setOnClickListener(mfgListener);
        $(R.id.ivOffDown).setOnClickListener(mfgListener);
        $(R.id.ivOffRight).setOnClickListener(mfgListener);
        $(R.id.ivOffNnlarge).setOnClickListener(mfgListener);
        $(R.id.ivOffNarrow).setOnClickListener(mfgListener);
        $(R.id.ivOffFull).setOnClickListener(mfgListener);

        mRotateBar = $(R.id.sbar_rotate);
        mRotateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int angle = 360 * progress / seekBar.getMax();
                    mIFg.setAngle(angle);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int angle = 360 * seekBar.getProgress() / seekBar.getMax();
                mIFg.setAngle(angle);
            }
        });

        $(R.id.ivBgHorizontal).setOnClickListener(mbgListener);
        $(R.id.ivBgVer).setOnClickListener(mbgListener);

        setProgressValue();
    }

    private void setProgressValue() {
        CollageInfo tmp = mIFg.getCurrentCollageInfo();
        if (null != tmp && null != mRotateBar) {
            PEImageObject peImageObject = tmp.getImageObject();
            mRotateBar.setProgress(peImageObject.getShowAngle() % 360 * mRotateBar.getMax() / 360);
        }
    }

    private View.OnClickListener mbgListener = v -> onCheckBgId(v.getId());
    private View.OnClickListener mfgListener = v -> onCheckFgId(v.getId());

    /**
     * 背景
     */
    private void onCheckBgId(int id) {
        if (id == R.id.ivBgHorizontal) {
            mMenuCallback.onMirrorLeftright();
        } else if (id == R.id.ivBgVer) {
            mMenuCallback.onMirrorUpDown();
        }
    }

    /**
     * 前景
     */
    private void onCheckFgId(int id) {
        if (null != mIFg) {
            if (id == R.id.ivOffCenter) {
                mIFg.offCenter();
            } else if (id == R.id.ivOffLeft) {
                mIFg.offLeft();
            } else if (id == R.id.ivOffTop) {
                mIFg.offUp();
            } else if (id == R.id.ivOffDown) {
                mIFg.offDown();
            } else if (id == R.id.ivOffRight) {
                mIFg.offRight();
            } else if (id == R.id.ivOffNnlarge) {
                mIFg.offLarge();
            } else if (id == R.id.ivOffNarrow) {
                mIFg.offNarrow();
            } else if (id == R.id.ivOffFull) {
                mIFg.offFull();
            }
        }
    }


    @Override
    public void onCancelClick() {
        showAlert(new AlertCallback() {
            @Override
            public void cancel() {

            }

            @Override
            public void sure() {
                //放弃修改
                mMenuCallBack.onCancel();
            }
        });

    }

    @Override
    public void onSureClick() {
        //保存下草稿，保证异常退出位置实时保存
        if (getActivity() instanceof EditActivity) {
            ((EditActivity) getActivity()).getParamHandler().onSaveDraft(IMenu.pip);
        }
        mMenuCallBack.onSure();
    }

    public void onAngleChanged() {
        if (isRunning) {
            setProgressValue();
        }
    }
}
