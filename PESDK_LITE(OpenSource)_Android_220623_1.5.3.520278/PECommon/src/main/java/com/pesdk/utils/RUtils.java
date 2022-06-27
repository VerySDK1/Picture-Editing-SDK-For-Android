package com.pesdk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.vecore.utils.UriUtils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;

/**
 * 适配部分Uri 使用方式
 */
public class RUtils {


    /***
     * android 适配29 + 打开bitmap
     * @param uriString
     */
    public static FileDescriptor getFileDescriptor(Context context, String uriString) {
        try {
            if (UriUtils.isUri(uriString)) { //uri方式
                return context.getContentResolver().openFileDescriptor(Uri.parse(uriString), "r").getFileDescriptor();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param pathOUri 路径|Uri
     */
    public static MediaMetadataRetriever getAvailableMetadataRetriever(Context context, String pathOUri) {
        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();
            FileDescriptor fileDescriptor = getFileDescriptor(context, pathOUri);
            if (null != fileDescriptor) {
                retriever.setDataSource(fileDescriptor);
            } else {
                retriever.setDataSource(pathOUri);
            }
        } catch (RuntimeException e) {
        }
        return retriever;
    }

    /**
     * 获取文件的 bitmap
     *
     * @param context
     * @param pathOUri
     * @param op
     * @return
     */
    public static Bitmap decodeFile(Context context, String pathOUri, BitmapFactory.Options op) {
        FileDescriptor fileDescriptor = getFileDescriptor(context, pathOUri);
        Bitmap bmp = null;
        if (null != fileDescriptor) {
            bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, op);
        } else {
            bmp = BitmapFactory.decodeFile(pathOUri, op);
        }
        return bmp;
    }
}
