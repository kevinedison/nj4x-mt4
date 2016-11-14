/*
 * Metatrader Java (JFX) / .Net (NJ4X) library
 * Copyright (c) 2008-2014 by Gerasimenko Roman.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistribution of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The names "JFX" or "NJ4X" must not be used to endorse or
 * promote products derived from this software without prior
 * written permission. For written permission, please contact
 * roman.gerasimenko@nj4x.com
 *
 * 4. Products derived from this software may not be called "JFX" or
 * "NJ4X", nor may "JFX" or "NJ4X" appear in their name,
 * without prior written permission of Gerasimenko Roman.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE JFX CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.jfx.ts.io;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simplifies execution of the external to the current JVM processes.
 * User: roman
 * Date: 11.06.2004
 * Time: 17:01:35
 *
 * @noinspection JavaDoc
 */
public class ExternalProcess {
    public final static ExecutorService executorService;
    private static final AtomicInteger tNo = new AtomicInteger(0);
    public static final Logger LOGGER = Logger.getLogger(ExternalProcess.class);

    static {
        executorService = java.util.concurrent.Executors.newCachedThreadPool(new CachedThreadFactory("ExternalProcess-"));
//        executorService = EfficientThreadPoolExecutor.get(16, 64, 10, TimeUnit.SECONDS, 256, "ExternalProcess-");
    }

    private String[] command;
    private String[] envp;
    private File workingDir;
    private Process process;
    private LineListener outListener, errListener;
    private Future<?> outJob;
    private Future<?> errJob;
    private int exitCode;
    //    private Thread finalizee;

    /**
     * "-1" <b>lineNb</b> and "null" <b>line</b> in call to <b>outListener.onLine(int lineNb, StringBuffer line)</b>
     * means that the ExternalProcess has been destroyed
     *
     * @param outListener
     */
    public synchronized void setOutListener(LineListener outListener) {
        if (outListener != null) {
            this.outListener = outListener;
        }
    }

    /**
     * "-1" <b>lineNb</b> and "null" <b>line</b> in call to <b>errListener.onLine(int lineNb, StringBuffer line)</b>
     * means that the ExternalProcess has been destroyed
     *
     * @param errListener
     */
    public synchronized void setErrListener(LineListener errListener) {
        this.errListener = errListener;
    }

    public String getOut() {
        try {
            outJob.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outListener instanceof DefaultOutListener ? ((DefaultOutListener) outListener).out.toString() : null;
    }

    public String getErr() {
        try {
            errJob.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errListener instanceof DefaultErrListener ? ((DefaultErrListener) errListener).err.toString() : null;
    }

    public synchronized LineListener getOutListener() {
        return outListener == null ? (outListener = new DefaultOutListener(isHidden)) : outListener;
    }

    public synchronized LineListener getErrListener() {
        return errListener == null ? (errListener = new DefaultErrListener(isHidden)) : errListener;
    }

    protected ExternalProcess() {
    }

    public ExternalProcess(String... command) {
        this.command = command;
    }

    public ExternalProcess(String[] command, String[] envp, String working_dir) {
        this.command = command;
        this.envp = envp;
        this.workingDir = working_dir == null ? null : new File(working_dir);
    }

    protected void setCommand(String[] command) {
        this.command = command;
    }

    public Process getProcess() {
        return process;
    }

    public void kill() {
        if (process != null) {
            try {
                process.destroy();
//                Runtime.getRuntime().removeShutdownHook(finalizee);
            } finally {
                process = null;
            }
        }
    }

    public void restart() throws IOException {
        kill();
        start();
    }

    public void setEnvp(String[] envp) {
        this.envp = envp;
    }

    private boolean isHidden;

    public ExternalProcess setIsHidden(boolean isHidden) {
        this.isHidden = isHidden;
        return this;
    }

    public int run() throws IOException, InterruptedException {
        return run(0);
    }

    public int run(int timeoutMillis) throws IOException, InterruptedException {
        exitCode = Integer.MIN_VALUE;
        try {
            if (Log4JUtil.isConfigured() && (!isHidden && LOGGER.isDebugEnabled() || LOGGER.isTraceEnabled())) {
                LOGGER.debug("ExternalProcess<START>: cmd="
                                + asString(command)
                                + ",  envp=" + asString(envp)
                                + ",  wd=" + workingDir
                );
            }
            start();
            if (timeoutMillis == 0) {
                return (exitCode = process.waitFor());
            } else {
                try {
                    Method waitFor = process.getClass().getMethod("waitFor", Long.TYPE, TimeUnit.class);
                    waitFor.invoke(process, timeoutMillis, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    //e.printStackTrace();
                    return (exitCode = process.waitFor());//no timeout functionality
                }
            }
            return (exitCode = 12345);
        } finally {
            kill();
            //
            if (Log4JUtil.isConfigured() && (!isHidden && LOGGER.isDebugEnabled() || LOGGER.isTraceEnabled())) {
                LOGGER.debug("ExternalProcess<END>: cmd="
                                + asString(command)
                                + ",  exit_code=" + (exitCode == Integer.MIN_VALUE ? "not defined" : "" + exitCode)
                );
            }
        }
    }

    public static String asString(String... p) {
        if (p == null) {
            return "<null>";
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < p.length; i++) {
                String s = p[i];
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(s);
            }
            return sb.toString();
        }
    }

    public void start() throws IOException {
        if (process != null) {
            throw new IOException("Kill it first...");
        }
        this.process = Runtime.getRuntime().exec(command, envp, workingDir);
        //
        this.process.getOutputStream().close(); // saved me 3 days of headache - http://stackoverflow.com/questions/13367527/java-stuck-in-infinite-loop-executing-a-wmic-command-on-windows-server-2003
        outJob = executorService.submit(new ProcessStreamReader(this.process.getInputStream(), getOutListener()));
        errJob = executorService.submit(new ProcessStreamReader(this.process.getErrorStream(), getErrListener()));
    }

    public boolean isHealthy() {
        try {
            return process != null && process.getInputStream() != null;
        } catch (Throwable e) {
            kill();
            return false;
        }
    }

    private static class DefaultOutListener implements LineListener {
        StringBuilder out = new StringBuilder();
        private boolean isHidden;

        public DefaultOutListener(boolean isHidden) {
            this.isHidden = isHidden;
        }

        public void onLine(int lineNb, StringBuffer line) {
            if (line != null) {
                out.append(line).append('\n');
                if (!isHidden && Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("OUT#%04d> %s", lineNb, line.toString()));
                }
            }
        }
    }

    private static class DefaultErrListener implements LineListener {
        StringBuilder err = new StringBuilder();
        private boolean isHidden;

        public DefaultErrListener(boolean isHidden) {
            this.isHidden = isHidden;
        }

        public void onLine(int lineNb, StringBuffer line) {
            if (line != null) {
                err.append(line).append('\n');
                if (!isHidden && Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("ERR#%04d> %s", lineNb, line.toString()));
                }
            }
        }
    }

    private class ProcessStreamReader implements Runnable {
        private InputStream processStream;
        private LineListener streamListener;

        public ProcessStreamReader(InputStream processStream, LineListener streamListener) {
            //setDaemon(true);
            this.processStream = processStream;
            this.streamListener = streamListener;
        }

        public void run() {
            Thread currentThread = Thread.currentThread();
            String name = currentThread.getName();
            try {
                String threadName = command[0].substring(command[0].lastIndexOf('\\') + 1).toUpperCase().trim();
                if (threadName.endsWith(".EXE")) {
                    threadName = threadName.substring(0, threadName.length() - 4);
                }
                currentThread.setName(threadName);
                //
                new InputStreamManager(processStream).readASCIILines(streamListener);
                //
//                int line = 0;
//                BufferedReader reader = new BufferedReader(new InputStreamReader(processStream));
//                if (processStream != null) {
//                    String str;
//                    while ((str = reader.readLine()) != null) {
//                        streamListener.onLine(line++, new StringBuffer(str));
//                    }
//                }
                //
                streamListener.onLine(-1, null);
            } catch (IOException e) {
                getErrListener().onLine(-1, new StringBuffer("" + e + ": " + e.getMessage()));
            } finally {
                try {
                    processStream.close();
                } catch (Throwable ignore) {
                }
                currentThread.setName(name);
            }
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < command.length; i++) {
            sb.append(command[i]).append(' ');
        }
        return sb.toString();
    }

    public static void main(final String[] args) throws Throwable {
/*
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        pb.redirectInput(ProcessBuilder.Redirect.PIPE);
        Process start = pb.start();
        System.out.println("exit: " + start.waitFor());
*/
        extProc(args);
    }

    public static void extProc(final String[] args) throws IOException, InterruptedException {
        final ExternalProcess p = new ExternalProcess(
                args
        );
/*
        new Thread() {
            {
                setDaemon(true);
            }

            @Override
            public void run() {
                try {
                    new InputStreamManager(System.in).readASCIILines(new LineListener() {
                        @Override
                        public void onLine(int lineNb, StringBuffer line) {
                            try {
                                System.err.println("Sending [" + line + "] to " + asString(args));
                                p.sendToProcess(line.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
*/
        p.start();
        System.out.println("Started: " + asString(args));
//        int exit = p.run();
        int exit = p.process.waitFor();
        //
        String out = p.getOut();
        System.out.println("Exit: " + exit);
        System.out.println("Out: " + out);
        System.out.println("Err: " + p.getErr());
        //Thread.sleep(10000);
    }
}
