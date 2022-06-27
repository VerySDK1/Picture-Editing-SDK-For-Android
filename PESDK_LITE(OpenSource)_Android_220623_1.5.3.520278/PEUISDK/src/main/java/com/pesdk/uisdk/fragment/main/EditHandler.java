package com.pesdk.uisdk.fragment.main;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.widget.FrameLayout;

import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.edit.EditDragHandler;
import com.pesdk.uisdk.listener.IEditCallback;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.vecore.base.lib.ui.PreviewFrameLayout;

import java.util.List;

/**
 * 拆分事件
 */
public class EditHandler {
    private static final String TAG = "EditHandler";
    private FrameLayout mLinearWords;
    private MenuCallback mMenuCallback;
    private PreviewFrameLayout mPreviewFrameLayout;
    private EditDragHandler mEditDragHandler;
    private IEditCallback mEditCallback;
    private IndexHelper mIndexHelper;
    private ImageHandlerListener mVideoHandlerListener;

    public EditHandler(FrameLayout linearWords, IEditCallback editCallback, MenuCallback callback, ImageHandlerListener listener,
                       PreviewFrameLayout previewFrameLayout, EditDragHandler dragHandler) {
        mLinearWords = linearWords;
        mMenuCallback = callback;
        mEditCallback = editCallback;
        mPreviewFrameLayout = previewFrameLayout;
        mEditDragHandler = dragHandler;
        mVideoHandlerListener = listener;
        mIndexHelper = new IndexHelper(listener);
    }


    /**
     * 编辑按钮（文字、贴纸）
     */
    public void onEdit(boolean text) {
        if (text) {   //文字
            WordInfoExt tmp = mEditDragHandler.getEditWord();
            if (null != tmp) {
                mMenuCallback.onText(tmp);
            }
        } else {    //贴纸
            if (mEditCallback.getMenu() != IMenu.sticker) {
                mMenuCallback.onSticker(mEditDragHandler.getEditSticker());
            }
        }
    }

    public void setClickRange(@IClickRange int clickRange) {
        mClickRange = clickRange;
    }

    @IClickRange
    private int mClickRange = IClickRange.range_all;

    /**
     * 点击屏幕任意位置，响应元素编辑
     *
     * @param downX
     * @param downY
     */
    public boolean onClickVideo(float downX, float downY) {
        Log.e(TAG, "onClickVideo: " + downX + "*" + downY + " mClickRange:" + mClickRange);
        //验证顺序： 字幕|贴纸 |涂鸦 >叠加  >图层
        if (clickJudgeText(downX, downY) && mClickRange == IClickRange.range_all) {
            exitOverPip();
            return true;
        } else if (clickJudgeDoodle(true) && mClickRange == IClickRange.range_all) {
            return true;
        } else if (clickJudgeOverlay(downX, downY) && (mClickRange == IClickRange.range_overlay || mClickRange == IClickRange.range_all)) {   //选中叠加
            mEditDragHandler.onSave();
            return true;
        } else if (clickJudgePIP(downX, downY) && mClickRange == IClickRange.range_all) {   //选中图层
            mEditDragHandler.onSave();
            return true;
        } else {
            Log.e(TAG, "onClickVideo: un select item ");
            //退出选中状态: 保存当前修改
            if (mEditDragHandler.onSave()) { //文字、贴纸
                return false;
            } else if (exitOverPip()) {
                mVideoHandlerListener.onSelectedItem(IMenu.MODE_PREVIEW, -1);
                return true;
            } else {
                //切换为：啥都未选中
                mVideoHandlerListener.onExitSelect();
                return false;
            }
        }
    }

    /**
     * 退出图层、叠加
     */
    public boolean exitOverPip() {
        if (exitOverLay()) { //退出叠加
            return true;
        } else if (exitPip()) { //退出图层
            return true;
        } else if (exitBG()) { //退出天空|背景
            return true;
        } else {
            return false;
        }
    }

    private boolean exitOverLay() {
        if (mEditCallback.getOverLayHandler().getCurrentCollageInfo() != null) { //退出叠加
            Log.i(TAG, "exitOverLay: overlay: " + mEditCallback.getOverLayHandler().getCurrentCollageInfo());
            mEditCallback.getOverLayHandler().exitEditMode();
            return true;
        } else {
            return false;
        }
    }

    private boolean exitPip() {
        if (mEditCallback.getPipLayerHandler().getCurrentCollageInfo() != null) { //退出图层
            Log.i(TAG, "exitPip: pip: " + mEditCallback.getPipLayerHandler().getCurrentCollageInfo());
            mEditCallback.getPipLayerHandler().exitEditMode();
            return true;
        } else {
            return false;
        }
    }

    private boolean exitBG() {
        if (mEditCallback.getSkyHandler().getCurrentBg() != null) { //退出天空|背景
            Log.i(TAG, "exitBG: sky:" + mEditCallback.getSkyHandler().getCurrentBg());
            mEditCallback.getSkyHandler().exitEditMode();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 点击判断字幕
     */
    private boolean clickJudgeText(float downX, float downY) {
        //贴纸
        List<StickerInfo> stickerInfos = mEditCallback.getEditDataHandler().getParam().getStickerList();
        for (int i = stickerInfos.size() - 1; i >= 0; i--) {
            StickerInfo info = stickerInfos.get(i);
            //先判断是否在当前时间内 判断点击的位置
            RectF rect = info.getRectOriginal();
            Matrix matrix = new Matrix();
            matrix.setRotate(-info.getRotateAngle(), rect.centerX(), rect.centerY());
            float[] dst = new float[2];
            float[] src = new float[]{downX, downY};
            matrix.mapPoints(dst, src);
            if (rect.contains((int) dst[0], (int) dst[1])) {
                setSelectId(info.getId(), IMenu.sticker);
                return true;
            }
        }


        float x = downX / mLinearWords.getWidth();
        float y = downY / mLinearWords.getHeight();

        //新版文字
        List<WordInfoExt> wordNewList = mEditCallback.getEditDataHandler().getParam().getWordList();  //倒序验证
        Log.e(TAG, "clickJudgeText: " + wordNewList.size());
        for (int i = wordNewList.size() - 1; i >= 0; i--) {
            WordInfoExt info = wordNewList.get(i);
            //先判断是否在当前时间内 判断点击的位置
            RectF rectF = info.getShowRectF();
            Matrix matrix = new Matrix();
            matrix.setRotate(-info.getAngle(), rectF.centerX(), rectF.centerY());
            float[] dst = new float[2];
            float[] src = new float[]{downX / mPreviewFrameLayout.getWidth(), downY / mPreviewFrameLayout.getHeight()};
            matrix.mapPoints(dst, src);
            if (rectF.contains(x, y)) {
                setSelectId(info.getId(), IMenu.text);
                return true;
            } else {
                //取消选中

            }
        }

        return false;
    }

    /**
     * 点击画中画判断
     */
    private boolean clickJudgePIP(float downX, float downY) {
        //画中画
        List<CollageInfo> collageInfos = mEditCallback.getEditDataHandler().getParam().getCollageList();  //倒序验证
        boolean hasItem = false;
        for (int i = collageInfos.size() - 1; i >= 0; i--) {
            CollageInfo info = collageInfos.get(i);
            //先判断是否在当前时间内 判断点击的位置
            RectF rectF = info.getImageObject().getShowRectF();
            Matrix matrix = new Matrix();
            matrix.setRotate(-info.getImageObject().getAngle(), rectF.centerX(), rectF.centerY());
            float[] dst = new float[2];
            float[] src = new float[]{downX / mPreviewFrameLayout.getWidth(), downY / mPreviewFrameLayout.getHeight()};
            matrix.mapPoints(dst, src);
            if (rectF.contains(dst[0], dst[1])) {
                exitOverLay();//交叉退出
                setSelectId(info.getId(), IMenu.pip);
                hasItem = true;
                break;
            }
        }
        if (!hasItem) { //没有画中画
            exitOverPip();
        }
        return hasItem;
    }

    /**
     * 叠加点击
     */
    private boolean clickJudgeOverlay(float downX, float downY) {
        List<CollageInfo> collageInfos = mEditCallback.getEditDataHandler().getParam().getOverLayList();  //倒序验证
        boolean hasItem = false;
        for (int i = collageInfos.size() - 1; i >= 0; i--) {
            CollageInfo info = collageInfos.get(i);
            //先判断是否在当前时间内 判断点击的位置
            RectF rectF = info.getImageObject().getShowRectF();
            Matrix matrix = new Matrix();
            matrix.setRotate(-info.getImageObject().getAngle(), rectF.centerX(), rectF.centerY());
            float[] dst = new float[2];
            float[] src = new float[]{downX / mPreviewFrameLayout.getWidth(), downY / mPreviewFrameLayout.getHeight()};
            matrix.mapPoints(dst, src);
//            Log.e(TAG, "clickJudgeOverlay: " + " " + rectF + " " + Arrays.toString(src) + " dst:" + Arrays.toString(dst));
            if (rectF.contains(dst[0], dst[1])) {
                exitOverPip(); //交叉退出
                setSelectId(info.getId(), IMenu.overlay);
                hasItem = true;
                break;
            }
        }
        if (!hasItem) {
            exitOverLay();
        }
        return hasItem;
    }


    /**
     * 点击判断涂鸦    最后判断 只根据时间判断
     */
    private boolean clickJudgeDoodle(boolean preview) {
        //涂鸦
//        List<GraffitiInfo> graffitiInfos = mEditCallback.getEditDataHandler().getParam().getGraffitList();  //倒序验证
//        for (int i = graffitiInfos.size() - 1; i >= 0; i--) {
//            GraffitiInfo info = graffitiInfos.get(i);
//            //先判断是否在当前时间内 判断点击的位置
//            if (preview) {
////                    onClickMenu($(R.id.rb_doodle));
//            }
//            return true;
//        }
        return false;
    }

    public void setSelectId(int id) {
        int index = -1;
        int mode = -1;
        if (id != -1) {
            if ((index = mIndexHelper.getWordIndex(id)) >= 0) {
                mode = IMenu.text;
            } else if ((index = mIndexHelper.getStickerIndex(id)) >= 0) {
                mode = IMenu.sticker;
            }
        }
        Log.e(TAG, "setSelectId: " + id + " " + index + " " + mode);
        onClickDataIndex(index, mode);
    }


    /**
     * 设置选中
     */
    public void setSelectId(int id, @IMenu int mode) {
        int index = getIndex(mode, id);
        Log.e(TAG, "setSelectId: " + index + " src:" + id + " mode:" + mode);
        onClickDataIndex(index, mode);
    }

    public int getIndex(@IMenu int mode, int id) {
        int index = -1;
        if (mode == IMenu.text) {
            index = mIndexHelper.getWordIndex(id);
        } else if (mode == IMenu.sticker) {
            index = mIndexHelper.getStickerIndex(id);
        } else if (mode == IMenu.overlay) {
            index = mIndexHelper.getOverLayIndex(id);
        } else if (mode == IMenu.pip) {
            index = mIndexHelper.getPipIndex(id);
        }
        return index;
    }

    public void setSelectIndex(int index, @IMenu int menu) {
        onClickDataIndex(index, menu);
    }

    /**
     * 设置选中
     */
    private void onClickDataIndex(int index, int type) {
        Log.e(TAG, "onClickDataIndex: " + index + " " + type);
        if (index != -1) {
            //判断是否在当前时间内 移动到最近的时间内
            if (type == IMenu.text) {
                if (mEditCallback.getEditDragHandler().edit(index, type, true, true)) {
                }
            } else if (type == IMenu.sticker) {
                if (mEditCallback.getEditDragHandler().edit(index, type, false, false)) {
                }
            } else if (type == IMenu.overlay) {
                CollageInfo tmp = mEditCallback.getEditDataHandler().getOverLay(index);
                if (tmp == null) {
                    Log.e(TAG, "onClickDataIndex: " + type + " index:" + index);
                    return;
                }
                mEditCallback.getOverLayHandler().edit(tmp, true);
                mVideoHandlerListener.onSelectedItem(type, index);
            } else if (type == IMenu.pip) {
                CollageInfo tmp = mEditCallback.getEditDataHandler().getPip(index);
                if (tmp == null) {
                    Log.e(TAG, "onClickDataIndex: " + type + " index:" + index);
                    return;
                }
                boolean enableDelete = mEditCallback.getEditDataHandler().enablePipDeleteMenu();
                mEditCallback.getPipLayerHandler().edit(tmp, enableDelete, true);//先设置选中Item
                mVideoHandlerListener.onSelectedItem(type, index);//后UI
            }
        } else { //退出编辑模式
            mVideoHandlerListener.preMenu();
        }
    }


}
