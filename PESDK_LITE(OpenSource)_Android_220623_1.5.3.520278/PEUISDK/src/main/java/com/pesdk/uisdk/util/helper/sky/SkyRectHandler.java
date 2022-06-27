package com.pesdk.uisdk.util.helper.sky;

import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.widget.edit.DragMediaView;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.models.FlipType;
import com.vecore.models.PEImageObject;

/**
 * 仅控制单个天空（缩放、调位置） , 不处理复制、删除相关逻辑
 */
class SkyRectHandler {
    private static final String TAG = "LayerHandler";
    private ImageHandlerListener mVideoEditorHandler;
    private Callback mCallback;

    public SkyRectHandler(ImageHandlerListener editorHandler, Callback callback) {
        mVideoEditorHandler = editorHandler;
        mCallback = callback;
    }


    private PEImageObject mPEImageObject;


    public PEImageObject getPEImageObject() {
        return mPEImageObject;
    }

    /**
     * 移除旧的画中画
     */
    public void remove() {
//        if (mCurrentCollageInfo != null) {
//            LayerManager.remove(mCurrentCollageInfo);
//        }
    }


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case MSG_REPLACE: //替换
//                    updata();
//                    mCollageAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private final int MSG_REPLACE = 121;

//    /**
//     * 新增单个
//     *
//     * @param object
//     */
//    public void insert(PEImageObject object, boolean showDeleteButton, boolean showEdit) {
//        initDragView(object, showDeleteButton, showEdit);
//    }

    private DragMediaView dragView;

    /**
     * 构造拖拽方法
     *
     * @param peImageObject
     */
    public void initDragView(PEImageObject peImageObject, boolean showDelete, boolean showEdit) {
        exitEditMode();
        mPEImageObject = peImageObject;
        int[] size = new int[]{mVideoEditorHandler.getContainer().getWidth(), mVideoEditorHandler.getContainer().getHeight()};
        RectF showRectF = peImageObject.getShowRectF();
        Rect rect = new Rect((int) (showRectF.left * size[0]), (int) (showRectF.top * size[1]), (int) (showRectF.right * size[0]), (int) (showRectF.bottom * size[1]));
        Log.e(TAG, "initDragView: " + size + " rect:" + rect);
        dragView = new DragMediaView(mVideoEditorHandler.getContainer().getContext(), -peImageObject.getShowAngle(), size, rect, FlipType.FLIP_TYPE_NONE);
        dragView.setShowControl(true);
        dragView.setControl(true);
        dragView.setShowDeleteButton(showDelete);
        dragView.setShowEditButton(showEdit);
        dragView.setDelListener(new DragMediaView.onDelListener() {
            @Override
            public void onDelete(DragMediaView view) {
                mCallback.delete(view);
            }

            @Override
            public void onCopy() {
//                mHandler.postDelayed(() -> {
//                    mCallback.copy(collageInfo);
//                }, 50);

            }

            @Override
            public void onEdit(DragMediaView mediaView) {
//                EditLayerActivity.onLayerEdit(mActivity, collageInfo.getMediaObject(), EditActivity.RC_LAYER_EDIT);
            }

        });
        dragView.setTouchListener(new DragMediaView.OnTouchListener() {
            private long nLastVibTime = 0;
            private int nLastX = 0, nLastY;
            private final int OFF_PIXIL = CoreUtils.dpToPixel(10); //容错20*n像素


            @Override
            public void onClick(float x, float y) {

                Log.e(TAG, "onClick: " + x + "*" + y + " " + peImageObject);
            }

            @Override
            public void onRectChange() {
                if (null != dragView) {
                    refresh(peImageObject);
                }
            }

            @Override
            public void onExitEdit() {
                exitEditMode();
            }

            @Override
            public void onTouchUp() {

            }
        });
        mVideoEditorHandler.getContainer().addView(dragView);
        dragView.setId(peImageObject.hashCode());
    }

    /**
     * 刷新显示位置
     */
    private void refresh(PEImageObject imageObject) {
        RectF rectF = dragView.getSrcRectFInPlayer();
        int viewAngle = dragView.getRotateAngle();
        imageObject.setShowAngle(-viewAngle);
        imageObject.setShowRectF(rectF);
        Log.e(TAG, "refresh: " + viewAngle + " " + rectF);
        imageObject.refresh();
    }


//
//    @Override
//    public void offCenter() {
//        if (null != dragView) {
//            dragView.update(new PointF(0.5f, 0.5f));
//            mHandler.postDelayed(() -> refresh(mPEImageObject), 100);
//        }
//    }
//
//    @Override
//    public void offLeft() {
//        if (null != dragView) {
//            dragView.offSet(-5, 0);
//            postRefresh();
//        }
//    }
//
//    @Override
//    public void offUp() {
//        if (null != dragView) {
//            dragView.offSet(0, -5);
//            postRefresh();
//        }
//    }
//
//    @Override
//    public void offDown() {
//        if (null != dragView) {
//            dragView.offSet(0, 5);
//            postRefresh();
//        }
//    }
//
//    @Override
//    public void offRight() {
//        if (null != dragView) {
//            dragView.offSet(5, 0);
//            postRefresh();
//        }
//    }

    private void postRefresh() {
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 30);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            refresh(mPEImageObject);
        }
    };

//    @Override
//    public void offLarge() {
//        if (null != dragView) {
//            boolean result = dragView.offsetScale(0.05f);
//            if (result) {
//                postRefresh();
//            } else {
//                Utils.autoToastNomal(dragView.getContext(), R.string.pesdk_scale_limit);
//            }
//        }
//    }
//
//    @Override
//    public void offNarrow() {
//        if (null != dragView) {
//            boolean result = dragView.offsetScale(-0.05f);
//            if (result) {
//                postRefresh();
//            } else {
//                Utils.autoToastNomal(dragView.getContext(), R.string.pesdk_scale_limit);
//            }
//        }
//    }
//
//    @Override
//    public void offFull() {
//        if (null != dragView) { //保证两边铺满
//            Rect rectF = dragView.getSrcRect();
//            float scale = Math.max(mVideoEditorHandler.getContainer().getWidth() / (0.0f + rectF.width()), mVideoEditorHandler.getContainer().getHeight() / (0.0f + rectF.height()));
//            if (scale > 1) {
//                boolean result = dragView.offsetScale(scale - 1);
//                if (result) {
//                    postRefresh();
//                } else {
//                    Utils.autoToastNomal(dragView.getContext(), R.string.pesdk_scale_limit);
//                }
//            }
//        }
//    }


    /**
     * 再次编辑
     *
     * @param info
     * @param showDeleteButton
     */
    public void edit(PEImageObject info, boolean showDeleteButton, boolean showEdit) {
        initDragView(info, showDeleteButton, showEdit);
    }

    /**
     * 退出编辑模式
     */
    public void exitEditMode() {
        if (dragView != null) {
            mVideoEditorHandler.getContainer().removeView(dragView);
            dragView.recycle();
            dragView = null;
        }
    }

    public void reset() {
        mPEImageObject = null;
    }


    public interface Callback {
        void prepared();

        void copy(PEImageObject base);

        void delete(DragMediaView view);

    }
}
