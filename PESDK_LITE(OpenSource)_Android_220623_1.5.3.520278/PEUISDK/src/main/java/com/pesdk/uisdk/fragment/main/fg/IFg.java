package com.pesdk.uisdk.fragment.main.fg;

import com.pesdk.uisdk.bean.model.CollageInfo;
import com.pesdk.uisdk.Interface.Ioff;

/**
 *
 */
public interface IFg extends Ioff {

    /**
     * 获取集合最后的Layer （最上层）
     */
    CollageInfo getTopMedia();//获取前景


    /**
     * 保存并退出
     */
    void exit2Main();

}
