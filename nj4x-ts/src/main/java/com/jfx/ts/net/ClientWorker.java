package com.jfx.ts.net;

import com.jfx.ts.io.Log4JUtil;
import com.jfx.ts.io.PSUtils;
import com.jfx.ts.net.ws.dto.Nj4xClientInfo;
import com.jfx.ts.net.ws.dto.Nj4xMT4Account;
import com.jfx.ts.net.ws.dto.Nj4xParams;
import com.jfx.ts.xml.DOMUtil;
import org.w3c.dom.Document;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 每个连线终端的工作
 * User: roman
 * Date: 05/08/2014
 * Time: 21:54
 */
public class ClientWorker {
    public static final int MONITOR_TIME_SECONDS = 300;
    static final String PROXY_SERVER;
    static final String PROXY_TYPE;
    static final String PROXY_LOGIN;
    static final String PROXY_PASSWORD;
    static final Hashtable initTerms = new Hashtable();
    private final static ConcurrentHashMap<String, Set<String>> brokersByHosts = new ConcurrentHashMap<>();  //代理MAP，使用host来识别的
    private final static ConcurrentHashMap<Integer, ProcessToFollow> fpMap = new ConcurrentHashMap<>();   //不知道是什么Map
    //它是一个基于链接节点的无界线程安全队列。该队列的元素遵循先进先出的原则。头是最先加入的，尾是最近加入的。
//    插入元素是追加到尾上。提取一个元素是从头提取
    private final static ConcurrentLinkedQueue<ProcessToFollow> fpQueue = new ConcurrentLinkedQueue<>();  //不知道是什么队列
    private final static Set<String> fpBrokers = Collections.synchronizedSet(new HashSet<String>()); // brokers followed for 5 minutes 超过5min的代理

    static {
        PROXY_SERVER = System.getenv("JFX_PROXY_SERVER");
        PROXY_TYPE = System.getenv("JFX_PROXY_TYPE");
        PROXY_LOGIN = System.getenv("JFX_PROXY_LOGIN");
        PROXY_PASSWORD = System.getenv("JFX_PROXY_PASSWORD");
    }

    static {

        TS.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            //日志的压缩
            {
                try {
                    Path log = Paths.get(TS.JFX_HOME).resolve("log").resolve("jfx_term_hosts.log");
                    if (log.toFile().length() > 1024 * 1024) {
                        Files.move(log, log.getParent().resolve("jfx_term_hosts.log.1"), StandardCopyOption.REPLACE_EXISTING);
                    }
                    Files.write(log,
                            (
                                    "\r\n\r\n----------------- ------------------- ------------------- -----------------"
                                            + "\r\n----------------- STARTUP (" + new Date() + ") -----------------"
                                            + "\r\n----------------- ------------------- ------------------- -----------------\r\n"
                            ).getBytes(),
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    TS.LOGGER.error("Error opening jfx_term_hosts.log", e);
                }
            }

            String lastReport = null;

            //这个应该是负责通信的功能
            public void run() {
                boolean someBrokersFinished = false;//是不是有代理结束连接了
                //ProcessToFollow这个类是记录代理的，应该是这样的
                ArrayList<ProcessToFollow> putBack = new ArrayList<>();//这个对象负责要回退的对象吧
                ProcessToFollow processToFollow;
                //poll 获取并移除此队列的头 ，如果此队列为空，则返回 null
                while ((processToFollow = fpQueue.poll()) != null) {
                    if (processToFollow.totalTime() > MONITOR_TIME_SECONDS) { //存在的时间大于300，监视的时间300秒
                        someBrokersFinished = true;
                        fpMap.remove(processToFollow.pid);//去除
                        fpBrokers.add(processToFollow.brokerId);//增加
                        continue;
                    }
                    //如果不存在这个东西
                    if (!fpBrokers.contains(processToFollow.brokerId)) {
                        //应该是C++通过底层的PID来找到网络连接信息的，原理就是netstat
                        PSUtils.MibTcpRowOwnerPid[] netStats = PSUtils.currentProcessConnections(processToFollow.pid);
                        if (netStats.length == 0) {
                            someBrokersFinished = true;
                            fpMap.remove(processToFollow.pid);//建立连接失败
                            continue;
                        }
                        putBack.add(processToFollow);//回退这个添加目前的工作进程、
                        //遍历这个连接的信息
                        for (PSUtils.MibTcpRowOwnerPid ns : netStats) {
                            String remoteAddress = ns.getRemoteAddress();//获取远程连接地址
                            if (!remoteAddress.equals(processToFollow.jfxHost)) {
                                String hostPort = remoteAddress + ":" + ns.remotePort;
                                //
                                Set<String> brokers = brokersByHosts.get(hostPort);//根据远程地址存储代理节点
                                if (brokers == null) {
                                    Set<String> _b = Collections.synchronizedSet(new HashSet<String>());
                                    if ((brokers = brokersByHosts.putIfAbsent(hostPort, _b)) == null) {
                                        brokers = _b;
                                    }
                                }
                                brokers.add(processToFollow.brokerId);//这一步不知道干嘛的
                            }
                        }
                    }
                }
                for (ProcessToFollow p : putBack) fpQueue.offer(p);//在尾部添加
                //
                if (someBrokersFinished) {//someBrokersFinished的意思是代理连接完成，可以传输数据了，我感觉是这样的
                    String report = getReport();//不知道这个repot是干嘛的，貌似是生成日志的
                    if (lastReport == null || !lastReport.equals(report)) {
                        try {
                            TS.LOGGER.info("Updating jfx_term_hosts.log");
                            Files.write(Paths.get(TS.JFX_HOME).resolve("log").resolve("jfx_term_hosts.log"),
                                    ("\r\n------------- " + brokersByHosts.size() + " Hosts (" + new Date() + ") -------------\r\n" + report).getBytes(), StandardOpenOption.APPEND);
                            lastReport = report;
                        } catch (IOException e) {
                            TS.LOGGER.error("Error appending jfx_term_hosts.log", e);
                        }
                    }
                }
            }

            public String getReport() {
                StringBuilder sb = new StringBuilder();
                Set<Map.Entry<String, Set<String>>> entries = brokersByHosts.entrySet();
                //noinspection unchecked
                Map.Entry<String, Set<String>>[] hb = new Map.Entry[entries.size()];
                entries.toArray(hb);
                Arrays.sort(hb, new Comparator<Map.Entry<String, Set<String>>>() {
                    @Override
                    public int compare(Map.Entry<String, Set<String>> t1, Map.Entry<String, Set<String>> t2) {
                        int cmp = t1.getValue().size() - t2.getValue().size();
                        if (cmp == 0) {
                            cmp = t1.getKey().compareTo(t2.getKey());
                        }
                        return cmp;
                    }
                });
                for (Map.Entry<String, Set<String>> e : hb) {
                    String host = e.getKey();
                    Set<String> brokers = e.getValue();
                    if (sb.length() > 0) {
                        sb.append("\r\n");
                    }
                    sb.append(host);
                    for (int i = host.length(); i < 28; ++i) {
                        sb.append(' ');
                    }
                    sb.append(" used by ");
                    for (int i = String.valueOf(brokers.size()).length(); i < 5; ++i) {
                        sb.append(' ');
                    }
                    sb.append(brokers.size());
                    sb.append(" broker").append(brokers.size() > 1 ? "s" : " ").append(" -> ");
                    sb.append(brokers);
                }
                sb.append("\r\n");
                return sb.toString();
            }
        }, 30, 10, TimeUnit.SECONDS);
    }

    public long token;
    protected TS ts;
    protected Nj4xClientInfo cInfo;
    private String savedThreadName;
    private long threadUseStart;
    private String threadUseName;

    public ClientWorker(TS ts) {
        this.ts = ts;
        cInfo = null;
    }

    ClientWorker(TS ts, long token, Nj4xClientInfo cInfo) {
        this.ts = ts;
        this.token = token;
        this.cInfo = cInfo;
        this.threadUseStart = System.currentTimeMillis();//线程的开始时间
    }

    static String getSymbolsDir(String rootDir) /*throws IOException */ {
        try {
            return getSymbolsDir(rootDir, false);
        } catch (IOException e) {
            TS.LOGGER.error("Unexpected getSymbolsDir(" + rootDir + ") error", e);
            return null;
        }
    }

    static String getSymbolsDir(String rootDir, boolean throwException) throws IOException {
        File hist = new File(rootDir + "/history");
        if (hist.exists()) {
            int max = throwException ? 10 : 1;
            for (int t = 0; t < max; t++) {
                if (t > 0) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignore) {
                        break;
                    }
                }
                File[] files = hist.listFiles();
                if (files != null) {
                    //noinspection ForLoopReplaceableByForEach
                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        if (file.isDirectory()) {
                            String pathname = file.getAbsolutePath() + "/symbols.raw";
                            File symRawFile = new File(pathname);
                            if (symRawFile.exists() && symRawFile.length() > 0) {
                                return file.getAbsolutePath();
                            }
                        }
                    }
                }
            }
            if (throwException) {
                throw new IOException("symbols.raw not found in '" + rootDir + "/history'");
            }
        }
        if (throwException) {
            throw new IOException("Directory '" + rootDir + "/history' does not exist");
        }
        return null;
    }

    static ArrayList<String> loadSymbols(String dir) throws IOException {
        String rowSymDir = getSymbolsDir(dir, true);
        if (rowSymDir != null) {
            String pathname = rowSymDir + "/symbols.raw";
            if (new File(pathname).exists()) {
                ArrayList<String> groups = new ArrayList<>();
                //
                //
                RandomAccessFile raf = null;
                try {
                    raf = new RandomAccessFile(rowSymDir + "/symgroups.raw", "r");
//                    MappedByteBuffer bb = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, raf.length());
                    //
                    long len = raf.length();
                    byte[] grp = new byte[16];
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < len; j += 0x50) {
                        raf.seek(j);
                        raf.read(grp);
                        sb.setLength(0);
                        //noinspection ForLoopReplaceableByForEach
                        for (int k = 0; k < grp.length; k++) {
                            byte b = grp[k];
                            if (b == 0) {
                                break;
                            } else {
                                sb.append((char) b);
                            }
                        }
                        //
                        if (sb.length() > 0) {
                            groups.add(sb.toString());
                        }
                    }
                    //
                    if (groups.size() == 0) {
                        groups.add("All");
                    }
                } finally {
                    if (raf != null) {
                        raf.close();
                    }
                }
                //
                ArrayList<String> symbols = new ArrayList<>();
                raf = null;
                try {
                    raf = new RandomAccessFile(pathname, "r");
                    long len = raf.length();
                    byte[] sym = new byte[12];
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < len; j += 0x790) {
                        raf.seek(j);
                        raf.read(sym);
                        sb.setLength(0);
                        //noinspection ForLoopReplaceableByForEach
                        for (int k = 0; k < sym.length; k++) {
                            byte b = sym[k];
                            if (b == 0) {
                                break;
                            } else {
                                sb.append((char) b);
                            }
                        }
                        //
                        raf.seek(j + 100);
                        byte b1 = raf.readByte();
                        byte b2 = raf.readByte();
                        byte b3 = raf.readByte();
                        byte b4 = raf.readByte();
                        long g = (b4 & 0xff000000) | (b3 & 0x00ff0000) | (b2 & 0x0000ff00) | (b1 & 0x000000ff);
                        sb.append(',').append(groups.get(Math.min((int) g, groups.size() - 1)));
                        //
                        symbols.add(sb.toString());
                    }
                } finally {
                    if (raf != null) {
                        raf.close();
                    }
                }
                return symbols;
            }
        }
        return null;
    }

    public static void unlockTerminalProcessing(String terminalProcessName) {
        initTerms.remove(terminalProcessName);
    }

    public static boolean lockTerminalProcessing(String terminalProcessName) {
        long start = System.currentTimeMillis();
        synchronized (initTerms) {
            while (initTerms.get(terminalProcessName) != null) {
                try {
                    long now = System.currentTimeMillis();
                    if (now - start > 15000) {
                        String msg = "Timeout waiting for [" + terminalProcessName + "] dir lock";
                        TS.LOGGER.error(msg);
                        return true;
                    }
                    initTerms.wait(1000);
                } catch (InterruptedException e) {
                    TS.LOGGER.error("Error on initTerms.wait(1000)", e);
                    return true;
                }
            }
            //
            //noinspection unchecked
            initTerms.put(terminalProcessName, terminalProcessName);
        }
        return false;
    }

    static void deleteDir(File dir) throws IOException {
        deleteDir(dir, false);
    }

    public static void deleteDir(File dir, boolean keepRootDir) throws IOException {
        deleteDir(dir, keepRootDir, null);
    }

    public static void deleteDir(File dir, boolean keepRootDir, FilenameFilter lowerCaseNameFilter) throws IOException {
        File[] files = dir.listFiles();
        if (lowerCaseNameFilter != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; files != null && i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                deleteDir(file);
            } else if (lowerCaseNameFilter == null || lowerCaseNameFilter.accept(dir, file.getName().toLowerCase())) {
                boolean b = file.delete();
                if (!b) {
                    throw new IOException("Can not delete file: " + file.getAbsolutePath());
                }
            }
        }
        if (!keepRootDir) {
            boolean b1 = dir.delete();
            if (!b1) {
                throw new IOException("Can not delete dir: " + dir.getAbsolutePath());
            }
        }
        if (files == null) {
            TS.LOGGER.warn("No files to delete, dir=" + dir.getAbsolutePath());
        }
    }

    @Override
    public String toString() {
        return cInfo.clientName;
    }

    public String runTerminal(Nj4xMT4Account account, Nj4xParams nj4xEAParams, boolean restartTerminalIfRunning) {
        try {
            return new TerminalParams(account, nj4xEAParams).runTerminal(restartTerminalIfRunning, this);
        } catch (NoSrvConnection e) {
            String m = "No connection to server: " + e;
            TS.LOGGER.error(m, e);
            return m;
        } catch (SrvFileNotFound e) {
            String m = "SRV file not found: " + e;
            TS.LOGGER.error(m, e);
            return m;
        } catch (MaxNumberOfTerminalsReached e) {
            String m = "Reached max number of terminals: " + e;
            TS.LOGGER.error(m, e);
            return m;
        } catch (InvalidUserNamePassword e) {
            String m = "Invalid user name or password: " + e;
            TS.LOGGER.error(m, e);
            return m;
        } catch (TerminalInstallationIsRequired e) {
            String m = e.getMessage();
            TS.LOGGER.error(m, e);
            return m;
        } catch (Throwable e) {
            e.printStackTrace();
            String m = "Unexpected error: " + e;
            TS.LOGGER.error(m, e);
            return m;
        }
    }

    public String checkTerminal(Nj4xMT4Account account, Nj4xParams nj4xEAParams) {
        try {
            return new TerminalParams(account, nj4xEAParams).checkTerminal(this);
        } catch (NoSrvConnection e) {
            String m = "No connection to server: " + e;
            TS.LOGGER.error(m, e);
            return m;
        } catch (SrvFileNotFound e) {
            String m = "SRV file not found: " + e;
            TS.LOGGER.error(m, e);
            return m;
        } catch (MaxNumberOfTerminalsReached e) {
            String m = "Reached max number of terminals: " + e;
            TS.LOGGER.error(m, e);
            return m;
        } catch (InvalidUserNamePassword e) {
            String m = "Invalid user name or password: " + e;
            TS.LOGGER.error(m, e);
            return m;
        } catch (TerminalInstallationIsRequired e) {
            String m = e.getMessage();
            TS.LOGGER.error(m, e);
            return m;
        } catch (Throwable e) {
            e.printStackTrace();
            String m = "Unexpected error: " + e;
            TS.LOGGER.error(m, e);
            return m;
        }
    }

    public boolean startTermCheckLogon(String dir, String terminalProcessName, TerminalParams tp, SymbolsXML symbolsXML, boolean checkLogon) throws IOException {
        String symDir = null;
        int pid = -1;
        try {
            ts.gui.startConnection(tp);
            //
            boolean ok;
            if (Log4JUtil.isConfigured() && TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Starting terminal for " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " chk?=" + checkLogon);
            }
            tp.initTerminalDirectory();
            if (!tp.isMT5/* && tp.srvIsNotIPAddress()*/) {
                createSrvFile(dir, tp);
            }
            //
            String initFileName = getIniFileName();
            tp.writeInitIni(dir, initFileName);
            if (checkLogon) {
                if (tp.isMT5) {
                    File symbolsDatFile = tp.getSymbolsFile(dir);
                    if (symbolsDatFile != null)
                        symbolsDatFile.delete();
                } else {
                    symDir = getSymbolsDir(dir);
                    if (symDir != null) {
                        File fSymDir = new File(symDir);
                        File[] syms = fSymDir.listFiles(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                String lcName = name.toLowerCase();
                                return lcName.startsWith("sym") && lcName.endsWith(".raw");
                            }
                        });
                        for (File sym : syms) {
                            sym.delete();
                        }
                    }
                }
            }
            //
            File profilesDefaultDir = new File(dir + tp.getProfilesDefault());
            deleteDir(profilesDefaultDir, true);
            new FileOutputStream(profilesDefaultDir.getAbsolutePath() + "/order.wnd").close();
            //
            if (symbolsXML != null && !tp.isTesterTerminal()) {
                tp.prepareCharts(dir, symbolsXML.getSym1(), symbolsXML.getSym2());
            }
            //
            SwHide swHide = !tp.isTesterTerminal() || tp.isCloseTerminalAtTheEnd() ? SwHide.DEFAULT : SwHide.SHOW;
            pid = ts.runTermProcessGetPID(tp, terminalProcessName, dir, symbolsXML == null, swHide);
//            pid = 0;//emulation of error
            if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
                TS.LOGGER.info("Started terminal for " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + ']');
            }
            long time = Long.parseLong(System.getProperty("jfx.term_process.wait", "5000"));
            if (pid == 0) {
                waitForProcess(terminalProcessName, time);
                pid = ts.getPID(terminalProcessName);
            }
            if (pid <= 0) {
                deleteIniFile(dir, initFileName);
                return false;
            }
            //
            if (checkLogon) {
                if (Log4JUtil.isConfigured() && TS.LOGGER.isTraceEnabled()) {
                    TS.LOGGER.trace("P1 " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + ']');
                }
//                symDir = _checkLogon(pid, tp, dir, terminalProcessName);
                int netConnections = 0;
                boolean isTermProcessHere = true;
                int fastRestartCount = 0;
                do {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (Log4JUtil.isConfigured() && TS.LOGGER.isTraceEnabled()) {
                        TS.LOGGER.trace("P2 " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + ']');
                    }
                    netConnections = PSUtils.currentProcessConnections(pid).length;//TS.netStat(pid).size();
                    if (Log4JUtil.isConfigured() && TS.LOGGER.isTraceEnabled()) {
                        TS.LOGGER.trace("P3 " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + ']');
                    }
                    if (netConnections == 0) {
                        boolean updatePID = (System.currentTimeMillis() - tp.start.getTime()) > 5000;
                        int currentPID = ts.getPID(terminalProcessName, updatePID);
                        if (Log4JUtil.isConfigured() && TS.LOGGER.isTraceEnabled()) {
                            TS.LOGGER.trace("P4 " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + ']' + " isRunning=" + PSUtils.isRunning(terminalProcessName));
                        }
                        if (currentPID <= 0 && PSUtils.isRunning(terminalProcessName)) {
                            TS.LOGGER.warn("Process " + terminalProcessName + " [PID=" + pid + "] [currentPID=" + currentPID + "] is running. updatePID=" + updatePID);
                            isTermProcessHere = true;
                        } else {
                            int maxRestarts = 1;
                            if (!(isTermProcessHere = (currentPID == pid)) && fastRestartCount < maxRestarts) {
                                if (Log4JUtil.isConfigured() && TS.LOGGER.isTraceEnabled()) {
                                    TS.LOGGER.trace("P7 " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + ']');
                                }
                                fastRestartCount++;
                                //
                                if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
//                                ts.getPID(terminalProcessName);
                                    TS.LOGGER.info("Re-Starting (#" + fastRestartCount + "/" + maxRestarts + ") terminal for " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + "] [foundPID=" + currentPID + "]");
                                }
                                //
                                ts.killProcessUngracefully(terminalProcessName);
                                pid = ts.runTermProcessGetPID(tp, terminalProcessName, dir, symbolsXML == null, swHide);
                                //
                                if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
                                    TS.LOGGER.info("Re-Started (#" + fastRestartCount + "/" + maxRestarts + ") terminal for " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + "] [foundPID=" + ts.getPID(terminalProcessName) + "]");
                                }
                                time = Long.parseLong(System.getProperty("jfx.term_process.wait", "5000"));
                                if (pid == 0) {
                                    waitForProcess(terminalProcessName, time);
                                    pid = ts.getPID(terminalProcessName);
                                }
                                isTermProcessHere = pid > 0;
                            } else {
                                if (Log4JUtil.isConfigured() && TS.LOGGER.isTraceEnabled()) {
                                    TS.LOGGER.trace("P8 " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid
                                            + "] pid=" + pid
                                            + "] currentPID=" + currentPID
                                            + "] isTermProcessHere=" + isTermProcessHere
                                            + "] fastRestartCount=" + fastRestartCount
                                            + "] maxRestarts=" + maxRestarts
                                    );
                                }
                            }
                        }
                    } else {
                        if (Log4JUtil.isConfigured() && TS.LOGGER.isTraceEnabled()) {
                            TS.LOGGER.trace("P5 " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + ']');
                        }
                    }
                } while (netConnections == 0
                        && isTermProcessHere
                        && (System.currentTimeMillis() - tp.start.getTime() <= TS.CONNECTION_TIMEOUT_MILLIS));
                //
                if (tp.isMT5) {
                    if (tp.getSymbolsFile(dir) != null) {
                        symDir = dir;
                    }
                } else {
                    if (Log4JUtil.isConfigured() && TS.LOGGER.isTraceEnabled()) {
                        TS.LOGGER.trace("P6 " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + ']');
                    }
                    symDir = getSymbolsDir(dir);
                }
                //
                if (symDir == null && netConnections == 0) {
                    if (isTermProcessHere) {
                        TS.LOGGER.error("INVALID USER/PASSWORD for PID=" + pid + ", " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " (5?=" + tp.isMT5 + ") [timeout=" + (System.currentTimeMillis() - tp.start.getTime()) + "]");
                    } else {
                        TS.LOGGER.error("Max number of terminals reached: PID=" + pid + ", " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " (5?=" + tp.isMT5 + ") [time=" + (System.currentTimeMillis() - tp.start.getTime()) + "]");
                    }
                    deleteIniFile(dir, getIniFileName());
                    if (isTermProcessHere) {
                        ts.killProcess(terminalProcessName, true);
                        throwInvalidUserNamePasswordException(pid, tp, "timeout=", "" + TS.CONNECTION_TIMEOUT_MILLIS + ")");
                    } else {
                        throw new MaxNumberOfTerminalsReached(tp.user + '/' + tp.srv);
                    }
                }
            }
            return true;
        } finally {
            ts.gui.endConnection(tp);
            TS.LOGGER.info("startTermCheckLogon: PID=" + pid + ", status=" + (symDir == null ? "PENDING" : (pid > 0 ? "SUCCESS" : "PROCESS_ERROR")) + ", time=" + (System.currentTimeMillis() - tp.start.getTime()) + "mls, symDir=" + symDir + ", terminal=" + terminalProcessName);
        }
    }

    public String getIniFileName() {
        return "init.ini";
    }

    public boolean checkStatus(HashMap<Integer, Set<PSUtils.MibState>> portStatuses, Integer port, PSUtils.MibState status) {
        Set<PSUtils.MibState> statuses = portStatuses.get(port);
        return statuses != null && statuses.contains(status);
    }

/*
    private ArrayList<String> getNetStats_old(int pid, HashMap<String, Set<String>> hostStatuses, HashMap<String, Set<String>> portStatuses, HashMap<String, Set<String>> statusHosts) throws IOException {
        ArrayList<String> netStats = TS.netStat(pid);
        for (String ns : netStats) {
            ns = condenseSpaces(ns);
            String[] split = ns.split(" ");
            if (split.length == 5) {
                String host = split[2];
                String status = split[3];
                String port = host.split(":")[1];
                //
                Set<String> statuses = hostStatuses.get(host);
                if (statuses == null) {
                    hostStatuses.put(host, statuses = new HashSet<>());
                }
                statuses.add(status);
                //
                statuses = portStatuses.get(port);
                if (statuses == null) {
                    portStatuses.put(port, statuses = new HashSet<>());
                }
                statuses.add(status);
                //
                Set<String> hosts = statusHosts.get(status);
                if (hosts == null) {
                    statusHosts.put(status, hosts = new HashSet<>());
                }
                hosts.add(host);
            }
        }
        return netStats;
    }
*/

    public boolean checkStatus(HashMap<String, Set<PSUtils.MibState>> hostStatus, String host, PSUtils.MibState status) {
        Set<PSUtils.MibState> statuses = hostStatus.get(host);
        return statuses != null && statuses.contains(status);
    }

    public String throwInvalidUserNamePasswordException(int pid, TerminalParams tp, String host, PSUtils.MibState status) {
        throw new InvalidUserNamePassword(tp.user + " (" + tp.tenant + ", PID=" + pid + ", " + host + " " + status + ")");
    }

    public String throwInvalidUserNamePasswordException(int pid, TerminalParams tp, String host, String msg) {
        throw new InvalidUserNamePassword(tp.user + " (" + tp.tenant + ", PID=" + pid + ", " + host + " " + msg + ")");
    }

    public boolean checkTermLogon(String dir, String terminalProcessName, TerminalParams tp) {
        String symDir = null;
        final int pid = ts.getPID(terminalProcessName);
        long pidStartTime = System.currentTimeMillis();
        try {
            if (Log4JUtil.isConfigured() && TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Checking terminal logon for " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + ']');
            }
            if (pid <= 0) {
                TS.LOGGER.error("Client terminal process not found: " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + ']');
                return false;
//                throw new RuntimeException("Client terminal process not found: " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + ']');
            }
            //
            pidStartTime = ts.getPIDStartTime(pid);
            long assumeConnectionIsOkInMillis = Long.parseLong(System.getProperty("nj4x.verify_broker_connection_millis", "1500"));
            long start = System.currentTimeMillis();
            //
            String clientServer = tp.jfxHost + ":" + tp.jfxPort;
            String proxyServer = "";
            String proxyHost = "";
            String proxyPort = "";
            if (tp.proxyServer != null) {
                proxyServer = tp.proxyServer;
                String[] split = proxyServer.split(":");
                if (split.length == 2) {
                    proxyHost = split[0];
                    proxyPort = split[1];
                }
            }
            //
            // Wait for user logon
            //
            HashMap<String, Long> hostConnectionStart = new HashMap<>();
            HashMap<String, Set<PSUtils.MibState>> hostStatuses = new HashMap<>();
            HashMap<Integer, Set<PSUtils.MibState>> portStatuses = new HashMap<>();
            HashMap<PSUtils.MibState, Set<String>> statusHosts = new HashMap<>();
            big:
            do {
                hostStatuses.clear();
                portStatuses.clear();
                statusHosts.clear();
                //
                PSUtils.MibTcpRowOwnerPid[] netStats = getNetStats(tp, pid, hostStatuses, portStatuses, statusHosts);
                if (netStats.length > 0) {
                    for (PSUtils.MibState status : statusHosts.keySet()) {
                        if (status == PSUtils.MibState.ESTAB) {
                            for (String hostPort : statusHosts.get(status)) {
                                if (!hostPort.startsWith("127.0.0.1") && hostStatuses.get(hostPort).size() == 1) {
                                    String port = hostPort.split(":")[1];
                                    switch (port) {
                                        case "80":
                                        case "8080":
                                        case "1950":
                                            if (!hostPort.equals(proxyServer) || !port.equals(proxyPort)) break;
                                        default:
                                            if (!tp.isMT5 || hostPort.startsWith(tp.srv) || hostPort.equals(proxyServer) || hostPort.equals(clientServer)) {
                                                Long startTime = hostConnectionStart.get(hostPort);
                                                if (startTime == null) {
                                                    hostConnectionStart.put(hostPort, /*start = */startTime = System.currentTimeMillis());
                                                }
                                                long t = System.currentTimeMillis() - startTime;
                                                if (TS.LOGGER.isDebugEnabled()) {
                                                    TS.LOGGER.debug("Assuming PID=" + pid + ", " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')'
                                                            + " (5?=" + tp.isMT5 + ") connection (" + hostPort + " " + status
                                                            + ") is OK, time=" + t);
                                                }
                                                if (t > assumeConnectionIsOkInMillis) {
                                                    symDir = dir;
                                                    break big;
                                                }
                                            }
                                    }
                                }
                            }
                        } else if (status == PSUtils.MibState.FIN_WAIT1 || status == PSUtils.MibState.FIN_WAIT2) {
                            for (String hostPort : statusHosts.get(status)) {
                                hostConnectionStart.remove(hostPort);
                                Integer port = Integer.parseInt(hostPort.split(":")[1]);
                                switch (port) {
                                    case 80:
                                    case 8080:
                                    case 1950:
                                        if (!hostPort.equals(proxyServer) || !port.equals(proxyPort)) break;
                                    default:
                                        if (!checkStatus(portStatuses, port, PSUtils.MibState.ESTAB)
                                                && !checkStatus(portStatuses, port, PSUtils.MibState.SYN_SENT)
                                                && !checkStatus(portStatuses, 1950, PSUtils.MibState.ESTAB)
                                                && !checkStatus(hostStatuses, proxyServer, PSUtils.MibState.ESTAB)
                                                && !checkStatus(hostStatuses, tp.jfxHost + ":" + tp.jfxPort, PSUtils.MibState.ESTAB)
                                                && !checkStatus(portStatuses, 443, PSUtils.MibState.ESTAB)
                                                ) {
                                            //
                                            if (!tp.isMT5) {
                                                TS.LOGGER.error("INVALID USER/PASSWORD for PID=" + pid + ", " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " (5?=" + tp.isMT5 + ") [NETSTAT=" + format(netStats) + "] port=[" + port + ']');
                                                ts.killProcess(terminalProcessName, true);
                                                deleteIniFile(dir, getIniFileName());
                                                throwInvalidUserNamePasswordException(pid, tp, hostPort, status);
                                                //                                        break big;
                                            } else if (hostPort.startsWith(tp.srv)) {
                                                TS.LOGGER.error("INVALID USER/PASSWORD for PID=" + pid + ", " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " (5?=" + tp.isMT5 + ") [NETSTAT=" + format(netStats) + ']');
                                                ts.killProcess(terminalProcessName, true);
                                                deleteIniFile(dir, getIniFileName());
                                                throwInvalidUserNamePasswordException(pid, tp, hostPort, status);
                                                //                                        break big;
                                            }
                                        }
                                }
                            }
                        }
                    }
                } else if ((System.currentTimeMillis() - pidStartTime) > 60000) {
                    String hostPort = "";
                    PSUtils.MibState status = PSUtils.MibState.NO_CONNECTIONS_DETECTED;
                    if (!tp.isMT5) {
                        TS.LOGGER.error("INVALID USER/PASSWORD for PID=" + pid + ", " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " (5?=" + tp.isMT5 + ") [NETSTAT=" + format(netStats) + ']');
                        ts.killProcess(terminalProcessName, true);
                        deleteIniFile(dir, getIniFileName());
                        throwInvalidUserNamePasswordException(pid, tp, hostPort, status);
                        //                                        break big;
                    } else {
                        TS.LOGGER.error("INVALID USER/PASSWORD for PID=" + pid + ", " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " (5?=" + tp.isMT5 + ") [NETSTAT=" + format(netStats) + ']');
                        ts.killProcess(terminalProcessName, true);
                        deleteIniFile(dir, getIniFileName());
                        throwInvalidUserNamePasswordException(pid, tp, hostPort, status);
                        //                                        break big;
                    }
                }
                //
                if (tp.isMT5) {
                    if (tp.getSymbolsFile(dir) != null) {
                        symDir = dir;
                        break;
                    }
                } else {
                    symDir = getSymbolsDir(dir);
                    if (symDir != null) {
                        break;
                    }
                }
                //
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
            }
            while (System.currentTimeMillis() - start <= assumeConnectionIsOkInMillis * 2);
            //
            boolean isTimeout = System.currentTimeMillis() - pidStartTime > TS.CONNECTION_TIMEOUT_MILLIS;
            if (symDir == null && isTimeout) {
                TS.LOGGER.error("INVALID USER/PASSWORD for PID=" + pid + ", " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " (5?=" + tp.isMT5 + ") [timeout=" + (System.currentTimeMillis() - pidStartTime) + "] NETSTAT=[" + format(getNetStats(tp, pid, hostStatuses, portStatuses, statusHosts)) + "]");
                ts.killProcess(terminalProcessName, true);
                deleteIniFile(dir, getIniFileName());
                throwInvalidUserNamePasswordException(pid, tp, "timeout=", "" + (System.currentTimeMillis() - pidStartTime) + ")");
            } else if (isTimeout) {
                String message = "Timeout connecting PID=" + pid + ", " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " (5?=" + tp.isMT5 + ") [timeout=" + (System.currentTimeMillis() - pidStartTime) + "] NETSTAT=[" + format(getNetStats(tp, pid, hostStatuses, portStatuses, statusHosts)) + "]";
                TS.LOGGER.error(message);
                ts.killProcess(terminalProcessName, true);//todo leave terminal running
                deleteIniFile(dir, getIniFileName());
                throw new RuntimeException(message);
            }
            return true;//symDir != null || registeredTerminalPID != null
        } finally {
            TS.LOGGER.info("checkTermLogon: PID="
                    + pid + ",  status=" + (symDir == null ? "FAILURE" : (System.currentTimeMillis() - pidStartTime > TS.CONNECTION_TIMEOUT_MILLIS ? "TIMEOUT" : "SUCCESS"))
                    + ", time=" + (System.currentTimeMillis() - tp.start.getTime())
                    + "mls, total=" + (System.currentTimeMillis() - pidStartTime)
                    + "mls, symDir=" + symDir + ", terminal=" + terminalProcessName);
        }
    }

    private PSUtils.MibTcpRowOwnerPid[] getNetStats(TerminalParams tp, int pid, HashMap<String, Set<PSUtils.MibState>> hostStatuses, HashMap<Integer, Set<PSUtils.MibState>> portStatuses, HashMap<PSUtils.MibState, Set<String>> statusHosts) /*throws IOException */ {
        PSUtils.MibTcpRowOwnerPid[] netStats = PSUtils.currentProcessConnections(pid);
        for (PSUtils.MibTcpRowOwnerPid ns : netStats) {
            String remoteAddress = ns.getRemoteAddress();
            String hostPort = remoteAddress + ":" + ns.remotePort;
            //
            if ((tp.proxyServer == null || tp.proxyServer.length() == 0) && !fpBrokers.contains(tp.getSrv())) {
                ProcessToFollow processToFollow = new ProcessToFollow(pid, tp.getSrv(), tp.jfxHost);
                if (fpMap.putIfAbsent(pid, processToFollow) == null) {
                    fpQueue.offer(processToFollow);
                }
            }
            //
            PSUtils.MibState status = ns.getState();
            int port = ns.remotePort;
            //
            Set<PSUtils.MibState> statuses = hostStatuses.get(hostPort);
            if (statuses == null) {
                hostStatuses.put(hostPort, statuses = new HashSet<>());
            }
            statuses.add(status);
            //
            statuses = portStatuses.get(port);
            if (statuses == null) {
                portStatuses.put(port, statuses = new HashSet<>());
            }
            statuses.add(status);
            //
            Set<String> hosts = statusHosts.get(status);
            if (hosts == null) {
                statusHosts.put(status, hosts = new HashSet<>());
            }
            hosts.add(hostPort);
        }
        return netStats;
    }

    private String format(PSUtils.MibTcpRowOwnerPid[] netStats) {
        StringBuilder sb = new StringBuilder();
        for (PSUtils.MibTcpRowOwnerPid s : netStats) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private String condenseSpaces(String ns) {
        String prev = ns.replace("\t", " ");
        ns = prev.replace("  ", " ");
        while (!prev.equals(ns)) {
            prev = ns;
            ns = ns.replace("  ", " ");
        }
        return ns.trim();
    }

    private boolean isUpToDate(String dir, TerminalParams tp) {
        String librariesSubdir = tp.MT4IfDll().startsWith("mt4if") ? "/experts/libraries/"
                : (tp.isMT5 ? "/MQL5/Libraries/" : "/MQL4/Libraries/");
        File dll = new File(dir + librariesSubdir + tp.MT4IfDll());
        String dllFileName = TS.IS_WIN_XP ? tp.MT4IfXPDll() : tp.MT4IfDll();
        File dll0 = new File((tp.isMT5 ? TS.JFX_HOME_ZTERM_MT5_DIR : TS.JFX_HOME_ZTERM_DIR) + librariesSubdir + (tp.is64BitTerminal ? TerminalParams.toX64DLLName(dllFileName) : dllFileName));
        return dll.exists() && dll.length() == dll0.length() && dll.lastModified() >= dll0.lastModified();
    }

    public boolean isOkAndRunning(String terminalProcessName, TerminalParams terminalParams, boolean longRun) throws IOException {
        if (longRun) {
            if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
                TS.LOGGER.info("Killing terminal for " + terminalParams.user + '/' + terminalParams.srv);
            }
            ts.killProcessUngracefully(terminalProcessName);
        } else if (checkTermConnection(terminalProcessName, terminalParams, true)) {
            if (TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("OK, running: " + terminalProcessName);
            }
            return true;
        }
        return false;
    }

    public boolean checkTermConnection(String terminalProcessName, TerminalParams tp, boolean updatePIDs) throws IOException {
        final int pid = ts.getPID(terminalProcessName, updatePIDs);
        long pidStartTime = System.currentTimeMillis();
        String netStatsInfo = "<hidden>";
        String clientServer = tp.jfxHost + ":" + tp.jfxPort;
        clientServer = clientServer.equals(":") ? ("127.0.0.1:" + (ts.getPort() + 3)) : clientServer;
        boolean status = false;
        try {
            if (Log4JUtil.isConfigured() && TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Checking terminal connections for " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + ']');
            }
            if (pid <= 0) {
                if (Log4JUtil.isConfigured() && TS.LOGGER.isDebugEnabled()) {
                    TS.LOGGER.debug("Client terminal process not found: " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + ']');
                }
                return false;
//                throw new RuntimeException("Client terminal process not found: " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " [PID=" + pid + ']');
            }
            //
            pidStartTime = ts.getPIDStartTime(pid);
            long assumeConnectionIsOkInMillis = Long.parseLong(System.getProperty("nj4x.verify_mt4_connection_millis", "8000"));
            long start = System.currentTimeMillis();
            //
            //
            // Wait for mt4 connection
            //
            HashMap<String, Set<PSUtils.MibState>> hostStatuses = new HashMap<>();
            HashMap<Integer, Set<PSUtils.MibState>> portStatuses = new HashMap<>();
            HashMap<PSUtils.MibState, Set<String>> statusHosts = new HashMap<>();
            do {
                hostStatuses.clear();
                portStatuses.clear();
                statusHosts.clear();
                //
                PSUtils.MibTcpRowOwnerPid[] netStats = getNetStats(tp, pid, hostStatuses, portStatuses, statusHosts);
                if (netStats.length > 0) {
                    if (TS.LOGGER.isDebugEnabled()) {
                        netStatsInfo = format(netStats);
                    }
                    //
                    if (checkStatus(hostStatuses, clientServer, PSUtils.MibState.ESTAB)
                            ) {
                        status = true;
                        break;
                    }
                }
                //
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
            }
            while (System.currentTimeMillis() - start <= assumeConnectionIsOkInMillis);
            //
            if (!status)
                ts.killProcessUngracefully(terminalProcessName);
            //
            return status;
        } finally {
            TS.LOGGER.info("checkTermConnections: PID="
                    + pid + ", status=" + (status ? "FOUND" : "NOT-FOUND")
                    + ", time=" + (System.currentTimeMillis() - tp.start.getTime())
                    + "mls, terminal=" + terminalProcessName
                    + ", clientServer=[" + clientServer
                    + "] NETSTAT=[" + netStatsInfo + "]"
            );
        }
    }

    public void waitForProcess(String terminalProcessName, long waitProcess) {
        if (Log4JUtil.isConfigured() && TS.LOGGER.isDebugEnabled()) {
            TS.LOGGER.debug("Waiting " + waitProcess + " millis");
        }
        long start = System.currentTimeMillis();
        do {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        } while (System.currentTimeMillis() - start <= waitProcess && !PSUtils.isRunning(terminalProcessName));
    }

    String getSymbolsXMLFileName(TerminalParams terminalParams) {
        return TS.getTermDir() + terminalParams.getTerminalDirectoryNoTenantLastName() + "/config/" + terminalParams.srv + ".sym";
    }

    public String stopTerminal(Nj4xMT4Account account, Nj4xParams nj4xEAParams, boolean kill) {
        return stopTerm(new TerminalParams(account, nj4xEAParams), kill, false);
    }

    protected String stopTerminal(String line, boolean kill) throws IOException {
        return stopTerm(new TerminalParams(line.substring(ClientWorkerThread.STOPTERM.length())), kill, kill);
    }

    public String stopTerm(TerminalParams terminalParams, boolean kill, boolean immediately) {
        final String dir = TS.getTermDir() + terminalParams.getTerminalDirectoryLastName();
        //
        final String processName = dir + "\\terminal.exe";
        //
        boolean b = true;
        try {
            b = lockTerminalProcessing(processName);
            //
            if (PSUtils.isRunning(processName)) {
                if (kill) {
                    if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
                        TS.LOGGER.info("Killing terminal " + terminalParams.user + '/' + terminalParams.srv + " process=" + processName);
                    }
                    ts.killProcessUngracefully(processName);
                } else {
                    if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
                        TS.LOGGER.info("Stopping terminal " + terminalParams.user + '/' + terminalParams.srv + " immediately=" + immediately + " process=" + processName);
                    }
                    //
                    if (immediately) {
                        ts.killProcess(processName);
                    } else {
                        ScheduledFuture schedule = TS.scheduledExecutorService.schedule(new Runnable() {
                            public void run() {
                                if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
                                    TS.LOGGER.info("Timer: Stop terminal " + processName);
                                }
                                ts.killProcess(processName, true);
                            }
                        }, TS.JFX_TERM_IDLE_TMOUT_SECONDS, TimeUnit.SECONDS);
                        TS.terminations.put(processName, schedule);
                    }
                }
            }
            //
            return "OK";
        } finally {
            if (!b) {
                unlockTerminalProcessing(processName);
            }
            if (kill || immediately) {
                TS.scheduledExecutorService.schedule(new Runnable() {
                    public void run() {
                        ts.updateTerminals();
                    }
                }, 2, TimeUnit.SECONDS);
            }
        }
    }

    protected String getSymbols(String line) throws IOException {
        TerminalParams terminalParams = new TerminalParams(line.substring(ClientWorkerThread.GETSYMBOLS.length()));
        //
        final String dir = TS.getTermDir() + terminalParams.getTerminalDirectoryLastName();
        //
        //
        String symFileName = getSymbolsXMLFileName(terminalParams);
        File symFile = new File(symFileName);
        if (symFile.exists()) {
            Document doc = DOMUtil.getDocument(symFileName);
            return DOMUtil.serializeDocumentToString(doc).replace("\r", "").replace("\n", "");
        } else {
            StringBuilder sb = new StringBuilder();
            ArrayList<String> list = loadSymbols(dir);
            if (list == null) {
                throw new RuntimeException("Invalid GetSymbols Request: " + line);
            }
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < list.size(); i++) {
                String s = list.get(i);
                if (sb.length() > 0) {
                    sb.append('|');
                }
                sb.append(s);
            }
            return sb.toString();
        }
    }

    protected String getSRV() throws IOException {
        StringBuilder sb = new StringBuilder();
        ArrayList<String> srvFilesList = getSRVFilesList();
        for (int i = 0; i < srvFilesList.size(); i++) {
            String s = srvFilesList.get(i);
            if (sb.length() > 0) {
                sb.append('|');
            }
            sb.append(s);
        }
        if (Log4JUtil.isConfigured() && TS.LOGGER.isTraceEnabled()) {
            TS.LOGGER.trace("Client " + cInfo.clientName + " got SRV list: " + sb);
        }
        //
        return sb.toString();
    }

    public ArrayList<String> getSRVFilesList() throws IOException {
        ArrayList<String> list = new ArrayList<>();
        final String dir = TS.JFX_HOME_SRV_DIR + "\\";
        File[] files = new File(dir).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".srv");
            }
        });
        for (File file : files) {
            String name = file.getName();
            list.add(name.substring(0, name.length() - 4));
        }
        if (Log4JUtil.isConfigured() && TS.LOGGER.isTraceEnabled()) {
            TS.LOGGER.trace("Client " + cInfo.clientName + " got SRV list: " + list);
        }
        //
        return list;
    }

    protected int countTerminals() throws IOException {
        int cnt = ts.getTerminals().size();
        if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
            TS.LOGGER.info("Client " + cInfo.clientName + " found " + cnt + " terminal(s)");
        }
        return cnt;
    }

    private static class CountTermsExpToken {
        public int token;
        public long startTime;

        public CountTermsExpToken(int token) {
            this.token = token;
            startTime = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - startTime > 5;
        }
    }

    private static final AtomicInteger countTermsToken = new AtomicInteger(100000);
    private static final HashMap<String, List<CountTermsExpToken>> countTermsTokens = new HashMap<>();

    protected int[] countTerminals(String broker, String account) throws IOException {
        String brokerPrefix = TerminalParams.brokerToDirName(broker) + ' ';
        String accountPrefix = brokerPrefix + account + ' ';
        int token = countTermsToken.incrementAndGet();
        int brokerTerms = 0;
        int accountTerms = 0;
        int total = 0;
        synchronized (countTermsTokens) {
            List<CountTermsExpToken> accountExpTokens = countTermsTokens.get(accountPrefix);
            List<CountTermsExpToken> brokerExpTokens = countTermsTokens.get(brokerPrefix);
            if (accountExpTokens == null) {
                countTermsTokens.put(accountPrefix, accountExpTokens = new ArrayList<>());
            }
            if (brokerExpTokens == null) {
                countTermsTokens.put(brokerPrefix, brokerExpTokens = new ArrayList<>());
            }
            removeExpired(accountExpTokens);
            removeExpired(brokerExpTokens);
            accountExpTokens.add(new CountTermsExpToken(token));
            brokerExpTokens.add(new CountTermsExpToken(token));
            accountTerms = accountExpTokens.size() - 1;
            brokerTerms = brokerExpTokens.size() - 1;
        }
        //
        if (TS.managedTerminalsRegistry == null) {
            ArrayList<String> terminals = ts.getTerminals();
            total = terminals.size();
            for (String term : terminals) {
                int endIndex = term.indexOf("\\terminal.exe");
                endIndex = endIndex > 0 ? endIndex : term.indexOf("\\terminal64.exe");
                String tDir = term.substring(0, endIndex);
                tDir = tDir.substring(tDir.lastIndexOf('\\') + 1);
                if (tDir.startsWith(accountPrefix)) {
                    accountTerms++;
                    brokerTerms++;
                } else if (tDir.startsWith(brokerPrefix)) {
                    brokerTerms++;
                }
            }
        } else {
            ArrayList<TS.IManagedTerminal> terminals = new ArrayList<>(TS.managedTerminalsRegistry.values());
            total = terminals.size();
            boolean isManagedTermExists = false;
            for (TS.IManagedTerminal term : terminals) {
                String tDir = term.getModule().substring(0, term.getModule().indexOf("\\terminal.exe"));
                tDir = tDir.substring(tDir.lastIndexOf('\\') + 1);
                if (tDir.startsWith(accountPrefix)) {
                    isManagedTermExists = true;
                    accountTerms += term.getNumberOfClientConnections();
                    brokerTerms++;
                } else if (tDir.startsWith(brokerPrefix)) {
                    brokerTerms++;
                }
            }
            if (isManagedTermExists && accountTerms == 0) {
                accountTerms = 99;
            }
        }
        //
        if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
            TS.LOGGER.info("Found "
                    + total + " total, "
                    + brokerTerms + " broker (" + broker + "), "
                    + accountTerms + " account (" + account + ") terminals."
            );
        }
        return new int[]{token, total, brokerTerms, accountTerms};
    }

    private void removeExpired(List<CountTermsExpToken> accountExpTokens) {
        for (Iterator<CountTermsExpToken> i = accountExpTokens.iterator(); i.hasNext(); ) {
            if (i.next().isExpired()) {
                i.remove();
            }
        }
    }

    public void deleteIniFile(String dir, String fileName) {
        //noinspection EmptyCatchBlock
        try {
            new File(dir + "/" + fileName).delete();
        } catch (Exception e) {
        }
    }

    private void createSrvFile(String dir, TerminalParams terminalParams) throws IOException {
        if (System.getProperty("copy_all_srv", "true").equals("true")) {
            ArrayList<File> allSrvFromConfig = getAllSrvFromConfig(terminalParams);
            if (allSrvFromConfig != null && allSrvFromConfig.size() > 0) {
                for (File from : allSrvFromConfig) {
                    String pathname = dir + "/config/" + from.getName();
                    File srvFile = new File(pathname);
                    if (!srvFile.exists() || from.lastModified() > srvFile.lastModified()) {
                        if (TS.LOGGER.isDebugEnabled()) {
                            TS.LOGGER.debug("Copying " + from + " to " + srvFile);
                        }
                        TS.copyFile(from, pathname);
                        //
                        //checkServerConnection(srvFile);
                    } else {
                        if (TS.LOGGER.isDebugEnabled()) {
                            TS.LOGGER.debug("Skip copying " + from + " to " + srvFile + ": exists?=" + (srvFile.exists())
                                    + ", ts1=" + from.lastModified()
                                    + ", ts2=" + srvFile.lastModified()
                            );
                        }
                    }
                }
            } else {
                if (terminalParams.srvIsNotIPAddress()) {
                    throw new SrvFileNotFound(terminalParams.srv);
                }
            }
        } else {
            String pathname = dir + "/config/" + terminalParams.srv + ".srv";
            File srvFile = new File(pathname);
            File from = getSrvFromConfig(terminalParams);
            if (!srvFile.exists() || from != null && from.lastModified() > srvFile.lastModified()) {
                createSrvFromConfig(terminalParams, pathname);
                //
                if (!srvFile.exists()) {
                    throw new SrvFileNotFound(terminalParams.srv);
                }
                //
                checkServerConnection(srvFile);
            } else {
                throw new SrvFileNotFound(terminalParams.srv);
            }
        }
    }

    private void checkServerConnection(File srvFile) throws IOException {
        RandomAccessFile raf = null;
        byte[] host = new byte[64];
        try {
            raf = new RandomAccessFile(srvFile.getAbsolutePath(), "r");
            raf.seek(0xd8);
            raf.read(host);
        } finally {
            if (raf != null) {
                raf.close();
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        //noinspection ForLoopReplaceableByForEach
        for (int k = 0; k < host.length; k++) {
            byte b = host[k];
            if (b == 0) {
                break;
            } else {
                sb.append((char) b);
            }
        }
        //
        int port = 443;
        String ip;
        int semicol = sb.indexOf(":");
        if (semicol > 0) {
            port = Integer.parseInt(sb.substring(semicol + 1));
            ip = sb.substring(0, semicol);
        } else {
            ip = sb.toString();
        }
        if (ip.length() == 0) {
            return;
        }
        try {
            //todo: cache checks
            if (TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Checking server connection: " + ip + ":" + port + " srv=" + srvFile.getAbsolutePath());
            }
            Socket srvSocket = new Socket(ip, port);
            if (srvSocket.isConnected()) {
                if (TS.LOGGER.isDebugEnabled()) {
                    TS.LOGGER.debug("Connection to " + ip + ":" + port + " is OK");
                }
                srvSocket.close();
            } else {
                throw new NoSrvConnection(ip + ":" + port);
            }
        } catch (IOException e) {
            throw new NoSrvConnection("" + ip + ":" + port + ", " + e);
        }
    }

    private File getSrvFromConfig(TerminalParams terminalParams) throws IOException {
        if (new File(TS.JFX_HOME_SRV_DIR).exists()) {
            File from = new File(TS.JFX_HOME_SRV_DIR + "/" + terminalParams.srv + ".srv");
            if (from.exists()) {
                return from;
            }
        }
        return null;
    }

    private ArrayList<File> getAllSrvFromConfig(TerminalParams terminalParams) throws IOException {
        File dir = new File(TS.JFX_HOME_SRV_DIR);
        if (dir.exists()) {
            final String prefix = (terminalParams.srv.contains("-") ? terminalParams.srv.substring(0, terminalParams.srv.indexOf('-') + 1) : terminalParams.srv).toLowerCase();
            final ArrayList<File> res = new ArrayList<>();
            if (TS.LOGGER.isTraceEnabled()) {
                TS.LOGGER.trace("Scanning " + dir + ", prefix=" + prefix);
            }
            Files.walkFileTree(dir.toPath(), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    File f = file.toFile();
                    String name = f.getName().toLowerCase();
                    if (name.startsWith(prefix) && name.endsWith(".srv")) {
                        if (TS.LOGGER.isTraceEnabled()) {
                            TS.LOGGER.trace("Added   " + f.getName());
                        }
                        res.add(f);
                    } else {
                        if (TS.LOGGER.isTraceEnabled()) {
                            TS.LOGGER.trace("Skipped " + f.getName());
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            });
            if (TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("End of scan, dir=" + dir + ", prefix=" + prefix + ", res=" + res);
            }
            return res;
        } else {
            TS.LOGGER.error(dir + " does not exists.");
        }
        return null;
    }

    private void createSrvFromConfig(TerminalParams tp, String pathname) throws IOException {
        File from = getSrvFromConfig(tp);
        TS.copyFile(from, pathname);
    }

    private void createSrvFromResource(TerminalParams terminalParams, String pathname) throws IOException {
        ZipInputStream zis = new ZipInputStream(TS.class.getResourceAsStream("resources/srv.zip"));
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
            String name = zipEntry.getName();
            //
            if (!zipEntry.isDirectory() && name.equals(terminalParams.srv + ".srv")) {
                FileOutputStream fos = new FileOutputStream(pathname);
                byte[] buf = new byte[1024];
                int cnt;
                while ((cnt = zis.read(buf)) > 0) {
                    fos.write(buf, 0, cnt);
                }
                fos.close();
                break;
            }
        }
        zis.close();
    }

    public long getLastUsageTimeIntervalMillis() {
        return System.currentTimeMillis() - threadUseStart;
    }

    public void setCurrentThreadName(String info) {
        savedThreadName = Thread.currentThread().getName();
        threadUseStart = System.currentTimeMillis();
        threadUseName = cInfo.clientName.replace(TS.NJ4X_UUID, "") + ": " + info;
        if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
            TS.LOGGER.info(threadUseName);
        }
        Thread.currentThread().setName(threadUseName);
    }

    public void restoreCurrentThreadName() {
        if (savedThreadName != null) {
            Thread.currentThread().setName(savedThreadName);
            long time = (System.currentTimeMillis() - threadUseStart) / 1000;
            if (time > 3 && Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
                TS.LOGGER.info(threadUseName + " method exit, " + savedThreadName + " has been used for " + time + " sec");
            }
            savedThreadName = null;
            threadUseName = null;
        }
    }

    //这个私有类应该是标识代理的
    private static class ProcessToFollow {
        int pid;
        String brokerId;
        long startTime;
        private String jfxHost;

        public ProcessToFollow(int pid, String brokerId, String jfxHost) {
            this.pid = pid;
            this.brokerId = brokerId;
            this.jfxHost = jfxHost;
            startTime = System.currentTimeMillis();
        }
        //计算这个代理存在了多长时间了
        public int totalTime() {
            return (int) ((System.currentTimeMillis() - startTime) / 1000);
        }
    }
}
