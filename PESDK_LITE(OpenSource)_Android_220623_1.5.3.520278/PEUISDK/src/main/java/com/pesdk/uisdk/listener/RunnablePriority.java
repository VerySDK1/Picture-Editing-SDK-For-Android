package com.pesdk.uisdk.listener;

/**
 * 优先级
 */
public class RunnablePriority implements Runnable, Comparable<RunnablePriority> {

    //先比较 priority   再比较 时间
    private final int mPriority;//优先级
    private int mTime;//缩略图时间点
    //线程
    private OnPriorityRun mPriorityRun;
    private OnPriorityTimeRun mPriorityTimeRun;

    public RunnablePriority(int priority, OnPriorityRun priorityRun) {
        mPriority = priority;
        mPriorityRun = priorityRun;
    }

    public RunnablePriority(int priority, int time, OnPriorityTimeRun priorityRun) {
        mPriority = priority;
        mPriorityTimeRun = priorityRun;
        mTime = time;
    }

    @Override
    public int compareTo(RunnablePriority o) {
        //倒序排列
        if (this.mPriority < o.mPriority) {
            return 1;
        } else if (this.mPriority > o.mPriority){
            return -1;
        }
        //升序排列
        if (this.mTime > o.mTime) {
            return 1;
        } else if (this.mTime < o.mTime) {
            return -1;
        }
        return 0;
    }

    @Override
    public void run() {
        // 执行任务代码..
        if (mPriorityRun != null) {
            mPriorityRun.run();
        } else if (mPriorityTimeRun != null) {
            mPriorityTimeRun.run(mTime);
        }
    }

}
