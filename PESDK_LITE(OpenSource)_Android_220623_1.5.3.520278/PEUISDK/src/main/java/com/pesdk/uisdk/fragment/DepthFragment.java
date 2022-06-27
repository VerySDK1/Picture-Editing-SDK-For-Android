package com.pesdk.uisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.ExtImageInfo;
import com.pesdk.uisdk.bean.code.Segment;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.UndoInfo;
import com.pesdk.uisdk.edit.EditDataHandler;
import com.pesdk.uisdk.fragment.helper.DepthBarHandler;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.uisdk.util.helper.FilterUtil;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;
import com.vecore.models.VisualFilterConfig;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 景深:
 * a. 无背景时，自动添加当前图片作为背景(模糊滤镜)+原始对象抠图，且拖动原图过程中，背景也要保持同步的显示位置
 * b. 有背景时，仅操作前景
 */
@Deprecated
public class DepthFragment extends BaseFragment {

    public static DepthFragment newInstance() {

        Bundle args = new Bundle();

        DepthFragment fragment = new DepthFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private final int TYPE_SCENE = 2;
    private final int TYPE_FG = 1;

    private ImageHandlerListener mVideoHandlerListener;
    private PEScene mScene, mBK;
    private PEImageObject mBGBlur;
    private CollageInfo mCollageInfo, mBKCollage;
    private int nType = 0;
    private DepthBarHandler mBarHandler;
    private ViewGroup mMenuRevokeLayout;

    /**
     * 背景
     */
    public void setScene(PEScene scene, EditDataHandler editDataHandler) {
        nType = TYPE_SCENE;
        mBKCollage = null;
        mCollageInfo = null;
        mScene = scene;
        if (null != scene) {
            PEImageObject imageObject = scene.getPEImageObject();
            mBK = scene.copy();
//            editDataHandler.replaceImage(scene, IMenu.depth); //**********************************当前状态，先保存下(退出时放弃此记录)
            if (null == scene.getBackground()) {
                try {
                    mBGBlur = new PEImageObject(getActivity(), imageObject.getMediaPath());
                    mBGBlur.setClipRect(imageObject.getClipRect());
                    scene.setBackground(mBGBlur);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
                Utils.setSegment(scene.getPEImageObject(), Segment.SEGMENT_PERSON);
            } else {
                mBGBlur = scene.getBackground();
            }
        } else {
            Log.e(TAG, "setScene: error....");
            mBK = null;
            mBGBlur = null;
        }
    }


    /**
     * 前景
     *
     * @param collageInfo
     */
    public void setFG(CollageInfo collageInfo) {
        nType = TYPE_FG;
        mBK = null;
        mScene = null;
        mCollageInfo = collageInfo;
        if (null != collageInfo) {
            mBKCollage = new CollageInfo(collageInfo, false);
            PEImageObject mediaObject = collageInfo.getImageObject();
            if (null == collageInfo.getBG()) {
                Utils.setSegment(mediaObject, Segment.SEGMENT_PERSON);
                mBGBlur = new PEImageObject(mediaObject);
                collageInfo.setBG(mBGBlur);      //画中画，底图(高斯模糊)需插入到上一个Index;
            } else {
                mBGBlur = collageInfo.getBG();
            }

        } else {
            mBKCollage = null;
            mBGBlur = null;
        }
    }

    public void setMenuRevokeLayout(ViewGroup menuRevokeLayout) {
        mMenuRevokeLayout = menuRevokeLayout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mVideoHandlerListener = (ImageHandlerListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_depth_layout, container, false);
        changeByHand = false;
        return mRoot;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.pesdk_depth);
        mMenuRevokeLayout.findViewById(R.id.btnChildDiff).setVisibility(View.VISIBLE);
        mMenuRevokeLayout.findViewById(R.id.btnChildRevoke).setVisibility(View.GONE);
        mMenuRevokeLayout.findViewById(R.id.btnChildUndo).setVisibility(View.GONE);
        mMenuRevokeLayout.findViewById(R.id.btnChildReset).setVisibility(View.GONE);
        mBarHandler = new DepthBarHandler(mRoot, new DepthBarHandler.Callback() {
            @Override
            public void progress(float factor) {
                changeByHand = true;
                List<VisualFilterConfig> list = new ArrayList<>();
                list.add(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR, factor));
                try {
                    mBGBlur.changeFilterList(list);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStopTrackingTouch(float factor) {

            }
        });
        if (null != mBGBlur) {
            float value = FilterUtil.getBlurValue(mBGBlur.getFilterList());
            mRoot.post(() -> mBarHandler.setFactor(value));
        }
        mVideoHandlerListener.reBuild();
        PEImageObject tmp = mScene.getPEImageObject();
        PEImageObject bg = mScene.getBackground();
        if (null != bg && TextUtils.equals(tmp.getMediaPath(), bg.getMediaPath())) {
            //自身作为背景 (不可以调节)
        }

    }

    private boolean changeByHand = false;

    @Override
    public void onCancelClick() {
        if (hasChanged()) {
            showAlert(new AlertCallback() {
                @Override
                public void cancel() {

                }

                @Override
                public void sure() {
                    giveUp();
                    mMenuCallBack.onCancel();
                }
            });
        } else { //放弃进入时拷贝的记录
            giveUp();
            mMenuCallBack.onCancel();
        }
    }

    private void giveUp() {
        if (nType == TYPE_SCENE) {
            UndoInfo info = mVideoHandlerListener.getParamHandler().onDeleteStep();
            if (null != info) {
                mVideoHandlerListener.getParamHandler().restorePE((ExtImageInfo) info.getList().get(0));
            }
        } else if (nType == TYPE_FG) {
            mCollageInfo.setBG(mBKCollage.getBG());
        }
        mVideoHandlerListener.reBuild();
    }

    private boolean hasChanged() {
        return changeByHand || !mScene.getPEImageObject().getShowRectF().equals(mBK.getPEImageObject().getShowRectF());//滤镜||显示位置有变化
    }

    @Override
    public void onSureClick() {
        if (hasChanged()) {
            mVideoHandlerListener.getParamHandler().onSaveDraft(IMenu.depth);
        } else { //用户未做任何操作,放弃进入时记录的数据
            giveUp();
        }
        mVideoHandlerListener.reBuild();
        mMenuCallBack.onSure();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRoot = null;
        mMenuCallBack = null;
        mBarHandler = null;
        mVideoHandlerListener = null;
    }
}
