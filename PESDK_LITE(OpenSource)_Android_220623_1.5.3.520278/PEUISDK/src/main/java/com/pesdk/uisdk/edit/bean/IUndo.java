package com.pesdk.uisdk.edit.bean;

import com.pesdk.uisdk.bean.FilterInfo;
import com.pesdk.uisdk.bean.FrameInfo;
import com.pesdk.uisdk.bean.ExtImageInfo;
import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.bean.model.GraffitiInfo;
import com.pesdk.uisdk.bean.model.StickerInfo;
import com.pesdk.uisdk.bean.model.WordInfoExt;
import com.vecore.models.EffectInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 撤销、还原时备份数据
 */
public interface IUndo {

    ArrayList<StickerInfo> getCloneStickerInfos();

    ArrayList<WordInfoExt> getCloneWordNewInfos();

    ArrayList<GraffitiInfo> getCloneGraffitiInfos();

    ArrayList<EffectInfo> getCloneEffects();

    ArrayList<CollageInfo> getCloneCollageInfos();

    ArrayList<FilterInfo> getCloneFilterInfos();

    ArrayList<FrameInfo> getCloneFrameInfos();

    ArrayList<CollageInfo> getCloneOverLayList();

    ExtImageInfo getCloneScene();

    List<ExtImageInfo> getCloneSceneList();

}
