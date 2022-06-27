package com.pesdk.uisdk.edit;

import android.content.Context;
import android.graphics.RectF;

import com.pesdk.uisdk.Interface.Ioff;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.vecore.utils.MiscUtils;

/**
 * 处理字幕、贴纸 、图层、叠加  偏移位置
 */
public class OFFHandler implements Ioff {


    private WordInfoExt mWordInfoExt;
    private StickerInfo mStickerInfo;
    private Ioff mIoff; //前景微调|叠加
    private EditDragHandler mDragHandler;
    private ImageHandlerListener mListener;
    private Context mContext;

    public OFFHandler(Context context, EditDragHandler editDragHandler, ImageHandlerListener videoHandlerListener) {
        mContext = context;
        mDragHandler = editDragHandler;
        mListener = videoHandlerListener;

    }

    /**
     * 叠加|图层
     */
    public void onSelectedItem(Ioff ioff) {
        mIoff = ioff;
        mWordInfoExt = null;
        mStickerInfo = null;
    }


    /**
     * 选中文字
     */
    public void onSelectedItem(WordInfoExt wordNewInfo) {
        mWordInfoExt = wordNewInfo;
        mStickerInfo = null;
        mIoff = null;
    }

    /**
     * 素材
     */
    public void onSelectedItem(StickerInfo info) {
        mStickerInfo = info;
        mWordInfoExt = null;
        mIoff = null;
    }

    public void reset() {
        mStickerInfo = null;
        mWordInfoExt = null;
        mIoff = null;
    }

    /**
     * 改变样式后都要刷新
     */
    private void refresh() {
        if (mWordInfoExt != null) {
            mWordInfoExt.refresh(false);
            onSaveDraft();
        }
    }

    private int DEFAULT_TIME = 3000;
    private long mDraftTime = 0;
    /**
     * 保存过
     */
    private boolean mIsSaveStep = false;

    /**
     * 保存步骤
     */
    private void onSaveStep() {
        if (mIsSaveStep) {
            return;
        }
        mIsSaveStep = true;
        mListener.getParamHandler().onSaveStep(mContext.getString(R.string.pesdk_prompt_adjust), IMenu.text);
    }

    /**
     * 保存草稿
     */
    private void onSaveDraft() {
        if (System.currentTimeMillis() - mDraftTime > DEFAULT_TIME) {
            mDraftTime = System.currentTimeMillis();
            mListener.getParamHandler().onSaveDraft(IMenu.text);
        }
    }

    /**
     * 偏移贴纸  单位：px
     */
    private void offsetSticker(float dx, float dy) {
        RectF rectF = new RectF(mStickerInfo.getRectOriginal());
        rectF.offset(dx, dy);
        freshStickerImp(rectF);
    }

    /**
     * 刷新贴纸位置 单位：px
     */
    private void freshStickerImp(RectF rectF) {
        mStickerInfo.setCenterxy(new float[]{rectF.centerX() / (0.0f + mListener.getContainer().getWidth()), rectF.centerY() / (0.0f + mListener.getContainer().getHeight())});
        mStickerInfo.setRectOriginal(rectF);
        mDragHandler.addKeyframe(true);
    }

    @Override
    public void offCenter() {
        if (mWordInfoExt != null) {
            //中心点
            RectF showRect = mWordInfoExt.getShowRectF();
            float dstX = 0.5f - showRect.centerX();
            float dstY = 0.5f - showRect.centerY();
            onMove(dstX, dstY);
        } else if (mStickerInfo != null) {//中心点
            RectF showRect = mStickerInfo.getRectOriginal();
            int pw = mListener.getContainer().getWidth();
            int pH = mListener.getContainer().getHeight();
            showRect.offset(pw / 2 - showRect.centerX(), pH / 2 - showRect.centerY());
            freshStickerImp(onCenterSticker(pw, pH, showRect));
        } else if (null != mIoff) {
            mIoff.offCenter();
        }
    }

    private void onMove(float x, float y) {
        onSaveStep();
        RectF showRect = mWordInfoExt.getShowRectF();
        showRect.offset(x, y);
        mWordInfoExt.refreshShow(showRect);
        refresh();
        mDragHandler.onGetPosition(mListener.getCurrentPosition());
    }


    /**
     * 缩放增量
     */
    private void onZoomWord(float offDisf) {
        onSaveStep();
        mWordInfoExt.refreshShow(zoomRect(mWordInfoExt.getShowRectF(), offDisf));
        refresh();
        mDragHandler.onGetPosition(mListener.getCurrentPosition());
    }

    private RectF zoomRect(RectF showRect, float offsetScale) {
        float tmp = 1 + offsetScale;
        return MiscUtils.zoomRectF(showRect, tmp, tmp); //宽高、等比例放大
    }


    @Override
    public void offLeft() {
        if (null != mWordInfoExt) {//0~1.0f
            onMove(-0.01f, 0);
        } else if (null != mStickerInfo) { //贴纸单位: 像素
            offsetSticker(-5, 0);
        } else if (null != mIoff) {
            mIoff.offLeft();
        }
    }


    @Override
    public void offUp() {
        if (null != mWordInfoExt) {
            onMove(0, -0.01f);
        } else if (null != mStickerInfo) {
            offsetSticker(0, -5);
        } else if (null != mIoff) {
            mIoff.offUp();
        }
    }

    @Override
    public void offDown() {
        if (null != mWordInfoExt) {
            onMove(0, 0.01f);
        } else if (null != mStickerInfo) {
            offsetSticker(0, 5);
        } else if (null != mIoff) {
            mIoff.offDown();
        }


    }

    @Override
    public void offRight() {
        if (null != mWordInfoExt) {
            onMove(0.01f, 0);
        } else if (null != mStickerInfo) {
            offsetSticker(5, 0);
        } else if (null != mIoff) {
            mIoff.offRight();
        }

    }

    @Override
    public void offLarge() {
        if (null != mWordInfoExt) {
            onZoomWord(0.05f);
        } else if (null != mStickerInfo) {
            freshStickerImp(zoomRect(mStickerInfo.getRectOriginal(), 0.05f));
        } else if (null != mIoff) {
            mIoff.offLarge();
        }

    }

    @Override
    public void offNarrow() {
        if (null != mWordInfoExt) {
            onZoomWord(-0.05f);
        } else if (null != mStickerInfo) {
            freshStickerImp(zoomRect(mStickerInfo.getRectOriginal(), -0.05f));
        } else if (null != mIoff) {
            mIoff.offNarrow();
        }

    }

    @Override
    public void offFull() {
        if (null != mWordInfoExt) { //一边铺满 （需要缩放+平移）
            RectF showRect = mWordInfoExt.getShowRectF();
            int pw = mListener.getContainer().getWidth();
            int pH = mListener.getContainer().getHeight();
            float scale = Math.min(pw / (showRect.width() * pw), pH / (pH * showRect.height()));
            //宽高、等比例放大
            RectF rectF = onCenterWord(MiscUtils.zoomRectF(showRect, scale, scale));
            onSaveStep();
            mWordInfoExt.refreshShow(rectF);
            refresh();
            mDragHandler.onGetPosition(mListener.getCurrentPosition());
        } else if (null != mStickerInfo) {//一边铺满 （需要缩放+平移）
            RectF rectF = new RectF(mStickerInfo.getRectOriginal());
            int pw = mListener.getContainer().getWidth();
            int pH = mListener.getContainer().getHeight();
            float scale = Math.min(pw / rectF.width(), pH / rectF.height());
            rectF = MiscUtils.zoomRectF(rectF, scale, scale);//铺满
            freshStickerImp(onCenterSticker(pw, pH, rectF));
        } else if (null != mIoff) {
            mIoff.offFull();
        }

    }

    @Override
    public void setAngle(int angle) {
        if (null != mIoff) {
            mIoff.setAngle(angle);
        }
    }

    /**
     * 文字平移居中
     */
    private RectF onCenterWord(RectF rect) {
        float px = rect.centerX();
        float py = rect.centerY();
        rect.offset(0.5f - px, 0.5f - py);
        return rect;
    }

    /**
     * 贴纸居中，单位: 像素
     */
    private RectF onCenterSticker(int pW, int pH, RectF rect) {
        rect.offset(pW / 2 - rect.centerX(), pH / 2 - rect.centerY());
        return rect;
    }

    /**
     * 当前正在编辑的item
     *
     * @return
     */
    @Override
    public CollageInfo getCurrentCollageInfo() {
        return null != mIoff ? mIoff.getCurrentCollageInfo() : null;
    }
}
