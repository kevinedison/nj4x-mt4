package com.jfx.ts.io;

import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadFactory;

public class CachedThreadFactory implements ThreadFactory {
    private static final Map<Thread, Integer> threadsInCache = new WeakHashMap<Thread, Integer>();
    private final String threadNamePrefix;
    private int priority;

    public CachedThreadFactory(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }
    public CachedThreadFactory(String threadNamePrefix, int priority) {
        this.threadNamePrefix = threadNamePrefix;
        this.priority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        synchronized (threadsInCache) {
            int max = threadsInCache.size() + 2;
            int id = 1;
            HashSet<Integer> currentThreadIDs = new HashSet<Integer>(threadsInCache.values());
            for (; id < max; id++) {
                if (!currentThreadIDs.contains(id)) {
                    break;
                }
            }
            Thread thread = new Thread(r, threadNamePrefix + id);
            threadsInCache.put(thread, id);
            return thread;
        }
    }
}
