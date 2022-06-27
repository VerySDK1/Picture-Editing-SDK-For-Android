package com.pesdk.api.manager;

import android.graphics.RectF;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.Gravity;

import com.vecore.models.Watermark;

import androidx.annotation.Keep;

/**
 * PESDK导出配置类
 */
@Keep
public class ExportConfiguration implements Parcelable {

    private static final int EXPORT_SIZE_max_MIN_SIDE = 2160;

    /**
     * 视频保存路径（文件夹）
     */
    public final String saveDir;

    /**
     * 29+ 支持分区存储
     */
    public final String artist;


    public final String relative_path;
    public final boolean saveToAlbum;

    protected ExportConfiguration(Parcel in) {
        saveDir = in.readString();
        artist = in.readString();
        relative_path = in.readString();
        saveToAlbum = in.readByte() != 0;
        watermarkPath = in.readString();
        enableTextWatermark = in.readByte() != 0;
        textWatermarkContent = in.readString();
        textWatermarkSize = in.readInt();
        textWatermarkColor = in.readInt();
        textWatermarkShadowColor = in.readInt();
        watermarkShowRectF = in.readParcelable(RectF.class.getClassLoader());
        isGravityMode = in.readByte() != 0;
        xAdj = in.readInt();
        yAdj = in.readInt();
        mWatermarkGravity = in.readInt();
        exportMinSide = in.readInt();
        watermarkShowMode = in.readInt();
        watermarkPortLayoutRectF = in.readParcelable(RectF.class.getClassLoader());
        watermarkLandLayoutRectF = in.readParcelable(RectF.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(saveDir);
        dest.writeString(artist);
        dest.writeString(relative_path);
        dest.writeByte((byte) (saveToAlbum ? 1 : 0));
        dest.writeString(watermarkPath);
        dest.writeByte((byte) (enableTextWatermark ? 1 : 0));
        dest.writeString(textWatermarkContent);
        dest.writeInt(textWatermarkSize);
        dest.writeInt(textWatermarkColor);
        dest.writeInt(textWatermarkShadowColor);
        dest.writeParcelable(watermarkShowRectF, flags);
        dest.writeByte((byte) (isGravityMode ? 1 : 0));
        dest.writeInt(xAdj);
        dest.writeInt(yAdj);
        dest.writeInt(mWatermarkGravity);
        dest.writeInt(exportMinSide);
        dest.writeInt(watermarkShowMode);
        dest.writeParcelable(watermarkPortLayoutRectF, flags);
        dest.writeParcelable(watermarkLandLayoutRectF, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExportConfiguration> CREATOR = new Creator<ExportConfiguration>() {
        @Override
        public ExportConfiguration createFromParcel(Parcel in) {
            return new ExportConfiguration(in);
        }

        @Override
        public ExportConfiguration[] newArray(int size) {
            return new ExportConfiguration[size];
        }
    };

    public String getArtist() {
        return !TextUtils.isEmpty(artist) ? artist : "pe";
    }

    public String getRelative_path() {
        return !TextUtils.isEmpty(relative_path) ? relative_path : Environment.DIRECTORY_DCIM + "/pe";
    }


    /**
     * 是否水印路径
     */
    private String watermarkPath;

    public String getWatermarkPath() {
        return watermarkPath;
    }

    public void setWatermarkPath(String watermarkPath) {
        this.watermarkPath = watermarkPath;
    }

    /**
     * 是否为文字水印
     */
    public boolean enableTextWatermark = false;
    /**
     * 文字水印内容
     */
    public String textWatermarkContent = null;
    /**
     * 文字水印大小
     */
    public int textWatermarkSize;
    /**
     * 文字水印颜色
     */
    public int textWatermarkColor;
    /**
     * 文字阴影颜色
     */
    public int textWatermarkShadowColor;
    /**
     * 水印显示区域
     */
    public final RectF watermarkShowRectF;

    public final boolean isGravityMode;
    //距离边框的绝对像素
    public final int xAdj, yAdj;
    //基于Android系统的 Gravity  (参照：Gravity.apply(int gravity, int w, int h, Rect container, int xAdj, int yAdj, Rect outRect))
    public final int mWatermarkGravity;


    /**
     * 分辨率
     */
    private int exportMinSide = 540;

    /**
     * 水印显示模式
     */
    public int watermarkShowMode = Watermark.MODE_DEFAULT;
    /**
     * 使用自定义导出引导界面
     */
    public boolean useCustomExportGuide = false;

    /**
     * 水印显示区域(竖屏)
     */
    public RectF watermarkPortLayoutRectF;

    /**
     * 水印显示区域(横屏)
     */
    public RectF watermarkLandLayoutRectF;

    /**
     * 获取导出图片的最大边
     *
     * @return 最大边
     */
    public int getMinSide() {
        return Math.min(EXPORT_SIZE_max_MIN_SIDE, exportMinSide);
    }


    /**
     * 导出时，用户主动指定最小边
     */
    public void setMinSide(int minWH) {
        if (minWH >= 16 && minWH <= EXPORT_SIZE_max_MIN_SIDE) {
            exportMinSide = minWH;
        }
    }


    private ExportConfiguration(Builder builder) {
        this.saveDir = builder.mSavePath;
        this.saveToAlbum = builder.saveToAlbum;
        this.artist = builder.artist;
        this.relative_path = builder.relative_path;
        this.exportMinSide = builder.mExportMinSide;
        this.watermarkPath = builder.mWatermarkPath;
        this.enableTextWatermark = builder.mEnableTextWatermark;
        this.textWatermarkContent = builder.mTextWatermarkContent;
        this.textWatermarkSize = builder.mTextWatermarkSize;
        this.textWatermarkColor = builder.mTextWatermarkColor;
        this.textWatermarkShadowColor = builder.mTextWatermarkShadowColor;
        this.watermarkShowRectF = builder.mWatermarkShowRectF;
        this.watermarkLandLayoutRectF = builder.mWatermarkLandLayout;
        this.watermarkPortLayoutRectF = builder.mWatermarkPortLayout;
        this.watermarkShowMode = builder.mWatermarkShowMode;


        this.isGravityMode = builder.isGravityMode;
        this.mWatermarkGravity = builder.mWatermarkGravity;
        this.xAdj = builder.xAdj;
        this.yAdj = builder.yAdj;
        this.useCustomExportGuide=builder.mUseCustomExportGuide;
    }

    /**
     * Builder class for {@link ExportConfiguration} objects.
     */
    public static class Builder {
        private int mExportMinSide = 540; //默认540
        private String mSavePath = null; // relative_path 为null时，启用
        private boolean saveToAlbum = true; //保存到相册
        private String artist;
        private String relative_path;
        private String mWatermarkPath = null;
        private boolean mEnableTextWatermark = false;
        private String mTextWatermarkContent = "";
        private int mTextWatermarkSize = 10;
        private int mTextWatermarkColor = 0;
        private int mTextWatermarkShadowColor = 0;
        private RectF mWatermarkShowRectF = null;
        private RectF mWatermarkPortLayout = null;
        private RectF mWatermarkLandLayout = null;
        private int mWatermarkShowMode;
        private boolean isGravityMode = true;
        private boolean mUseCustomExportGuide = false;

        //距离边框的绝对像素
        private int xAdj, yAdj = 0;
        //基于Android系统的 Gravity  (参照：Gravity.apply(int gravity, int w, int h, Rect container, int xAdj, int yAdj, Rect outRect))
        private int mWatermarkGravity = Gravity.LEFT | Gravity.TOP;

        /**
         * 设置是否使用自定义导出引导界面
         *
         * @param useCustomExportGuide
         * @return
         */
        public Builder useCustomExportGuide(boolean useCustomExportGuide) {
            mUseCustomExportGuide = useCustomExportGuide;
            return this;
        }

        /**
         * 水印位置
         */
        public Builder setWatermarkGravity(int gravity) {
            this.mWatermarkGravity = gravity;
            isGravityMode = true;
            return this;
        }

        /**
         * 距离视频边的像素
         *
         * @param xAdj
         * @param yAdj
         * @return
         */
        public Builder setAdj(int xAdj, int yAdj) {
            this.xAdj = xAdj;
            this.yAdj = yAdj;
            return this;
        }

        /**
         * 导出后的存放目录，
         *
         * @param saveToAlbum 是否保存到系统图库
         */
        public Builder saveToAlbum(boolean saveToAlbum) {
            this.saveToAlbum = saveToAlbum;
            return this;
        }

        /**
         * 保存到系统provider的参数 （详见android 29 分区存储适配）
         *
         * @param artist
         * @param relative_path 图片|视频 相对目录
         */
        public Builder setRelativePath(String artist, String relative_path) {
            this.artist = artist;
            this.relative_path = relative_path; //https://www.jianshu.com/p/b3595fc1f9be
            return this;
        }

        /**
         * /**
         * 设置导出视频路径
         *
         * @param savePath 导出视频路径,传null将保存到默认路径
         */
        public Builder setSavePath(String savePath) {
            this.mSavePath = savePath;
            return this;
        }


        /**
         * 设置导出视频最小边
         *
         * @param minSide 导出视频最小边
         */
        public Builder setMinSide(int minSide) {
            mExportMinSide = Math.max(176, Math.min(minSide, EXPORT_SIZE_max_MIN_SIDE));
            return this;
        }


        /**
         * 设置图片水印路径
         *
         * @param path 水印路径
         */
        public Builder setWatermarkPath(String path) {
            this.mWatermarkPath = path;
            return this;
        }

        /**
         * 设置是否使用文字水印（启用文字水印，图片水印将失效）
         *
         * @param enable
         */
        public Builder enableTextWatermark(boolean enable) {
            this.mEnableTextWatermark = enable;
            return this;
        }

        /**
         * 设置文字水印内容
         *
         * @param content
         */
        public Builder setTextWatermarkContent(String content) {
            this.mTextWatermarkContent = content;
            return this;
        }

        /**
         * 设置文字水印大小
         *
         * @param size
         */
        public Builder setTextWatermarkSize(int size) {
            this.mTextWatermarkSize = size;
            return this;
        }

        /**
         * 设置文字水印颜色
         *
         * @param color 文字颜色（默认白色）
         */
        public Builder setTextWatermarkColor(int color) {
            this.mTextWatermarkColor = color;
            return this;
        }

        /**
         * 设置文字水印阴影颜色（不设置将没有阴影）
         *
         * @param color 文字阴影颜色（默认无阴影）
         */
        public Builder setTextWatermarkShadowColor(int color) {
            this.mTextWatermarkShadowColor = color;
            return this;
        }

        /**
         * 设置水印显示区域 与 setWatermarkLayout互斥
         *
         * @param rectF rectF.left 代表在x轴的位置 <br>
         *              rectF.top 代表在y轴的位置 <br>
         *              rectF.right 代表x轴方向的缩放比例 <br>
         *              rectF.bottom 代表y轴方向的缩放比例 <br>
         */
        public Builder setWatermarkPosition(RectF rectF) {
            if (rectF != null) {
                isGravityMode = false;
                rectF.left = Math.max(0, Math.min(1, rectF.left));
                rectF.top = Math.max(0, Math.min(1, rectF.top));
                if (rectF.right == 0) {
                    rectF.right = 1;
                }
                if (rectF.bottom == 0) {
                    rectF.bottom = 1;
                }
                mWatermarkShowRectF = rectF;
                mWatermarkPortLayout = null;
                mWatermarkLandLayout = null;
            }
            return this;
        }

        /**
         * 设置水印显示区域 与 setWatermarkPosition互斥
         *
         * @param landRectF 横屏水印<br>
         *                  rectF.left 代表水印在x轴left位置(范围0-1 下同) <br>
         *                  rectF.top 代表水印在y轴top位置 <br>
         *                  rectF.right 代表水印在x轴right位置 <br>
         *                  rectF.bottom 代表水印在y轴bottom位置<br>
         * @param portRectF 竖屏水印<br>
         *                  同上
         */
        public Builder setWatermarkLayout(RectF landRectF, RectF portRectF) {
            isGravityMode = false;
            if (portRectF != null) {
                portRectF.left = Math.max(0, Math.min(1, portRectF.left));
                portRectF.right = Math.max(0, Math.min(1, portRectF.right));
                portRectF.top = Math.max(0, Math.min(1, portRectF.top));
                portRectF.bottom = Math.max(0, Math.min(1, portRectF.bottom));
            }
            if (landRectF != null) {
                landRectF.left = Math.max(0, Math.min(1, landRectF.left));
                landRectF.right = Math.max(0, Math.min(1, landRectF.right));
                landRectF.top = Math.max(0, Math.min(1, landRectF.top));
                landRectF.bottom = Math.max(0, Math.min(1, landRectF.bottom));
            }
            mWatermarkPortLayout = portRectF;
            mWatermarkLandLayout = landRectF;
            mWatermarkShowRectF = null;
            return this;
        }

        /**
         * 设置水印显示模式
         *
         * @param showMode 显示模式
         * @return
         */
        public Builder setWatermarkShowMode(int showMode) {
            mWatermarkShowMode = showMode;
            return this;
        }


        public ExportConfiguration get() {
            return new ExportConfiguration(this);
        }
    }


}
