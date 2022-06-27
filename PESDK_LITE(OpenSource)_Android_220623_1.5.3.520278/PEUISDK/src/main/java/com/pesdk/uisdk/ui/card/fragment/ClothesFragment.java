package com.pesdk.uisdk.ui.card.fragment;

import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.pesdk.bean.SortBean;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.BaseRVAdapter;
import com.pesdk.uisdk.adapter.HairAdapter;
import com.pesdk.uisdk.adapter.SortAdapter;
import com.pesdk.uisdk.bean.model.ItemBean;
import com.pesdk.uisdk.beauty.bean.BeautyFaceInfo;
import com.pesdk.uisdk.beauty.bean.FaceClothesParam;
import com.pesdk.uisdk.beauty.bean.FaceHairInfo;
import com.pesdk.uisdk.data.vm.ClothesVM;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.listener.OnItemClickListener;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.helper.IndexHelper;
import com.vecore.base.downfile.utils.DownLoadUtils;
import com.vecore.base.downfile.utils.IDownFileListener;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.base.lib.utils.LogUtil;
import com.vecore.models.PEImageObject;
import com.vecore.models.caption.CaptionLiteObject;
import com.vecore.utils.MiscUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 3.换装
 */
public class ClothesFragment extends AbsBaseFragment {

    public static ClothesFragment newInstance() {

        Bundle args = new Bundle();

        ClothesFragment fragment = new ClothesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView mRecyclerView;
    private ClothesVM mVM;
    private SortAdapter mSortAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_card_clothes, container, false);
        mVM = new ViewModelProvider(getActivity(), new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(ClothesVM.class);
        mVM.getListData().observe(getViewLifecycleOwner(), this::onClothList);
        mVM.getSortData().observe(getViewLifecycleOwner(), this::onSortResult);
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

    private HairAdapter mAdapter;

    /**
     * 单个发型列表
     */
    private void onClothList(List<ItemBean> itemBeans) {
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

    private final String EXTENSION = ClothesVM.EXTENSION;

    /**
     * 下载滤镜
     */
    private void down(int itemId, final ItemBean info) {
        String url = info.getFile();
        if (!mMap.containsKey(url)) {
            /**
             * 支持指定下载文件的存放位置
             */
            String path = null;
            if (url.contains(EXTENSION)) {
                path = PathUtils.getTempFileNameForSdcard(PathUtils.getClothesPath(), "clothes", EXTENSION);
            } else {
                path = PathUtils.getClothesItem(info.getFile());
            }
            final DownLoadUtils download = new DownLoadUtils(getContext(), itemId, url, path);
            download.DownFile(new IDownFileListener() {

                @Override
                public void onProgress(long mid, int progress) {
                    Log.e(TAG, "onProgress: " + mid + "   " + progress);
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
                        LogUtil.i(TAG, "downFinished:" + localPath);
                        if (localPath.endsWith(EXTENSION)) {
                            String targetPath = PathUtils.getClothesChildDir(url);
                            String dst = null;
                            try {
                                dst = FileUtils.unzip(null, localPath, targetPath);
                                if (!TextUtils.isEmpty(dst)) {//删除临时文件
                                    FileUtils.deleteAll(localPath);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.e(TAG, "Finished: " + targetPath + " >" + dst);
                            info.setLocalPath(dst);
                        } else {
                            info.setLocalPath(localPath);
                        }

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


    /**
     *
     */
    private void applyHair(String path) {
        applyDefaultHair(path);
    }


    /**
     * 默认衣服居中显示
     */
    private void applyDefaultHair(String root) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        File fRoot = new File(root);
        String file;
        if (fRoot.isDirectory()) {
            file = new File(root, "file.png").getAbsolutePath();
        } else {
            file = root;
        }

        BitmapFactory.decodeFile(file, op);
        CaptionLiteObject liteObject = new CaptionLiteObject(file, op.outWidth, op.outHeight);
        CaptionLiteObject last = mCallback.getLastHair();

        BeautyFaceInfo beautyFaceInfo = mCallback.getBeautyFace();  //找人脸位置
        FaceClothesParam param = beautyFaceInfo.applyClothesParam();

        PEImageObject tmp = mCallback.getBase();

        RectF rectF = tmp.getShowRectF(); //源媒体
        float pw = 1080f;
        float ph = pw / mCallback.getAsp();


        float width = 1f;//todo 保证衣服铺满w和h，可expadding


        float left = (param.getJawPointF().x * rectF.width() + rectF.left) - width / 2f;
        float top = (param.getJawPointF().y * rectF.height() + rectF.top);
        float dstW = pw * width;
        float dstH = dstW / (op.outWidth * 1f / op.outHeight);

        RectF rect = new RectF(left, top, left + width, top + (dstH * 1f / ph));


//        Log.e(TAG, "applyDefaultHair: " + rectF + " >" + rect + " " + param.getJawPointF() + " " + last);
        if (null != last) {
            RectF lastRectF = last.getShowRectF();
            //保证，和最小边不变
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

            dst.offset(lastRectF.centerX() - dst.centerX(), lastRectF.top - dst.top);
            liteObject.setShowRectF(dst);
            liteObject.setAngle(last.getAngle());
        } else {
            liteObject.setShowRectF(rect);
        }
        mCallback.updateCaption(liteObject);
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
         * 隐藏头发控制按钮
         */
        void hideUI();

        BeautyFaceInfo getBeautyFace();


        void onCancelHair();

        /**
         * 保存衣服
         */
        void onClothesSure();
    }

}
