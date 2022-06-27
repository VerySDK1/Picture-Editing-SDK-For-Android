package com.pesdk.uisdk.fragment.main;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.ui.home.EditBaseActivity;
import com.vecore.base.lib.utils.CoreUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * 工具栏主菜单
 */
public class MainMenuFragment extends AbsBaseFragment {


    public static MainMenuFragment newInstance() {
        return new MainMenuFragment();
    }

    private MenuCallback mMenuCallback;
    private ImageHandlerListener mVideoHandlerListener;

    @IMenu
    public int getMenu() {
        return mMenu;
    }

    @IMenu
    private int mMenu = IMenu.MODE_PREVIEW;


    public void resetMainEdit() {
        mMenu = IMenu.MODE_PREVIEW;
    }

    /**
     * 编辑单个类型的元素
     */
    public void setMenuEdit(@IMenu int menuId) {
        mMenu = menuId;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mMenuCallback = (MenuCallback) context;
        mVideoHandlerListener = (ImageHandlerListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMenuCallback = null;
        mVideoHandlerListener = null;
    }


    private ViewGroup mMenuGroup;
    private View btnRect, btnBack;
    private TextView mBG;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_main_menu_layout, container, false);
        mMenuGroup = $(R.id.mMenuGroup);
        btnRect = $(R.id.btn_rect_adjust);
        btnBack = $(R.id.btn_child_back);
        mBG = $(R.id.btn_canvas);
        initLayout();
        initView();
        onMainUI();
        return mRoot;
    }


    private void initLayout() {
        int size = (int) (CoreUtils.getMetrics().widthPixels / 5.6);
        size = size - CoreUtils.dpToPixel(8 * 2); //margin 8dp*2
        boolean isEn = ((EditBaseActivity) getActivity()).isEn();
        int len = mMenuGroup.getChildCount();
        for (int i = 0; i < len; i++) {
            View view = mMenuGroup.getChildAt(i);
            if (isEn) {
                int vId = view.getId();
                if (vId == R.id.btn_canvas || vId == R.id.btn_import || vId == R.id.btn_dewatermark
                        || vId == R.id.btn_proportion || vId == R.id.btn_proportion2
                        || vId == R.id.btn_koutu || vId == R.id.btn_mask
                ) { //英文翻译较长.组件增加宽度
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                    lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    view.setLayoutParams(lp);
                } else {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                    lp.width = size;
                    view.setLayoutParams(lp);
                }
            } else {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                lp.width = size;
                view.setLayoutParams(lp);
            }

        }
    }

    private void initView() {
        $(R.id.btn_sticker).setOnClickListener(mClickListener);
        proportion = $(R.id.btn_proportion);
        proportion.setOnClickListener(mClickListener);

        koutu = $(R.id.btn_koutu);
        koutu.setOnClickListener(mClickListener);
        $(R.id.btn_text).setOnClickListener(mClickListener);

        $(R.id.btn_filter).setOnClickListener(mClickListener);
        $(R.id.btn_beauty).setOnClickListener(mClickListener);
        crop = $(R.id.btn_crop);
        crop.setOnClickListener(mClickListener);
        $(R.id.btn_smear).setOnClickListener(mClickListener);
        $(R.id.btn_graffiti).setOnClickListener(mClickListener);
        adjust = $(R.id.btn_adjust);
        adjust.setOnClickListener(mClickListener);
        $(R.id.btn_blur).setOnClickListener(mClickListener);
        $(R.id.btn_aperture).setOnClickListener(mClickListener);
        $(R.id.btn_hdr).setOnClickListener(mClickListener);
        $(R.id.btn_holy_light).setOnClickListener(mClickListener);
        $(R.id.btn_dewatermark).setOnClickListener(mClickListener);
        $(R.id.btn_watermark).setOnClickListener(mClickListener);
        $(R.id.btn_pip).setOnClickListener(mClickListener);
        mBG.setOnClickListener(mClickListener);
        $(R.id.btn_sky).setOnClickListener(mClickListener);
        $(R.id.btn_overlay).setOnClickListener(mClickListener);

        $(R.id.btn_mosaic).setOnClickListener(mClickListener);
        mask = $(R.id.btn_mask);
        mask.setOnClickListener(mClickListener);
        depth = $(R.id.btn_depth);
        depth.setOnClickListener(mClickListener);
        $(R.id.btn_frame).setOnClickListener(mClickListener);

        adjust.setVisibility(View.VISIBLE);
        depth.setVisibility(View.GONE);
        $(R.id.btn_import).setOnClickListener(mClickListener);
        $(R.id.btn_replace).setOnClickListener(mClickListener);
        btnRect.setOnClickListener(mClickListener);
        $(R.id.btn_child_back).setOnClickListener(mClickListener);


        $(R.id.btn_text2).setOnClickListener(mClickListener);
        $(R.id.btn_sticker2).setOnClickListener(mClickListener);
        $(R.id.btn_frame2).setOnClickListener(mClickListener);
        $(R.id.btn_overlay2).setOnClickListener(mClickListener);
        $(R.id.btn_proportion2).setOnClickListener(mClickListener);
        $(R.id.btn_graffiti2).setOnClickListener(mClickListener);
    }

    public void onMainUI() {
        if (null == mRoot) {
            return;
        }
        {
            fixBg(false);
            onMenu1(View.GONE);
            onMenu2(View.VISIBLE);
            onMenu3(View.GONE);
        }
        Log.e(TAG, "onMainUi: " + mMenu + " =? " + IMenu.MODE_PREVIEW);
    }

    private View adjust, depth, crop, mask, koutu, proportion;

    private View.OnClickListener mClickListener = v -> onCheckId(v.getId());


    /**
     * 文字、素材、图层、叠加    需微调位置
     */
    public void onSelectItem(@IMenu int menu, int index) {
        Log.e(TAG, "onSelectItem: " + menu + " " + index + " " + mRoot);
        if (null == mRoot) {
            return;
        }
        mMenu = menu;
        if (index >= 0) {
            if (menu == IMenu.pip) { //编辑画中画
                onPipSelected();
            } else if (menu == IMenu.overlay) { //叠加
                fixBg(false);
                onTextSelected();
                $(R.id.btn_replace).setVisibility(View.VISIBLE);
            } else { //选中文字、贴纸...
                fixBg(false);
                onTextSelected();
                $(R.id.btn_replace).setVisibility(View.VISIBLE);
            }
        } else {
            onMainUI();
        }
    }

    /**
     * 选中画中画
     */
    public void onPipSelected() {
        Log.e(TAG, "onPipSelected: ");
        if (null == mRoot) {
            return;
        }

        fixBg(true);
        onMenu2(View.GONE);
        onMenu1(View.VISIBLE);
        onMenu3(View.VISIBLE);
    }

    private void fixBg(boolean isPip) {
        if (isPip) {
            mBG.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.pesdk_ic_pip_bg, 0, 0);
            mBG.setText(R.string.pesdk_pip_bg);
        } else {
            mBG.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.pesdk_ic_canvas_n, 0, 0);
            mBG.setText(R.string.pesdk_canvas);
        }
    }


    /**
     * 选中文字、贴纸
     */
    private void onTextSelected() {
        Log.e(TAG, "onTextSelected: ");
        if (null == mRoot) {
            return;
        }
        onMenu1(View.GONE);
        onMenu2(View.VISIBLE);
        onMenu3(View.GONE);
    }


    /**
     * 选中画中画时（fg）: 抠图、天空、人像 、裁剪、消除笔、蒙版、替换
     */
    private void onMenu1(int visibility) {
        koutu.setVisibility(visibility);
        $(R.id.btn_blur).setVisibility(visibility);
        $(R.id.btn_sky).setVisibility(visibility);
        $(R.id.btn_beauty).setVisibility(visibility);
        $(R.id.btn_crop).setVisibility(visibility);
        $(R.id.btn_dewatermark).setVisibility(visibility);
        $(R.id.btn_replace).setVisibility(visibility);
        $(R.id.btn_mask).setVisibility(visibility);
        btnRect.setVisibility(visibility);
        btnBack.setVisibility(visibility);
//        $(R.id.btn_pip).setVisibility(visibility);
    }

    /**
     * 普通主轨（All）： 文字、素材、叠加、边框、画笔、比例
     *
     * @param visibility
     */
    private void onMenu2(int visibility) {
//        Log.e(TAG, "onMenu2: " + visibility + " " + mVideoHandlerListener.getParamHandler().getParam().getExtImage().getBackground());
        if (visibility == View.VISIBLE && mVideoHandlerListener.getParamHandler().getExtImage().getBackground() != null) { //有背景
            $(R.id.btn_blur).setVisibility(visibility);
        } else {
            $(R.id.btn_blur).setVisibility(View.GONE);
        }
        $(R.id.btn_text).setVisibility(visibility);
        $(R.id.btn_sticker).setVisibility(visibility);
        $(R.id.btn_overlay).setVisibility(visibility);
        $(R.id.btn_frame).setVisibility(visibility);
        $(R.id.btn_graffiti).setVisibility(visibility);
        $(R.id.btn_proportion).setVisibility(visibility == View.VISIBLE ? (mVideoHandlerListener.getParamHandler().getParam().getFrameList().size() == 0 ? View.VISIBLE : View.GONE) : visibility);
//        $(R.id.btn_pip).setVisibility(visibility);
    }


    /**
     * 选中画中画（部分菜单排版到末尾）： 文字、素材、叠加、边框、画笔、比例
     *
     * @param visibility
     */
    private void onMenu3(int visibility) {
        $(R.id.btn_text2).setVisibility(visibility);
        $(R.id.btn_sticker2).setVisibility(visibility);
        $(R.id.btn_overlay2).setVisibility(visibility);
        $(R.id.btn_frame2).setVisibility(visibility);
        $(R.id.btn_graffiti2).setVisibility(visibility);
        $(R.id.btn_proportion2).setVisibility(visibility == View.VISIBLE ? (mVideoHandlerListener.getParamHandler().getParam().getFrameList().size() == 0 ? View.VISIBLE : View.GONE) : visibility);
    }

    public void addText() {
        mMenuCallback.onText(null);
    }

    public void addSticker() {
        mMenuCallback.onSticker(null);
    }

    public void addOverlay() {
        mVideoHandlerListener.preMenu();
        mMenuCallback.onOverlay();
    }


    public void addLayer() {
        mMenuCallback.onAddLayer();
    }

    public void setProportionEanble(boolean enable) {
        if (null != proportion) {
            proportion.setVisibility(enable ? View.VISIBLE : View.GONE);
        }
    }

    private void onCheckId(int vId) {
        if (vId == R.id.btn_child_back) {//返回主菜单
            mMenuCallback.onBack2Main();
        } else if (vId == R.id.btn_import) { //添加
            addLayer();
        } else if (vId == R.id.btn_sticker || vId == R.id.btn_sticker2) { //贴纸
            mVideoHandlerListener.preMenu();
            addSticker();
        } else if (vId == R.id.btn_text || vId == R.id.btn_text2) { //文字
            mVideoHandlerListener.preMenu();
            addText();
        } else if (vId == R.id.btn_graffiti || vId == R.id.btn_graffiti2) {//涂鸦
            mVideoHandlerListener.preMenu();
            mMenu = IMenu.graffiti;
            mMenuCallback.onGraffiti();
        } else if (vId == R.id.btn_proportion || vId == R.id.btn_proportion2) {//比例
            mVideoHandlerListener.preMenu();
            mMenuCallback.onProportion();
        } else if (vId == R.id.btn_blur) {//模糊度
            mMenu = IMenu.blur;
            mMenuCallback.onBlur();
        } else if (vId == R.id.btn_replace) { //替换
            mMenuCallback.onReplace();
        } else if (vId == R.id.btn_rect_adjust) { //微调
            mMenuCallback.onRectAdjust();
        }
        //这4种功能,图册也支持
        else if (vId == R.id.btn_crop) {//裁剪
            mMenu = IMenu.crop;
            mMenuCallback.onCrop();
        } else if (vId == R.id.btn_filter) {//滤镜
            mMenu = IMenu.filter;
            mMenuCallback.onFilter();
        } else if (vId == R.id.btn_beauty) { //美颜
            mMenuCallback.onBeauty();
        } else if (vId == R.id.btn_adjust) { //调整
            mMenu = IMenu.adjust;
            mMenuCallback.onAdjust();
        } else if (vId == R.id.btn_watermark) { //加水印图片
            mVideoHandlerListener.preMenu();
            mMenu = IMenu.watermark;
            mMenuCallback.onWatermark();
        } else if (vId == R.id.btn_dewatermark) { //去水印
            mMenuCallback.onErase();
        } else if (vId == R.id.btn_pip) {
            mVideoHandlerListener.preMenu();
            mMenu = IMenu.pip;
            mMenuCallback.onPip();
        } else if (vId == R.id.btn_canvas) { //背景
            mMenuCallback.onCanvas();
        } else if (vId == R.id.btn_sky) { //天空
            mMenuCallback.onSky();
        } else if (vId == R.id.btn_koutu) { //抠图
            mMenuCallback.onKoutu();
        } else if (vId == R.id.btn_overlay || vId == R.id.btn_overlay2) {
            mVideoHandlerListener.preMenu();
            mMenu = IMenu.overlay;
            mMenuCallback.onOverlay();
        } else if (vId == R.id.btn_mosaic) {
            mVideoHandlerListener.preMenu();
            mMenu = IMenu.mosaic;
            mMenuCallback.onMosaic();
        } else if (vId == R.id.btn_mask) {
            mMenuCallback.onMask();
        } else if (vId == R.id.btn_depth) {
            mVideoHandlerListener.preMenu();
            mMenu = IMenu.depth;
            mMenuCallback.onDepth();
        } else if (vId == R.id.btn_frame || vId == R.id.btn_frame2) {
            mMenu = IMenu.frame;
            mMenuCallback.onFrame();
        } else {
            Log.e(TAG, "onCheckId: " + vId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRoot = null;
        adjust = null;
        depth = null;
        crop = null;
        mask = null;
        koutu = null;
    }

}