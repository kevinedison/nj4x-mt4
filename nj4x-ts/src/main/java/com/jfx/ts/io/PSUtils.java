/*
 * Copyright (c) 2008-2014 by Gerasimenko Roman.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistribution of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 * 3. The name "JFX" must not be used to endorse or promote
 *     products derived from this software without prior written
 *     permission.
 *     For written permission, please contact roman.gerasimenko@gmail.com
 *
 * 4. Products derived from this software may not be called "JFX",
 *     nor may "JFX" appear in their name, without prior written
 *     permission of Gerasimenko Roman.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE JFX CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 */

package com.jfx.ts.io;

import com.jfx.ts.net.TS;
import com.jfx.ts.net.TerminalServer;

import java.io.File;

public class PSUtils {
    public static final boolean IS_CPU1_FREE = System.getProperty("is_cpu1_free", "" + (TerminalServer.AVAILABLE_PROCESSORS > 2)).equals("true");

    // Load the dll that exports functions callable from java
    static {
        if (!LibrariesUtil.IS_OK) {
            String dllFileName = null;
            try {
                if (LibrariesUtil.isX64) {
                    dllFileName = "PSUtils_x64.dll";
                } else {
                    dllFileName = "PSUtils.dll";
                }
                String libFileName = LibrariesUtil.LIBS_DIR + File.separator + dllFileName;
                System.load(libFileName);
            } catch (Throwable t) {
                t.printStackTrace();
                try {
                    System.load(dllFileName);
                } catch (Exception e) {
                    System.exit(2);
                }
            }
        }
    }

    // Imported function declarations
    private static native String[] checkProcess(String processName, boolean killIt, boolean printOutput);

    private static native String[] QUSER();

    private static native int startProcess2(String commandLine, String workingDir, boolean hide, boolean save1CPU, boolean minimized);

    private static native int startProcess(String commandLine, String workingDir, boolean hide, boolean save1CPU);

    public static native boolean postMsg(boolean isSendToTheTopMostWindow, int hWnd, int msg, int p1, long p2);

    public static native boolean postMsgByPID(int pid, int msg, int p1, long p2);

    public static native boolean swShowHideByPID(boolean swShow, int PID);

    public static native boolean toggleWindowVisibilityByPID(int PID);

    public static native boolean terminateApp(int PID, int timeoutMillis);

    public static native int getHwndByPID(int PID);

    public static native int localSessionId();

    public static int startProcess2(String commandLine, String workingDir, boolean hide, boolean minimized) {
        if (LibrariesUtil.isWindows) {
            return startProcess2(commandLine, workingDir, hide, IS_CPU1_FREE, minimized);
        } else {
            return 0;//todo
        }
    }

    public static WtsSessionInfo[] getSessions(){
        WtsSessionInfo[] res = null;
        String[] qUsers = QUSER();
        if (qUsers != null) {
            res = new WtsSessionInfo[qUsers.length];
            for (int i = 0; i < qUsers.length; i++) {
                res[i] = new WtsSessionInfo(qUsers[i]);
            }
        }
        return res;
    }

    public static class WtsSessionInfo {
        public int id;
        /*
          	WTSActive, = 0
            WTSConnected, = 1
            WTSConnectQuery, = 2
            WTSShadow, = 3
            WTSDisconnected, = 4
            WTSIdle, = 5
            WTSListen, = 6
            WTSReset, = 7
            WTSDown, = 8
            WTSInit = 9
        */
        public int state;
        public String name, user;

        @Override
        public String toString() {
            return "Session {" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", state=" + state +
                    ", user='" + user + '\'' +
                    '}';
        }

        public WtsSessionInfo(String info) {
            String[] split = info.split("\u0001");
            id = Integer.parseInt(split[0]);
            name = split[1];
            state = Integer.parseInt(split[2]);
            user = split[3].toUpperCase();
        }
    }

    public static enum MibState {
        NO_CONNECTIONS_DETECTED, CLOSED, LISTEN, SYN_SENT, SYN_RCVD, ESTAB, FIN_WAIT1, FIN_WAIT2, CLOSE_WAIT, CLOSING, LAST_ACK, TIME_WAIT, DELETE_TCB;
    }
    //这个是TCP连接的对象
    public static class MibTcpRowOwnerPid {
        public int state, localAddr, localPort, remoteAddr, remotePort, pid;

        public MibTcpRowOwnerPid(int state, int localAddr, int localPort, int remoteAddr, int remotePort, int pid) {
            this.state = state;
            this.localAddr = localAddr;
            this.localPort = localPort;
            this.remoteAddr = remoteAddr;
            this.remotePort = remotePort;
            this.pid = pid;
        }

        private byte[] unpack(int bytes) {
            return new byte[]{
                    (byte) ((bytes >>> 24) & 0xff),
                    (byte) ((bytes >>> 16) & 0xff),
                    (byte) ((bytes >>> 8) & 0xff),
                    (byte) ((bytes) & 0xff)
            };
        }

        private String addrToString(int bytes) {
            return ""
                    + ((int) ((bytes >>> 24) & 0xff)) + '.'
                    + ((int) ((bytes >>> 16) & 0xff)) + '.'
                    + ((int) ((bytes >>> 8) & 0xff)) + '.'
                    + ((int) ((bytes) & 0xff));
        }

        public MibState getState() {
            switch (state) {
                case 1:
                    return MibState.CLOSED;
                case 2:
                    return MibState.LISTEN;
                case 3:
                    return MibState.SYN_SENT;
                case 4:
                    return MibState.SYN_RCVD;
                case 5:
                    return MibState.ESTAB;
                case 6:
                    return MibState.FIN_WAIT1;
                case 7:
                    return MibState.FIN_WAIT2;
                case 8:
                    return MibState.CLOSE_WAIT;
                case 9:
                    return MibState.CLOSING;
                case 10:
                    return MibState.LAST_ACK;
                case 11:
                    return MibState.TIME_WAIT;
                case 12:
                    return MibState.DELETE_TCB;
            }
            return MibState.NO_CONNECTIONS_DETECTED;
        }

        public String getLocalAddress() {
            return addrToString(localAddr);
        }

        public int getLocalAddressAsInt() {
            return localAddr;
        }

        public int getLocalPort() {
            return localPort;
        }

        public String getRemoteAddress() {
            return addrToString(remoteAddr);
        }

        public int getRemoteAddressInt() {
            return remoteAddr;
        }

        public int getRemotePort() {
            return remotePort;
        }

        public int getPid() {
            return pid;
        }

        @Override
        public String toString() {
            return getLocalAddress() + ':' + localPort + " -> " + getRemoteAddress() + ':' + remotePort + ' ' + getState() + " (pid=" + pid + ')';
        }

    }

    public static native MibTcpRowOwnerPid[] getProcessConnections(int pid);

    public static native int duplicateSocket(int pid, int fd, byte[] res);

    public static native int closeSocket(int fd);

    public static native boolean isAdministrator();  //标识符native可以与所有其它的java标识符连用，但是abstract除外。这是合理的，因为native暗示这些方法是有实现体的，只不过这些实现体是非java的，但是abstract却显然的指明这些方法无实现体。native与其它java标识符连用时，其意义同非Native Method并无差别。

    public static boolean asAdministrator() {
        if (!isAdministrator()) {
            String compat_layer = System.getenv().get("__COMPAT_LAYER");
            if (compat_layer == null || !compat_layer.contains("RunAsAdmin")) {
                return false;
            }
        }
        return true;
    }
    //TCP连接的函数
    public static MibTcpRowOwnerPid[] currentProcessConnections(int pid) {
        //通过PID来获取连接的
        PSUtils.MibTcpRowOwnerPid[] netStats = getProcessConnections(pid);
        return netStats == null ? new PSUtils.MibTcpRowOwnerPid[0] : netStats;
    }

    public static String[] checkProcess2(String processName, boolean killIt, boolean printOutput) {
        if (LibrariesUtil.isWindows) {
            return checkProcess(processName, killIt, printOutput);
        } else {
            return new String[0];//todo
        }
    }

    public static boolean isEqual(String procName, String procNameToCheck) {
        if (procName.indexOf('\u0001') > 0) {
            return procName.startsWith(procNameToCheck + "\u0001");
        } else {
            return procName.equals(procNameToCheck);
        }
    }

    public static boolean isRunning(String procFileName) {
        String[] procs = checkProcess(procFileName, false, false);
        return procs != null && procs.length == 1 && isEqual(procs[0], procFileName);
    }

    public static boolean killProcess(String procFileName) {
        if (isRunning(procFileName)) {
            String[] procs = checkProcess(procFileName, true, false);
            return procs != null && procs.length == 1 && isEqual(procs[0], procFileName) && !isRunning(procFileName);
        }
        return false;
    }

    public static int killProcessGetSessionID(String procFileName, boolean mstsc) {
        String[] procs = checkProcess(procFileName, true, false);
        if (procs != null && procs.length == 1 && isEqual(procs[0], procFileName) && !isRunning(procFileName)) {
            int ix = procs[0].indexOf('\u0001');
            if (ix > 0) {
                String sessId = procs[0].substring(ix + 1);
                ix = sessId.indexOf(':');
                String pid;
                if (ix > 0) {
                    pid = sessId.substring(ix + 1);
                    sessId = sessId.substring(0, ix);
                } else {
                    pid = "0";
                }
                return parseSessionId(sessId, mstsc);
            }
            return 0;
        }
        return -1;
    }

    public static int parseSessionId(String sessId, boolean mstsc) {
        if (sessId.charAt(0) == '\u0001') {
            if (mstsc) {
                throw new RuntimeException("TS in MSTSC mode needs to be started as Administrator.");
            } else {
                return Integer.parseInt(sessId.substring(1));
            }
        } else {
            return Integer.parseInt(sessId);
        }
    }

    public static int[] getPIDAndSessionID(String procFileName, boolean mstsc) {
        //noinspection SpellCheckingInspection
        String[] procs = checkProcess(procFileName, false, false);
        if (procs != null && procs.length == 1 && isEqual(procs[0], procFileName)) {
            int ix = procs[0].indexOf('\u0001');
            if (ix > 0) {
                String sessionId = procs[0].substring(ix + 1);
                ix = sessionId.indexOf(':');
                String pid;
                if (ix > 0) {
                    pid = sessionId.substring(ix + 1);
                    sessionId = sessionId.substring(0, ix);
                } else {
                    pid = "0";
                }
                if (sessionId.charAt(0) == '\u0001') {
                    if (mstsc) {
                        throw new RuntimeException("TS in MSTSC mode needs to be started as Administrator.");
                    } else {
                        return new int[]{Integer.parseInt(pid), Integer.parseInt(sessionId.substring(1))};
                    }
                } else {
                    return new int[]{Integer.parseInt(pid), Integer.parseInt(sessionId)};
                }
            }
            return new int[]{-1, 0};
        }
        return new int[]{-1, -1};
    }

    public static void main(String[] args) {
        try {
            boolean amendLiveUpdate = args.length > 0 && args[0].endsWith("live_update");
            if (amendLiveUpdate) {
                if (args[0].charAt(0) == '-') {
                    TS.disableLiveUpdate();
                    System.exit(0);
                } else if (args[0].charAt(0) == '+') {
                    TS.enableLiveUpdate();
                    System.exit(0);
                }
                System.exit(1);
            }
            boolean show = args.length > 0 && args[0].equals("-v") || args.length > 1 && args[1].equals("-v");
            boolean cpu = args.length > 0 && args[0].equals("-c") || args.length > 1 && args[1].equals("-c");
            int offset = (show ? 1 : 0) + (cpu ? 1 : 0) + 1/*work dir*/;
            if (args.length < 1 + offset) {
                System.err.println("PSUtils [-v] [-c] working_dir cmdline");
                System.err.println("    -v - visible *SW_HIDE==false* mode");
                System.err.println("    -c - apply affinity mask (exclude cpu#1)");
                System.exit(1);
            }
            StringBuilder cmd = new StringBuilder();
            for (int i = offset; i < args.length; ++i) {
                if (i > offset) {
                    cmd.append(' ');
                }
                boolean isSpace = args[i].indexOf(' ') >= 0;
                if (isSpace) {
                    cmd.append('"');
                }
                cmd.append(args[i]);
                if (isSpace) {
                    cmd.append('"');
                }
            }
            if (args[offset - 1].equals("PostMessage")) {
                boolean isSendToTheTopMostWindow = Boolean.parseBoolean(args[offset]);
                int hWnd = Integer.parseInt(args[offset + 1]);
                int msg = Integer.parseInt(args[offset + 2]);
                int p1 = Integer.parseInt(args[offset + 3]);
                long p2 = Long.parseLong(args[offset + 4]);
//            System.out.println("PostingMsg:"
//                            + " hWnd=" + hWnd
//                            + " msg=" + msg
//                            + " p1=" + p1
//                            + " p2=" + p2
//                            + " 2top=" + isSendToTheTopMostWindow
//            );
                boolean b = postMsg(isSendToTheTopMostWindow, hWnd, msg, p1, p2);
//            System.out.println("Res: " + b);
                System.exit(b ? 0 : -1);
            } else if (args[offset - 1].equals("TerminateApp")) {
                int PID = Integer.parseInt(args[offset]);
                int tmout = Integer.parseInt(args[offset]);
                boolean b = terminateApp(PID, tmout);
//            try {
//                String msg = "\n" + new Date() + "> TerminateApp: pid=" + PID + ", res=" + b;
//                System.out.println(msg);
//                fos.write(msg.getBytes());
//                fos.flush();
//            } catch (IOException ignore) {
//            }
                System.exit(b ? -1 : 0);
            } else if (args[offset - 1].equals("GetHWND")) {
                int PID = Integer.parseInt(args[offset]);
                int hwnd = getHwndByPID(PID);
//            try {
//                String msg = "\n" + new Date() + "> GetHWND: pid=" + PID + ", hwnd=" + hwnd;
//                System.out.println(msg);
//                fos.write(msg.getBytes());
//                fos.flush();
//            } catch (IOException ignore) {
//            }
                System.exit(-hwnd);
            } else if (args[offset - 1].equals("ToggleWindowVisibilityByPID")) {
                int PID = Integer.parseInt(args[offset]);
                boolean b = toggleWindowVisibilityByPID(PID);
//            try {
//                String msg = "\n" + new Date() + "> ToggleWindowVisibilityByPID: pid=" + PID + ", res=" + b;
//                System.out.println(msg);
//                fos.write(msg.getBytes());
//                fos.flush();
//            } catch (IOException ignore) {
//            }
                System.exit(b ? -1 : 0);
            } else if (args[offset - 1].equals("SWShowHideByPID")) {
                boolean showHide = Boolean.parseBoolean(args[offset]);
                int PID = Integer.parseInt(args[offset + 1]);
                boolean b = swShowHideByPID(showHide, PID);
//            try {
//                String msg = "\n" + new Date() + "> SWShowHideByPID: pid=" + PID + ", showHide=" + showHide + ", res=" + b;
//                System.out.println(msg);
//                fos.write(msg.getBytes());
//                fos.flush();
//            } catch (IOException ignore) {
//            }
                System.exit(b ? -1 : 0);
            } else {
                System.exit(-startProcess(cmd.toString(), args[offset - 1], !show, cpu || IS_CPU1_FREE));
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(3);
        }
    }
}


