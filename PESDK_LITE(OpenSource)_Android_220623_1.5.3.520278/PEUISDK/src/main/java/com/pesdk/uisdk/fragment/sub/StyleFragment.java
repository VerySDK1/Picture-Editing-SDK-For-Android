package com.pesdk.uisdk.fragment.sub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.bean.model.PresetStyle;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.beauty.widget.ExtSeekBar3;
import com.pesdk.uisdk.fragment.AbsBaseFragment;
import com.pesdk.uisdk.widget.ColorBar;
import com.pesdk.uisdk.widget.ExtSeekBar2;
import com.vecore.models.caption.CaptionItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 样式
 */
public class StyleFragment extends AbsBaseFragment {
    public static StyleFragment newInstance() {

        Bundle args = new Bundle();

        StyleFragment fragment = new StyleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private int MAX_STROKE_WIDTH = 5;//最大描边宽度
    private int MAX_SHADOW_WIDTH = 10;//最大阴影偏移量

    //比例
    private static final float LEFT_VALUE = 0.1f;

    private RadioGroup mRgMenu;
    private RadioButton mRbText, mRbStroke, mRbShadow, mRbLabel;
    //当前选中的菜单
    private final int TEXT = 0, STROKE = 1, SHADOW = 2, LABEL = 3, ALIGN = 4, TEXT_SPACING = 5;
    private int mStatue = TEXT;


    private ViewGroup mTextStyleLayout;//粗体、习题
    private LinearLayout mLlColor;//颜色
    //文本
    private CheckBox mBtnBold, mBtnItalic, mBtnLine;//粗体、斜体
    private boolean mIsBold = false;//是否粗体
    private boolean mIsItalic = false;//是否斜体
    private boolean mIsUnderline = false;//是否下划线
    //seekbar
    private LinearLayout mLlSeekbar;
    private TextView mTvAlpha, mTvRadius, mTvDistance, mTvAngle, mTVSubMenuText;
    private ExtSeekBar2 mBarAlpha, mBarDistance, mBarAngle, mBarRadius;
    //颜色值 文本、描边、阴影、标签
    private int mTextColor, mStrokeColor, mShadowColor, mLabelColor;
    //描边、阴影、透明度  值
    private float mStrokeValue, mAlphaValue;
    private ViewGroup mPositionLayout, mShadowLayout, mSpacingLayout;
    private ColorBar mColorBar;


    private float mShadowRadius;//半径
    private float mShadowDistance;//距离
    private float mShadowAngle;//角度
    private float mShadowAlpha;//阴影透明度

    private float mLineSpacing;
    private float mWordKerning;
    private TextView mTvLineSpacing;
    private ExtSeekBar3 mBarLineSpacing;
    private TextView mTvWordKerning;
    private ExtSeekBar3 mBarWordKerning;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.pesdk_subtitle_style_layout, container, false);
        return mRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRgMenu = $(R.id.rg_menu);
        mRbText = $(R.id.rb_text);
        mRbStroke = $(R.id.rb_stroke);
        mRbShadow = $(R.id.rb_shadow);
        mRbLabel = $(R.id.rb_label);
        mColorBar = $(R.id.colorBar);

        mPositionLayout = $(R.id.subtitle_position_layout);
        mSpacingLayout = $(R.id.ll_spacing);
        //默认选中文本
        mRbText.setChecked(true);
        mRgMenu.setOnCheckedChangeListener((group, checkedId) -> switchUI(checkedId));

        mTextStyleLayout = $(R.id.ll_text_style);
        mShadowLayout = $(R.id.shadowLayout);
        mLlColor = $(R.id.ll_color);
        //文本
        mColorBar.setCallback(new ColorBar.Callback() { //颜色
            @Override
            public void onNone() {
                if (mStatue == TEXT) {
                    mTextColor = 0;
                    if (mListener != null) {
                        mListener.onColor(mTextColor);
                    }
                } else if (mStatue == STROKE) {
                    mStrokeColor = 0;
                    if (mListener != null) {
                        mListener.onStroke(mStrokeColor, mStrokeValue * MAX_STROKE_WIDTH);
                    }
                } else if (mStatue == SHADOW) {
                    mShadowColor = 0;
                    notifyShadow();
                } else if (mStatue == LABEL) {
                    mLabelColor = 0;
                    if (mListener != null) {
                        mListener.onLabel(mLabelColor);
                    }
                }
            }

            @Override
            public void onColor(int color) {
                if (mStatue == TEXT) {
                    mTextColor = color;
                    if (mListener != null) {
                        mListener.onColor(mTextColor);
                    }
                } else if (mStatue == STROKE) {
                    mStrokeColor = color;
                    if (mListener != null) {
                        mListener.onStroke(mStrokeColor, mStrokeValue * MAX_STROKE_WIDTH);
                    }
                } else if (mStatue == SHADOW) {
                    mShadowColor = color;
                    setDefaultShadowStyle();
                    notifyShadow();
                } else if (mStatue == LABEL) {
                    mLabelColor = color;
                    if (mListener != null) {
                        mListener.onLabel(mLabelColor);
                    }
                }
            }
        });

        mBtnBold = $(R.id.btn_bold);
        mBtnItalic = $(R.id.btn_italic);
        mBtnLine = $(R.id.btn_b_line);
        mBtnBold.setOnClickListener(v -> {
            v.postDelayed(() -> {
                mIsBold = mBtnBold.isChecked();
                if (mListener != null) {
                    mListener.onBold(mIsBold);
                }
            }, 100);

        });
        mBtnItalic.setOnClickListener(v -> {
            mBtnItalic.postDelayed(() -> {
                mIsItalic = mBtnItalic.isChecked();
                if (mListener != null) {
                    mListener.onItalic(mIsItalic);
                }
            }, 100);

        });
        mBtnLine.setOnClickListener(v -> {
            mBtnLine.postDelayed(() -> {
                mIsUnderline = mBtnLine.isChecked();
                if (mListener != null) {
                    mListener.onUnderLine(mIsUnderline);
                }
            }, 100);

        });
        mLlSeekbar = $(R.id.ll_seekbar);
        mTvAlpha = $(R.id.tv_bar_value);
        mTVSubMenuText = $(R.id.tvSubMenuText);
        mBarAlpha = $(R.id.seekbar);
        mBarAlpha.setIsShowPrompt(false);
        mBarAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mTvAlpha.setText(Integer.toString(progress));
                    float p = progress / 100.0f;
                    if (mStatue == TEXT) {
                        mAlphaValue = 1 - p;
                        if (mListener != null) {
                            mListener.onAlpha(mAlphaValue);
                        }
                    } else if (mStatue == STROKE) {
                        mStrokeValue = p;
                        mStrokeValue = mStrokeValue == 0 ? 0.0001f : mStrokeValue;
                        if (mListener != null) {
                            mListener.onStroke(mStrokeColor, mStrokeValue * MAX_STROKE_WIDTH);
                        }
                    } else if (mStatue == SHADOW) {
                        mShadowAlpha = p;
                        notifyShadow();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mTvRadius = $(R.id.tv_radius);
        mBarRadius = $(R.id.bar_radius);
        mBarRadius.setIsShowPrompt(false);
        mBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mTvRadius.setText(Integer.toString(progress));
                    mShadowRadius = progress / 100.0f;
                    notifyShadow();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mTvDistance = $(R.id.tv_distance);
        mBarDistance = $(R.id.bar_distance);
        mBarDistance.setIsShowPrompt(false);
        mBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mTvDistance.setText(Integer.toString(progress));
                    mShadowDistance = progress / 100.0f;
                    notifyShadow();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mTvAngle = $(R.id.tv_angle);
        mBarAngle = $(R.id.bar_angle);
        mBarAngle.setIsShowPrompt(false);
        mBarAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mTvAngle.setText(Integer.toString(progress));
                    mShadowAngle = progress / 100.0f;
                    notifyShadow();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mTvLineSpacing = $(R.id.tv_line_spacing);
        mBarLineSpacing = $(R.id.bar_line_spacing);
        mBarLineSpacing.setLeftValue(LEFT_VALUE);
        mBarLineSpacing.setShowPrompt(false);
        mBarLineSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float v = LEFT_VALUE * 100;
                if (progress > v) {
                    mLineSpacing = (progress - v) / (100 - v);
                } else {
                    mLineSpacing = (progress - v) / v;
                }
                mTvLineSpacing.setText(Integer.toString((int) (mLineSpacing * 100)));
                if (fromUser) {
                    mListener.onSpacing(mWordKerning, mLineSpacing);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mTvWordKerning = $(R.id.tv_wordKerning);
        mBarWordKerning = $(R.id.bar_wordKerning);
        mBarWordKerning.setLeftValue(LEFT_VALUE);
        mBarWordKerning.setShowPrompt(false);
        mBarWordKerning.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float v = LEFT_VALUE * 100;
                if (progress > v) {
                    mWordKerning = (progress - v) / (100 - v);
                } else {
                    mWordKerning = (progress - v) / v;
                }
                mTvWordKerning.setText(Integer.toString((int) (mWordKerning * 100)));
                if (fromUser) {
                    mListener.onSpacing(mWordKerning, mLineSpacing);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //对齐
        $(R.id.iv_align_left).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onAlign(0, 0);
            }
        });
        $(R.id.iv_align_center).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onAlign(1, 1);
            }
        });
        $(R.id.iv_align_right).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onAlign(2, 2);
            }
        });
        $(R.id.iv_align_up).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onAlign(-1, 0);
            }
        });
        $(R.id.iv_align_middle).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onAlign(-1, 1);
            }
        });
        $(R.id.iv_align_down).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onAlign(-1, 2);
            }
        });
    }

    /**
     * 设置阴影颜色时 (阴影效果默认样式)
     */
    private void setDefaultShadowStyle() {
        if (mShadowRadius == 0) { //没有设置阴影半径时
            mShadowRadius = 0.5f; // 默认0.5f,不然首次设置颜色无效果
            setShadowLayoutValue();
        }
        if (mShadowDistance == 0) {
            mShadowDistance = 0.1f; // 默认0.1f
            setShadowLayoutValue();
        }
    }

    /**
     * 恢复默认
     */
    public void reset() {
        reset(new WordInfoExt());
    }

    /**
     * 恢复选中
     */
    public void reset(WordInfoExt info) {
        if (info == null || mRbText == null) {
            return;
        }
        CaptionItem captionItem = info.getCaption().getCaptionItem();
        if (captionItem == null) {
            return;
        }
        //粗体、斜体、下划线
        mIsBold = captionItem.isBold();
        mIsItalic = captionItem.isItalic();
        mIsUnderline = captionItem.isUnderline();
        //背景
        mLabelColor = captionItem.getBackgroundColor();
        //透明
        mAlphaValue = captionItem.getAlpha();


        //阴影
        mShadowColor = captionItem.getShadowColor();
        mShadowRadius = captionItem.getShadowRadius();
        mShadowDistance = captionItem.getShadowDistance();
        mShadowAngle = captionItem.getShadowAngle();
        mShadowAlpha = captionItem.getShadowAlpha();

        //描边
        mStrokeColor = captionItem.getOutlineColor();
        mStrokeValue = captionItem.getOutlineWidth();
        //颜色
        mTextColor = captionItem.getTextColor();
        //间距
        mLineSpacing = captionItem.getLineSpacing();
        mWordKerning = captionItem.getWordKerning();
        resetUI();
    }

    private void resetUI() {
        if (mBarLineSpacing == null) {
            return;
        }
        //粗体、斜体、下划线
        mBtnBold.setChecked(mIsBold);
        mBtnItalic.setChecked(mIsItalic);
        mBtnLine.setChecked(mIsUnderline);

        //阴影
        setShadowLayoutValue();

        //间距
        setSpacingValue();

    }

    /**
     * 阴影
     */
    private void setShadowLayoutValue() {
        int tmp = (int) (mShadowRadius * 100);
        mBarRadius.setProgress(tmp);
        mTvRadius.setText(tmp + "");
        tmp = (int) (mShadowDistance * 100);
        mBarDistance.setProgress(tmp);
        mTvDistance.setText(tmp + "");
        tmp = (int) (mShadowAngle * 100);
        mBarAngle.setProgress(tmp);
        mTvAngle.setText(tmp + "");
        tmp = (int) (mShadowAlpha * 100);
        mBarAlpha.setProgress(tmp);
        mTvAlpha.setText(tmp + "");

    }


    private void notifyShadow() {
        if (mListener != null) {
            mListener.onShadow(mShadowColor, mShadowRadius, mShadowDistance, mShadowAngle, mShadowAlpha);
        }
    }

    private static final String TAG = "CaptionFunctionHandler";

    /**
     * 切换菜单
     *
     * @param id
     */
    public void switchUI(int id) {
        mRbText.setChecked(false);
        mRbStroke.setChecked(false);
        mRbShadow.setChecked(false);
        mRbLabel.setChecked(false);

        mLlColor.setVisibility(View.GONE);
        mTextStyleLayout.setVisibility(View.GONE);
        mLlSeekbar.setVisibility(View.GONE);
        mPositionLayout.setVisibility(View.GONE);
        mShadowLayout.setVisibility(View.GONE);
        mSpacingLayout.setVisibility(View.GONE);

        if (id == R.id.rb_text) {
            mStatue = TEXT;
            mLlColor.setVisibility(View.VISIBLE);
            mLlSeekbar.setVisibility(View.VISIBLE);
            mTextStyleLayout.setVisibility(View.VISIBLE);
            mTVSubMenuText.setText(R.string.pesdk_subtitle_alpha);
            mBarAlpha.setProgress((int) (mAlphaValue * 100));
            mRbText.setChecked(true);
            if (mTextColor != 0) {
                mColorBar.setColor(mTextColor);
            }
        } else if (id == R.id.rb_stroke) {
            mStatue = STROKE;
            mLlColor.setVisibility(View.VISIBLE);
            mLlSeekbar.setVisibility(View.VISIBLE);
            mTVSubMenuText.setText(R.string.pesdk_subtitle_border_size);
            mBarAlpha.setProgress((int) (mStrokeValue * 100));
            mRbStroke.setChecked(true);
            if (mStrokeColor != 0) {
                mColorBar.setColor(mStrokeColor);
            }
        } else if (id == R.id.rb_shadow) {//阴影
            mStatue = SHADOW;
            mLlColor.setVisibility(View.VISIBLE);
            mShadowLayout.setVisibility(View.VISIBLE);
            mLlSeekbar.setVisibility(View.VISIBLE);
            mRbShadow.setChecked(true);
            if (mShadowColor != 0) {
                mColorBar.setColor(mShadowColor);
            }
            mTVSubMenuText.setText(R.string.pesdk_subtitle_alpha);
            setShadowLayoutValue();
        } else if (id == R.id.rb_label) {
            mStatue = LABEL;
            mLlColor.setVisibility(View.VISIBLE);
            mRbLabel.setChecked(true);
            if (mLabelColor != 0) {
                mColorBar.setColor(mLabelColor);
            }
        } else if (id == R.id.rb_align) {
            mStatue = ALIGN;
            mPositionLayout.setVisibility(View.VISIBLE);
        } else if (id == R.id.rb_text_padding) {
            mStatue = TEXT_SPACING;
            mSpacingLayout.setVisibility(View.VISIBLE);
            //间距
            setSpacingValue();
        }

    }

    /**
     * 间距
     */
    private void setSpacingValue() {
        float v = LEFT_VALUE * 100;
        if (mLineSpacing >= 0) {
            mBarLineSpacing.setProgress((int) (mLineSpacing * (100 - v) + v));
            mTvWordKerning.setText(Integer.toString((int) (mLineSpacing * 100)));
        } else {
            mBarLineSpacing.setProgress((int) (mLineSpacing * v + v));
            mTvWordKerning.setText(Integer.toString((int) (mLineSpacing * 10)));
        }
        if (mWordKerning >= 0) {
            mBarWordKerning.setProgress((int) (mWordKerning * (100 - v) + v));
            mTvWordKerning.setText(Integer.toString((int) (mWordKerning * 100)));
        } else {
            mBarWordKerning.setProgress((int) (mWordKerning * v + v));
            mTvWordKerning.setText(Integer.toString((int) (mWordKerning * 10)));
        }
    }



    private OnSubtitleStyleListener mListener;

    public void setListener(OnSubtitleStyleListener listener) {
        this.mListener = listener;
    }


    public interface OnSubtitleStyleListener {

        /**
         * 加粗
         */
        void onBold(boolean b);

        /**
         * 斜体
         */
        void onItalic(boolean b);

        /**
         * 下划线
         */
        void onUnderLine(boolean underLine);

        /**
         * 文本颜色
         */
        void onColor(int color);

        /**
         * 描边
         */
        void onStroke(int color, float value);

        /**
         * 阴影
         */
        void onShadow(int color, float radius, float distance, float angle, float alpha);

        /**
         * 透明度
         */
        void onAlpha(float value);

        /**
         * 标签
         */
        void onLabel(int color);

        /**
         * 预设
         */
        void onPreset(PresetStyle style);

        /**
         * 间距
         */
        void onSpacing(float wordKerning, float lineSpacing);


        /**
         * 对齐
         */
        void onAlign(int hor, int ver);
    }


}
