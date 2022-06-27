package com.pesdk.uisdk.bean;

/**
 *下载进度
 */
public class LineProgress {
    private int position, progress;

    public int getPosition() {
        return position;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public LineProgress(int position, int progress) {
        this.position = position;
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "LineProgress{" +
                "position=" + position +
                ", progress=" + progress +
                '}';
    }
}
