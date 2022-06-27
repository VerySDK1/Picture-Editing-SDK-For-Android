package com.pesdk.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

/**
 */
public class Utils {

    /**
     * 查找容器范围内的组件
     */
    public static <T extends View> T $(View mRoot, int resId) {
        return mRoot.findViewById(resId);
    }

    public static void showAutoHideDialog(Context context, String strMessage) {
        Toast.makeText(context,strMessage,Toast.LENGTH_SHORT).show();
    }
}
