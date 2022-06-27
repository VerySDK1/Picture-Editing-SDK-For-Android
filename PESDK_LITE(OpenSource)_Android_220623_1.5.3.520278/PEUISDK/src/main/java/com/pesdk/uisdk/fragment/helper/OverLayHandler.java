package com.pesdk.uisdk.fragment.helper;

import android.graphics.RectF;
import android.util.Log;

import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.export.LayerManager;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.fragment.main.fg.IFg;
import com.pesdk.uisdk.fragment.main.fg.LayerHandler;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.widget.edit.DragMediaView;

import java.util.List;

/**
 * 叠加
 */
public class OverLayHandler implements IFg {
    private static final String TAG = "OverLayHandler";

    private LayerHandler mLayerHandler;
    private ImageHandlerListener mVideoEditorHandler;
    private Callback mCallback;
    @IMenu
    private final int menu = IMenu.overlay;

    public OverLayHandler(ImageHandlerListener editorHandler, Callback callback) {
        mVideoEditorHandler = editorHandler;
        mCallback = callback;
        mLayerHandler = new LayerHandler(IMenu.overlay,editorHandler, new LayerHandler.Callback() {
            @Override
            public void prepared() {

            }

            @Override
            public void copy(CollageInfo base) { //复制叠加
//                Log.e(TAG, "copy: " + base);
                CollageInfo dst = new CollageInfo(base, true);
                RectF show = dst.getImageObject().getShowRectF();
                float offSet = show.centerX() >= 0.75f ? -0.05f : 0.05f;
                show.offset(offSet, offSet);
                dst.getImageObject().setShowRectF(show);
                mLayerHandler.insert(dst, true, false);
                mVideoEditorHandler.getParamHandler().addOverlay(dst, mCallback.isMainFragment());
            }

            @Override
            public void delete(DragMediaView view) { //删除叠加
//                Log.e(TAG, "delete: " + view);
                preDeleteStep();
                onDeleteOCancelMix(view);

                CollageInfo tmp = getTopMedia();
//                Log.e(TAG, "delete: " + tmp);
                if (null != tmp) {
                    edit(tmp, true);
                    mLayerHandler.initDragView(tmp, true, false);
                } else {
                    mLayerHandler.exitEditMode();
                    mCallback.delete();
                }
            }

            @Override
            public void onEdit(CollageInfo base) {
                Log.e(TAG, "onEdit: " + base);

            }

            @Override
            public void onAngleChanged() {
                mCallback.onAngleChanged();
            }
        });

    }

    /**
     * 删除
     */
    private boolean onDeleteOCancelMix(DragMediaView view) {
        boolean result = false;
        if (null != view) {
            mVideoEditorHandler.getContainer().removeView(view);
            view.recycle();
        }
        if (null != mLayerHandler.getCurrentCollageInfo()) {
            delete(mLayerHandler.getCurrentCollageInfo());
            mLayerHandler.reset();
            result = true;
        }
        return result;
    }

    /**
     * 删除
     */
    private void delete(CollageInfo info) {
        if (null != info) {
            LayerManager.remove(info);
            mVideoEditorHandler.getParamHandler().deleteOverlay(info);
            mVideoEditorHandler.getEditor().refresh();
        }

    }

    public CollageInfo getCurrentCollageInfo() {
        return mLayerHandler.getCurrentCollageInfo();
    }

    @Override
    public CollageInfo getTopMedia() {
        List<CollageInfo> list = mVideoEditorHandler.getParamHandler().getParam().getOverLayList();
        int len = list.size();
        if (len > 0) { //恢复最上层的dj
            return list.get(len - 1);
        }
        return null;
    }

    @Override
    public void offCenter() {
        mLayerHandler.offCenter();
    }

    @Override
    public void offLeft() {
        mLayerHandler.offLeft();
    }

    @Override
    public void offUp() {
        mLayerHandler.offUp();
    }

    @Override
    public void offDown() {
        mLayerHandler.offDown();
    }

    @Override
    public void offRight() {
        mLayerHandler.offRight();
    }

    @Override
    public void offLarge() {
        mLayerHandler.offLarge();
    }

    @Override
    public void offNarrow() {
        mLayerHandler.offNarrow();
    }

    @Override
    public void offFull() {
        mLayerHandler.offFull();
    }

    @Override
    public void setAngle(int angle) {
        mLayerHandler.setAngle(angle);
    }

    /**
     * 恢复最上层的前景
     */
    public CollageInfo restoreFg(boolean showDeleteButton) {
        CollageInfo info = getTopMedia();
        if (null != info) { //恢复最上层的图层
            edit(info, showDeleteButton);
        }
        return info;
    }

    private CollageInfo mLast;

    /**
     * 再次编辑
     *
     * @param info
     * @param showDeleteButton
     */
    public void edit(CollageInfo info, boolean showDeleteButton) {
        Log.e(TAG, "edit: " + showDeleteButton + " " + info);
        mLast = info.copy();
        mLayerHandler.edit(info, showDeleteButton, false);
        mCallback.restoreItem(info);
        mVideoEditorHandler.getParamHandler().onSaveAdjustStep(menu);
    }

    /**
     * 参数未发生变化，退出编辑|删除时， 删除记录的步骤
     */
    private void preDeleteStep() {
        if (mLayerHandler.isSameParam(mLast, mLayerHandler.getCurrentCollageInfo())) { //参数未发生变化
            mVideoEditorHandler.getParamHandler().onDeleteStep();
        }
        mLast = null;
    }

    /**
     * 退出编辑模式
     */
    public void exitEditMode() {
        preDeleteStep();

        mLayerHandler.exitEditMode();
        mLayerHandler.reset();
    }

    /**
     * 保存并退出
     */
    @Override
    public void exit2Main() {
        mLayerHandler.exit2Main();
    }

    public interface Callback {

        void restoreItem(CollageInfo uiData);

        void delete();

        /**
         * 是否在主界面
         *
         * @return
         */
        boolean isMainFragment();


        /**
         * 更改了旋转角度（UI需要同步做成响应）
         */
        void onAngleChanged();
    }
}
