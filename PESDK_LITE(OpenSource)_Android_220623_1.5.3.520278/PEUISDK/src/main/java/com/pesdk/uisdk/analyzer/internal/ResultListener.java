package com.pesdk.uisdk.analyzer.internal;

/**
 *
 */
public interface ResultListener {

    /**
     * 抠图是否成功
     *
     * @param success true 成功;false 失败
     */
    void onResult(boolean success);
}
