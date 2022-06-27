package com.pesdk.uisdk.util.manager;

import android.content.Context;

import com.pesdk.uisdk.bean.record.RecordBean;
import com.pesdk.uisdk.util.AppConfiguration;
import com.vecore.VECoreHelper;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.util.HashMap;
import java.util.List;

public class MaskManager {
    private MaskManager() {

    }

    private volatile static MaskManager instance;

    public static MaskManager getInstance() {
        if (null == instance) {
            synchronized (MaskManager.class) {
                if (null == instance) {
                    instance = new MaskManager();
                }
            }
        }
        return instance;
    }

    //name,maskId
    private HashMap<String, Integer> registeredData = new HashMap<>();

    public void add(String name, int id) {
        registeredData.put(name, id);
    }

    /**
     * @param dir
     * @return
     */
    public int getRegistered(String dir) {
        Integer re = registeredData.get(dir);
        if (null == re) {
            return 0;
        }
        return re;
    }

    public void recycle() {
        registeredData.clear();
    }


    /**
     * 进入就注册
     */
    public void init(Context context) {
        ThreadPoolUtils.executeEx(() -> {
            List<RecordBean> list = AppConfiguration.getMaskList();
            if (null != list) {
                int len = list.size();
                for (int i = 0; i < len; i++) {
                    RecordBean bean = list.get(i);
                    init(bean.getName(), bean.getPath());
                }
            }
        });
    }


    public boolean init(String name, String maskDir) {
        int id = VECoreHelper.registerMask(maskDir);
        if (id <= 0) {
            return false;
        }
        add(name, id);
        return true;
    }

}
