package com.pesdk.uisdk.Interface;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pesdk.uisdk.R;

import androidx.annotation.NonNull;

/**
 * 解决输入法造成整体界面向上推，解决方案：输入框对应的fragment单独作为一个Frame  ，只推这一个frame， 播放器所在的Frame不动
 *
 * @author JIAN
 * @create 2019/4/24
 * @Describe
 */
public class OnSubGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
    private View root;
    private View scrollToView;
    private Rect mRootRect = new Rect();//Activity根布局的显示区域
    private Rect rectVisible = new Rect();//可见的区域（打开输入法后，此区域会变矮）
    private static final String TAG = "SubGlobalLayoutListener";
    private View mLlWordEditer;
    private int mEditParentHeight;
    private int mMenuBarHeight;  //上部控制栏的高度
    private RadioGroup mRadioGroup;

    /**
     * 输入法是否已打开
     *
     * @return
     */
    public boolean isShowInput() {
        return bInputOpenEd;
    }

    private int rHeight;
    private boolean isFullScreen = false; //挖孔屏rectVisible有差异
    private final int MIN_INPUT_HEIGHT = 300; //最小的输入法高度
    private InputListener mListener;

    public OnSubGlobalLayoutListener(@NonNull View root, @NonNull View scrollToView, @NonNull View mLlWordEditer, RadioGroup radioGroup, boolean fullscreen, InputListener listener) {
        this.root = root;
        mListener = listener;
        this.scrollToView = scrollToView;
        this.root.getGlobalVisibleRect(mRootRect);
        this.mLlWordEditer = mLlWordEditer;
        bInputOpenEd = false;
        mEditParentHeight = root.getResources().getDimensionPixelSize(R.dimen.input_edit_parent_height);
        mMenuBarHeight = root.getResources().getDimensionPixelSize(R.dimen.input_edit_parent_height);
        rHeight = root.getRootView().getHeight(); //
        root.getWindowVisibleDisplayFrame(rectVisible); //当前状态下的content有效区域
        isFullScreen = fullscreen;
        int viewH = scrollToView.getHeight();
        defaultY = (isFullScreen ? rectVisible.height() : rectVisible.bottom) - viewH; //部分原生手机底部有间隙，需排除  （此种方式可自动匹配出全屏、非全屏、虚拟导航栏）
//        Log.e(TAG, "OnSubGlobalLayoutListener: " + mRootRect + " " + rectVisible + " >" + viewH
//                + " defaultY:" + defaultY
//                + " getStatusBarHeight: " + CoreUtils.getStatusBarHeight(root.getContext()) + " vi :" + CoreUtils.getVirtualBarHeight(root.getContext()) + " rHeight:" + rHeight + " isFullScreen:" + isFullScreen);

        //A60
//       a :  OnSubGlobalLayoutListener: Rect(0, 0 - 1080, 2340) Rect(0, 112 - 1080, 2214) >607 defaultY:1607 getStatusBarHeight: 112 vi :237 rHeight:2340
//      b:  OnSubGlobalLayoutListener: Rect(0, 0 - 1080, 2102) Rect(0, 112 - 1080, 2214) >525 defaultY:1577 getStatusBarHeight: 112 vi :237 rHeight:2228 isFullScreen:true
        //8T
//        OnSubGlobalLayoutListener: Rect(0, 0 - 1080, 2296) Rect(0, 104 - 1080, 2400) >563 defaultY:1837 getStatusBarHeight: 104 vi :103 rHeight:2296 isFullScreen:false
//        OnSubGlobalLayoutListener: Rect(0, 0 - 1080, 2400) Rect(0, 104 - 1080, 2400) >608 defaultY:1792 getStatusBarHeight: 104 vi :103 rHeight:2400 isFullScreen:false
        mRadioGroup = radioGroup;
    }

    //输入法是否打开
    private boolean bInputOpenEd = false;
    private int defaultY = 0;//没打开键盘时的Y

    private int nLastCheckId;

    @Override
    public void onGlobalLayout() {
        root.getWindowVisibleDisplayFrame(rectVisible);
        int rootInvisibleHeight = rHeight - (isFullScreen ? rectVisible.height() : rectVisible.bottom);
//        Log.e(TAG, "onGlobalLayout: " + rHeight + " >" + rectVisible);
        // 若不可视区域高度大于200，则键盘显示
        if (rootInvisibleHeight > MIN_INPUT_HEIGHT) {
            // rootInvisibleHeight值为输入法Frame的高度
            int[] location = new int[2];
            // 获取scrollToView在窗体的坐标
            mLlWordEditer.getLocationInWindow(location);
            int tY = rHeight - rootInvisibleHeight - mEditParentHeight - mMenuBarHeight;
//            Log.e(TAG, "onGlobalLayout:   打开键盘:" + tY + "  " + Arrays.toString(location) + "  mEditParentHeight:" + mEditParentHeight + "  mLlWordEditer:" + mLlWordEditer.getHeight());
            if (scrollToView != null) {
                if (location[1] > tY && !bInputOpenEd) {
                    bResetRbMenu = true;
                    nLastCheckId = mRadioGroup.getCheckedRadioButtonId();
                    ((RadioButton) mRadioGroup.findViewById(R.id.subtitle_input)).setChecked(true);
                    // 输入法打开对于目标区域有遮挡
                    bInputOpenEd = true;
                    scrollToView.setY(tY);
                    if (null != mListener) {
                        mListener.onInput(true);
                    }
                }
            }
        } else {
            if (scrollToView != null && bInputOpenEd) {
//                testY();
                resetStyleUI();
                scrollToView.setY(defaultY);
                bInputOpenEd = false;
            }
        }
    }

    /**
     * 测试验证构造方法中的defaultY==re？
     */
    private void testY() {
        Rect tmp = new Rect();
        root.getWindowVisibleDisplayFrame(tmp); //当前状态下的content有效区域
        int re = tmp.bottom - scrollToView.getHeight();
        Rect viewPRect = new Rect();
        scrollToView.getWindowVisibleDisplayFrame(viewPRect);
        Log.e(TAG, "onGlobalLayout: yincang  " + re + "==" + defaultY + " tmp ：" + tmp + "   viewPRect:" + viewPRect);
    }

    /**
     * 是否需要重置选中的菜单
     *
     * @param reset true  恢复选中的菜单 ；false 不需要重置 场景：编辑状态，主动切换选中项时
     */
    public void setResetRbMenu(boolean reset) {
        this.bResetRbMenu = reset;
    }

    private boolean bResetRbMenu = true;

    private void resetStyleUI() {
        if (nLastCheckId != -1 && bResetRbMenu) {
            if (nLastCheckId == R.id.subtitle_input) {
                nLastCheckId = R.id.subtitle_style;
            }
            if (null != mListener) {
                mListener.onInput(false);
            }
            try {
                View view = mRadioGroup.findViewById(nLastCheckId);
                if (view instanceof RadioButton) {
                    ((RadioButton) view).setChecked(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    //主动恢复到默认UI状态
    public void resetUI() {
        Log.e(TAG, "resetUI: " + defaultY + " >" + bInputOpenEd);
        if (defaultY > 0) {
            // 键盘隐藏
            if (scrollToView != null) {
                resetStyleUI();
                scrollToView.setY(defaultY);
                bInputOpenEd = false;
            }
        }
    }
}

