package com.pesdk.uisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.adapter.BaseRVAdapter;
import com.pesdk.uisdk.adapter.LayerAdapter;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.internal.SdkEntryHandler;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.listener.OnItemClickListener;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.PEImageObject;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 图层-调节层次
 */
public class PipFragment extends BaseFragment {
    public static PipFragment newInstance() {
        Bundle args = new Bundle();
        PipFragment fragment = new PipFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView mRecyclerView;
    private ImageHandlerListener mEditorHandler;
    private LayerAdapter adapter;
    private ActivityResultLauncher<Void> mResultLauncher;
    private View ivAdd, ivDelete, ivUp, ivDown, ivSw;
    private CheckBox ivHide;
    private SeekBar sbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityResultContract<Void, ArrayList<String>> albumContract = SdkEntryHandler.getInstance().getAlbumContract();
        if (albumContract != null) {
            mResultLauncher = registerForActivityResult(albumContract, result -> {
                if (null != result && result.size() > 0) {
                    mRoot.postDelayed(() -> insertLayer(result.get(0)), 100);
                }
            });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mEditorHandler = (ImageHandlerListener) context;
    }

    private ArrayList<CollageInfo> mBkList; //记录进入fragment前的列表

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_layer_layout, container, false);
        //此数据用于退出时,比较列表数据是否有变化
        mBkList = mEditorHandler.getParamHandler().getCloneCollageInfos();
        //1.自动添加一个调整步骤
        mEditorHandler.getParamHandler().onSaveAdjustStep(IMenu.pip);
        //2.暂停记录事件（待退出界面时,直接替换collagelist数据)
        mEditorHandler.getParamHandler().pause();
        return mRoot;
    }


    /**
     * 选中单个
     */
    private void onItemCheck(int position, CollageInfo info) {
        mEditorHandler.getForeground().exitEditMode();

        ivDelete.setEnabled(true);
        ivDown.setEnabled(position != 0);
        ivUp.setEnabled(adapter.getItemCount() - 1 != position);  //不是最上层的layer

        ivHide.setChecked(info.isHide());
        sbar.setEnabled(!info.isHide());
        fixAlpha(info.getAlpha());
        mEditorHandler.getForeground().reEdit(info, false, false); //再次编辑
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.pesdk_menu_layer);
        mRecyclerView = $(R.id.rv);
        adapter = new LayerAdapter(Glide.with(this), true);
        mRecyclerView.setAdapter(adapter);
        List<CollageInfo> list = initData();
        adapter.addAll(list, list.size() > 0 ? 0 : BaseRVAdapter.UN_CHECK);
        adapter.setOnItemClickListener((OnItemClickListener<CollageInfo>) (position, item) -> {
            onItemCheck(position, item);
        });
        sbar = $(R.id.sbar_range);
        sbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int last = -1;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (Math.abs(last - progress) > 3) {
                        last = progress;
                        changeAlpha(progress * 1.0f / sbar.getMax());
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                last = -1;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                changeAlpha(seekBar.getProgress() * 1.0f / sbar.getMax());
            }
        });

        if (list.size() > 0) {
            CollageInfo obj = list.get(0);
            fixAlpha(obj.getAlpha());
        } else {
            fixAlpha(1f);
        }
        ivAdd = $(R.id.ivAdd);
        ivDelete = $(R.id.ivDelete);
        ivUp = $(R.id.ivUp);
        ivDown = $(R.id.ivDown);
        ivSw = $(R.id.ivSw);
        ivHide = $(R.id.ivHide);
        initLisener();
        mRoot.postDelayed(() -> {
            int tmp = adapter.getChecked();
            if (tmp >= 0) {
                onItemCheck(tmp, adapter.getItem(tmp));
            }
        }, 100);
    }

    /**
     * 恢复透明度
     *
     * @param alpha
     */
    private void fixAlpha(float alpha) {
        sbar.setProgress((int) (alpha * sbar.getMax()));
    }

    private void initLisener() {
        ivAdd.setOnClickListener(v -> {
            if (mResultLauncher != null) {
                mResultLauncher.launch(null);
            }
        });
        ivDelete.setOnClickListener(v -> {
            CollageInfo data = getCheckedData();
            if (data == null) {
                return;
            }
            unCheck();
            mEditorHandler.getForeground().exitEditMode();
            mEditorHandler.getForeground().delete(data); //删除当前Layer
            adapter.addAll(initData(), BaseRVAdapter.UN_CHECK);
        });
        ivUp.setOnClickListener(v -> {
            CollageInfo data = getCheckedData();
            if (data == null) {
                return;
            }
            onUp(data);
        });
        ivDown.setOnClickListener(v -> {
            CollageInfo data = getCheckedData();
            if (data == null) {
                return;
            }
            onDown(data);
        });
        ivHide.setOnClickListener(v -> {
            CollageInfo data = getCheckedData();
            if (data == null) {
                return;
            }
            v.postDelayed(() -> onHide(data), 100);
        });
    }

    /**
     * 隐藏单个
     */
    private void onHide(CollageInfo info) {
        boolean isHide = ivHide.isChecked();
        sbar.setEnabled(!isHide);
        info.setHide(isHide);
        adapter.notifyDataSetChanged();
    }

    /**
     * 向下移动
     */
    private void onDown(CollageInfo info) {
        mEditorHandler.getForeground().exitEditMode(); //退出Layer选中状态
        List<CollageInfo> collageInfos = mEditorHandler.getParamHandler().getParam().getCollageList();
        int index = getIndexInLayerList(info, collageInfos);
        if (index > 0) {
            CollageInfo select = collageInfos.remove(index);//移除当前选中项
            int bIndex = index - 1;
            collageInfos.add(index - 1, select);
            mEditorHandler.reBuild();

            refreshAdapter(bIndex);
            restoreChecked();
            mEditorHandler.getForeground().reEdit(select, false, false);
        }

    }

    private void restoreChecked() {
        int index = adapter.getChecked();
        if (index >= 0) {
            onItemCheck(index, adapter.getItem(index));
        }
    }


    /**
     * 层次往上移
     */
    private void onUp(CollageInfo data) {
        List<CollageInfo> collageInfos = mEditorHandler.getParamHandler().getParam().getCollageList();
        int index = getIndexInLayerList(data, collageInfos);
        if (index >= 0) {
            CollageInfo select = collageInfos.remove(index); //放到集合末尾
            int bIndex = index + 1;
            collageInfos.add(bIndex, select);
            mEditorHandler.reBuild();

            refreshAdapter(bIndex);
            restoreChecked();
            mEditorHandler.getForeground().reEdit(select, false, false);
        }

    }


    /**
     * 一个item都未选中
     */
    private void unCheck() {
        ivDelete.setEnabled(false);
        ivUp.setEnabled(false);
        ivDown.setEnabled(false);
        ivSw.setEnabled(false);
        ivHide.setEnabled(false);
    }


    private int getIndexInLayerList(Object info, List<CollageInfo> list) {
        int index = BaseRVAdapter.UN_CHECK;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (info == list.get(i)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private CollageInfo getCheckedData() {
        if (adapter.getChecked() >= 0) {
            return adapter.getItem(adapter.getChecked());
        }
        return null;
    }

    private final int LAST_INDEX = -2;

    /**
     * 新增图层Layer
     */
    private void insertLayer(String path) {
        ivHide.setChecked(false);
        sbar.setProgress(100);
        try {
            mEditorHandler.getForeground().onMixItemAdd(new PEImageObject(path), false, false);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        refreshAdapter(LAST_INDEX);
        restoreChecked();
    }

    /**
     * -2表示，选中最后项
     *
     * @param index
     */
    private void refreshAdapter(int index) {
        List<CollageInfo> list = initData();
        int len = list.size();
        adapter.addAll(list, index >= BaseRVAdapter.UN_CHECK ? index : (len > 0 ? len - 1 : BaseRVAdapter.UN_CHECK));
    }

    /**
     * 更改透明度
     *
     * @param alpha
     */
    private void changeAlpha(float alpha) {
        adapter.getItem(adapter.getChecked()).setAlpha(alpha);
    }

    @Override
    public void onCancelClick() {
        mEditorHandler.getForeground().exitEditMode(); //退出Layer选中状态
        adapter.clearChecked();
        unCheck();
        if (isChanged(initData())) { //数据有变化
            showAlert(new ProportionFragment.AlertCallback() {

                @Override
                public void cancel() {


                }

                @Override
                public void sure() {//放弃所有操作, 需要替换list为之前备份的数据
                    mEditorHandler.getParamHandler().onUndo();
                    mEditorHandler.getParamHandler().restorePipList(mBkList);
                    back();
                }
            });
        } else {
            //撤销进入界面时，自动记录的一条数据
            mEditorHandler.getParamHandler().onUndo();
            back();
        }
    }

    private void back() {
        mEditorHandler.getParamHandler().resume();
        mEditorHandler.reBuild();
        mMenuCallBack.onCancel();
    }

    /**
     * 进入前的数据和当前pip列表是否一致
     */
    private boolean isChanged(List<CollageInfo> tmp) {
        if (tmp.size() != mBkList.size()) {
            return true;
        }
        int len = mBkList.size();
        for (int i = 0; i < len; i++) {
            CollageInfo item = mBkList.get(i);
            if (!item.equals(tmp.get(i))) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onSureClick() {
        mEditorHandler.getParamHandler().resume();
        mMenuCallBack.onSure();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEditorHandler.getParamHandler().resume();
        if (null != mBkList) {
            mBkList.clear();
        }
    }

    private List<CollageInfo> initData() {
        return mEditorHandler.getParamHandler().getParam().getCollageList();
    }


}
