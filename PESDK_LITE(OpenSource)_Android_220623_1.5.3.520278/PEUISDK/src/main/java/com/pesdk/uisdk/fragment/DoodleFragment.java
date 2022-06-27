package com.pesdk.uisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.model.GraffitiInfo;
import com.pesdk.uisdk.fragment.child.IRevokeListener;
import com.pesdk.uisdk.fragment.child.RevokeHandler;
import com.pesdk.uisdk.fragment.helper.StrokeHandler;
import com.pesdk.uisdk.listener.IEditCallback;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.util.PathUtils;
import com.pesdk.uisdk.widget.ColorBar;
import com.pesdk.uisdk.widget.doodle.DoodleView;
import com.pesdk.uisdk.widget.doodle.bean.Mode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.pesdk.uisdk.widget.doodle.DoodleView.GRAPH_TYPE.ARROW;
import static com.pesdk.uisdk.widget.doodle.DoodleView.GRAPH_TYPE.RECT;


/**
 * 涂鸦- 用户自由绘制模式 操作面板  可设置画笔粗细 画笔颜色
 */
public class DoodleFragment extends BaseFragment {
    public static final String TAG = "DoodleFragment";

    public static DoodleFragment newInstance() {
        return new DoodleFragment();
    }

    private DoodleView mDoodleView;
    private ImageView mEraserView;
    private RevokeHandler mRevokeHandler;
    private SeekBar mBar;
    private ViewGroup mMenuRevokeLayout;
    private ColorBar mColorBar;
    private IEditCallback mEditCallback;
    private ImageHandlerListener mVideoHandlerListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mEditCallback = (IEditCallback) context;
        mVideoHandlerListener = (ImageHandlerListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_graffiti_layout, container, false);
        return mRoot;
    }

    public void setMenuRevokeLayout(ViewGroup menuRevokeLayout) {
        mMenuRevokeLayout = menuRevokeLayout;
    }


    public void setDoodleView(DoodleView doodleView) {
        mDoodleView = doodleView;
    }

    private StrokeHandler mStrokeHandler;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.pesdk_doodling);
        mBar = $(R.id.sbStrokeWdith);
        mBar.setProgress(50);
        mStrokeHandler = new StrokeHandler(mBar, mDoodleView);
        mStrokeHandler.init();
        mEraserView = $(R.id.paint_eraser);
        mColorBar = $(R.id.colorBar);
        mColorBar.setCallback(new ColorBar.Callback() {
            @Override
            public void onNone() {

            }

            @Override
            public void onColor(int color) {
                mDoodleView.setPaintColor(color);
            }
        });

        mEraserView = $(R.id.paint_eraser);
        mEraserView.setOnClickListener(v -> {


        });
        RadioButton rbRect = $(R.id.rb_doodle_rect);
        rbRect.setOnClickListener(v -> {
            mDoodleView.setGraphType(RECT);
        });
        RadioButton rbTriangle = $(R.id.rb_doodle_triangle);
        rbTriangle.setOnClickListener(v -> {
            mDoodleView.setGraphType(ARROW);
        });
        RadioButton rbFree = $(R.id.rb_doodle_free);
        rbFree.setOnClickListener(v -> {
            mDoodleView.setMode(Mode.DOODLE_MODE);
        });
        mDoodleView.setMode(Mode.DOODLE_MODE); //默认涂鸦
        mDoodleView.setEditable(true);

        mMenuRevokeLayout.findViewById(R.id.btnChildRevoke).setVisibility(View.VISIBLE);
        mMenuRevokeLayout.findViewById(R.id.btnChildUndo).setVisibility(View.VISIBLE);
        mMenuRevokeLayout.findViewById(R.id.btnChildReset).setVisibility(View.GONE);
        mRevokeHandler = new RevokeHandler(mMenuRevokeLayout, true, false, new IRevokeListener() {
            @Override
            public void onRevoke() {
                mDoodleView.revoke();
                checkUIStatus();
            }

            @Override
            public void onUndo() {
                mDoodleView.undo();
                checkUIStatus();
            }

            @Override
            public void onReset() {
                mDoodleView.reset();
                mDoodleView.postInvalidate();
                checkUIStatus();
            }

            @Override
            public void onDiffBegin() {
                mDoodleView.setVisibility(View.GONE);
            }

            @Override
            public void onDiffEnd() {
                mDoodleView.setVisibility(View.VISIBLE);
            }

        });

        mDoodleView.setCallBack(new DoodleView.DoodleCallback() {
            @Override
            public void onDrawStart() {

            }

            @Override
            public void onDrawing() {

            }

            @Override
            public void onDrawComplete(boolean hand) {
                mDoodleView.postInvalidate();
            }


            @Override
            public void onRevertStateChanged(boolean canRevert) {
                checkUIStatus();
            }
        });
        checkUIStatus();
        mDoodleView.setPaintColor(mColorBar.getColor());
    }


    private void checkUIStatus() {
        mRevokeHandler.setRevokeEnable(mDoodleView.getRevokeSize() > 0);
        mRevokeHandler.setUndoEnable(mDoodleView.getUndoSize() > 0);
        mRevokeHandler.setResetEnable(!mDoodleView.isEmpty());
        mRevokeHandler.setDiffEnable(!mDoodleView.isEmpty());
    }


    @Override
    public void onCancelClick() {
        showAlert(new AlertCallback() {
            @Override
            public void cancel() {

            }

            @Override
            public void sure() {
                mMenuCallBack.onCancel();
            }
        });

    }


    @Override
    public void onSureClick() {
        if (!mDoodleView.isEmpty()) { //有绘制内容
            String path = PathUtils.getTempFileNameForSdcard("Temp_graffiti", "png");
            mDoodleView.save(path);
            mDoodleView.reset();
            GraffitiInfo graffitiInfo = new GraffitiInfo();
            graffitiInfo.setPath(path);
            graffitiInfo.createObject();
            mEditCallback.getEditDataHandler().addGraffiti(graffitiInfo);
            mVideoHandlerListener.getEditorImage().updateSubtitleObject(graffitiInfo.getLiteObject());
        }
        mMenuCallBack.onSure();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRoot = null;
        mMenuCallBack = null;
        mStrokeHandler = null;
    }
}
