package com.pesdk.uisdk.bean.model.flower;

import android.os.Parcel;
import android.os.Parcelable;

import com.vecore.models.caption.EffectColor;
import com.vecore.models.caption.EffectColorConfig;
import com.vecore.models.caption.EffectConfig;

/**
 * 花字
 */
public class WordFlower implements Parcelable {

    //版本
    private long ver;
    //最低核心
    private long minCoreVer;
    //普通
    private Normal normal;
    //阴影
    private Shadow shadow;
    //多层阴影
    private Shadow[] shadows;
    //描边
    private Stroke[] stroke;


    public EffectConfig getEffect() {

        EffectConfig baseConfig = new EffectConfig();

        //填充配置
        //LabelObject.EffectDiffConfig diffConfig = new LabelObject.EffectDiffConfig();
        //baseConfig.diffConfig = diffConfig;

        //普通文字颜色配置
        if (normal != null) {
            WordFlower.Color[] color = normal.getColor();
            for (WordFlower.Color c : color) {
                baseConfig.addNormalColor(c.getColor(), c.getFactor());
            }
            baseConfig.setNormalGradient(normal.isGradient());
        }

        //轮廓
        if (stroke != null) {
            for (WordFlower.Stroke s : stroke) {
                EffectColorConfig colorConfig = new EffectColorConfig();
                colorConfig.gradient(s.isGradient());
                colorConfig.setOutline(s.getWidth());
                WordFlower.Color[] colors = s.getColor();
                for (WordFlower.Color c : colors) {
                    colorConfig.addColor(new EffectColor(c.getColor(), c.getFactor()));
                }
                float[] colorAngleFactor = s.getColorAngleFactor();
                if (colorAngleFactor != null && colorAngleFactor.length > 2) {
                    colorConfig.setColorAngle(colorAngleFactor[0], colorAngleFactor[1], colorAngleFactor[2]);
                }
                baseConfig.addColorConfig(colorConfig);
            }
        }

        //阴影
        if (shadow != null) {
            EffectConfig shadowConfig;
            if (shadow.isAuto()) {
                shadowConfig = new EffectConfig();
            } else {
                shadowConfig = new EffectConfig(shadow.getColor());
            }
            shadowConfig.setShadow(shadow.getBlur(), shadow.getDistance(), shadow.getAngle());
            //轮廓
            if (shadow.stroke != null && shadow.stroke.length > 0) {
                for (WordFlower.Stroke s : shadow.stroke) {
                    EffectColorConfig colorConfig = new EffectColorConfig();
                    colorConfig.gradient(s.isGradient());
                    colorConfig.setOutline(s.getWidth());
                    WordFlower.Color[] colors = s.getColor();
                    for (WordFlower.Color c : colors) {
                        colorConfig.addColor(new EffectColor(c.getColor(), c.getFactor()));
                    }
                    float[] colorAngleFactor = s.getColorAngleFactor();
                    if (colorAngleFactor != null && colorAngleFactor.length > 2) {
                        colorConfig.setColorAngle(colorAngleFactor[0], colorAngleFactor[1], colorAngleFactor[2]);
                    }
                    shadowConfig.addColorConfig(colorConfig);
                }
            }
            baseConfig.setShadow(shadow.getBlur(), shadow.getDistance(), shadow.getAngle(), shadowConfig);
        }

        //阴影
        if (shadows != null) {
            for (WordFlower.Shadow shadow : shadows) {
                EffectConfig shadowConfig;
                if (shadow.isAuto()) {
                    shadowConfig = new EffectConfig();
                } else {
                    shadowConfig = new EffectConfig(shadow.getColor());
                }
                shadowConfig.setShadow(shadow.getBlur(), shadow.getDistance(), shadow.getAngle());
                //轮廓
                if (shadow.stroke != null && shadow.stroke.length > 0) {
                    for (WordFlower.Stroke s : shadow.stroke) {
                        EffectColorConfig colorConfig = new EffectColorConfig();
                        colorConfig.gradient(s.isGradient());
                        colorConfig.setOutline(s.getWidth());
                        WordFlower.Color[] colors = s.getColor();
                        for (WordFlower.Color c : colors) {
                            colorConfig.addColor(new EffectColor(c.getColor(), c.getFactor()));
                        }
                        float[] colorAngleFactor = s.getColorAngleFactor();
                        if (colorAngleFactor != null && colorAngleFactor.length > 2) {
                            colorConfig.setColorAngle(colorAngleFactor[0], colorAngleFactor[1], colorAngleFactor[2]);
                        }
                        shadowConfig.addColorConfig(colorConfig);
                    }
                }
                baseConfig.addShadow(shadow.getBlur(), shadow.getDistance(), shadow.getAngle(), shadowConfig);
            }
        }

        return baseConfig;
    }

    public long getVer() {
        return ver;
    }

    public void setVer(long value) {
        this.ver = value;
    }

    public long getMinCoreVer() {
        return minCoreVer;
    }

    public void setMinCoreVer(long value) {
        this.minCoreVer = value;
    }

    public Normal getNormal() {
        return normal;
    }

    public void setNormal(Normal value) {
        this.normal = value;
    }

    public Shadow getShadow() {
        return shadow;
    }

    public void setShadow(Shadow value) {
        this.shadow = value;
    }

    public Shadow[] getShadows() {
        return shadows;
    }

    public void setShadows(Shadow[] shadows) {
        this.shadows = shadows;
    }

    public Stroke[] getStroke() {
        return stroke;
    }

    public void setStroke(Stroke[] value) {
        this.stroke = value;
    }


    //rgb转颜色
    public static int rgb2Color(int[] color) {
        if (color == null || color.length < 4) {
            return 0;
        }
        return android.graphics.Color.argb(color[3], color[0], color[1], color[2]);
    }


    //普通文字颜色配置
    public static class Normal implements Parcelable {
        //描述
        private String note;
        //渐变
        private int gradient;
        //颜色
        private Color[] color;

        public String getNote() {
            return note;
        }

        public void setNote(String value) {
            this.note = value;
        }

        public boolean isGradient() {
            return gradient == 1;
        }

        public void setGradient(int value) {
            this.gradient = value;
        }

        public Color[] getColor() {
            return color;
        }

        public void setColor(Color[] value) {
            this.color = value;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.note);
            dest.writeInt(this.gradient);
            dest.writeTypedArray(this.color, flags);
        }

        protected Normal(Parcel in) {
            this.note = in.readString();
            this.gradient = in.readInt();
            this.color = in.createTypedArray(Color.CREATOR);
        }

        public static final Creator<Normal> CREATOR = new Creator<Normal>() {
            @Override
            public Normal createFromParcel(Parcel source) {
                return new Normal(source);
            }

            @Override
            public Normal[] newArray(int size) {
                return new Normal[size];
            }
        };
    }

    //阴影
    public static class Shadow implements Parcelable {
        //描述
        private String note;
        //自动
        private int auto;
        //颜色
        private int[] color;
        //距离
        private float distance;
        //角度
        private int angle;
        //模糊
        private float blur;
        //描边
        private Stroke[] stroke;

        public String getNote() {
            return note;
        }

        public void setNote(String value) {
            this.note = value;
        }

        public boolean isAuto() {
            return auto == 1;
        }

        public void setAuto(int value) {
            this.auto = value;
        }

        public int getColor() {
            return rgb2Color(color);
        }

        public void setColor(int[] value) {
            this.color = value;
        }

        public float getDistance() {
            return distance;
        }

        public void setDistance(float value) {
            this.distance = value;
        }

        public long getAngle() {
            return angle;
        }

        public void setAngle(int value) {
            this.angle = value;
        }

        public float getBlur() {
            return blur;
        }

        public void setBlur(float value) {
            this.blur = value;
        }

        public Stroke[] getStroke() {
            return stroke;
        }

        public void setStroke(Stroke[] stroke) {
            this.stroke = stroke;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.note);
            dest.writeInt(this.auto);
            dest.writeIntArray(this.color);
            dest.writeFloat(this.distance);
            dest.writeInt(this.angle);
            dest.writeFloat(this.blur);
            dest.writeTypedArray(this.stroke, flags);
        }

        protected Shadow(Parcel in) {
            this.note = in.readString();
            this.auto = in.readInt();
            this.color = in.createIntArray();
            this.distance = in.readFloat();
            this.angle = in.readInt();
            this.blur = in.readFloat();
            this.stroke = in.createTypedArray(Stroke.CREATOR);
        }

        public static final Creator<Shadow> CREATOR = new Creator<Shadow>() {
            @Override
            public Shadow createFromParcel(Parcel source) {
                return new Shadow(source);
            }

            @Override
            public Shadow[] newArray(int size) {
                return new Shadow[size];
            }
        };
    }

    //描边
    public static class Stroke implements Parcelable {
        //描述
        private String note;
        //是否为渐变色
        private int gradient;
        //轮廓宽度(0-1.0)
        private float width;
        //颜色角度因子（0-1）
        private float[] colorAngleFactor;
        //颜色
        private Color[] color;

        public String getNote() {
            return note;
        }

        public void setNote(String value) {
            this.note = value;
        }

        public boolean isGradient() {
            return gradient == 1;
        }

        public void setGradient(int value) {
            this.gradient = value;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float value) {
            this.width = value;
        }

        public float[] getColorAngleFactor() {
            return colorAngleFactor;
        }

        public void setColorAngleFactor(float[] value) {
            this.colorAngleFactor = value;
        }

        public Color[] getColor() {
            return color;
        }

        public void setColor(Color[] value) {
            this.color = value;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.note);
            dest.writeInt(this.gradient);
            dest.writeFloat(this.width);
            dest.writeFloatArray(this.colorAngleFactor);
            dest.writeTypedArray(this.color, flags);
        }

        protected Stroke(Parcel in) {
            this.note = in.readString();
            this.gradient = in.readInt();
            this.width = in.readFloat();
            this.colorAngleFactor = in.createFloatArray();
            this.color = in.createTypedArray(Color.CREATOR);
        }

        public static final Creator<Stroke> CREATOR = new Creator<Stroke>() {
            @Override
            public Stroke createFromParcel(Parcel source) {
                return new Stroke(source);
            }

            @Override
            public Stroke[] newArray(int size) {
                return new Stroke[size];
            }
        };
    }

    //颜色
    public static class Color implements Parcelable {
        //颜色
        int[] color;
        //颜色占比(推荐0.-1.0范围）
        float factor;

        public int getColor() {
            return rgb2Color(color);
        }

        public void setColor(int[] color) {
            this.color = color;
        }

        public float getFactor() {
            return factor;
        }

        public void setFactor(float factor) {
            this.factor = factor;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeIntArray(this.color);
            dest.writeFloat(this.factor);
        }

        protected Color(Parcel in) {
            this.color = in.createIntArray();
            this.factor = in.readFloat();
        }

        public static final Creator<Color> CREATOR = new Creator<Color>() {
            @Override
            public Color createFromParcel(Parcel source) {
                return new Color(source);
            }

            @Override
            public Color[] newArray(int size) {
                return new Color[size];
            }
        };
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.ver);
        dest.writeLong(this.minCoreVer);
        dest.writeParcelable(this.normal, flags);
        dest.writeParcelable(this.shadow, flags);
        dest.writeTypedArray(this.shadows, flags);
        dest.writeTypedArray(this.stroke, flags);
    }

    protected WordFlower(Parcel in) {
        this.ver = in.readLong();
        this.minCoreVer = in.readLong();
        this.normal = in.readParcelable(Normal.class.getClassLoader());
        this.shadow = in.readParcelable(Shadow.class.getClassLoader());
        this.shadows = in.createTypedArray(Shadow.CREATOR);
        this.stroke = in.createTypedArray(Stroke.CREATOR);
    }

    public static final Creator<WordFlower> CREATOR = new Creator<WordFlower>() {
        @Override
        public WordFlower createFromParcel(Parcel source) {
            return new WordFlower(source);
        }

        @Override
        public WordFlower[] newArray(int size) {
            return new WordFlower[size];
        }
    };
}
