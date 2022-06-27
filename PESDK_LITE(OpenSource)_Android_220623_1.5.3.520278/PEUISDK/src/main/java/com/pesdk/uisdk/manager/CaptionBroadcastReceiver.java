package com.pesdk.uisdk.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.pesdk.uisdk.bean.model.WordTemplateInfo;
import com.vecore.models.internal.LabelStatusUpdatedIntent;

/**
 * 广播 计算大小
 */
public class CaptionBroadcastReceiver extends BroadcastReceiver {

    private WordInfoExt mWordInfoExt;
    private WordTemplateInfo mWordTemplateInfo;

    public void setWordInfoExt(WordInfoExt wordInfoExt) {
        mWordInfoExt = wordInfoExt;
    }

    public void setWordTemplateInfo(WordTemplateInfo wordTemplateInfo) {
        mWordTemplateInfo = wordTemplateInfo;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent instanceof LabelStatusUpdatedIntent) {
            if (mWordInfoExt != null) {
                mWordInfoExt.refreshFontSize();
            } else if (mWordTemplateInfo != null) {
                mWordTemplateInfo.refreshFontSize();
            }
        }
    }

}
