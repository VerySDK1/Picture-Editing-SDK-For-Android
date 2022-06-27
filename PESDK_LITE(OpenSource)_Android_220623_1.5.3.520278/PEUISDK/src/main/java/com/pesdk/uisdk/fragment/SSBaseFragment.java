package com.pesdk.uisdk.fragment;

import android.content.Context;

import com.pesdk.uisdk.bean.model.ITimeLine;
import com.pesdk.uisdk.edit.EditDragHandler;
import com.pesdk.uisdk.listener.IEditCallback;
import com.pesdk.uisdk.listener.ImageHandlerListener;

/**
 * 字幕、贴纸公共部分
 */
public abstract class SSBaseFragment<E extends ITimeLine> extends EditBaseFragment<E> {

    protected ImageHandlerListener mListener;
    protected EditDragHandler mDragHandler;

    protected E mCurrentInfo;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = getContext();
        if (getActivity() instanceof ImageHandlerListener) {
            mListener = (ImageHandlerListener) getActivity();
        }
        if (context instanceof IEditCallback) {
            mDragHandler = ((IEditCallback) context).getEditDragHandler();
        }
    }


    /**
     * 新增单个
     */
    abstract void onBtnAddClick();


    /**
     * 默认状态下，清除recycleview中被选中的item  、 隐藏 编辑、 删除 、退出缩略图编辑模式
     */
    abstract void resetUI();



}
