package com.pesdk.uisdk.fragment;

/**
 * 支持编辑元素
 */
public abstract class EditBaseFragment<E> extends BaseFragment {

    protected E mEditInfo; //要编辑的元素
    protected E mBkEdit;//备份一份正在编辑的item。用于放弃更改,恢复原始
    protected boolean isEditItem = false; //true: 选中单个字幕|贴纸。 二次编辑

    public void setEditInfo(E info) {
        mEditInfo = info;
        isEditItem = info != null;
        mBkEdit = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isEditItem = false; //清理编辑状态
        mEditInfo = null;
    }
}
