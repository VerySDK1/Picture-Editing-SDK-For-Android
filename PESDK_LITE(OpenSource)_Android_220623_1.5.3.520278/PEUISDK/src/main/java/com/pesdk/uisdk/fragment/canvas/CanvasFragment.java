package com.pesdk.uisdk.fragment.canvas;

import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.pesdk.bean.SortBean;
import com.pesdk.net.PENetworkApi;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.analyzer.AnalyzerManager;
import com.pesdk.uisdk.bean.ExtImageInfo;
import com.pesdk.uisdk.bean.code.Segment;
import com.pesdk.uisdk.bean.code.SegmentResult;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.bean.model.PipBgParam;
import com.pesdk.uisdk.bean.model.UndoInfo;
import com.pesdk.uisdk.data.vm.CanvasVM;
import com.pesdk.uisdk.fragment.canvas.callback.Callback;
import com.pesdk.uisdk.fragment.helper.DepthBarHandler;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.listener.ViewCallback;
import com.pesdk.uisdk.util.helper.FilterUtil;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.pesdk.uisdk.util.helper.PipHelper;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;
import com.vecore.utils.MiscUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

/**
 * 背景界面 （画布）
 */
public class CanvasFragment extends AbsCanvasFragment {

    private ImageHandlerListener mVideoEditorHandler;

    private ViewCallback mViewCallback;

    public static CanvasFragment newInstance() {
        Bundle args = new Bundle();
        CanvasFragment fragment = new CanvasFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void preStyle() {
        saveSync(IMenu.canvas);
        AnalyzerManager.getInstance().setEnableShowSegmentToast(true);
    }

    @Override
    protected void onResultImage(String path) {
        preStyle();
        setStyle(path);
        checkChecked(null); //更改其他fragment的选中项
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mVideoEditorHandler = (ImageHandlerListener) context;
        mViewCallback = (ViewCallback) context;
    }

    private CanvasVM mVM;
    private ViewPager2 rvPager;
    private TabLayout mTableLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_canvas, container, false);
        mVM = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(CanvasVM.class);
        mVM.getSortData().observe(getViewLifecycleOwner(), this::onSortResult);

        if (null != mScene) {
            PEImageObject bg = mScene.getBackground();
            if (null != bg) {
                mEditCallback.getSkyHandler().edit(bg, false);
            } else if (mScene.getBackgroundColor() != PEScene.UNKNOWN_COLOR) {
            } else {
            }
        }


        rvPager = $(R.id.vpager);
        rvPager.setSaveEnabled(false);
//        rvPager.setUserInputEnabled(false);//拦截viewpager2 横向滑动
        mTableLayout = $(R.id.tabs);

        $(R.id.btnLocal).setOnClickListener(v -> {
            if (hasSkyBg()) {
                onToastMsg();
                return;
            }
            if (mResultLauncher != null) {
                mResultLauncher.launch(null);
            }
        });
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.pesdk_canvas);
        return mRoot;
    }

    /**
     * 清理背景
     */
    private void none() {
        goneDepth();
        if (hasSkyBg()) {
            onToastMsg();
            return;
        }
        if (null != mScene) {
            mScene.setBackground(PEScene.UNKNOWN_COLOR);
            mEditCallback.getSkyHandler().exitEditMode();
        } else if (null != mCollageInfo) {
            mCollageInfo.setBG(null);
        }
        mVideoEditorHandler.reBuild();
        checkChecked(null); //更改其他fragment的选中项
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVM.process();
        if (null != mScene) { //图片编辑:背景-景深效果
            PEImageObject bg = mScene.getBackground();
            boolean hasBg = bg != null;
            if (hasBg && !TextUtils.isEmpty(bg.getMediaPath())) {
                initEditBar(hasBg ? FilterUtil.getBlurValue(bg.getFilterList()) : 0f);
            }
        } else if (null != mCollageInfo) {
            PEImageObject bg = mCollageInfo.getBG();
            boolean hasBg = bg != null;
            if (hasBg && !TextUtils.isEmpty(bg.getMediaPath())) {
                initEditBar(hasBg ? FilterUtil.getBlurValue(bg.getFilterList()) : 0f);
            }
        }

    }

    private void initBar() {
        if (null != mCollageInfo) {
            PEImageObject bg = mCollageInfo.getBG();
            boolean hasBg = bg != null;
            if (hasBg && !TextUtils.isEmpty(bg.getMediaPath())) {
                initEditBar(hasBg ? FilterUtil.getBlurValue(bg.getFilterList()) : 0f);
            }
        } else if (null != mScene) { //图片编辑:背景-景深效果
            PEImageObject bg = mScene.getBackground();
            boolean hasBg = bg != null;
            if (hasBg && !TextUtils.isEmpty(bg.getMediaPath())) {
                initEditBar(hasBg ? FilterUtil.getBlurValue(bg.getFilterList()) : 0f);
            }
        }
    }

    private List<SortBean> sortList;
    private SparseArray<Fragment> mCacheList = new SparseArray<>();

    private void onSortResult(List<SortBean> list) {
        sortList = list;
        mCacheList.clear();
        rvPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                return sortList.size();
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    ColorFragment fragment = ColorFragment.newInstance();
                    fragment.setCallback(mCallback);
                    mCacheList.append(position, fragment);
                    return fragment;
                } else {
                    StyleFragment fragment = StyleFragment.newInstance(PENetworkApi.Bground);
                    fragment.setSortApi(sortList.get(position));
                    fragment.setCallback(mCallback);
                    mCacheList.append(position, fragment);
                    return fragment;
                }
            }
        });
        initTabs();
        //自定义
        new TabLayoutMediator(mTableLayout, rvPager, false, (tab12, position) -> {

        }, tab1 -> { //切换分组
            if (tab1.getPosition() == 0) { //无
                none();
            }
        }).attach();
    }

    private void initTabs() {
        TabLayout.Tab tab = mTableLayout.newTab();
        tab.setText(R.string.pesdk_none);
        mTableLayout.addTab(tab);

        for (SortBean item : sortList) {
            tab = mTableLayout.newTab();
            tab.setText(item.getName());
            mTableLayout.addTab(tab);
        }
        mTableLayout.selectTab(mTableLayout.getTabAt(1)); //纯色
    }

    /**
     * 更改其他fragment的选中项
     *
     * @param exclude 排除当前自己
     */
    private void checkChecked(Fragment exclude) {
        for (int i = 0; i < mCacheList.size(); i++) {
            Fragment fragment = mCacheList.valueAt(i);
            if (exclude == fragment) {
                continue;
            }
            if (fragment instanceof ColorFragment) {
                ((ColorFragment) fragment).checkChecked();
            } else if (fragment instanceof StyleFragment) {
                ((StyleFragment) fragment).checkChecked();
            }
        }
    }


    private Callback mCallback = new Callback() {
        @Override
        public void onColor(int color) {
            saveSync(IMenu.canvas);
            AnalyzerManager.getInstance().setEnableShowSegmentToast(true);
            if (updateColorBG(color)) {
                int index = rvPager.getCurrentItem();
                if (mTableLayout.getSelectedTabPosition() != index + 1) {
                    mTableLayout.selectTab(mTableLayout.getTabAt(index + 1), true);
                    checkChecked(null);
                } else {
                    checkChecked(mCacheList.get(index));
                }
            } else {
                AnalyzerManager.getInstance().setEnableShowSegmentToast(false);
            }
            goneDepth();
        }

        @Override
        public int getBgColor() {
            if (null != mScene) {
                return mScene.getBackgroundColor();
            } else if (null != mCollageInfo) {
                PEImageObject bg = mCollageInfo.getBG();
                if (null != bg) {
                    PipBgParam param = (PipBgParam) bg.getTag();
                    if (null != param) {
                        return param.getColor();
                    }
                }
                return PEScene.UNKNOWN_COLOR;
            }
            return PEScene.UNKNOWN_COLOR;
        }

        @Override
        public void onStyle(String path) {
            preStyle();
            if (setStyle(path)) {
                int index = rvPager.getCurrentItem();
                if (mTableLayout.getSelectedTabPosition() != index + 1) {
                    mTableLayout.selectTab(mTableLayout.getTabAt(index + 1), true);
                    checkChecked(null);
                } else {
                    checkChecked(mCacheList.get(index));
                }
            }
        }

        @Override
        public String getStyle() {
            int bgColor = getBgColor();
            if (null != mScene) {
                if (null != mScene.getBackground()) {
                    return mScene.getBackground().getMediaPath();
                } else {
                    return null;
                }
            } else if (null != mCollageInfo) {
                if (bgColor == PEScene.UNKNOWN_COLOR) {
                    PEImageObject bg = mCollageInfo.getBG();
                    if (null != bg && bg.getTag() instanceof PipBgParam) {
                        PipBgParam pipBgParam = (PipBgParam) bg.getTag();
                        return pipBgParam.getPath();
                    }
                }
            }
            return null;
        }
    };
    private DepthBarHandler barHandler;

    /**
     * 图片编辑: 背景-景深
     */
    private void initEditBar(float filterValue) {
        mViewCallback.getDepth().setVisibility(View.VISIBLE);
        if (null == barHandler) {
            barHandler = new DepthBarHandler(mViewCallback.getDepth(), new DepthBarHandler.Callback() {
                @Override
                public void progress(float factor) {
                    if (null != mScene) {
                        PipHelper.onBlurBG(mScene.getBackground(), factor);
                    } else if (null != mCollageInfo) {
                        PipHelper.onBlurPipBg(mCollageInfo.getBG(), factor);
                    }
                }

                @Override
                public void onStopTrackingTouch(float factor) {

                }
            });
        }
        mRoot.post(() -> barHandler.setFactor(filterValue));
    }

    private void goneDepth() {
        if (null != mViewCallback.getDepth()) {
            mViewCallback.getDepth().setVisibility(View.GONE);
        }
    }


    private boolean updateColorBG(int color) {
        mEditCallback.getSkyHandler().exitEditMode();
        if (hasSkyBg()) { //没有人像，且天空抠图成功
            onToastMsg();
            return false;
        }
        if (null != mScene) {
            mScene.setBackground(color);
        } else if (null != mCollageInfo) {
            PipHelper.onPipSegment(mCollageInfo, Segment.SEGMENT_PERSON);
            PipHelper.onPipBgColor(mCollageInfo, color);
        }
        mVideoEditorHandler.reBuild();
        return true;
    }

    /**
     * 已经有了天空分割的背景，不再继续执行人像分割的逻辑
     *
     * @return
     */
    private boolean hasSkyBg() {
        if (null != mCollageInfo) {
            ImageOb ob = PEHelper.initImageOb(mCollageInfo.getImageObject());
            return ob.getPersonResult() != SegmentResult.AI_SUCCESS && ob.getSkyResult() == SegmentResult.AI_SUCCESS && mCollageInfo.getBG() != null;
        } else {
            return false;
        }
    }


    /**
     * 切换背景图
     */
    private boolean setStyle(String path) {
        if (hasSkyBg()) { //没有人像，且天空抠图成功
            onToastMsg();
            return false;
        }
        if (null != mScene) {
            try {
                PEImageObject bg = new PEImageObject(getContext(), path);
                float tmp = mVideoEditorHandler.getPlayerAsp();
                RectF dst = new RectF();
                MiscUtils.fixShowRectFByExpanding(bg.getWidth() * 1.0f / bg.getHeight(), 1080, (int) (1080 / tmp), dst);
                bg.setShowRectF(dst);
                mScene.setBackground(bg);
                mEditCallback.getSkyHandler().edit(bg, false);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        } else if (null != mCollageInfo) {
            PipHelper.onPipSegment(mCollageInfo, Segment.SEGMENT_PERSON);
            PipHelper.onPipBGStyle(mCollageInfo, null != barHandler ? barHandler.getValue() : 0, path);
        }
        mVideoEditorHandler.reBuild();
        initBar();
        return true;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AnalyzerManager.getInstance().setEnableShowSegmentToast(false);
        goneDepth();
        mRoot = null;
        mTableLayout = null;
        rvPager = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AnalyzerManager.getInstance().setEnableShowSegmentToast(false);
        mVM = null;
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        for (int i = 0; i < mCacheList.size(); i++) {
            Fragment tmp = mCacheList.valueAt(i);
            if (null != tmp) {
                fragmentTransaction.remove(tmp);
            }
        }
        fragmentTransaction.commitAllowingStateLoss();
        mCacheList.clear();
        mResultLauncher = null;
        if (null != sortList) {
            sortList.clear();
            sortList = null;
        }
        gcGlide();
    }


    @Override
    public void onCancelClick() {
        if (hasChanged()) {
            showAlert(new AlertCallback() {
                @Override
                public void cancel() {

                }

                @Override
                public void sure() {
                    AnalyzerManager.getInstance().setEnableShowSegmentToast(false);
                    if (null != mScene) {
                        UndoInfo info = mVideoEditorHandler.getParamHandler().onDeleteStep();
                        if (null != info && info.getList() != null && info.getMode() == IMenu.canvas && info.getList().size() > 0) {
                            mVideoEditorHandler.getParamHandler().restorePE((ExtImageInfo) info.getList().get(0));
                        }
                    } else {
                        mVideoEditorHandler.getParamHandler().onUndo();
                    }

                    mEditCallback.getSkyHandler().exitEditMode();
                    mVideoEditorHandler.reBuild();
                    mVideoEditorHandler.onBack();
                }
            });
        } else {
            // 无修改
            AnalyzerManager.getInstance().setEnableShowSegmentToast(false);
            mEditCallback.getSkyHandler().exitEditMode();
            mVideoEditorHandler.onBack();
        }
    }

    @Override
    public void onSureClick() {
        AnalyzerManager.getInstance().setEnableShowSegmentToast(false);
        mEditCallback.getSkyHandler().exitEditMode();
        mVideoEditorHandler.onSure();
    }

    @Override
    void onToastMsg() {
        onToast(R.string.pesdk_toast_person_segment);
    }

}
