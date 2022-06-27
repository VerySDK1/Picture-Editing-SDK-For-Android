package com.pesdk.uisdk.fragment.main;

import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.listener.ImageHandlerListener;

import java.util.List;

/**
 * 通过Id查找下标
 */
public class IndexHelper {

    private ImageHandlerListener mVideoHandlerListener;

    public IndexHelper(ImageHandlerListener videoHandlerListener) {
        mVideoHandlerListener = videoHandlerListener;
    }

    public int getWordIndex(int id) {
        int index = -1;
        List<WordInfoExt> list = mVideoHandlerListener.getParamHandler().getParam().getWordList();
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (list.get(i).getId() == id) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int getStickerIndex(int id) {
        int index = -1;
        List<StickerInfo> list = mVideoHandlerListener.getParamHandler().getParam().getStickerList();
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (list.get(i).getId() == id) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int getOverLayIndex(int id) {
        int index = -1;
        List<CollageInfo> list = mVideoHandlerListener.getParamHandler().getParam().getOverLayList();
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (list.get(i).getId() == id) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int getPipIndex(int id) {
        int index = -1;
        List<CollageInfo> list = mVideoHandlerListener.getParamHandler().getParam().getCollageList();
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (list.get(i).getId() == id) {
                index = i;
                break;
            }
        }
        return index;
    }

    public WordInfoExt getWord(int id) {
        int index = getWordIndex(id);
        if (index >= 0) {
            return mVideoHandlerListener.getParamHandler().getParam().getWordList().get(index);
        } else {
            return null;
        }
    }

    public StickerInfo getSticker(int id) {
        int index = getStickerIndex(id);
        if (index >= 0) {
            return mVideoHandlerListener.getParamHandler().getParam().getStickerList().get(index);
        } else {
            return null;
        }
    }
}
