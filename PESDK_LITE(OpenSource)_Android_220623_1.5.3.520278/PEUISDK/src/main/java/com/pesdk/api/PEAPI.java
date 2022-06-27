package com.pesdk.api;

import android.content.Context;

import com.pesdk.uisdk.bean.image.VirtualIImageInfo;
import com.pesdk.uisdk.database.DraftData;

import java.util.ArrayList;

import androidx.annotation.Keep;

/**
 * 备份Info，用于异常退出后恢复
 * 进入编辑时，创建一个Info，实时存储当前的操作（特效、滤镜....）
 */
@Keep
public class PEAPI {
    private static PEAPI instance;
    private VirtualIImageInfo mShortVideoInfo;

    public static PEAPI getInstance() {
        if (null == instance) {
            instance = new PEAPI();
        }
        return instance;
    }

    private Context mContext;

    /**
     *
     */
    public void init(Context context) {
        mContext = context.getApplicationContext();
        DraftData.getInstance().initilize(context);
    }

    /**
     * 开始进入主编辑,构造新的info，用来实时存储（崩溃之后，需要恢复此备份）
     */
    public void onShortImageEdit() {
        VirtualIImageInfo imageInfo = new VirtualIImageInfo();
        imageInfo.createBasePath();
        DraftData.getInstance().insertOrReplace(imageInfo);
        mShortVideoInfo = imageInfo;
        if (imageInfo.getId() == VirtualIImageInfo.ERROR_DB_ID) {
            mShortVideoInfo = (VirtualIImageInfo) getLastEditingShortInfo(mContext); //获取下备份短视频id（自增长，必须写入数据库后才知道id）
        }
    }

    public void onShortImageEdit(VirtualIImageInfo info) {
        mShortVideoInfo = info;
        info.createBasePath();
        DraftData.getInstance().insertOrReplace(info);
        if (mShortVideoInfo.getId() == VirtualIImageInfo.ERROR_DB_ID) {
            mShortVideoInfo = (VirtualIImageInfo) getLastEditingShortInfo(mContext); //获取下备份短视频id（自增长，必须写入数据库后才知道id）
        }
    }


    /**
     * 获取最近编辑中的短视频信息
     *
     * @return 短视频信息
     */
    public IVirtualImageInfo getLastEditingShortInfo(Context context) {
        if (null == mContext) {
            init(context);
        }
        if (null == mContext) {
            return null;
        }
        DraftData.getInstance().initilize(mContext);
        ArrayList<IVirtualImageInfo> list = DraftData.getInstance().getAll(VirtualIImageInfo.INFO_TYPE_NORMAL);
        if (null != list && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


    /**
     * 同步当前视频信息到数据库
     */
    public void syncToDB() {
        syncToDB(mContext, mShortVideoInfo);
    }

    /**
     * 同步信息到数据库
     *
     * @param context
     * @param shortVideoInfoImp
     */
    public void syncToDB(Context context, VirtualIImageInfo shortVideoInfoImp) {
        if (null != shortVideoInfoImp) {
            if (mShortVideoInfo != shortVideoInfoImp) {
                mShortVideoInfo = shortVideoInfoImp;
            }
            DraftData.getInstance().initilize(context);
            DraftData.getInstance().insertOrReplace(shortVideoInfoImp);
        }
    }

    public VirtualIImageInfo getShortImageInfo() {
        return mShortVideoInfo;
    }


    public void setShortVideo(VirtualIImageInfo shortVideo) {
        mShortVideoInfo = shortVideo;
    }

    /**
     * 清理异常退出前的备份
     */
    public void clearEditingShortImage() {
        mShortVideoInfo = (VirtualIImageInfo) getLastEditingShortInfo(mContext);
        deleteEditInfo();
    }

    /**
     * 导出成功后，删除备份短视频
     */
    public void deleteEditInfo() {
        if (null != mShortVideoInfo) {
            DraftData.getInstance().initilize(mContext);
            DraftData.getInstance().delete(mShortVideoInfo.getId());
            mShortVideoInfo = null;
        }
    }


}
