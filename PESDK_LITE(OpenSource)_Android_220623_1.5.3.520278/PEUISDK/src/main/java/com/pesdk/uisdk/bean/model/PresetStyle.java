package com.pesdk.uisdk.bean.model;

public class PresetStyle {

    private int mIcon;//图标
    private int mTextColor;//文本颜色
    private int mStrokeColor;//描边颜色
    private float mStrokeValue;//描边大小
    private int mShadowColor;//阴影颜色
    private float mShadowValue;//阴影大小
    private int mLabel;//标签
    private float mAlpha = 1;//透明度
    private boolean mIsBold;//粗体
    private boolean mIsItalic;//斜体

    //文字 描边
    public PresetStyle(int icon, int textColor, int strokeColor, float strokeValue) {
        mIcon = icon;
        mTextColor = textColor;
        mStrokeColor = strokeColor;
        mStrokeValue = strokeValue;
    }

    //文字 标签
    public PresetStyle(int icon, int textColor, int label) {
        mIcon = icon;
        mTextColor = textColor;
        mLabel = label;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        mStrokeColor = strokeColor;
    }

    public float getStrokeValue() {
        return mStrokeValue;
    }

    public void setStrokeValue(float strokeValue) {
        mStrokeValue = strokeValue;
    }

    public int getShadowColor() {
        return mShadowColor;
    }

    public void setShadowColor(int shadowColor) {
        mShadowColor = shadowColor;
    }

    public float getShadowValue() {
        return mShadowValue;
    }

    public void setShadowValue(float shadowValue) {
        mShadowValue = shadowValue;
    }

    public int getLabel() {
        return mLabel;
    }

    public void setLabel(int label) {
        mLabel = label;
    }

    public float getAlpha() {
        return mAlpha;
    }

    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }

    public boolean isBold() {
        return mIsBold;
    }

    public void setBold(boolean bold) {
        mIsBold = bold;
    }

    public boolean isItalic() {
        return mIsItalic;
    }

    public void setItalic(boolean italic) {
        mIsItalic = italic;
    }

    public int getIcon() {
        return mIcon;
    }
}
