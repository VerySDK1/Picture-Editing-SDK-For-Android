package com.pesdk.uisdk.bean.model;

import android.graphics.Color;
import android.graphics.RectF;
import android.util.SparseArray;

import com.pesdk.bean.DataBean;
import com.pesdk.uisdk.util.helper.CommonStyleUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 字幕特效 type==0,可以写字
 * 单个字幕特效，默认的样式（旋转、hint 、字体颜色等）
 */
public class StyleInfo implements Serializable {
    public int index = 0;
    public String code;//名字
    public String caption;//路径
    public String icon;//图标
    public String mlocalpath;//本地路径
    public long nTime = 0;//更新时间

    /**
     * 竖排
     */
    public boolean vertical;
    /**
     * 记录当前下载的版本
     */
    public boolean isdownloaded = false;
    /**
     * 仅贴纸支持apng
     */
    public boolean isApng = false;
    /**
     * 字幕的拉伸区域
     */
    public double left;
    public double top;
    public double right;
    public double buttom;

    /**
     * 是否字幕
     */
    private boolean isSub = true;

    /**
     * pid  路径的hashcode值
     */
    public int pid;
    /**
     * 0,可以写字
     */
    public int type;

    /**
     * 是否拉伸
     */
    public boolean lashen = false;
    /**
     * 是否只以一行方式显示
     */
    public boolean onlyone = false;
    /**
     * 是否阴影
     */
    public boolean shadow = false;

    public static final double DEFAULT_RECT_W = -1;
    /**
     * 部分贴纸支持直接设置横向占比  (相对于播放器的预览size  0~1.0f)
     */
    public double rectW = DEFAULT_RECT_W;

    /**
     * 默认缩放比
     */
    public float disf = 0.5f;
    public double srcWidth, srcHeight; //config中定义的宽高
    public double w, width;
    public double h;
    /**
     * 旋转角度
     */
    public float rotateAngle = 0.0f;
    public int du;
    public int tLeft;
    public int tTop;
    public int tRight;
    public int tButtom;
    public int tWidth;
    public int tHeight;
    /**
     * 字体
     */
    public String tFont;
    /**
     * 循环特效、字幕取第0帧
     */
    public SparseArray<FrameInfo> frameArray = new SparseArray<>();
    public ArrayList<TimeArray> timeArrays = new ArrayList<>();
    public String blendMode;

    /**
     * 默认 允许循环
     */
    public boolean unLoop = false;

    /**
     * 时间
     */
    private int frameDruation = 100;

    /**
     * 显示
     */
    public RectF mShowRectF;

    /**
     * 描边颜色 、描边宽度
     */
    public int strokeColor = 0;
    public int strokeWidth = 0;

    /**
     * 默认文本
     */
    private String hint = "";
    /**
     * 默认字体颜色
     */
    private int textDefaultColor = Color.WHITE;

    /**
     * 马赛克
     */
    public static final String FILTER_PIX = "pixelate";
    /**
     * 高斯
     */
    public static final String FILTER_BLUR = "blur";
    public String filter;
    public String filterPng;

    /**
     * 图片旋转中心点坐标在x，y的比例
     */
    public float[] centerxy = new float[]{0.5f, 0.5f};

    /**
     * 字幕、贴纸
     */
    public CommonStyleUtils.STYPE st = CommonStyleUtils.STYPE.sub;

    /**
     * 文本在组件中的显示区域
     */
    private RectF mTextRectF = new RectF(0.01f, 0.01f, 0.99f, 0.99f);

    /**
     * 贴纸分类代码
     */
    public String category;
    /**
     * 资源id
     */
    public String resourceId;


    /**
     * 自定义的字幕、特效
     *
     * @param isSub 是否是字幕
     */
    public StyleInfo(boolean isSub) {
        this.isSub = isSub;
    }

    public StyleInfo(boolean isSub, DataBean dataBean) {
        this.isSub = isSub;
        code = dataBean.getName();
        caption = dataBean.getFile();
        icon = dataBean.getCover();
        pid = code.hashCode();
        nTime = dataBean.getUpdatetime();
        st = isSub ? CommonStyleUtils.STYPE.sub : CommonStyleUtils.STYPE.special;
        index = caption.hashCode();
        resourceId = dataBean.getId();

    }


    /**
     * 是否是字幕
     */
    public boolean isSub() {
        return isSub;
    }


    /**
     * 是否有设置拉伸比例
     */
    public boolean isSetSizeW() {
        return rectW != DEFAULT_RECT_W;
    }

    /**
     * 修正单帧的时间
     */
    public void fixFrameDruation() {
        if (null != frameArray && frameArray.size() >= 2) {
            frameDruation = frameArray.valueAt(1).time
                    - frameArray.valueAt(0).time;
        } else {
            frameDruation = 100;
        }
    }

    /**
     * 特效单帧画面的duration ，单位:毫秒
     */
    public int getFrameDuration() {
        return frameDruation;
    }

    /**
     * 部分特效需要handler 循环绘制 (只有一张背景图，不需要循环绘制)
     */
    public boolean needWhileDraw() {
        return st == CommonStyleUtils.STYPE.special && (null != frameArray && frameArray.size() >= 2);
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getHint() {
        return hint;
    }

    public int getTextDefaultColor() {
        return textDefaultColor;
    }

    public RectF getTextRectF() {
        return mTextRectF;
    }

    public void setTextRectF(RectF textRectF) {
        mTextRectF = textRectF;
    }

    public void setNinePitch(RectF ninePitch) {
        mNinePitch = ninePitch;
    }

    private RectF mNinePitch = new RectF(0.1f, 0.1f, 0.99f, 0.99f);

    /**
     * 可拉伸的区域
     */
    public RectF getNinePitch() {
        return mNinePitch;
    }

    /**
     * 初始化默认的text 信息
     */
    public void initDefault(String hint, int textDefaultColor, RectF textRectF) {
        this.hint = hint;
        this.textDefaultColor = textDefaultColor;
        if (null != textRectF) {
            this.mTextRectF = textRectF;
        }
    }

    @Override
    public String toString() {
        return "StyleInfo{" +
                ", hash=" + hashCode() +
                ", index=" + index +
                ", code='" + code + '\'' +
//                ", caption='" + caption + '\'' +
                ", mlocalpath='" + mlocalpath + '\'' +
                ", vertical=" + vertical +
                ", nTime=" + nTime +
                ", isdownloaded=" + isdownloaded +
                ", left=" + left +
                ", top=" + top +
                ", right=" + right +
                ", buttom=" + buttom +
                ", isSub=" + isSub +
                ", pid=" + pid +
                ", type=" + type +
                ", lashen=" + lashen +
                ", onlyone=" + onlyone +
                ", shadow=" + shadow +
                ", disf=" + disf +
                ", w=" + w +
                ", h=" + h +
                ", du=" + du +
                ", tLeft=" + tLeft +
                ", tTop=" + tTop +
                ", tWidth=" + tWidth +
                ", tHeight=" + tHeight +
                ", tRight=" + tRight +
                ", tButtom=" + tButtom +
                ", tFont='" + tFont + '\'' +
//                ", frameArray=" + frameArray +
//                ", timeArrays=" + timeArrays +
                ", frameDruation=" + frameDruation +
                ", mShowRectF=" + mShowRectF +
                ", hint='" + hint + '\'' +
                ", mTextRectF=" + mTextRectF +
                ", mNinePitch=" + mNinePitch +
                ", filter='" + filter + '\'' +
                ", centerxy=" + Arrays.toString(centerxy) +
                '}';
    }

    /**
     * 获取config.json
     */
    public static String getConfigPath(String dir) {
        String name = CommonStyleUtils.CONFIG_JSON;
        File file = new File(dir, name);
        if (file.exists()) {
            try {
                return file.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            File[] fs = new File(dir).listFiles();
            if (null != fs) {
                int len = fs.length;
                for (int i = 0; i < len; i++) {
                    if (fs[i].isDirectory()) {
                        try {
                            return new File(fs[i], name).getCanonicalPath();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return null;
    }

}
