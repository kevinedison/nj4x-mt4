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

package com.jfx.ts.net;

import com.jfx.md5.MD5;
import com.jfx.net.TerminalClient;
import com.jfx.ts.gui.TSConfigGUI;
import com.jfx.ts.io.*;
import com.jfx.ts.jmx.JMXServer;
import com.jfx.ts.net.ws.dto.Nj4xClientInfo;
import com.jfx.ts.net.ws.dto.Nj4xInvalidTokenException;
import com.jfx.ts.net.ws.dto.Nj4xSessionExpiredException;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

/**
 * 整个TS这个类就是初始化的工具
 * User: roman
 * Date: Jun 13, 2005
 * Time: 6:55:03 PM
 */
@SuppressWarnings({"SpellCheckingInspection", "ResultOfMethodCallIgnored", "Convert2Diamond"})
public class TS {
    /**
     * The Version.
     */
    static final String version = "2.6.2";
    /**
     * The constant NJ4X.
     */
    public static final String NJ4X = version;
    /**
     * The constant NJ4X_UUID.
     */
    public static final String NJ4X_UUID = "29a50980516c";

    /**
     * The constant SPCH.
     */
    public static final String SPCH = "SPCH";
    /**
     * The constant BUILD_MT4.
     */
    public static final String BUILD_MT4 = "950";
    /**
     * The constant BUILD_MT5.
     */
    public static final String BUILD_MT5 = "1150";

    /**
     * The constant REMOTE_DESKTOP_USERS.
     */
    public static final String REMOTE_DESKTOP_USERS = "Remote Desktop Users";
    /**
     * The constant ADMINISTRATORS.
     */
    public static final String ADMINISTRATORS = "Administrators";
    /**
     * The constant USERS.
     */
    public static final String USERS = "Users";
    /**
     * The constant P_RAN_AS_SERVICE.
     */
    public static final boolean P_RAN_AS_SERVICE = System.getProperty("ran_as_service", "false").equals("true");
    /**
     * The constant P_START_GUI.
     */
    public static final boolean P_START_GUI = System.getProperty("start_gui", "true").equals("true");
    /**
     * 貌似是只是用GUI模式，待核实
     */
    public static boolean P_GUI_ONLY = System.getProperty("gui_only", "false").equals("true");
    /**
     * The constant P_USE_MSTSC.
     */
    public static boolean P_USE_MSTSC = System.getProperty("use_mstsc", "false").equals("true") && BoxUtils.BOXID == 0;
    /**
     * The P mstsc port.远程端口吧
     */
    static final String P_MSTSC_PORT = System.getProperty("mstsc_port", "3389");
    /**
     * The constant MIN_DISK_SPACE_GB.最小的硬盘大小
     */
//
    public static String MIN_DISK_SPACE_GB = System.getProperty("min_disk_space_gb", "1");
    /**
     * The constant MAX_TERMS.这个应该是最大的终端数吧，2.0.2开头的版本是160个，其他是32个终端
     */
    public static final int MAX_TERMS = version.startsWith("2.0.2") ? 160 : 32;

    /**
     * The Jfx term idle tmout seconds. 超时时间
     */
    static final long JFX_TERM_IDLE_TMOUT_SECONDS;  //21600
    /**
     * The constant JFX_HOME. JFX的路径
     */
    public static final String JFX_HOME;   //  C:\Users\Micheal\jfx_term
    /**
     * The constant hostname.
     */
    public static String hostname;    //DESKTOP-CT2L27I   计算机的名称


    /**
     * 1、静态代码块是在类加载时自动执行的，非静态代码块在创建对象自动执行的代码，不创建对象不执行该类的非静态代码块。 顺序： 静态代码块--》非静态代码块--》类构造方法。
     * 2、在静态方法里面只能直接调用同类中其他的静态成员（包括变量和方法），而不能直接访问类中的非静态成员。因为对于非静态的方法和变量，需要先创建类的实例对象后方可使用，而静态方法在使用前不用创建任何对象。
     * 3、如果某些代码必须要在项目启动时候就执行的时候，我们可以采用静态代码块，这种代码是主动执行的；需要在项目启动的时候就初始化，在不创建对象的情况下，其他程序来调用的时候，需要使用静态方法，此时代码是被动执行的。
     * 区别：静态代码块是自动执行的；静态方法是被调用的时候才执行的；
     * 作用：静态代码块可以用来初始化一些项目最常用的变量和对象；静态方法可以用作不创建对象也可以能需要执行的代码。
     */
    static {
        try {
            //不知道为什么要外部程序，不知道是干什么的，跑起来之后看看hostname是个什么鬼
            ExternalProcess p = new ExternalProcess("hostname");
            p.run();
            TS.hostname = p.getOut().trim();
        } catch (Exception e) {
            TS.LOGGER.error("hostname cmd error", e);
            TS.hostname = System.getenv("COMPUTERNAME");
        }
    }

    static {
        //貌似是初始化JFX的HOME路径，也许是
        JFX_HOME = System.getProperty("home", System.getProperty("user.home") + File.separatorChar + "jfx_term");
        System.setProperty("home", JFX_HOME);
        String tmout = System.getenv("JFX_TERM_IDLE_TMOUT_SECONDS");
        if (tmout == null) {
            JFX_TERM_IDLE_TMOUT_SECONDS = 3600 * 6;
        } else {
            JFX_TERM_IDLE_TMOUT_SECONDS = Long.parseLong(tmout);
        }
    }

    //以下都是各个模块的路径 一会儿都吧路径写到注释里面

    /**
     * The Jfx home config.
     */
    static final String JFX_HOME_CONFIG = JFX_HOME + File.separatorChar + "config";  // C:\Users\Micheal\jfx_term\config
    /**
     * The Jfx home zterm dir.
     * pathSeparatorChar是路径分隔符，在Window上是";"，在Unix上是":"
     */
    static final String JFX_HOME_ZTERM_DIR = JFX_HOME + File.separatorChar + "zero_term";  //C:\Users\Micheal\jfx_term\zero_term
    /**
     * The Jfx home zterm mt 5 dir.
     */
    static final String JFX_HOME_ZTERM_MT5_DIR = JFX_HOME + File.separatorChar + "zero_term_mt5";  // C:\Users\Micheal\jfx_term\zero_term_mt5
    /**
     * The Jfx home srv dir.
     */
    static final String JFX_HOME_SRV_DIR = JFX_HOME + File.separatorChar + "srv";  // C:\Users\Micheal\jfx_term\srv
    /**
     * The Jfx home ea dir.
     */
    static final String JFX_HOME_EA_DIR = JFX_HOME + File.separatorChar + "ea"; // C:\Users\Micheal\jfx_term\ea
    /**
     * The Jfx home experts dir.
     */
    static final String JFX_HOME_EXPERTS_DIR = JFX_HOME + File.separatorChar + "experts";// C:\Users\Micheal\jfx_term\experts
    /**
     * The Jfx home indicators dir.
     */
    static final String JFX_HOME_INDICATORS_DIR = JFX_HOME + File.separatorChar + "indicators";  //C:\Users\Micheal\jfx_term\indicators
    /**
     * The Jfx home chr dir.
     */
    static final String JFX_HOME_CHR_DIR = JFX_HOME + File.separatorChar + "chr";  //C:\Users\Micheal\jfx_term\chr
    /**
     * The constant JMX_CONFIG_XML.
     */
    public static final String JMX_CONFIG_XML = JFX_HOME_CONFIG + File.separatorChar + "mbean_config.xml";//C:\Users\Micheal\jfx_term\config\mbean_config.xml
    /**
     * The constant LOGGING_CONFIG_XML.
     */
    public static String LOGGING_CONFIG_XML = JFX_HOME_CONFIG + File.separatorChar + (P_GUI_ONLY ? "gui_" : "") + "logging.xml";//C:\Users\Micheal\jfx_term\config\logging.xml
    /**
     * The constant LOGGER.
     */
    public static final Logger LOGGER;

    /**
     * The constant TERM_DIR.   C:\.7788
     */
    public static String TERM_DIR = null;   //C:\.7788

    private static TSConfig tsConfig;

    /**
     * 这个应该是新建这些路径的静态块
     */
    static {
        //mkdir函数，如果存在就返回false
        new File(getTargetTermDir()).mkdirs();
        //
        new File(JFX_HOME_CONFIG).mkdirs();
        new File(JFX_HOME_SRV_DIR).mkdirs();
        new File(JFX_HOME_EA_DIR).mkdirs();
        new File(JFX_HOME_EXPERTS_DIR).mkdirs();
        new File(JFX_HOME_INDICATORS_DIR).mkdirs();
        new File(JFX_HOME_CHR_DIR).mkdirs();
        //
        TerminalClient terminalClient = null;
        try {
            terminalClient = new TerminalClient("127.0.0.1", Integer.parseInt(System.getProperty("port", "7788")));
            TS.P_GUI_ONLY = true;
            if (!TS.P_USE_MSTSC) {
                String mode = terminalClient.ask(ClientWorkerThread.GETMODE).toLowerCase();
                if (mode.contains("mstsc=true")) {
                    TS.P_USE_MSTSC = true;
                }
            }
        } catch (IOException e) {
            //ignore, seems this is 1st TS instance or port is used by another app
        } finally {
            if (terminalClient != null) {
                try {
                    terminalClient.close();
                } catch (IOException ignore) {
                }
            }
        }
        //
        LOGGING_CONFIG_XML = JFX_HOME_CONFIG + File.separatorChar + (P_GUI_ONLY ? "gui_" : "") + "logging.xml";
        if (!new File(LOGGING_CONFIG_XML).exists()) {
            try {
                String loggingXml = ResourceReader.getClassResourceReader(TerminalServer.class, true).getProperty("logging.xml");
                if (P_GUI_ONLY) {
                    loggingXml = loggingXml.replace("jfx_term.log", "gui_jfx_term.log");
                }
                writeFile(LOGGING_CONFIG_XML, loggingXml.replace("./jfx_term/", JFX_HOME.replace('\\', '/') + "/").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //
        if (!new File(JMX_CONFIG_XML).exists()) {
            try {
                writeFile(JMX_CONFIG_XML, ResourceReader.getClassResourceReader(TerminalServer.class).getProperty("mbean_config.xml").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //
        DOMConfigurator.configureAndWatch(LOGGING_CONFIG_XML);
        LOGGER = Logger.getLogger(TS.class);
        //
        String termDirLn = System.getenv("SystemDrive") + "\\." + System.getProperty("port", "7788");
        File tmpDirLnk = new File(termDirLn);
        if (tmpDirLnk.exists() && !P_GUI_ONLY) {
            String linkDir = getLinkDir(tmpDirLnk);
            if (linkDir == null || !new File(getTargetTermDir()).equals(new File(linkDir))) {
                tmpDirLnk.delete();
            }
        }
        if (!tmpDirLnk.exists()) {
            tmpDirLnk.delete();
            ExternalProcess mklink = new ExternalProcess("cmd", "/C", "mklink", "/J", termDirLn, getTargetTermDir());
            try {
                mklink.run();
                TERM_DIR = tmpDirLnk.exists() ? termDirLn : null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            TERM_DIR = termDirLn;
        }
        //
        tsConfig = new TSConfig();
    }

    private static String getLinkDir(File lnk) {
        ExternalProcess dir = new ExternalProcess("cmd", "/C", "dir", "/AL", lnk.getAbsolutePath() + "*");
        try {
            dir.run();
            String out = dir.getOut();
            String s = lnk.getName() + " [";
            int ix = out.indexOf(s);
            int ix2 = out.lastIndexOf(']');
            if (ix > 0 && ix2 > ix) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("GetLinkDir: " + lnk.getAbsolutePath() + " -> " + out.substring(ix + s.length(), ix2));
                }
                return out.substring(ix + s.length(), ix2);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("GetLinkDir(" + lnk.getAbsolutePath() + ") -> " + out);
                }
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * The Psexesvc.
     */
    static final String PSEXESVC = System.getenv("SystemRoot") + File.separator + "PSEXESVC.EXE";
    /**
     * The Mstsc.
     */
    static final String MSTSC = System.getenv("SystemRoot") + File.separator + "system32" + File.separatorChar + "mstsc.exe";
    /**
     * The Net.
     */
    static final String NET = System.getenv("SystemRoot") + File.separator + "system32" + File.separatorChar + "net.exe";
    /**
     * The Icacls.
     */
    static final String ICACLS = System.getenv("SystemRoot") + File.separator + "system32" + File.separatorChar + "icacls.exe";
    /**
     * The Cmdkey.
     */
    static final String CMDKEY = System.getenv("SystemRoot") + File.separator + "system32" + File.separatorChar + "cmdkey.exe";
    /**
     * The Quser.
     */
    static final String QUSER = System.getenv("SystemRoot") + File.separator + "system32" + File.separatorChar + "quser.exe";
    /**
     * The Reg.
     */
    static final String REG = System.getenv("SystemRoot") + File.separator + "system32" + File.separatorChar + "reg.exe";
    /**
     * The Wmic.
     */
    static final String WMIC = System.getenv("SystemRoot") + File.separator + "system32" + File.separator + "wbem" + File.separatorChar + "WMIC.exe";
    /**
     * The Use paexec.
     */
    static final boolean USE_PAEXEC = System.getProperty("use_paexec", "false").equals("true");
    /**
     * The Psexec.
     */
    static final String PSEXEC = "\"" + System.getProperty("user.dir") + File.separator + (USE_PAEXEC ? "paexec.exe\"" : "psexec.exe\"");
    /**
     * The Psutils.
     */
    static final String[] _PSUTILS = new String[]{
            PSEXEC,
            "\"" + System.getProperty("java.home") + File.separator + "bin" + File.separator + "javaw.exe\"",
            "-cp",
            System.getProperty("java.class.path"),
//            "\"" + System.getProperty("user.dir") + File.separator + ".." + File.separator + "lib" + File.separator + "jfx.jar\"",
            "com.jfx.ts.io.PSUtils"
    };
    /**
     * The Mstsc.
     */
    boolean mstsc;
    /**
     * The Session manager.
     */
    SessionManager sessionManager;
    /**
     * The Gui.
     */
    TSConfigGUI gui;
    private ScheduledFuture<?> netstatJob;            //连接监控任务
    private ScheduledFuture<?> spaceMonitoringJob;    //空间监控任务

    /**
     * Gets session manager.
     *
     * @return the session manager
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * 解压交易商的信息文件srv，每次都要这儿样做,如果存在就不复制了
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    static void extractResourceSrv() throws IOException {
        if (new File(JFX_HOME_SRV_DIR).exists()) {
            ZipInputStream zis = new ZipInputStream(TS.class.getResourceAsStream("/com/jfx/ts/net/resources/srv.zip"));
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                String name = zipEntry.getName();
                String pathname = JFX_HOME/*_SRV_DIR*/ + "/" + name;//C:\Users\Micheal\jfx_term/srv/
                File file = new File(pathname);
                if (zipEntry.isDirectory()) {
                    if (file.isFile())
                        file.delete();
                    file.mkdirs();
                } else {
                    if (file.isDirectory())
                        deleteDirectory(file);
                    if (!file.exists()) {
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(pathname);
                            byte[] buf = new byte[1024];
                            int cnt;
                            while ((cnt = zis.read(buf)) > 0) {
                                fos.write(buf, 0, cnt);
                            }
                        } finally {
                            if (fos != null) {
                                fos.close();
                            }
                        }
                    }
                }
            }
            zis.close();
        }
    }

    /**
     * Delete directory.
     *
     * @param dir the dir
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    static void deleteDirectory(File dir) throws IOException {
        Files.walkFileTree(dir.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        dir.delete();
    }

    /**
     * The Is win xp.
     */
    static boolean IS_WIN_XP = System.getProperty("os.version", "-").equals("5.2")
            || System.getProperty("os.name", "-").equals("Windows XP")
            || System.getProperty("os.name", "-").equals("Windows 2003");
    /**
     * The Is 64 bit vm.
     */
    static boolean IS_64BIT_VM = System.getProperty("sun.arch.data.model").equals("64");
    /**
     * The Is 32 bit vm.
     */
    static boolean IS_32BIT_VM = System.getProperty("sun.arch.data.model").equals("32");

    /**
     * Copy file.
     *
     * @param from       the from
     * @param toPathName the to path name
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    static void copyFile(File from, String toPathName) throws IOException {
        if (from != null) {

            Path FROM = Paths.get(from.getAbsolutePath());
            Path TO = Paths.get(toPathName);
            //overwrite existing file, if exists
            CopyOption[] options = new CopyOption[]{
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
            };
            Files.copy(FROM, TO, options);

        }
    }

    /**
     * 配置云端的文件夹
     */
    public static void initCloudDirectories() {
        GDriveAccess.getInstance().setupDownloadFolder(
                GDriveAccess.P_SRV,    // from g-drive folder id
                new File(TS.JFX_HOME_SRV_DIR),         // to local dir
                ".srv"          // file suffix
        );
        GDriveAccess.getInstance().setupDownloadFolder(
                GDriveAccess.P_ZERO_TERM,       // from g-drive folder id
                new File(TS.JFX_HOME_ZTERM_DIR.replace("zero_term", "custom_term")),          // to local dir
                "terminal.exe",         // file suffix
                "metaeditor.exe"        // file suffix
        );
        GDriveAccess.getInstance().setupDownloadFolder(
                GDriveAccess.P_ZERO_TERM_MT5,       // from g-drive folder id
                new File(TS.JFX_HOME_ZTERM_MT5_DIR.replace("zero_term", "custom_term")),          // to local dir
                "terminal.exe",         // file suffix
                "metaeditor.exe",
                "metatester.exe",
                "terminal64.exe",
                "metaeditor64.exe",
                "metatester64.exe"
        );
    }

    /**
     * Init terminal directory from zip.
     * zero item不知道干嘛的，将zip的文件初始化到终端的配置文件夹中，应该是MT4的程序，要配置的，这个还要看MT4的文档
     * zip应该是配置文件的压缩包
     *
     * @param dir the dir
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    static void initTerminalDirectoryFromZip(String dir) throws IOException {
        ZipInputStream zis = null;
        try {
//            String customTermDir = dir.replace("zero_term", "custom_term");
            File rootDir = new File(dir);   //C:\Users\Micheal\jfx_term6
            boolean rootDirExists = rootDir.exists();
            zis = new ZipInputStream(TS.class.getResourceAsStream("resources/basic_trader.zip"));
            //
            if (!rootDirExists) {
                rootDir.mkdirs();
            }
            //
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                String name = zipEntry.getName();
                String pathname = dir + '/' + name;
//                String customPathName = customTermDir + '/' + name;
                //
//                File customFile = new File(customPathName);
                //noinspection PointlessBooleanExpression,ConstantConditions
                if (!isPropriatoryFile(pathname) && false/*customFile.exists() && customFile.isFile()*/) {
                    File subFile = new File(pathname);
                    if (subFile.isDirectory()) {
                        deleteDirectory(subFile);
                    }
//                    copyFile(customFile, pathname);
                } else {
                    if (zipEntry.isDirectory()) {
                        File subdir = new File(pathname);
                        if (subdir.exists()) {
                            if (subdir.isFile()) {
                                subdir.delete();
                                if (!subdir.mkdirs()) {
                                    LOGGER.error("Error making directory: " + subdir);
                                }
                            }
                        } else {
                            if (!subdir.mkdirs()) {
                                LOGGER.error("Error making directory: " + subdir);
                            }
                        }
                    } else {
                        File subFile = new File(pathname);
                        if (subFile.isDirectory()) {
                            deleteDirectory(subFile);
                        }
                        if (!subFile.exists()
                                || zipEntry.getTime() > subFile.lastModified()
                                || isPropriatoryFile(pathname)/*
                                && subFile.length() != zipEntry.getSize()*/) {
                            String newPathname = pathname + ".tmp";
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(newPathname);
                                byte[] buf = new byte[10240];
                                int cnt;
                                while ((cnt = zis.read(buf)) > 0) {
                                    fos.write(buf, 0, cnt);
                                }
                                fos.flush();
                            } catch (Throwable th) {
                                LOGGER.error("Error creating file: " + subFile.getAbsolutePath(), th);
                            } finally {
                                if (fos != null) {
                                    fos.close();
                                }
                            }
                            //
                            if (subFile.exists()) {
                                Path dest = Paths.get(pathname);
                                byte[] n1 = Files.readAllBytes(dest);
                                Path sour = Paths.get(newPathname);
                                byte[] n2 = Files.readAllBytes(sour);
                                if (Arrays.equals(n1, n2)) {
                                    new File(newPathname).delete();
                                } else {
                                    Files.delete(dest);
                                    Files.move(sour, dest);
                                }
                            } else {
                                Path dest = Paths.get(pathname);
                                Path sour = Paths.get(newPathname);
                                Files.move(sour, dest);
                            }
                        }
                    }
                }
            }
            //
/*
            File eaCfgDir = new File(JFX_HOME_EA_DIR);
            if (eaCfgDir.exists()) {
                File termExpertsDir = new File(rootDir, "experts");
                File[] files = eaCfgDir.listFiles();
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < files.length; i++) {
                    File from = files[i];
                    File to = new File(termExpertsDir, from.getName());
                    if (!to.exists() || from != null && from.lastModified() > to.lastModified()) {
                        copyFile(from, to.getAbsolutePath());
                    }
                }
            }
*/
            if (P_USE_MSTSC) {
                assertProgramExitCode(0, ICACLS, dir, "/grant", "Everyone:F", "/T");
                assertProgramExitCode(0, ICACLS, JFX_HOME_SRV_DIR, "/grant", "Everyone:F", "/T");
                assertProgramExitCode(0, ICACLS, JFX_HOME_EA_DIR, "/grant", "Everyone:F", "/T");
                assertProgramExitCode(0, ICACLS, JFX_HOME_CHR_DIR, "/grant", "Everyone:F", "/T");
            }
            //
            if (!new File(JFX_HOME_ZTERM_DIR + "/terminal.exe").exists()
                    && !new File(JFX_HOME_ZTERM_DIR.replace("zero_term", "custom_term") + "/terminal.exe").exists()
                    ) {
                //选择32的程序
                selectAndCopyTerminalExe(JFX_HOME_ZTERM_DIR, "terminal.exe");
            }
            String ztermMt5Dir = JFX_HOME_ZTERM_MT5_DIR;
            if (termExeNotFound("terminal64.exe", ztermMt5Dir)
                    && termExeNotFound("terminal64.exe", ztermMt5Dir.replace("zero_term", "custom_term"))
                    && termExeNotFound("terminal.exe", ztermMt5Dir)
                    && termExeNotFound("terminal.exe", ztermMt5Dir.replace("zero_term", "custom_term"))
                    ) {
                String terminalFileName = IS_32BIT_VM ? "terminal.exe" : "terminal64.exe";
                //选择MT5的终端？
                selectAndCopyTerminalExe(JFX_HOME_ZTERM_MT5_DIR, terminalFileName);
            }
        } catch (Throwable t) {
            LOGGER.error("Error creating dir: " + dir, t);
            throw new IOException("" + t);
        } finally {
            if (zis != null) {
                zis.close();
            }
        }
    }

    private static boolean termExeNotFound(String terminalFileName, String ztermMt5Dir) {
        return new File(ztermMt5Dir).exists()
                && !new File(ztermMt5Dir + "/" + terminalFileName).exists()
                && !new File(ztermMt5Dir + "/terminal.exe").exists();
    }

    /**
     * 这个是没有找到MT4的终端时候提示的
     * @param dir
     * @param file
     */
    private static void selectAndCopyTerminalExe(String dir, String file) {
        final String mt45 = dir.equals(JFX_HOME_ZTERM_DIR) ? "MT4" : "MT5";
        String dialogTitle = "Select " + mt45 + " client terminal executable";
        JEditorPane pane = new JEditorPane("text/html",
                "<b>" + mt45 +
                        "</b> client terminal (<b>" + file + "</b>) not found in <b>" + dir
                        + "</b><br><br>Would you like to select it?"
        );
        pane.setEditable(false);
        pane.setBackground(Color.YELLOW);
        if (JOptionPane.YES_NO_OPTION == JOptionPane.showConfirmDialog(null,
                pane,
                dialogTitle,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        )) {
            //Create a file chooser
            File selectedFile = new File(file);
            try {
                SearchFilePathVisitor visitor = new SearchFilePathVisitor(selectedFile.getName());
                Files.walkFileTree(
                        Paths.get(System.getenv("SystemRoot"), "..",
                                IS_32BIT_VM || dir.equals(JFX_HOME_ZTERM_DIR) ? "Program Files (x86)" : "Program Files"
                        ),
                        visitor
                );
                selectedFile = visitor.fileFound == null ? selectedFile : visitor.fileFound;
            } catch (IOException e) {
                e.printStackTrace();
            }
            final JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(selectedFile);
            //fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    String name = f.getName();
                    return f.isDirectory() || name.startsWith("terminal") && name.endsWith(".exe");
                }

                @Override
                public String getDescription() {
                    return mt45 + " Client Terminal";
                }
            });
            fc.setDialogTitle(dialogTitle);
            //
            int returnVal = fc.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File terminalExeFile = fc.getSelectedFile();
                Path target = Paths.get(dir, terminalExeFile.getName());
                String msg = "Copying [" + terminalExeFile + "] to " + dir;
                TS.LOGGER.info(msg);
                try {
                    Files.copy(terminalExeFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                    //todo copy all exe files from terminalExeFile dir
                } catch (IOException e) {
                    TS.LOGGER.error("Error while " + msg, e);
                }
                return;
            }
        }
        pane = new JEditorPane("text/html",
                mt45.equals("MT4")
                        ? "NJ4X Terminal Server requires MT4 client terminal.<br>" +
                        "Please download and install MetaTrader-4 client terminal at " +
                        "<br><b>http://www.metaquotes.net/en/metatrader4/trading_terminal</b>"
                        : (file.endsWith("64.exe")
                        ? "NJ4X Terminal Server requires MT5 client terminal.<br>" +
                        "Please download and install MetaTrader-5 client terminal at <br><b>http://www.metaquotes.net/en/metatrader5</b>"
                        : "NJ4X Terminal Server requires MT5 client terminal.<br>" +
                        "Please download and install 32-bit MetaTrader-5 client terminal at <br><b>http://www.metaquotes.net/en/metatrader5</b>"
                )
        );
        pane.setEditable(false);
        pane.setBackground(Color.YELLOW);
        JOptionPane.showMessageDialog(null,
                pane,
                dialogTitle,
                JOptionPane.WARNING_MESSAGE
        );
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(
                        mt45.equals("MT4")
                                ? "http://www.metaquotes.net/en/metatrader4/trading_terminal"
                                : "http://www.metaquotes.net/en/metatrader5"));
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Is propriatory file boolean.
     *
     * @param pathname the pathname
     *
     * @return the boolean
     */
    static boolean isPropriatoryFile(String pathname) {
        String pnLower = pathname.toLowerCase();
        String dllPrefix = pnLower.contains("zero_term_mt5") || pnLower.contains("custom_term_mt5") ? "mt5if" : "mt45if";
        return (pnLower.endsWith("jfx.ex4")
                || pnLower.endsWith("nj4x.ex4")
                || pnLower.endsWith("pl.ex4")
                || pnLower.endsWith("ol.ex4")
                || pnLower.endsWith("tl.ex4")
                || pnLower.endsWith("btl.ex4")
                || pnLower.endsWith("jfx.ex5")
                || pnLower.endsWith(dllPrefix + ".dll")
                || pnLower.endsWith(dllPrefix + "_x64.dll")
                || pnLower.endsWith(dllPrefix + "_xp.dll")
                || pnLower.endsWith(dllPrefix + "_xp_x64.dll")
        );
    }

    /**
     * Disable live update.
     */
    public static void disableLiveUpdate() {
        System.setProperty("disable_mt4_live_update", "true");
        amendLiveUpdates();
    }

    /**
     * Enable live update.
     */
    public static void enableLiveUpdate() {
        System.setProperty("disable_mt4_live_update", "false");
        amendLiveUpdates();
    }

    /**
     * Amend live updates.
     */
    static void amendLiveUpdates() {
        try {
            disableLiveUpdateIfNeeded(new File(System.getProperty("user.home").substring(0, 3) + "ProgramData\\MetaQuotes\\WebInstall\\mt5clw\\terminal.exe"));
            disableLiveUpdateIfNeeded(new File(System.getProperty("user.home") + "\\AppData\\Roaming\\MetaQuotes\\WebInstall\\mt5clw\\terminal.exe"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disable live update if needed.
     *
     * @param newTerm the new term
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    static void disableLiveUpdateIfNeeded(File newTerm) throws IOException {
        String mt4LiveUpdate = System.getenv().get("DISABLE_MT4_LIVE_UPDATE");
        boolean disableLiveUpdate = System.getProperty("disable_mt4_live_update", mt4LiveUpdate == null ? "true" : mt4LiveUpdate).equals("true");
        try {
            File metaQuotesDir = newTerm.getParentFile().getParentFile().getParentFile();
            if (disableLiveUpdate) {
                disableDir(new File(metaQuotesDir.getAbsolutePath() + "/WebInstall"));
                disableDir(new File(metaQuotesDir.getAbsolutePath() + "/Terminal/Common"));
                disableDir(new File(metaQuotesDir.getAbsolutePath() + "/Terminal/Community"));
                disableDir(new File(metaQuotesDir.getAbsolutePath() + "/TesterWS"));
            } else {
                enableDir(new File(metaQuotesDir.getAbsolutePath() + "/WebInstall"));
                enableDir(new File(metaQuotesDir.getAbsolutePath() + "/Terminal/Common"));
                enableDir(new File(metaQuotesDir.getAbsolutePath() + "/Terminal/Community"));
                enableDir(new File(metaQuotesDir.getAbsolutePath() + "/TesterWS"));
            }
        } catch (IOException e) {
            LOGGER.warn("Disable live update error occured.", e);
        }
    }

    /**
     * Enable dir.
     *
     * @param mt4clw the mt 4 clw
     */
    static void enableDir(File mt4clw) {
        if (mt4clw.exists() && mt4clw.isFile()) {
            mt4clw.delete();
            if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
                LOGGER.info("Live update enabled: " + mt4clw.getAbsolutePath() + " has been unblocked");
            }
        }
    }

    /**
     * Disable dir.
     *
     * @param metaQuotesDir the meta quotes dir
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    static void disableDir(File metaQuotesDir) throws IOException {
        if (metaQuotesDir.exists()) {
            if (metaQuotesDir.isDirectory()) {
                deleteDirectory(metaQuotesDir);
                new FileOutputStream(metaQuotesDir).close();
                if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
                    LOGGER.info("Live update disabled: " + metaQuotesDir.getAbsolutePath() + " has been blocked");
                }
            }
        } else {
            File webInstall = metaQuotesDir.getParentFile();
            if (webInstall.exists() && webInstall.isDirectory()) {
                new FileOutputStream(metaQuotesDir).close();
                if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
                    LOGGER.info("Live update disabled: " + metaQuotesDir.getAbsolutePath() + " has been blocked");
                }
            }
        }
    }

    /**
     * Copy live updated terminal if needed.
     *
     * @param newTerm the new term
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    static void copyLiveUpdatedTerminalIfNeeded(File newTerm) throws IOException {
        if (newTerm.exists()) {
            if (!System.getProperty("disable_mt4_live_update", "true").equals("true")) {
                String currentTermPath = JFX_HOME_ZTERM_DIR + "\\terminal.exe";
                File currentTerm = new File(currentTermPath);
                if (!currentTerm.exists() || currentTerm.lastModified() < newTerm.lastModified()) {
                    copyFile(newTerm, currentTermPath);
                    if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                        LOGGER.debug("New terminal has been installed: file_sz=" + newTerm.length());
                    }
                }
            }
            boolean deleted = newTerm.delete();
            if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                LOGGER.debug("Live update disabled: " + deleted);
            }
        }
    }

//    static void copyDir(File fromDir, File toDir) throws IOException {
//        boolean timing = fromDir.equals(new File(JFX_HOME_ZTERM_DIR));
//        long start = timing ? System.currentTimeMillis() : 0;
//        File[] entries = fromDir.listFiles();
//        if (entries != null) {
//            for (File file : entries) {
//                String fromFileName = file.getName();
//                String toPathName = toDir.getAbsolutePath() + '/' + fromFileName;
//                if (file.isDirectory()) {
//                    File toSubDir = new File(toPathName);
//                    if (!toSubDir.exists() && !toSubDir.mkdirs()) {
//                        LOGGER.error("Error making directory: " + toSubDir);
//                    } else {
//                        copyDir(file, toSubDir);
//                    }
//                } else {
//                    File subFile = new File(toPathName);
//                    if (!subFile.exists() || subFile.lastModified() < file.lastModified()/* || subFile.length() != file.length()*/) {
//                        copyFile(file, toPathName);
//                    }
//                }
//            }
//            long end = timing ? System.currentTimeMillis() : 0;
//            if (timing && Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
//                LOGGER.debug("Zero term copy time=" + (end - start) + " millis, " + toDir.getAbsolutePath());
//            }
//        }
//    }

    /**
     * Write file.
     *
     * @param fName the f name
     * @param bytes the bytes
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    public static void writeFile(String fName, byte[] bytes) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fName);
            fos.write(bytes);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * The constant scheduledExecutorService.
     */
    public static ScheduledExecutorService scheduledExecutorService
            = java.util.concurrent.Executors.newScheduledThreadPool(64);
    /**
     * The Terminations.
     */
//            = EfficientThreadPoolExecutor.get(16, 64, 10, TimeUnit.SECONDS, 256, "TS-schedule-");
    static Hashtable<String, ScheduledFuture> terminations;
    /**
     * The Sym files.
     */
    static final HashSet<String> symFiles = new HashSet<String>();

    private String port;

    /**
     * Gets port as string.
     *
     * @return the port as string
     */
    public String getPortAsString() {
        return port;
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    public int getPort() {
        return Integer.parseInt(port);
    }

    /**
     * Instantiates a new Ts.
     */
    public TS() {
    }

    /**
     * Instantiates a new Ts.
     *
     * @param port the port
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    public TS(String port) throws IOException {
        this.port = port;
//        scheduledExecutorService = java.util.concurrent.Executors.newScheduledThreadPool(16);
        terminations = new Hashtable<String, ScheduledFuture>();
        //
        startGUI();
        //
        if (!P_GUI_ONLY) {
            new File(JFX_HOME_CONFIG).mkdirs();
            new File(JFX_HOME_SRV_DIR).mkdirs();
            try {
                extractResourceSrv();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //
            try {
                initTerminalDirectoryFromZip(JFX_HOME + File.separatorChar);  //配置终端的文件
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //
        initCloudDirectories();  //貌似是配置云端的文件夹之类的，但是不知道有什么用
        //
        if (!P_GUI_ONLY) {
            System.setProperty(JMXServer.JMX_IMPL_CLASS, JMXServer.MX4J_IMPL_CLASS);
            System.setProperty(JMXServer.MX4J_CONFIG_XML, JMX_CONFIG_XML);    //mbean.xml文件是MT4的配置文件，至少从这个上面看是的
            try {
                JMXServer.getInstance();
            } catch (RuntimeException ignored) {
            }
        }
        //
        if (P_USE_MSTSC) {
            initMstscMode();  //初始化MSTSC模式，应该是远程模式吧，主要是初始化session的连接
        } else {
            sessionManager = new SessionManager(this);
            sessionManager.getMaxDesktopsForSharedSection();
            sessionManager.initTerminalsUpdatingJob();
        }
        //
        if (!P_GUI_ONLY) {
            try {
                Class.forName("com.jfx.ts.net.ty.CliServer").getConstructor(this.getClass()).newInstance(this);
                Class.forName("com.jfx.ts.net.ty.MgmtServer").getConstructor(this.getClass()).newInstance(this);
            } catch (Exception ignore) {
            }
            new ListenerThread(this).start();
        }
        //
        if (!P_GUI_ONLY) {  //这个磁盘监控每60s进行一次6
            spaceMonitoringJob =    scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        long freeSpaceGB = 0;
                        long usableSpace = 0;
                        final Path terminalsDirectory = Paths.get(getTermDir());//C:\.7788
                        try {
                            usableSpace = Files.getFileStore(terminalsDirectory).getUsableSpace();
                            freeSpaceGB = usableSpace / 1024 / 1024 / 1024;  //C盘剩下126G的空闲容量
                        } catch (IOException e) {
                            LOGGER.error("Error calculating usable space at " + getTermDir(), e);
                            return;
                        }
                        if (freeSpaceGB >= Long.parseLong(TS.MIN_DISK_SPACE_GB)) {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("Free space is OK: " + freeSpaceGB + "GB (minimum set to " + TS.MIN_DISK_SPACE_GB + "GB)");
                            }
                            return;
                        }
                        //
                        LOGGER.warn("Free space is low: " + usableSpace + " i.e. " + freeSpaceGB + "GB (minimum set to " + TS.MIN_DISK_SPACE_GB + "GB)");
                        final HashMap<String, Integer> terminals = new HashMap<String, Integer>();
                        for (Session s : sessionManager.getSessions()) {
                            terminals.putAll(s.getTermProcesses());
                        }
                        class FPair {
                            public File socketLogFile;
                            public long lastModified;

                            public FPair(File socketLogFile) {
                                this.socketLogFile = socketLogFile;
                                this.lastModified = socketLogFile.exists() ? socketLogFile.lastModified() : 0;
                            }
                        }
                        ;
                        final ArrayList<FPair> logsToCheck = new ArrayList<>();
                        try {
                            LOGGER.info("Begin scanning space at " + terminalsDirectory);
                            Files.walkFileTree(terminalsDirectory, EnumSet.noneOf(FileVisitOption.class), 2,
                                    new FileVisitor<Path>() {
                                        @Override
                                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                            return FileVisitResult.CONTINUE;
                                        }

                                        @Override
                                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                            String path = file.toString();
                                            if (path.endsWith("\\terminal.exe")) {
                                                if (!terminals.containsKey(path)) {
                                                    logsToCheck.add(new FPair(file.getParent().resolve("logs").resolve("socket.log").toFile()));
                                                    if (logsToCheck.size() % 1000 == 0) {
                                                        LOGGER.info("Scan space at " + terminalsDirectory + ": scanned " + logsToCheck.size() + " terms");
                                                    }
                                                }
                                                return FileVisitResult.SKIP_SIBLINGS;
                                            } else {
                                                return FileVisitResult.CONTINUE;
                                            }
                                        }

                                        @Override
                                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                                            return FileVisitResult.CONTINUE;
                                        }

                                        @Override
                                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                            return FileVisitResult.CONTINUE;
                                        }
                                    });
                        } catch (IOException e) {
                            LOGGER.error("Error walking at the term_dir: " + getTermDir(), e);
                        }
                        LOGGER.info("Scan space at " + terminalsDirectory + " complete. logsToCheck=" + logsToCheck.size());
                        //
                        Collections.sort(logsToCheck, new Comparator<FPair>() {
                            @Override
                            public int compare(FPair o1, FPair o2) {
                                return (int) Math.signum(o1.lastModified - o2.lastModified);
                            }
                        });
                        for (FPair socketLog : logsToCheck) {
                            try {
                                usableSpace = Files.getFileStore(terminalsDirectory).getUsableSpace();
                                freeSpaceGB = usableSpace / 1024 / 1024 / 1024;
                            } catch (IOException e) {
                                LOGGER.error("Error calculating usable space at " + socketLog.socketLogFile.getParentFile().getParent(), e);
                                return;
                            }
                            if (freeSpaceGB >= Long.parseLong(TS.MIN_DISK_SPACE_GB)) {
                                LOGGER.info("Free space is OK: " + freeSpaceGB + "GB (minimum set to " + TS.MIN_DISK_SPACE_GB + "GB)");
                                break;
                            }
                            File terminal = new File(socketLog.socketLogFile.getParentFile().getParentFile().getAbsolutePath() + "\\terminal.exe");
                            final String terminalProcessName = terminal.getAbsolutePath();
                            if (!ClientWorker.lockTerminalProcessing(terminalProcessName)) {
                                try {
                                    if (!PSUtils.isRunning(terminalProcessName)
                                            && terminal.delete()) {
                                        deleteDirectory(terminal.getParentFile());
                                        LOGGER.info("Cleaned terminal's directory: " + terminal.getParent());
                                    }
                                } catch (IOException e) {
                                    LOGGER.error("Error deleting terminal's directory: " + terminal.getParent(), e);
                                } finally {
                                    ClientWorker.unlockTerminalProcessing(terminalProcessName);
                                }
                            }
                        }
                        if (freeSpaceGB < Long.parseLong(TS.MIN_DISK_SPACE_GB) && logsToCheck.size() > 0) {
                            LOGGER.info("Free space is still low: " + usableSpace + " i.e. " + freeSpaceGB + "GB (minimum set to " + TS.MIN_DISK_SPACE_GB + "GB)");
                        }
                    } catch (Exception e) {
                        LOGGER.error("Free space job error", e);
                    }
                }
            }, 5, 60, TimeUnit.SECONDS);
        }
    }

    private static class TSConfig extends java.util.Properties {

        /**
         * The constant TS_CONFIG.
         */
        public static final String TS_CONFIG = TS.JFX_HOME_CONFIG + "/ts_config.xml";

        /**
         * Instantiates a new Ts config.
         */
        public TSConfig() {
            File cfgFile = new File(TS_CONFIG);
            if (cfgFile.exists()) {
                try {
                    loadFromXML(new FileInputStream(cfgFile));
                } catch (IOException e) {
                    LOGGER.error("ts_config.xml load error", e);
                }
            }
        }

        @Override
        public synchronized void putAll(Map<?, ?> t) {
            try {
                super.putAll(t);
            } finally {
                persist("multiple properties set at " + new Date());
            }
        }

        @Override
        public synchronized Object setProperty(String key, String value) {
            try {
                return super.setProperty(key, value);
            } finally {
                persist(key + " property set at " + new Date());
            }
        }

        /**
         * Remove properties.
         *
         * @param keys the keys
         */
        public synchronized void removeProperties(String... keys) {
            try {
                for (int i = 0; i < keys.length; i++) {
                    String key = keys[i];
                    super.remove(key);
                }
            } finally {
                persist("Properties removed at " + new Date());
            }
        }

        private void persist(String comment) {
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(TS_CONFIG);
                storeToXML(os, comment);
            } catch (IOException e) {
                LOGGER.error("ts_config.xml persistance error", e);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }

    /**
     * Sets configuration value.
     *
     * @param key   the key
     * @param value the value
     */
    public static void setConfigurationValue(String key, String value) {
        tsConfig.setProperty(key, value);
    }

    /**
     * Sets configuration values.
     *
     * @param kv the kv
     */
    public static void setConfigurationValues(Map<String, String> kv) {
        tsConfig.putAll(kv);
    }

    /**
     * Remove configuration values.
     *
     * @param keys the keys
     */
    public static void removeConfigurationValues(String... keys) {
        tsConfig.removeProperties(keys);
    }

    /**
     * Gets configuration value.
     *
     * @param key          the key
     * @param defaultValue the default value
     *
     * @return the configuration value
     */
    public static String getConfigurationValue(String key, String defaultValue) {
        return tsConfig.getProperty(key, defaultValue);
    }

    private void startGUI() {
        gui = new TSConfigGUI();
        gui.setTs(this);
        //
        if (P_START_GUI) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JFrame frame = new JFrame("NJ4X Terminal Server Dashboard, v" + version);
                    frame.setContentPane(gui.tsConfigGUIRoot);
                    frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setLocationRelativeTo(null);//centered
                    frame.setIconImage(new ImageIcon(this.getClass().getResource("/jfx_logo_s.png")).getImage());
                    frame.setVisible(true);
                }
            });
        }
    }

    private void initMstscMode() throws IOException {
        try {
            mstsc = false;
            //
            if (canNotUseMstsc(null)) return;
            //
            loadTsUsers(); //初始化Ts的suers，主要是初始化sessionmanager
            mstsc = true;
            LOGGER.info("MSTSC mode initialization succeded");
        } finally {
            if (!mstsc) {
                LOGGER.error("MSTSC mode initialization error");
                LOGGER.error("You need to fix an error above before starting TS in MSTSC mode (-Duse_mstsc=true)");
            }
        }
    }

    /**
     * Can not use mstsc boolean.
     *
     * @param args the args
     *
     * @return the boolean
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    static boolean canNotUseMstsc(String[] args) throws IOException {
        if (!new File(MSTSC).exists()) {
            String message = "MSTSC mode initialization error: " + MSTSC + " not found";
            LOGGER.error(message);
            throw new IOException(message);
//            return true;
        }
        //
        ExternalProcess p = new ExternalProcess(NET, "localgroup");
        int exit = -1;
        try {
            exit = p.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (exit != 0) {
            String message = "MSTSC mode initialization error: get local groups: " + p.getErr();
            LOGGER.error(message);
            throw new IOException(message);
//            return true;
        }
        String out = p.getOut();
        if (!out.contains("*" + REMOTE_DESKTOP_USERS)) {
            String message = "MSTSC mode initialization error: group " + REMOTE_DESKTOP_USERS + " not found: [" + out + "]";
            LOGGER.error(message);
            throw new IOException(message);
//            return true;
        }
        if (!out.contains("*" + ADMINISTRATORS)) {
            String message = "MSTSC mode initialization error: group " + ADMINISTRATORS + " not found: [" + out + "]";
            LOGGER.error(message);
            throw new IOException(message);
//            return true;
        }
        if (!out.contains("*" + USERS)) {
            String message = "MSTSC mode initialization error: group " + USERS + " not found";
            LOGGER.error(message);
            throw new IOException(message);
//            return true;
        }
        //
        if (!USE_PAEXEC) {
            assertProgramExitCode(-1, PSEXEC, "-accepteula");
        }
        assertProgramExitCode(1, _PSUTILS);
//            String osv = System.getProperty("os.version");
//            if (osv.equals("5.1") || osv.equals("5.2") || System.getProperty("wmic_via_psexec", "false").equals("true")) {
//                assertProgramExitCode(0, PSEXEC, WMIC, "CPU", "LIST", "/format:CSV");
//            } else {
        assertProgramExitCode(0, WMIC, "/interactive:off", "CPU", "LIST", "/format:CSV");
//            }
        assertProgramExitCode(1, CMDKEY);
//        assertProgramExitCode(0, QUSER);
        PSUtils.WtsSessionInfo[] quser = PSUtils.getSessions();
        if (quser != null) {
            for (PSUtils.WtsSessionInfo aQuser : quser) {
                LOGGER.info("" + aQuser);
            }
        }
        //assertProgramExitCode(0, REG, "/?");
        assertProgramExitCode(0, REG, "add", "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Terminal Server", "/v", "fDenyTSConnections", "/t", "REG_DWORD", "/d", "0", "/f");
//            assertProgramExitCode(0, ICACLS, getTermDir(), "/grant", "Everyone:(OI)(CI)F", "/T"); //todo check this TS startup speedup works well
        //
        RunAsNj4x(args);
        //
        return false;
    }

    /**
     * The constant USE_NJ4X_USER.
     */
    final static boolean USE_NJ4X_USER = false;

    private static void RunAsNj4x(String[] args) throws IOException {
        if (!USE_NJ4X_USER) return;
        SessionManager sessionManager = new SessionManager(null);
        sessionManager.createTsUser(0);
        TsSystemUser nj4xUser = sessionManager.getTsUserCreateTemplateIfNeeded(0);
        if (!System.getProperty("user.name").equals(nj4xUser.getName())) {
            ArrayList<String> cmdLine = new ArrayList<String>();
            //
            cmdLine.add(TS.PSEXEC);
//            cmdLine.add("-d");
            cmdLine.add("-i");
            cmdLine.add("-h");
            cmdLine.add("-u");
            cmdLine.add(nj4xUser.name);
            cmdLine.add("-p");
            cmdLine.add(nj4xUser.password);
            cmdLine.add("-w");
            cmdLine.add(System.getProperty("user.dir"));
            cmdLine.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "javaw.exe");
            cmdLine.add("-Xms1024M");
            cmdLine.add("-Xmx1024M");
            cmdLine.add("-cp");
            cmdLine.add(System.getProperty("java.class.path"));
            cmdLine.add("-Dcom.sun.management.jmxremote");
            cmdLine.add("-Dcom.sun.management.jmxremote.port=1" + System.getProperty("port", "7788"));
            cmdLine.add("-Dcom.sun.management.jmxremote.authenticate=false");
            cmdLine.add("-Dcom.sun.management.jmxremote.ssl=false");
            //cmdLine.add("-Dnj4x.user="+System.getProperty("user.name"));
            /*
            com.sun.management.jmxremote.authenticate=false
com.sun.management.jmxremote.ssl=false
            */
            cmdLine.add("com.jfx.ts.net.TerminalServer");
            if (args == null) {
                Collections.addAll(cmdLine, "port", System.getProperty("port", "7788"), "use_mstsc", "true", "gui_only", "" + P_GUI_ONLY);
            } else {
                Collections.addAll(cmdLine, args);
            }
            new ExternalProcess(cmdLine.toArray(new String[cmdLine.size()])).start();
            System.exit(0);
        }
    }

    private void loadTsUsers() throws IOException {
        sessionManager = new SessionManager(this);
        //这个就是要加载session
        sessionManager.loadSessions().initTsUsers();
    }

    /**
     * Assert program exit code string.
     *
     * @param exitCode the exit code
     * @param cmd      the cmd
     *
     * @return the string
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    static String assertProgramExitCode(int exitCode, String... cmd) throws IOException {
        return assertProgramExitCode(exitCode, null, cmd);
    }

    /**
     * Assert program exit code string.
     *
     * @param exitCode    the exit code
     * @param outListener the out listener
     * @param cmd         the cmd
     *
     * @return the string
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    static String assertProgramExitCode(int exitCode, LineListener outListener, String... cmd) throws IOException {
        if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
            LOGGER.debug("BEGIN: assert Program ExitCode=" + exitCode + ", cmd=" + ExternalProcess.asString(cmd));
        }
        ExternalProcess p = new ExternalProcess(cmd);
        p.setIsHidden(cmd[0].equals(TS.ICACLS)).setOutListener(outListener);
        int exit;
        try {
            exit = p.run();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
            LOGGER.debug("END  : assert Program ExitCode=" + exitCode + ", cmd=" + ExternalProcess.asString(cmd));
        }
        if (exit != exitCode) {
            throw new IOException("" + p + " exit code=" + exit + ", err=" + p.getErr());
        }
        return p.getOut();
    }

    /**
     * Run program get exit code int.
     *
     * @param cmd the cmd
     *
     * @return the int
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    static int runProgramGetExitCode(String... cmd) throws IOException {
        return runProgramGetExitCode(0, cmd);
    }

    /**
     * Run program get exit code int.
     *
     * @param tmoutMillis the tmout millis
     * @param cmd         the cmd
     *
     * @return the int
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    static int runProgramGetExitCode(int tmoutMillis, String... cmd) throws IOException {
        try {
            return new ExternalProcess(cmd).run(tmoutMillis);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    /**
     * The No net stat.
     */
    static boolean noNetStat = System.getProperty("no_netstat", "false").equals("true");
    /**
     * The constant CONNECTION_TIMEOUT_MILLIS.
     */
    public static int CONNECTION_TIMEOUT_MILLIS = Integer.parseInt(System.getProperty("conn_timeout_seconds", "120")) * 1000;
    /**
     * The constant NO_NETSTAT_DELAY_MILLIS.
     */
    public static int NO_NETSTAT_DELAY_MILLIS = CONNECTION_TIMEOUT_MILLIS / 2; //应该是没有连接时的延迟时间
    /**
     * The Last net stat.
     */
    static long lastNetStat = 0;
    /**
     * The Net stat buffer.
     */
    static volatile ArrayList<String> netStatBuffer = new ArrayList<String>();

/*
    static ArrayList<String> netStat(final int pid) throws IOException {
        return netStat(pid, true);
    }
*/

/*
    static ArrayList<String> netStat(final int pid, final boolean debug) throws IOException {
        final ArrayList<String> sb = new ArrayList<String>();
        if (PSUtils.isWindows && !noNetStat) {
            final String suffix = " " + pid;
            //
            ArrayList<String> netStat = TS.netStatBuffer;
            for (String line : netStat) {
                if (line.endsWith(suffix)) {
                    if (debug && Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                        LOGGER.debug(line);
                    }
                    sb.add(line);
                }
            }
            //
            return sb;
        } else {
            return sb;
        }
    }
*/

    /**
     * The Dir 2 symbols.
     */
    final Hashtable<String, String[]> dir2symbols = new Hashtable<String, String[]>();

    /**
     * Gets term dir.
     *
     * @return the term dir
     */
    public static String getTermDir() {
        if (TERM_DIR == null) {
            return getTargetTermDir();
        } else {
            return TERM_DIR;
        }
    }

    /**
     * Gets target term dir.
     *
     * @return the target term dir
     */
    public static String getTargetTermDir() {
        String uh = System.getProperty("program_data_dir", P_USE_MSTSC ? "C:\\ProgramData\\nj4x" : System.getProperty("user.home"));
        return uh + "\\.jfx_terminals";
    }

    /**
     * Log.
     *
     * @param msg the msg
     */
    public void log(String msg) {
        if (Log4JUtil.isConfigured()) {
            LOGGER.info(msg);
        } else {
            System.out.println(msg);
        }
    }

    /**
     * Pause.
     *
     * @param msg the msg
     */
    static void pause(String msg) {
        System.out.println(msg);
        try {
            System.in.read();
        } catch (IOException ignored) {
        }
    }

    /**
     * Run term process get pid int.
     *
     * @param tp                  the tp
     * @param terminalProcessName the terminal process name
     * @param dir                 the dir
     * @param runInTSSession      the run in ts session
     * @param swHide              the sw hide
     *
     * @return the int
     */
    int runTermProcessGetPID(TerminalParams tp, String terminalProcessName, String dir, boolean runInTSSession, SwHide swHide) {
        Session s = null;
        if (mstsc) {
            if (runInTSSession) {
                s = sessionManager.getLocalSession();
            } else {
                s = sessionManager.getMinLoadSession();
            }
        } else {
            s = sessionManager.getLocalSession();
        }
//        String terminalArgs = "/portable /skipupdate init.ini";
        String terminalArgs = tp.isMT5 ? "/portable /skipupdate /config:init.ini" : "/portable /skipupdate init.ini";
        boolean hide = swHide == SwHide.DEFAULT
                ? TerminalParams.SW_HIDE.equals("true")
                : (swHide == SwHide.HIDE || swHide == SwHide.SHOW_MINIMIZED);
        int pid;
        if (s != null) {
            try {
                if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
                    TS.LOGGER.info("Starting terminal for " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " " + s);
                }
                if (s.user == null || s.user.id <= 0) {
                    pid = PSUtils.startProcess2("\"" + terminalProcessName + "\" " + terminalArgs, dir, hide, tp.isMT5 || swHide == SwHide.SHOW_MINIMIZED);
                    if (pid > 0) {
                        s.addTermProcess(terminalProcessName, pid);
                    }
                    return pid;
                } else {
                    if (tp.isMT5) {
                        pid = s.runProcessGetPID(!hide, PSUtils.IS_CPU1_FREE, dir, terminalProcessName, "/portable", "/skipupdate", "/config:init.ini");
                    } else {
                        pid = s.runProcessGetPID(!hide, PSUtils.IS_CPU1_FREE, dir, terminalProcessName, "/portable", "/skipupdate", "init.ini");
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("Run " + terminalProcessName + " error", e);
                return 0;
            }
        } else {
            try {
                if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
                    TS.LOGGER.info("Starting terminal for " + tp.user + '/' + tp.srv + '(' + tp.tenant + ')' + " session=null");
                }
                pid = PSUtils.startProcess2("\"" + terminalProcessName + "\" " + terminalArgs, dir, hide, tp.isMT5 || swHide == SwHide.SHOW_MINIMIZED);
            } finally {
                sessionManager.updateTerminals();
            }
        }
        if (pid >= 1000000000) { //1073741000
            // error
            long error = ((long) -pid);
            TS.LOGGER.error(String.format("Terimnal is unable to start correctly: 0x%X, %s", error, terminalProcessName));
            return -pid;
        } else {
            return pid;
        }
    }

    private int terminateProcess(int pid) throws IOException {
        ArrayList<String> cmdLine = new ArrayList<String>();
        cmdLine.add("taskkill.exe");
        cmdLine.add("/PID");
        cmdLine.add("" + pid);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Terminating process: " + cmdLine);
        }
        String[] cmd = cmdLine.toArray(new String[cmdLine.size()]);
        int exit = runProgramGetExitCode(30000, cmd);
        LOGGER.info("Terminating process: " + pid + " exit=" + exit);
        if (exit != 0) {
            LOGGER.error("Process termination error, code=" + exit);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cmd.length; i++) {
                String s = cmd[i];
                if (i > 0) {
                    sb.append(' ');
                }
                boolean isSpace = s.indexOf(' ') >= 0;
                if (isSpace) {
                    sb.append('"');
                }
                sb.append(s);
                if (isSpace) {
                    sb.append('"');
                }
            }
            LOGGER.error("Command line: " + sb);
        }
        return exit;
    }

    /**
     * Wmclose process boolean.
     *
     * @param terminalProcessName the terminal process name
     * @param show                the show
     *
     * @return the boolean
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    boolean wmcloseProcess(String terminalProcessName, boolean show) throws IOException {
        int hWnd = getHWND(terminalProcessName);
        return wmcloseProcess(hWnd, terminalProcessName, show);
    }

    private boolean wmcloseProcess(int hWnd, String terminalProcessName, boolean show) {
        if (hWnd == 0) {
            LOGGER.warn("No HWND found: " + terminalProcessName);
            return false;
        } else {
            boolean b = PSUtils.postMsg(true, hWnd, 16, 0, 0);
            Boolean wmClosed = hwndWmClosed.get(hWnd);
            if (wmClosed == null) {
                hwndWmClosed.put(hWnd, wmClosed = b);
            }
            if (show && !wmClosed) {
                LOGGER.info("Closing terminal:"
                        + " res=" + b
                        + " hWnd=" + hWnd
                        + " " + terminalProcessName
                );
            }
            return b;
        }
    }

    /**
     * Gets hwnd.
     *
     * @param terminalProcessName the terminal process name
     *
     * @return the hwnd
     */
    public int getHWND(String terminalProcessName) {
        int[] pidSessId = PSUtils.getPIDAndSessionID(terminalProcessName, P_USE_MSTSC);
        return getHWND(pidSessId);
/*
        Path mt4Home = Paths.get(terminalProcessName).getParent();
        File mt4WndFile = mt4Home.resolve("experts/files/wnd").toFile();
        File mt45WndFile = mt4Home.resolve((mt4Home.resolve("MQL5").toFile().exists() ? "MQL5" : "MQL4") + "/Files/wnd").toFile();
        File wnd;
        if (mt4WndFile.exists() && mt45WndFile.exists()) {
            wnd = mt4WndFile.lastModified() > mt45WndFile.lastModified() ? mt4WndFile : mt45WndFile;
        } else {
            wnd = mt4WndFile.exists() ? mt4WndFile : mt45WndFile;
        }
        if (wnd.exists() && wnd.length() > 0) {
            try {
                return new Integer(new String(Files.readAllBytes(wnd.toPath())).trim());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
*/
    }

    /**
     * Toggle window visibility by pid boolean.
     *
     * @param pid the pid
     *
     * @return the boolean
     */
    public boolean toggleWindowVisibilityByPID(int pid) {
        if (sessionManager == null || sessionManager.getSessions().size() < 2) {
            return PSUtils.toggleWindowVisibilityByPID(pid);
        } else {
            Session session = sessionManager.getSessionByPID(pid);
            return session != null && session.toggleWindowVisibilityByPID(pid);
        }
    }

    /**
     * Sw show hide by pid boolean.
     *
     * @param show the show
     * @param pid  the pid
     *
     * @return the boolean
     */
    public boolean swShowHideByPID(boolean show, int pid) {
        if (sessionManager == null || sessionManager.getSessions().size() < 2) {
            return PSUtils.swShowHideByPID(show, pid);
        } else {
            Session session = sessionManager.getSessionByPID(pid);
            return session != null && session.swShowHideByPID(show, pid);
        }
    }

    /**
     * Gets hwnd.
     *
     * @param pidSessId the pid sess id
     *
     * @return the hwnd
     */
    public int getHWND(int[] pidSessId) {
        int pid = pidSessId[0];
        if (sessionManager == null || sessionManager.getSessions().size() < 2) {
            return PSUtils.getHwndByPID(pid);
        } else {
            int sessId = pidSessId[1];
            Session session = sessionManager.getSessionById(sessId);
            if (session != null) {
                return session.getHWND(pid);
            } else {
                return 0;
            }
        }
    }

    /**
     * Switch endianess byte [ ].
     *
     * @param a the a
     *
     * @return the byte [ ]
     */
    public static byte[] switchEndianess(byte[] a) {
        for (int i = 0; i + 1 < a.length; i += 2) {
            byte b = a[i];
            a[i] = a[i + 1];
            a[i + 1] = b;
        }
        return a;
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    public static void main(String[] args) throws IOException {
        LibrariesUtil.initEmbeddedLibraries();
        if (args.length == 0) {
            killTerminalsInTermDir();
        } else if (args.length == 1 && args[0].equals("-termip")) {
            dupmTerminalsIP();
        }
    }

    /**
     * Dupm terminals ip.
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    static void dupmTerminalsIP() throws IOException {
        try {
            String TASKLIST = System.getenv("SystemRoot") + File.separator + "system32" + File.separatorChar + "tasklist.exe";
            ExternalProcess taskList = new ExternalProcess(TASKLIST, "/fi", "imagename eq terminal.exe", "/fo", "csv", "/nh", "/v");
            taskList.run();
            String terminals = taskList.getOut().trim();
            //
            taskList = new ExternalProcess(TASKLIST, "/fi", "imagename eq terminal64.exe", "/fo", "csv", "/nh", "/v");
            taskList.run();
            terminals += "\n" + taskList.getOut().trim();
            //
            String[] split = terminals.split("\n");
            HashMap<String, String> ip1 = new HashMap<>();
            HashMap<String, String> ip2 = new HashMap<>();
            int maxNameLen = 0;
            for (String term : split) {
//                    System.out.println(term);
                String[] _term = term.split("\",\"");
                if (_term.length < 2) {
                    System.out.println(term);
                    continue;
                }
                String pid = _term[1];
                String name = _term[_term.length - 1];
                if (maxNameLen < name.length())
                    maxNameLen = name.length();
                PSUtils.MibTcpRowOwnerPid[] net = PSUtils.currentProcessConnections(Integer.parseInt(pid));
                for (PSUtils.MibTcpRowOwnerPid s : net) {
                    if (s.getState() == PSUtils.MibState.ESTAB) {
                        //System.out.println(name + " -> " + _net[2]);
                        ip1.put(name, s.getRemoteAddress() + ':' + s.getRemotePort());
                    } else {
                        //System.out.println(name + " ?? " + _net[2]);
                        ip2.put(name, s.getRemoteAddress() + ':' + s.getRemotePort());
                    }
                }
/*
                ArrayList<String> net = netStat(Integer.parseInt(pid), false);
                for (String s : net) {
                    while (s.contains("  ")) s = s.replace("  ", " ");
                    String[] _net = s.trim().split(" ");
                    if (_net[3].equals("ESTABLISHED")) {
                        //System.out.println(name + " -> " + _net[2]);
                        ip1.put(name, _net[2]);
                    } else {
                        //System.out.println(name + " ?? " + _net[2]);
                        ip2.put(name, _net[2]);
                    }
                }
*/
            }
            for (Map.Entry<String, String> e : ip1.entrySet()) {
                System.out.println(String.format("\"%-" + maxNameLen + "s -> %s", e.getKey(), e.getValue()));
            }
            for (Map.Entry<String, String> e : ip2.entrySet()) {
                if (!ip1.containsKey(e.getKey())) {
                    System.out.println(String.format("\"%-" + maxNameLen + "s .> %s", e.getKey(), e.getValue()));
                }
            }
            System.out.println("Press Enter");
            System.in.read();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Kill terminals in term dir.
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    static void killTerminalsInTermDir() throws IOException {
        Files.walkFileTree(Paths.get(getTermDir()), new HashSet<FileVisitOption>(), 2, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String mt4 = file.toString();
                if (mt4.endsWith("terminal.exe")) {
                    PSUtils.killProcessGetSessionID(mt4, false);
                    return FileVisitResult.SKIP_SIBLINGS;
                } else {
                    return FileVisitResult.CONTINUE;
                }
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * The Hwnd wm closed.
     */
    final WeakHashMap<Integer, Boolean> hwndWmClosed = new WeakHashMap<>();

    /**
     * Kill process boolean.
     *
     * @param terminalProcessName the terminal process name
     *
     * @return the boolean
     */
    boolean killProcess(String terminalProcessName) {
        return killProcess(terminalProcessName, false);
    }

    /**
     * Kill process boolean.
     *
     * @param terminalProcessName the terminal process name
     * @param ignoreRes           the ignore res
     *
     * @return the boolean
     */
    boolean killProcess(String terminalProcessName, boolean ignoreRes) {
        int[] pidSessId = PSUtils.getPIDAndSessionID(terminalProcessName, mstsc);
        int pid = pidSessId[0];
        int sessId = pidSessId[1];
        if (pid > 0) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("wm_close process " + " (PID=" + pid/* + ", hWND=" + hwnd*/ + "): " + terminalProcessName);
            }
            //
            int tmoutMillis = ignoreRes ? 100 : 5000;
            if (mstsc) {
                if (sessId >= 0) {
                    Session session = sessionManager.getSessionById(sessId);
                    boolean exit = session.terminateApp(pid, tmoutMillis, LOGGER.isDebugEnabled());
                    if (exit) {
                        session.removeTermProcessOrPID(terminalProcessName, pid);
                        return true;
                    }
                } else {
                    LOGGER.error("Can not detect SessionId for process: " + terminalProcessName);
                }
                return false;
            } else {
                boolean b = terminateApp(pid, tmoutMillis, LOGGER.isDebugEnabled());
                if (b) {
                    Session session = sessionManager.getSessionById(sessId);
                    if (session != null) {
                        session.removeTermProcessOrPID(terminalProcessName, pid);
                    }
                }
                return b;
            }
        } else {
            LOGGER.warn("Can not get PID of process: " + terminalProcessName);
            //throw new MaxNumberOfTerminalsReached(terminalProcessName);
            return false;
        }
    }

    /**
     * Terminate app boolean.
     *
     * @param pid          the pid
     * @param tmoutMillis  the tmout millis
     * @param debugEnabled the debug enabled
     *
     * @return the boolean
     */
    boolean terminateApp(int pid, int tmoutMillis, boolean debugEnabled) {
        boolean b = PSUtils.terminateApp(pid, tmoutMillis);
        if (debugEnabled) {
            LOGGER.debug("TerminateApp: pid=" + pid + ", tmout=" + tmoutMillis + ", res=" + b);
        }
        return b;
    }

    @SuppressWarnings("UnusedDeclaration")
    private boolean killProcessWithVisibleWindow(String terminalProcessName) {
        int[] pidSessId = PSUtils.getPIDAndSessionID(terminalProcessName, mstsc);
        int pid = pidSessId[0];
        int sessId = pidSessId[1];
        if (pid > 0) {
            try {
                if (mstsc) {
                    if (sessId >= 0) {
                        int exit = sessionManager.getSessionById(sessId).terminateProcess(pid, false);
                        if (exit > 0) {
                            // todo move it to killTerminal
                            sessionManager.getSessionById(sessId).removeTermProcessOrPID(terminalProcessName, pid);
                            return true;
                        }
                    } else {
                        LOGGER.error("Can not detect SessionId for process: " + terminalProcessName);
                    }
                    return false;
                } else {
                    int exit = terminateProcess(pid);
                    return exit == 0;
                }
            } catch (IOException e) {
                LOGGER.error("Can not terminate process: " + terminalProcessName, e);
                return false;
            }
        } else {
            LOGGER.error("Can not get PID of process: " + terminalProcessName);
            return false;
        }
    }

    /**
     * Kill process ungracefully boolean.
     *
     * @param terminalProcessName the terminal process name
     *
     * @return the boolean
     */
    public boolean killProcessUngracefully(String terminalProcessName) {
        int sessId = PSUtils.killProcessGetSessionID(terminalProcessName, mstsc);
        if (sessId >= 0) {
            if (mstsc) {
                Session sessionById = sessionManager.getSessionById(sessId);
                if (sessionById != null) { // terminal may be started manually in custom session
                    sessionById.removeTermProcessOrPID(terminalProcessName, 0);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Kill terminal.
     *
     * @param terminalProcessName the terminal process name
     */
    public void killTerminal(String terminalProcessName) {
        while (PSUtils.isRunning(terminalProcessName)) {
            killProcess(terminalProcessName);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
//        pause("Process is not running: [" + terminalProcessName + "]");
    }

    private final HashMap<Long, ClientWorker> clients = new HashMap<>();  //存储clients的
    private long lastToken = System.currentTimeMillis();
    private Future clientsCleanerJob = null;
    /**
     * The constant SESSION_TIMEOUT_MILLIS.
     */
    public static final int SESSION_TIMEOUT_MILLIS = 60000;

    /**
     * 开始一个新的终端的工作，这个是具体的开始工作
     *
     * @param cInfo the c info
     *
     * @return the client worker
     */
    public ClientWorker newClientWorker(Nj4xClientInfo cInfo) {  //Nj4xClientInfo这个类很简单
        ClientWorker cw;
        synchronized (clients) {
            long token = ++lastToken;//token就是当前连接的一个标识
            cw = new ClientWorker(this, token, cInfo);//实例化一个终端工作对象
            clients.put(token, cw);
            //
            if (clientsCleanerJob == null) {
                clientsCleanerJob = TerminalServer.CACHED_THREAD_POOL.submit(new Runnable() {
                    @Override
                    public void run() {
                        while (!clientsCleanerJob.isCancelled()) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                            }
                            //
                            ArrayList<Long> toDelete = new ArrayList<Long>();//实例化一个要删除的终端list
                            synchronized (clients) {
                                for (Map.Entry<Long, ClientWorker> e : clients.entrySet()) {
                                    //如果上一次的通信事件到现在的时间大于60000就断掉连接
                                    if (e.getValue().getLastUsageTimeIntervalMillis() > SESSION_TIMEOUT_MILLIS) {
                                        toDelete.add(e.getKey());
                                    }
                                }
                                for (Long token : toDelete) {
                                    //提示连接断掉了，直接删掉client
                                    TS.LOGGER.warn("Client session timed out: token=" + token + ", client=" + clients.get(token));
                                    clients.remove(token);
                                }
                            }
                        }
                    }
                });
            }
        }
        //
        return cw;
    }

    /**
     * Gets client worker.
     *
     * @param token the token
     * @param info  the info
     *
     * @return the client worker
     *
     * @exception Nj4xInvalidTokenException   the nj 4 x invalid token exception
     * @exception Nj4xSessionExpiredException the nj 4 x session expired exception
     * @exception Nj4xInvalidTokenException   the nj 4 x invalid token exception
     * @exception Nj4xSessionExpiredException the nj 4 x session expired exception
     * @exception Nj4xInvalidTokenException   the nj 4 x invalid token exception
     * @exception Nj4xSessionExpiredException the nj 4 x session expired exception
     * @exception Nj4xInvalidTokenException   the nj 4 x invalid token exception
     * @exception Nj4xSessionExpiredException the nj 4 x session expired exception
     * @exception Nj4xInvalidTokenException   the nj 4 x invalid token exception
     * @exception Nj4xSessionExpiredException the nj 4 x session expired exception
     */
    public ClientWorker getClientWorker(long token, String info) throws Nj4xInvalidTokenException, Nj4xSessionExpiredException {
        if (token > 0) {
            ClientWorker cw;
            synchronized (clients) {
                cw = clients.get(token);
            }
            if (cw == null) {
                throw new Nj4xSessionExpiredException("Session expired: " + token);
            } else {
                cw.setCurrentThreadName(info);
                return cw;
            }
        } else {
            throw new Nj4xInvalidTokenException();
        }
    }

    /**
     * Remove client worker client worker.
     *
     * @param token the token
     *
     * @return the client worker
     *
     * @exception Nj4xInvalidTokenException   the nj 4 x invalid token exception
     * @exception Nj4xSessionExpiredException the nj 4 x session expired exception
     * @exception Nj4xInvalidTokenException   the nj 4 x invalid token exception
     * @exception Nj4xSessionExpiredException the nj 4 x session expired exception
     * @exception Nj4xInvalidTokenException   the nj 4 x invalid token exception
     * @exception Nj4xSessionExpiredException the nj 4 x session expired exception
     * @exception Nj4xInvalidTokenException   the nj 4 x invalid token exception
     * @exception Nj4xSessionExpiredException the nj 4 x session expired exception
     * @exception Nj4xInvalidTokenException   the nj 4 x invalid token exception
     * @exception Nj4xSessionExpiredException the nj 4 x session expired exception
     */
    public ClientWorker removeClientWorker(long token) throws Nj4xInvalidTokenException, Nj4xSessionExpiredException {
        if (token > 0) {
            ClientWorker cw;
            synchronized (clients) {
                cw = clients.remove(token);
            }
            if (cw == null) {
                throw new Nj4xSessionExpiredException("Session expired: " + token);
            } else {
                return cw;
            }
        } else {
            throw new Nj4xInvalidTokenException();
        }
    }

    /**
     * Update terminals.
     */
    public void updateTerminals() {
        if (sessionManager != null) {
            sessionManager.updateTerminals();
        }
    }

    /**
     * Gets terminals.
     *
     * @return the terminals
     */
    public ArrayList<String> getTerminals() {
        ArrayList<String> terms = new ArrayList<>();
        for (Session s : sessionManager.getSessions()) {
            for (Map.Entry<String, Integer> p : s.getTermProcesses().entrySet()) {
                String pPath = p.getKey();
                terms.add(pPath);
            }
        }
        return terms;
    }

    /**
     * The interface Managed terminal.
     */
    public interface IManagedTerminal {
        /**
         * Gets module.
         *
         * @return the module
         */
        String getModule();

        /**
         * Gets number of client connections.
         *
         * @return the number of client connections
         */
        int getNumberOfClientConnections();

        /**
         * Is hooked boolean.
         *
         * @return the boolean
         */
        boolean isHooked();

        /**
         * Transfer socket.
         *
         * @param cliChannelHdlr the cli channel hdlr
         */
        void transferSocket(Object cliChannelHdlr);
    }

    /**
     * The Managed terminals registry.
     */
    public static ConcurrentHashMap<String, IManagedTerminal> managedTerminalsRegistry = null;

    /**
     * Kill terminals.
     */
    public void killTerminals() {
        for (String t : getTerminals()) {
            killProcessUngracefully(t);
        }
    }

    /**
     * Gets pid.
     *
     * @param termName the term name
     *
     * @return the pid
     */
    public int getPID(String termName) {
        return getPID(termName, false);
    }

    /**
     * Gets pid.
     *
     * @param termName  the term name
     * @param updatePID the update pid
     *
     * @return the pid
     */
    public int getPID(String termName, boolean updatePID) {
        int pid = -1;
        if (!updatePID && sessionManager != null) {
            pid = sessionManager.getPID(termName);
        }
        if (pid > 0) {
            return pid;
        } else {
            int[] pidAndSessionID = PSUtils.getPIDAndSessionID(termName, mstsc);
            if (updatePID) {
                pid = sessionManager.getPID(termName);
                if (pid != pidAndSessionID[0]) {
                    sessionManager.removeTermProcessOrPID(termName, pid);
                }
            }
            return pidAndSessionID[0];
        }
    }

    /**
     * Gets pid start time.
     *
     * @param pid the pid
     *
     * @return the pid start time
     */
    public long getPIDStartTime(int pid) {
        Long start = null;
        if (sessionManager != null) {
            start = sessionManager.getPIDStartTime(pid);
        }
        if (start == null) {
            return System.currentTimeMillis();
        } else {
            return start;
        }
    }

    private static String getCmdName(int cmd) {
        switch (cmd) {
            case 0:
                return "iBars";
            case 1:
                return "iBarShift";
            case 2:
                return "iClose";
            case 3:
                return "iHigh";
            case 4:
                return "iLow";
            case 5:
                return "iOpen";
            case 6:
                return "iVolume";
            case 7:
                return "iTime";
            case 8:
                return "iLowest";
            case 9:
                return "iHighest";
            case 10:
                return "AccountBalance";
            case 11:
                return "AccountCredit";
            case 12:
                return "AccountCompany";
            case 13:
                return "AccountCurrency";
            case 14:
                return "AccountEquity";
            case 15:
                return "AccountFreeMargin";
            case 16:
                return "AccountMargin";
            case 17:
                return "AccountName";
            case 18:
                return "AccountNumber";
            case 19:
                return "AccountProfit";
            case 20:
                return "GetLastError";
            case 21:
                return "IsConnected";
            case 22:
                return "IsDemo";
            case 23:
                return "IsTesting";
            case 24:
                return "IsVisualMode";
            case 25:
                return "GetTickCount";
            case 26:
                return "Comment";
            case 27:
                return "MarketInfo";
            case 28:
                return "Print";
            case 29:
                return "Day";
            case 30:
                return "DayOfWeek";
            case 31:
                return "DayOfYear";
            case 32:
                return "Hour";
            case 33:
                return "Minute";
            case 34:
                return "Month";
            case 35:
                return "Seconds";
            case 36:
                return "TimeCurrent";
            case 37:
                return "Year";
            case 38:
                return "ObjectCreate";
            case 39:
                return "ObjectCreate";
            case 40:
                return "ObjectCreate";
            case 41:
                return "ObjectDelete";
            case 42:
                return "ObjectGet";
            case 43:
                return "ObjectSet";
            case 44:
                return "ObjectGetFiboDescription";
            case 45:
                return "ObjectSetFiboDescription";
            case 46:
                return "ObjectSetText";
            case 47:
                return "ObjectsTotal";
            case 48:
                return "ObjectType";
            case 49:
                return "iAC";
            case 50:
                return "iAD";
            case 51:
                return "iAlligator";
            case 52:
                return "iADX";
            case 53:
                return "iATR";
            case 54:
                return "iAO";
            case 55:
                return "iBearsPower";
            case 56:
                return "iBands";
            case 57:
                return "iBullsPower";
            case 58:
                return "iCCI";
            case 59:
                return "iCustom";
            case 60:
                return "iDeMarker";
            case 61:
                return "iEnvelopes";
            case 62:
                return "iForce";
            case 63:
                return "iFractals";
            case 64:
                return "iGator";
            case 65:
                return "iBWMFI";
            case 66:
                return "iMomentum";
            case 67:
                return "iMFI";
            case 68:
                return "iMA";
            case 69:
                return "iOsMA";
            case 70:
                return "iMACD";
            case 71:
                return "iOBV";
            case 72:
                return "iSAR";
            case 73:
                return "iRSI";
            case 74:
                return "iRVI";
            case 75:
                return "iStdDev";
            case 76:
                return "iStochastic";
            case 77:
                return "iWPR";
            case 78:
                return "OrderClose";
            case 79:
                return "OrderCloseBy";
            case 80:
                return "OrderClosePrice";
            case 81:
                return "OrderCloseTime";
            case 82:
                return "OrderComment";
            case 83:
                return "OrderCommission";
            case 84:
                return "OrderDelete";
            case 85:
                return "OrderExpiration";
            case 86:
                return "OrderLots";
            case 87:
                return "OrderMagicNumber";
            case 88:
                return "OrderModify";
            case 89:
                return "OrderOpenPrice";
            case 90:
                return "OrderOpenTime";
            case 91:
                return "OrderPrint";
            case 92:
                return "OrderProfit";
            case 93:
                return "OrderSelect";
            case 94:
                return "OrderSend";
            case 95:
                return "OrdersHistoryTotal";
            case 96:
                return "OrderStopLoss";
            case 97:
                return "OrdersTotal";
            case 98:
                return "OrderSwap";
            case 99:
                return "OrderSymbol";
            case 100:
                return "OrderTakeProfit";
            case 101:
                return "OrderTicket";
            case 102:
                return "OrderType";
            case 103:
                return "IsTradeContextBusy";
            case 104:
                return "RefreshRates";
            case 105:
                return "AccountStopoutLevel";
            case 106:
                return "AccountStopoutMode";
            case 107:
                return "MessageBox";
            case 108:
                return "UninitializeReason";
            case 109:
                return "IsTradeAllowed";
            case 110:
                return "IsStopped";
            case 111:
                return "IsOptimization";
            case 112:
                return "IsLibrariesAllowed";
            case 113:
                return "IsDllsAllowed";
            case 114:
                return "IsExpertEnabled";
            case 115:
                return "AccountFreeMarginCheck";
            case 116:
                return "AccountFreeMarginMode";
            case 117:
                return "AccountLeverage";
            case 118:
                return "AccountServer";
            case 119:
                return "TerminalCompany";
            case 120:
                return "TerminalName";
            case 121:
                return "TerminalPath";
            case 122:
                return "Alert";
            case 123:
                return "PlaySound";
            case 124:
                return "ObjectDescription";
            case 125:
                return "ObjectFind";
            case 126:
                return "ObjectGetShiftByValue";
            case 127:
                return "ObjectGetValueByShift";
            case 128:
                return "ObjectMove";
            case 129:
                return "ObjectName";
            case 130:
                return "ObjectsDeleteAll";
            case 131:
                return "iIchimoku";
            case 132:
                return "HideTestIndicators";
            case 133:
                return "Period";
            case 134:
                return "Symbol";
            case 135:
                return "WindowBarsPerChart";
            case 136:
                return "WindowFirstVisibleBar";
            case 137:
                return "WindowExpertName";
            case 138:
                return "WindowFind";
            case 139:
                return "WindowIsVisible";
            case 140:
                return "WindowPriceMax";
            case 141:
                return "WindowPriceMin";
            case 142:
                return "WindowOnDropped";
            case 143:
                return "WindowXOnDropped";
            case 144:
                return "WindowYOnDropped";
            case 145:
                return "WindowPriceOnDropped";
            case 146:
                return "WindowTimeOnDropped";
            case 147:
                return "WindowsTotal";
            case 148:
                return "WindowRedraw";
            case 149:
                return "WindowScreenShot";
            case 150:
                return "WindowHandle";
            case 151:
                return "GlobalVariableCheck";
            case 152:
                return "GlobalVariableDel";
            case 153:
                return "GlobalVariableGet";
            case 154:
                return "GlobalVariableName";
            case 155:
                return "GlobalVariableSet";
            case 156:
                return "GlobalVariableSetOnCondition";
            case 157:
                return "GlobalVariablesDeleteAll";
            case 158:
                return "GlobalVariablesTotal";
            case 159:
                return "SymbolsTotal";
            case 160:
                return "SymbolName";
            case 161:
                return "SymbolSelect";
            case 162:
                return "TerminalClose";
            case 163:
                return "SymbolInfo";
            case 164:
                return "AccountInfo";
            case 165:
                return "serverTimeGMTOffset";
            case 166:
                return "IsTradeAllowed";
            case 10000:
                return "SetAutoRefresh";
            case 10001:
                return "MarketInfoAll";
            case 10002:
                return "NewTick";
            case 10012:
                return "TicksListenerAll";
            case 10003:
                return "OrderGet";
            case 10004:
                return "NewPosition";
            case 10005:
                return "OrderGetAll";
        }
        return "CMD" + cmd;
    }

    /**
     * Gets statistics by pid.
     *
     * @param pid  the pid
     * @param path the path
     *
     * @return the statistics by pid
     */
    public boolean getStatisticsByPID(int pid, String path) {
        String socketLog = path.replace("terminal.exe", "logs\\stats.log");
        try {
            final String prefix = String.format("%06d ", pid);
            new InputStreamManager(new FileInputStream(socketLog)).readASCIILines(new LineListener() {
                @Override
                public void onLine(int lineNb, StringBuffer line) {
                    String s = line.toString();
                    int ix = s.indexOf('>');
                    if (ix > 0) {
                        String[] stats = s.substring(ix + 2).split(",");
                        if (stats.length > 1) {
                            StringBuilder sb = new StringBuilder(getCmdName(Integer.parseInt(stats[0])));
                            for (int i = 1; i < stats.length; i++) {
                                String stat = stats[i];
                                sb.append("    ").append(stat);
                            }
                            LOGGER.info(sb.toString());
                        }
                    }
                }
            });
            return true;
        } catch (IOException e) {
            LOGGER.error("Error getting statistics for PID=" + pid + ", path=" + path);
        }
        return false;
    }

    /**
     * Is expert installed boolean.
     *
     * @param _eaName the ea name
     *
     * @return the boolean
     */
    public static boolean isExpertInstalled(String _eaName) {
        String eaFileName = eaToFileName(_eaName);
        File file = new File(JFX_HOME_EXPERTS_DIR + "/" + eaFileName);
        if (file.exists()) {
            if (!file.isDirectory()) {
                return true;
            }
            File ea = new File(JFX_HOME_EXPERTS_DIR + "/" + eaFileName + "/Experts/" + eaFileName);
            return ea.exists();
        }
        return false;
    }

    /**
     * Is indicator installed boolean.
     *
     * @param _indicatorName the indicator name
     *
     * @return the boolean
     */
    public static boolean isIndicatorInstalled(String _indicatorName) {
        String indicatorFileName = indicatorToFileName(_indicatorName);
        File file = new File(JFX_HOME_INDICATORS_DIR + "/" + indicatorFileName);
        if (file.exists()) {
            if (!file.isDirectory()) {
                return true;
            }
            File ea = new File(JFX_HOME_INDICATORS_DIR + "/" + indicatorFileName + "/Indicators/" + indicatorFileName);
            return ea.exists();
        }
        return false;
    }

    /**
     * Gets installed experts.
     *
     * @return the installed experts
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    public static ArrayList<String> getInstalledExperts() throws IOException {
        ArrayList<String> res = new ArrayList<>();
        for (String ea : new File(JFX_HOME_EXPERTS_DIR).list()) {
            res.add(fileNameToEA(ea));
        }
        return res;
    }

    /**
     * Gets installed indicators.
     *
     * @return the installed indicators
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    public static ArrayList<String> getInstalledIndicators() throws IOException {
        ArrayList<String> res = new ArrayList<>();
        for (String ea : new File(JFX_HOME_INDICATORS_DIR).list()) {
            res.add(fileNameToEA(ea));
        }
        return res;
    }

    private static String eaToFileName(String eaName) {
        return eaName.endsWith(".ex4") ? eaName : eaName + ".ex4";
    }

    private static String indicatorToFileName(String indicatorName) {
        indicatorName = indicatorName.startsWith("_ind_") ? indicatorName : "_ind_" + indicatorName;
        return indicatorName.endsWith(".ex4") ? indicatorName : indicatorName + ".ex4";
    }

    private static String fileNameToEA(String eaFileName) {
        return eaFileName.endsWith(".ex4") ? eaFileName.substring(0, eaFileName.length() - 4) : eaFileName;
    }

    /**
     * Install expert.
     *
     * @param _eaName the ea name
     * @param content the content
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    public static void installExpert(String _eaName, byte[] content) throws IOException {
        String eaFileName = eaToFileName(_eaName);
        File file = new File(JFX_HOME_EXPERTS_DIR + "/" + eaFileName);
        if (file.exists()) {
            if (!file.isDirectory()) {
                Files.write(file.toPath(), content);
            } else {
                File ea = new File(JFX_HOME_EXPERTS_DIR + "/" + eaFileName + "/Experts/" + eaFileName);
                Files.write(ea.toPath(), content);
            }
        } else {
            Files.write(file.toPath(), content);
        }
    }

    /**
     * Install indicator.
     *
     * @param _indicatorName the indicator name
     * @param content        the content
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    public static void installIndicator(String _indicatorName, byte[] content) throws IOException {
        String iFileName = indicatorToFileName(_indicatorName);
        File file = new File(JFX_HOME_INDICATORS_DIR + "/" + iFileName);
        if (file.exists()) {
            if (!file.isDirectory()) {
                Files.write(file.toPath(), content);
            } else {
                File ea = new File(JFX_HOME_INDICATORS_DIR + "/" + iFileName + "/Indicators/" + iFileName);
                Files.write(ea.toPath(), content);
            }
        } else {
            Files.write(file.toPath(), content);
        }
    }

    /**
     * Install expert library.
     *
     * @param _eaName the ea name
     * @param libName the lib name
     * @param content the content
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    public static void installExpertLibrary(String _eaName, String libName, byte[] content) throws IOException {
        String eaFileName = eaToFileName(_eaName);
        File file = new File(JFX_HOME_EXPERTS_DIR + "/" + eaFileName);
        if (file.exists()) {
            if (!file.isDirectory()) {
                byte[] eaContent = Files.readAllBytes(file.toPath());
                file.delete();
                File eaDir = new File(JFX_HOME_EXPERTS_DIR + "/" + eaFileName + "/Experts");
                eaDir.mkdirs();
                Files.write(eaDir.toPath().resolve(eaFileName), eaContent);
            }
            File libDir = new File(JFX_HOME_EXPERTS_DIR + "/" + eaFileName + "/Libraries");
            libDir.mkdirs();
            Files.write(libDir.toPath().resolve(libName), content);
        } else {
            throw new IOException("'" + eaFileName + "' expert's installation does not exist.");
        }
    }

    /**
     * Copy expert to.
     *
     * @param _eaName the ea name
     * @param termDir the term dir
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    public static void copyExpertTo(String _eaName, final String termDir) throws IOException {
        String eaFileName = eaToFileName(_eaName);
        final File file = new File(JFX_HOME_EXPERTS_DIR + "/" + eaFileName);
        if (file.exists()) {
            if (file.isDirectory()) {
                Files.walkFileTree(file.toPath(), new FileVisitor<Path>() {
                    Path mql4 = null;

                    @Override
                    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                        mql4 = mql4 == null ? Paths.get(termDir + "/MQL4") : mql4.resolve(path.getFileName());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                        Files.copy(path, mql4.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                        return FileVisitResult.TERMINATE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
                        mql4 = mql4.getParent();
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                Files.copy(file.toPath(), Paths.get(termDir + "/MQL4/Experts").resolve(eaFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            throw new IOException("'" + eaFileName + "' expert's installation does not exist: " + file.getAbsolutePath());
        }
    }

    /**
     * Copy indicator to.
     *
     * @param _indicatorName the indicator name
     * @param termDir        the term dir
     *
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     * @exception IOException the io exception
     */
    public static void copyIndicatorTo(String _indicatorName, final String termDir) throws IOException {
        String indicatorFileName = indicatorToFileName(_indicatorName);
        final File file = new File(JFX_HOME_INDICATORS_DIR + "/" + indicatorFileName);
        if (file.exists()) {
            if (file.isDirectory()) {
                Files.walkFileTree(file.toPath(), new FileVisitor<Path>() {
                    Path mql4 = null;

                    @Override
                    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                        mql4 = mql4 == null ? Paths.get(termDir + "/MQL4") : mql4.resolve(path.getFileName());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                        Files.copy(path, mql4.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                        return FileVisitResult.TERMINATE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
                        mql4 = mql4.getParent();
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                Files.copy(file.toPath(), Paths.get(termDir + "/MQL4/Indicators").resolve(indicatorFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            throw new IOException("'" + indicatorFileName + "' indicator's installation does not exist: " + file.getAbsolutePath());
        }
    }

    /**
     * The type Mt 4 module.
     */
    public static class Mt4Module {
        private final String passwordHash;
        /**
         * The Is check required.
         */
        public boolean isCheckRequired = true;
        /**
         * The Module.
         */
        public final String module;
        /**
         * The Check future.
         */
        public ScheduledFuture<?> checkFuture;

        /**
         * Instantiates a new Mt 4 module.
         *
         * @param tp the tp
         */
        public Mt4Module(TerminalParams tp) {
            module = tp.getTerminalProcessFullPathName();
            passwordHash = MD5.MD5(tp.pass);
        }
    }

    private static ConcurrentHashMap<String, Mt4Module> modulePassword = new ConcurrentHashMap<>(); // module -> passwdHash
    private static ConcurrentHashMap<String, Mt4Module> incomingConnectionModule = new ConcurrentHashMap<>(); // conn_id -> module
    private static ConcurrentHashMap<String, String> incomingConnectionError = new ConcurrentHashMap<>(); // conn_id -> error

    /**
     * Register incoming connection module 0 string.
     *
     * @param tp           the tp
     * @param clientWorker the client worker
     *
     * @return the string
     */
    public static String registerIncomingConnectionModule0(final TerminalParams tp, final ClientWorker clientWorker) {
        boolean isRunning = false;
        if (tp.isNj4xParams()) {
            Mt4Module mt4Module = new Mt4Module(tp);
            if (isRunning) {
                Mt4Module absent = modulePassword.putIfAbsent(mt4Module.module, mt4Module);
                if (absent != null && !absent.passwordHash.equals(mt4Module.passwordHash)) {
//                    throw new RuntimeException("Already connecting with a different credentials");
                    return "NOK, Terminal is already started with a different credentials";
                }
                mt4Module = absent == null ? mt4Module : absent;
            } else {
                modulePassword.put(mt4Module.module, mt4Module);
            }
            //
            incomingConnectionModule.put(tp.strategy, mt4Module);
        }
        return isRunning ? "OK, running" : "OK, started";
    }

    /**
     * Register incoming connection module 1 string.
     *
     * @param tp           the tp
     * @param clientWorker the client worker
     *
     * @return the string
     */
    public static String registerIncomingConnectionModule1(final TerminalParams tp, final ClientWorker clientWorker) {
        if (tp.isNj4xParams()) {
            Mt4Module mt4Module = modulePassword.get(tp.getTerminalProcessFullPathName());
            //
            if (mt4Module.isCheckRequired) {
                scheduledExecutorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        Mt4Module mt4Module = incomingConnectionModule.get(tp.strategy);
                        if (mt4Module.isCheckRequired) {
                            try {
                                String status = tp.checkTerminal(clientWorker);
                                if (status.startsWith("OK")) {
                                    scheduledExecutorService.schedule(this, 5, TimeUnit.SECONDS);
                                } else {
                                    incomingConnectionError.put(tp.strategy, status);
                                }
                            } catch (NoSrvConnection e) {
                                String m = "No connection to server: " + e;
                                TS.LOGGER.error(m, e);
                                incomingConnectionError.put(tp.strategy, m);
                            } catch (SrvFileNotFound e) {
                                String m = "SRV file not found: " + e;
                                TS.LOGGER.error(m, e);
                                incomingConnectionError.put(tp.strategy, m);
                            } catch (MaxNumberOfTerminalsReached e) {
                                String m = "Reached max number of terminals: " + e;
                                TS.LOGGER.error(m, e);
                                incomingConnectionError.put(tp.strategy, m);
                            } catch (InvalidUserNamePassword e) {
                                String m = "Invalid user name or password: " + e;
                                TS.LOGGER.error(m, e);
                                incomingConnectionError.put(tp.strategy, m);
                            } catch (TerminalInstallationIsRequired e) {
                                String m = e.getMessage();
                                TS.LOGGER.error(m, e);
                                incomingConnectionError.put(tp.strategy, m);
                            } catch (Throwable e) {
                                e.printStackTrace();
                                String m = "Unexpected error: " + e;
                                TS.LOGGER.error(m, e);
                                incomingConnectionError.put(tp.strategy, m);
                            }
                        }
                    }
                }, 10, TimeUnit.SECONDS);
            }
        }
        return "OK, started";
    }

    /**
     * Register incoming connection module string.
     *
     * @param tp           the tp
     * @param clientWorker the client worker
     * @param isRunning    the is running
     *
     * @return the string
     */
    public static String registerIncomingConnectionModule(final TerminalParams tp, final ClientWorker clientWorker, boolean isRunning) {
        if (tp.isNj4xParams()) {
            Mt4Module mt4Module = new Mt4Module(tp);
            if (isRunning) {
                Mt4Module absent = modulePassword.putIfAbsent(mt4Module.module, mt4Module);
                if (absent != null && !absent.passwordHash.equals(mt4Module.passwordHash)) {
                    return "NOK, Terminal is already started with a different credentials";
                }
                mt4Module = absent == null ? mt4Module : absent;
            } else {
                modulePassword.put(mt4Module.module, mt4Module);
            }
            //
            incomingConnectionModule.put(tp.strategy, mt4Module);
            //
            if (mt4Module.isCheckRequired && mt4Module.checkFuture == null) {
                mt4Module.checkFuture = scheduledExecutorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        Mt4Module mt4Module = incomingConnectionModule.get(tp.strategy);
                        if (mt4Module.isCheckRequired) {
                            try {
                                String status = tp.checkTerminal(clientWorker);
                                if (status.startsWith("OK")) {
                                    mt4Module.checkFuture = scheduledExecutorService.schedule(this, 15, TimeUnit.SECONDS);
                                } else {
                                    mt4Module.checkFuture = null;
                                    incomingConnectionError.put(tp.strategy, status);
                                }
                            } catch (NoSrvConnection e) {
                                String m = "No connection to server: " + e;
                                TS.LOGGER.error(m, e);
                                incomingConnectionError.put(tp.strategy, m);
                            } catch (SrvFileNotFound e) {
                                String m = "SRV file not found: " + e;
                                TS.LOGGER.error(m, e);
                                incomingConnectionError.put(tp.strategy, m);
                            } catch (MaxNumberOfTerminalsReached e) {
                                String m = "Reached max number of terminals: " + e;
                                TS.LOGGER.error(m, e);
                                incomingConnectionError.put(tp.strategy, m);
                            } catch (InvalidUserNamePassword e) {
                                String m = "Invalid user name or password: " + e;
                                TS.LOGGER.error(m, e);
                                incomingConnectionError.put(tp.strategy, m);
                            } catch (TerminalInstallationIsRequired e) {
                                String m = e.getMessage();
                                TS.LOGGER.error(m, e);
                                incomingConnectionError.put(tp.strategy, m);
                            } catch (Throwable e) {
                                e.printStackTrace();
                                String m = "Unexpected error: " + e;
                                TS.LOGGER.error(m, e);
                                incomingConnectionError.put(tp.strategy, m);
                            }
                        }
                    }
                }, 15, TimeUnit.SECONDS);
            }
        }
        return isRunning ? "OK, running" : "OK, started";
    }

    /**
     * Unregister incoming connection module.
     *
     * @param strategy      the strategy
     * @param isTransferred the is transferred
     */
// module -> okPasswordHash
    public static void unregisterIncomingConnectionModule(String strategy, boolean isTransferred) {
        incomingConnectionModule.remove(strategy);
        incomingConnectionError.remove(strategy);
    }

    /**
     * Gets incoming connection module.
     *
     * @param strategy the strategy
     *
     * @return the incoming connection module
     */
    public static Mt4Module getIncomingConnectionModule(String strategy) {
        return incomingConnectionModule.get(strategy);
    }

    /**
     * Check incoming connection error string.
     *
     * @param strategy the strategy
     *
     * @return the string
     */
    public static String checkIncomingConnectionError(String strategy) {
        return incomingConnectionError.get(strategy);
    }

    private static class SearchFilePathVisitor implements FileVisitor<Path> {
        /**
         * The File found.
         */
        public File fileFound = null;
        private String selectedFileName;

        /**
         * Instantiates a new Search file path visitor.
         *
         * @param selectedFileName the selected file name
         */
        public SearchFilePathVisitor(String selectedFileName) {
            this.selectedFileName = selectedFileName;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return fileFound != null ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String fileName = file.getName(file.getNameCount() - 1).toString();
            if (fileName.equals(selectedFileName)) {
                fileFound = file.toFile();
            }
            return fileFound != null ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return fileFound != null ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return fileFound != null ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
        }
    }
}