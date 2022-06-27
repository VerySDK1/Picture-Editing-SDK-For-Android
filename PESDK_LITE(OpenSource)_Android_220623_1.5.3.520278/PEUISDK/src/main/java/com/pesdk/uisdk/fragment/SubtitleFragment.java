package com.pesdk.uisdk.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pesdk.uisdk.Interface.OnSubGlobalLayoutListener;
import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.model.PresetStyle;
import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.bean.model.UndoInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.fragment.main.IMenu;
import com.pesdk.uisdk.fragment.sub.FlowerFragment;
import com.pesdk.uisdk.fragment.sub.StyleFragment;
import com.pesdk.uisdk.fragment.sub.SubtiltleFontFragment;
import com.pesdk.uisdk.fragment.sub.SubtitleBubbleFragment;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.uisdk.util.helper.SubUtils;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.base.lib.utils.InputUtls;
import com.vecore.models.caption.CaptionItem;

import org.jetbrains.annotations.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


/**
 * 字幕
 * 1. 每次进入。要么新增 要么编辑（需传入要编辑的对象）
 */
public class SubtitleFragment extends SSBaseFragment<WordInfoExt> {
    private View mTreeView;
    private VirtualImage mVirtualVideo;
    private EditText mEtSubtitle;
    private RadioGroup mRgMenu;
    private FrameLayout mLinearWords;
    private VirtualImageView mPlayer;
    private ImageView mIvClear;
    private int mLayoutWidth = 1024, mLayoutHeight = 1024;
    private View mLlWordEditer;
    private SubtiltleFontFragment mSubtiltleFontFragment;
    private SubtitleBubbleFragment mBubbleFragment;
    private FlowerFragment mFlowerFragment;
    private StyleFragment mStyleFragment;
    /**
     * 保存时间
     */
    private long mDraftTime = 0;


    /**
     * 时间
     */
    private static final int DEFAULT_TIME = 1000;


    public static SubtitleFragment newInstance() {
        SubtitleFragment subtitleFragment = new SubtitleFragment();
        Bundle bundle = new Bundle();
        subtitleFragment.setArguments(bundle);
        return subtitleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "SubtitleFragment";
        SubUtils.getInstance().exportDefault(getContext());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_fragment_subtitle_layout, container, false);
        mCurrentInfo = mEditInfo;
        return mRoot;
    }


    @Override
    public void setEditInfo(WordInfoExt info) {
        super.setEditInfo(info);
        if (isEditItem) {
            Log.e(TAG, "setEditInfo: " + info.getCaptionItem().getTextContent() + " . " + info.getCaption().getText());
            mBkEdit = info.copy();
        }

    }

    private SubtiltleFontFragment.ITTFHandlerListener mTTFListener = new SubtiltleFontFragment.ITTFHandlerListener() {

        @Override
        public void onItemClick(String ttf, int position) {
            if (null != mCurrentInfo) {
                if (ttf.equals(mContext.getString(R.string.pesdk_default_ttf))) {
                    mCurrentInfo.getCaptionItem().setFontFile(null);
                } else {
                    mCurrentInfo.getCaptionItem().setFontFile(ttf);
                }
                refresh();
            }
        }

        @Override
        public String getLocalFile() {
            return null != mCurrentInfo ? mCurrentInfo.getCaptionItem().getFontFile() : null;
        }
    };

    private boolean mIsAddCaption = false;// 新增true，编辑false
    private boolean isChangedByInputManager = true;
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            boolean empty = s.length() <= 0;
            if (isChangedByInputManager) {
                if (empty) {
                    mCurrentInfo.setText(mEtSubtitle.getHint().toString());
                } else {
                    String tmp = s.toString();
                    int maxLines = mEtSubtitle.getMaxLines();
                    if (maxLines > 0 && tmp.length() > maxLines) {
                        mCurrentInfo.setText(tmp.substring(0, maxLines));
                    } else {
                        mCurrentInfo.setText(tmp);
                    }
                }
                mCurrentInfo.refreshMeasuring();
                onSaveDraft();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void showInput() {
        InputUtls.showInput(mEtSubtitle);
    }

    private Runnable mInputRunnable = new Runnable() {
        @Override
        public void run() {
            bindWatcher();
            showInput();
            String text = mEtSubtitle.getText().toString();
            if (!getString(R.string.pesdk_sub_hint).equals(text)) {
                mEtSubtitle.setSelection(text.length());
            }
        }
    };
    private View.OnClickListener mClearSubtitle = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mEtSubtitle.setText("");
        }
    };

    /**
     * 装载fragment的容器
     */
    private View mFragmentContainer;
    //注册输入法监听，动态调整bu布局setY
    private OnSubGlobalLayoutListener mGlobalLayoutListener;
    private View.OnClickListener mRGItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.subtitle_input) { //键盘
                if (null != mGlobalLayoutListener && mGlobalLayoutListener.isShowInput()) {
                    mGlobalLayoutListener.setResetRbMenu(true);
                    hideInput();
                } else {
                    postShowInput();
                }
            } else {
                if (null != mGlobalLayoutListener) {
                    mGlobalLayoutListener.setResetRbMenu(false);
                }
                onMenu(v.getId());
            }
        }
    };

    private void bindWatcher() {
        mEtSubtitle.removeTextChangedListener(mTextWatcher);
        mEtSubtitle.addTextChangedListener(mTextWatcher);
    }

    private void removeWatcher() {
        InputUtls.hideKeyboard(mEtSubtitle);
        mEtSubtitle.removeTextChangedListener(mTextWatcher);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        $(R.id.subtitle_input).setOnClickListener(mRGItemClickListener);
        $(R.id.subtitle_style).setOnClickListener(mRGItemClickListener);
        $(R.id.subtitle_flower).setOnClickListener(mRGItemClickListener);
        $(R.id.subtitle_bubble).setOnClickListener(mRGItemClickListener);
        $(R.id.subtitle_ttf).setOnClickListener(mRGItemClickListener);
        initView();
        init();
        mRoot.post(() -> onBtnAddClick());
    }

    @Override
    public void onCancelClick() {
        showAlert(new AlertCallback() {
            @Override
            public void cancel() {

            }

            @Override
            public void sure() {
                onMenuBackPressed();
                removeWatcher();
                if (null != mCurrentInfo) {
                    if (isEditItem) { //放弃编辑
                        mDragHandler.exitEditWord();
                        //移除当前，并替换成编辑前的字幕
                        mCurrentInfo.getCaption().removeListLiteObject();
                        mCurrentInfo = null;

                        UndoInfo info = mListener.getParamHandler().onDeleteStep();  //移除当前，并替换成编辑前的字幕列表
                        Log.e(TAG, "sure: " + info);
                        if (null != info && info.getMode() == IMenu.text) {
                            mListener.getParamHandler().restoreTextList(info.getList());
                        }
                        mListener.reBuild();
                    } else { //放弃新增（删除）
                        mListener.getParamHandler().deleteWordNewInfo(mCurrentInfo);
                        mListener.getParamHandler().onDeleteStep();
                        mCurrentInfo.getCaption().removeListLiteObject();
                        //播放器内容改变,刷新播放器
                        mListener.getEditor().refresh();
                        mCurrentInfo = null;
                    }
                }
                exitFragment();
                mListener.onSure(false);
                mListener.onSelectData(-1);
            }
        });

    }

    @Override
    public void onSureClick() {
        removeWatcher();
        removeInputListener();
        //新增-保存
        String text = mCurrentInfo.getText();
//        Log.e(TAG, "onSureClick: " + isEditItem + " text:" + text);
        if (!isEditItem && TextUtils.isEmpty(text)) { //没文字（放弃新增）
            onMenuBackPressed();
            //删除
            mListener.getParamHandler().deleteWordNewInfo(mCurrentInfo);
            mListener.getParamHandler().onDeleteStep();
            mCurrentInfo.getCaption().removeListLiteObject();
            mListener.getEditor().refresh();
            mMenuCallBack.onSure();
        } else if (isEditItem && TextUtils.isEmpty(text)) { //编辑且文字“”
            mCurrentInfo.getCaption().getCaptionItem().setHintContent(getString(R.string.pesdk_sub_hint));
            mCurrentInfo.refreshMeasuring();
            mListener.getEditor().refresh();
        } else {
            if (null != mCurrentInfo) {
                SysAlertDialog.showLoadingDialog(mContext, R.string.pesdk_isloading);
                //先移除监听再隐藏输入法(移除的时候调用了强制恢复布局)
                removeInputListener();
                InputUtls.hideKeyboard(mEtSubtitle);
                onSaveBtnItemImp();
            } else {
                //异常
                Log.e(TAG, "onSaveChangeListener: onSaveChangeListener is null");
            }
        }
        exitFragment();
        mDragHandler.onSave(); //保存并退出编辑模式
        mListener.onSure(false);
    }

    private void exitFragment() {
        mEtSubtitle.setText("");
    }

    /**
     * 新增单个
     */
    @Override
    void onBtnAddClick() {
        Log.e(TAG, "onBtnAddClick: " + this);
        if (mCurrentInfo == null) { //新增
            setEditInputText("");
            //添加进入
            mCurrentInfo = new WordInfoExt();
            //大小
            mCurrentInfo.setVirtualVideo(mVirtualVideo, mPlayer);
            //默认位置大小
            mCurrentInfo.initDefault(getString(R.string.pesdk_sub_hint));

            //添加
            int index = mListener.getParamHandler().addWordNewInfo(mCurrentInfo);
            Log.e(TAG, "onBtnAddClick: " + index);
            if (mDragHandler != null) {
                mDragHandler.edit(index, IMenu.text);
            }
            mCurrentInfo.setId(Utils.getWordId());
            //中心点不变
            mCurrentInfo.refreshMeasuring();
            onSaveDraft();
            onStartSub(true, true);
        } else { //编辑
            mListener.getParamHandler().onSaveAdjustStep(IMenu.text);
            //输入框显示输入文字
            String text = mCurrentInfo.getCaption().getText();
            Log.e(TAG, "onBtnAddClick: " + text);
            setEditInputText(text);
            onStartSub(false, true);
        }
    }


    /**
     * 改变样式后都要刷新
     */
    private void refresh() {
        if (mCurrentInfo != null) {
            mCurrentInfo.refresh(false);
        }
    }

    /**
     * 保存草稿
     */
    private void onSaveDraft() {
        if (System.currentTimeMillis() - mDraftTime > DEFAULT_TIME) {
            mDraftTime = System.currentTimeMillis();
            mListener.getParamHandler().onSaveDraft(IMenu.text);
        }
    }

    private void initView() {
        mLlWordEditer = $(R.id.thelocation);
        mStyleFragment = StyleFragment.newInstance();
        mStyleFragment.setListener(new StyleFragment.OnSubtitleStyleListener() {
            @Override
            public void onBold(boolean b) {
                if (mCurrentInfo != null) {
                    mCurrentInfo.getCaptionItem().setBold(b);
                    refresh();
                }
            }

            @Override
            public void onItalic(boolean b) {
                if (mCurrentInfo != null) {
                    mCurrentInfo.getCaptionItem().setItalic(b);
                    refresh();
                }
            }

            @Override
            public void onUnderLine(boolean underLine) {
                if (mCurrentInfo != null) {
                    mCurrentInfo.getCaptionItem().setUnderline(underLine);
                    refresh();
                }
            }

            @Override
            public void onColor(int color) {
                if (mCurrentInfo != null) {
                    mCurrentInfo.getCaptionItem().setTextColor(color);
                    refresh();
                }
            }

            @Override
            public void onStroke(int color, float value) {
                if (mCurrentInfo != null) {
                    if (color == 0) {
                        mCurrentInfo.getCaptionItem().setOutline(false);
                    } else {
                        mCurrentInfo.getCaptionItem().setOutline(true);
                        mCurrentInfo.getCaptionItem().setOutlineColor(color);
                        mCurrentInfo.getCaptionItem().setOutlineWidth(value);
                    }
                    refresh();
                }
            }

            @Override
            public void onShadow(int color, float radius, float distance, float angle, float alpha) {
                if (mCurrentInfo != null) {
                    if (color == 0) {
                        mCurrentInfo.getCaptionItem().setShadow(false);
                    } else {
                        mCurrentInfo.getCaptionItem().setShadow(color, radius, distance, angle);
                        mCurrentInfo.getCaptionItem().setShadowAlpha(alpha);
                    }
                    refresh();
                }
            }

            @Override
            public void onAlpha(float value) {
                if (mCurrentInfo != null) {
                    mCurrentInfo.getCaptionItem().setAlpha(value);
                    refresh();
                }
            }

            @Override
            public void onLabel(int value) {
                if (mCurrentInfo != null) {
                    mCurrentInfo.getCaptionItem().setBackgroundColor(value);
                    refresh();
                }
            }

            @Override
            public void onPreset(PresetStyle style) {
                if (mCurrentInfo != null) {
                    CaptionItem captionItem = mCurrentInfo.getCaptionItem();
                    captionItem.setBold(style.isBold());
                    captionItem.setItalic(style.isItalic());
                    captionItem.setTextColor(style.getTextColor());
                    captionItem.setAlpha(style.getAlpha());
                    captionItem.setBackgroundColor(style.getLabel());
                    if (style.getStrokeColor() == 0) {
                        captionItem.setOutline(false);
                    } else {
                        captionItem.setOutline(true);
                        captionItem.setOutlineColor(style.getStrokeColor());
                        captionItem.setOutlineWidth(style.getStrokeValue() / 5);
                    }

                    if (style.getShadowColor() == 0) {
                        captionItem.setShadow(false);
                    } else {
                        captionItem.setShadow(style.getShadowColor(), style.getShadowValue(), style.getShadowValue(), 0);
                    }
                    captionItem.refreshEffect();
                    refresh();
                }
            }

            @Override
            public void onSpacing(float wordKerning, float lineSpacing) {
                if (null != mCurrentInfo) {
                    CaptionItem captionItem = mCurrentInfo.getCaptionItem();
                    captionItem.setLineSpacing(lineSpacing);
                    captionItem.setWordKerning(wordKerning);
                    mCurrentInfo.refreshMeasuring();
                }

            }

            @Override
            public void onAlign(int hor, int ver) {
                if (null != mCurrentInfo) {
                    mCurrentInfo.getCaptionItem().setAlignment(hor, ver);
                    refresh();
                }
            }

        });
        changeFragment(mStyleFragment);
        mIvClear = $(R.id.ivClear);
        $(R.id.subtitle_save).setOnClickListener(v -> onSureClick());
        mEtSubtitle = $(R.id.subtitle_et);
        mRgMenu = $(R.id.subtitle_menu_group);
        mIvClear.setOnClickListener(mClearSubtitle);
        mTreeView = getActivity().findViewById(android.R.id.content);
    }


    @Override
    public void onDestroyView() {
        Log.e(TAG, "onDestroyView: " + this);
        mRgMenu.setOnCheckedChangeListener(null);
        mLinearWords.removeAllViews();
        super.onDestroyView();
        removeInputListener();

        mRoot = null;
        mStyleFragment = null;
        mFlowerFragment = null;
        mSubtiltleFontFragment = null;
        mBubbleFragment = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gcGlide();
    }


    /**
     * 给输入框设置内容
     *
     * @param text 横排排版下的文本
     */
    private void setEditInputText(String text) {
        mEtSubtitle.setText(text);
        if (null != text) {
            mEtSubtitle.setSelection(text.length());
        }
    }


    /**
     * 进入字幕的入口
     */
    private void init() {
        mVirtualVideo = mListener.getEditorImage();
        mLinearWords = mListener.getContainer();
        mPlayer = mListener.getEditor();
        mIsAddCaption = false;
        mLayoutWidth = mLinearWords.getWidth();
        mLayoutHeight = mLinearWords.getHeight();

        Log.e(TAG, "init: " + mLayoutWidth + "*" + mLayoutHeight);
        SubUtils.getInstance().recycle();

    }


    private void hideInput() {
        InputUtls.hideKeyboard(mEtSubtitle);
    }


    /***
     * 保存单个字幕
     */
    private void onSaveBtnItemImp() {
        if (mIsAddCaption) {
            mIsAddCaption = false;
            resetUI();
        }
        mPlayer.refresh();
        onMenuViewOnBackpressed();
        SysAlertDialog.cancelLoadingDialog();
    }


    /**
     * 开始编辑字幕
     *
     * @param isAdd
     * @param showInput
     */
    private void onStartSub(boolean isAdd, boolean showInput) {
//        Log.e(TAG, "onStartSub: " + " " + isAdd);
        controlKeyboardLayout();
        if (showInput) {
            //新增时，弹出输入框
            postShowInput();
        }
        ((RadioButton) $(showInput ? R.id.subtitle_input : R.id.subtitle_style)).setChecked(true);
    }


    private void postShowInput() {
        mEtSubtitle.removeCallbacks(mInputRunnable);
        mEtSubtitle.postDelayed(mInputRunnable, 100);
    }


    private void onMenuStyle() {
        hideInput();
        mStyleFragment.reset(mCurrentInfo);
        changeFragment(mStyleFragment);
    }

    /**
     * 控制menu部分的布局状态
     *
     * @param checkedId
     */
    private void onMenu(int checkedId) {
        if (null == mCurrentInfo) {
            Log.e(TAG, "onMenu: info is null ");
            return;
        }
        if (checkedId == R.id.subtitle_input) {
            postShowInput();
        } else if (checkedId == R.id.subtitle_style) { //新样式
            onMenuStyle();
        } else if (checkedId == R.id.subtitle_flower) { //花字
            hideInput();
            if (null == mFlowerFragment) {
                mFlowerFragment = FlowerFragment.newInstance();
            }
            mFlowerFragment.setCheck(mCurrentInfo.getFlower());
            mFlowerFragment.setFlowerListener(info -> {
                //设置花字
                if (mCurrentInfo != null) {
                    mCurrentInfo.setFlower(info);
                    refresh();
                }
            });
            changeFragment(mFlowerFragment);
        } else if (checkedId == R.id.subtitle_bubble) {
            hideInput();
            if (null == mBubbleFragment) {
                mBubbleFragment = SubtitleBubbleFragment.newInstance();
            }
            mBubbleFragment.setListener(new SubtitleBubbleFragment.BubbleListener() {
                @Override
                public void onSelect(StyleInfo styleInfo) {
                    if (mCurrentInfo == null) {
                        return;
                    }
                    //取消注册
                    mDragHandler.unRegisteredCaption();
                    //设置气泡刷新
                    mCurrentInfo.setBubble(styleInfo, true);
                    if (mCurrentInfo.getCaption().isAutoSize()) {
                        mCurrentInfo.refresh(true);
                        //外框
                        mDragHandler.onGetPosition(mListener.getCurrentPosition());
                    } else {
                        //注册 刷新宽高
                        mCurrentInfo.getCaption().cutoverCaption(mDragHandler.getReceiver());
                    }
                    onSaveDraft();
                }

                @Override
                public void onFailed() {
                    SysAlertDialog.cancelLoadingDialog();
                }

                @Override
                public void onSuccess() {

                }
            });
            mBubbleFragment.resetSelect(mCurrentInfo.getCategory(), mCurrentInfo.getResourceId());
            changeFragment(mBubbleFragment);
        } else if (checkedId == R.id.subtitle_ttf) {
            hideInput();
            if (null == mSubtiltleFontFragment) {
                mSubtiltleFontFragment = SubtiltleFontFragment.newInstance();
            }
            mSubtiltleFontFragment.setListener(mTTFListener);
            changeFragment(mSubtiltleFontFragment);
        }
    }


    /**
     * UI按钮恢复到默认
     */
    @Override
    void resetUI() {
    }


    /**
     * 从字幕(样式、大小）界面  ->返回到字幕1(添加字幕界面)
     * 二级界面-->一级界面
     */
    private void onMenuBackPressed() {
        onMenuViewOnBackpressed();
        removeInputListener();
    }


    /**
     * 点击上级界面
     */
    private void onMenuViewOnBackpressed() {
        removeWatcher();
        mEtSubtitle.setText("");
//        Log.e(TAG, "onMenuViewOnBackpressed: " + this);
    }


    /**
     * fragment的容器对应的父容器
     */
    public SubtitleFragment setFragmentContainer(View fragmentContainer) {
        mFragmentContainer = fragmentContainer;
        return this;
    }


    private void controlKeyboardLayout() {
        removeInputListener();
        if (null != mTreeView && null != mFragmentContainer && null != mLlWordEditer) {
            boolean result = (getActivity().getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
            mGlobalLayoutListener = new OnSubGlobalLayoutListener(mTreeView, mFragmentContainer, mLlWordEditer,
                    $(R.id.subtitle_menu_group), result, bOpenInput -> {
                //                    Log.e(TAG, "onInput: " + bOpenInput + " >" + mCurrentInfo + " >" + mCurrentInfo.getCaptionObject().isEditing());
            });
            mTreeView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
        }
    }

    private void removeInputListener() {
        if (null != mTreeView) {
            if (null != mGlobalLayoutListener) {
                //先移除监听再隐藏输入法(移除的时候调用了强制恢复布局)
                mTreeView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
                mGlobalLayoutListener.resetUI();
                mGlobalLayoutListener = null;
            }
        }
    }


    private Fragment mCurrentFragment;

    /**
     * 切换fragment
     */
    private void changeFragment(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        FragmentManager manager = getChildFragmentManager();
        if (mCurrentFragment != null) {
            manager.beginTransaction().hide(mCurrentFragment).commitAllowingStateLoss();
        }
        if (!fragment.isAdded()) {
            manager.beginTransaction().remove(fragment).commitAllowingStateLoss();
            manager.beginTransaction()
                    .add(R.id.fragment, fragment)
                    .show(fragment)
                    .commitAllowingStateLoss();
        } else {
            manager.beginTransaction().show(fragment).commitAllowingStateLoss();
        }
        mCurrentFragment = fragment;
    }


}