package com.pesdk.uisdk.data.model;

import android.graphics.Rect;

import com.vecore.models.FlipType;
import com.vecore.models.PEImageObject;

/**
 *
 */
public class EditModel {



    private boolean isLand(PEImageObject mediaObject) {
        int angle = mediaObject.getShowAngle() % 360;
        return Math.abs(angle - 90) <= 45 || Math.abs(angle - 270) <= 45; //旋转为  (约= 90 |270 )
    }

    /**
     * 应用镜像
     *
     * @param isHorizontal true 左右镜像 ;false 上下镜像
     * @param mediaObject
     */
    public void applyMirror(boolean isHorizontal, PEImageObject mediaObject) {
        if (isLand(mediaObject)) { //修正旋转之后， 左右镜像更改为上下镜像
            isHorizontal = !isHorizontal;
        }
        FlipType tmp = mediaObject.getFlipType();
        if (isHorizontal) {       //左右
            Rect mRectVideoClipBound = mediaObject.getClipRect();
            if (FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL == tmp) {
                mediaObject.setFlipType(FlipType.FLIP_TYPE_VERTICAL);
                if (isLand(mediaObject)) {
                    mRectVideoClipBound.set(mediaObject.getWidth() - mRectVideoClipBound.right, mRectVideoClipBound.top,
                            mediaObject.getWidth() - mRectVideoClipBound.left, mRectVideoClipBound.bottom);
                }
            } else if (FlipType.FLIP_TYPE_HORIZONTAL == tmp) {
                mediaObject.setFlipType(FlipType.FLIP_TYPE_NONE);
                mRectVideoClipBound.set(mediaObject.getWidth() - mRectVideoClipBound.right, mRectVideoClipBound.top,
                        mediaObject.getWidth() - mRectVideoClipBound.left, mRectVideoClipBound.bottom);
            } else if (FlipType.FLIP_TYPE_VERTICAL == tmp) {
                mediaObject.setFlipType(FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL);
                if (isLand(mediaObject)) {
                    mRectVideoClipBound.set(mediaObject.getWidth() - mRectVideoClipBound.right, mRectVideoClipBound.top,
                            mediaObject.getWidth() - mRectVideoClipBound.left, mRectVideoClipBound.bottom);
                }
            } else {
                mediaObject.setFlipType(FlipType.FLIP_TYPE_HORIZONTAL);
                mRectVideoClipBound.set(mediaObject.getWidth() - mRectVideoClipBound.right, mRectVideoClipBound.top,
                        mediaObject.getWidth() - mRectVideoClipBound.left, mRectVideoClipBound.bottom);
            }
        } else { //上下
            Rect clipRect = mediaObject.getClipRect();
            if (FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL == tmp) {
                mediaObject.setFlipType(FlipType.FLIP_TYPE_HORIZONTAL);
            } else if (FlipType.FLIP_TYPE_VERTICAL == tmp) {
                mediaObject.setFlipType(FlipType.FLIP_TYPE_NONE);
                clipRect.set(clipRect.left, mediaObject.getHeight() - clipRect.bottom, clipRect.right, mediaObject.getHeight() - clipRect.top);
            } else if (FlipType.FLIP_TYPE_HORIZONTAL == tmp) {
                mediaObject.setFlipType(FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL);
            } else {
                mediaObject.setFlipType(FlipType.FLIP_TYPE_VERTICAL);
                clipRect.set(clipRect.left, mediaObject.getHeight() - clipRect.bottom, clipRect.right, mediaObject.getHeight() - clipRect.top);
            }
        }
        mediaObject.refresh();
    }

}
