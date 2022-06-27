package com.pesdk.uisdk.fragment;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.pesdk.bean.SortBean;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.SortAdapter;
import com.pesdk.uisdk.adapter.StickerAdapter;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.bean.net.TResult;
import com.pesdk.uisdk.data.vm.StickerVM;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.fragment.sticker.StickerExportHandler;
import com.pesdk.uisdk.layoutmanager.WrapContentLinearLayoutManager;
import com.pesdk.uisdk.listener.OnItemClickListener;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.uisdk.util.helper.CommonStyleUtils;
import com.pesdk.uisdk.util.helper.IndexHelper;
import com.pesdk.uisdk.util.helper.StickerUtils;
import com.pesdk.uisdk.util.helper.SubUtils;
import com.pesdk.uisdk.widget.ParallaxRecyclerView;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.pesdk.widget.loading.CustomLoadingView;
import com.vecore.VirtualImage;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.utils.Log;

import java.io.File;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 仅有新增逻辑，切换样式，直接新增。 无编辑流程。若编辑，需点击主界面删除再新增
 */
public class StickerFragment extends SSBaseFragment<StickerInfo> {

    public static StickerFragment newInstance() {
        StickerFragment fragment = new StickerFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    //是否正在下载
    private boolean mIsDownloading = false;
    //分类
    private RecyclerView mRvStickerSort;
    private SortAdapter mSortAdapter;
    //数据
    private ParallaxRecyclerView mRvStickerData;
    private StickerAdapter mDataAdpter;
    // 新增true，编辑false
    private boolean mEdit = false;
    private boolean misAddState = false; //true 新增  ;false 编辑

    @Override
    public void setEditInfo(StickerInfo info) {
        super.setEditInfo(info);
        if (isEditItem) {
            mBkEdit = info.copy();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "StickerFragment";
    }


    private StickerVM mVM;
    private boolean autoAddItem = true;
    /**
     * loading
     */
    private CustomLoadingView mLoadingView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_sticker_layout, container, false);
        autoAddItem = true;
        bSwitchHand = false;
        mCurrentInfo = mEditInfo;
        mVM = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(StickerVM.class);
        mVM.getLiveData().observe(getViewLifecycleOwner(), this::onSortResult);
        mVM.getData().observe(getViewLifecycleOwner(), this::onDataResult);
        mLoadingView = $(R.id.loading);
        mLoadingView.setBackground(ContextCompat.getColor(getContext(), R.color.pesdk_white));
        mLoadingView.setHideCancel(true);
        return mRoot;
    }

    /**
     * 分类列表
     */
    private void onSortResult(List<SortBean> sortApis) {
        if (null == sortApis) {
            mLoadingView.loadError(getString(R.string.common_pe_loading_error));
        } else {
            int index = 0;
            if (null != mEditInfo && !TextUtils.isEmpty(mEditInfo.getCategory())) {
                index = IndexHelper.getSortIndex(sortApis, mEditInfo.getCategory());
                index = Math.max(0, index);
            }
            mSortAdapter.addAll(sortApis, index);
            if (sortApis.size() > 0) {     //默认获取第一个
                mVM.load(sortApis.get(index));
            }
        }

    }

    private boolean bSwitchHand = false; //用户点击主动切换分类

    /**
     * 单个列表
     *
     * @param result
     */
    private void onDataResult(TResult result) {
        SysAlertDialog.cancelLoadingDialog();
        if (null == result) {
            mLoadingView.loadError(getString(R.string.common_pe_loading_error));
        } else {
            List<StyleInfo> list = result.getList();
            if (isRunning) {
                mLoadingView.setVisibility(View.GONE);
                int index = null != mCurrentInfo ? IndexHelper.getStyleIndex(list, mCurrentInfo.getIcon()) : 0;
                onData(list, index); //切换Adapter数据
                if (null != mEditInfo) {
                    if (!bSwitchHand) { //首次响应编辑
                        onEditClick(index, mDataAdpter.getItem(index));
                    }
                } else if (autoAddItem) {  //首次自动新增
                    autoAddItem = false;
                    mRoot.postDelayed(() -> onBtnAddClick(), 100);
                }
            }
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRvStickerSort = $(R.id.sticker_sort);
        mRvStickerData = $(R.id.sticker_data);
        init();
    }

    @Override
    public void onCancelClick() {
        showAlert(new AlertCallback() {
            @Override
            public void cancel() {

            }

            @Override
            public void sure() {
                misAddState = false;
                Log.e(TAG, "onCancelClick: " + isEditItem + " >" + mCurrentInfo);
                if (null != mCurrentInfo) {
                    if (isEditItem) { //放弃编辑
                        mListener.getParamHandler().restoreSticker(mCurrentInfo, mBkEdit);  //还原旧的贴纸
                        mListener.getParamHandler().onDeleteStep(); //删除edit是临时保存的步骤
                        mCurrentInfo.removeListLiteObject(mListener.getEditorImage());
                        mBkEdit.update(mListener.getEditorImage()); //还原旧的贴纸
                    } else { //放弃新增
                        if (mListener.getParamHandler().deleteSticker(mCurrentInfo)) {
                            mListener.getParamHandler().onDeleteStep();
                        }
                        mCurrentInfo.removeListLiteObject(mListener.getEditorImage());
                    }
                }
                mListener.getEditor().refresh();
                mListener.onSure(false);
                mCurrentInfo = null;
                mListener.onSelectData(-1);
            }
        });
    }


    @Override
    public void onSureClick() {
        Log.e(TAG, "onSureClick: " + mCurrentInfo);
        if (mIsDownloading) {
            SysAlertDialog.showLoadingDialog(mContext, mContext.getString(R.string.pesdk_isloading));
        } else {
            if (null != mCurrentInfo) {
                int id = mCurrentInfo.getStyleId();
                StyleInfo styleInfo = StickerUtils.getInstance().getStyleInfo(id);
                if (null != styleInfo && styleInfo.isdownloaded) {
                    SysAlertDialog.cancelLoadingDialog();
                    misAddState = false;
                    mDragHandler.onSave(); //保存并退出编辑模式
                    mListener.onSure(false);
                } else {
                    //参数有误，异常情况，放弃当前贴纸
                    Log.e(TAG, "onSaveListener : error " + styleInfo);
                }
            } else {
                //异常
                Log.e(TAG, "onSaveListener is null ");
            }
        }
    }


    /**
     * 进入贴纸
     */
    private void init() {
        mRvStickerSort.setLayoutManager(new WrapContentLinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mSortAdapter = new SortAdapter(getContext());
        mRvStickerSort.setAdapter(mSortAdapter);
        mSortAdapter.setOnItemClickListener((OnItemClickListener<SortBean>) (position, item) -> {
            //获取到分类 根据分类去获取数据
            bSwitchHand = true;
            mVM.load(item);
        });
        mRvStickerData.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mDataAdpter = new StickerAdapter(mContext, Glide.with(this));
        mRvStickerData.setAdapter(mDataAdpter);
        mRvStickerData.setListener(new ParallaxRecyclerView.OnLoadListener() {
            @Override
            public void onPull() {
                mSortAdapter.loadUp();
            }

            @Override
            public void onPush() {
                mSortAdapter.loadDown();
            }

            @Override
            public void firstItem(int firstItem) {

            }
        });

        mDataAdpter.setOnItemClickListener((OnItemClickListener<StyleInfo>) (position, item) -> { //切换样式时，新增贴纸
            if (null != mCurrentInfo) {
                StyleInfo styleInfo = StickerUtils.getInstance().getStyleInfo(item.pid);
//                Log.e(TAG, "init: " + SubUtils.DEFAULT_ID + " " + item.pid + " " + styleInfo);
                if (null != styleInfo && styleInfo.isdownloaded) {
                    SysAlertDialog.cancelLoadingDialog();
                    misAddState = false;
                    if (mCurrentInfo.getStyleId() == SubUtils.DEFAULT_ID) {
                        addItemStyle(mStickerIndex, position, item);
                    } else {
                        onStyleItem(position); //修改样式
                    }
                }
            }
        });
        //获取分类
        mVM.loadSort();
    }


    /**
     * 单个贴纸
     */
    private void initsp() {
        if (null != mCurrentInfo) {
            if (!misAddState) {
                onCheckStyle(mCurrentInfo);
            }
        }
    }

    private int mStickerIndex;

    /**
     * 新增单个
     */
    @Override
    void onBtnAddClick() {
        misAddState = true;
        int count = mDataAdpter.getItemCount();
        if (count > 0) {
            mCurrentInfo = new StickerInfo();
            mCurrentInfo.setId(Utils.getWordId());
            mStickerIndex = mListener.getParamHandler().addSticker(mCurrentInfo, !mEdit);

            int index = Math.max(0, mDataAdpter.getChecked());
            StyleInfo info = mDataAdpter.getItem(index);
            Log.e(TAG, "onBtnAddClick: " + index + " " + info);
            android.util.Log.e(TAG, "onBtnAddClick: mCurrentInfo: " + mCurrentInfo);
            if (info.isdownloaded) {  //已下载
                addItemStyle(mStickerIndex, index, info);
            } else {
                onDownSticker(index);
            }
        } else {
            Log.e(TAG, "onBtnAddClick: " + count);
        }
    }


    /**
     * 准备绑定样式
     */
    private void addItemStyle(int stickerIndex, int index, StyleInfo info) {
        File config = new File(info.mlocalpath, CommonStyleUtils.CONFIG_JSON);
        if (!FileUtils.isExist(config)) {
            Log.e(TAG, "addItemStyle: file not found" + config.getAbsolutePath());
            return;
        }
        fixRect(info);
        fixScale(info);
        onStyleItemDownloaded(index, info);
        mDragHandler.edit(stickerIndex, IMenu.sticker);
    }

    private void fixRect(StyleInfo info) {
        if (info.srcWidth > 0 && info.srcHeight > 0) {
            RectF rectOriginal = mCurrentInfo.getRectOriginal();
            if (rectOriginal != null && !rectOriginal.isEmpty()) {
                double asp = info.srcWidth / info.srcHeight;
                float value = Math.max(rectOriginal.width(), rectOriginal.height());
                float newWidth;
                float newHeight;
                PointF center = new PointF(rectOriginal.centerX(), rectOriginal.centerY());
                if (asp > 1) {
                    newWidth = value;
                    newHeight = (float) (value / asp);
                } else {
                    newWidth = (float) (value * asp);
                    newHeight = value;
                }
                rectOriginal.set(center.x - newWidth / 2, center.y - newHeight / 2,
                        center.x + newWidth / 2, center.y + newHeight / 2);
            }
        }
    }

    private void fixScale(StyleInfo info) {
        if (!mEdit) {
            //默认值
            if (info.isSetSizeW()) {
                //config.有设置相对显示比例
                mCurrentInfo.setDisf(info.disf);
            } else {
                //未设置相对显示比例，取一个合适的比例即可
                mCurrentInfo.setDisf(info.disf);
            }
        }
    }

    /**
     * 编辑按钮
     */
    private void onEditClick(int index, StyleInfo info) {
        if (null != mEditInfo) {
            //清除播放器中的liteObject，此刻交由UI界面绘制
            mEditInfo.removeListLiteObject(mListener.getEditorImage());
        }
        if (null != mCurrentInfo) {
            onStyleItemDownloaded(index, info);
        }
        mListener.getEditor().refresh();
    }

    @Override
    public int onBackPressed() {
        misAddState = false;
        return super.onBackPressed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRoot = null;
        if (null != mDataAdpter) {
            mDataAdpter.onDestory();
            mDataAdpter = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gcGlide();
    }


    private void applyResourceId(StyleInfo styleInfo) {
        mCurrentInfo.setStyleId(styleInfo.pid);
        mCurrentInfo.setCategory(styleInfo.category, styleInfo.icon);
        mCurrentInfo.setResourceId(styleInfo.resourceId);
    }

    /**
     * 下载样式完成
     */
    private void onStyleItemDownloaded(int position, StyleInfo styleInfo) {
        Log.e(TAG, "onStyleItemDownloaded: " + position + " " + mCurrentInfo);
        if (-1 != position) {
            if (null != mCurrentInfo) {
                if (null != styleInfo) {
                    applyResourceId(styleInfo);
                }
                initsp();
                StyleInfo info = mDataAdpter.getItem(position);
                if (info != null && TextUtils.equals(info.caption, styleInfo.caption)) { //未切换分类
                    onStyleItem(position);
                } else { //下载完成时，已经切换分类
                    applyStyle(styleInfo);
                }
                if (mDragHandler != null) {
                    mDragHandler.onGetPosition(mListener.getCurrentPosition());
                }
            }
            if (null != mDataAdpter) {
                mDataAdpter.notifyItemChanged(position);
            }
        }
    }


    //设置贴纸
    private void onStyleItem(int position) {
        mRvStickerData.scrollToPosition(position);
        StyleInfo info = mDataAdpter.getItem(position);
        Log.e(TAG, "onStyleItem: " + position);
        if (null == info || mCurrentInfo == null) {
            Log.e(TAG, "onStyleItem->info==null");
            return;
        }

        if (info.isdownloaded) {
            mDataAdpter.setCheckItem(position);
            applyResourceId(info);
            initsp();
            applyStyle(info);
        } else {
            // 执行下载
            onDownSticker(position);
        }
    }

    /**
     * 下载贴纸
     */
    private void onDownSticker(int position) {
        int at = position % mDataAdpter.getItemCount();
        View child = mRvStickerData.getLayoutManager().findViewByPosition(at);
        if (null != child) {
            mDataAdpter.onDown(position, child);
        }
    }

    /**
     * 应用已下载的样式
     */
    private void applyStyle(StyleInfo info) {
        applyResourceId(info);
        if (mCurrentInfo.getDisf() == 1) {
            //默认值
            if (info.isSetSizeW()) { //config.有设置相对显示比例
                mCurrentInfo.setDisf(info.disf);
            } else { //未设置相对显示比例，取一个合适的比例即可
                mCurrentInfo.setDisf(info.disf);
            }
        }
        bindLiteList(mCurrentInfo);
    }


    /**
     * 绑定新的liteObject
     */
    private void bindLiteList(StickerInfo stickerInfo) {
        if (null == stickerInfo) {
            return;
        }
        FrameLayout container = mListener.getContainer();
        int nWidth = container.getWidth();
        int nHeight = container.getHeight();
        stickerInfo.setPreviewAsp((nWidth / (nHeight + .0f)));
        stickerInfo.setParent(nWidth, nHeight);
        VirtualImage virtualVideo = mListener.getEditorImage();
        //移除listLiteObject
        stickerInfo.removeListLiteObject(virtualVideo);
        //构建新的lite列表
        new StickerExportHandler(mContext, stickerInfo, nWidth, nHeight).export(virtualVideo);
    }


    private void onCheckStyle(StickerInfo info) {
        if (mDataAdpter.getItemCount() > 0) {
            mDataAdpter.setCheckItem(mDataAdpter.getPosition(info.getStyleId()));
        }
    }

    /**
     * 开始编辑
     */
    private void onStartSub(boolean isdownload) {
        if (isdownload) {
            initsp();
        }
        if (null != mCurrentInfo) {
            int cp = mDataAdpter.getPosition(mCurrentInfo.getStyleId());
            if (!misAddState) {
                mDataAdpter.setCheckItem(cp);
            }
        } else {
            mDataAdpter.setCheckItem(-1);
        }

    }

    private void onData(List<StyleInfo> styleInfos, int index) {
        if (null != mDataAdpter) {
            if (styleInfos != null && styleInfos.size() > 0) {
                mDataAdpter.addStyles(styleInfos, index);
                mRvStickerData.scrollToPosition(index);
                if (mEdit) {
                    mEdit = false;
                    onStyleItem(mDataAdpter.getPosition(mCurrentInfo.getStyleId()));
                    onStartSub(true);
                    mEdit = false;
                }
            }
        }
    }


    /**
     * UI按钮恢复到默认
     */
    @Override
    void resetUI() {
        mCurrentInfo = null;
    }


}
