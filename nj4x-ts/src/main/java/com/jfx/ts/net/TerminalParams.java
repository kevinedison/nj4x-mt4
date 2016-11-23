package com.jfx.ts.net;

import com.jfx.ts.io.Log4JUtil;
import com.jfx.ts.io.ResourceReader;
import com.jfx.ts.net.ws.dto.Nj4xChartParams;
import com.jfx.ts.net.ws.dto.Nj4xMT4Account;
import com.jfx.ts.net.ws.dto.Nj4xParams;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * 记录这个是终端的参数，就是描述终端的
 * User: roman
 * Date: 06/08/2014
 * Time: 09:44
 */
public class TerminalParams {
    public static final String MT4IF_DLL = "mt45if.dll";
    public static final String MT4IF_XP_DLL = "mt45if_xp.dll";
    public static final String MT5IF_DLL = "mt5if.dll";
    public static final String MT5IF_XP_DLL = "mt5if_xp.dll";
    public static String SW_HIDE = System.getProperty("SW_HIDE", "true");
    //
    public Date start = new Date();
    public String pass;
    //
    protected String proxyServer, proxyType, proxyUser, proxyPassword;
    protected boolean isMT5, is64BitTerminal;
    protected String srv;
    protected String user;
    protected String strategy;
    protected String jfxHost;
    protected String jfxPort;
    protected String symbol;
    protected String tenant;
    protected String parentTenant;
    protected String termHomeDir;
    protected int historyPeriod, period;
    @SuppressWarnings({"FieldCanBeLocal"})
    int comma1Pos;
    int comma2Pos;
    String params;
    boolean asynchOrdersOperations;
    ArrayList<String> charts;

    TerminalParams(Nj4xMT4Account account, Nj4xParams nj4xParams) {
        proxyServer = account.proxyServer;
        proxyType = account.proxyType;
        proxyUser = account.proxyUser;
        proxyPassword = account.proxyPassword;
        srv = account.srv;
        isMT5 = srv.startsWith("5*");
        srv = isMT5 ? srv.substring(2) : srv;
        user = account.user;
        int atIx = user.indexOf('@');
        if (atIx > 0) {
            termHomeDir = user.substring(atIx + 1);
            user = user.substring(0, atIx);
            String stdDirPrefix = srv.replace(":", "_").replace(" ", "_") + ' ' + user;
            if (!termHomeDir.startsWith(stdDirPrefix)) {
                termHomeDir = (stdDirPrefix + ' ' + termHomeDir).trim();
            }
        }
        pass = account.password;
        //
//        termHomeDir = nj4xParams.termHomeDir != null ? nj4xParams.termHomeDir : termHomeDir;
        //
        jfxHost = nj4xParams.jfxHost;
        jfxPort = String.valueOf(nj4xParams.jfxPort);
        tenant = nj4xParams.tenant;
        //
        asynchOrdersOperations = nj4xParams.asynchOrdersOperations;
        strategy = nj4xParams.strategy;
        parentTenant = nj4xParams.parentTenant;
        symbol = nj4xParams.symbol;
        period = nj4xParams.period;
        historyPeriod = nj4xParams.historyPeriod;
        //
        if (nj4xParams.charts != null && nj4xParams.charts.size() > 0) {
            charts = new ArrayList<>();
            for (Nj4xChartParams chart : nj4xParams.charts) {
                charts.add(chart.toString());
            }
        }
        //
        if (srv == null
                || user == null
                || pass == null
                || strategy == null
                || jfxHost == null
                || jfxPort == null
                ) {
            throw new RuntimeException("Invalid terminal params: " + account + ", " + nj4xParams);
        }
        //
        if (jfxHost.equals("localhost")) {
            jfxHost = "127.0.0.1";
        }
        //
        if (jfxHost.length() > 0 && Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
            TS.LOGGER.info("Using remote address: " + jfxHost + ':' + jfxPort + " for " + this + " connection");
        }
    }

    public TerminalParams(String params) {
        asynchOrdersOperations = params.charAt(0) != ':';
        this.params = asynchOrdersOperations ? params : params.substring(1);
        comma2Pos = -1;
        //
        srv = next();
        {
            int ix = 0;
            if (srv == null) {
                return;
            } else {
                ix = srv.indexOf('@');
            }
            if (ix > 0) {
                String proxyParams = srv.substring(ix + 1);
                String _srv = srv.substring(0, ix);
                ix = proxyParams.indexOf('\u0001');
                if (ix > 0) {
                    srv = _srv;
                    proxyServer = proxyParams.substring(0, ix);
                    proxyParams = proxyParams.substring(ix + 1);
                    ix = proxyParams.indexOf('\u0001');
                    if (ix < 0) return;
                    //
                    proxyType = proxyParams.substring(0, ix);
                    proxyParams = proxyParams.substring(ix + 1);
                    ix = proxyParams.indexOf('\u0001');
                    if (ix < 0) return;
                    //
                    proxyUser = proxyParams.substring(0, ix);
                    proxyParams = proxyParams.substring(ix + 1);
                    ix = proxyParams.indexOf('\u0001');
                    if (ix < 0) return;
                    //
                    proxyPassword = proxyParams.substring(0, ix);
                }
            }
        }
        isMT5 = srv.startsWith("5*");
        srv = isMT5 ? srv.substring(2) : srv;
        user = next();
        int atIx = 0;
        if (user == null) {
            return;
        } else {
            int i = user.lastIndexOf(':');
            if (i > 0) {
                historyPeriod = Integer.parseInt(user.substring(i + 1));
                user = user.substring(0, i);
            }
            atIx = user.indexOf('@');
        }
        if (atIx > 0) {
            termHomeDir = user.substring(atIx + 1);
            user = user.substring(0, atIx);
            String stdDirPrefix = srv.replace(":", "_").replace(" ", "_") + ' ' + user;
            if (!termHomeDir.startsWith(stdDirPrefix)) {
                termHomeDir = (stdDirPrefix + ' ' + termHomeDir).trim();
            }
        }
        pass = next();
        strategy = next();
        jfxHost = next();
        tenant = "";
        parentTenant = "";
        jfxPort = next();
        int ix = 0;
        if (jfxPort == null) {
            return;
        } else {
            ix = jfxPort.indexOf(':');
        }
        if (ix >= 0) {
            tenant = jfxPort.substring(ix + 1);
            jfxPort = jfxPort.substring(0, ix);
            ix = tenant.indexOf('\u0002');
            if (ix > 0) {
                parentTenant = tenant.substring(ix + 1);
                tenant = tenant.substring(0, ix);
            } else {
                parentTenant = tenant;
            }
        }
        symbol = next();
        if (symbol != null && symbol.length() > 0) {
            int i = symbol.lastIndexOf(':');
            if (i >= 0) {
                period = Integer.parseInt(symbol.substring(i + 1));
                symbol = symbol.substring(0, i);
            } else {
                period = 240;
            }
        }
        //
        String sNCharts = next();
        if (sNCharts != null) {
            int nCharts = sNCharts.length() == 0 ? 0 : Integer.parseInt(sNCharts);
            if (nCharts > 0) {
                charts = new ArrayList<>(nCharts);
                for (int i = 0; i < nCharts; i++) {
                    charts.add(next());
                }
            }
        }
        //
        if (srv == null
                || user == null
                || pass == null
                || strategy == null
                || jfxHost == null
                || jfxPort == null
                ) {
            throw new RuntimeException("Invalid terminal params: [" + params + ']');
        }
        if (jfxHost.length() > 0 && Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
            TS.LOGGER.info("Using remote address: " + jfxHost + ':' + jfxPort + " for " + this + " connection");
        }
    }

    protected TerminalParams() {
    }

    public static String maskPassword(String line) {
        String pass = new TerminalParams(line).pass;
        if (pass != null && pass.length() > 0) {
            return line.replace("|" + pass + "|", "*");
        } else {
            return line;
        }
    }

    public static String toX64DLLName(String dllName) {
        return dllName.replace(".dll", "_x64.dll");
    }

    void initTerminalDirectory() throws IOException {
        String dir = getTerminalDirPathName();
        try {
            File rootDir = new File(dir);
            boolean rootDirExists = rootDir.exists();
            if (!rootDirExists) {
                //noinspection ResultOfMethodCallIgnored
                rootDir.mkdirs();
            }
            //
            String zeroTermDirName = isMT5 ? TS.JFX_HOME_ZTERM_MT5_DIR : TS.JFX_HOME_ZTERM_DIR;
            File zeroTermDir = new File(zeroTermDirName);
            File customTermDir = new File(zeroTermDirName.replace("zero_term", "custom_term"));
            File newTerm;
            File newTerm2;
            if (isMT5) {
                newTerm = new File(System.getProperty("user.home").substring(0, 3) + "ProgramData\\MetaQuotes\\WebInstall\\mt5clw\\terminal.exe");
                newTerm2 = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\MetaQuotes\\WebInstall\\mt5clw\\terminal.exe");
            } else {
                newTerm = new File(System.getProperty("user.home").substring(0, 3) + "ProgramData\\MetaQuotes\\WebInstall\\mt4clw\\terminal.exe");
                newTerm2 = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\MetaQuotes\\WebInstall\\mt4clw\\terminal.exe");
            }
            TS.disableLiveUpdateIfNeeded(newTerm);
            TS.disableLiveUpdateIfNeeded(newTerm2);
            TS.copyLiveUpdatedTerminalIfNeeded(newTerm);
            TS.copyLiveUpdatedTerminalIfNeeded(newTerm2);
            //
            new File(dir + "\\config\\accounts.ini").delete();
            new File(dir + "\\config\\community.ini").delete();
            new File(dir + "\\config\\email.ini").delete();
            new File(dir + "\\config\\experts.ini").delete();
            new File(dir + "\\config\\notifications.ini").delete();
            new File(dir + "\\config\\publish.ini").delete();
            new File(dir + "\\config\\server.ini").delete();
            new File(dir + "\\history\\default\\symbols.sel").delete();
            //
            if (isMT5) {
                Path zTerm64Path = zeroTermDir.toPath().resolve("terminal64.exe");
                Path cTerm64Path = customTermDir.toPath().resolve("terminal64.exe");
                is64BitTerminal = zTerm64Path.toFile().exists() || cTerm64Path.toFile().exists();
            }
            //
            copyDir(zeroTermDir, rootDir);
            if (customTermDir.exists()) {
                copyDir(customTermDir, rootDir);
            }
            //
            Path termPath = Paths.get(dir, "terminal.exe");
            if (!termPath.toFile().exists()) {
                if (isMT5) {
                    termPath = Paths.get(dir, "terminal64.exe");
                    if (termPath.toFile().exists()) {
                        is64BitTerminal = true;
                    } else {
                        throw new TerminalInstallationIsRequired("Client terminal does not exist: " + termPath.toString());
                    }
                } else {
                    throw new TerminalInstallationIsRequired("Client terminal does not exist: " + termPath.toString());
                }
            }
            //
            Path terminalIniPath = rootDir.toPath().resolve("config/terminal.ini");
            byte[] terminalIniBytes = Files.readAllBytes(terminalIniPath);
            String terminalIni = new String(terminalIniBytes);
            String newTerminalIni = fixBuildNumber(terminalIni, "LastBuildDataPath");
            if (isMT5) {
                newTerminalIni = fixBuildNumber(newTerminalIni, "LastBuild");
                newTerminalIni = fixBuildNumber(newTerminalIni, "LastBuildIDE");
                newTerminalIni = fixBuildNumber(newTerminalIni, "LastBuildMQL");
                newTerminalIni = fixBuildNumber(newTerminalIni, "LastBuildTester");
            }
            if (historyPeriod != 0) {
                String hp = "\nHistoryPeriod=";
                int i = newTerminalIni.indexOf(hp);
                if (i > 0) {
                    newTerminalIni = newTerminalIni.substring(0, i + hp.length()) + historyPeriod + '\r'
                            + newTerminalIni.substring(newTerminalIni.indexOf('\n', i));
                } else {
                    String settings = "[Settings]";
                    i = newTerminalIni.indexOf(settings);
                    if (i > 0) {
                        newTerminalIni = newTerminalIni.substring(0, i) + settings + "\r\nHistoryPeriod=" + historyPeriod + newTerminalIni.substring(i + settings.length());
                    } else {
                        TS.LOGGER.warn("Can not find terminal.ini [Settings] section: " + this);
                    }
                }
            }
            if (!terminalIni.equals(newTerminalIni)) {
                if (TS.LOGGER.isDebugEnabled()) {
                    TS.LOGGER.debug("Fixing mt4 terminal build number (" + (isMT5 ? TS.BUILD_MT5 : TS.BUILD_MT4) + ") in " + terminalIniPath.toString());
                }
                Files.write(terminalIniPath, newTerminalIni.getBytes());
            }
            //
            File eaCfgDir = new File(TS.JFX_HOME_EA_DIR);
            if (eaCfgDir.exists()) {
                File termExpertsDir = new File(rootDir, (isMT5 ? "/MQL5/Experts" : "/MQL4/Experts"));
                File[] files = eaCfgDir.listFiles();
                if (files != null) {
                    //noinspection ForLoopReplaceableByForEach
                    for (int i = 0; i < files.length; i++) {
                        File from = files[i];
                        String name = from.getName();
                        if (name.endsWith(isMT5 ? ".ex5" : ".ex4")) {
                            File to = new File(termExpertsDir, name);
                            if (!to.exists() || from.lastModified() > to.lastModified()) {
                                copyFile(from, to.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            TS.LOGGER.error("Error creating dir: " + dir, t);
            throw new IOException("" + t);
        }
    }

    private String fixBuildNumber(String terminalIni, String lbdp) {
        String newTerminalIni = null;
        lbdp = lbdp + '=';
        String marker = lbdp + (isMT5 ? TS.BUILD_MT5 : TS.BUILD_MT4);
        if (!terminalIni.contains(marker)) {
            int ix = terminalIni.indexOf(lbdp);
            if (ix > 0) {
                int ix2 = terminalIni.indexOf("\r\n", ix);
                ix2 = ix2 < 0 ? terminalIni.indexOf("\n", ix) : ix2;
                newTerminalIni = terminalIni.substring(0, ix) + marker + terminalIni.substring(ix2);
            }
        }
        return newTerminalIni == null ? terminalIni : newTerminalIni;
    }

    protected void copyFile(File from, String toPathName) throws IOException {
        if (from != null) {
            String nameInLowerCase = from.getName().toLowerCase();
            boolean isFXT = nameInLowerCase.endsWith(".fxt");
            boolean isHST = nameInLowerCase.endsWith(".hst");
            boolean isHistory = isFXT || isHST || toPathName.contains("history");
            if (isHistory) {
                return;
            }
            copyFile(from, toPathName, nameInLowerCase, false);
        }
    }

    protected void copyFile(File from, String toPathName, String nameInLowerCase, boolean isHistory) throws IOException {
        boolean isPathModified = false;
        String mt4ifDLL = MT4IfDll();
        String mt4XPIfDLL = MT4IfXPDll();
        if (is64BitTerminal) {
            String mt4Ifx64dll = toX64DLLName(TS.IS_WIN_XP ? mt4XPIfDLL : mt4ifDLL);
            if (nameInLowerCase.equals(mt4ifDLL)
                    && new File(from.getParent() + "/" + mt4Ifx64dll).exists()
                    ) {
                TS.LOGGER.debug("Skip " + from);
                return;
            }
            if (nameInLowerCase.equals(mt4Ifx64dll)) {
                toPathName = toPathName.replace(mt4Ifx64dll, mt4ifDLL);
                TS.LOGGER.debug("Copying " + from + " to " + toPathName);
                isPathModified = true;
            }
            //
            if (nameInLowerCase.equals("terminal64.exe")) {
                toPathName = toPathName.replace("terminal64.exe", "terminal.exe");
                TS.LOGGER.debug("Copying " + from + " to " + toPathName);
                isPathModified = true;
            }
        } else if (TS.IS_WIN_XP) {
            if (nameInLowerCase.equals(mt4ifDLL)
                    && new File(from.getParent() + "/" + mt4XPIfDLL).exists()
                    ) {
                TS.LOGGER.debug("Skip " + from);
                return;
            }
            if (nameInLowerCase.equals(mt4XPIfDLL)) {
                toPathName = toPathName.replace(mt4XPIfDLL, mt4ifDLL);
                TS.LOGGER.debug("Copying " + from + " to " + toPathName);
                isPathModified = true;
            }
        }

        Path FROM = Paths.get(from.getAbsolutePath());
        Path TO = Paths.get(toPathName);
        if (isHistory) {
            File fromFile = FROM.toFile();
            File toFile = TO.toFile();
            if (toFile.exists() && toFile.length() == fromFile.length()) {
                TS.LOGGER.debug("Skip copy of existing " + toFile.getAbsolutePath());
                return; // skip copy of potentially large historical files
            }
        }
        if (!isPathModified || !TO.toFile().exists() || TO.toFile().lastModified() < FROM.toFile().lastModified()) {
            //overwrite existing file, if exists
            CopyOption[] options = new CopyOption[]{
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
            };
            long start = System.currentTimeMillis();
            try {
                Files.copy(FROM, TO, options);
            } catch (IOException e) {
                if (FROM.toFile().isFile() && TO.toFile().isDirectory()
                        || FROM.toFile().isDirectory() && TO.toFile().isFile()
                        ) {
                    if (TO.toFile().isDirectory()) {
                        TS.deleteDirectory(TO.toFile());
                    } else {
                        TO.toFile().delete();
                    }
                    Files.copy(FROM, TO, options);
                } else {
                    throw e;
                }
            }
            long time = System.currentTimeMillis() - start;
            if (time > 1000) {
                TS.LOGGER.info("File " + TO + " copied in " + (time / 1000) + " secs");
            }
        }
    }

    private void copyDir(File fromDir, File toDir) throws IOException {
        boolean timing = toDir.equals(new File(getTerminalDirPathName()));
        long start = timing ? System.currentTimeMillis() : 0;
        File[] entries = fromDir.listFiles();
        if (entries != null) {
            for (File file : entries) {
                String fromFileName = file.getName();
                String toPathName = toDir.getAbsolutePath() + '/' + fromFileName;
                if (file.isDirectory()) {
                    File toSubDir = new File(toPathName);
                    if (!toSubDir.exists() && !toSubDir.mkdirs()) {
                        TS.LOGGER.error("Error making directory: " + toSubDir);
                    } else {
                        copyDir(file, toSubDir);
                    }
                } else {
                    File subFile = new File(toPathName);
                    if (!subFile.exists() || subFile.lastModified() < file.lastModified()/* || subFile.length() != file.length()*/) {
                        copyFile(file, toPathName);
                    }
                }
            }
            long end = timing ? System.currentTimeMillis() : 0;
            if (timing && Log4JUtil.isConfigured() && TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Zero term copy time=" + (end - start) + " millis, '" + toDir.getName() + "' from '" + fromDir.getAbsolutePath() + "'");
            }
        }
    }

    @Override
    public String toString() {
        return srv + "/" + user + " (" + tenant + ")";
    }

    private String next() {
        if (comma2Pos == -1024) {
            return null;
        } else {
            comma1Pos = comma2Pos;
            comma2Pos = params.indexOf('|', comma1Pos + 1);
            if (comma2Pos >= 0) {
                return params.substring(comma1Pos + 1, comma2Pos);
            } else {
                comma2Pos = -1024;
                return params.substring(comma1Pos + 1);
            }
        }
    }

    public String getProfilesDefault() {
        return isMT5 ? "/Profiles/Charts/Default/" : "/profiles/default/";
    }

    public int writeTickListenerCharts(String dir, String sym1) throws IOException {
        if (charts == null) {
            return 0;
        } else {
            for (String chartSymbol : charts) {
                String[] args = null;
                int ix = chartSymbol.indexOf(':');
                if (ix > 0) {
                    args = chartSymbol.substring(ix + 1).split(":");
                    chartSymbol = chartSymbol.substring(0, ix);
                }
                if (chartSymbol.startsWith(TS.SPCH)) {
                    if (args != null && args.length == 1) {
                        try {
                            Double.parseDouble(args[0]);
                        } catch (NumberFormatException nAn) {
                            sym1 = args[0];
                            args = null;
                        }
                    }
                    writeChart("chart01" + (isMT5 ? "_mt5" : ""), chartSymbol, sym1, dir + getProfilesDefault() + chartSymbol + ".chr", args);
                } else {
                    writeChart("chart01" + (isMT5 ? "_mt5" : ""), chartSymbol, chartSymbol, dir + getProfilesDefault() + chartSymbol + ".chr", args);
                }
            }
            return charts.size();
        }
    }

    private void writeChart(String chartName, String suffix, String symbol, String fName, String[] args) throws IOException {
        String ii = ResourceReader.getClassResourceReader(TerminalServer.class, true).getProperty(chartName);
        ii = ii.replace("$DEBUG_DLL$", TS.LOGGER.isDebugEnabled() ? "1" : "0");
        ii = ii.replace("$jfx_host$", jfxHost);
        ii = ii.replace("$jfx_port$", jfxPort);
        ii = ii.replace("$strategy$", strategy + '|' + suffix);
        ii = ii.replace("$symbol$", symbol);
        ii = ii.replace("$param1$", suffix.equals(symbol) || suffix.equals(TS.SPCH + "_T_LSNR") ? "1" : "0");
        ii = ii.replace("period=240", "period=" + period);
        int initParamNo = 2;
        if (args != null) {
            //noinspection ForLoopReplaceableByForEach
            for (String arg : args) {
                ii = ii.replace(" ; param" + initParamNo, "param" + initParamNo + "=" + arg);
                initParamNo++;
                if (initParamNo > 10) {
                    break;
                }
            }
        }
        TS.writeFile(fName, ii.getBytes());
    }

    public File getSymbolsFile(String dir) {
        if (isMT5) {
            File bases = new File(dir + "/Bases");
            if (bases.exists()) {
                File[] files = bases.listFiles();
                if (files != null) {
                    //noinspection ForLoopReplaceableByForEach
                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        if (file.isDirectory()) {
                            String pathname = file.getAbsolutePath() + "/symbols/symbols-" + user + ".dat";
                            File symRawFile = new File(pathname);
                            if (symRawFile.exists() && symRawFile.length() > 0) {
                                return symRawFile;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public String getTerminalProcessFullPathName() {
        return getTerminalDirPathName() + "\\terminal.exe";
    }

    public String getTerminalDirPathName() {
        return TS.getTermDir() + getTerminalDirectoryLastName();
    }

    public String getTerminalDirectoryLastName() {
        return getTerminalDirectoryLastName(false);
    }

    public String getTerminalDirectoryNoTenantLastName() {
        return getTerminalDirectoryLastName(true);
    }

    public static String brokerToDirName(String broker) {
        return broker.replace(":", "_").replace(" ", "_").replace(".", "_");
    }

    public String getTerminalDirectoryLastName(boolean noTenant) {
        String t = noTenant ? parentTenant : tenant;
        String termDir = brokerToDirName(this.srv)
                + ' ' + user
                + (jfxHost == null || jfxHost.length() == 0 || isLocalhost() ? "" : " " + jfxHost.replace(".", "_"))
                + (jfxPort == null || jfxPort.length() == 0 || jfxPort.equals("7777") ? "" : " " + jfxPort)
                + (t == null || t.length() == 0 ? "" : " " + t).replace(".", "_");
        return '\\' + (termHomeDir == null ? termDir : termHomeDir);
    }

    protected boolean isLocalhost() {
        return jfxHost.equals("127.0.0.1") || jfxHost.equals("*");
    }

    public void prepareCharts(String dir, String sym1, String sym2) throws IOException {
        cleanChartsDir(dir);
        //
        writeChartStd(dir, "chart01" + (isMT5 ? "_mt5" : ""), sym1);
        writeChartStd(dir, "chart02" + (isMT5 ? "_mt5" : ""), sym2);
        writeTickListenerCharts(dir, sym1);
        // todo: implement TickListener charts
        File chrCfgDir = new File(TS.JFX_HOME_CHR_DIR);
        if (chrCfgDir.exists() && (tenant == null || tenant.length() == 0)) {
            File termProfileDir = new File(dir + getProfilesDefault());
            File[] files = chrCfgDir.listFiles();
            if (files != null) {
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < files.length; i++) {
                    File from = files[i];
                    File to = new File(termProfileDir, "chart" + (i < 7 ? "0" : "") + (i + 3) + ".chr");
                    if (!to.exists()
                            || from != null && from.lastModified() > to.lastModified()
                            || from != null && from.length() != to.length()) {
                        copyFile(from, to.getAbsolutePath());
                    }
                }
            }
        }
    }

    protected void cleanChartsDir(String dir) throws IOException {
        Files.walkFileTree(Paths.get(dir), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.endsWith(".chr") || file.endsWith(".CHR")) {
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
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

    private void writeChartStd(String dir, String chartName, String symbol) throws IOException {
        String fName = dir + getProfilesDefault() + chartName + ".chr";
        if (asynchOrdersOperations || !chartName.startsWith("chart02")) {
            writeChart(chartName, symbol, fName);
        } else {
            //noinspection ResultOfMethodCallIgnored
            new File(fName).delete();
        }
    }

    protected void writeChart(String chartName, String symbol, String fName) throws IOException {
        String ii = ResourceReader.getClassResourceReader(TerminalServer.class, true).getProperty(chartName);
        ii = ii.replace("$sw_hide$", SW_HIDE);
        ii = ii.replace("$DEBUG_DLL$", TS.LOGGER.isDebugEnabled() ? "1" : "0");
        ii = ii.replace("$jfx_host$", jfxHost);
        ii = ii.replace("$jfx_port$", jfxPort);
        ii = ii.replace("$strategy$", strategy + chartName.substring("chart0".length(), "chart0".length() + 1));
        ii = ii.replace("$symbol$", (this.symbol == null || this.symbol.length() == 0 ? symbol : this.symbol));
        ii = ii.replace("$param1$", (this.symbol == null || this.symbol.length() == 0 ? "0" : "1"));
        ii = ii.replace("period=240", "period=" + period);
        if (isNj4xParams()) {
            ii = ii.replace("name=jfx", "name=nj4x");
        }
        TS.writeFile(fName, isMT5 ? TS.switchEndianess(ii.getBytes(Charset.forName("Unicode"))) : ii.getBytes());
    }

    public boolean isNj4xParams() {
        return jfxHost.length() == 0 && jfxPort.length() == 0;
    }

    public String runTerminal(boolean longRun, final ClientWorker clientWorker) throws IOException {
        TS ts = clientWorker.ts;
        //
        boolean b = !(strategy.length() == 32 && strategy.matches("[\\dABCDEF]+")) && !isNj4xParams();
        if (TS.LOGGER.isDebugEnabled() && !longRun && b) {
            TS.LOGGER.debug("Strategy=[" + strategy + "] len=" + strategy.length() + " match(\\dABCDEF)=" + strategy.matches("[\\dABCDEF]+"));
        }
        longRun |= b;
        //
        final String dir = TS.getTermDir() + getTerminalDirectoryLastName();
        //
        boolean ok;
        final String module = getTerminalProcessFullPathName();
        //
        ScheduledFuture schedule = TS.terminations.remove(module);
        if (schedule != null) {
            schedule.cancel(false);
        }
        //
        b = true;
        try {
            if (b = ClientWorker.lockTerminalProcessing(module)) return null;
            //
            SymbolsXML symbolsXML = new SymbolsXML(clientWorker, this, TS.getTermDir() + getTerminalDirectoryNoTenantLastName());
            if (clientWorker.isOkAndRunning(module, this, longRun)) {
                String res = TS.registerIncomingConnectionModule(this, clientWorker, true);
                if (res.startsWith("OK, ")) {
                    return res;
                } else {
                    TS.LOGGER.warn(module + " -> " + res);
                    ts.killProcessUngracefully(module);
                }
            }
            //
            boolean checkLogon = checkLogon();
            TS.registerIncomingConnectionModule0(this, clientWorker);
            if (clientWorker.startTermCheckLogon(dir, module, this, symbolsXML, checkLogon)) {
                return TS.registerIncomingConnectionModule1(this, clientWorker);
            } else {
                clientWorker.deleteIniFile(dir, "init.ini");
                return "NOK, can not start terminal";
            }
        } finally {
            if (!b) {
                ClientWorker.unlockTerminalProcessing(module);
            }
        }
    }

    public void runTerminal(boolean longRun, final ClientWorkerHdlr cwh) throws IOException {
        runTerminal(longRun, cwh, false);
    }

//    private static ConcurrentHashMap<String, ClientWorkerHdlr> runTermSchedules = new ConcurrentHashMap<>();
    private void runTerminal(boolean longRun, ClientWorkerHdlr cwh, boolean isContinuing) throws IOException {
        final String module = getTerminalProcessFullPathName();
//        if (!cwh.isActive()) {
//            cwh = runTermSchedules.get(module);
//            TS.LOGGER.warn(module + " :: RUNTERM - strategy [" + strategy + "] is inactive, using latest hdlr: " + cwh);
//        }
        if (cwh == null || !cwh.isActive()) {
            TS.LOGGER.warn(module + " :: Skip RUNTERM - strategy [" + strategy + "] is inactive.");
            return;
        }
        //
        ClientWorker clientWorker = cwh.getClientWorker();
        TS ts = clientWorker.ts;
        //
        boolean b = !(strategy.length() == 32 && strategy.matches("[\\dABCDEF]+")) && !isNj4xParams();
        if (TS.LOGGER.isDebugEnabled() && !longRun && b) {
            TS.LOGGER.debug("Strategy=[" + strategy + "] len=" + strategy.length() + " match(\\dABCDEF)=" + strategy.matches("[\\dABCDEF]+"));
        }
        longRun |= b;
        //
        final String dir = TS.getTermDir() + getTerminalDirectoryLastName();
        //
        boolean ok;
        //
        ScheduledFuture schedule = TS.terminations.remove(module);
        if (schedule != null) {
            schedule.cancel(false);
        }
        //
        b = true;
        try {
            if (b = ClientWorker.lockTerminalProcessing(module)) return;
            //
            SymbolsXML symbolsXML = new SymbolsXML(clientWorker, this, TS.getTermDir() + getTerminalDirectoryNoTenantLastName());
            if (clientWorker.isOkAndRunning(module, this, longRun)) {
                String res = TS.registerIncomingConnectionModule(this, clientWorker, true);
                if (res.startsWith("OK, ")) {
                    cwh.sendToClient(res);
                    return;
                } else {
                    TS.LOGGER.warn(module + " -> " + res);
                    ts.killProcessUngracefully(module);
                }
            }
            //
            if (isContinuing) {
                long ttr = System.currentTimeMillis() - start.getTime(); // time to start
                if (ttr > 10000) {
                    TS.LOGGER.warn("" + this + " :: TTR (Time to RUNTERM) " + ttr + " millis"
                            + ", runterm_threads=" + TerminalServer.MAX_TERMINAL_STARTUP_THREADS
                    );
                }
                start = new Date();
                boolean checkLogon = checkLogon();
                TS.registerIncomingConnectionModule0(this, clientWorker);
                if (clientWorker.startTermCheckLogon(dir, module, this, symbolsXML, checkLogon)) {
                    cwh.sendToClient(TS.registerIncomingConnectionModule1(this, clientWorker));
                } else {
                    clientWorker.deleteIniFile(dir, "init.ini");
                    cwh.sendToClient("NOK, can not start terminal");
                }
            } else {
                final boolean finalLongRun = longRun;
                final ClientWorkerHdlr _cwh = cwh;
//                runTermSchedules.put(module, cwh);
                TerminalServer.FIXED_THREAD_POOL.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            runTerminal(finalLongRun, _cwh, true);
                        } catch (NoSrvConnection e) {
                            String m = "No connection to server: " + e;
                            TS.LOGGER.error(m, e);
                            _cwh.sendToClientIgnoreException(m);
                        } catch (SrvFileNotFound e) {
                            String m = "SRV file not found: " + e;
                            TS.LOGGER.error(m, e);
                            _cwh.sendToClientIgnoreException(m);
                        } catch (MaxNumberOfTerminalsReached e) {
                            String m = "Reached max number of terminals: " + e;
                            TS.LOGGER.error(m, e);
                            _cwh.sendToClientIgnoreException(m);
                        } catch (InvalidUserNamePassword e) {
                            String m = "Invalid user name or password: " + e;
                            TS.LOGGER.error(m, e);
                            _cwh.sendToClientIgnoreException(m);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            String m = "Unexpected error: " + e;
                            TS.LOGGER.error(m, e);
                            _cwh.sendToClientIgnoreException(m);
                        }
                    }
                });
                //
                cwh.sendToClient("OK, RUNTERM has been scheduled (" + strategy + ")");
            }
        } finally {
            if (!b) {
                ClientWorker.unlockTerminalProcessing(module);
            }
        }
    }

    public String checkTerminal(final ClientWorker clientWorker) {
        final String terminalProcessName = getTerminalProcessFullPathName();
        //
        boolean b = true;
        try {
            if (b = ClientWorker.lockTerminalProcessing(terminalProcessName)) return "NOK, can not lock terminal";
            //
            if (clientWorker.checkTermLogon(TS.getTermDir() + getTerminalDirectoryLastName(), terminalProcessName, this)) {
                return "OK, started";
            } else {
                return "NOK, can not find terminal";
            }
        } finally {
            if (!b) {
                ClientWorker.unlockTerminalProcessing(terminalProcessName);
            }
        }
    }

    protected SwHide swHide() {
        return SwHide.DEFAULT;
    }

    protected boolean checkLogon() {
        return true;
    }

    public boolean isTesterTerminal() {
        return false;
    }

    public boolean isCustomTerminal() {
        return false;
    }

    public boolean isCloseTerminalAtTheEnd() {
        return false;
    }

    public void writeInitIni(String dir, String fileName) throws IOException {
        int port = Integer.parseInt(System.getProperty("port", "7788")) + 3;// todo get port from ts
        //
        String ii = ResourceReader.getClassResourceReader(TerminalServer.class, true)
                .getProperty("init_" + (isMT5 ? "mt5" : "mt4") + ".ini");
        ii = ii.replace("$mgmt_port$", "" + port);
        ii = ii.replace("$login$", user);
        ii = ii.replace("$password$", pass);
        ii = ii.replace("$server$", srv);
//            ii = ii.replace("$jfx_host$", tp.jfxHost);
//            ii = ii.replace("$jfx_port$", tp.jfxPort);
//            ii = ii.replace("$strategy$", tp.strategy);
        if (proxyServer != null) {
            ii = ii.replace("ProxyEnable=false", "ProxyEnable=true");
            ii = ii.replace("$ProxyServer$", proxyServer);
            ii = ii.replace("$ProxyType$", proxyType);
            if (proxyUser != null && proxyUser.length() > 0) {
                ii = ii.replace("$ProxyLogin$", proxyUser);
                ii = ii.replace("$ProxyPassword$", proxyPassword);
            } else {
                ii = ii.replace("ProxyLogin=$ProxyLogin$", "ProxyLogin=");
                ii = ii.replace("ProxyPassword=$ProxyPassword$", "ProxyPassword=");
            }
        } else if (ClientWorker.PROXY_SERVER != null && ClientWorker.PROXY_SERVER.length() > 0) {
            ii = ii.replace("ProxyEnable=false", "ProxyEnable=true");
            ii = ii.replace("$ProxyServer$", ClientWorker.PROXY_SERVER);
            ii = ii.replace("$ProxyType$", ClientWorker.PROXY_TYPE);
            if (ClientWorker.PROXY_LOGIN != null && ClientWorker.PROXY_LOGIN.length() > 0) {
                ii = ii.replace("$ProxyLogin$", ClientWorker.PROXY_LOGIN);
                ii = ii.replace("$ProxyPassword$", ClientWorker.PROXY_PASSWORD);
            } else {
                ii = ii.replace("ProxyLogin=$ProxyLogin$", "ProxyLogin=");
                ii = ii.replace("ProxyPassword=$ProxyPassword$", "ProxyPassword=");
            }
        }
        String fName = dir + "/" + fileName;
        //
        ii = appendCustomParams(ii);
        //
//        byte[] bytes = ii.getBytes(Charset.forName("Unicode"));
//        byte[] bytes = TS.switchEndianess(ii.getBytes(Charset.forName("Unicode")));
        byte[] bytes = ii.getBytes();
        TS.writeFile(fName, bytes);
    }

    protected String appendCustomParams(String ii) {
        return ii;
    }

    public String MT4IfDll() {
        return (isMT5 ? MT5IF_DLL : MT4IF_DLL);
    }

    public String MT4IfXPDll() {
        return (isMT5 ? MT5IF_XP_DLL : MT4IF_XP_DLL);
    }

    public String guessSymbol() {
        if (this.symbol == null || this.symbol.length() == 0) {
//            return charts == null || charts.size() == 0 ? null : charts.get(0);
            if (charts != null && charts.size() > 0) {
                for (String sym : charts) {
                    if (!sym.startsWith(TS.SPCH)) {
                        return sym;
                    }
                }
            }
            return null;
        } else {
            return this.symbol;
        }
    }

    public String getSymbolFromSrvConfig() throws IOException {
        String fileName = TS.JFX_HOME_SRV_DIR + "/"
                + srv.replace(":", "_").replace(".", "_")
                + ".sym.txt";
        Path path = Paths.get(fileName);
        String s = null;
        if (Files.exists(path)) {
            List<String> symbols = Files.readAllLines(path, Charset.defaultCharset());
            s = symbols != null && symbols.size() > 0 ? symbols.get(0) : null;
        }
        if (s == null) {
            s = "EURUSD";
            //Files.write(path, (s + "\n").getBytes());
        }
        return s;
    }

    public boolean srvIsNotIPAddress() {
        int i = srv.indexOf('.');
        if (i > 0) {
            i = srv.indexOf('.', i + 1);
            if (i > 0) {
                i = srv.indexOf('.', i + 1);
                if (i > 0) {
                    i = srv.indexOf('.', i + 1);
                    if (i == -1) {
                        return false;
                    }
                }
            }
        } else if (!srv.equals(srv.toLowerCase())) {
            return true;
        }
        try {
            int ix = srv.lastIndexOf(':');
            if (ix > 0) {
                //noinspection ResultOfMethodCallIgnored
                InetAddress.getByName(srv.substring(0, ix));
            } else {
                //noinspection ResultOfMethodCallIgnored
                InetAddress.getByName(srv);
            }
            if (TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("InetAddress: " + srv);
            }
            return false;
        } catch (UnknownHostException e) {
            return true;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TerminalParams that = (TerminalParams) o;

        if (isMT5 != that.isMT5) return false;
        if (!srv.equals(that.srv)) return false;
        if (!user.equals(that.user)) return false;
        return getTerminalDirectoryLastName().equals(that.getTerminalDirectoryLastName());

    }

    @Override
    public int hashCode() {
        return getTerminalDirectoryLastName().hashCode();
    }

    public String getSrv() {
        return srv;
    }

    public String getUser() {
        return user;
    }
}
