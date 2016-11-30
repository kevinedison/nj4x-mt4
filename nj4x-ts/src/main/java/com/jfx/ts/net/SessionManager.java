package com.jfx.ts.net;

import com.jfx.ts.io.ExternalProcess;
import com.jfx.ts.io.LineListener;
import com.jfx.ts.io.Log4JUtil;
import com.jfx.ts.io.PSUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * todo: comments
 * User: roman
 * Date: 05/08/2014
 * Time: 22:03
 */
@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public class SessionManager {
    private final WeakHashMap<Integer, Long> pidStartTime = new WeakHashMap<>();
    /**
     * The Num terminals per session.
     */
    public int numTerminalsPerSession;
    /**
     * T通过id来识别session的map
     */
    Map<Integer, Session> sessionById = new ConcurrentHashMap<Integer, Session>(); //
    /**
     * 通过tsuser来识别session的map
     */
    Map<TsSystemUser, Session> sessionByTsUser = new ConcurrentHashMap<>();//
    /**
     * The Max windows sessions.
     */
    int maxWindowsSessions;
    /**
     * The Ts users.
     */
    HashMap<Integer, TsSystemUser> tsUsers = new HashMap<Integer, TsSystemUser>();
    /**
     * The Max user id.
     */
    int maxUserId = Integer.MIN_VALUE;
    private TS ts;
    private Session localSession;
    private Runnable newSessionCreator;
    private int lastTermsFound = -1;

    /**
     * Instantiates a new Session manager.
     *
     * @param ts the ts
     */
    public SessionManager(TS ts) {

        this.ts = ts;
        localSession = getSessionByIdCreateIfNeeded(PSUtils.localSessionId());
        TS.LOGGER.info("Local session: " + localSession);
    }

    /**
     * Start rdp client.
     *
     * @param tsSystemUser the ts system user
     *
     * @exception IOException the io exception
     */
    public static void StartRDPClient(TsSystemUser tsSystemUser) throws IOException {
        TS.assertProgramExitCode(0, TS.CMDKEY, "/generic:TERMSRV/127.0.0.1", "/user:" + tsSystemUser.name, "/pass:" + tsSystemUser.password);
        PSUtils.startProcess2(TS.MSTSC + " /v 127.0.0.1:" + TS.P_MSTSC_PORT, ".", false, false);
    }

    /**
     * Gets sessions.
     *
     * @return the sessions
     */
    public Collection<Session> getSessions() {
        return sessionById.values();
    }

    //
    private TsSystemUser getTsUserCreateTemplateIfNeeded(String userName) {
        TsSystemUser key = new TsSystemUser(ts, userName);
        TsSystemUser tsSystemUser = tsUsers.get(key.id);
        return tsSystemUser == null ? key : tsSystemUser;
    }

    /**
     * Gets ts user create template if needed.
     *
     * @param id the id
     *
     * @return the ts user create template if needed
     */
    TsSystemUser getTsUserCreateTemplateIfNeeded(int id) {
        TsSystemUser tsSystemUser = tsUsers.get(id);
        if (tsSystemUser == null) {
            tsSystemUser = new TsSystemUser(ts, id);
        }
        return tsSystemUser;
    }

    private void addTsUser(TsSystemUser u) {
        tsUsers.put(u.id, u);
        updateTsUser(u);
    }

    /**
     * Load sessions session manager.
     *
     * @return the session manager
     *
     * @exception IOException the io exception
     */
//加载session
    public SessionManager loadSessions() throws IOException {
        getMaxDesktopsForSharedSection();
        loadExistingSessions();
        return initTerminalsUpdatingJob();
    }

    /**
     * Init terminals updating job session manager.
     *
     * @return the session manager
     */
//初始化终端管理的任务
    public SessionManager initTerminalsUpdatingJob() {
        //初次运行的时候要初始化终端一次
        updateTerminals();
        TS.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (Log4JUtil.isConfigured() && TS.LOGGER.isTraceEnabled()) {
                    TS.LOGGER.trace("Timer: update session's load level");
                }
                if (TS.P_USE_MSTSC) {
                    try {
                        loadExistingSessions();
                    } catch (Throwable e) {
                        TS.LOGGER.error("Error loading current sessions", e);
                    }
                }
                updateTerminals();
            }
        }, 30, 60, TimeUnit.SECONDS);
        return this;
    }

    /**
     * Update terminals.
     */
//更新终端
    synchronized void updateTerminals() {
        final String dir = TS.getTermDir() + "\\";
        int totalTermsCount = 0;
        HashMap<Integer, Session> scannedSessions = new HashMap<>(); //存储session的map
        String[] processes = PSUtils.checkProcess2(dir, false, false);  //这个方法是import的，应该是c++的程序
//        这个进程应该是c++的进程
        for (String process : processes) { //processes是计算机所有的进程
            if (process.startsWith(dir)) {
                totalTermsCount++;
                PidSession pidSession = new PidSession(process);
                int sessionId = pidSession.getSessionId();
                int pid = pidSession.getPid();
                Session s = scannedSessions.get(sessionId);
                if (s == null) {
                    scannedSessions.put(sessionId, s = new Session(ts, sessionId));//如果不存在就存储
                    s.user = sessionId == 0 //|| localSession != null && sessionId == localSession.id
                            ? getTsUserCreateTemplateIfNeeded(0) : new TsSystemUser(ts);
                }
                //貌似是给session一个新的线程
                s.addTermProcessUnsafe(process.substring(0, process.indexOf('\u0001')), pid);
//            } else if (!ts.mstsc && scannedSessions.size() == 0) {
//                PidSession pidSession = new PidSession(process).invoke();
//                Session s;
//                scannedSessions.put(pidSession.getSessionId(), s = new Session(ts, pidSession.getSessionId()));
//                s.user = new TsSystemUser(ts);
            }
        }
        if (lastTermsFound != totalTermsCount && Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
            lastTermsFound = totalTermsCount;
            TS.LOGGER.info("Found " + totalTermsCount + " terminal" + (totalTermsCount > 1 ? "s" : "") + " running");
        }
        //
        // update real sessions，把这些session存在byid里面
        //
        for (Session _s : scannedSessions.values()) {
            Session s = sessionById.get(_s.id);
            if (s == null) {
                if (ts.mstsc) {
                    throw new RuntimeException("Session not found: " + _s.id);
                } else {
                    sessionById.put(_s.id, /*localSession = */_s);
                }
            } else {
                s.setTermProcesses(_s);//s.termProcesses = _s.termProcesses;
            }
        }
        //同步session的map里面的session
        for (Session _s : sessionById.values()) {
            Session s = scannedSessions.get(_s.id);
            if (s == null) {
                _s.clearAllTermProcesses();
/*
                Session s0 = sessionById.get(0);
                if (_s == localSession && s0 != null && _s != s0) {
                    TS.LOGGER.warn("fix localSession: " + localSession + " -> " + s0);
                    localSession = s0;
                }
*/
            }
        }
        //
        ts.gui.updateTerminals(this);
    }
    //加载存在的session
    private void loadExistingSessions() throws IOException {
        loadExistingSessions(null);
    }
    //加载存在的session
    private void loadExistingSessions(final TsSystemUser user) throws IOException {
        final Set<Integer> _sessions = new HashSet<>();

        PSUtils.WtsSessionInfo[] sessionInfos = PSUtils.getSessions();
        if (sessionInfos == null) {
            if (TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("loadExistingSessions by QUSER...");
            }
            /*
            USERNAME              SESSIONNAME        ID  STATE   IDLE TIME  LOGON TIME
            >administrator         console             1  Active      none   6/3/2013 9:21 AM
            h4                                        2  Disc            .  6/3/2013 1:29 PM
             */
            TS.assertProgramExitCode(0, new LineListener() {
                int userNameStartIx;
                int idEndIx;

                @Override
                public void onLine(int lineNb, StringBuffer line) {
                    if (lineNb == -1) {
                        //eof
                        return;
                    }
                    String _line = line.toString().toUpperCase();
                    if (lineNb == 1) {
                        userNameStartIx = _line.indexOf(" USERNAME ") + 1;
                        idEndIx = _line.indexOf(" ID ") + 3;
                    } else {
                        UserNameSessionId userNameSessionId = new UserNameSessionId(_line).invoke();
                        _sessions.add(userNameSessionId.getSessId());
                        if (_line.startsWith(">") && user == null) {
                            // current session
                            if (!sessionById.containsKey(userNameSessionId.getSessId())) {
                                TsSystemUser currentUser = getTsUserCreateTemplateIfNeeded(-1);
                                currentUser.name = userNameSessionId.getUserName();
                                localSession = getSessionByIdCreateIfNeeded(userNameSessionId.getSessId());//todo
                                currentUser.setSession(localSession);
                                addTsUser(currentUser);
                            }
                        } else if (_line.startsWith(" " + TsSystemUser.NJ4X_HOST)) {
//                        UserNameSessionId userNameSessionId = new UserNameSessionId(_line).invoke();
//                        _sessions.add(userNameSessionId.getSessId());
                            if (!sessionById.containsKey(userNameSessionId.getSessId())
                                    && (user == null || userNameSessionId.getUserName().equals(user.name))) {
                                TsSystemUser tsSystemUser = user == null ? getTsUserCreateTemplateIfNeeded(userNameSessionId.getUserName()) : user;
                                tsSystemUser.setSession(getSessionByIdCreateIfNeeded(userNameSessionId.getSessId()));
                                addTsUser(tsSystemUser);
                            }
                        }
                    }
                }

                class UserNameSessionId {
                    private String line;
                    private String userName;
                    private int sessId;

                    public UserNameSessionId(String _line) {
                        line = _line;
                    }

                    public String getUserName() {
                        return userName;
                    }

                    public int getSessId() {
                        return sessId;
                    }

                    public UserNameSessionId invoke() {
                        userName = line.substring(userNameStartIx, line.indexOf(' ', userNameStartIx));
                        int ix = idEndIx - 1;
                        for (; ix > 0; --ix) {
                            if (line.charAt(ix) == ' ') {
                                break;
                            }
                        }
                        sessId = Integer.parseInt(line.substring(ix + 1, idEndIx));
                        return this;
                    }
                }
            }, TS.QUSER);
        } else {
            if (TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("before loadExistingSessions...");
                for (Session s : sessionById.values()) {
                    TS.LOGGER.debug("known: " + s.toString());
                }
                TS.LOGGER.debug("loadExistingSessions... (user=" + (user == null ? "all" : user.getName()) + ") (Local " + localSession + ")");
            }
            for (PSUtils.WtsSessionInfo sessionInfo : sessionInfos) {
                if (localSession != null && localSession.id == sessionInfo.id) {
                    _sessions.add(sessionInfo.id);
                } else {
                    if (sessionInfo.id == 0 || !sessionInfo.user.equals("(NULL)")) {
                        if (TS.LOGGER.isDebugEnabled()) {
                            TS.LOGGER.debug("" + sessionInfo);
                        }
                        if (sessionInfo.state == 0/*Active*/
                                && (user == null || localSession != null && localSession.id == sessionInfo.id)
                                && (TS.P_GUI_ONLY || !TS.P_RAN_AS_SERVICE)) {
                            _sessions.add(sessionInfo.id);
                            // current session
                            if (!sessionById.containsKey(sessionInfo.id)) {
                                TsSystemUser currentUser = getTsUserCreateTemplateIfNeeded(-1);
                                currentUser.name = sessionInfo.user;
                                localSession = getSessionByIdCreateIfNeeded(sessionInfo.id);
                                currentUser.setSession(localSession);
                                addTsUser(currentUser);
                            }
                        } else if (sessionInfo.id == 0/*Services*/
                                && (user == null || localSession != null && localSession.id == sessionInfo.id)
                                && (TS.P_GUI_ONLY || TS.P_RAN_AS_SERVICE)) {
                            _sessions.add(sessionInfo.id);
                            // services session
                            if (!sessionById.containsKey(sessionInfo.id)) {
                                TsSystemUser tsSystemUser = getTsUserCreateTemplateIfNeeded(0);
                                tsSystemUser.setSession(localSession = getSessionByIdCreateIfNeeded(sessionInfo.id));
                                addTsUser(tsSystemUser);
                            }
                        } else {
                            if (sessionInfo.user.toUpperCase().startsWith(TsSystemUser.NJ4X_HOST)) {
                                _sessions.add(sessionInfo.id);
                                if (!sessionById.containsKey(sessionInfo.id)
                                        && (user == null || sessionInfo.user.equals(user.name))) {
                                    TsSystemUser tsSystemUser = user == null ? getTsUserCreateTemplateIfNeeded(sessionInfo.user) : user;
                                    tsSystemUser.setSession(getSessionByIdCreateIfNeeded(sessionInfo.id));
                                    addTsUser(tsSystemUser);
                                }
                            } else {
                                TS.LOGGER.warn("Skip(b): " + sessionInfo);
                            }
                        }
                    } else {
                        TS.LOGGER.warn("Skip(a): " + sessionInfo);
                    }
                }
            }
        }
        //
        if (!TS.USE_NJ4X_USER && sessionById.size() != _sessions.size()) {
            ArrayList<Session> sessionsToRemove = new ArrayList<>();
            for (Session s : sessionById.values()) {
                if (!_sessions.contains(s.id))
                    sessionsToRemove.add(s);
            }
            for (Session s : sessionsToRemove) {
                TS.LOGGER.warn("Removing: " + s);
                sessionById.remove(s.id);
                if (s.user != null) {
                    tsUsers.remove(s.user.id);
                }
            }
//            maxWindowsSessions = sessionById.size();
            TS.LOGGER.warn("Loaded sessions: " + _sessions);
            TS.LOGGER.warn("Users: " + tsUsers.values());
//            TS.LOGGER.warn("Hold maxWindowsSessions at " + maxWindowsSessions + ", Sessions: " + sessionById.values());
            TS.LOGGER.warn("Sessions: " + sessionById.values());
        }
    }

    private Session getSessionByIdCreateIfNeeded(int sessionId) {
        Session session = sessionById.get(sessionId);
        if (session == null) {
            sessionById.put(sessionId, session = new Session(ts, sessionId));
        }
        return session;
    }

    /**
     * Gets session by id.
     *
     * @param sessionId the session id
     *
     * @return the session by id
     */
    public Session getSessionById(int sessionId) {
        return sessionById.get(sessionId);
    }

    /**
     * Gets session by pid.
     *
     * @param pid the pid
     *
     * @return the session by pid
     */
    public Session getSessionByPID(int pid) {
        for (Session s : sessionById.values()) {
            if (s.isPIDOwner(pid)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Gets pid.
     *
     * @param termName the term name
     *
     * @return the pid
     */
    public int getPID(String termName) {
        for (Session s : sessionById.values()) {
            int pid = s.getPID(termName);
            if (pid > 0) {
                synchronized (pidStartTime) {
                    pidStartTime.put(pid, s.getPIDStartTime(pid));
                }
                //
                return pid;
            }
        }
        return -1;
    }

    /**
     * Remove term process or pid.
     *
     * @param processName the process name
     * @param pid         the pid
     */
    void removeTermProcessOrPID(String processName, int pid) {
        for (Session s : sessionById.values()) {
            s.removeTermProcessOrPID(processName, pid);
        }
    }

    /**
     * Gets pid start time.
     *
     * @param pid the pid
     *
     * @return the pid start time
     */
    public Long getPIDStartTime(int pid) { // getPID must be called 1st
        synchronized (pidStartTime) {
            return pidStartTime.get(pid);
        }
    }

    /**
     * Create ts user boolean.
     *
     * @param id TS user id (1,2,..)
     *
     * @return true if user has been created, false - if user existed
     *
     * @exception IOException         the io exception
     * @exception java.io.IOException
     */
    public synchronized boolean createTsUser(int id) throws IOException {
        boolean isCreated = false;
        TsSystemUser u = getTsUserCreateTemplateIfNeeded(id);
        if (!u.exists()) {
            String osv = System.getProperty("os.version");
            if (osv.equals("5.1") || osv.equals("5.2")) {
                TS.assertProgramExitCode(0, TS.NET, "user", u.name, u.password, "/add", "/PASSWORDCHG:NO", "/PASSWORDREQ:YES");
            } else {
                TS.assertProgramExitCode(0, TS.NET, "user", u.name, u.password, "/add", "/PASSWORDCHG:NO", "/PASSWORDREQ:YES", "/LOGONPASSWORDCHG:NO");
            }
            TS.assertProgramExitCode(0, TS.NET, "localgroup", TS.REMOTE_DESKTOP_USERS, u.name, "/add");
            if (u.name.equals("NJ4X")) {
                TS.assertProgramExitCode(0, TS.NET, "localgroup", TS.ADMINISTRATORS, u.name, "/add");
            }
            TS.runProgramGetExitCode(TS.NET, "localgroup", TS.USERS, u.name, "/delete");
//            if (osv.equals("5.1") || osv.equals("5.2") || System.getProperty("wmic_via_psexec", "false").equals("true")) {
//                TS.assertProgramExitCode(0, TS.PSEXEC, TS.WMIC, "USERACCOUNT", "WHERE", "Name='" + u.name + "'", "SET", "PasswordExpires=FALSE");
//            } else {
            TS.assertProgramExitCode(0, TS.WMIC, "USERACCOUNT", "WHERE", "Name='" + u.name + "'", "SET", "PasswordExpires=FALSE");
//            }
            isCreated = true;
        }
        //
        addTsUser(u.getReady());
        if (maxUserId < id) {
            maxUserId = id;
        }
        return isCreated;
    }

    /**
     * Start session.
     *
     * @param tsSystemUser the ts system user
     *
     * @exception IOException the io exception
     */
    public synchronized void startSession(TsSystemUser tsSystemUser) throws IOException {
        StartRDPClient(tsSystemUser);
        for (int i = 0; i < 15; ++i) {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ignore) {
            }
            loadExistingSessions(tsSystemUser);
            if (tsSystemUser.session != null) {
                break;
            }
        }
        //
        if (tsSystemUser.session == null) {
//            throw new IOException("Can not start RDP session for " + tsSystemUser.name);
            TS.LOGGER.error("Can not start RDP session for " + tsSystemUser.name);
            if (TS.P_RAN_AS_SERVICE) {
                TS.LOGGER.warn("Exit TS service, should be restarted\n\n");
                System.exit(-1);
            }
        } else {
            TS.assertProgramExitCode(0,
                    TS.PSEXEC, "-u", tsSystemUser.name, "-p", tsSystemUser.password,
                    "-i", String.valueOf(tsSystemUser.session.id),
                    "-w", System.getProperty("user.dir"),
                    System.getProperty("java.home") + File.separator + "bin" + File.separator + "javaw.exe",
                    "-cp", System.getProperty("java.class.path"),
                    "com.jfx.ts.io.PSUtils",
                    System.getProperty("disable_mt4_live_update", "true").equals("true") ? "-live_update" : "+live_update"
            );
            try {
                TS.assertProgramExitCode(0, TS.PSEXEC, "-u", tsSystemUser.name, "-p", tsSystemUser.password, "-i", String.valueOf(tsSystemUser.session.id), "tsdiscon.exe");
                TS.assertProgramExitCode(0, "tsdiscon.exe", String.valueOf(tsSystemUser.session.id), "/V");
            } catch (Throwable e) {
                TS.assertProgramExitCode(0, "tsdiscon.exe", String.valueOf(tsSystemUser.session.id), "/V");
            }
        }
    }

    /**
     * Update ts user.
     *
     * @param u the u
     */
    public void updateTsUser(TsSystemUser u) {
        if (u.session != null) {
            Session session = sessionById.get(u.session.id);
            assert session == u.session;
            session.user = u;
            sessionByTsUser.put(u, session);
        }
    }

    private int getSessionViewSize() throws IOException {
        ExternalProcess p = new ExternalProcess(
                TS.REG, "query", "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Memory Management\\",
                "/v", "SessionViewSize");
        final int[] vsz = new int[1];
        p.setOutListener(new LineListener() {
            private String sessionViewSize;

            public void onLine(int lineNb, StringBuffer line) {
                if (lineNb > 0) {
                    if (line.indexOf("SessionViewSize") > 0) {
                        sessionViewSize = line.toString().trim();
                        sessionViewSize = sessionViewSize.replace('\t', ' ');
                    }
                } else {
                    if (sessionViewSize == null) {
                        int mb = 32;
                        TS.LOGGER.info("Guessed (no registry key found) SessionViewSize=" + mb + " MB");
                        synchronized (vsz) {
                            vsz[0] = mb;
                            vsz.notifyAll();
                        }
                    } else {
                        String[] split = sessionViewSize.split(" ");
                        String v = split[split.length - 1];
                        int mb;
                        if (v.startsWith("0x")) {
                            mb = Integer.parseInt(v.substring(2), 16);
                        } else if (v.indexOf("0x") > 0) {
                            mb = Integer.parseInt(v.substring(v.indexOf("0x") + 2).trim(), 16);
                        } else {
                            mb = Integer.parseInt(v);
                        }
                        TS.LOGGER.info("SessionViewSize=" + mb + " MB");
                        synchronized (vsz) {
                            vsz[0] = mb;
                            vsz.notifyAll();
                        }
                    }
                }
            }
        });
        p.start();
        try {
            synchronized (vsz) {
                vsz.wait(1000);
            }
            p.getProcess().waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        return vsz[0];
    }
    //这个不知道是干嘛的，要单步调试一下

    /**
     * csrss.exe通常是系统的正常进程，所在的进程文件是csrss或csrss.exe，是微软客户端、服务端运行时子系统，windows的核心进程之一。管理Windows图形相关任务，对系统的正常运行非常重要。csrss是Client/Server
     * Runtime Server Subsystem的简称，即客户/服务器运行子系统，用以控制Windows图形相关子系统，必须一直运行。csrss用于维持Windows的控制，创建或者删除线程和一些16位的虚拟MS-DOS环境。也有可能是W32.Netsky.AB@mm等病毒创建的。
     *
     * @return max desktops for shared section
     *
     * @exception IOException the io exception
     * @exception IOException
     */
    int getMaxDesktopsForSharedSection() throws IOException {
        final int sessionViewSize = getSessionViewSize() * 1024;
        //windows对应的就是csrss.exe
        ExternalProcess p = new ExternalProcess(TS.REG, "query",
                "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\SubSystems", "/v", "Windows");
        final int[] vsz = new int[1];
        p.setOutListener(new LineListener() {
            private String sharedSection;

            public void onLine(int lineNb, StringBuffer line) {
                int calculatedNumTermsPerSession;
                if (lineNb > 0) {
                    if (line.indexOf("SharedSection") > 0) {
                        sharedSection = line.toString().trim();
                    }
                } else {
                    if (sharedSection == null) {
                        calculatedNumTermsPerSession = numTerminalsPerSession = TS.MAX_TERMS;
                        maxWindowsSessions = 1;
                        synchronized (vsz) {
                            vsz[0] = 1;
                            vsz.notifyAll();
                        }
                    } else {
                        String[] split = sharedSection.split("SharedSection=")[1].split(" ")[0].split(",");
                        int interactiveDesktopHeap = Integer.parseInt(split[1]);
                        int nonInteractiveDesktopHeap = split.length == 3 ? Integer.parseInt(split[2]) : interactiveDesktopHeap;
                        TS.LOGGER.info("SharedSection=" + split[0] + ',' + interactiveDesktopHeap + ',' + nonInteractiveDesktopHeap);
                        //
                        // 64mb - 120 t
                        // Nmb  - x
                        // x = (Nmb * 120) / 40mb
                        //
                        calculatedNumTermsPerSession = interactiveDesktopHeap * 120 / 64 / 1024;
                        int maxTerminalsPerDesktop;
                        try {
                            maxTerminalsPerDesktop = Integer.parseInt(System.getProperty("max_terminals_per_desktop", "" + Math.min(TS.MAX_TERMS, calculatedNumTermsPerSession)));
                        } catch (NumberFormatException e) {
                            TS.LOGGER.error("Invalid system parameter 'max_terminals_per_desktop' (not an integer): " + System.getProperty("max_terminals_per_desktop"));
                            maxTerminalsPerDesktop = calculatedNumTermsPerSession;
                        }
                        numTerminalsPerSession = Math.min(calculatedNumTermsPerSession, maxTerminalsPerDesktop);
                        int stdDesktopSize = 192 + 96 + interactiveDesktopHeap;
                        int noninteractiveDesktopSize = stdDesktopSize + 5 * nonInteractiveDesktopHeap;
                        synchronized (vsz) {
                            vsz[0] = (sessionViewSize - noninteractiveDesktopSize) / stdDesktopSize;
                            vsz.notifyAll();
                        }
                        maxWindowsSessions = vsz[0];
                    }
                    int maxSessions = maxWindowsSessions + 1;
                    int maxTerms = (TS.P_USE_MSTSC ? vsz[0] * 2 : 1) * numTerminalsPerSession;
                    //
                    ts.gui.maxSessionsField.setText(String.valueOf(maxSessions));
                    ts.gui.maxTerminalsField.setText(String.valueOf(maxTerms));
                    //
                    TS.LOGGER.info("Max Windows(TM) sessions: " + maxSessions
                                    + ", max terms per session: " + numTerminalsPerSession
//                                + (numTerminalsPerSession == calculatedNumTermsPerSession ? "" : " of potential " + calculatedNumTermsPerSession)
                    );
                    TS.LOGGER.info("Max platform terminals: " + maxTerms);
                }
            }
        });
        p.start();
        try {
            synchronized (vsz) {
                vsz.wait(1000);
            }
            p.getProcess().waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        return vsz[0];
    }

    /**
     * Load level pct int.
     *
     * @return the int
     */
    public int loadLevelPct() {
        double ll = 0;
        int cnt = 0;
        for (Session s : sessionById.values()) {
            double _ll = s.getLoad();
            if (TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Sessoin #" + s.id
                        + ", user=" + (s.user == null ? System.getProperty("user.name") : s.user.name)
                        + ": load_rate=" + _ll
                );
            }
            ll += _ll;
            cnt++;
        }
        int llPct = (int) (ll / cnt * 100.0);
        if (TS.LOGGER.isDebugEnabled()) {
            TS.LOGGER.debug("TS load_level_pct=" + llPct);
        }
        return llPct;
    }

    /**
     * Gets min load session.
     *
     * @return the min load session
     */
    public Session getMinLoadSession() {
        double ll = 0;
        int cnt = 0;
        double minLoad = Double.MAX_VALUE;
        Session minLoadSession = null;
        for (Session s : sessionById.values()) {
            double _ll = s.getLoad();
            ll += _ll;
            cnt++;
            if (_ll < minLoad) {
                minLoad = _ll;
                minLoadSession = s;
            }
        }
        int llPct = (int) (ll / cnt * 100.0);
        if (llPct > 79 && maxWindowsSessions > sessionById.size() && newSessionCreator == null) {
            synchronized (this) {
                if (newSessionCreator == null) {
                    newSessionCreator = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                synchronized (this) {
                                    int loadPct = loadLevelPct();
                                    if (loadPct > 79 && maxWindowsSessions > sessionById.size()) {
                                        try {
                                            TS.LOGGER.info("Load level=" + loadPct + "%, creating NJ4X host user #" + (maxUserId + 1) + '/' + maxWindowsSessions + " session");
                                            createTsUser(maxUserId + 1);
                                        } catch (IOException e) {
                                            TS.LOGGER.error("Add NJ4X host user #" + (maxUserId + 1) + " error", e);
                                        }
                                    }
                                }
                            } finally {
                                newSessionCreator = null;
                            }
                        }
                    };
                    TS.scheduledExecutorService.submit(newSessionCreator);
                }
            }
        }
        return minLoadSession;
    }

    /**
     * Gets local session.
     *
     * @return the local session
     */
    public Session getLocalSession() {
        return localSession;
    }

    /**
     * Init ts users.
     *
     * @exception IOException the io exception
     */
//  初始化Ts的users
    public void initTsUsers() throws IOException {
        int id = 1;
        //noinspection StatementWithEmptyBody
        while (!createTsUser(id)
                && loadLevelPct() > 80
                && id < maxWindowsSessions) {
            id++;
        }
        //
        try {
            while (tsUsers.size() < Math.min(15, maxWindowsSessions / 2)) {
                int uId = 1 + tsUsers.size();
                createTsUser(uId);
                if (uId == 1 + tsUsers.size()) {
                    TS.LOGGER.info("InitTSUsers completed, maxWindowsSessions=" + maxWindowsSessions);
                    TS.LOGGER.info("Sessions: " + sessionById.values());
                    TS.LOGGER.info("Users: " + tsUsers.values());
                    break;
                }
            }
        } catch (IOException e) {
            TS.LOGGER.error("Error preparing remote desktops: " + e.getMessage(), e);
            if (tsUsers.size() < 2) {
                throw e;
            }
            TS.LOGGER.warn("Sessions: " + sessionById.values());
            TS.LOGGER.warn("Users: " + tsUsers.values());
        }
    }

}
