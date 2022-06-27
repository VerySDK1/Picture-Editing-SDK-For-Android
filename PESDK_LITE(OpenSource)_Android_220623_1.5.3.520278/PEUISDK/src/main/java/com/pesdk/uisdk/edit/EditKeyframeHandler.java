package com.pesdk.uisdk.edit;

import android.graphics.RectF;

import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.edit.listener.VideoKeyframeHandlerListener;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.uisdk.widget.edit.AreaInfo;
import com.pesdk.uisdk.widget.edit.EditDragView;
import com.vecore.BaseVirtual;
import com.vecore.models.AnimationObject;
import com.vecore.models.caption.CaptionItem;
import com.vecore.models.caption.CaptionLiteObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class EditKeyframeHandler implements VideoKeyframeHandlerListener {


    private final ImageHandlerListener mVideoHandlerListener;

    /**
     * 两个节点之间至少相隔 100ms
     */
    public static final int MIN_DIFFERENCE = 100;
    /**
     * 同步core中的动画逻辑， 保证至少有一段字幕处理静止的状态 ,
     */
    public static final int CAPTION_SHOWLINE = Utils.s2ms(BaseVirtual.CAPTION_STATIC_SHOW_MIN_DURATION);

    /**
     * 输出大小
     */
    private final BaseVirtual.Size mOutputSize = new BaseVirtual.Size(0, 0);
    /**
     * 没有角度旋转 显示区域
     */
    private RectF mRectF = new RectF();
    /**
     * 旋转角度
     */
    private int mAngle = 0;

    public EditKeyframeHandler(ImageHandlerListener videoHandlerListener) {
        mVideoHandlerListener = videoHandlerListener;
    }

    @Override
    public void setWordExtProgress(int progress, EditDragView dragView, WordInfoExt info) {
        if (mVideoHandlerListener != null && info != null && dragView != null
                && progress >= info.getStart() && progress <= info.getEnd()) {
            //输出大小
            int width = mVideoHandlerListener.getContainer().getWidth();
            int height = mVideoHandlerListener.getContainer().getHeight();
            //其他区域
            if (info.getCaption().isCaptionLite() && !info.isHideDashed()) {
                ArrayList<CaptionItem> captionAll = info.getCaption().getCaptionAll();
                if (captionAll != null) {
                    ArrayList<AreaInfo> areaList = new ArrayList<>();
                    for (CaptionItem item : captionAll) {
                        areaList.add(new AreaInfo(item.getShowRect(), item.getAngle()));
                    }
                    dragView.setOtherAreaList(areaList);
                    dragView.setOtherShow(true);
                } else {
                    dragView.setOtherShow(false);
                }
            } else {
                dragView.setOtherShow(false);
            }

            //关键帧
            List<AnimationObject> keyFrameAnimateList = info.getCaption().getKeyFrameAnimateList();
            if (keyFrameAnimateList == null || keyFrameAnimateList.size() <= 0) {
                //显示区域
                RectF showRectF = info.getShowRectF();
                RectF rectF = new RectF(showRectF.left * width,
                        showRectF.top * height,
                        showRectF.right * width,
                        showRectF.bottom * height);
                dragView.setData(rectF, info.getAngle());

                //设置显示位置和角度
                RectF show = new RectF(
                        rectF.left / width, rectF.top / height,
                        rectF.right / width, rectF.bottom / height
                );
                info.setShowRectF(show);
                info.getCaption().setRotateCaption(info.getAngle());
            } else {
                AnimationObject start = null;
                AnimationObject end = null;
                AnimationObject upTime = null;
                for (int i = 0; i < keyFrameAnimateList.size(); i++) {
                    float atTime = keyFrameAnimateList.get(i).getAtTime() + Utils.ms2s(info.getStart());

                    //误差
                    if (Math.abs(Utils.s2ms(atTime) - progress) < MIN_DIFFERENCE / 2.0f) {
                        end = keyFrameAnimateList.get(i);
                        RectF dst = new RectF();
                        dst.left = end.getRectPosition().left * mOutputSize.width;
                        dst.right = end.getRectPosition().right * mOutputSize.width;
                        dst.top = end.getRectPosition().top * mOutputSize.height;
                        dst.bottom = end.getRectPosition().bottom * mOutputSize.height;
                        dragView.setData(dst, end.getRotate());
                        return;
                    }

                    if (Utils.ms2s(progress) < atTime) {
                        end = keyFrameAnimateList.get(i);
                        start = upTime;
                        break;
                    }
                    upTime = keyFrameAnimateList.get(i);
                }
                if (start != null && end != null) {
                    RectF startRctf = start.getRectPosition();
                    RectF endRctf = end.getRectPosition();
                    float factor = (Utils.ms2s(progress - info.getStart()) - start.getAtTime())
                            / (end.getAtTime() - start.getAtTime());
                    mOutputSize.set(width, height);
                    //计算现在位置
                    RectF dst = new RectF();
                    dst.left = (startRctf.left + (endRctf.left - startRctf.left) * factor) * mOutputSize.width;
                    dst.right = (startRctf.right + (endRctf.right - startRctf.right) * factor) * mOutputSize.width;
                    dst.top = (startRctf.top + (endRctf.top - startRctf.top) * factor) * mOutputSize.height;
                    dst.bottom = (startRctf.bottom + (endRctf.bottom - startRctf.bottom) * factor) * mOutputSize.height;
                    float dstAngle = start.getRotate() + (end.getRotate() - start.getRotate()) * factor;

                    dragView.setData(dst, dstAngle);

                    //设置显示位置和角度
                    RectF showRectF = new RectF(
                            dst.left / mOutputSize.width, dst.top / mOutputSize.height,
                            dst.right / mOutputSize.width, dst.bottom / mOutputSize.height
                    );
                    info.setShowRectF(showRectF);
                    info.getCaption().setRotateCaption(dstAngle);
                }
            }
        }
    }

    /**
     * 贴纸
     */
    @Override
    public void setStickerProgress(int progress, EditDragView dragView, StickerInfo info) {
        if (mVideoHandlerListener != null && info != null && dragView != null) {
            ArrayList<CaptionLiteObject> captionLiteObjects = info.getList();
            if (captionLiteObjects.size() > 0) {
                //计算显示位置
                CaptionLiteObject object = captionLiteObjects.get(0);
                List<AnimationObject> animationObjects = object.getAnimationList();
                if (animationObjects != null && animationObjects.size() > 0) {
                    AnimationObject start = null;
                    AnimationObject end = null;
                    AnimationObject upTime = null;

                    for (int i = 0; i < animationObjects.size(); i++) {
                        float atTime =0;

                        //误差
                        if (Math.abs(Utils.s2ms(atTime) - progress) < MIN_DIFFERENCE / 2.0f) {
                            end = animationObjects.get(i);
                            RectF dst = new RectF();
                            dst.left = end.getRectPosition().left * mOutputSize.width;
                            dst.right = end.getRectPosition().right * mOutputSize.width;
                            dst.top = end.getRectPosition().top * mOutputSize.height;
                            dst.bottom = end.getRectPosition().bottom * mOutputSize.height;
                            dragView.setData(dst, -end.getRotate());
                            info.setRectOriginal(dst);
                            info.setRotateAngle(-end.getRotate());
                            return;
                        }

                        if (Utils.ms2s(progress) < atTime) {
                            end = animationObjects.get(i);
                            start = upTime;
                            break;
                        }
                        upTime = animationObjects.get(i);
                    }
                    if (start != null && end != null) {
                        RectF startRctf = start.getRectPosition();
                        RectF endRctf = end.getRectPosition();
                        float factor = 1f;
                        mOutputSize.set(mVideoHandlerListener.getContainer().getWidth(),
                                mVideoHandlerListener.getContainer().getHeight());
                        //计算现在位置
                        RectF dst = new RectF();
                        dst.left = (startRctf.left + (endRctf.left - startRctf.left) * factor) * mOutputSize.width;
                        dst.right = (startRctf.right + (endRctf.right - startRctf.right) * factor) * mOutputSize.width;
                        dst.top = (startRctf.top + (endRctf.top - startRctf.top) * factor) * mOutputSize.height;
                        dst.bottom = (startRctf.bottom + (endRctf.bottom - startRctf.bottom) * factor) * mOutputSize.height;
                        float dstAngle = start.getRotate() + (end.getRotate() - start.getRotate()) * factor;

                        dragView.setData(dst, -dstAngle);
                        info.setRectOriginal(dst);
                        info.setRotateAngle(-dstAngle);
                        return;
                    }
                }
            }
            mOutputSize.set(mVideoHandlerListener.getContainer().getWidth(),
                    mVideoHandlerListener.getContainer().getHeight());
            dragView.setData(info.getRectOriginal(), info.getRotateAngle());
            info.setRectOriginal(info.getRectOriginal());
            info.setRotateAngle(info.getRotateAngle());
        }
    }

}
