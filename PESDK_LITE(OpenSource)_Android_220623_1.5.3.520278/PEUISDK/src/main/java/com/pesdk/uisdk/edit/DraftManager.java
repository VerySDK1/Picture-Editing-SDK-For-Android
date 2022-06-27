package com.pesdk.uisdk.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import com.pesdk.api.IVirtualImageInfo;
import com.pesdk.uisdk.bean.ExtImageInfo;
import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.database.DraftData;
import com.pesdk.uisdk.edit.bean.IParam;
import com.pesdk.uisdk.export.DataManager;
import com.pesdk.uisdk.export.ExportHelper;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.listener.RunnablePriority;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.base.lib.utils.LogUtil;
import com.vecore.listener.ExportListener;
import com.vecore.models.ImageConfig;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 草稿管理
 */
public class DraftManager {
    private static final String TAG = "DraftManager";

    @SuppressLint("StaticFieldLeak")
    private static volatile DraftManager singleton = null;

    private DraftManager() {
    }

    public static DraftManager getInstance() {
        if (singleton == null) {
            synchronized (DraftManager.class) {
                if (singleton == null) {
                    singleton = new DraftManager();
                }
            }
        }
        return singleton;
    }

    private Context mContext;
    /**
     * 保存草稿
     */
    private boolean mIsUpdate = false;
    /**
     * 草稿
     */
    private VirtualIImageInfo mVirtualIImageInfo;
    private OnDraftListener mListener;
    /**
     * 保存全部中 取消所有还未保存的
     */
    private boolean mIsSaveAll = false;
    /**
     * 保存草稿线程
     */
    private ThreadPoolExecutor mExecutorPool;
    /**
     * 阻塞队列。
     * 当核心线程都被占用，且阻塞队列已满的情况下，才会开启额外线程。
     */
    private BlockingQueue<Runnable> mBlockingQueue;


    /**
     * 初始化
     */
    public void init(Context context) {
        mContext = context.getApplicationContext();
        initThread();
    }

    /**
     * 线程
     */
    private void initThread() {
        // 创建一个核心线程数为1、最大线程数为1的线程池
        int CORE_POOL_SIZE = 1;//核心线程数
        int MAX_POOL_SIZE = 1;//最大线程数
        long KEEP_ALIVE_TIME = 10;//空闲线程超时时间
        mBlockingQueue = new PriorityBlockingQueue<>();
        mExecutorPool = new ThreadPoolExecutor(CORE_POOL_SIZE,
                MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                mBlockingQueue, new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    /**
     * 设置接口
     */
    public void setListener(OnDraftListener listener) {
        mListener = listener;
    }

    /**
     * 设置草稿
     */
    public void setShortInfo(VirtualIImageInfo shortVideoInfoImp) {
        mVirtualIImageInfo = shortVideoInfoImp;
        //初始保存
        onSaveDraft(IMenu.MODE_PREVIEW);
    }


    /**
     * 结束
     */
    public void onExit() {
        if (mBlockingQueue != null) {
            mBlockingQueue.clear();
            mBlockingQueue = null;
        }
        if (mExecutorPool != null) {
            mExecutorPool.shutdownNow();
            mExecutorPool = null;
        }
        mIsUpdate = false;
    }

    /**
     * 退出app
     */
    public void onExitApp() {
        if (mBlockingQueue != null) {
            mBlockingQueue.clear();
            mBlockingQueue = null;
        }
        if (mExecutorPool != null) {
            mExecutorPool.shutdownNow();
            mExecutorPool = null;
        }
        DraftData.getInstance().close();
        mIsUpdate = false;
    }

    /**
     * 保存单个类型的数据list到草稿
     */
    public void onSaveDraft(final int mode) {
        if (mListener == null || mIsSaveAll) {
            Log.e(TAG, "onSaveDraft: " + mListener + " mIsSaveAll:" + mIsSaveAll + " >" + mode);
            return;
        }
        if (mExecutorPool == null) {
            initThread();
        }
        mIsUpdate = true;
        mExecutorPool.execute(new RunnablePriority(1, (int) System.currentTimeMillis(),
                time -> {
                    EditDataHandler handler = mListener.getHandler();
                    //更新
                    mVirtualIImageInfo.setUpdateTime(System.currentTimeMillis());
                    if (mode == IMenu.mainTrack || mode == IMenu.erase || mode == IMenu.crop || mode == IMenu.depth || mode == IMenu.canvas || mode == IMenu.mirror) {
                        //涂抹、裁剪后，需要更新主媒体
                        mVirtualIImageInfo.setExtImageInfo(handler.getExtImage());
                    } else if (mode == IMenu.effect) {  //特效
                        mVirtualIImageInfo.setEffectInfoList(handler.getParam().getEffectList());
                    } else if (mode == IMenu.frame) { //边框
                        mVirtualIImageInfo.setFrameInfoList(handler.getParam().getFrameList());
                    } else if (mode == IMenu.overlay) { //叠加
                        mVirtualIImageInfo.setOverlayList(handler.getParam().getOverLayList());
                    } else if (mode == IMenu.graffiti) {
                        //涂鸦
                        mVirtualIImageInfo.setGraffitiList(handler.getParam().getGraffitList());
                    } else if (mode == IMenu.text) {
                        //新版字幕
                        mVirtualIImageInfo.setWordInfoList(handler.getParam().getWordList());
                    } else if (mode == IMenu.sticker) {
                        //贴纸
                        mVirtualIImageInfo.setStickerInfos(handler.getParam().getStickerList());
                    } else if (mode == IMenu.pip) {  //画中画
                        mVirtualIImageInfo.setCollageInfos(handler.getParam().getCollageList());
                    } else if (mode == IMenu.filter) {//虚拟图片滤镜
                        ExtImageInfo scene = handler.getExtImage();
                        IParam param = handler.getParam();
                        scene.setFilter(param.getFilter() != null ? param.getFilter().copy() : null);
                    } else if (mode == IMenu.adjust) {//虚拟图片调色
                        ExtImageInfo scene = handler.getExtImage();
                        IParam param = handler.getParam();
                        scene.setAdjust(param.getAdjust() != null ? param.getAdjust().copy() : null);
                    } else if (mode == IMenu.proportion) {  //比例
                        mVirtualIImageInfo.setProportionMode(handler.getParam().getProportionMode(), handler.getParam().getProportionValue());
                    } else if (mode == IMenu.mix) { //图片合成
                        saveAll();
                    } else {
                        saveAll();
                    }
                    update(mVirtualIImageInfo);
                }));
    }

    /**
     * 保存草稿所有的
     */
    public void onSaveDraftAll() {
        if (mExecutorPool == null) {
            initThread();
        }
        mIsSaveAll = true;
        //移出所有的为完成的保存
        mBlockingQueue.clear();
        mExecutorPool.execute(new RunnablePriority(10, () -> {
            saveAll();
            //草稿箱显示封面
            if (mListener.getEditor() != null) {//封面
                VirtualImageView editor = mListener.getEditor();
                float asp = editor.getPreviewWidth() * 1.0f / editor.getPreviewHeight();
                captureCover(asp, mVirtualIImageInfo.copy());
            }
            boolean insert = false;
            //移入草稿箱
            mVirtualIImageInfo.moveToDraft(mContext);
            if (insert) {
                int id = insert(mVirtualIImageInfo);
                mVirtualIImageInfo.setId(id);
            } else {
                update(mVirtualIImageInfo);
            }
            mIsSaveAll = false;
            mListener.onEnd();
        }));
    }

    private void saveAll() {
        EditDataHandler dataHandler = mListener.getHandler();
        mVirtualIImageInfo.setUpdateTime(System.currentTimeMillis());
        //场景
        ExtImageInfo src = dataHandler.getExtImage();
        if (null != src) {
            mVirtualIImageInfo.setExtImageInfo(src.copy());
        }
        //字幕
        mVirtualIImageInfo.setWordInfoList(dataHandler.getParam().getWordList());
        //贴纸
        mVirtualIImageInfo.setStickerInfos(dataHandler.getParam().getStickerList());
        // 涂鸦
        mVirtualIImageInfo.setGraffitiList(dataHandler.getParam().getGraffitList());


        //马赛克
//        mVirtualIImageInfo.setMOInfos(dataHandler.getMaskList());
        //特效
        mVirtualIImageInfo.setEffectInfoList(dataHandler.getParam().getEffectList());
        mVirtualIImageInfo.setCollageInfos(dataHandler.getParam().getCollageList());// 图层
        mVirtualIImageInfo.setOverlayList(dataHandler.getParam().getOverLayList());//叠加
        mVirtualIImageInfo.setFrameInfoList(dataHandler.getParam().getFrameList());//边框


        mVirtualIImageInfo.setProportionMode(dataHandler.getParam().getProportionMode(), dataHandler.getParam().getProportionValue());
    }


    /**
     * 是否修改
     */
    public boolean isUpdate() {
        return mIsUpdate;
    }

    //封面截取最小边
    public static final int COVER_MIN = 300;

    private final Object lock = new Object();

    /**
     * 截取封面 不保存步骤
     */
    private void captureCover(float asp, VirtualIImageInfo info) {
        int[] wh = ExportHelper.initWH(asp, COVER_MIN);
        VirtualImage virtualImage = new VirtualImage();
        DataManager.loadData(virtualImage, info);
        virtualImage.setPreviewAspectRatio(asp);
        String dst;
        String basePath = mVirtualIImageInfo.getBasePath();
        if (TextUtils.isEmpty(basePath)) {
            dst = PathUtils.getTempFileNameForSdcard("cover", "jpg");
        } else {
            dst = PathUtils.getTempFileNameForSdcard(basePath, "cover", "jpg");
        }
        virtualImage.export(mContext, dst, new ImageConfig(wh[0], wh[1], Color.TRANSPARENT), new ExportListener() {

            @Override
            public void onExportStart() {

            }

            @Override
            public boolean onExporting(int progress, int max) {
                return true;
            }

            @Override
            public void onExportEnd(int result, int extra, String info) {
                LogUtil.i(TAG, "onExportEnd: " + result);
                virtualImage.release();
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            mVirtualIImageInfo.setCover(dst);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 草稿箱线程
     */
    public void execute(Runnable runnable) {
        if (mExecutorPool == null) {
            initThread();
        }
        mExecutorPool.execute(runnable);
    }


    /**
     * 插入草稿
     */
    public int insert(VirtualIImageInfo info) {
        if (mContext == null) {
            LogUtil.w(TAG, "update: context is null ");
            return 0;
        }
        DraftData.getInstance().initilize(mContext);
        return DraftData.getInstance().insertOrReplace(info);
    }

    /**
     * 插入草稿
     */
    public int update(VirtualIImageInfo info) {
        if (mContext == null) {
            LogUtil.w(TAG, "update: context is null ");
            return 0;
        }
        DraftData.getInstance().initilize(mContext);
        return (int) DraftData.getInstance().update(info);
    }

    /**
     * 获取所有
     */
    public ArrayList<IVirtualImageInfo> getAll() {
        if (mContext == null) {
            LogUtil.w(TAG, "update: context is null ");
            return null;
        }
        DraftData.getInstance().initilize(mContext);
        return DraftData.getInstance().getAll(VirtualIImageInfo.INFO_TYPE_NORMAL);
    }

    /**
     * 查询
     */
    public VirtualIImageInfo queryOne(int id) {
        if (mContext == null) {
            LogUtil.w(TAG, "update: context is null ");
            return null;
        }
        DraftData.getInstance().initilize(mContext);
        return DraftData.getInstance().queryOne(id);
    }

    /**
     * 删除
     */
//    public int delete(int id) {
//        if (mContext == null) {
//            return 0;
//        }
//        DraftData.getInstance().initilize(mContext);
//        return DraftData.getInstance().delete(id);
//    }

    /**
     * 获取数据库最后一条
     */
    public VirtualIImageInfo getLast() {
        if (mContext == null) {
            return null;
        }
        DraftData.getInstance().initilize(mContext);
        return DraftData.getInstance().queryLast();
    }

    /**
     * 清空数据库
     */
    public void allDelete() {
        if (mContext == null) {
            LogUtil.w(TAG, "update: context is null ");
            return;
        }
        DraftData.getInstance().initilize(mContext);
        DraftData.getInstance().allDelete();
    }


    public interface OnDraftListener {

        /**
         * 获取handler
         */
        EditDataHandler getHandler();


        /**
         * 获取 虚拟视频播放器
         */
        VirtualImageView getEditor();

        /**
         * 保存完成
         */
        void onEnd();
    }
}
