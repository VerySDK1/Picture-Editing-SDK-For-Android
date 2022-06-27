package com.pesdk.uisdk.util.helper;

import com.vecore.models.BlendParameters;
import com.vecore.models.PEImageObject;

/**
 *
 */
public class OverlayHelper {

    /**
     * 叠加专用标识
     */
    public static void setOverlay(PEImageObject peImageObject) {
        peImageObject.setBlendParameters(new BlendParameters.Screen());
    }
}
