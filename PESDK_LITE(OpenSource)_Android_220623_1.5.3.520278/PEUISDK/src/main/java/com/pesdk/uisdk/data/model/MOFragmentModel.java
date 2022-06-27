package com.pesdk.uisdk.data.model;

import android.content.Context;
import android.graphics.RectF;

import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.models.DewatermarkObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * 马赛克|去水印的样式数据
 *
 * @create 2019/3/26
 */
public class MOFragmentModel {
    private int previewHeight, previewWidth;

    private MOFragmentModel() {
    }

    public MOFragmentModel(int previewWidth, int previewHeight) {
        this.previewWidth = previewWidth;
        this.previewHeight = previewHeight;
    }

    /**
     * 获取样式列表
     */
    public List<MOModel> getData() {
        List<MOModel> list = new ArrayList();
        float pw = 360.0f;
        float ph = 360.0f;
        float fl = 0.5f - (pw / 2 / previewWidth);
        float ft = 0.5f - (ph / 2 / previewHeight);
        RectF rectF = new RectF(fl, ft, fl + (pw / previewWidth), ft + (ph / previewHeight));
        //1.高斯模糊
        list.add(new MOModel(new RectF(rectF), DewatermarkObject.Type.blur));
        //2.马赛克|像素化
        list.add(new MOModel(new RectF(rectF), DewatermarkObject.Type.mosaic));
        //3.去水印
        list.add(new MOModel(rectF, DewatermarkObject.Type.watermark));
        return list;
    }

    public List<MOModel> getOSDData(Context context) {
        List<MOModel> list = new ArrayList();
        float pw = CoreUtils.dip2px(context, 100); //默认大小
        float ph = CoreUtils.dip2px(context, 55);
        float fl = 0.5f - (pw / 2 / previewWidth);
        float ft = 0.5f - (ph / 2 / previewHeight);
        RectF rectF = new RectF(fl, ft, fl + (pw / previewWidth), ft + (ph / previewHeight));
        //1.高斯模糊
        list.add(new MOModel(new RectF(rectF), DewatermarkObject.Type.blur));
        //2.马赛克|像素化
        list.add(new MOModel(new RectF(rectF), DewatermarkObject.Type.mosaic));
        //3.去水印
        list.add(new MOModel(rectF, DewatermarkObject.Type.watermark));
        return list;
    }

    public MOModel getWatermark(Context context) {
        List<MOModel> list = new ArrayList();
        float pw = CoreUtils.dip2px(context, 100); //默认大小
        float ph = CoreUtils.dip2px(context, 55);
        float fl = 0.5f - (pw / 2 / previewWidth);
        float ft = 0.5f - (ph / 2 / previewHeight);
        RectF rectF = new RectF(fl, ft, fl + (pw / previewWidth), ft + (ph / previewHeight));
        //3.去水印
        return new MOModel(rectF, DewatermarkObject.Type.watermark);
    }

    /**
     * 去水印模板
     *
     * @create 2020/1/9
     */
    public static class MOModel {


        private RectF mRectF;
        private final DewatermarkObject.Type mType;

        public MOModel(RectF rectF, @NonNull DewatermarkObject.Type type) {
            mRectF = rectF;
            mType = type;
        }

        public RectF getRectF() {
            return mRectF;
        }

        public DewatermarkObject.Type getType() {
            return mType;
        }

    }

}
