package com.pesdk.api.manager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.pesdk.uisdk.R;
import com.vecore.models.WatermarkBuilder;

public class TextWatermarkBuilder extends WatermarkBuilder {

    private Context mContext;
    private String mWatermarkContent;
    private int mTextSize;
    private int mTextColor;
    private int mTextShadowColor;

    public TextWatermarkBuilder(Context context, String path) {
        super();
        setPath(path);
        mContext = context;
        mTextColor = mContext.getResources().getColor(R.color.pesdk_white);
        mTextShadowColor = 0;
    }

    @Override
    public View getView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.pesdk_layout_watermark, null);
        TextView textView = (TextView) view.findViewById(R.id.tvWatermarkContent);
        textView.setText(mWatermarkContent);
        textView.setTextSize(mTextSize);
        textView.setTextColor(mTextColor);
        textView.setShadowLayer(5, 0, 0, mTextShadowColor);
        return view;
    }


    public void setTextSize(int size) {
        mTextSize = size;
    }

    public int getTextSize() {
        return mTextSize;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
    }

    public int getTextShadowColor() {
        return mTextShadowColor;
    }

    public void setTextShadowColor(int textShadowColor) {
        this.mTextShadowColor = textShadowColor;
    }


    public void setWatermarkContent(String content) {
        mWatermarkContent = content;
    }

}
