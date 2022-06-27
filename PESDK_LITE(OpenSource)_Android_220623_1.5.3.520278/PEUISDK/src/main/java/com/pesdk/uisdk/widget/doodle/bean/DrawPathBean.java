package com.pesdk.uisdk.widget.doodle.bean;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * 涂鸦
 */
public class DrawPathBean {
    public Path path;
    public Paint paint;
    public IPaint mIPaint;
    public Mode mode;

    public DrawPathBean(Path path, Paint paint, IPaint iPaint, Mode mode) {
        this.paint = paint;
        this.path = path;
        this.mode = mode;
        this.mIPaint = iPaint;
    }
}
