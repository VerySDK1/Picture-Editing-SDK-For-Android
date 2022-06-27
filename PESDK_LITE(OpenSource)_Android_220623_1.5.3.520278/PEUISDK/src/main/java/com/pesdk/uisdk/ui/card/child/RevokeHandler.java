package com.pesdk.uisdk.ui.card.child;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;

import com.pesdk.uisdk.R;

/**
 * 画笔|消除笔界面   4个菜单
 */
public class RevokeHandler {

    private IRevokeListener mListener;
    private View ivRevoke, ivUndo, ivReset, ivDiff, ivEarse;

    public RevokeHandler(Activity viewGroup, boolean visibleDiff, boolean visibleReset, IRevokeListener listener) {
        mListener = listener;

        ivRevoke = viewGroup.findViewById(R.id.btnChildRevoke);
        ivUndo = viewGroup.findViewById(R.id.btnChildUndo);
        ivReset = viewGroup.findViewById(R.id.btnChildReset);
        ivDiff = viewGroup.findViewById(R.id.btnChildDiff);
        ivEarse = viewGroup.findViewById(R.id.btnEarse);

        ivEarse.setOnClickListener(v -> {
            listener.onEarse();
        });
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


    public void setUndoVisibility(int visibility) {
        ivUndo.setVisibility(visibility);
        ivRevoke.setVisibility(visibility);
        ivDiff.setVisibility(visibility);
    }


    public void setEarseVisibility(int visibility) {
        ivEarse.setVisibility(visibility);
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
