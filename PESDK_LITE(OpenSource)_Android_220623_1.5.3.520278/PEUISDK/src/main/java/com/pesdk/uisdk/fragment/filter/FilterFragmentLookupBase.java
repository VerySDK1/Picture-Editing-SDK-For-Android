package com.pesdk.uisdk.fragment.filter;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.bumptech.glide.Glide;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.FilterLookupAdapter;
import com.pesdk.uisdk.adapter.SortAdapter;
import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.bean.model.UndoInfo;
import com.pesdk.uisdk.bean.net.WebFilterInfo;
import com.pesdk.uisdk.fragment.BaseFragment;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.listener.OnItemClickListener;
import com.pesdk.uisdk.listener.ViewCallback;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.uisdk.util.helper.FilterUtil;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.vecore.models.PEImageObject;
import com.vecore.models.VisualFilterConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * lookup滤镜
 */
abstract class FilterFragmentLookupBase extends BaseFragment {
    private VisualFilterConfig tmpLookup = null;
    protected SortAdapter mSortAdapter;


    protected abstract int getLayoutId();

    public abstract void onSelectedImp(int nItemId);

    protected FilterLookupAdapter mAdapter;
    protected ImageHandlerListener mListener;
    private ViewCallback mViewCallback;

    /**
     * 默认的锐度
     */
    private float mDefaultValue = 1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mListener = (ImageHandlerListener) context;
        mViewCallback = (ViewCallback) context;
    }


    protected RecyclerView rvSort, rvFilter;
    private SeekBar mStrengthBar;
    protected View mStrengthView;
    protected HashMap<String, Integer> mMap = new HashMap<>();

    void onBarEnable(boolean enable) {
        Log.e(TAG, "onBarEnable: " + enable);
        mStrengthBar.setEnabled(enable);
    }

    /**
     * 滤镜
     */
    FilterInfo mFilterInfo, mbk;
    //图层-滤镜
    PEImageObject mPipImageObject;

    private boolean isEditFilter = false; //是否属于编辑滤镜
    private boolean bIsRecordEdit = false; //是否已经记录了首次编辑状态

    /**
     * 背景-滤镜
     */
    public void setFilterInfo(FilterInfo filterInfo) {
        mPipImageObject = null;
        mbk = null;
        mFilterInfo = filterInfo;
        fixEdit();
    }


    /**
     * 图层-滤镜
     */
    public void setLayerInfo(PEImageObject pipImageObject) {
        mFilterInfo = null;
        mbk = null;
        mPipImageObject = pipImageObject;
        if (pipImageObject == null) {
            return;
        }

        ImageOb temp = PEHelper.initImageOb(pipImageObject);
        mFilterInfo = temp.getFilter();
        fixEdit();
    }

    private void fixEdit() {
        isEditFilter = null != mFilterInfo;
        bIsRecordEdit = false;
    }

    /**
     * 设置滤镜
     */
    private void changeFilter(PEImageObject object) {
        FilterUtil.applyFilter(object);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(getLayoutId(), container, false);
        mbk = (null != mFilterInfo) ? mFilterInfo.copy() : null;
        rvSort = mRoot.findViewById(R.id.filter_sort);
        rvFilter = mRoot.findViewById(R.id.recyclerViewFilter);
        mStrengthView = Utils.$(mViewCallback.getFilterValueGroup(), R.id.ll_sbar);
        mStrengthBar = Utils.$(mViewCallback.getFilterValueGroup(), R.id.sbarStrength);
        mAdapter = new FilterLookupAdapter(getContext(), Glide.with(this));
        mAdapter.setEnableRepeatClick(true);
        rvFilter.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFilter.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((OnItemClickListener<WebFilterInfo>) (position, item) -> {
            onSelectedImp(position);
            mStrengthBar.setEnabled(position >= 0);//true 启用该视图
            onBarEnable(position >= 0);
        });
        mAdapter.setProgressCallBack(() -> mMap);
        return mRoot;
    }

    @Override
    public int onBackPressed() {
        if (isRunning) {
            return 1;
        }
        return super.onBackPressed();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        float sharpen = Float.NaN;
        tmpLookup = (null != mFilterInfo) ? mFilterInfo.getLookupConfig() : null;
        if (null != tmpLookup) {
            //滤镜程度就是调节的锐度参数
            sharpen = tmpLookup.getSharpen();
        }
        int value = Float.isNaN(sharpen) ? 100 : (int) (sharpen * 100);
        mStrengthBar.setProgress(value);
        mStrengthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mDefaultValue = progress / 100.0f;
                    onValue();
                    if (null != mPipImageObject) {//图层
                        changeFilter(mPipImageObject);
                    } else {
                        mListener.onFilterChange();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {//开始触摸
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {//完成触摸
                onValue();
                if (null != mPipImageObject) {
                    changeFilter(mPipImageObject);
                } else {
                    mListener.onFilterChange();
                }
            }
        });
    }

    private void onValue() {
        if (null != tmpLookup) {
            tmpLookup.setDefaultValue(mDefaultValue);
        }
    }

    @IMenu
    private final int type = IMenu.filter;

    private VisualFilterConfig initFilterUI(WebFilterInfo info) {
        VisualFilterConfig tmpLookup;
        if (info != null) {
            if (info.getUrl().contains("zip")) {
                //解压
                try {
                    String p = info.getLocalPath();
                    //读取 config.json
                    String content = PathUtils.readFile(p, "config.json");
                    if (TextUtils.isEmpty(content)) {
                        content = PathUtils.readFile(p, ".json");
                        if (TextUtils.isEmpty(content)) {
                            return null;
                        }
                    }
                    JSONObject jsonObject = new JSONObject(content);
                    if ("MosaicPixel".equals(jsonObject.optString("builtIn"))) {
                        boolean strip = jsonObject.optBoolean("strip");
                        tmpLookup = new VisualFilterConfig.Pixelate(strip);
                        tmpLookup.setFilterFilePath(p);
                    } else {
                        return null;
                    }
                } catch (JSONException e) {
                    tmpLookup = new VisualFilterConfig(info.getLocalPath());
                }
            } else {
                tmpLookup = new VisualFilterConfig(info.getLocalPath());
            }
        } else {
            tmpLookup = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_NORMAL);
        }
        return tmpLookup;
    }


    /**
     * 切换滤镜效果
     */
    void switchFliter(int index) {
        WebFilterInfo info = null;
        if (index >= 0) {
            info = mAdapter.getItem(index);
            tmpLookup = initFilterUI(info);
            tmpLookup.setDefaultValue(mDefaultValue);
            showFilterValue();
        } else {// 原始图片效果
            tmpLookup = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_NORMAL);
            goneFilterValue();
        }
        if (null != mPipImageObject) { //图层
            ImageOb imageOb = PEHelper.initImageOb(mPipImageObject);
            if (mFilterInfo == null) {
                mFilterInfo = new FilterInfo(tmpLookup);
                //记录一个临时步骤
                if (!bIsRecordEdit) { //已经编辑为调整滤镜
                    bIsRecordEdit = true;
                    mListener.getParamHandler().onSaveAdjustStep(IMenu.pip);
                }
            } else {
                if (isEditFilter) {//二次进入时,编辑滤镜
                    if (!bIsRecordEdit) { //已经编辑为调整滤镜
                        bIsRecordEdit = true;
                        mListener.getParamHandler().onSaveAdjustStep(IMenu.pip);
                    }
                }
                mFilterInfo.setLookupConfig(tmpLookup);
            }
            imageOb.setFilterInfo(mFilterInfo);
            setNetId(info);
            changeFilter(mPipImageObject);
        } else { //背景-滤镜
            if (mFilterInfo == null) {
                mFilterInfo = new FilterInfo(tmpLookup);
                mListener.getParamHandler().addFilterInfo(mFilterInfo, type); //新增滤镜
            } else {
                if (isEditFilter) {//二次进入时,编辑滤镜
                    if (!bIsRecordEdit) { //已经编辑为调整滤镜
                        bIsRecordEdit = true;
                        mListener.getParamHandler().editFilterInfo(mFilterInfo, type); //编辑
                    }
                }
                mFilterInfo.setLookupConfig(tmpLookup);
            }
            setNetId(info);
            mListener.onFilterChange();
        }
    }

    private void setNetId(WebFilterInfo info) {
        if (info != null) {
            mFilterInfo.setNetworkId(info.getGroupId(), info.getId());
        } else {
            mFilterInfo.setNetworkId("", "");
        }
    }

    @Override
    public void onCancelClick() {
        if (null != mFilterInfo && (null == mbk || !mFilterInfo.equals(mbk))) {
            showAlert(new AlertCallback() {
                @Override
                public void cancel() {

                }

                @Override
                public void sure() {
                    if (null != mPipImageObject) {
                        if (bIsRecordEdit) {//删除临时的滤镜步骤
                            mListener.getParamHandler().onDeleteStep();
                            ImageOb ob = PEHelper.initImageOb(mPipImageObject);
                            ob.setFilterInfo(mbk);
                            changeFilter(mPipImageObject); //恢复原始滤镜
                        }
                    } else {
                        mListener.getParamHandler().deleteFilterInfo(mFilterInfo);
                        UndoInfo info = mListener.getParamHandler().onDeleteStep();
                        if (null != info) {
                            mListener.getParamHandler().setFilterList(info.getList());
                        }
                        mListener.onFilterChange();
                    }
                    mMenuCallBack.onCancel();
                }
            });
        } else {
            mMenuCallBack.onCancel();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        goneFilterValue();
    }

    private void goneFilterValue() {
        if (null != mViewCallback.getFilterValueGroup()) {
            mViewCallback.getFilterValueGroup().setVisibility(View.GONE);
        }
    }

    void showFilterValue() {
        if (null != mViewCallback.getFilterValueGroup()) {
            mViewCallback.getFilterValueGroup().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSureClick() {
        mMenuCallBack.onSure();
    }


}
