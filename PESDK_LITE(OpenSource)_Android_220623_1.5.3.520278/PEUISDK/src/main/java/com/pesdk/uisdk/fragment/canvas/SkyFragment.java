package com.pesdk.uisdk.fragment.canvas;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.pesdk.bean.SortBean;
import com.pesdk.net.PENetworkApi;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.code.Segment;
import com.pesdk.uisdk.bean.code.SegmentResult;
import com.pesdk.uisdk.bean.model.ImageOb;
import com.pesdk.uisdk.bean.model.PipBgParam;
import com.pesdk.uisdk.data.vm.SkyVM;
import com.pesdk.uisdk.fragment.canvas.callback.SkyCallback;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.uisdk.util.helper.PEHelper;
import com.pesdk.uisdk.util.helper.PipHelper;
import com.vecore.models.PEImageObject;
import com.vecore.models.PEScene;

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
public class SkyFragment extends AbsCanvasFragment {


    public static SkyFragment newInstance() {
        Bundle args = new Bundle();
        SkyFragment fragment = new SkyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onResultImage(String path) {
        if (onStyleImp(path, false)) {
            checkChecked(null); //更改其他fragment的选中项
        }
    }


    private SkyVM mVM;
    private ViewPager2 rvPager;
    private TabLayout mTableLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_canvas, container, false);
        mVM = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(SkyVM.class);
        mVM.getSortData().observe(getViewLifecycleOwner(), this::onSortResult);
        insert = false;
        rvPager = $(R.id.vpager);
        rvPager.setSaveEnabled(false);
        mTableLayout = $(R.id.tabs);
        $(R.id.btnLocal).setOnClickListener(v -> {
            if (!isSky()) {
                onToastMsg();
                return;
            } else if (hasPersonBg()) {
                onToastMsg();
                return;
            }
            if (mResultLauncher != null) {
                mResultLauncher.launch(null);
            }
        });
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.pesdk_sky);
        return mRoot;
    }

    /**
     * 清理背景
     */
    private void none() {
        if (hasPersonBg()) {
            onToastMsg();
            return;
        }
        if (null != mScene) {
            mScene.setBackground(PEScene.UNKNOWN_COLOR);
            mEditCallback.getSkyHandler().exitEditMode();
            mScene.setBackground(null);
        } else {
            mCollageInfo.setBG(null);
            Utils.setSegment(mCollageInfo.getImageObject(), Segment.NONE);
        }
        mVideoEditorHandler.reBuild();

        checkChecked(null); //更改其他fragment的选中项
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVM.process();
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
                StyleFragment fragment = StyleFragment.newInstance(PENetworkApi.Sky);
                fragment.setSortApi(sortList.get(position));
                fragment.setCallback(mCallback);
                mCacheList.append(position, fragment);
                return fragment;
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
        mTableLayout.selectTab(mTableLayout.getTabAt(1));
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
            if (fragment instanceof StyleFragment) {
                ((StyleFragment) fragment).checkChecked();
            }
        }
    }

    @IMenu
    private final int mMenu = IMenu.sky;


    private boolean onStyleImp(String path, boolean changeTab) {
        ImageOb ob = null;
        if (null != mScene) {
//            ob = PEHelper.initImageOb(mScene.getPEImageObject());
        } else {
            ob = PEHelper.initImageOb(mCollageInfo.getImageObject());
        }
        if (ob.getSkyResult() == SegmentResult.AI_SUCCESS) {
            saveSync(mMenu);
            if (setStyle(path)) {
                if (changeTab) {
                    int index = rvPager.getCurrentItem();
                    if (mTableLayout.getSelectedTabPosition() != index + 1) {
                        mTableLayout.selectTab(mTableLayout.getTabAt(index + 1), true);
                        checkChecked(null);
                    } else {
                        checkChecked(mCacheList.get(index));
                    }
                }
                return true;
            }
        } else {
            onToastMsg();
        }
        return false;
    }

    private SkyCallback mCallback = new SkyCallback() {

        @Override
        public void onStyle(String path) {
            onStyleImp(path, true);
        }

        @Override
        public String getStyle() {
            if (null != mScene) {
                return null != mScene.getBackground() ? mScene.getBackground().getMediaPath() : null;
            } else if (null != mCollageInfo) {
                PEImageObject bg = mCollageInfo.getBG();
                if (null != bg && bg.getTag() instanceof PipBgParam) {
                    PipBgParam pipBgParam = (PipBgParam) bg.getTag();
                    return pipBgParam.getPath();
                }
            }
            return null;
        }
    };


    /**
     * 已经有了人像分割的背景，不再继续执行天空分割的逻辑
     */
    private boolean hasPersonBg() {
        ImageOb ob = PEHelper.initImageOb(mCollageInfo.getImageObject());
        return ob.getSkyResult() != SegmentResult.AI_SUCCESS && ob.getPersonResult() == SegmentResult.AI_SUCCESS && mCollageInfo.getBG() != null;
    }

    /**
     * 是否是天空类型图片
     */
    private boolean isSky() {
        PEImageObject imageObject = null;
        if (null != mCollageInfo) {
            imageObject = mCollageInfo.getImageObject();
        }
        ImageOb ob = PEHelper.initImageOb(imageObject);
        return ob.getSkyResult() == SegmentResult.AI_SUCCESS;
    }


    /**
     * 切换背景图
     */
    private boolean setStyle(String path) {
        if (hasPersonBg()) {
            onToastMsg();
            return false;
        }
        PipHelper.onPipSegment(mCollageInfo, Segment.SEGMENT_SKY);
        PipHelper.onPipBGStyle(mCollageInfo, 0, path);
        mVideoEditorHandler.reBuild();
        return true;
    }

    @Override
    void onToastMsg() {
        onToast(R.string.pesdk_toast_sky_segment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRoot = null;
        mTableLayout = null;
        rvPager = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
//                    if (null != mScene) {
//                        UndoInfo info = mVideoEditorHandler.getParamHandler().onDeleteStep();
//                        if (null != info && info.getList() != null && info.getMode() == IMenu.sky && info.getList().size() > 0) {
//                            mVideoEditorHandler.getParamHandler().restorePE((SceneInfo) info.getList().get(0));
//                        }
//                        mEditCallback.getSkyHandler().exitEditMode();
//                    } else {
                    mVideoEditorHandler.getParamHandler().onUndo();
//                    }
                    mVideoEditorHandler.reBuild();
                    mVideoEditorHandler.onBack();
                }
            });
        } else {
            // 无修改
            mEditCallback.getSkyHandler().exitEditMode();
            mVideoEditorHandler.onBack();
        }
    }

    @Override
    public void onSureClick() {
        mEditCallback.getSkyHandler().exitEditMode();
        mVideoEditorHandler.onSure();
    }
}
