package com.pesdk.album.uisdk.widget.photoview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;

import androidx.appcompat.widget.AppCompatImageView;


public class PhotoView extends AppCompatImageView {

    private PhotoViewAttach attach;

    private ScaleType pendingScaleType;

    public PhotoView(Context context) {
        this(context, null);
    }

    public PhotoView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public PhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init();
    }

    private void init() {
        attach = new PhotoViewAttach(this);
        super.setScaleType(ScaleType.MATRIX);
        if (pendingScaleType != null) {
            setScaleType(pendingScaleType);
            pendingScaleType = null;
        }
    }

    public PhotoViewAttach getAttach() {
        return attach;
    }

    @Override
    public ScaleType getScaleType() {
        return attach.getScaleType();
    }

    @Override
    public Matrix getImageMatrix() {
        return attach.getImageMatrix();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        attach.setOnLongClickListener(l);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        attach.setOnClickListener(l);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (attach == null) {
            pendingScaleType = scaleType;
        } else {
            attach.setScaleType(scaleType);
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        // setImageBitmap calls through to this method
        if (attach != null) {
            attach.update();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (attach != null) {
            attach.update();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (attach != null) {
            attach.update();
        }
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (changed) {
            attach.update();
        }
        return changed;
    }

    public void setRotationTo(float rotationDegree) {
        attach.setRotationTo(rotationDegree);
    }

    public void setRotationBy(float rotationDegree) {
        attach.setRotationBy(rotationDegree);
    }

    public boolean isZoomable() {
        return attach.isZoomable();
    }

    public void setZoomable(boolean zoomable) {
        attach.setZoomable(zoomable);
    }

    public RectF getDisplayRect() {
        return attach.getDisplayRect();
    }

    public void getDisplayMatrix(Matrix matrix) {
        attach.getDisplayMatrix(matrix);
    }

    public boolean setDisplayMatrix(Matrix finalRectangle) {
        return attach.setDisplayMatrix(finalRectangle);
    }

    public void getSuppMatrix(Matrix matrix) {
        attach.getSuppMatrix(matrix);
    }

    public boolean setSuppMatrix(Matrix matrix) {
        return attach.setDisplayMatrix(matrix);
    }

    public float getMinimumScale() {
        return attach.getMinimumScale();
    }

    public float getMediumScale() {
        return attach.getMediumScale();
    }

    public float getMaximumScale() {
        return attach.getMaximumScale();
    }

    public float getScale() {
        return attach.getScale();
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        attach.setAllowParentInterceptOnEdge(allow);
    }

    public void setMinimumScale(float minimumScale) {
        attach.setMinimumScale(minimumScale);
    }

    public void setMediumScale(float mediumScale) {
        attach.setMediumScale(mediumScale);
    }

    public void setMaximumScale(float maximumScale) {
        attach.setMaximumScale(maximumScale);
    }

    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        attach.setScaleLevels(minimumScale, mediumScale, maximumScale);
    }

    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        attach.setOnMatrixChangeListener(listener);
    }

    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        attach.setOnPhotoTapListener(listener);
    }

    public void setOnOutsidePhotoTapListener(OnOutsidePhotoTapListener listener) {
        attach.setOnOutsidePhotoTapListener(listener);
    }

    public void setOnViewTapListener(OnViewTapListener listener) {
        attach.setOnViewTapListener(listener);
    }

    public void setOnViewDragListener(OnViewDragListener listener) {
        attach.setOnViewDragListener(listener);
    }

    public void setScale(float scale) {
        attach.setScale(scale);
    }

    public void setScale(float scale, boolean animate) {
        attach.setScale(scale, animate);
    }

    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        attach.setScale(scale, focalX, focalY, animate);
    }

    public void setZoomTransitionDuration(int milliseconds) {
        attach.setZoomTransitionDuration(milliseconds);
    }

    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener onDoubleTapListener) {
        attach.setOnDoubleTapListener(onDoubleTapListener);
    }

    public void setOnScaleChangeListener(OnScaleChangedListener onScaleChangedListener) {
        attach.setOnScaleChangeListener(onScaleChangedListener);
    }

    public void setOnSingleFlingListener(OnSingleFlingListener onSingleFlingListener) {
        attach.setOnSingleFlingListener(onSingleFlingListener);
    }
}
