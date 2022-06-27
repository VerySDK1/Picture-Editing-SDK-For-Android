package com.pesdk.uisdk.fragment.main;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 主编辑对应的菜单
 */
@Retention(RetentionPolicy.CLASS)
public @interface IMenu {


    int MODE_PREVIEW = 100; //主编辑


    int sticker = 101;
    int text = 102;
    int filter = 103;
    int beauty = 104;
    int crop = 105;
    int smear = 106;  //涂抹
    int graffiti = 107;
    int adjust = 108;
    int blur = 109;
    int aperture = 110; //光圈
    int hdr = 111;
    int holy_light = 112; //圣光
    int watermark = 113; //水印
    int erase = 114; //消除笔
    int pip = 115; //图层
    int effect = 117; //特效
    int mosaic = 118; //马赛克
    int koutu = 119; //抠图
    //    int addPip = 120; //添加图层
    int canvas = 121; //背景
    int mask = 122; //形状填充:蒙版
    int depth = 123; //景深
    int proportion = 124; //比例
    int mirror = 125;//镜像
    int frame = 126; //边框
    int overlay = 127; //叠加(与图层类似)
    int sky = 128; //天空抠图
    int mainTrack =129;//主图

    int mix=130;//图片合成，此步骤将当前虚拟图片导出为一张图片作为底图

}