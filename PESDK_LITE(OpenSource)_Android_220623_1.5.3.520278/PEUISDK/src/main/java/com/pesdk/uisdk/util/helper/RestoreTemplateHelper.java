package com.pesdk.uisdk.util.helper;

import android.content.Context;
import android.graphics.RectF;

import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.edit.EditDataHandler;
import com.pesdk.uisdk.fragment.sticker.StickerExportHandler;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.io.File;
import java.util.List;

/**
 * 恢复模板中的文字、贴纸，需要首次build之后再恢复，再build   （即第一次需要build两次）
 */
public class RestoreTemplateHelper {


    public void restoreTemplate(Context context, VirtualImage virtualImage, VirtualImageView virtualImageView, int width, int height, EditDataHandler mDataHandler, Callback callback) {

        ThreadPoolUtils.executeEx(new ThreadPoolUtils.ThreadPoolRunnable() {
            @Override
            public void onBackground() {
                restoreTemplateData(context, virtualImage, virtualImageView, mDataHandler.getParam().getWordList(), mDataHandler.getParam().getStickerList(), mDataHandler.getParam().getOverLayList(), width, height);
//                ArrayList<WordInfoExt> wordExtList = mDataHandler.getParam().getWordList();
                //下载动画
//                if (wordExtList != null && wordExtList.size() > 0) {
//                    for (WordInfoExt info : wordExtList) {
//                        AnimInfo recoverInAnim = info.getRecoverInAnim();
//                        if (recoverInAnim != null) {
//                            boolean downloaded = recoverInAnim.isDownloaded();
//                            if (!downloaded) {
//                                mDownAnimList.add(recoverInAnim);
//                            }
//                        }
//                        AnimInfo recoverOutAnim = info.getRecoverOutAnim();
//                        if (recoverOutAnim != null) {
//                            boolean downloaded = recoverOutAnim.isDownloaded();
//                            if (!downloaded) {
//                                mDownAnimList.add(recoverOutAnim);
//                            }
//                        }
//                    }
//                }

            }

            @Override
            public void onEnd() {
                super.onEnd();
                callback.prepared();
            }
        });
    }

    public void restoreTemplate(Context context, VirtualImage virtualImage, VirtualImageView virtualImageView, int width, int height,
                                List<WordInfoExt> wordExtList, List<StickerInfo> stickerInfo,
                                List<CollageInfo> overLayList, Callback callback) {

        ThreadPoolUtils.executeEx(new ThreadPoolUtils.ThreadPoolRunnable() {
            @Override
            public void onBackground() {
                restoreTemplateData(context, virtualImage, virtualImageView, wordExtList, stickerInfo, overLayList, width, height);
            }

            @Override
            public void onEnd() {
                super.onEnd();
                callback.prepared();
            }
        });
    }


    public static interface Callback {

        void prepared();
    }

    /**
     * 模板恢复
     *
     * @param context
     * @param virtualVideo
     * @param virtualVideoView
     * @param wordInfoExts     文字
     * @param stickerInfos     贴纸
     * @param collageInfos     叠加
     * @param width
     * @param height
     */
    private void restoreTemplateData(Context context, VirtualImage virtualVideo, VirtualImageView virtualVideoView, List<WordInfoExt> wordInfoExts, List<StickerInfo> stickerInfos,
                                     List<CollageInfo> collageInfos, int width, int height) {

        CommonStyleUtils.init(width, height);

        //贴纸
        if (stickerInfos != null && stickerInfos.size() > 0) {
            for (StickerInfo info : stickerInfos) {
                //样式
                StyleInfo styleInfo = StickerUtils.getInstance().getStyleInfo(info.getStyleId(), info.getResourceId());
                if (styleInfo != null) {
                    File config = new File(styleInfo.mlocalpath, CommonStyleUtils.CONFIG_JSON);
                    CommonStyleUtils.getConfig(config, styleInfo);
                    info.setStyleId(styleInfo.pid);
                }
                RectF rectOriginal = info.getRectOriginal();
                info.setRectOriginal(new RectF(rectOriginal.left * width, rectOriginal.top * height,
                        rectOriginal.right * width, rectOriginal.bottom * height));
                info.setPreviewAsp(width * 1.0f / height);
                info.setParent(width, height);
            }
            new StickerExportHandler(context, stickerInfos, width, height).export(virtualVideo);
        }

//        //马赛克
//        ArrayList<MOInfo> maskList = handler.getMaskList();
//        if (maskList != null && maskList.size() > 0) {
//            for (MOInfo moInfo : maskList) {
//                try {
//                    moInfo.getObject().setVirtualVideo(virtualVideo, virtualVideoView);
//                    moInfo.getObject().setParentSize(width, height);
//                    moInfo.getObject().quitEditCaptionMode(true);
//                } catch (InvalidArgumentException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        //新版字幕
        if (wordInfoExts != null && wordInfoExts.size() > 0) {
            for (WordInfoExt info : wordInfoExts) {
                info.setVirtualVideo(virtualVideo, virtualVideoView);
                info.setPreviewAsp(width * 1.0f / height);
                info.recoverBubble();
            }
        }

//        //字幕模板
//        ArrayList<WordTemplateInfo> wordTemplateList = handler.getWordTemplateList();
//        if (wordTemplateList != null && wordTemplateList.size() > 0) {
//            Iterator<WordTemplateInfo> iterator = wordTemplateList.iterator();
//            while (iterator.hasNext()) {
//                WordTemplateInfo info = iterator.next();
//                info.setVirtualVideo(virtualVideo, virtualVideoView);
//                info.setPreviewAsp(width * 1.0f / height);
//                //样式
//                String localPath = info.getLocalPath();
//                SubTemplateInfo temp = null;
//                if (!TextUtils.isEmpty(localPath)) {
//                    temp = SubTemplateManager.getInstance().parsingConfig(info.getLocalPath());
//                }
//                if (temp == null) {
//                    iterator.remove();
//                } else {
//                    info.recoverTemplate(temp);
//                }
//
//                if (info.isBindScene() && scenes != null) {
//                    String identifierScene = info.getIdentifierScene();
//                    for (Scene scene : scenes) {
//                        if (identifierScene.equals(scene.identifier)) {
//                            info.setApplyScene(scene, true);
//                            break;
//                        }
//                    }
//                }
//            }
//        }

//        //片尾
//        WordInfo endingText = handler.getEndingText();
//        if (endingText != null) {
//            try {
//                RectF showRectF = endingText.getCaptionObject().getShowRectF();
//                endingText.getCaptionObject().setVirtualVideo(virtualVideo, virtualVideoView);
//                //样式
//                StyleInfo styleInfo = SubUtils.getInstance().getStyleInfo(endingText.getStyleId());
//                if (styleInfo != null) {
//                    File config = new File(styleInfo.mlocalpath, CommonStyleUtils.CONFIG_JSON);
//                    CommonStyleUtils.getConfig(config, styleInfo);
//                    double tmp = styleInfo.srcHeight;
//                    if (tmp == 0) {
//                        //使用config.json中定义的宽高，避免图片
//                        BitmapFactory.Options op = new BitmapFactory.Options();
//                        op.inJustDecodeBounds = true;
//                        BitmapFactory.decodeFile(styleInfo.frameArray.valueAt(0).pic, op);
//                        tmp = op.outHeight;
//                    }
//                    // 根据高来修正缩放参数（兼容单行无限拉伸）
//                    float disf = (float) (showRectF.height() * height / tmp);
//                    endingText.setDisf(disf);
//                    Utils.setCaptionStyle(styleInfo, endingText);
//                }
//                endingText.getCaptionObject().setCenter(new PointF(showRectF.centerX(), showRectF.centerY()));
//                endingText.getCaptionObject().apply();
//            } catch (InvalidArgumentException e) {
//                e.printStackTrace();
//            }
//        }

        //叠加
//        if (null != collageInfos) {
//            for (CollageInfo info : collageInfos) {
//                PEImageObject peImageObject = info.getImageObject();
//                float asp = width / (height + 0.0f);
//                RectF dst = new RectF();
//
////                RectF showRectF = ResultHelper.fixPipShowRectF(srcShowRectF, clipRectF, mVirtualImageFragment.getSubEditorParent());
////                MiscUtils.fixShowRectFByExpanding(peImageObject.getWidth() / (peImageObject.getHeight() + 0.0f), 720, (int) (720 / asp), dst);
////                peImageObject.setShowRectF(dst);
////                peImageObject.setBlendParameters(new BlendParameters.Screen());
//
//
//            }
//
//        }


    }
}
