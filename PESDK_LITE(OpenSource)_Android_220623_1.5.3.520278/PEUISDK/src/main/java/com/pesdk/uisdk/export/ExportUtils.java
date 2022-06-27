package com.pesdk.uisdk.export;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * 写入相册
 */
class ExportUtils {
    private static final int video = 0;
    private static final int image = 1;
    private static final int gif = 2;
    private static final int audio = 3;

    public static ContentValues createContentValue(int width, int height, String artist, String displayName) {
        return createContentValue(width, height, image, artist, displayName, 0);
    }

    private static ContentValues createContentValue(int width, int height, int mediaType, String artist, String displayName, int duration) {
        ContentValues videoValues = new ContentValues();
        videoValues.put(MediaStore.MediaColumns.TITLE, artist);
        String type = "image/jpeg";
        if (mediaType == video) {
            type = "video/mp4";
            videoValues.put(MediaStore.Video.Media.ARTIST, artist);
            videoValues.put(MediaStore.Video.Media.DESCRIPTION, artist);
        } else if (mediaType == gif) {
            type = "image/gif";
        } else if (mediaType == audio) {
            type = "audio/mpeg";
        } else if (mediaType == image) {
//            type = "image/jpeg";
            String extension = MimeTypeMap.getFileExtensionFromUrl(displayName);
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        videoValues.put(MediaStore.MediaColumns.MIME_TYPE, type);
        videoValues.put(MediaStore.MediaColumns.DATE_TAKEN, String.valueOf(System.currentTimeMillis()));
        videoValues.put(MediaStore.MediaColumns.WIDTH, width);
        videoValues.put(MediaStore.MediaColumns.HEIGHT, height);
        videoValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        if (duration > 0) {
            videoValues.put(MediaStore.MediaColumns.DURATION, duration);
        }
        return videoValues;
    }


    /**
     * 保存到系统图库 （仅支持29 以下的设备）
     *
     * @param path
     * @param width
     * @param height
     * @param artist
     */
    public static void insertToAlbumP(Context context, String path, int width, int height, String artist) {
        insertToAlbumP(context, path, width, height, image, artist, 0);
    }

    private static void insertToAlbumP(Context context, String path, int width, int height, int mediaType, String artist, int duation) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ContentValues contentValues = createContentValue(width, height, mediaType, artist, new File(path).getName(), duation);
            contentValues.put(MediaStore.MediaColumns.DATA, path);
            Uri uri = null;
            if (mediaType == video) {
                uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            } else if (mediaType == audio) {
                uri = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
            } else if (mediaType == image | mediaType == gif) {
                uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            }
            if (uri != null) {
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            }
        }
    }

}
