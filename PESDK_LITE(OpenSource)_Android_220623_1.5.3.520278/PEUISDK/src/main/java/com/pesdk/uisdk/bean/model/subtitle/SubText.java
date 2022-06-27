package com.pesdk.uisdk.bean.model.subtitle;

import android.graphics.Color;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import com.pesdk.uisdk.bean.model.flower.FlowerManager;
import com.pesdk.uisdk.bean.model.flower.WordFlower;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.models.caption.CaptionItem;

/**
 * 文字
 */
public class SubText implements Parcelable {

    //本地文件路径
    public String mLocalPath;
    //内容
    private String textContent;
    //楼空
    private int mask;
    //颜色 250 250 250
    private int[] textColor;
    //字体小
    private float fontSize;
    //字体 **.ttc
    private String fontFile;
    //显示区域 *, *, *, *
    private float[] showRect;
    //对齐 center
    private String alignment;
    //竖排、加粗、倾斜、下划线 0/1
    private int vertical;
    private int bold;
    private int italic;
    private int underline;
    //花字
    private String color_text;
    //阴影
    private int shadow;
    private int shadowAutoColor;
    private int[] shadowColor;
    private int shadowAngle;
    private float shadowBlur;
    private float shadowDistance;
    private float[] shadowOffset;
    //透明
    private float alpha;
    //角度
    private int angle;
    //轮廓
    private int outline;
    private float outlineWidth;
    private int[] outlineColor;
    //背景
    private int background;
    private int[] backgroundColor;
    //颜色
    private int[] ktvColor;
    private int[] ktvOutlineColor;
    private int[] ktvShadowColor;
    //时间
    private float startTime;
    private float duration;
    //动画
    private SubTextAnim[] anims;


    public SubText copy() {
        SubText subText = new SubText();
        subText.setLocalPath(mLocalPath);
        subText.setTextContent(textContent);
        subText.setMask(mask);
        subText.setTextColor(textColor);
        subText.setFontSize(fontSize);
        subText.setFontFile(fontFile);
        subText.setShowRect(showRect);
        subText.setAlignment(alignment);

        subText.setVertical(vertical);
        subText.setBold(bold);
        subText.setItalic(italic);
        subText.setUnderline(underline);

        subText.setColorText(color_text);

        subText.setShadow(shadow);
        subText.setShadowAutoColor(shadowAutoColor);
        subText.setShadowColor(shadowColor);
        subText.setShadowAngle(shadowAngle);
        subText.setShadowDistance(shadowDistance);
        subText.setShadowOffset(shadowOffset);
        subText.setShadowBlur(shadowBlur);

        subText.setAlpha(alpha);

        subText.setAngle(angle);

        subText.setOutline(outline);
        subText.setOutlineWidth(outlineWidth);
        subText.setOutlineColor(outlineColor);

        subText.setBackground(background);
        subText.setBackgroundColor(backgroundColor);

        subText.setKtvColor(ktvColor);
        subText.setKtvOutlineColor(ktvOutlineColor);
        subText.setKtvShadowColor(ktvShadowColor);

        subText.setStartTime(startTime);
        subText.setDuration(duration);

        subText.setAnims(anims);
        return subText;
    }

    public CaptionItem getLabel() {
        CaptionItem captionItem = new CaptionItem();
        //文字
        captionItem.setHintContent(textContent);
        //镂空
        captionItem.setMask(mask == 1);
        //大小
        captionItem.setFontSize(fontSize);
        //文字颜色
        captionItem.setTextColor(getTextColor());
        //字体
        captionItem.setFontFile(getFontFile());
        //对齐
        if (vertical == 1) {
            if ("center".equals(alignment)) {
                captionItem.setAlignment(1, 1);
            } else if ("left".equals(alignment)) {
                captionItem.setAlignment(1, 0);
            } else if ("right".equals(alignment)) {
                captionItem.setAlignment(1, 2);
            } else {
                captionItem.setAlignment(1, 1);
            }
        } else {
            if ("center".equals(alignment)) {
                captionItem.setAlignment(1, 1);
            } else if ("left".equals(alignment)) {
                captionItem.setAlignment(0, 1);
            } else if ("right".equals(alignment)) {
                captionItem.setAlignment(2, 1);
            } else {
                captionItem.setAlignment(1, 1);
            }
        }
        //粗体
        captionItem.setVertical(vertical);//方向
        captionItem.setBold(bold == 1);
        captionItem.setItalic(italic == 1);
        captionItem.setUnderline(underline == 1);
        //角度
        captionItem.setAngle(angle);
        //透明
        captionItem.setAlpha(alpha);
        //描边
        boolean o = outline == 1;
        captionItem.setOutline(o);
        if (o) {
            captionItem.setOutlineColor(getOutlineColor());
            captionItem.setOutlineWidth(outlineWidth);
        }
        //阴影
        o = shadow == 1;
        captionItem.setShadow(o);
        if (o) {
            if (shadowOffset != null && shadowOffset.length >= 2) {
                captionItem.setShadow(getShadowColor(), shadowOffset[0], shadowOffset[1], shadowAngle);
            } else {
                captionItem.setShadow(getShadowColor(), shadowBlur, shadowDistance, shadowAngle);
            }
        }
        //背景
        if (background == 1) {
            captionItem.setBackgroundColor(getBackgroundColor());
        }
        //KTV
        captionItem.setKtvShadowColor(getKtvShadowColor());
        captionItem.setKtvOutlineColor(getKtvOutlineColor());
        captionItem.setKtvColor(getKtvColor());
        //时间
        captionItem.setStartTime(startTime);
        captionItem.setDuration(duration);

        //花字
        if (color_text != null) {
            WordFlower wordFlower = FlowerManager.getInstance().parsingConfig(PathUtils.getFilePath(mLocalPath, color_text));
            captionItem.setEffectConfig(wordFlower == null ? null : wordFlower.getEffect());
        }

        return captionItem;
    }


    public String getFontFile() {
        return mLocalPath + "/" + fontFile;
    }

    public RectF getShowRect() {
        if (showRect != null && showRect.length == 4) {
            return new RectF(showRect[0], showRect[1], showRect[2], showRect[3]);
        }
        return null;
    }

    public int getTextColor() {
        if (textColor != null && textColor.length == 3) {
            return Color.argb(255, textColor[0], textColor[1], textColor[2]);
        }
        return 0;
    }

    public int getShadowColor() {
        if (shadowColor != null && shadowColor.length == 3) {
            return Color.argb(255, shadowColor[0], shadowColor[1], shadowColor[2]);
        }
        return 0;
    }

    public int getOutlineColor() {
        if (outlineColor != null && outlineColor.length == 3) {
            return Color.argb(255, outlineColor[0], outlineColor[1], outlineColor[2]);
        }
        return 0;
    }

    public int getBackgroundColor() {
        if (backgroundColor != null && backgroundColor.length == 3) {
            return Color.argb(255, backgroundColor[0], backgroundColor[1], backgroundColor[2]);
        }
        return 0;
    }

    public int getKtvColor() {
        if (ktvColor != null && ktvColor.length == 3) {
            return Color.argb(255, ktvColor[0], ktvColor[1], ktvColor[2]);
        }
        return 0;
    }

    public int getKtvOutlineColor() {
        if (ktvOutlineColor != null && ktvOutlineColor.length == 3) {
            return Color.argb(255, ktvOutlineColor[0], ktvOutlineColor[1], ktvOutlineColor[2]);
        }
        return 0;
    }

    public int getKtvShadowColor() {
        if (ktvShadowColor != null && ktvShadowColor.length == 3) {
            return Color.argb(255, ktvShadowColor[0], ktvShadowColor[1], ktvShadowColor[2]);
        }
        return 0;
    }

    public float getFontSize() {
        return fontSize;
    }

    public SubTextAnim[] getAnim() {
        return anims;
    }

    public void setLocalPath(String localPath) {
        mLocalPath = localPath;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    public void setTextColor(int[] textColor) {
        this.textColor = textColor;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public void setFontFile(String fontFile) {
        this.fontFile = fontFile;
    }

    public void setShowRect(float[] showRect) {
        this.showRect = showRect;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public void setVertical(int vertical) {
        this.vertical = vertical;
    }

    public void setBold(int bold) {
        this.bold = bold;
    }

    public void setItalic(int italic) {
        this.italic = italic;
    }

    public void setUnderline(int underline) {
        this.underline = underline;
    }

    public void setColorText(String color_text) {
        this.color_text = color_text;
    }

    public void setShadowAutoColor(int shadowAutoColor) {
        this.shadowAutoColor = shadowAutoColor;
    }

    public void setShadowColor(int[] shadowColor) {
        this.shadowColor = shadowColor;
    }

    public void setShadow(int shadow) {
        this.shadow = shadow;
    }

    public void setShadowAngle(int shadowAngle) {
        this.shadowAngle = shadowAngle;
    }

    public void setShadowBlur(float shadowBlur) {
        this.shadowBlur = shadowBlur;
    }

    public void setShadowDistance(float shadowDistance) {
        this.shadowDistance = shadowDistance;
    }

    public void setShadowOffset(float[] shadowOffset) {
        this.shadowOffset = shadowOffset;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public void setOutline(int outline) {
        this.outline = outline;
    }

    public void setOutlineWidth(float outlineWidth) {
        this.outlineWidth = outlineWidth;
    }

    public void setOutlineColor(int[] outlineColor) {
        this.outlineColor = outlineColor;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public void setBackgroundColor(int[] backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setKtvColor(int[] ktvColor) {
        this.ktvColor = ktvColor;
    }

    public void setKtvOutlineColor(int[] ktvOutlineColor) {
        this.ktvOutlineColor = ktvOutlineColor;
    }

    public void setKtvShadowColor(int[] ktvShadowColor) {
        this.ktvShadowColor = ktvShadowColor;
    }

    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public void setAnims(SubTextAnim[] anims) {
        if (anims != null && anims.length > 0) {
            this.anims = new SubTextAnim[anims.length];
            for (int i = 0; i < anims.length; i++) {
                this.anims[i] = anims[i].copy();
            }
        } else {
            this.anims = null;
        }
    }


    public static Creator<SubText> getCREATOR() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mLocalPath);
        dest.writeString(this.textContent);
        dest.writeIntArray(this.textColor);
        dest.writeFloat(this.fontSize);
        dest.writeString(this.fontFile);
        dest.writeFloatArray(this.showRect);
        dest.writeString(this.alignment);
        dest.writeInt(this.vertical);
        dest.writeInt(this.bold);
        dest.writeInt(this.italic);
        dest.writeInt(this.underline);
        dest.writeString(this.color_text);
        dest.writeInt(this.shadow);
        dest.writeInt(this.shadowAutoColor);
        dest.writeInt(this.shadowAngle);
        dest.writeFloat(this.shadowBlur);
        dest.writeFloat(this.shadowDistance);
        dest.writeFloatArray(this.shadowOffset);
        dest.writeIntArray(this.shadowColor);
        dest.writeFloat(this.alpha);
        dest.writeInt(this.angle);
        dest.writeInt(this.outline);
        dest.writeFloat(this.outlineWidth);
        dest.writeIntArray(this.outlineColor);
        dest.writeInt(this.background);
        dest.writeIntArray(this.backgroundColor);
        dest.writeIntArray(this.ktvColor);
        dest.writeIntArray(this.ktvOutlineColor);
        dest.writeIntArray(this.ktvShadowColor);
        dest.writeFloat(this.startTime);
        dest.writeFloat(this.duration);
        dest.writeTypedArray(this.anims, flags);
    }

    public SubText() {
    }

    protected SubText(Parcel in) {
        this.mLocalPath = in.readString();
        this.textContent = in.readString();
        this.textColor = in.createIntArray();
        this.fontSize = in.readFloat();
        this.fontFile = in.readString();
        this.showRect = in.createFloatArray();
        this.alignment = in.readString();
        this.vertical = in.readInt();
        this.bold = in.readInt();
        this.italic = in.readInt();
        this.underline = in.readInt();
        this.color_text = in.readString();
        this.shadow = in.readInt();
        this.shadowAutoColor = in.readInt();
        this.shadowAngle = in.readInt();
        this.shadowBlur = in.readFloat();
        this.shadowDistance = in.readFloat();
        this.shadowOffset = in.createFloatArray();
        this.shadowColor = in.createIntArray();
        this.alpha = in.readFloat();
        this.angle = in.readInt();
        this.outline = in.readInt();
        this.outlineWidth = in.readFloat();
        this.outlineColor = in.createIntArray();
        this.background = in.readInt();
        this.backgroundColor = in.createIntArray();
        this.ktvColor = in.createIntArray();
        this.ktvOutlineColor = in.createIntArray();
        this.ktvShadowColor = in.createIntArray();
        this.startTime = in.readFloat();
        this.duration = in.readFloat();
        this.anims = in.createTypedArray(SubTextAnim.CREATOR);
    }

    public static final Creator<SubText> CREATOR = new Creator<SubText>() {
        @Override
        public SubText createFromParcel(Parcel source) {
            return new SubText(source);
        }

        @Override
        public SubText[] newArray(int size) {
            return new SubText[size];
        }
    };
}
