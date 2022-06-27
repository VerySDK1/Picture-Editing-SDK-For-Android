package com.pesdk.uisdk.fragment.main.fg;

import android.graphics.RectF;

import com.pesdk.uisdk.analyzer.AnalyzerManager;
import com.pesdk.uisdk.bean.code.Segment;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.export.LayerManager;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.util.helper.ModelHelperImp;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.pesdk.uisdk.widget.edit.DragMediaView;
import com.vecore.models.PEImageObject;
import com.vecore.utils.MiscUtils;

import java.util.List;

/**
 * 前景-图层
 */
public class PipLayerHandler implements IFg {
    private static final String TAG = "PipLayerHandler";

    private LayerHandler mLayerHandler;
    private ImageHandlerListener mVideoEditorHandler;
    private Callback mCallback;
    @IMenu
    private final int menu = IMenu.pip;

    public PipLayerHandler(ImageHandlerListener editorHandler, Callback callback) {
        mVideoEditorHandler = editorHandler;
        mCallback = callback;
        mLayerHandler = new LayerHandler(menu,editorHandler, new LayerHandler.Callback() {
            @Override
            public void prepared() {

            }

            @Override
            public void copy(CollageInfo base) {
                CollageInfo dst = new CollageInfo(base, true);
                RectF show = dst.getImageObject().getShowRectF();
                float offSet = show.centerX() >= 0.75f ? -0.05f : 0.05f;
                show.offset(offSet, offSet);
                dst.getImageObject().setShowRectF(show);
                if (null != dst.getBG()) {
                    dst.getBG().setShowRectF(show);
                }
                mLayerHandler.insert(dst, true, true);
                mVideoEditorHandler.getParamHandler().addCollage(dst);
            }

            @Override
            public void delete(DragMediaView view) {
//                Log.e(TAG, "delete: " + view);
                preDeleteStep(); //是否需要删除一个临时步骤
                onDeleteOCancelMix(view);

                CollageInfo tmp = getTopMedia();
                if (null != tmp) { //此处不能创建临时步骤，避免上次的删除无法还原
                    boolean enableDelete = mVideoEditorHandler.getParamHandler().enablePipDeleteMenu();
                    mLayerHandler.initDragView(tmp, enableDelete, true);
                    mVideoEditorHandler.onSelectedItem(IMenu.pip, mVideoEditorHandler.getIndex(IMenu.pip, tmp.getId()));
                } else {
                    mLayerHandler.exitEditMode();
                }
                mCallback.delete();
            }

            @Override
            public void onEdit(CollageInfo src) {
                if (null == src) { //是否画中画抠像
                    return;
                }
                ImageOb imageOb = PEHelper.initImageOb(src.getImageObject());
//                LogUtil.i(TAG, "onEdit: src: " + src);
                new ModelHelperImp().checkAnalyzer(editorHandler.getContainer().getContext(), () -> {
                    mVideoEditorHandler.getParamHandler().onSaveAdjustStep(IMenu.pip);
                    imageOb.setSegment(imageOb.getSegmentType() != Segment.SEGMENT_PERSON ? Segment.SEGMENT_PERSON : Segment.NONE);
//                    Log.i(TAG, "onEdit: " + imageOb);
                    if (AnalyzerManager.getInstance().isRegistered(src)) {//仅更新单个
                        AnalyzerManager.getInstance().force();
                        src.getImageObject().refresh();
                    } else {
                        mVideoEditorHandler.reBuild(); //build原因:因为没有画中画实时插入时,未启用抠图时不会设置抠图回调。
                    }
                });
            }

            @Override
            public void onAngleChanged() {
                mCallback.onAngleChanged();
            }
        });
    }


    public CollageInfo getCurrentCollageInfo() {
        return mLayerHandler.getCurrentCollageInfo();
    }

    /**
     * 构造默认的显示位置
     *
     * @param mediaObject
     * @return
     */
    private RectF initDefaultLayerRect(PEImageObject mediaObject) {
        int pw = mVideoEditorHandler.getContainer().getWidth();
        int ph = mVideoEditorHandler.getContainer().getHeight();
        return initDefaultLayerRect(mediaObject, pw, ph);
    }


    /**
     * 构造默认的显示位置
     *
     * @param mediaObject
     * @return
     */
    public RectF initDefaultLayerRect(PEImageObject mediaObject, int vGroupWidth, int vGroupHeight) {
        RectF dst = new RectF();
        MiscUtils.fixShowRectF((mediaObject.getWidth() / (mediaObject.getHeight() + 0.0f)), vGroupWidth, vGroupHeight, dst);
        return dst;
    }

    /**
     * 新增单个图层
     */
    public void onMixItemAdd(PEImageObject mediaObject, boolean showDeleteButton, boolean showEdit) {
        mediaObject.setShowRectF(initDefaultLayerRect(mediaObject));
        CollageInfo collageInfo = new CollageInfo(mediaObject);
        mLayerHandler.onMixItemAdd(collageInfo, showDeleteButton, showEdit);
        mVideoEditorHandler.getParamHandler().addCollage(collageInfo);
        mVideoEditorHandler.getParamHandler().setEditMode(menu);
    }

    /**
     * 仅添加新增数据
     */
    public CollageInfo addItem(PEImageObject mediaObject) {
        mediaObject.setShowRectF(initDefaultLayerRect(mediaObject));
        CollageInfo collageInfo = new CollageInfo(mediaObject);
        LayerManager.insertCollage(collageInfo);

        mVideoEditorHandler.getParamHandler().addCollage(collageInfo);
        return collageInfo;
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
     *
     * @param info
     */
    public void delete(CollageInfo info) {
        if (null != info) {
            LayerManager.remove(info);
            mVideoEditorHandler.getParamHandler().deleteCollage(info);
            mVideoEditorHandler.getEditor().refresh();
        }

    }

    @Override
    public CollageInfo getTopMedia() {
        List<CollageInfo> list = mVideoEditorHandler.getParamHandler().getParam().getCollageList();
        int len = list.size();
        if (len > 0) { //恢复最上层的图层
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
    public CollageInfo restoreFg(boolean showDeleteButton, boolean showEdit) {
        CollageInfo info = getTopMedia();
        if (null != info) { //恢复最上层的图层
            edit(info, showDeleteButton, showEdit);
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
    public void edit(CollageInfo info, boolean showDeleteButton, boolean showEdit) {
        mLast = info.copy();
        mLayerHandler.edit(info, showDeleteButton, showEdit);
        mVideoEditorHandler.getParamHandler().onSaveAdjustStep(menu); //记录一个临时步骤
    }

    public void reEdit(CollageInfo info, boolean showDeleteButton, boolean showEdit) {
        mLast = null;
        mLayerHandler.edit(info, showDeleteButton, showEdit);
    }

    /**
     * 参数未发生变化，退出编辑|删除时， 删除记录的步骤
     */
    private void preDeleteStep() {
        if (null != mLayerHandler && null != mLast && mLayerHandler.isSameParam(mLast, mLayerHandler.getCurrentCollageInfo())) { //参数未发生变化
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
        mLayerHandler.exit2Main( );
    }

    /**
     * 编辑单个图层时，锁定图层的拖拽功能，隐藏图层上的按钮 （如：进入图层-滤镜时，隐藏图层中的按钮，仅保留拖动功能）
     */
    public void lockItem() {
        mLayerHandler.lockItem();
        preDeleteStep();
    }

    public void setUnavailable(boolean unavailable) {
        mLayerHandler.setUnavailable(unavailable);
    }

    /**
     * 解锁限制 (如：进入图层-滤镜时->保存退出时，恢复所有拖拽组件的点击事件)
     */
    public void unLockItem() {
        mLayerHandler.unLockItem();

    }

    public interface Callback {
        void prepared();

        void delete();

        /**
         * 更改了旋转角度（UI需要同步做成响应）
         */
        void onAngleChanged();
    }
}
