package com.pesdk.uisdk.bean.template;

import android.graphics.Color;
import android.text.TextUtils;

import com.pesdk.uisdk.bean.RegisteredInfo;
import com.pesdk.uisdk.util.PathUtils;
import com.vecore.PECore;
import com.vecore.VECoreHelper;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.FileUtils;
import com.vecore.customFilter.TextureResource;
import com.vecore.models.BlendParameters;
import com.vecore.models.DewatermarkObject;
import com.vecore.models.EffectType;
import com.vecore.models.MusicFilterType;
import com.vecore.models.TransitionType;
import com.vecore.models.VisualCustomFilter;
import com.vecore.models.VisualFilterConfig;
import com.vecore.models.customfilter.IEffect;
import com.vecore.models.customfilter.ITransition;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static com.vecore.models.VisualFilterConfig.FILTER_ID_NORMAL;

/**
 * 模板工具类
 */
public class TemplateUtils {

    //文件夹 moveFile 本地目录(..../template/****) 移动到的目录(..../template/****/Sticker) 子目录名字(Sticker)
    public static final String DIR_STICKER = "Sticker";
    public static final String DIR_OVERLAY = "Overlay";//叠加
    public static final String DIR_FRAME = "Frame";//边框
    public static final String DIR_SUBTITLE = "Subtitle";
    public static final String DIR_MEDIA = "Media";
    public static final String DIR_DOODLE = "Doodle";
    public static final String DIR_BG = "Bg";
    public static final String DIR_FILTER = "Filter";
    //    public static final String DIR_EFFECT = "Effect";
    public static final String DIR_MASK = "Mask";

    //创建目录
    public static String mkDir(String localPath, String dir) {
        File filterFile = new File(localPath, dir);
        PathUtils.checkPath(filterFile, true);
        return PathUtils.getFilePath(filterFile);
    }

    private static final ArrayList<RegisteredInfo> sRegistered = new ArrayList<>();

    /**
     * 注册蒙版
     */
    public static int registeredMask(String dir) {
        //目录null
        if (TextUtils.isEmpty(dir) || !FileUtils.isExist(dir)) {
            return 0;
        }
        RegisteredInfo registered = getRegistered(dir);
        int id;
        if (registered == null) {
            id = VECoreHelper.registerMask(dir);
            if (id > 0) {
                setRegistered(new RegisteredInfo(dir, id));
            }
        } else {
            id = registered.getRegisterId();
        }
        return id;
    }

    /**
     * 已经注册
     */
    public static RegisteredInfo getRegistered(String path) {
        if (sRegistered.size() > 0 && !TextUtils.isEmpty(path)) {
            for (RegisteredInfo info : sRegistered) {
                if (info.getPath().equals(path)) {
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * 记录
     */
    public static void setRegistered(RegisteredInfo info) {
        sRegistered.add(info);
    }

    /**
     * 清除
     */
    public static void clear() {
        sRegistered.clear();
    }

    //注册 特效和滤镜
    public static int registeredID(String dir) {
        return registeredID(dir, null, false);
    }

    //注册 特效和滤镜  转场需要使用   dir config.json级目录
    public static int registeredID(String dir, String freezePath, boolean transition) {
        //目录null
        if (TextUtils.isEmpty(dir) || !FileUtils.isExist(dir)) {
            return 0;
        }
        if (transition) { //转场
            ITransition transitionFilter = VECoreHelper.registerTransition(dir);
            if (null == transitionFilter) {
                return 0;
            }
            if (transitionFilter.getTransitionType() != null) { //内置转场
                return -transitionFilter.getTransitionType().ordinal();
            } else { //滤镜转场
                return transitionFilter.getId();
            }
        } else { //特效
            IEffect effect = VECoreHelper.registerEffect(dir);
            if (null != effect) {
                if (VisualFilterConfig.FILTER_ID_ECHO == effect.getId()) {
                    return -VisualFilterConfig.FILTER_ID_ECHO;
                }
                return effect.getId();
            }
        }
        return 0;
    }

    //注册 转场
    public static int registeredTransition(String transitionPath) {
        int id = registeredID(transitionPath, null, true);
        if (id == 0) {
            File file = new File(transitionPath);
            String path = transitionPath;
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        if (f.getName().toLowerCase().contains("glsl")) {
                            path = PathUtils.getFilePath(f);
                            break;
                        }
                    }
                }
            }
            //单文件
            VisualCustomFilter visualCustomFilter = new VisualCustomFilter(true);
            visualCustomFilter.setFragmentShader(FileUtils.readTxtFile(CoreUtils.getContext(), path));
            visualCustomFilter.setTextureResources(new TextureResource[]{new TextureResource("from"),
                    new TextureResource("to")});
            //返回注册id
            id = PECore.registerCustomFilter(CoreUtils.getContext(), visualCustomFilter);
        }
        return id;
    }


    //混合类型
    private static final HashMap<Integer, BlendParameters> sBlendParameters = new HashMap<>();

    public static int getBlendIndex(BlendParameters blendParameters) {
        if (sBlendParameters.size() <= 0) {
            sBlendParameters.put(3, new BlendParameters.Darken());
            sBlendParameters.put(4, new BlendParameters.Screen());
            sBlendParameters.put(5, new BlendParameters.Overlay());
            sBlendParameters.put(6, new BlendParameters.Multiply());
            sBlendParameters.put(7, new BlendParameters.Lighten());
            sBlendParameters.put(8, new BlendParameters.HardLight());
            sBlendParameters.put(9, new BlendParameters.SoftLight());
            sBlendParameters.put(10, new BlendParameters.LinearBurn());
            sBlendParameters.put(11, new BlendParameters.ColorBurn());
            sBlendParameters.put(12, new BlendParameters.ColorDodge());
        }
        for (HashMap.Entry<Integer, BlendParameters> entry : sBlendParameters.entrySet()) {
            if (entry.getValue().equals(blendParameters)) {
                return entry.getKey();
            }
        }
        return 0;
    }

    public static BlendParameters getBlend(int index) {
        if (sBlendParameters.size() <= 0) {
            sBlendParameters.put(3, new BlendParameters.Darken());
            sBlendParameters.put(4, new BlendParameters.Screen());
            sBlendParameters.put(5, new BlendParameters.Overlay());
            sBlendParameters.put(6, new BlendParameters.Multiply());
            sBlendParameters.put(7, new BlendParameters.Lighten());
            sBlendParameters.put(8, new BlendParameters.HardLight());
            sBlendParameters.put(9, new BlendParameters.SoftLight());
            sBlendParameters.put(10, new BlendParameters.LinearBurn());
            sBlendParameters.put(11, new BlendParameters.ColorBurn());
            sBlendParameters.put(12, new BlendParameters.ColorDodge());
        }
        for (HashMap.Entry<Integer, BlendParameters> entry : sBlendParameters.entrySet()) {
            if (entry.getKey() == index) {
                return entry.getValue();
            }
        }
        return null;
    }


    //音效类型
    private static final HashMap<Integer, Integer> sAudioFilterType = new HashMap<>();

    public static int getAudioIndex(MusicFilterType musicFilterType) {
        if (musicFilterType == null) {
            return 0;
        }
        int type = musicFilterType.ordinal();
        if (sAudioFilterType.size() <= 0) {
            sAudioFilterType.put(1, MusicFilterType.MUSIC_FILTER_BOY.ordinal());
            sAudioFilterType.put(2, MusicFilterType.MUSIC_FILTER_GIRL.ordinal());
            sAudioFilterType.put(3, MusicFilterType.MUSIC_FILTER_MONSTER.ordinal());
            sAudioFilterType.put(4, MusicFilterType.MUSIC_FILTER_CARTOON.ordinal());
            sAudioFilterType.put(6, MusicFilterType.MUSIC_FILTER_ECHO.ordinal());
            sAudioFilterType.put(7, MusicFilterType.MUSIC_FILTER_REVERB.ordinal());
            sAudioFilterType.put(8, MusicFilterType.MUSIC_FILTER_ROOM.ordinal());
            sAudioFilterType.put(9, MusicFilterType.MUSIC_FILTER_DANCE.ordinal());
            sAudioFilterType.put(10, MusicFilterType.MUSIC_FILTER_KTV.ordinal());
            sAudioFilterType.put(11, MusicFilterType.MUSIC_FILTER_FACTORY.ordinal());
            sAudioFilterType.put(12, MusicFilterType.MUSIC_FILTER_ARENA.ordinal());
            sAudioFilterType.put(13, MusicFilterType.MUSIC_FILTER_ELECTRI.ordinal());
            sAudioFilterType.put(14, MusicFilterType.MUSIC_FILTER_CUSTOM.ordinal());
        }

        for (HashMap.Entry<Integer, Integer> entry : sAudioFilterType.entrySet()) {
            if (entry.getValue() == type) {
                return entry.getKey();
            }
        }
        return 0;
    }

    public static int getAudioFilterType(int index) {
        if (sAudioFilterType.size() <= 0) {
            sAudioFilterType.put(1, MusicFilterType.MUSIC_FILTER_BOY.ordinal());
            sAudioFilterType.put(2, MusicFilterType.MUSIC_FILTER_GIRL.ordinal());
            sAudioFilterType.put(3, MusicFilterType.MUSIC_FILTER_MONSTER.ordinal());
            sAudioFilterType.put(4, MusicFilterType.MUSIC_FILTER_CARTOON.ordinal());
            sAudioFilterType.put(6, MusicFilterType.MUSIC_FILTER_ECHO.ordinal());
            sAudioFilterType.put(7, MusicFilterType.MUSIC_FILTER_REVERB.ordinal());
            sAudioFilterType.put(8, MusicFilterType.MUSIC_FILTER_ROOM.ordinal());
            sAudioFilterType.put(9, MusicFilterType.MUSIC_FILTER_DANCE.ordinal());
            sAudioFilterType.put(10, MusicFilterType.MUSIC_FILTER_KTV.ordinal());
            sAudioFilterType.put(11, MusicFilterType.MUSIC_FILTER_FACTORY.ordinal());
            sAudioFilterType.put(12, MusicFilterType.MUSIC_FILTER_ARENA.ordinal());
            sAudioFilterType.put(13, MusicFilterType.MUSIC_FILTER_ELECTRI.ordinal());
            sAudioFilterType.put(14, MusicFilterType.MUSIC_FILTER_CUSTOM.ordinal());
        }

        for (HashMap.Entry<Integer, Integer> entry : sAudioFilterType.entrySet()) {
            if (entry.getKey() == index) {
                return entry.getValue();
            }
        }
        return 0;
    }


    //特效类型
    private static final HashMap<Integer, EffectType> sEffectType = new HashMap<>();

    public static int getEffectIndex(EffectType type) {
        if (type == null) {
            return 0;
        }
        if (sEffectType.size() <= 0) {
            sEffectType.put(0, EffectType.NONE);
            sEffectType.put(1, EffectType.TREMBLE);
            sEffectType.put(2, EffectType.AWAKENE);
            sEffectType.put(3, EffectType.GAUSSIAN_BLUR);
            sEffectType.put(4, EffectType.HEARTBEAT);
            sEffectType.put(5, EffectType.SPOTLIGHT);
            sEffectType.put(6, EffectType.SLOW);
            sEffectType.put(7, EffectType.REPEAT);
            sEffectType.put(8, EffectType.REVERSE);
        }
        for (HashMap.Entry<Integer, EffectType> entry : sEffectType.entrySet()) {
            if (entry.getValue() == type) {
                return entry.getKey();
            }
        }
        return 0;
    }

    public static EffectType getEffectType(int index) {
        if (sEffectType.size() <= 0) {
            sEffectType.put(0, EffectType.NONE);
            sEffectType.put(1, EffectType.TREMBLE);
            sEffectType.put(2, EffectType.AWAKENE);
            sEffectType.put(3, EffectType.GAUSSIAN_BLUR);
            sEffectType.put(4, EffectType.HEARTBEAT);
            sEffectType.put(5, EffectType.SPOTLIGHT);
            sEffectType.put(6, EffectType.SLOW);
            sEffectType.put(7, EffectType.REPEAT);
            sEffectType.put(8, EffectType.REVERSE);
        }
        for (HashMap.Entry<Integer, EffectType> entry : sEffectType.entrySet()) {
            if (entry.getKey() == index) {
                return entry.getValue();
            }
        }
        return EffectType.NONE;
    }


    //马赛克
    private static final HashMap<Integer, String> sMosaicsType = new HashMap<>();

    public static int getMosaicsIndex(String name) {
        if (TextUtils.isEmpty(name)) {
            return 0;
        }
        if (sMosaicsType.size() <= 0) {
            sMosaicsType.put(0, "像素化");
            sMosaicsType.put(1, "高斯模糊");
            sMosaicsType.put(2, "去水印");
            sMosaicsType.put(3, "Pixel");
            sMosaicsType.put(4, "Blur");
            sMosaicsType.put(5, "Inpaint");
        }
        for (HashMap.Entry<Integer, String> entry : sMosaicsType.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey() % 3;
            }
        }
        return 0;
    }

    public static String getMosaicsName(int index) {
        if (sMosaicsType.size() <= 0) {
            sMosaicsType.put(0, "像素化");
            sMosaicsType.put(1, "高斯模糊");
            sMosaicsType.put(2, "去水印");
            sMosaicsType.put(3, "Pixel");
            sMosaicsType.put(4, "Blur");
            sMosaicsType.put(5, "Inpaint");
        }
        for (HashMap.Entry<Integer, String> entry : sMosaicsType.entrySet()) {
            if (entry.getKey() == index) {
                return entry.getValue();
            }
        }
        return "高斯模糊";
    }

    public static DewatermarkObject.Type getMosaicsType(int index) {
        if (index == 1 || index == 4) {
            return DewatermarkObject.Type.blur;
        } else if (index == 2 || index == 5) {
            return DewatermarkObject.Type.watermark;
        } else if (index == 0 || index == 3) {
            return DewatermarkObject.Type.mosaic;
        }
        return null;
    }


    //转场类型
    private static final HashMap<Integer, TransitionType> sTransitionType = new HashMap<>();

    public static int getTransitionIndex(TransitionType type) {
        if (type == null) {
            return 0;
        }
        if (sTransitionType.size() <= 0) {
            sTransitionType.put(0, TransitionType.TRANSITION_NULL);
            sTransitionType.put(1, TransitionType.TRANSITION_TO_LEFT);
            sTransitionType.put(2, TransitionType.TRANSITION_TO_RIGHT);
            sTransitionType.put(3, TransitionType.TRANSITION_TO_UP);
            sTransitionType.put(4, TransitionType.TRANSITION_TO_DOWN);
            sTransitionType.put(5, TransitionType.TRANSITION_OVERLAP);
            sTransitionType.put(6, TransitionType.TRANSITION_BLINK_BLACK);
            sTransitionType.put(7, TransitionType.TRANSITION_BLINK_WHITE);
            sTransitionType.put(8, TransitionType.TRANSITION_GRAY);
            sTransitionType.put(13, TransitionType.TRANSITION_CUSTOM_FILTER_ID);
        }
        for (HashMap.Entry<Integer, TransitionType> entry : sTransitionType.entrySet()) {
            if (entry.getValue() == type) {
                return entry.getKey();
            }
        }
        return 0;
    }

    public static TransitionType getTransitionType(int index) {
        if (sTransitionType.size() <= 0) {
            sTransitionType.put(0, TransitionType.TRANSITION_NULL);
            sTransitionType.put(1, TransitionType.TRANSITION_TO_LEFT);
            sTransitionType.put(2, TransitionType.TRANSITION_TO_RIGHT);
            sTransitionType.put(3, TransitionType.TRANSITION_TO_UP);
            sTransitionType.put(4, TransitionType.TRANSITION_TO_DOWN);
            sTransitionType.put(5, TransitionType.TRANSITION_OVERLAP);
            sTransitionType.put(6, TransitionType.TRANSITION_BLINK_BLACK);
            sTransitionType.put(7, TransitionType.TRANSITION_BLINK_WHITE);
            sTransitionType.put(8, TransitionType.TRANSITION_GRAY);
            sTransitionType.put(13, TransitionType.TRANSITION_CUSTOM_FILTER_ID);
        }
        for (HashMap.Entry<Integer, TransitionType> entry : sTransitionType.entrySet()) {
            if (entry.getKey() == index) {
                return entry.getValue();
            }
        }
        return TransitionType.TRANSITION_NULL;
    }


    //分组颜色
    private static final ArrayList<Integer> mGroupColor = new ArrayList<>();

    public static int getGroupColor(int group) {
        if (mGroupColor.size() <= 0) {
            mGroupColor.add(Color.RED);
            mGroupColor.add(Color.parseColor("#6699ff"));
            mGroupColor.add(Color.parseColor("#CD6090"));
            mGroupColor.add(Color.parseColor("#99cc33"));
            mGroupColor.add(Color.parseColor("#CD3700"));
            mGroupColor.add(Color.parseColor("#ffcc00"));
            mGroupColor.add(Color.parseColor("#ff6600"));
            mGroupColor.add(Color.parseColor("#00ff00"));
            mGroupColor.add(Color.parseColor("#ff00ff"));
            mGroupColor.add(Color.parseColor("#FF7F00"));
            mGroupColor.add(Color.parseColor("#FF4500"));
            mGroupColor.add(Color.parseColor("#FF1493"));
            mGroupColor.add(Color.parseColor("#EEC591"));
            mGroupColor.add(Color.parseColor("#EECBAD"));
            mGroupColor.add(Color.parseColor("#D3D3D3"));
            mGroupColor.add(Color.parseColor("#D2691E"));
            mGroupColor.add(Color.parseColor("#CD950C"));
            mGroupColor.add(Color.parseColor("#CD661D"));
            mGroupColor.add(Color.parseColor("#CD6090"));
            mGroupColor.add(Color.parseColor("#CD3700"));
            mGroupColor.add(Color.parseColor("#CAFF70"));
            mGroupColor.add(Color.parseColor("#CAE1FF"));
            mGroupColor.add(Color.parseColor("#C0FF3E"));
            mGroupColor.add(Color.parseColor("#BCEE68"));
            mGroupColor.add(Color.parseColor("#B8860B"));
            mGroupColor.add(Color.parseColor("#AB82FF"));
            mGroupColor.add(Color.parseColor("#A52A2A"));
            mGroupColor.add(Color.parseColor("#97FFFF"));
        }
        return mGroupColor.get(group % mGroupColor.size());
    }

    /**
     * 滤镜扩展
     */
    public static int getFilterType(VisualFilterConfig config) {
        if (config instanceof VisualFilterConfig.Pixelate) {
            return 12;
        }
        return 11;
    }

    public static VisualFilterConfig filterTypeToId(int type) {
        if (type == 12) {
            return new VisualFilterConfig.Pixelate(true);
        }
        return new VisualFilterConfig(FILTER_ID_NORMAL);
    }

}
