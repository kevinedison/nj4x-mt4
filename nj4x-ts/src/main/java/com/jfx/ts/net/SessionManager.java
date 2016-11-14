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
    public int numTerminalsPerSession;
    Map<Integer, Session> sessionById = new ConcurrentHashMap<Integer, Session>();
    Map<TsSystemUser, Session> sessionByTsUser = new ConcurrentHashMap<>();
    int maxWindowsSessions;
    HashMap<Integer, TsSystemUser> tsUsers = new HashMap<Integer, TsSystemUser>();
    int maxUserId = Integer.MIN_VALUE;
    private TS ts;
    private Session localSession;
    private Runnable newSessionCreator;
    private int lastTermsFound = -1;

    public SessionManager(TS ts) {
        this.ts = ts;
        localSession = getSessionByIdCreateIfNeeded(PSUtils.localSessionId());
        TS.LOGGER.info("Local session: " + localSession);
    }

    public static void StartRDPClient(TsSystemUser tsSystemUser) throws IOException {
        TS.assertProgramExitCode(0, TS.CMDKEY, "/generic:TERMSRV/127.0.0.1", "/user:" + tsSystemUser.name, "/pass:" + tsSystemUser.password);
        PSUtils.startProcess2(TS.MSTSC + " /v 127.0.0.1:" + TS.P_MSTSC_PORT, ".", false, false);
    }

    public Collection<Session> getSessions() {
        return sessionById.values();
    }

    //
    private TsSystemUser getTsUserCreateTemplateIfNeeded(String userName) {
        TsSystemUser key = new TsSystemUser(ts, userName);
        TsSystemUser tsSystemUser = tsUsers.get(key.id);
        return tsSystemUser == null ? key : tsSystemUser;
    }

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

    public SessionManager loadSessions() throws IOException {
        getMaxDesktopsForSharedSection();
        loadExistingSessions();
        return initTerminalsUpdatingJob();
    }

    public SessionManager initTerminalsUpdatingJob() {
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
                //
                updateTerminals();
            }
        }, 30, 60, TimeUnit.SECONDS);
        return this;
    }

    synchronized void updateTerminals() {
        final String dir = TS.getTermDir() + "\\";
        int totalTermsCount = 0;
        HashMap<Integer, Session> scannedSessions = new HashMap<>();
        String[] processes = PSUtils.checkProcess2(dir, false, false);
        for (String process : processes) {
            if (process.startsWith(dir)) {
                totalTermsCount++;
                PidSession pidSession = new PidSession(process);
                int sessionId = pidSession.getSessionId();
                int pid = pidSession.getPid();
                Session s = scannedSessions.get(sessionId);
                if (s == null) {
                    scannedSessions.put(sessionId, s = new Session(ts, sessionId));
                    s.user = sessionId == 0 //|| localSession != null && sessionId == localSession.id
                            ? getTsUserCreateTemplateIfNeeded(0) : new TsSystemUser(ts);
                }
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
        // update real sessions
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

    private void loadExistingSessions() throws IOException {
        loadExistingSessions(null);
    }

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
                TS.LOGGER.debug("loadExistingSessions... (user=" + (user == null ? "all" : user.getName()) + ") (Local "+localSession+")");
            }
            for (PSUtils.WtsSessionInfo sessionInfo : sessionInfos) {
                if (localSession!=null && localSession.id == sessionInfo.id){
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

    public Session getSessionById(int sessionId) {
        return sessionById.get(sessionId);
    }

    public Session getSessionByPID(int pid) {
        for (Session s : sessionById.values()) {
            if (s.isPIDOwner(pid)) {
                return s;
            }
        }
        return null;
    }

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

    void removeTermProcessOrPID(String processName, int pid) {
        for (Session s : sessionById.values()) {
            s.removeTermProcessOrPID(processName, pid);
        }
    }

    public Long getPIDStartTime(int pid) { // getPID must be called 1st
        synchronized (pidStartTime) {
            return pidStartTime.get(pid);
        }
    }

    /**
     * @param id TS user id (1,2,..)
     * @return true if user has been created, false - if user existed
     * @throws java.io.IOException
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

    int getMaxDesktopsForSharedSection() throws IOException {
        final int sessionViewSize = getSessionViewSize() * 1024;
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

    public Session getLocalSession() {
        return localSession;
    }

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
