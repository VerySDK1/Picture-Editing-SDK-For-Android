package com.pesdk.uisdk.widget;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.vecore.base.lib.ui.ExtTextView;

public class AutoView extends LinearLayout {
    private final int SRCWIDTH = 45;
    private Paint mTextPaint = new Paint();
    private boolean mOnUp, mIsLeft;
    private int mOffXPx;

    private double mPcenterX;
    private View mAutoView;
    private ExtTextView mTextView;
    private String mText;
    private int mAutoViewMargin = -1;


    public AutoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float textSize = 15;
        this.setOrientation(LinearLayout.VERTICAL);
        mTextView = new ExtTextView(context,null);
        mTextView.setText(mText);
        mTextView.setTextSize(textSize);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setAntiAlias(true);
        mTextView.setTextColor(getResources().getColor(R.color.pesdk_black));
        mTextView.setPadding(8, 8, 8, 8);
        mTextView.setBackgroundResource(R.drawable.pesdk_autoview_bg);
        mTextView.setSingleLine(true);
        mTextView.setMarqueeRepeatLimit(1);
        mTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mAutoView = new View(context);

    }

    public TextView getTextView() {
        return mTextView;
    }

    public void setAutoViewMargin(int margin) {
        mAutoViewMargin = margin;
    }

    public void setUpOrDown(boolean mOnUp, int offXpx, int strId, boolean isLeft,
                            double pCenterX) {
        this.mOnUp = mOnUp;
        this.mIsLeft = isLeft;
        mPcenterX = pCenterX;


        if (offXpx == -1) {
            mAutoView.setVisibility(View.GONE);
        }

        mText = mTextView.getResources().getString(strId);

        offXpx = offXpx - SRCWIDTH / 2;
        this.mOffXPx = (int) offXpx;

        mTextView.setText(mText);

        LayoutParams ltv = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        mTextView.setLayoutParams(ltv);

        LayoutParams lp = new LayoutParams(SRCWIDTH, SRCWIDTH);

        if (mOnUp) {
            mAutoView.setBackgroundResource(R.drawable.pesdk_up);
            if (mIsLeft) {
                lp.setMargins(mOffXPx, 0, mOffXPx + lp.width, lp.height);
            } else {
                int right = mTextView.getWidth() - mOffXPx;
                lp.setMargins(right - SRCWIDTH, 0, right, lp.height);
            }

            mAutoView.setLayoutParams(lp);
            this.addView(mAutoView);

            ltv.setMargins(0, -lp.bottomMargin, 0, 0);
            mTextView.setLayoutParams(ltv);
            this.addView(mTextView);
        } else {
            mTextView.setLayoutParams(ltv);
            this.addView(mTextView);

            if (mIsLeft) {
                if (mAutoViewMargin != -1) {
                    lp.setMargins(mAutoViewMargin, 0, 0, lp.height);
                } else {
                    lp.setMargins(mOffXPx, 0, mOffXPx + lp.width, lp.height);
                }
            } else {
                int right = mTextView.getWidth() - mOffXPx;
                lp.setMargins(right - SRCWIDTH, 0, right, lp.height);
            }
            mAutoView.setBackgroundResource(R.drawable.pesdk_down);
            mAutoView.setLayoutParams(lp);

            this.addView(mAutoView);
        }

    }


    /**
     * 重新刷新位置
     *
     * @return
     */
    public int[] setLocation() {
        LayoutParams lp = new LayoutParams(SRCWIDTH, SRCWIDTH);

        if (mPcenterX != 0) {
            mOffXPx = (int) (mTextView.getWidth() * mPcenterX - mAutoView.getWidth() / 2);
        }

        if (mOnUp) {
            if (mIsLeft) {
                lp.setMargins(mOffXPx, 0, mOffXPx + lp.width, lp.height);
            } else {
                int right = mTextView.getWidth() - mOffXPx;
                lp.setMargins(right - SRCWIDTH, 0, right, lp.height);
            }
            mAutoView.setLayoutParams(lp);
        } else {
            if (mIsLeft) {
                lp.setMargins(mOffXPx, 0, mOffXPx + lp.width, lp.height);
            } else {
                int right = mTextView.getWidth() - mOffXPx;
                lp.setMargins(right - SRCWIDTH, 0, right, lp.height);
            }
            mAutoView.setLayoutParams(lp);
        }
        int[] size = new int[]{mTextView.getWidth(),
                mTextView.getHeight() + mAutoView.getHeight()};
        return size;
    }

}
