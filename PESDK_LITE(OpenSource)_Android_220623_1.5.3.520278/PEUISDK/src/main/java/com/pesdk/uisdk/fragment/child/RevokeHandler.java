package com.pesdk.uisdk.fragment.child;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.pesdk.uisdk.R;

/**
 * 画笔|消除笔界面   4个菜单
 */
public class RevokeHandler {


    private ViewGroup mViewGroup;
    private IRevokeListener mListener;
    private View ivRevoke, ivUndo, ivReset, ivDiff;

    public RevokeHandler(ViewGroup viewGroup, boolean visibleDiff, boolean visibleReset, IRevokeListener listener) {
        mViewGroup = viewGroup;
        mListener = listener;

        ivRevoke = mViewGroup.findViewById(R.id.btnChildRevoke);
        ivUndo = mViewGroup.findViewById(R.id.btnChildUndo);
        ivReset = mViewGroup.findViewById(R.id.btnChildReset);
        ivDiff = mViewGroup.findViewById(R.id.btnChildDiff);
        ivReset.setVisibility(visibleReset ? View.VISIBLE : View.GONE);
        ivDiff.setVisibility(visibleDiff ? View.VISIBLE : View.GONE);

        ivRevoke.setOnClickListener(v -> mListener.onRevoke());
        ivUndo.setOnClickListener(v -> mListener.onUndo());
        ivReset.setOnClickListener(v -> mListener.onReset());
        ivDiff.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mListener.onDiffBegin();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mListener.onDiffEnd();
            }
            return false;
        });
    }


    public void setRevokeEnable(boolean enable) {
        ivRevoke.setEnabled(enable);
    }

    public void setUndoEnable(boolean enable) {
        ivUndo.setEnabled(enable);
    }

    public void setResetEnable(boolean enable) {
        ivReset.setEnabled(enable);
    }

    public void setDiffEnable(boolean enable) {
        ivDiff.setEnabled(enable);
    }

}
