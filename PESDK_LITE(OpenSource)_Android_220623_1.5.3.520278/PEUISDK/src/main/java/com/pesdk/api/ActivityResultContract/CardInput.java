package com.pesdk.api.ActivityResultContract;

import com.vecore.BaseVirtual;

/**
 *
 */
public class CardInput {
    private String path;
    private BaseVirtual.Size mSize; //输出size

    public CardInput(String path, BaseVirtual.Size size) {
        this.path = path;
        mSize = size;
    }

    public String getPath() {
        return path;
    }

    public BaseVirtual.Size getSize() {
        return mSize;
    }
}
