package com.pesdk.uisdk.edit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.edit.listener.IDragHandlerListener;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.fragment.main.IndexHelper;
import com.pesdk.uisdk.fragment.sticker.StickerExportHandler;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.listener.PreviewPositionListener;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.uisdk.widget.edit.DragBorderLineView;
import com.pesdk.uisdk.widget.edit.EditDragView;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.models.AnimationObject;
import com.vecore.models.FlipType;
import com.vecore.models.caption.CaptionLiteObject;
import com.vecore.models.internal.LabelStatusUpdatedIntent;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制拖拽： 字幕、
 */
public class EditDragHandler implements PreviewPositionListener, IDragHandlerListener {
    /**
     * 当前编辑的贴纸
     */
    private StickerInfo mStickerInfo;
    /**
     * 当前编辑的文字
     */
    private WordInfoExt mWordInfoExt;
    private CaptionBroadcastReceiver mReceiver;
    private final ImageHandlerListener mListener;
    private final EditKeyframeHandler mKeyframeHandler;
    private Context mContext;
    private static final String TAG = "EditDragHandler";
    /**
     * 媒体拖动
     */
    private EditDragView mDragView;
    private DragBorderLineView mDragBorderView;
    /**
     * 是否改变
     */
    private boolean mIsChange = false;
    /**
     * 预览模式
     */
    private boolean mIsPreview = false;

    public ImageHandlerListener getListener() {
        return mListener;
    }

    public EditDragHandler(Context context) {
        mContext = context;
        mListener = (ImageHandlerListener) context;
        mKeyframeHandler = new EditKeyframeHandler(mListener);
    }


    /**
     * 设置
     */
    public void setDragView(EditDragView dragView, DragBorderLineView border) {
        mDragView = dragView;
        mDragBorderView = border;
        mDragView.setListener(new EditDragView.OnDragListener() {
            //容错
            final int TOLERANCE_PIXEL = CoreUtils.dpToPixel(5);
            final int TOLERANCE_TIME = 1300;
            //相隔时间
            long mHorTime = 0;
            long mVerTime = 0;
            //是否矫正
            boolean mDrawHor = false;
            boolean mDrawVer = false;

            long time = 0;
            int clickNum = 0;

            @Override
            public void onClickOther(int position) {
                Log.e(TAG, "onClickOther: " + position);
            }

            @Override
            public void onClick(boolean in, float x, float y) {
                Log.e(TAG, "onClick: " + in + " " + x + "*" + y);
                if (in) {
                    //判断双击
                    if (System.currentTimeMillis() - time < 300 || time == 0) {
                        clickNum++;
                        if (clickNum >= 2) {
                            clickNum = 0;
                            onEdit();
                        }
                    } else {
                        clickNum = 0;
                    }
                    time = System.currentTimeMillis();
                } else { //没有点击到当前字幕、贴纸框内
                    mFrameListener.onClickPosition(x, y);
                }
            }

            @Override
            public void onDelete() {
                Log.e(TAG, "onDelete: " + this);
                //删除有很多 根据当前模式和选中删除
                if (null != mWordInfoExt) {
                    int id = mWordInfoExt.getId();
                    onSave();
                    WordInfoExt word = new IndexHelper(mListener).getWord(id);
                    mListener.getParamHandler().deleteWordNewInfo2(word);
                    word.getCaption().removeListLiteObject();
                    //播放器内容改变,刷新播放器
                    mListener.getEditor().refresh();
                    mFrameListener.onDeleted(IMenu.text, id);
                } else if (null != mStickerInfo) {
                    int id = mStickerInfo.getId();
                    onSave();
                    StickerInfo sticker = new IndexHelper(mListener).getSticker(id);
                    if (mListener.getParamHandler().deleteSticker2(sticker)) {
                        sticker.removeListLiteObject(mListener.getEditorImage());
                        mListener.getEditor().refresh();
                    }
                    mFrameListener.onDeleted(IMenu.sticker, id);
                }
            }

            @Override
            public void onAlign(int align) {
                Log.e(TAG, "onAlign: " + align);
                if (mWordInfoExt != null) {
                    mWordInfoExt.getCaptionItem().setAlignment(align, align);
                }
            }

            @Override
            public void onCopy() {
                Log.e(TAG, "onCopy: " + this);
                if (null != mWordInfoExt) {
                    int id = mWordInfoExt.getId();
                    onSave();
                    mDragBorderView.postDelayed(() -> {
                        WordInfoExt base = new IndexHelper(mListener).getWord(id);
                        WordInfoExt tmp = base.copy();
                        tmp.setId(Utils.getWordId());
                        RectF src = new RectF(base.getShowRectF());
                        src.offset(0.05f, 0.05f);
                        tmp.setShowRectF(src);
                        tmp.refreshMeasuring();
                        mListener.getEditor().refresh();
                        int index = mListener.getParamHandler().addWordNewInfo(tmp);
                        edit(index, IMenu.text, true, true);
                    }, 100);

                } else if (null != mStickerInfo) {
                    int id = mStickerInfo.getId();
                    onSave();
                    mDragBorderView.postDelayed(() -> { //保存草稿保存
                        StickerInfo base = new IndexHelper(mListener).getSticker(id);
                        StickerInfo tmp = base.copy();
                        tmp.setId(Utils.getWordId());
                        tmp.recycle();
                        RectF src = new RectF(base.getRectOriginal());
                        src.offset(20, 20);
                        tmp.setCenterxy(new float[]{src.centerX() / (getWidth() + 0.0f), src.centerY() / (getHeight() + 0.0f)});
                        tmp.setRectOriginal(src);
                        Log.e(TAG, "onCopy: >>>dst: " + tmp);
                        refreshStickerRect(tmp);
                        int index = mListener.getParamHandler().addSticker(tmp, true);
                        edit(index, IMenu.sticker);
                    }, 100);
                }
            }

            @Override
            public void onEdit() { //编辑替换为镜像
                Log.e(TAG, "onEdit: " + this);
                if (mStickerInfo != null) {//这里仅有贴纸有镜像
                    mListener.getParamHandler().onSaveAdjustStep(IMenu.sticker);
                    if (mStickerInfo.getFlipType() != FlipType.FLIP_TYPE_HORIZONTAL) {
                        mStickerInfo.setFlipType(FlipType.FLIP_TYPE_HORIZONTAL);
                    } else {
                        mStickerInfo.setFlipType(FlipType.FLIP_TYPE_NONE);
                    }
                    refreshStickerRect(mStickerInfo);
                } else if (mWordInfoExt != null) {
                    mFrameListener.onEdit(true);
                }
            }

            @Override
            public boolean onRectChange(RectF rectF, float angle) {
                //设置显示位置和角度
                RectF mShowRectF = new RectF(rectF.left / getWidth(), rectF.top / getHeight(), rectF.right / getWidth(), rectF.bottom / getHeight());
//                Log.e(TAG, "onRectChange: " + mShowRectF);
                dragBorder(mShowRectF);
                if (mStickerInfo != null) {
                    //设置角度
                    mStickerInfo.setCenterxy(new float[]{mShowRectF.centerX(), mShowRectF.centerY()});
                    mStickerInfo.setRectOriginal(rectF);
                    mStickerInfo.setRotateAngle(angle);
                    addKeyframe(true);
                } else if (mWordInfoExt != null) {
                    mWordInfoExt.setAngle(angle);
                    mWordInfoExt.refreshShow(mShowRectF);
                    List<AnimationObject> list = mWordInfoExt.getCaption().getKeyFrameAnimateList();
                    if (list != null && list.size() > 0) {
//                        addKeyframe(true);
                    } else {
                        //刷新
                        mWordInfoExt.refresh(false);
                    }
                }
                return true;
            }

            /**
             * 中心线组件
             */
            private void dragBorder(RectF showRectF) {
                if (mDragBorderView == null) {
                    return;
                }
                int widthContainer = mListener.getContainer().getWidth();
                int heightContainer = mListener.getContainer().getHeight();
                //中心点
                PointF point = mDragView.getCenter();
                //左右
                int width = mDragView.getWidth();

//                Log.e(TAG, "dragBorder: " + mDragView.getWidth() + "*" + mDragView.getHeight() + "   >group:" + widthContainer + "*" + heightContainer);


                int centerX = width / 2;
                boolean drawHor = Math.abs(point.x - centerX) < TOLERANCE_PIXEL;
                if (drawHor && System.currentTimeMillis() - mHorTime < TOLERANCE_TIME) {
                    showRectF.set((1 - showRectF.width()) / 2, showRectF.top,
                            (1 + showRectF.width()) / 2, showRectF.bottom);
                    mDragView.setMoveShowRect(showRectF.left * widthContainer,
                            showRectF.top * heightContainer,
                            showRectF.right * widthContainer,
                            showRectF.bottom * heightContainer);
                } else {
                    mHorTime = System.currentTimeMillis();
                }
                mDragBorderView.drawHorLine(drawHor);
                //上下
                int height = mDragView.getHeight();
                int centerY = height / 2;
                boolean drawVer = Math.abs(point.y - centerY) < TOLERANCE_PIXEL;
                if (drawVer && System.currentTimeMillis() - mVerTime < TOLERANCE_TIME) {
                    showRectF.set(showRectF.left, (1 - showRectF.height()) / 2,
                            showRectF.right, (1 + showRectF.height()) / 2);
                    mDragView.setMoveShowRect(showRectF.left * widthContainer,
                            showRectF.top * heightContainer,
                            showRectF.right * widthContainer,
                            showRectF.bottom * heightContainer);
                } else {
                    mVerTime = System.currentTimeMillis();
                }
                mDragBorderView.drawVerLine(drawVer);

                if (((drawHor && !mDrawHor) || (drawVer && !mDrawVer))) {
                    //触发震动
                    Utils.onVibrator(mContext);
                }
                mDrawHor = drawHor;
                mDrawVer = drawVer;
            }

            @Override
            public void onTouchDown() {
                Log.e(TAG, "onTouchDown: " + this);
//                mListener.onVideoPause();
//                if (mWordInfo != null) {
//                    if (mWordInfo.getAnimList() != null && mWordInfo.getAnimList().size() > 0) {
//                        mListener.onSeekTo((int) ((mWordInfo.getEnd() + mWordInfo.getStart()) / 2), true);
//                    }
//                } else if (mMOInfo != null) {
//                    if (!mIsChange) {
//                        mIsChange = true;
//                        //保存步骤
//                        mListener.getParamHandler().onSaveAdjustStep(MODE_MASK);
//                    }
//                }
            }

            @Override
            public void onTouchUp() {
                Log.e(TAG, "onTouchUp: " + this);

                int menu = IMenu.MODE_PREVIEW;
                if (null != mWordInfoExt) {
                    menu = IMenu.text;
                } else if (null != mStickerInfo) {
                    menu = IMenu.sticker;
                }
                //所有的都需要关键帧
                addKeyframe(true);
                if (mDragBorderView != null) {
                    mDragBorderView.drawHorLine(false);
                    mDragBorderView.drawVerLine(false);
                }
                //保存下草稿，保证异常退出位置实时保存
                mListener.getParamHandler().onSaveDraft(menu);
            }

            @Override
            public void onExitEdit() {
                Log.e(TAG, "onExitEdit: " + this);
                //响应退出编辑
//                if (null != getEditWord()) { //点击字幕空白区域，退出编辑
//                    onSave();
//                    mFrameListener.onExitEdit();
//                }else if(null!=getEditSticker()){
//                    onSave();
//                    mFrameListener.onExitEdit();
//                }
                onSave();
                mFrameListener.onExitEdit();
            }

            @Override
            public float getWidth() {
                return mListener.getContainer().getWidth();
            }

            @Override
            public float getHeight() {
                return mListener.getContainer().getHeight();
            }

        });
    }


    public WordInfoExt getEditWord() {
        return mWordInfoExt;
    }

    public StickerInfo getEditSticker() {
        return mStickerInfo;
    }

    /**
     * 注册字幕
     */
    public void registeredCaption() {
        Log.e(TAG, "registeredCaption: " + mWordInfoExt.getCaption().isAutoSize());
        if (mWordInfoExt != null && !mWordInfoExt.getCaption().isAutoSize()) {
            mReceiver = new CaptionBroadcastReceiver();
            String registered = mWordInfoExt.getCaption().registered(mReceiver);
            mReceiver.setAction(registered);
        }
    }

    @Override
    public void onGetPosition(int position) {
        //时间进度
        if (mDragView == null) {
            return;
        }
        if (mIsPreview) {
            //预览中
            mDragView.setVisibility(View.GONE);
            return;
        }
        //新版字幕
        if (mWordInfoExt != null) {
            if (mWordInfoExt.getStart() <= position && mWordInfoExt.getEnd() >= position) {
                if (mDragView.getVisibility() != View.VISIBLE) {
                    mDragView.setVisibility(View.VISIBLE);
//                    mFrameListener.onKeyframe(true);
                }
                mKeyframeHandler.setWordExtProgress(position, mDragView, mWordInfoExt);
            } else {
                if (mDragView.getVisibility() == View.VISIBLE) {
                    mDragView.setVisibility(View.GONE);
//                    mFrameListener.onKeyframe(false);
                }
            }
            return;
        }


        //马赛克
//        if (mMOInfo != null) {
//            //马赛克
//            MOInfo moInfo = mListener.getParamHandler().getMOInfo(mIndex);
//            if (mMOInfo != null && moInfo != null) {
//                if (moInfo.getStart() != mMOInfo.getStart() || moInfo
//                        .getEnd() != mMOInfo.getEnd()) {
//                    mMOInfo.setTimelineRange(moInfo.getStart(),
//                            moInfo.getEnd(), true);
//                    //实时刷新
//                    mListener.getEditor().refresh();
//                }
//                if (moInfo.getStart() <= position && moInfo
//                        .getEnd() >= position) {
//                    if (mDragView.getVisibility() == View.INVISIBLE) {
//                        mDragView.setVisibility(View.VISIBLE);
//                    }
//                } else {
//                    if (mDragView.getVisibility() == View.VISIBLE) {
//                        mDragView.setVisibility(View.INVISIBLE);
//                    }
//                }
//            }
//            return;
//        }

        //控制显示位置
        if (mStickerInfo == null) {
            mDragView.setVisibility(View.GONE);
            return;
        }
        if (mDragView.getVisibility() != View.VISIBLE) {
            mDragView.setVisibility(View.VISIBLE);
            mFrameListener.onKeyframe(true);
        }
        mKeyframeHandler.setStickerProgress(position, mDragView, mStickerInfo);

    }

    @Override
    public boolean onSave() {
        boolean saved = false;
        if (mWordInfoExt != null) {
            mListener.getParamHandler().onSaveDraft(IMenu.text);
            unRegisteredCaption();
            saved = true;
        } else if (mStickerInfo != null) {
            mListener.getParamHandler().onSaveDraft(IMenu.sticker);
            saved = true;
        }
        mWordInfoExt = null;
        mStickerInfo = null;
        mReceiver = null;

        exitDragView();
        return saved;
    }

    /**
     * 恢复编辑前的字幕
     */
    public void exitEditWord() {
        if (mWordInfoExt != null) {
            unRegisteredCaption();
        }
        mWordInfoExt = null;
        exitDragView();
    }


    private void exitDragView() {
        if (mDragView != null) {
            mDragView.setVisibility(View.GONE);
            mDragView.setOtherShow(false);
        }
    }


    /**
     * 取消注册字幕
     */
    public void unRegisteredCaption() {
        if (mReceiver != null) {
            if (mWordInfoExt != null) {
                mWordInfoExt.getCaption().unRegistered(mReceiver);
            }
        }
        mReceiver = null;
    }

    /**
     * 切换字幕时
     */
    public BroadcastReceiver getReceiver() {
        if (mWordInfoExt != null) {
            if (mReceiver != null) {
                unRegisteredCaption();
            }
            mReceiver = new CaptionBroadcastReceiver();
        }
        return mReceiver;
    }

    @Override
    public void addKeyframe(boolean update) {
        if (mStickerInfo != null) {//贴纸
            freshSticker(mStickerInfo);
            onGetPosition(mListener.getCurrentPosition());
        } else if (mWordInfoExt != null) {//新版字幕
            freshWordExt(mWordInfoExt);
            onGetPosition(mListener.getCurrentPosition());
        }
    }

    /**
     * 贴纸
     */
    private void freshSticker(StickerInfo info) {
        if (info == null) {
            return;
        }
        if (!mIsChange) {
            mIsChange = true;
            //保存步骤
            mListener.getParamHandler().onSaveAdjustStep(IMenu.sticker);
        }
        refreshStickerRect(info);
    }


    public void refreshStickerRect(StickerInfo info) {
        ArrayList<CaptionLiteObject> list = info.getList();
        int width = mListener.getContainer().getWidth();
        int height = mListener.getContainer().getHeight();
        if (list == null || list.size() == 0) {
            //设置新的liteObject
            new StickerExportHandler(mContext, info, width, height)
                    .export(mListener.getEditorImage());
        } else {
            RectF tmpRect = info.getRectOriginal();
            RectF show = new RectF(tmpRect.left / width, tmpRect.top / height, tmpRect.right / width, tmpRect.bottom / height);
            for (CaptionLiteObject object : list) {
                object.setShowRectF(new RectF(show));
                object.setAngle(-(int) (info.getRotateAngle()));
                //实时插入新的lite对象到虚拟视频
                mListener.getEditorImage().fastCaptionLite(object);
            }
            mListener.getEditor().refresh();
        }
    }

    /**
     * 刷新字幕
     */
    public void freshWordExt(WordInfoExt info) {
        if (info == null) {
            return;
        }
        Log.e(TAG, "freshWordExt: " + mIsChange + " " + info.getText());
        if (!mIsChange) {
            mIsChange = true;
            //保存步骤
            mListener.getParamHandler().onSaveAdjustStep(IMenu.text);
        }

        info.refresh(false);
    }

    @Override
    public void onPreview(boolean preview) {
        if (mIsPreview != preview) {
            mIsPreview = preview;
            onGetPosition(mListener.getCurrentPosition());
        }
    }

    @Override
    public boolean edit(int index, int mode) {
        return edit(index, mode, false, false);
    }

    /**
     * 文字新增时，不需要copy按钮
     *
     * @param index
     * @param mode
     * @param enableCopy
     * @return
     */
    @Override
    public boolean edit(int index, int mode, boolean enableCopy, boolean enableEdit) {
        Log.e(TAG, "edit: " + index + " " + mode + " enableCopy: " + enableCopy);
        mFrameListener.exitOtherSelectLayer();

        if (mode == IMenu.text) {
            return editWordNew(index, enableCopy, enableEdit);
        } else if (mode == IMenu.sticker) {
            return editSticker(index);
        }
        return false;
    }

    /**
     * 新版字幕
     */
    private boolean editWordNew(int index, boolean enableCopy, boolean enableEdit) {
        //保存上一个
        onSave();
        WordInfoExt wordInfo = mListener.getParamHandler().getWordNewInfo(index);
        if (wordInfo == null) {
            Log.e(TAG, "editWordNew: error: " + index);
            return false;
        }
        //控制显示
        mDragView.setCtrRotation(true);
        mDragView.setCtrDelete(true);
        mDragView.setCtrCopy(enableCopy);
        mDragView.setCtrEdit(enableEdit);
        mDragView.setControl(false);
        mDragView.setEnabledAngle(true);
        mDragView.setEnabledProportion(false);
        mDragView.onCaption();

        mIsChange = false;
        mWordInfoExt = wordInfo;
        mWordInfoExt.setVirtualVideo(mListener.getEditorImage(), mListener.getEditor());

        //固定大小 需要注册
        registeredCaption();

        //设置显示位置
        onGetPosition(mListener.getCurrentPosition());

        mListener.onSelectedItem(IMenu.text, index);
        return true;
    }


    /**
     * 编辑贴纸
     */
    private boolean editSticker(int index) {
        onSave();
        StickerInfo stickerInfo = mListener.getParamHandler().getStickerInfo(index);
        if (stickerInfo == null) {
            Log.e(TAG, "editSticker: error :" + index);
            return false;
        }
        mIsChange = false;
        mStickerInfo = stickerInfo;
        mDragView.setCtrRotation(true);
        mDragView.setCtrDelete(true);
        mDragView.setCtrCopy(true);
        mDragView.setCtrEdit(true);
        mDragView.setControl(false);
        mDragView.setEnabledAngle(true);
        mDragView.setEnabledProportion(false);
        mDragView.onSticker();

        //设置显示位置
        onGetPosition(mListener.getCurrentPosition());
        mListener.onSelectedItem(IMenu.sticker, index);
        return true;
    }


    /**
     * 注册
     */
    class CaptionBroadcastReceiver extends BroadcastReceiver {

        String action;

        public void setAction(String action) {
            this.action = action;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent instanceof LabelStatusUpdatedIntent) {
                if (mWordInfoExt != null) {
                    mWordInfoExt.refreshSize();
                } else {
                    return;
                }
                onGetPosition(mListener.getCurrentPosition());
            }
        }
    }


    private OnEditFrameListener mFrameListener;

    public void setFrameListener(OnEditFrameListener frameListener) {
        mFrameListener = frameListener;
    }

    public interface OnEditFrameListener {

        /**
         * 删除
         */
        void onDeleted(@IMenu int menu, int id);


        /**
         * 编辑 字幕
         */
        void onEdit(boolean text);

        /**
         * 点击位置
         */
        void onClickPosition(float x, float y);


        /**
         * 显示与隐藏
         */
        void onKeyframe(boolean show);


        /**
         * 退出编辑，点击空白区域，退出编辑
         */
        void onExitEdit();

        /**
         * 选中编辑文字、贴纸时，退出 图层、叠加的控制逻辑
         */
        void exitOtherSelectLayer();

    }

}
