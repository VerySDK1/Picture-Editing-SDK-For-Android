package com.pesdk.album.uisdk.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;

import com.pesdk.album.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

/**
 * 文字板， 输入字导出图片
 */
public class ExtEditPicView extends AppCompatEditText {
    private int mBgColor;
    private int mTextColor;
    private int mTextSide;
    private String mTtfPath = "";
    private final ArrayList<String> mStrList = new ArrayList<>();

    public ExtEditPicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBgColor = Color.BLACK;
        mTextColor = ContextCompat.getColor(context, R.color.white);
    }

    @Override
    public void setTextColor(int color) {
        mTextColor = color;
        super.setTextColor(mTextColor);

    }

    public void add(ArrayList<String> list) {
        mStrList.clear();
        mStrList.addAll(list);
        invalidate();
    }

    public String getTTF() {
        return mTtfPath;
    }

    public void setTTF(String ttf) {
        this.mTtfPath = ttf;
    }

    public void setBgColor(int bgColor) {
        mBgColor = bgColor;
        super.setBackgroundColor(mBgColor);
    }

    public int getBgColor() {
        return mBgColor;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextSide(int side) {
        mTextSide = side;
    }

    public int getTextSide() {
        return mTextSide;
    }

    /**
     * 生成图片文件
     */
    public int[] save(String path) {
        int[] wh = new int[2];
        setDrawingCacheEnabled(true);
        Bitmap bmp = getDrawingCache();
        File file = new File(path);
        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        wh[0] = bmp.getWidth();
        wh[1] = bmp.getHeight();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setDrawingCacheEnabled(false);
        bmp.recycle();
        return wh;
    }

}
