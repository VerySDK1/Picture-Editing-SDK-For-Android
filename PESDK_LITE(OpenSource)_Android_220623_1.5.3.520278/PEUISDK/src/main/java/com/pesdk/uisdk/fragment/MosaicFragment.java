package com.pesdk.uisdk.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.MOInfo;
import com.pesdk.uisdk.crop.CropView;
import com.pesdk.uisdk.data.model.MOFragmentModel;
import com.pesdk.uisdk.listener.ImageHandlerListener;
import com.pesdk.uisdk.util.Utils;
import com.pesdk.uisdk.widget.ExtSeekBar2;
import com.pesdk.uisdk.widget.SysAlertDialog;
import com.vecore.VirtualImage;
import com.vecore.VirtualImageView;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.exception.InvalidArgumentException;
import com.vecore.models.DewatermarkObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * 马赛克和去水印
 * step1 :新增单个 -》选中样式 （位置  类型  ） 、apply (拉满时间轴) - --》>  播放（确认单个的时间线  是否取消 ）  ->完成 （放入recycleview ->单选单个可以重新编辑 ）
 */
@Deprecated
public class MosaicFragment extends BaseFragment {
    private List<MOFragmentModel.MOModel> mModelList = new ArrayList<>();
    private final int OFF_TIME = 30;//偏移  容错

    public static MosaicFragment newInstance() {
        return new MosaicFragment();
    }


    public MosaicFragment() {
        super();
    }


    private ImageHandlerListener mEditorHandler;
    private VirtualImage mVirtualVideo;
    private boolean isScrollIngItem = false; //true 正在处理单个的时间线  (此时需要屏蔽Hlight效果)

//    /**
//     * 返回
//     *
//     * @param mISubtitle
//     */
//    public void setHandler(IFragmentHandler mISubtitle) {
//        this.mFragmentHandler = mISubtitle;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "OSDFragment";
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mEditorHandler = (ImageHandlerListener) context;
    }

    private RadioGroup mGroupMosaic;
    private RadioButton mRbClur, mRbPixel, mRbOsd;
    private ExtSeekBar2 mStrengthBar;
    private ImageView mOSDIcon;
    private RelativeLayout osdBarLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        enableScrollListener = false;
        mRoot = inflater.inflate(R.layout.pesdk_fragment_mosaic_layout, container, false);
        mStrengthBar = $(R.id.sbarStrength);
        mOSDIcon = $(R.id.osd_icon);
        osdBarLayout = $(R.id.osdBarLayout);
        osdBarLayout.setVisibility(View.INVISIBLE);
        mLinearWords = mEditorHandler.getContainer();
        //获取模板样式
        mModelList = new MOFragmentModel(mLinearWords.getWidth(), mLinearWords.getHeight()).getData();

        mStrengthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isFromUser = fromUser) {
                    if (null != mCurrentInfo && Math.abs(lastProgress - progress) > 5) {
                        lastProgress = progress;
                        mCurrentInfo.setValue(progress / (mStrengthBar.getMax() + 0.0f));
                        mCurrentInfo.getObject().quitEditCaptionMode(true);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            private boolean isFromUser = false;

            private int lastProgress = -1;

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isFromUser) {
                    if (null != mCurrentInfo) {
                        mCurrentInfo.setValue(seekBar.getProgress() / (mStrengthBar.getMax() + 0.0f));
                    }
                }
            }
        });

        initView();
        init();
        int mCurrentTime = mEditorHandler.getCurrentPosition();
        setProgressText(mCurrentTime, false);
        return mRoot;
    }

    /**
     * 底部显示
     */
    private void checkTitleLayout() {
        if (mMOInfoList.size() > 0 && !isMenuIng) {
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkTitleLayout();
        addClick();
    }

    @Override
    public void onCancelClick() {
        if (onBackPressed() == 1) {
            mPlayer.refresh();
            mCropView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSureClick() {
        if (isMenuIng) {
            //时间线拉到末尾，从开始位置播放，确认结束时间点
            int end = mEditorHandler.getDuration();
            onSaveBtnItem(end, false);
            if (isAddStep) {
                onCurrentSave(end / 1000.0f);
            } else {
                mCurrentInfo = null;
                resetMenuUI();
                checkTitleLayout();
            }
        } else {
            if (null != mCurrentInfo) {
                //保存当前编辑 （等效于完成按钮）
                onEditSure();
                mCurrentInfo = null;
            }
            mCropView.setVisibility(View.GONE);
//            TempVideoParams.getInstance().setMosaics(mMOInfoList);
            onBackToActivity(true);
//            mFragmentHandler.onBackPressed();
        }
        lastPreId = -1;
    }

    private void onEditSure() {
//        int[] arr = mSubtitleLine.getCurrent(mCurrentInfo.getId());
//        if (null != arr) {
        RectF tmp = mCropView.getCropF();
        if (null != tmp && null != lastRectF && !tmp.equals(lastRectF)) {
            //位置有变化需要新生成对象
            mCurrentInfo.setShowRectF(new RectF(tmp));
        }
        lastRectF = null;
//        } else {
//            isAddStep = false;
//            resetMenuUI();
//            mSubtitleLine.setShowCurrentFalse();
//            mCurrentInfo = null;
//        }
    }


    /**
     * 马赛克、水印
     */
    private View.OnClickListener mOsdClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null != mCurrentInfo) {
                mCurrentInfo.getObject().remove();
            }
            int id = v.getId();
            mStrengthBar.setEnabled(id != R.id.rb_osd);
            if (id == R.id.rb_blur) { //高斯模糊
                onBlurUI();
                onItemMosaicChecked(mModelList.get(0));
            } else if (id == R.id.rb_pixl) { //像素化
                onMosaicClickUI();
                onItemMosaicChecked(mModelList.get(1));
            } else if (id == R.id.rb_osd) { //去水印
                osdBarLayout.setVisibility(View.INVISIBLE);
                mOSDIcon.setImageResource(R.drawable.pesdk_osd_square_n);
                onItemMosaicChecked(mModelList.get(2));
            }
            if (null != mCurrentInfo) {
                mCurrentInfo.getObject().quitEditCaptionMode(true);
            }
        }
    };

    private void onBlurUI() {
        osdBarLayout.setVisibility(View.VISIBLE);
        mOSDIcon.setImageResource(R.drawable.pesdk_mosaic_blur_n);
    }

    private void onMosaicClickUI() {
        osdBarLayout.setVisibility(View.VISIBLE);
        mOSDIcon.setImageResource(R.drawable.pesdk_vepub_mosaic_square_n);
    }

    private View mMenuLayout;
    private ArrayList<MOInfo> mMOInfoList = new ArrayList<>(),
            mTempWordList = new ArrayList<>(), //进入时还原旧的列表
            mRevokeList = new ArrayList<>(); //编辑->放弃还原上一步的列表
    private FrameLayout mLinearWords;
    private VirtualImageView mPlayer;

    private TextView tvTitle;
    /**
     * 正在编辑中的字幕
     */
    private MOInfo mCurrentInfo;
    private int mLayoutWidth = 1024, mLayoutHeight = 1024;

    private CropView mCropView;

    private void initView() {
        mGroupMosaic = $(R.id.rgMosaic);
        tvTitle = $(R.id.tvTitles);
        //设置添加或删除item时的动画，这里使用默认动画
        tvTitle.setText("");

        mMenuLayout = $(R.id.osd_menu_layout);

        mDisplay = CoreUtils.getMetrics();

        mRbClur = $(R.id.rb_blur);
        mRbPixel = $(R.id.rb_pixl);
        mRbOsd = $(R.id.rb_osd);
        mRbClur.setOnClickListener(mOsdClickListener);
        mRbPixel.setOnClickListener(mOsdClickListener);
        mRbOsd.setOnClickListener(mOsdClickListener);


        if (mIsMosaic) {
            tvTitle.setText(R.string.pesdk_mosaic);//
        } else {
            tvTitle.setText(R.string.pesdk_dewatermark);
        }
    }

    /**
     * 退出编辑模式时，解绑回调并影藏控制器
     */
    private void unRegisterDrawRectListener() {
        mCropView.setVisibility(View.GONE);
    }


    /**
     * 删除已添加的MO
     * g
     *
     * @param moId
     */
    private MOInfo deleteItemImp(int moId) {

        //step1 从集合中删除
        int len = mMOInfoList.size();
        MOInfo deleteItem = null;
        for (int i = 0; i < len; i++) {
            if (mMOInfoList.get(i).getId() == moId) {
                deleteItem = mMOInfoList.remove(i);
                try {
                    deleteItem.getObject().setVirtualVideo(mVirtualVideo, mPlayer);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        //step 2:  从时间轴和播放器中删除
        if (null != deleteItem) {
            deleteItem.getObject().remove();
            mPlayer.refresh();
        }
        checkTitleLayout();
        unRegisterDrawRectListener();
        return deleteItem;
    }

    /**
     * 恢复按钮的UI
     */
    private void resetMenuUI() {
//        mTmpBar.setVisibility(View.VISIBLE);
    }

    //删除当前Item
    private View.OnClickListener mOnDeleteListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            pauseVideo();
            if (null != mCurrentInfo) {
                if (deleteItemImp(mCurrentInfo.getId()) != mCurrentInfo) {
                    mCurrentInfo.getObject().remove();
                    mPlayer.refresh();
                }
                mCurrentInfo = null;
            }
            mPlayer.refresh();
            resetMenuUI();
            onScrollProgress(mEditorHandler.getCurrentPosition());

        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        bUIPrepared = false;
        mHandler.removeCallbacks(restoreDataRunnable);
        mModelList.clear();
    }


    private DisplayMetrics mDisplay;
    private int mDuration = 1000;

    /**
     * 点击取消保存和重新进入时重置List
     */
    private void onListReset(boolean isInit) {
        if (isInit) {
            mTempWordList.clear();
        }
//        ArrayList<MOInfo> tempList = TempVideoParams.getInstance()  .getMosaicDuraionChecked();
        mMOInfoList.clear();
//        int len = tempList.size();
        MOInfo tmp;
//        for (int i = 0; i < len; i++) {
//            tmp = tempList.get(i);
//            tmp.resetChanged();
//            mMOInfoList.add(tmp);
//            if (isInit) {
//                mTempWordList.add(tmp.copy());
//            }
//        }
    }


    /**
     * 进入
     */
    private void init() {
        mVirtualVideo = mEditorHandler.getEditorImage();
        mPlayer = mEditorHandler.getEditor();
        mDuration = mEditorHandler.getDuration();
        isAddStep = false;
        mLayoutWidth = mLinearWords.getWidth();
        mLayoutHeight = mLinearWords.getHeight();


        mCropView = new CropView(mContext);
        mCropView.setOverlayShadowColor(Color.TRANSPARENT);
        mCropView.setEnableDrawSelectionFrame(false);
        mCropView.setVisibility(View.GONE);
        mCropView.setTouchListener(new CropView.ITouchListener() {
            @Override
            public void onTouchDown() {
            }

            @Override
            public void onTouchUp() {
                if (null != mCurrentInfo) {
                    mCurrentInfo.setShowRectF(mCropView.getCropF());
                    mCurrentInfo.getObject().quitEditCaptionMode(true);
                    saveToList(mCurrentInfo);
                }
            }

            @Override
            public void onMove() {
                if (null != mCurrentInfo) {
                    mCurrentInfo.setShowRectF(mCropView.getCropF());
                    mCurrentInfo.getObject().quitEditCaptionMode(true);
                }
            }
        });

        mLinearWords.addView(mCropView);
        onListReset(true);
        if (!bUIPrepared) {  //防止后台切回前台，字幕轴重新初始化
            mHandler.postDelayed(restoreDataRunnable, 100);
        }

        //图标
        mOSDIcon.setImageResource(mIsMosaic ? R.drawable.pesdk_vepub_mosaic_square_n : R.drawable.pesdk_osd_square_n);
    }


    /**
     * UI数据恢复成功
     */
    private boolean bUIPrepared = false;

    /**
     * 获取封面
     */
    private int getModelThumb(int modelId) {
        MOFragmentModel.MOModel info = getMOModel(modelId);
        if (null != info) {
            if (info.getType() == DewatermarkObject.Type.mosaic) {
                return R.drawable.pesdk_vepub_mosaic_square_n;
            } else if (info.getType() == DewatermarkObject.Type.blur) {
                return R.drawable.pesdk_mosaic_blur_n;
            } else {
                return R.drawable.pesdk_osd_square_n;
            }
        }
        return R.drawable.pesdk_vepub_mosaic_square_n;
    }

    /**
     * 获取对应的模板
     */
    private MOFragmentModel.MOModel getMOModel(int modelId) {
        MOFragmentModel.MOModel model = null;
        int len = mModelList.size();
        for (int i = 0; i < len; i++) {
            MOFragmentModel.MOModel tmp = mModelList.get(i);
            if (tmp.getType().ordinal() == modelId) {
                model = tmp;
                break;
            }
        }
        if (null == model) {
            model = mModelList.get(0);
        }
        return model;
    }


    //恢复数据
    private Runnable restoreDataRunnable = new Runnable() {

        @Override
        public void run() {
//            ArrayList<SubInfo> sublist = new ArrayList<>();
//            int len = mMOInfoList.size();
//            for (int i = 0; i < len; i++) {
//                sublist.add(new SubInfo(mMOInfoList.get(i)));
//            }
            bUIPrepared = true;
            lastPreId = -1;
        }
    };


    private void setProgressText(int progress) {
        setProgressText(progress, false);
    }


    private void setProgressText(int progress, boolean userScrollByRV) {
        progress = Math.max(0, progress);
    }


    /**
     * 新增true，编辑false
     */
    private boolean isAddStep = false;


    /**
     * 是否编辑当个item中 （新增或编辑时的状态）
     */
    private boolean isMenuIng = false;


    private void addClick() {
        //图标
        mOSDIcon.setImageResource(mIsMosaic ? R.drawable.pesdk_vepub_mosaic_square_n : R.drawable.pesdk_osd_square_n);
        //添加
        addOSD();
    }

    /**
     * 开始添加
     */
//    private View.OnClickListener onAddListener = new View.OnClickListener() {
//
//        @Override
//        public void onClick(View v) {
//            if (mModelList.size() < 3) {
//                Log.e(TAG, "onAddListener: recovering sub data ...");
//                return;
//            }
//            String menu = mTvAddSubtitle.getText().toString();
//            /**
//             * 新增
//             */
//            if (menu.equals(mContext.getString(R.string.add_mosaic)) || menu.equals(mContext.getString(R.string.add_dewatermark))) {
//                addClick();
//                //图标
//                mOSDIcon.setImageResource(mIsMosaic ? R.drawable.pesdk_vepub_mosaic_square_n : R.drawable.pesdk_osd_square_n);
//                //添加
//                addOSD();
//                mDelete.setEnabled(false);
//            } else if (mCropView.getVisibility() == View.VISIBLE) {
//                if (null != mCurrentInfo) {
//                    onCurrentSave(mEditorHandler.getDuration());
//                    mDelete.setEnabled(true);
//                }
//                mOSDIcon.setImageResource(mIsMosaic ? R.drawable.pesdk_vepub_mosaic_square_n : R.drawable.pesdk_osd_square_n);
//                //添加
//                addOSD();
//            }
//        }
//    };

    //添加
    private void addOSD() {
        if (null != mCurrentInfo) {
            //先保存当前编辑
            onEditSure();
        }
        // 判断该区域能否添加
        int progress = mEditorHandler.getCurrentPosition();

        isAddStep = true;
        mCurrentInfo = new MOInfo();
        try {
            mCurrentInfo.getObject().setVirtualVideo(mVirtualVideo, mPlayer);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        mCurrentInfo.setId(Utils.getWordId());

        //默认添加到整条时间线，预览时通过GL实现预览,所以必须设置一个大于当前时刻的结束点，待保存时重新修正结束点
        mCurrentInfo.setTimelineRange(progress, mEditorHandler.getDuration(), false);
        MOFragmentModel.MOModel model;
        if (mIsMosaic) {
            model = mModelList.get(1);
            mStrengthBar.setEnabled(false);
        } else {
            model = mModelList.get(2);
        }
        mCurrentInfo.setStyleId(model.getType().ordinal());
        onStartSub();
        mCurrentInfo.getObject().quitEditCaptionMode(true);
        mPlayer.refresh();
    }

    private int[] rgRbIds = new int[]{R.id.rb_blur, R.id.rb_pixl, R.id.rb_osd};


    /**
     * 恢复选中的效果
     *
     * @param styleId
     */
    private void resetGroupRB(int styleId) {
        int index = getStyleIndex(styleId);
        if (index >= 0 && index < rgRbIds.length) {
            if (index == 0) {
                mRbClur.setChecked(true);
                onBlurUI();
            } else if (index == 1) {
                mRbPixel.setChecked(true);
                onMosaicClickUI();
            } else if (index == 2) {
                osdBarLayout.setVisibility(View.INVISIBLE);
                mRbOsd.setChecked(true);
            }
        }
        mStrengthBar.setEnabled(mGroupMosaic.getCheckedRadioButtonId() != R.id.rb_osd);
    }

    /***
     * 保存（完成）当前
     * @param end  结束时间点 单位：S
     */
    private void onCurrentSave(float end) {
        if (null != mCurrentInfo) {
            pauseVideo();
                saveToList(mCurrentInfo);
            checkTitleLayout();
            mCurrentInfo = null;
        }
        isAddStep = false;
        mCropView.setVisibility(View.GONE);
        resetMenuUI();
    }

//    private View.OnClickListener onCancelDelete = new View.OnClickListener() {
//
//        @Override
//        public void onClick(View v) {
//            pauseVideo();
//            String menu = mBtnCancelEdit.getText().toString().trim();
//            if (getString(R.string.cancel).equals(menu)) {
//                //取消
//                if (null != mCurrentInfo) {
//                    if (deleteItemImp(mCurrentInfo.getId()) != mCurrentInfo) {
//                        mCurrentInfo.getObject().remove();
//                        mPlayer.refresh();
//                    }
//                    mCurrentInfo = null;
//                }
//                //恢复按钮状态
//                resetMenuUI();
//            } else if (getString(R.string.edit).equals(menu)) {
//                //编辑
//                if (mCurrentInfo != null) {
//                    int current = mEditorHandler.getCurrentPosition();
//                    int tmp = (int) (mCurrentInfo.getEnd() - OFF_TIME);
//                    if (current > tmp) { //适当偏移，防止刚好在此时间，当前帧不显示
//                        mEditorHandler.seekTo(tmp);
//                    } else if (current < mCurrentInfo.getStart() + OFF_TIME) {
//                        mEditorHandler.seekTo((int) (mCurrentInfo.getStart() + OFF_TIME));
//                    }
//                    mRevokeList.clear();
//                    for (MOInfo info : mMOInfoList) {//记录上次编辑，用于撤销更改
//                        mRevokeList.add(info.copy());
//                    }
//                    onStartSub();
//                }
//            }
//        }
//    };


    /**
     * 编辑已经存在的元素
     */
    private void onEditWordImp(MOInfo info) {
        if (null != info) {
            isAddStep = false;
            //编辑时，构造新对象
            mCurrentInfo = info;
            //当前编辑项高亮
            try {
                mCurrentInfo.getObject().setVirtualVideo(mVirtualVideo, mPlayer);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
            //记录之前的显示位置
            lastRectF = mCurrentInfo.getShowRectF();
            initItemWord(mCurrentInfo);
            checkTitleLayout();
            mCurrentInfo.getObject().quitEditCaptionMode(true);

        }
    }


    /**
     * @param nPos       真实的时间线  单位：ms
     * @param isRealLine 时间轴上是否显示真实的时间线
     */
    private void onWordEnd(int nPos, boolean isRealLine) {
        if (null == mCurrentInfo) {
            return;
        }
        if (-1 == nPos) {
            //点击完成->确认按钮，修改Object中的时间线
            mCurrentInfo.setTimelineRange(0, mEditorHandler.getDuration(), false);
        } else {
            //这里的false ,是为了防止设置时间线更新了，退出编辑时（getObject().quitEditCaptionMode(true);）显示Core中马赛克|水印时再次更新
            mCurrentInfo.setEnd(nPos < 0 ? mEditorHandler.getCurrentPosition() : nPos, false);
        }
        onSaveToList(false);
    }


    private void pauseVideo() {
    }


    /**
     * 点击menu_layout 中得保存(保存位置)，继续播放
     */
    private void onSaveBtnItem(int end, boolean isShowRealLine) {
        SysAlertDialog.showLoadingDialog(mContext, R.string.pesdk_isloading);
        if (null != mCurrentInfo) {
            if (null != mCropView) {
                mCurrentInfo.setShowRectF(mCropView.getCropF());
            }
            if (isAddStep) {
                //新增（保存）
                unRegisterDrawRectListener();
                onWordEnd(end, isShowRealLine);  //特别处理时间线
                //播放器内容改变,刷新播放器
                mPlayer.refresh();
            } else {
                //编辑（保存）
                onSaveToList(false);
                //播放器内容改变,刷新播放器
                mPlayer.refresh();
            }
        }
        isMenuIng = false;
        SysAlertDialog.cancelLoadingDialog();
    }

    /**
     * 不管时间线和位置有没变化，一律生成新的 ( apply(true) )
     *
     * @param needStart
     */
    private void saveInfo(boolean needStart) {
        if (null != mCurrentInfo) {
            //退出编辑模式
            mCurrentInfo.getObject().quitEditCaptionMode(true);
            //隐藏UI层的控制按钮
            unRegisterDrawRectListener();
            saveToList(mCurrentInfo);
        }

    }

    /**
     * 保存到集合
     *
     * @param info
     */
    private void saveToList(MOInfo info) {
        int re = getIndex(info.getId());
        if (re > -1) {
            mMOInfoList.set(re, info); // 重新编辑
        } else {
            mMOInfoList.add(info); // 新增
        }
    }

    /**
     * 当前字幕在集合的索引
     *
     * @param id 当前字幕的Id
     * @return
     */
    private int getIndex(int id) {
        return Utils.getIndex(mMOInfoList, id);
    }

    /**
     * 样式下标
     *
     * @param modelId
     */
    private int getStyleIndex(int modelId) {
        int index = -1;
        int len = mModelList.size();
        for (int i = 0; i < len; i++) {
            if (modelId == mModelList.get(i).getType().ordinal()) {
                index = i;
                break;
            }
        }
        return index;
    }


    /**
     * 开始新增
     */
    private void onStartSub() {
        isMenuIng = true;
        //被选中的样式
        resetGroupRB(mCurrentInfo.getStyleId());
        initItemWord(mCurrentInfo);
        checkTitleLayout();
    }

    private RectF lastRectF = null;


    /**
     * 单个编辑字幕
     *
     * @param info
     */
    private void initItemWord(MOInfo info) {
        MOFragmentModel.MOModel model = getMOModel(info.getStyleId());
        RectF clip;
        RectF showRect = info.getShowRectF();
        if (showRect.isEmpty()) {
            //新增
            clip = new RectF(model.getRectF());
            try {   //指定类型
                info.getObject().setMORectF(model.getType(), model.getRectF());
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        } else { //编辑
            try {
                info.getObject().setMOType(model.getType());
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
            clip = new RectF(showRect);
        }
        fixClipPx(clip);
        mCropView.initialize(clip, new RectF(0, 0, mLayoutWidth, mLayoutHeight), 0);
        mCropView.setVisibility(View.VISIBLE);
    }

    /**
     * @param clip
     */
    private void fixClipPx(RectF clip) {
        clip.left *= mLayoutWidth;
        clip.top *= mLayoutHeight;
        clip.right *= mLayoutWidth;
        clip.bottom *= mLayoutHeight;
    }


    private Handler mHandler = new Handler(msg -> false);

    /**
     * 从menu->addlayout
     */
    private void onMenuBackPressed() {
        if (null != mCurrentInfo) {
            mCurrentInfo.getObject().remove(); //删除正在修改的item
            int index = Utils.getIndex(mRevokeList, mCurrentInfo.getId());
            if (index >= 0) {
                int index0 = getIndex(mCurrentInfo.getId());
                if (index0 >= 0) {
                    MOInfo tmp = mRevokeList.get(index);
                    try {
                        tmp.getObject().setVirtualVideo(mVirtualVideo, mPlayer);
                        tmp.getObject().quitEditCaptionMode(true);
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
                    mMOInfoList.set(index0, tmp); //撤销更改
                }
            }
            mCurrentInfo = null;
            mPlayer.refresh();
        }
        unRegisterDrawRectListener();
    }


    private void onShowAlert() {
        SysAlertDialog.createAlertDialog(mContext,
                mContext.getString(R.string.pesdk_dialog_tips),
                mContext.getString(R.string.pesdk_cancel_all_changed),
                mContext.getString(R.string.pesdk_cancel),
                (dialog, which) -> {

                }, mContext.getString(R.string.pesdk_sure),
                (dialog, which) -> {
                    mCurrentInfo = null;
                    //实时删除OSD
                    for (MOInfo info : mMOInfoList) {
                        info.getObject().remove();
                    }
                    //实时添加
                    for (MOInfo info : mTempWordList) {
                        try {
                            info.getObject().setVirtualVideo(mVirtualVideo, mPlayer);
                            info.getObject().quitEditCaptionMode(true);
                        } catch (InvalidArgumentException e) {
                            e.printStackTrace();
                        }
                    }
//                    TempVideoParams.getInstance().setMosaics(mTempWordList);

                    onBackToActivity(false);
                    dialog.dismiss();
                }, false, null).show();
    }

    /**
     * 返回
     *
     * @param save
     */
    private void onBackToActivity(boolean save) {
        if (save) {
            onSaveToList(true);
            if (null != mCurrentInfo) {
                mCurrentInfo.getObject().quitEditCaptionMode(true);
            }
            mEditorHandler.onSure();
        } else {
            unRegisterDrawRectListener();
            if (null != mCurrentInfo) {
                mCurrentInfo.getObject().quitEditCaptionMode(false);
            }
            int len = mMOInfoList.size();
            for (int n = 0; n < len && n < mTempWordList.size(); n++) {
                mMOInfoList.get(n).set(mTempWordList.get(n));
            }
            mEditorHandler.onBack();
        }
        mCropView.setVisibility(View.GONE);
        bUIPrepared = false;

    }

    /**
     * 保存当前编辑字幕到集合 （完成按钮，播放按钮控制）
     */
    private void onSaveToList(boolean clearCurrent) {
        unRegisterDrawRectListener();
        saveInfo(clearCurrent);
    }


    /**
     * 播放中的进度
     *
     * @param progress (单位ms)
     */
    private void onScrollProgress(int progress) {
        onScrollThumbHLight(progress);
        onScrollProgress(progress, false);
    }

    private void onScrollProgress(int progress, boolean userScrollByRV) {
        setProgressText(progress, userScrollByRV);
    }


    private int lastPreId = -1;

    /***
     * 缩略图轴高亮
     * @param progress
     */
    private void onScrollThumbHLight(int progress) {
        onScrollThumbHLightByHand(progress, getIndex(lastPreId));
    }

    /**
     * @param progress 当前播放器位置
     * @param index    强制指定了选中项 >=0 时有效，用户指定的优先
     */
    private void onScrollThumbHLightByHand(int progress, int index) {
        if (isScrollIngItem || isAddStep) {
            return;
        }
        MOInfo current = (MOInfo) Utils.getTopItem(mMOInfoList, progress, index);

        //防止频繁更新
        if (null != current) {
            if (current.getId() != lastPreId) {
                lastPreId = current.getId();
                mCropView.setVisibility(View.GONE);
                onEditWordImp(current);
            }
            mCurrentInfo = current;
        } else {
            onNoneEditUI();
        }
    }

    /**
     * 退出可编辑模式
     */
    private void onNoneEditUI() {
        if (null != mCurrentInfo) {
            mCurrentInfo.getObject().quitEditCaptionMode(true);
            mCurrentInfo = null;
        }
        lastPreId = -1;
        mCropView.setVisibility(View.GONE);
    }


    /**
     * 切换样式
     *
     * @param info
     */
    private void onItemMosaicChecked(MOFragmentModel.MOModel info) {
        if (null != mCurrentInfo && null != info) {
            mCurrentInfo.setStyleId(info.getType().ordinal());
            RectF rectF = mCropView.getCropF();
            if (!rectF.isEmpty()) {
                //保存当前的矩形位置
                mCurrentInfo.setShowRectF(rectF);
            }
            initItemWord(mCurrentInfo);
        }
    }


    @Override
    public int onBackPressed() {
        if (isMenuIng) {
            onMenuBackPressed();
            isMenuIng = false;
            resetMenuUI();
            checkTitleLayout();
            isAddStep = false;
            lastPreId = -1;
            onScrollProgress(mEditorHandler.getCurrentPosition());
            return -1;
        } else {
//            if (!CommonStyleUtils.isEqualsSource(mMOInfoList, TempVideoParams.getInstance().getMosaicDuraionChecked()) || mIsUpdate) {
//                pauseVideo();
//                onShowAlert();
//                return 0;
//            } else {
//                onShowAlert();
//                return -1;
//            }
        }
        return 0;
    }


    //去水印还是马赛克   默认去水印
    private boolean mIsMosaic = true;

    /**
     * 马赛克
     */
    public void setMosaic(boolean b) {
        mIsMosaic = b;
        if (mOSDIcon != null) {
            mOSDIcon.setImageResource(mIsMosaic ? R.drawable.pesdk_vepub_mosaic_square_n : R.drawable.pesdk_osd_square_n);
        }

    }
}
