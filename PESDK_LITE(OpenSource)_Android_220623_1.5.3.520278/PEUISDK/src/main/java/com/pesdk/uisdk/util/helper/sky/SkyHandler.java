package com.pesdk.uisdk.util.helper.sky;

import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.widget.edit.DragMediaView;
import com.vecore.models.PEImageObject;

/**
 * 天空|背景 位置 (仅调整位置，暂无位置微调)
 */
public class SkyHandler {
    private static final String TAG = "SkyHandler";

    private SkyRectHandler mSkyRectHandler;
    private ImageHandlerListener mVideoEditorHandler;

    public SkyHandler(ImageHandlerListener editorHandler, Callback callback) {
        mVideoEditorHandler = editorHandler;
        mSkyRectHandler = new SkyRectHandler(editorHandler, new SkyRectHandler.Callback() {
            @Override
            public void prepared() {

            }

            @Override
            public void copy(PEImageObject base) { //复制叠加
//                CollageInfo dst = new CollageInfo(base, true);
//                RectF show = dst.getImageObject().getShowRectF();
//                float offSet = show.centerX() >= 0.75f ? -0.05f : 0.05f;
//                show.offset(offSet, offSet);
//                dst.getImageObject().setShowRectF(show);
//                mLayerHandler.insert(dst, true, false);
//                mVideoEditorHandler.getParamHandler().addOverlay(dst, mCallback.isMainFragment());
            }

            @Override
            public void delete(DragMediaView view) { //删除叠加
//                onDeleteOCancelMix(view);
//                CollageInfo tmp = getTopMedia();
//                if (null != tmp) {
//                    restoreFg(true);
//                    mLayerHandler.initDragView(tmp, true, false);
//                }
//                mCallback.delete();
            }
        });

    }


    /**
     * 移除旧的画中画
     */
    public void remove() {
        mSkyRectHandler.remove();
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
//        if (null != mLayerHandler.getCurrentCollageInfo()) {
//            delete(mLayerHandler.getCurrentCollageInfo());
//            mLayerHandler.reset();
//            result = true;
//        }
        return result;
    }

//    /**
//     * 删除
//     */
//    public void delete(CollageInfo info) {
////        if (null != info) {
////            LayerManager.remove(info);
////            mVideoEditorHandler.getParamHandler().deleteOverlay(info);
////            mVideoEditorHandler.getEditor().refresh();
////        }
//
//    }

    public PEImageObject getCurrentBg() {
        return mSkyRectHandler.getPEImageObject();
    }

//    @Override
//    public CollageInfo getTopMedia() {
////        List<CollageInfo> list = mVideoEditorHandler.getParamHandler().getParam().getOverLayList();
////        int len = list.size();
////        if (len > 0) { //恢复最上层的dj
////            return list.get(len - 1);
////        }
//        return null;
//    }
//
//    @Override
//    public void offCenter() {
//        mLayerHandler.offCenter();
//    }
//
//    @Override
//    public void offLeft() {
//        mLayerHandler.offLeft();
//    }
//
//    @Override
//    public void offUp() {
//        mLayerHandler.offUp();
//    }
//
//    @Override
//    public void offDown() {
//        mLayerHandler.offDown();
//    }
//
//    @Override
//    public void offRight() {
//        mLayerHandler.offRight();
//    }
//
//    @Override
//    public void offLarge() {
//        mLayerHandler.offLarge();
//    }
//
//    @Override
//    public void offNarrow() {
//        mLayerHandler.offNarrow();
//    }
//
//    @Override
//    public void offFull() {
//        mLayerHandler.offFull();
//    }

//    /**
//     * 恢复最上层的前景
//     */
//    public CollageInfo restoreFg(boolean showDeleteButton) {
////        CollageInfo info = getTopMedia();
////        if (null != info) { //恢复最上层的图层
////            edit(info, showDeleteButton);
////        }
////        return info;
//        return null;
//    }

    /**
     * 再次编辑
     *
     * @param info
     * @param showDeleteButton
     */
    public void edit(PEImageObject info, boolean showDeleteButton) {
        mSkyRectHandler.edit(info, showDeleteButton, false);
    }

    /**
     * 退出编辑模式
     */
    public void exitEditMode() {
        mSkyRectHandler.exitEditMode();
        mSkyRectHandler.reset();
    }


    public interface Callback {
        void prepared();

        void delete();

        /**
         * 是否在主界面
         *
         * @return
         */
        boolean isMainFragment();

    }
}
