package com.pesdk.uisdk.beauty.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pesdk.bean.SortBean;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.BaseRVAdapter;
import com.pesdk.uisdk.adapter.HairAdapter;
import com.pesdk.uisdk.adapter.SortAdapter;
import com.pesdk.uisdk.bean.model.ItemBean;
import com.pesdk.uisdk.beauty.bean.BeautyFaceInfo;
import com.pesdk.uisdk.beauty.bean.FaceEyeParam;
import com.pesdk.uisdk.beauty.bean.FaceHairInfo;
import com.pesdk.uisdk.data.vm.HairVM;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.listener.OnItemClickListener;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.helper.IndexHelper;
import com.vecore.base.downfile.utils.DownLoadUtils;
import com.vecore.base.downfile.utils.IDownFileListener;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.base.lib.utils.LogUtil;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.MediaObject;
import com.vecore.models.PEImageObject;
import com.vecore.models.caption.CaptionLiteObject;
import com.vecore.utils.MiscUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 换头发
 */
public class HairFragment extends AbsBaseFragment {
    public static HairFragment newInstance() {
        return new HairFragment();
    }

    private HairVM mVM;

    protected SortAdapter mSortAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_beauty_hair, container, false);
        mVM = new ViewModelProvider(getActivity(), new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(HairVM.class);
        mVM.getLiveData().observe(getViewLifecycleOwner(), this::onHairResult);
        mVM.getSortData().observe(getViewLifecycleOwner(), this::onSortResult);
        ((TextView) $(R.id.tv_title)).setText(R.string.pesdk_fu_hair);
        return mRoot;
    }

    public void setFaceHairInfo(FaceHairInfo hairInfo) {
        mFaceHairInfo = hairInfo;
    }

    private FaceHairInfo mFaceHairInfo;

    /**
     * 发型分组
     *
     * @param sortApis
     */
    private void onSortResult(List<SortBean> sortApis) {
        if (null != sortApis) {
            int index = BaseRVAdapter.UN_CHECK;
            if (null != mFaceHairInfo) {
                index = IndexHelper.getSortIndex(sortApis, mFaceHairInfo.getHairSortId());
                if (sortApis.size() > 1) {
                    mVM.loadData(sortApis.get(Math.max(1, index)));
                }
            } else {
                if (sortApis.size() > 1) {
                    mVM.loadData(sortApis.get(1));
                }
            }
            mSortAdapter.addAll(sortApis, index);
        }
    }

    private RecyclerView mRecyclerView;
    private HairAdapter mAdapter;

    /**
     * 单个发型列表
     */
    private void onHairResult(List<ItemBean> itemBeans) {
        int index = BaseRVAdapter.UN_CHECK;
        if (null != mFaceHairInfo) {
            index = IndexHelper.getIndex(itemBeans, mFaceHairInfo.getHairMaterialId());
        }
        mAdapter.addAll(itemBeans, index);
    }

    private RecyclerView mRVSort;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View cancel = $(R.id.btnLeft);
        cancel.setVisibility(View.VISIBLE);
        cancel.setOnClickListener(v -> onBackPressed());
        //确定
        $(R.id.btn_sure).setOnClickListener(v -> {
            onSure();
        });
        mRecyclerView = $(R.id.rvHair);
        mRVSort = $(R.id.filter_sort);
        mAdapter = new HairAdapter(getContext(), Glide.with(this));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((OnItemClickListener<ItemBean>) (position, info) -> {
            onSelectedImp(position);
        });
        mAdapter.setProgressCallBack(() -> mMap);
        mSortAdapter = new SortAdapter(getContext());
        mRVSort.setAdapter(mSortAdapter);
        mSortAdapter.setOnItemClickListener((OnItemClickListener<SortBean>) (position, item) -> {
            if (position == 0) { //清除美发
                mFaceHairInfo.setHair(item.getId(), null);
                mCallback.updateCaption(null);
                mAdapter.setChecked(BaseRVAdapter.UN_CHECK);
            } else { //切换分类
                mVM.loadData(item);
            }
        });
        mVM.loadSort();
    }

    private void onSure() {
        //1.将头发合并到人像图片上，生成新图，注意显示位置
        mCallback.preMergeHair();
    }

    private HashMap<String, Integer> mMap = new HashMap<>();

    @Override
    public int onBackPressed() {
        if (isRunning) {
            //恢复之前的头发参数
            mCallback.onCancelHair();
            return 1;
        }
        return 0;
    }


    /**
     * 下载滤镜
     */
    private void down(int itemId, final ItemBean info) {
        String url = info.getFile();
        if (!mMap.containsKey(url)) {
            /**
             * 支持指定下载文件的存放位置
             */
            String path = PathUtils.getTempFileNameForSdcard(PathUtils.getHairPath(), "hair", "zip");
            final DownLoadUtils download = new DownLoadUtils(getContext(), itemId, url, path);
            download.DownFile(new IDownFileListener() {

                @Override
                public void onProgress(long mid, int progress) {
                    if (isRunning) {
                        mMap.put(url, progress);
                        if (null != mAdapter) {
                            mAdapter.setdownProgress((int) mid);
                        }
                    }
                }

                @Override
                public void Finished(long mid, String localPath) {
                    if (null != mMap) {
                        mMap.remove(url);
                    }
                    if (isRunning) {
                        String targetPath = PathUtils.getHairChildDir(url);
                        String dst = null;
                        try {
                            dst = FileUtils.unzip(null, localPath, targetPath);
                            if (!TextUtils.isEmpty(dst)) {//删除临时文件
                                FileUtils.deleteAll(localPath);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        LogUtil.i(TAG, "downFinished:" + dst);
                        info.setLocalPath(dst);
                        if (null != mAdapter) {
                            int id = (int) mid;
                            mAdapter.setdownEnd(id);
                            onSelectedImp(id);
                        }

                    }
                }

                @Override
                public void Canceled(long mid) {
                    Log.e(TAG, "Canceled: " + mid);
                    if (null != mMap) {
                        mMap.remove(url);
                    }
                    if (isRunning) {
                        if (null != mAdapter) {
                            mAdapter.setdownFailed((int) mid);
                        }
                    }
                }
            });

            if (isRunning) {
                mMap.put(url, 1);
                if (null != mAdapter) {
                    mAdapter.setdownStart(itemId);
                }
            }
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void onSelectedImp(int nItemId) {
        ItemBean info = mAdapter.getItem(nItemId);
        if (FileUtils.isExist(info.getLocalPath())) {//已下载的,直接使用
            mSortAdapter.setCurrent(info.getSortId());
            mAdapter.onItemChecked(nItemId);
            switchItem(nItemId);
        } else if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
            onToast(R.string.common_check_network);
        } else {
            // 下载
            down(nItemId, info);
        }
    }

    private void switchItem(int index) {
        ItemBean bean = mAdapter.getItem(index);
        if (!FileUtils.isExist(bean.getLocalPath())) {
            Log.e(TAG, "switchItem: file not found");
            return;
        }
        mFaceHairInfo.setHair(bean.getSortId(), bean.getId());
        applyHair(bean.getLocalPath());
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private Callback mCallback;

    private void readConfig(String data, Point left, Point right) {
        if (TextUtils.isEmpty(data)) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONObject object = jsonObject.getJSONObject("left");
            left.set(object.optInt("x"), object.optInt("y"));

            object = jsonObject.getJSONObject("right");
            right.set(object.optInt("x"), object.optInt("y"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private void applyHair(String basePath) {
        File config = new File(basePath, "config.json").getAbsoluteFile();
        String hairPath = new File(basePath, "file.png").getAbsolutePath(); //头发
        if (FileUtils.isExist(config)) {
            Point left = new Point();
            Point right = new Point();
            readConfig(FileUtils.readTxtFile(config), left, right);
            float baseEyeDistance = Math.abs(right.x - left.x); //px
            if (baseEyeDistance == 0) {
                LogUtil.i(TAG, "applyHair: json无法自动设置头发位置 " + baseEyeDistance);
                applyDefaultHair(hairPath);
                return;
            }

            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(hairPath, op);
            PointF midPointI = new PointF((left.x + right.x) / 2, (left.y + right.y) / 2);
            LogUtil.i(TAG, "applyHair: " + baseEyeDistance + " " + midPointI);
            float scaleX = midPointI.x / op.outWidth;
            float scaleY = midPointI.y / op.outHeight;


            BeautyFaceInfo beautyFaceInfo = mCallback.getBeautyFace();  //从新图目标图片中找人脸位置
            FaceEyeParam param = beautyFaceInfo.applyBaseEyeDistance();
            PointF mid = param.getMidPointF();
            float eyeDistance = param.getBaseEyeDistance();


            Bitmap srcBmp = null;
            try {
                srcBmp = MiscUtils.getBitmapByMedia(new MediaObject(mCallback.getBase().getMediaPath()), 8192);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
                return;
            }
            int w = srcBmp.getWidth(), h = srcBmp.getHeight();

            float midX = mid.x * w; //两眼的中心点
            float midY = mid.y * h;
            float eyeDistancePx = eyeDistance * w;


            if (eyeDistancePx == 0) {
                LogUtil.i(TAG, "applyHair: 识别失败，无法自动设置头发位置 ");
                applyDefaultHair(hairPath);
            } else {
                float scale = Math.max(0.1f, Math.min(20, eyeDistancePx / baseEyeDistance));

                int hairW = op.outWidth;
                int hairH = op.outHeight;
                LogUtil.i(TAG, "applyHair: scale " + scale + " src:" + hairW + "*" + hairH);
                int a = (int) (scale * hairW);
                int b = (int) (scale * hairH);

                Rect rect = new Rect(0, 0, (int) (scale * hairW), (int) (scale * hairH));
                rect.offset((int) (midX - a * scaleX), (int) (midY - b * scaleY)); //估算美发的显示位置
                applyAutoHair(hairPath, hairW, hairH, rect, w, h); //自动设置美发(需要依赖基础和目标的人脸信息)
            }
        } else {
            applyDefaultHair(hairPath);
        }
    }


    /**
     * 自动设置头发在虚拟图片中的位置
     *
     * @param hairPath
     * @param hairW
     * @param hairH
     * @param rect     相对于媒体的显示位置
     * @param w
     * @param h
     */
    private void applyAutoHair(String hairPath, int hairW, int hairH, Rect rect, int w, int h) {
        PEImageObject peImageObject = mCallback.getBase();
        RectF mediaRectF = peImageObject.getShowRectF();


        RectF rectF = new RectF();
        rectF.left = rect.left * 1f / w;
        rectF.right = rect.right * 1f / w;
        rectF.top = rect.top * 1f / h;
        rectF.bottom = rect.bottom * 1f / h;


        RectF dst = new RectF(); //相对于虚拟图片的显示位置
        dst.left = mediaRectF.left + (mediaRectF.width() * rectF.left);
        dst.right = mediaRectF.left + (mediaRectF.width() * rectF.right);
        dst.top = mediaRectF.top + (mediaRectF.height() * rectF.top);
        dst.bottom = mediaRectF.top + (mediaRectF.height() * rectF.bottom);
        LogUtil.i(TAG, "applyAutoHair: " + rectF + " " + (rect.width() * 1f / rect.height()) + " dst:" + dst);

        CaptionLiteObject liteObject = new CaptionLiteObject(hairPath, hairW, hairH);
        liteObject.setShowRectF(dst);
        liteObject.setAngle(peImageObject.getShowAngle());
        mCallback.updateCaption(liteObject);
    }


    /**
     * 默认头发居中显示 （因为不清人脸信息，只能手动微调）
     */
    private void applyDefaultHair(String hair) {
        try {
            PEImageObject tmp = new PEImageObject(hair);
            int pw = 1080;
            int ph = (int) (pw / mCallback.getAsp());
            CaptionLiteObject liteObject = new CaptionLiteObject(hair, tmp.getWidth(), tmp.getHeight());
            CaptionLiteObject last = mCallback.getLastHair();
            RectF rect = new RectF();
            MiscUtils.fixShowRectF(tmp.getWidth() * 1f / tmp.getHeight(), pw, ph, rect);
            if (null != last) {
                RectF lastRectF = last.getShowRectF();
                //保证中心点，和最小边不变
                int w = (int) (lastRectF.width() * pw);
                int h = (int) (lastRectF.height() * ph);

                float scale;
                if (w > h) {
                    scale = lastRectF.width();
                } else {
                    scale = lastRectF.height();
                }
                RectF out = new RectF(rect);
                RectF dst = MiscUtils.zoomRectF(out, scale, scale);
                LogUtil.i(TAG, "applyDefaultHair: " + dst + " " + lastRectF + " out:" + out);
                dst.offset(lastRectF.centerX() - dst.centerX(), lastRectF.centerY() - dst.centerY());

                liteObject.setShowRectF(dst);
                liteObject.setAngle(last.getAngle());
            } else {
                liteObject.setShowRectF(rect);
            }
            mCallback.updateCaption(liteObject);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }


    public static interface Callback {


        PEImageObject getBase();


        float getAsp();

        void updateCaption(CaptionLiteObject object);


        /**
         * 当前头发的,切换时保证缩放和角度不变
         */
        CaptionLiteObject getLastHair();

        /**
         * 退出时合并头发和人像
         */
        void preMergeHair();

        /**
         * 隐藏头发控制按钮
         */
        void hideUI();

        BeautyFaceInfo getBeautyFace();


        void onCancelHair();
    }


}
