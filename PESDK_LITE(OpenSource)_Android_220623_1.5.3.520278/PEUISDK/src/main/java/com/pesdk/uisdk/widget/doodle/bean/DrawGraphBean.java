package com.pesdk.uisdk.widget.doodle.bean;

import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.pesdk.uisdk.widget.doodle.DoodleView;

import java.util.ArrayList;
import java.util.List;


/**
 * 图形:矩形图|三角形箭头
 */
public class DrawGraphBean {
    // 这四个点是实时变化的，用来绘制图形的四个点
    public float startX, startY, endX, endY;
    public DoodleView.GRAPH_TYPE type;
    public Paint paint;
    public PointF clickPoint = new PointF();
    // 两个点的变量，用于平移缩放的操作，只有在UP后，才会同步四个点的值
    public PointF startPoint = new PointF();
    public PointF endPoint = new PointF();
    // 是否是符合要求的图形
    public boolean isPass = false;
    // 用于撤销移动缩放的操作
    public List<RectF> rectFList = new ArrayList<>();
    public float nTriangleWidth = 10;
    public int mAlpha;

    public DrawGraphBean(float startX, float startY, float endx, float endY, DoodleView.GRAPH_TYPE type, Paint paint, float triangleWidth, int alpha) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endx;
        this.endY = endY;
        this.type = type;
        this.paint = paint;
        this.startPoint.x = startX;
        this.startPoint.y = startY;
        this.endPoint.x = endx;
        this.endPoint.y = endY;
        rectFList.add(new RectF(startX, startY, endx, endY));
        nTriangleWidth = triangleWidth;
        this.mAlpha = alpha;
    }

}
