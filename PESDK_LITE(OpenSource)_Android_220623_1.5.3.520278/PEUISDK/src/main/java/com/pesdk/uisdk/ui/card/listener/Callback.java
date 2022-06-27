package com.pesdk.uisdk.ui.card.listener;

import com.pesdk.uisdk.beauty.bean.FaceHairInfo;
import com.pesdk.uisdk.ui.card.fragment.ClothesFragment;

/**
 *
 */
public interface Callback {

    void onPreClothes();


    void goneEarse();

    FaceHairInfo getHairInfo();


    ClothesFragment.Callback getClothesCallback();
}
