package com.pesdk.uisdk.analyzer;

/**
 * 回调： 检查是否有人像|天空
 */
public interface ISegmentationListener {

    /**
     * @param exist true 有透明区域（人像、天空）； false 无人像
     */
    void existAlpha(boolean exist);
}
