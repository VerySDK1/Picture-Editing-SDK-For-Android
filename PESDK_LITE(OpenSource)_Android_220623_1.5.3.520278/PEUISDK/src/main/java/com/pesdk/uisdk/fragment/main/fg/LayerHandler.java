package com.pesdk.uisdk.fragment.main.fg;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;

import com.pesdk.uisdk.Interface.Ioff;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.code.Segment;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.export.LayerManager;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.pesdk.uisdk.widget.edit.DragMediaView;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.base.lib.utils.LogUtil;
import com.vecore.models.BlendParameters;
import com.vecore.models.FlipType;
import com.vecore.models.PEImageObject;

import java.util.List;

/**
 * 基础的仅控制单个图层对象（缩放、调位置） ,      不处理复制、删除相关逻辑 交由绑定的父handler
 */
public class LayerHandler implements Ioff {
    private static final String TAG = "LayerHandler";
    private ImageHandlerListener mVideoEditorHandler;
    private Callback mCallback;
    @IMenu
    private final int mMenu;

    private LayerHandler(@IMenu int menu) {
        mMenu = menu;
    }

    public LayerHandler(@IMenu int menu, ImageHandlerListener editorHandler, Callback callback) {
        this(menu);
        mVideoEditorHandler = editorHandler;
        mCallback = callback;
    }


    private CollageInfo mCurrentCollageInfo;

    @Override
    public CollageInfo getCurrentCollageInfo() {
        return mCurrentCollageInfo;
    }

    /**
     * 新增单个图层
     */
    public void onMixItemAdd(CollageInfo collageInfo, boolean showDeleteButton, boolean showEdit) {
        insert(collageInfo, showDeleteButton, showEdit);
        if (null != mCallback) {
            mCallback.prepared();
        }
    }

    private Handler mHandler = new Handler(message -> {
        switch (message.what) {
            default:
                break;
        }
        return false;
    });


    /**
     * 新增单个
     *
     * @param collageInfo
     */
    public void insert(CollageInfo collageInfo, boolean showDeleteButton, boolean showEdit) {
        initDragView(collageInfo, showDeleteButton, showEdit);
        LayerManager.insertCollage(collageInfo);
    }

    private DragMediaView dragView;

    /**
     * 构造拖拽方法
     *
     * @param collageInfo
     */
    public void initDragView(CollageInfo collageInfo, boolean showDelete, boolean showEdit) {
        exitEditMode();
        mCurrentCollageInfo = collageInfo;
        LogUtil.i(TAG, "initDragView:" + this + " item:" + collageInfo);
        ViewGroup tmp = mVideoEditorHandler.getPlayerContainer();
        int[] sizePlayer = new int[]{tmp.getWidth(), tmp.getHeight()};
        ViewGroup mLlContainer = mVideoEditorHandler.getContainer();
        int[] size = new int[]{mLlContainer.getWidth(), mLlContainer.getHeight()};
        RectF showRectF = collageInfo.getImageObject().getShowRectF();
        Rect rect = new Rect((int) (showRectF.left * size[0]), (int) (showRectF.top * size[1]), (int) (showRectF.right * size[0]), (int) (showRectF.bottom * size[1]));
        int dx = (sizePlayer[0] - size[0]) / 2;
        int dy = (sizePlayer[1] - size[1]) / 2;
        rect.offset(dx, dy);

        dragView = new DragMediaView(mVideoEditorHandler.getContainer().getContext(), -collageInfo.getImageObject().getShowAngle(), sizePlayer, rect, FlipType.FLIP_TYPE_NONE);
        dragView.setTranX(dx, dy);

        dragView.setShowControl(true);
        dragView.setControl(true);
        dragView.setShowDeleteButton(showDelete);
        dragView.setShowEditButton(showEdit);
        dragView.setDelListener(new DragMediaView.onDelListener() {
            @Override
            public void onDelete(DragMediaView view) {
                exitLine();
                mCallback.delete(view);
            }

            @Override
            public void onCopy() {
                exitLine();
                mHandler.postDelayed(() -> {
                    mCallback.copy(collageInfo);
                }, 50);

            }

            @Override
            public void onEdit(DragMediaView mediaView) {
                exitLine();
                mCallback.onEdit(collageInfo);
            }

        });
        dragView.setTouchListener(new DragMediaView.OnTouchListener() {


            @Override
            public void onClick(float x, float y) {
                Log.e(TAG, "onClick: " + x + "*" + y);
                exitLine();
                if (null == collageInfo) {
                    Log.e(TAG, "onClick: collageInfo is null");
                    return;
                }
                //1.找到当前point 对应最小边1/5 区域的一个正方形,最大100*100px， 若该正方形中的像素点有2/3 透明，则代表准备执行切换到另一个图层
                PEImageObject imageObject = collageInfo.getImageObject();
                String path = imageObject.getMediaPath();
                BlendParameters blendParameters = imageObject.getBlendParameters();
                boolean hasAlpha = false;
                if (new BlendParameters.Screen().equals(blendParameters)) { //叠加:验证黑色区域  todo 后续需排查画中画蒙版
                    //   叠加：OverlayHelper.setOverlay(imageObject);
                    Bitmap dst = LayerHandlerHelper.createCropBitmap(path, x, y);
                    if (null == dst) {
                        return;
                    }
                    hasAlpha = com.pesdk.uisdk.analyzer.internal.Util.hasBlack(dst, 2f / 3);
                    dst.recycle();
                } else { //画中画
                    if (collageInfo.getBG() != null) { //有背景图|背景颜色
                        hasAlpha = false;
                    } else { //无背景时
                        Bitmap dst = LayerHandlerHelper.createCropBitmap(path, x, y);
                        if (null == dst) {
                            return;
                        }
                        hasAlpha = com.pesdk.uisdk.analyzer.internal.Util.hasAlpha(dst, 2f / 3); //画中画自身
                        dst.recycle();
                        if (!hasAlpha) { //主图没有透明时,再排查有无背景人像、天空抠图
                            ImageOb ob = PEHelper.initImageOb(imageObject);
                            if (ob.getSegmentType() != Segment.NONE && FileUtils.isExist(ob.getMaskPath())) { //有mask
                                Bitmap tmp = LayerHandlerHelper.createCropBitmap(ob.getMaskPath(), x, y);
                                if (null == tmp) {
                                    return;
                                }
                                hasAlpha = com.pesdk.uisdk.analyzer.internal.Util.hasAlpha(tmp, 2f / 3);
                                tmp.recycle();
                            }
                        }
                    }
                }
                if (hasAlpha) { //当前点击的位置可认为是透明区域，需验证是否需要切换到其他资源
                    RectF rectF = imageObject.getShowRectF();
                    PointF tmp = new PointF(rectF.left + (rectF.width() * x), rectF.top + (rectF.height() * y));
                    autoSwtich(collageInfo, tmp);
                } else {//再次退出选中(防止满铺的画中画无法退出选中)
                    Log.e(TAG, "onClick: xxx");
                    onExitEdit();
                }
            }

            //容错
            final float TOLERANCE_PX = 10;
            final int TOLERANCE_TIME = 1000;
            //是否矫正
            boolean mDrawHor = false;
            boolean mDrawVer = false;
            //相隔时间
            long mHorTime = 0;
            long mVerTime = 0;

            @Override
            public void onRectChange() {
//                Log.e(TAG, "onRectChange: " + this + " " + dragView);
                if (null != dragView) {
                    //todo 记录，若位置改变是否需要存步骤
                    Rect rect = dragView.getSrcRect(); //在父容器的显示位置 px
                    //左右
                    boolean drawHor = Math.abs(rect.centerX() - sizePlayer[0] / 2) < TOLERANCE_PX;
                    if (drawHor && System.currentTimeMillis() - mHorTime < TOLERANCE_TIME) {
                        dragView.update(new PointF(0.5f, rect.centerY() * 1f / sizePlayer[1]));
                    } else {
                        mHorTime = System.currentTimeMillis();
                    }
                    mVideoEditorHandler.getLineView().drawHorLine(drawHor);


                    //上下
                    boolean drawVer = Math.abs(rect.centerY() - sizePlayer[1] / 2) < TOLERANCE_PX;
                    if (drawVer && System.currentTimeMillis() - mVerTime < TOLERANCE_TIME) {
                        dragView.update(new PointF(rect.centerX() * 1f / sizePlayer[0], 0.5f));
                    } else {
                        mVerTime = System.currentTimeMillis();
                    }

                    mVideoEditorHandler.getLineView().drawVerLine(drawVer);
                    if (((drawHor && !mDrawHor) || (drawVer && !mDrawVer))) {
                        //触发震动
                        Utils.onVibrator(dragView.getContext());
                    }
                    mDrawHor = drawHor;
                    mDrawVer = drawVer;


                    refresh(collageInfo);
                    if (null != mCallback) {
                        mCallback.onAngleChanged();
                    }
                }
            }


            @Override
            public void onExitEdit() {
//                Log.e(TAG, "onExitEdit: " + this);
                exit2Main(); //保存并退出
            }

            @Override
            public void onTouchUp() {
//                Log.e(TAG, "onTouchUp: " + this);
                exitLine();

                //保存下草稿，保证异常退出位置实时保存
                mVideoEditorHandler.getParamHandler().onSaveDraft(mMenu);
            }
        });


        mVideoEditorHandler.getPlayerContainer().addView(dragView);
        dragView.setId(collageInfo.getId());
    }

    /**
     * 保存并退出
     */
    public void exit2Main() {
        exitEditMode();
        reset(); //退出选中的
        mVideoEditorHandler.onExitSelect();//ui
    }


    /**
     * 是否可以切换到(下层资源)，若可以切换则自动切换 （切换规则，当前point,与其他资源的显示区域是否相交）
     * 其他资源：叠加、画中画(需验证画中画人像不带背景)
     *
     * @param exclude 要排除的item  todo 画中画人像透明
     * @param pointF  目标坐标
     */
    private void autoSwtich(CollageInfo exclude, PointF pointF) {
        List<CollageInfo> list = mVideoEditorHandler.getParamHandler().getParam().getOverLayList(); //多层叠加
        //1.遍历列表,寻找可切换的item
        CollageInfo dst = LayerHandlerHelper.getItem(exclude, pointF, list);
        if (dst != null) {//切换到其他叠加
            Log.e(TAG, "autoSwtich: overlay " + dst);
            //2.执行切换 叠加

            swtich2Exit(exclude);     //退出之前的对象
            mVideoEditorHandler.getOverLayerHandler().edit(dst, true); //编辑

            //3.响应到UI
            mVideoEditorHandler.onSelectedItem(IMenu.overlay, LayerHandlerHelper.getIndex(list, dst));
        } else {
            //无叠加,检索画中画
            list = mVideoEditorHandler.getParamHandler().getParam().getCollageList();
            dst = LayerHandlerHelper.getItem(exclude, pointF, list);
            Log.e(TAG, "autoSwtich: pip  " + dst);
            if (null != dst) {
                //2.执行切换 画中画
                swtich2Exit(exclude);
                mVideoEditorHandler.getForeground().edit(dst, true, true); //编辑

                //3.响应到UI
                mVideoEditorHandler.onSelectedItem(IMenu.pip, LayerHandlerHelper.getIndex(list, dst));
            }
        }
    }

    /**
     * 当前元素退出编辑
     *
     * @param exclude 正在编辑的对象
     */
    private void swtich2Exit(CollageInfo exclude) {
        if (exclude == mVideoEditorHandler.getOverLayerHandler().getCurrentCollageInfo()) {
            //退出叠加
            mVideoEditorHandler.getOverLayerHandler().exitEditMode();
        } else if (exclude == mVideoEditorHandler.getForeground().getCurrentCollageInfo()) {
            //退出画中画
            mVideoEditorHandler.getForeground().exitEditMode();
        } else {//可能是边框 todo
            exitEditMode();
        }
    }

    /**
     * 刷新显示位置
     */
    private void refresh(CollageInfo collageInfo) {
        RectF rectF = dragView.getSrcRectFInPlayer();
        int viewAngle = dragView.getRotateAngle();
        collageInfo.getImageObject().setShowAngle(-viewAngle);
        collageInfo.getImageObject().setShowRectF(rectF);

        PEImageObject bg = collageInfo.getBG();
        if (null != bg) { //景深
            bg.setShowAngle(-viewAngle);
            bg.setShowRectF(rectF);
            bg.refresh();
        }
        collageInfo.getImageObject().refresh();
    }


    /**
     * 删除
     *
     * @param info
     */
    public void delete(CollageInfo info) {
        if (null != info) {
            LayerManager.remove(info);
            mVideoEditorHandler.getVirtualImageInfo().getCollageInfos().remove(info);
            mVideoEditorHandler.getEditor().refresh();
        }

    }

    @Override
    public void offCenter() {
        if (null != dragView) {
            dragView.update(new PointF(0.5f, 0.5f));
            mHandler.postDelayed(() -> refresh(mCurrentCollageInfo), 100);
        }
    }

    @Override
    public void offLeft() {
        if (null != dragView) {
            dragView.offSet(-5, 0);
            postRefresh();
        }
    }

    @Override
    public void offUp() {
        if (null != dragView) {
            dragView.offSet(0, -5);
            postRefresh();
        }
    }

    @Override
    public void offDown() {
        if (null != dragView) {
            dragView.offSet(0, 5);
            postRefresh();
        }
    }

    @Override
    public void offRight() {
        if (null != dragView) {
            dragView.offSet(5, 0);
            postRefresh();
        }
    }

    private void postRefresh() {
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 25);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            refresh(mCurrentCollageInfo);
        }
    };

    @Override
    public void offLarge() {
        if (null != dragView) {
            boolean result = dragView.offsetScale(0.05f);
            if (result) {
                postRefresh();
            } else {
                Utils.autoToastNomal(dragView.getContext(), R.string.pesdk_scale_limit);
            }
        }
    }

    @Override
    public void offNarrow() {
        if (null != dragView) {
            boolean result = dragView.offsetScale(-0.05f);
            if (result) {
                postRefresh();
            } else {
                Utils.autoToastNomal(dragView.getContext(), R.string.pesdk_scale_limit);
            }
        }
    }

    @Override
    public void offFull() {
        if (null != dragView) { //保证两边铺满
            Rect rectF = dragView.getSrcRect();
            float scale = Math.max(mVideoEditorHandler.getContainer().getWidth() / (0.0f + rectF.width()), mVideoEditorHandler.getContainer().getHeight() / (0.0f + rectF.height()));
            if (scale > 1) {
                boolean result = dragView.offsetScale(scale - 1);
                if (result) {
                    postRefresh();
                } else {
                    Utils.autoToastNomal(dragView.getContext(), R.string.pesdk_scale_limit);
                }
            }
        }
    }

    @Override
    public void setAngle(int angle) {
        if (null != dragView) {
            dragView.setAngle(angle);
            refresh(mCurrentCollageInfo);
        }
    }


    /**
     * 再次编辑
     *
     * @param info
     * @param showDeleteButton
     */
    public void edit(CollageInfo info, boolean showDeleteButton, boolean showEdit) {
        initDragView(info, showDeleteButton, showEdit);
    }

    private void exitLine() {
        mVideoEditorHandler.getLineView().drawHorLine(false);
        mVideoEditorHandler.getLineView().drawVerLine(false);
    }

    /**
     * 退出编辑模式
     */
    public void exitEditMode() {
        exitLine();
        if (dragView != null) {
            mVideoEditorHandler.getPlayerContainer().removeView(dragView);
            dragView.recycle();
            dragView = null;
        }
    }

    public void reset() {
        mCurrentCollageInfo = null;
    }


    /***
     * 参数是否一致（显示位置&&旋转角度）
     * @param src
     * @param dst
     * @return true 没有发生变化
     */
    public boolean isSameParam(CollageInfo src, CollageInfo dst) {
        return PEHelper.isSameParam(src, dst);
    }

    public void unLockItem() {
        if (null != dragView) {
            dragView.setLock(false);
        }
    }

    public void lockItem() {
        if (null != dragView) {
            dragView.setLock(true);
        }
    }

    public void setUnavailable(boolean unable) {
        if (null != dragView) {
            dragView.setUnavailable(unable);
        }
    }


    public interface Callback {
        void prepared();

        void copy(CollageInfo base);

        void delete(DragMediaView view);

        void onEdit(CollageInfo base);

        /**
         * 更改了旋转角度（UI需要同步做成响应）
         */
        void onAngleChanged();

    }
}
