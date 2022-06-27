package com.pesdk.uisdk.util.helper;

import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.pesdk.uisdk.bean.model.FrameInfo;
import com.pesdk.uisdk.bean.model.StyleInfo;
import com.pesdk.uisdk.bean.model.TimeArray;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.base.lib.utils.LogUtil;
import com.vecore.base.net.JSONObjectEx;
import com.vecore.models.caption.CaptionObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class CommonStyleUtils {

    public static final String CONFIG_JSON = "config.json";

    private static String TAG = "CommonStyleUtils";

    private CommonStyleUtils() {

    }

    /**
     * 缩放文本提示
     */
    public static final int ZOOM_MIN_TEXT = 16;
    public static final int ZOOM_MAX_TEXT = 35;

    /**
     * 当前的播放器视频size
     */
    public static double previewWidth = 640.0, previewHeight = 640.0;

    /**
     * 字幕特效默认配置文件是按照640*360 大小设计的，所以为了适配任意的分辨率，需要转化为0~1.0f , 再根据目标宽高
     */
    private static final float PWIDTH = 640f;
    private static final float PHEIGHT = 640f;

    /**
     * 改变播放器大小 ，均需要重新初始化config
     */
    public static void init(double width, double height) {
        LogUtil.i(TAG, "init: " + width + "*" + height);
        previewWidth = width;
        previewHeight = height;
    }


    /**
     * 解析配置文件中的字幕、特效样式
     */
    public static void getConfig(File config, StyleInfo info) {
        String content = FileUtils.readTxtFile(PathUtils.getFilePath(config));
        getConfig(content, config, info);
    }


    public static void getConfig(String content, File config, StyleInfo info) {
        if (!TextUtils.isEmpty(content)) {
            try {
                JSONObjectEx json = new JSONObjectEx(content);
                if (json.optInt("ver", 0) >= 1) {
                    //211209 贴纸增加blend模式
                    info.blendMode = json.optString("blendMode", "");

                    //新的字幕json
                    //type==0 ,可以写文本 （0 可以写字(描边，字体参数有效)， 其他不可以）
                    info.type = json.optInt("type", 2);

                    //贴纸 支持apng  （190710）
                    info.isApng = json.optInt("apng", 0) == 1;
                    //默认状态 ，字幕特效在预览区域的位置的中心的坐标 与预览size的比例 单位:0~1.0f
                    info.centerxy[0] = (float) json.getDouble("centerX");
                    info.centerxy[1] = (float) json.getDouble("centerY");

                    //显示位置的宽占预览size的宽的比例
                    info.rectW = json.optDouble("rectW", StyleInfo.DEFAULT_RECT_W);

                    if (!info.isSetSizeW()) {
                        //默认时，字幕特效的显示宽高 单位:像素
                        info.w = info.srcWidth = json.getDouble("width");
                        info.h = info.srcHeight = json.getDouble("height");
                    }

                    //单列字幕 （190703）
                    info.vertical = json.optInt("vertical", 0) == 1;

                    // new change
                    info.lashen = false;
                    info.onlyone = false;
                    info.shadow = false;
                    if (json.has("shadow")) {
                        info.shadow = (json.getInt("shadow") == 1);
                    }
                    if (json.has("stretchability")) {
                        //是否支持可拉伸
                        info.lashen = (json.getInt("stretchability") == 1);
                        if (json.has("singleLine")) {
                            //是否单行显示
                            info.onlyone = (json.getInt("singleLine") == 1);
                        }
                        //字幕支持拉伸时，可拉伸区域(相当于字幕的文本框到各边的边框距)，距离字幕组件的边框的距离 单位像素
                        //可拉伸的区域距离四边框的距离
                        JSONArray jarr = json.optJSONArray("borderPadding");

                        Rect tmp = new Rect();
                        if (null != jarr && jarr.length() == 4) {
                            tmp.set(jarr.getInt(0), jarr.getInt(1), jarr.getInt(2), jarr.getInt(3));
                        }
                        //防止出现0  null
                        if (tmp.left == 0)
                            tmp.left = 1;
                        if (tmp.top == 0)
                            tmp.top = 1;
                        if (tmp.right == 0)
                            tmp.right = 1;
                        if (tmp.bottom == 0)
                            tmp.bottom = 1;

                        info.left = tmp.left;
                        info.top = tmp.top;
                        info.right = tmp.right;
                        info.buttom = tmp.bottom;

                        float fl = (float) ((0.0 + info.left) / info.w);
                        float fr = (float) ((0.0 + info.w - info.right) / info.w);
                        float ft = (float) ((0.0 + info.top) / info.h);
                        float fb = (float) ((0.0 + info.h - info.buttom) / info.h);

                        info.setNinePitch(new RectF(fl, ft, fr, fb));

                    }

                    if (info.type == 0) {

                        //info.tLeft ,tTop ,tRight,tBottom  文字区域距离字幕组件的上下左右边框的距离 (每一边的边框距均大于 （*****可拉伸区域的边框距 )) 单位:像素

                        JSONArray jarr = json.getJSONArray("textPadding");

                        info.tLeft = jarr.getInt(0);
                        info.tTop = jarr.getInt(1);
                        if (info.lashen) {
                            info.tRight = jarr.getInt(2);
                            info.tButtom = jarr.getInt(3);
                            info.tWidth = (int) (info.w - info.tLeft - info.tRight);
                            info.tHeight = (int) (info.h - info.tTop - info.tButtom);
                        } else {
                            //例如：字幕啪啪、密 ，此时info.tLeft ,info.tTop 代表字幕文字框的中心点，在字幕组件中的坐标  ，并非真实的left,top, 需-twidth/2 ,-theight/2
                            info.tWidth = json.getInt("textWidth");
                            info.tHeight = json.getInt("textHeight");
                        }

                        info.tFont = json.optString("textFont", "");
                        if (!TextUtils.isEmpty(info.tFont)) {
                            jarr = json.optJSONArray("strokeColor");
                            if (null != jarr && jarr.length() == 3) {
                                info.strokeColor = Color.rgb(jarr.getInt(0), jarr.getInt(1), jarr.getInt(2));
                            }
                            info.strokeWidth = json.optInt("strokeWidth", 0);

                        }
                    }
                    info.rotateAngle = (float) json.optInt("angle", 0);

                    info.code = json.optString("name");
                    //pid唯一标示 路径的hashcode值
                    String parent = config.getParent();
                    if (parent != null) {
                        info.pid = parent.hashCode();
                    }
                    //字幕可忽略
                    info.du = (int) (json.optDouble("duration", 1) * 1000);


                    if (info.type == 0) {

                        //-half 是针对(!lashen )模式时，需修正(左上角)顶点

                        RectF textRectF = new RectF();
                        if (info.lashen) {
                            float tleft = (float) ((info.tLeft + 0.0) / info.w);
                            float ttop = (float) ((info.tTop + 0.0) / info.h);
                            float tright = (float) ((info.w - info.tRight) / info.w);
                            float tbottom = (float) ((info.h - info.tButtom) / info.h);
                            textRectF.set(tleft, ttop, tright, tbottom);
                        } else {
                            int halfw = info.tWidth / 2;
                            int halfh = info.tHeight / 2;
                            //！lashen (（tleft，ttop)，就是文字框的中心点在字幕组件(info.w*info.h)中的坐标 )
                            float tleft = (float) ((info.tLeft - halfw + 0.0) / info.w);
                            float ttop = (float) ((info.tTop - halfh + 0.0) / info.h);
                            float tright = (float) ((info.tLeft + halfw + 0.0) / info.w);
                            float tbottom = (float) ((info.tTop + halfh + 0.0) / info.h);
                            textRectF.set(tleft, ttop, tright, tbottom);
                        }
                        //默认 文字、文字颜色、文字区域在背景图片中的位置 0~1.0f
                        String ptext = json.optString("textContent");
                        int textColor = Color.WHITE;
                        JSONArray jarr = json.optJSONArray("textColor");
                        if (null != jarr && jarr.length() == 3) {
                            textColor = Color.rgb(jarr.getInt(0), jarr.getInt(1), jarr.getInt(2));
                        }
                        info.initDefault(ptext, textColor, textRectF);
                    }


                    float fl = (float) (info.centerxy[0] - (info.w / 2 / previewWidth));
                    float ft = (float) (info.centerxy[1] - (info.h / 2 / previewHeight));

                    info.mShowRectF = new RectF(fl, ft, fl + (float) ((info.w + 0.0f) / previewWidth), ft + (float) ((info.h + 0.0f) / previewHeight));


                    //循环信息  先判断时间  （apng时，可以通过apng的duration 来修正）
                    info.timeArrays.clear();
                    JSONArray jarr = json.optJSONArray("timeArray");
                    if (null != jarr && jarr.length() > 0) {
                        int len = jarr.length();
                        JSONObject tmp = null;
                        for (int i = 0; i < len; i++) {
                            tmp = jarr.getJSONObject(i);
                            info.timeArrays.add(new TimeArray((int) (tmp.getDouble("beginTime") * 1000), (int) (tmp.getDouble("endTime") * 1000)));
                        }
                    } else {
                        //构建默认的参数
                        info.timeArrays.add(new TimeArray(0, 100));
                    }

                    info.frameArray.clear();
                    if (info.isApng) { //贴纸支持apng 190710
                        //检查下，apng 是否导出为png序列
                        if (null != config) {
                            StickerUtils.initApng(info.code, config.getParentFile(), info);
                        }
                    } else {
                        FrameInfo frameInfo = null;
                        //帧动画信息
                        jarr = json.optJSONArray("frameArray");
                        if (null != config) {
                            String tmpFilePath = config.getParent() + "/" + info.code;
                            if (null != jarr && jarr.length() > 0) {
                                int len = jarr.length();
                                JSONObject tmp = null;
                                for (int i = 0; i < len; i++) {
                                    tmp = jarr.getJSONObject(i);
                                    frameInfo = new FrameInfo();
                                    frameInfo.time = (int) (tmp.getDouble("time") * 1000);
                                    frameInfo.pic = tmpFilePath + tmp.getInt("pic") + ".webp";
                                    if (!com.vecore.base.lib.utils.FileUtils.isExist(frameInfo.pic)) {
                                        frameInfo.pic = tmpFilePath + tmp.getInt("pic") + ".png";
                                    }
                                    info.frameArray.put(frameInfo.time, frameInfo);
                                }
                            } else {
                                //构建默认的参数
                                frameInfo = new FrameInfo();
                                frameInfo.time = 0;
                                String tmp = tmpFilePath + "0.webp";
                                if (!com.vecore.base.lib.utils.FileUtils.isExist(tmp)) {
                                    tmp = tmpFilePath + "0.png";
                                }
                                if (!com.vecore.base.lib.utils.FileUtils.isExist(tmp)) {
                                    tmp = tmpFilePath + ".jpg"; //211209  增加blend  (仅一个jpg文件)
                                }
                                if (!com.vecore.base.lib.utils.FileUtils.isExist(tmp)) {
                                    tmp = tmpFilePath + ".png"; //211209
                                }
                                frameInfo.pic = tmp;
                                info.frameArray.put(frameInfo.time, frameInfo);
                            }
                        }
                    }
                    //读取size
                    StickerUtils.readSize(info);
                    info.fixFrameDruation();

                    if (info.isSetSizeW()) {
                        //设置了默认占比的，优先根据config.json
                        info.disf = (float) (previewWidth * info.rectW / info.w);
                    } else {
                        float scaleW = 1f;
                        if (info.frameArray.size() >= 1 && FileUtils.isExist(info.frameArray.valueAt(0).pic)) {

                            //中心点，在X轴上的距离左右最近的一边的距离
                            float minXMargin = Math.min((1 - info.centerxy[0]), info.centerxy[0]);

                            float maxOutXMargin = (float) (minXMargin * previewWidth) - 30;
                            float maxOutXMarginDesign = (minXMargin * PWIDTH);
                            scaleW = (((maxOutXMargin) / maxOutXMarginDesign));

                            //中心点在Y轴上距离上下最近的一边的距离
                            float minYMargin = Math.min((1 - info.centerxy[1]), info.centerxy[1]);
                            float maxOutYMargin = (float) (minYMargin * previewHeight) - 30;
                            float maxOutYMarginDesign = (minYMargin * PHEIGHT);
                            float defaultMaxScaleH = ((maxOutYMargin / maxOutYMarginDesign));

//                        Log.e(TAG, "getConfig: " + info.code + ">>" + scaleW + " >" + defaultMaxScaleH);
                            //至少保证，新增单个时，单个字幕默认大小完全在容器范围内(不要越界)
                            scaleW = Math.min(scaleW, defaultMaxScaleH);
                        }
                        //保证把手的右上角、右下角完全显示 (把手：84*84 有透明区域)
                        float minScaleH = (float) (50f / info.h);

                        float tmp = (float) Math.min(previewWidth / (info.w + 0.0f), previewHeight / (info.h + 0.0f)) * 0.75f;  //兼容: 保证大图(2000*4000)jpg ，首次完全展示在预览区域内的3/4
                        tmp = Math.min(Math.max(minScaleH, scaleW), tmp);

                        info.disf = Math.max(CaptionObject.MIN_SCALE, Math.min(CaptionObject.MAX_SCALE, tmp));
                        Log.e(TAG, "getConfig: " + info.code + " " + info.disf + "   " + info.w + " scaleW:" + scaleW + " scaleH:" + minScaleH + "    (" + previewWidth + " / " + previewHeight + ")");
                        if (CoreUtils.getMetrics().widthPixels < 720) {
                            info.disf = Math.min(info.disf, CaptionObject.MIN_SCALE);// 防止横屏字幕太大。
                        }
                    }

                    if (json.has("filter")) {
                        //马赛克或高斯 或“”
                        info.filter = json.getString("filter");
                        info.filterPng = config.getParent() + "/" + info.code + ".png";
                    }
                    info.isdownloaded = true;
                } else {
                    getConfig2(config, json, info);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 兼容旧版字幕、特效格式
     */
    @Deprecated
    private static void getConfig2(File config, JSONObject json, StyleInfo info) {
        try {
            //type==0 ,可以写文本 （0 可以写字(描边，字体参数有效)， 其他不可以）
            info.type = json.optInt("type", 2);

            //默认状态 ，字幕特效在预览区域的位置的中心的坐标 与预览size的比例 单位:0~1.0f
            info.centerxy[0] = (float) json.getDouble("x");
            info.centerxy[1] = (float) json.getDouble("y");
            //默认时，字幕特效的显示宽高 单位:像素
            info.w = json.getDouble("w");
            info.h = json.getDouble("h");

            // new change
            info.lashen = false;
            info.onlyone = false;
            info.shadow = false;
            if (json.has("shadow")) {
                info.shadow = (json.getInt("shadow") == 1);
            }
            if (json.has("lashen")) {
                info.lashen = (json.getInt("lashen") == 1);
                if (json.has("onlyone")) {
                    info.onlyone = (json.getInt("onlyone") == 1);
                }
                //字幕支持拉伸时，可拉伸区域(相当于字幕的文本框到各边的边框距)，距离字幕组件的边框的距离 单位像素
                //  like： "left": 2.0,"top": 2.0,"right": 2.0,"buttom": 2.0,
                info.left = json.getDouble("left");
                if (info.left == 0)
                    info.left = 1.0;
                info.top = json.getDouble("top");
                if (info.top == 0)
                    info.top = 1.0;
                info.right = json.getDouble("right");
                if (info.right == 0)
                    info.right = 1.0;
                info.buttom = json.getDouble("buttom");
                if (info.buttom == 0)
                    info.buttom = 1.0;

                float fl = (float) ((0.0 + info.left) / info.w);
                float fr = (float) ((0.0 + info.w - info.right) / info.w);
                float ft = (float) ((0.0 + info.top) / info.h);
                float fb = (float) ((0.0 + info.h - info.buttom) / info.h);

                info.setNinePitch(new RectF(fl, ft, fr, fb));

            } else {

            }

            if (info.type == 0) {

                //info.tLeft ,tTop ,tRight,tBottom  文字区域距离字幕组件的上下左右边框的距离 (每一边的边框距均大于 （*****可拉伸区域的边框距 )) 单位:像素

                info.tLeft = json.getInt("tLeft");
                info.tTop = json.getInt("tTop");
                if (info.lashen) {
                    info.tRight = json.getInt("tRight");
                    info.tButtom = json.getInt("tButtom");
                    info.tWidth = (int) (info.w - info.tLeft - info.tRight);
                    info.tHeight = (int) (info.h - info.tTop - info.tButtom);
                } else {
                    //例如：字幕啪啪、密 ，此时info.tLeft ,info.tTop 代表字幕文字框的中心点，在字幕组件中的坐标  ，并非真实的left,top, 需-twidth/2 ,-theight/2
                    info.tWidth = json.getInt("tWidth");
                    info.tHeight = json.getInt("tHeight");
                }

                info.tFont = json.optString("tFont", "");
                if (!TextUtils.isEmpty(info.tFont)) {
                    if (json.has("strokeR")) {
                        info.strokeColor = Color.rgb(json.getInt("strokeR"), json.getInt("strokeG"), json.getInt("strokeB"));
                    }
                    info.strokeWidth = json.optInt("strokeWidth", 0);

                }
            }
            info.rotateAngle = (float) json.optDouble("tAngle", 0.0);


            info.code = json.optString("n");
            info.pid = info.code.hashCode();
            //字幕可忽略
            info.du = (int) (json.optDouble("du", 1) * 1000);


            if (info.type == 0) {

                //-half 是针对(!lashen )模式时，需修正(左上角)顶点

                RectF textRectF = new RectF();
                if (info.lashen) {
                    float tleft = (float) ((info.tLeft + 0.0) / info.w);
                    float ttop = (float) ((info.tTop + 0.0) / info.h);
                    float tright = (float) ((info.w - info.tRight) / info.w);
                    float tbottom = (float) ((info.h - info.tButtom) / info.h);
                    textRectF.set(tleft, ttop, tright, tbottom);
                } else {
                    int halfw = info.tWidth / 2;
                    int halfh = info.tHeight / 2;
                    //！lashen (（tleft，ttop)，就是文字框的中心点在字幕组件(info.w*info.h)中的坐标 )
                    float tleft = (float) ((info.tLeft - halfw + 0.0) / info.w);
                    float ttop = (float) ((info.tTop - halfh + 0.0) / info.h);
                    float tright = (float) ((info.tLeft + halfw + 0.0) / info.w);
                    float tbottom = (float) ((info.tTop + halfh + 0.0) / info.h);
                    textRectF.set(tleft, ttop, tright, tbottom);
                }
                //默认 文字、文字颜色、文字区域在背景图片中的位置 0~1.0f
                String ptext = json.optString("pText");
                int textColor = Color.rgb(json.getInt("tR"), json.getInt("tG"), json.getInt("tB"));
                info.initDefault(ptext, textColor, textRectF);
            }


            info.mShowRectF = new RectF();

            float fl = (float) (info.centerxy[0] - (info.w / 2 / previewWidth));
            float ft = (float) (info.centerxy[1] - (info.h / 2 / previewHeight));

            info.mShowRectF.set(fl, ft, fl + (float) ((info.w + 0.0f) / previewWidth), ft + (float) ((info.h + 0.0f) / previewHeight));


            JSONArray jarr = json.getJSONArray("frameArry");
            info.frameArray.clear();
            String tpictemp = config.getParent() + "/" + info.code;
            int len = jarr.length();
            JSONObject jtemp = null;
            FrameInfo tempT = null;
            for (int i = 0; i < len; i++) {
                jtemp = jarr.getJSONObject(i);
                tempT = new FrameInfo();
                int ntime = (int) (jtemp.getDouble("time") * 1000);
                tempT.time = ntime;
                String path = tpictemp + jtemp.getInt("pic") + ".png";
                if (new File(path).exists()) {
                    tempT.pic = path;
                } else {
                    tempT.pic = tpictemp + jtemp.getInt("pic") + ".webp";
                }
                info.frameArray.put(ntime, tempT);
            }

            info.fixFrameDruation();


            info.timeArrays.clear();
            jarr = json.getJSONArray("timeArry");
            len = jarr.length();
            for (int i = 0; i < len; i++) {
                jtemp = jarr.getJSONObject(i);
                info.timeArrays.add(new TimeArray((int) (jtemp.getDouble("beginTime") * 1000), (int) (jtemp.getDouble("endTime") * 1000)));

            }


            Options op = new Options();
            op.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(info.frameArray.valueAt(0).pic, op);


            info.disf = (float) (info.w / (op.outWidth + 0.0f) * ((previewWidth + .0) / PWIDTH));
            if (info.disf < 1.2f) {
                //比例太低,目标缩放比设置为22 （默认如果此大小，有的字幕会超出容器范围 (中心点太靠边了)）
                float mTmp = ((22 - CommonStyleUtils.ZOOM_MIN_TEXT) / (CommonStyleUtils.ZOOM_MAX_TEXT - CommonStyleUtils.ZOOM_MIN_TEXT
                        + 0.0f) * (CaptionObject.MAX_SCALE - CaptionObject.MIN_SCALE))
                        + CaptionObject.MIN_SCALE;

                //中心点到预览区域的距离 单位：px 28px +2(字幕把手一半的宽度56x56)
                float manOffWidthPx = (float) (Math.min((1 - info.centerxy[0]), info.centerxy[0]) * previewWidth) - 30;
                //这个距离最大可以放大为%f倍 (防止超界)
                float tmp = (float) (manOffWidthPx / (info.w / 2.0f));
//
//                    Log.e(TAG, "getConfig: " + info.disf + "  " + manOffWidthPx + ">>" + info.w + "   " + collageMediaList + " mTmp:  " + mTmp + Arrays.toString(info.centerxy) + info.lashen + "  " +
//                            "" + info.onlyone + " " + info.mShowRectF);
                //+0.2f 防止有点lashen，默认比例太小

                if (info.isSub()) {
                    info.disf = Math.max(info.disf + 0.2f, Math.min(tmp, mTmp));
                } else {

                }
            }
//                Log.e(TAG, "getConfig: " + info.code + " " + info.disf + "   " + info.w + "/" + op.outWidth + "  * (" + previewWidth + " / " + PWIDTH + ")");
            if (CoreUtils.getMetrics().widthPixels < 720) {
                info.disf = Math.min(info.disf, 0.8f);// 防止横屏字幕太大。
            }

            if (json.has("filter")) {
                //马赛克或高斯 或“”
                info.filter = json.getString("filter");
                info.filterPng = config.getParent() + "/" + info.code + ".png";
            }

            info.isdownloaded = true;
//                Log.e(TAG, "getConfig: " + info);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析json
     */
    public static boolean checkStyle(File styleDir, StyleInfo info) {
        if (!styleDir.isDirectory() && styleDir.exists())
            return false;
        info.code = styleDir.getName();
        info.pid = info.code.hashCode();

        File fconfig = new File(PathUtils.getFilePath(styleDir), CONFIG_JSON);
        if (fconfig.exists() && fconfig.length() > 0) {
            getConfig(fconfig, info);
            return true;
        }
        return false;
    }

    /**
     * 字幕还是贴纸
     */
    public enum STYPE {
        sub, special;
    }

    /**
     * 根据newNum 找出最接近的Frame对应的StyleT
     *
     * @param nearNum      进度单位ims
     * @param itemDuration 特效单帧的duration， 单位：ms
     * @param isEdit       当前Item 是否正在编辑  （可以拖动 rect位置）
     * @param spDuration   当前特效的duration 单位：ms
     */
    public static FrameInfo search(int nearNum, int itemDuration, SparseArray<FrameInfo> arrayList, ArrayList<TimeArray> timeArrays, boolean isEdit, int spDuration) {
        FrameInfo item, resultP = null;
        int len = 0;
        if (null != arrayList && (len = arrayList.size()) >= 0) {
            if (len == 1) {
                //只有一张背景图
                resultP = arrayList.valueAt(0);
            } else {
                try {
                    int timeArraySize = timeArrays.size();
                    if (timeArraySize == 3) {

                        int headDuration = timeArrays.get(0).getDuration();


                        if (nearNum <= headDuration) {
                            // 差值实始化
                            int diffNum = Math.abs(arrayList.valueAt(0).time - nearNum);
                            int headCount = headDuration / itemDuration;
                            for (int i = 0; i < headCount; i++) {
                                item = arrayList.valueAt(i);
                                int diffNumTemp = Math.abs(item.time - nearNum);
                                if (diffNumTemp <= diffNum) {
                                    diffNum = diffNumTemp;
                                    resultP = item;
                                }
                            }
                        } else {
                            if (isEdit) {
                                TimeArray loopArray = timeArrays.get(1);
                                int loopdu = loopArray.getDuration();
                                int nd = ((nearNum - headDuration) % loopdu) / itemDuration;
                                resultP = arrayList.valueAt((loopArray.getBegin() / itemDuration) + nd);
                            } else {
                                //如果是滑动进度条 获取当前位置的特效图，（已经知道 开始、结束  ）

                                TimeArray lastArray = timeArrays.get(2);
                                if (nearNum < (spDuration - lastArray.getDuration())) {
                                    //循环中间部分
                                    TimeArray loopArray = timeArrays.get(1);
                                    int bodyItemDuration = loopArray.getDuration();
                                    int nd = ((nearNum - headDuration) % bodyItemDuration) / itemDuration;
                                    resultP = arrayList.valueAt((loopArray.getBegin() / itemDuration) + nd);
                                } else {
                                    //从末尾Array中找出要绘制的图片
                                    if (nearNum <= spDuration) {
                                        //距离结束还有多少毫秒
                                        int offd = spDuration - nearNum;
                                        //是倒数第几帧
                                        int lastf = offd / itemDuration;
                                        int t = (lastArray.getEnd() / itemDuration) - 1 - lastf;
                                        int tdst = Math.min(len - 1, Math.max(0, t));
                                        resultP = arrayList.valueAt(tdst);
                                    } else {
                                        resultP = arrayList.valueAt(len - 1);
                                    }
                                }
                            }
                        }


                    } else {


                        if (timeArraySize == 2) {
                            int headDuration = timeArrays.get(0).getDuration();

                            if (nearNum <= headDuration) {
                                int headCount = headDuration / itemDuration;
                                // 差值实始化
                                int diffNum = Math.abs(arrayList.valueAt(0).time - nearNum);
                                for (int i = 0; i < headCount; i++) {
                                    item = arrayList.valueAt(i);
                                    if (null != item) {
                                        int diffNumTemp = Math.abs(item.time - nearNum);
                                        if (diffNumTemp <= diffNum) {
                                            diffNum = diffNumTemp;
                                            resultP = item;
                                        }
                                    } else {
                                        resultP = arrayList.valueAt(0);
                                    }
                                }
                            } else {
                                if (isEdit) {
                                    TimeArray loopArray = timeArrays.get(1);
                                    int loopdu = loopArray.getDuration();
                                    int nd = ((nearNum - headDuration) % loopdu) / itemDuration;
                                    int index = (loopArray.getBegin() / itemDuration) + nd; //从循环部分的begin时刻算起
                                    int tIndex = Math.max(0, Math.min(index, (arrayList.size() - 1)));
                                    resultP = arrayList.valueAt(tIndex);
                                } else {
                                    //如果是滑动进度条 获取当前位置的特效图，（已经知道 开始、结束  ）
                                    TimeArray loopArray = timeArrays.get(1);
                                    if (nearNum < spDuration) {
                                        //循环中间部分
                                        int loopdu = loopArray.getDuration();
                                        int nd = ((nearNum - headDuration) % loopdu) / itemDuration;
                                        int index = (loopArray.getBegin() / itemDuration) + nd;
                                        int tIndex = Math.max(0, Math.min(index, (arrayList.size() - 1)));
                                        resultP = arrayList.valueAt(tIndex);
                                    }
                                }
                            }


                        } else {

                            // 差值实始化
                            int diffNum = Math.abs(arrayList.valueAt(0).time - nearNum);
                            //只有（header 和last 、header)
                            for (int i = 0; i < len; i++) {
                                item = arrayList.valueAt(i);
                                int diffNumTemp = Math.abs(item.time - nearNum);
                                if (diffNumTemp <= diffNum) {
                                    diffNum = diffNumTemp;
                                    resultP = item;
                                }
                            }
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    resultP = arrayList.valueAt(0);
                }
            }
        } else {
            Log.e(TAG, "search: frameArray size <=0");
        }
        return resultP;

    }

}
