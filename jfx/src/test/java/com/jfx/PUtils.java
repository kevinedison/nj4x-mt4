package com.jfx;

/**
 * Performance tests and utilities.
 * User: roman
 * Date: 12/02/14
 * Time: 11:01
 */
public class PUtils {
    public static long time(Job job) throws Exception {
        long start = System.currentTimeMillis();
        job.run();
        long end = System.currentTimeMillis();
        return (end - start);
    }

    public interface Job {
        public void run() throws Exception;
    }

    public static long calcTPS(Job j, int max, String verboseMessage) throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < max; i++) {
            j.run();
        }
        long end = System.currentTimeMillis();
        long tps = (long) ((double) (end -start) / max * 1000L);
        if (verboseMessage != null) {
            System.out.println(verboseMessage + ": iter=" + max
                    + ", time=" + (end - start)
                    + " millis, TPS=" + tps
            );
        }
        return tps;
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }
}
