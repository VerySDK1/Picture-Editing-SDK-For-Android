package com.pesdk.uisdk.fragment.canvas;

import android.content.Context;
import android.os.Bundle;

import com.pesdk.uisdk.bean.ExtImageInfo;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.fragment.BaseFragment;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.internal.SdkEntryHandler;
import com.pesdk.uisdk.listener.IEditCallback;
import com.pesdk.uisdk.listener.ImageHandlerListener;

import java.util.ArrayList;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;

/**
 *
 */
public abstract class AbsCanvasFragment extends BaseFragment {
    boolean insert = false;
    ActivityResultLauncher<Void> mResultLauncher;
    ExtImageInfo mBackupScene;
    ExtImageInfo mScene;
    CollageInfo mCollageInfo; //画中画背景
    CollageInfo mBkCollage;


    ImageHandlerListener mVideoEditorHandler;
    IEditCallback mEditCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mVideoEditorHandler = (ImageHandlerListener) context;
        mEditCallback = (IEditCallback) context;
    }

    /**
     * 虚拟图片设置背景（作为场景使用）
     */
    public void setPEScene(ExtImageInfo peScene) {
        mScene = peScene;
        mBackupScene = mScene.copy();
        mCollageInfo = null;
        mBkCollage = null;
    }

    /**
     * 画中画-背景
     */
    public void setCollageInfo(CollageInfo collageInfo) {
        mCollageInfo = collageInfo;
        mBkCollage = mCollageInfo.copy();
        mScene = null;
        mBackupScene = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        insert = false;
        ActivityResultContract<Void, ArrayList<String>> albumContract = SdkEntryHandler.getInstance().getAlbumContract();
        if (albumContract != null) {
            mResultLauncher = registerForActivityResult(albumContract, result -> {

                if (null != result && result.size() > 0) {
                    //增加单个layer
                    mRoot.postDelayed(() -> {
                        onResultImage(result.get(0));
                    }, 100);
                }
            });
        }
    }

    protected abstract void onResultImage(String path);

    boolean hasChanged() {
        if (insert) {
            return true;
        } else if (null != mCollageInfo) {
            return !mCollageInfo.getImageObject().getShowRectF().equals(mBkCollage.getImageObject().getShowRectF());
        } else if (null != mScene) {
            return false;
        } else
            return false;
    }

    void saveSync(@IMenu int menu) {
        if (!insert) {
            insert = true;
            if (null != mCollageInfo) {
                mVideoEditorHandler.getParamHandler().onSaveAdjustStep(IMenu.pip);
            } else if (null != mScene) {
                mVideoEditorHandler.getParamHandler().replaceImage(mScene, menu); //当前状态，先保存下
            }
        }
    }

    abstract void onToastMsg();

}
