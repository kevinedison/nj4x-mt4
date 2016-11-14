package com.jfx.ts.net;

import com.jfx.ts.io.Log4JUtil;
import com.jfx.ts.io.PSUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * todo: comments
 * User: roman
 * Date: 05/08/2014m
 * Time: 22:03
 */
public class Session {
    int id;
    TsSystemUser user;
    volatile ConcurrentHashMap<String, Integer> termProcesses = new ConcurrentHashMap<>();
    private TS ts;
    private ConcurrentHashMap<Integer, Long> pidStartTime = new ConcurrentHashMap<>();
    private AtomicInteger startingProcessesCount = new AtomicInteger(0);

    Session(TS ts, int id) {
        this.ts = ts;
        this.id = id;
    }

    @Override
    public String toString() {
        return "Session #" + id + (user == null ? "" : " (" + user.getName() + ")");
    }

    void addTermProcess(String processName, int _pid) {
        ts.sessionManager.removeTermProcessOrPID(processName, _pid);
        termProcesses.put(processName, _pid);
        pidStartTime.put(_pid, System.currentTimeMillis());
        if (Log4JUtil.isConfigured() && TS.LOGGER.isDebugEnabled()) {
            TS.LOGGER.debug("Session #" + id + " added process: " + processName + " [_pid=" + _pid + ']');
        }
    }

    void addTermProcessUnsafe(String processName, int _pid) {
        termProcesses.put(processName, _pid);
        pidStartTime.put(_pid, System.currentTimeMillis());
    }

    void removeTermProcessOrPID(String processName, int pid) {
        Integer _pid = termProcesses.remove(processName);
        if (_pid != null) {
            pidStartTime.remove(_pid);
            if (Log4JUtil.isConfigured() && TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Session #" + id + " removed process: " + processName + " [_pid=" + _pid + ']' + " [asked pid=" + pid + ']');
            }
        }
        if (pidStartTime.remove(pid) != null) {
            if (Log4JUtil.isConfigured() && TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Session #" + id + " foreign process: " + processName + " [_pid=" + _pid + ']' + " [asked pid=" + pid + ']');
            }
        }
    }

    public int getTermsCount() {
        return termProcesses.size();
    }

    public Map<String, Integer> getTermProcesses() {
        return termProcesses;
    }

    public void setTermProcesses(Session _s) {
        ConcurrentHashMap<String, Integer> currentTermProcesses = this.termProcesses;
        HashSet<Integer> pidToRemove = new HashSet<>();
        Enumeration<Integer> PIDs = pidStartTime.keys();
        while (PIDs.hasMoreElements()) {
            pidToRemove.add(PIDs.nextElement());
        }
        for (Map.Entry<String, Integer> e : _s.termProcesses.entrySet()) {
            Integer oldPID = currentTermProcesses.get(e.getKey());
            if (oldPID == null) {
                if (Log4JUtil.isConfigured() && TS.LOGGER.isDebugEnabled()) {
                    TS.LOGGER.debug("Session #" + id + "#   added process: " + e.getKey() + " [_pid=" + e.getValue() + ']');
                }
                pidStartTime.put(e.getValue(), System.currentTimeMillis());
            } else {
                pidToRemove.remove(oldPID);// process is ours, do not remove its PID
                if (!oldPID.equals(e.getValue())) {
                    if (Log4JUtil.isConfigured() && TS.LOGGER.isDebugEnabled()) {
                        TS.LOGGER.debug("Session #" + id + "# foreign process: " + e.getKey() + " [_pid=" + oldPID + ']' + " [asked pid=" + e.getValue() + ']');
                    }
                    pidToRemove.add(oldPID);
                    pidToRemove.remove(e.getValue());
                    pidStartTime.put(e.getValue(), System.currentTimeMillis());
                }
            }
        }
        for (Integer pid : pidToRemove) {
            pidStartTime.remove(pid);
        }
        this.termProcesses = _s.termProcesses;
    }

    public int getId() {
        return id;
    }

    public TsSystemUser getUser() {
        return user;
    }

    public double getLoad() {
        return ((double) (getTermsCount() + startingProcessesCount.get())) / ts.sessionManager.numTerminalsPerSession;
    }

    int runProcessGetPID(boolean show, boolean cpu, String dir, String... cmd) throws IOException {
        if (!TS.USE_NJ4X_USER) return runProcessGetPID_psutils(show, cpu, dir, cmd);
        startingProcessesCount.incrementAndGet();
        try {
//            try {
//                TS.assertProgramExitCode(0, TS.ICACLS, Paths.get(cmd[0]).getParent().toString(), "/grant", "\"" + user.name + "\":F", "/T");
//            } catch (Throwable t) {
//                t.printStackTrace();
//            }
            ArrayList<String> cmdLine = new ArrayList<String>();
            preparePSExecCall(cmdLine);
            cmdLine.add(dir);
            Collections.addAll(cmdLine, cmd);
            if (TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Session #" + id + " starting process: " + cmdLine);
            }
/*
            for (int i = 0; i < cmdLine.size(); i++) {
                String s = cmdLine.get(i);
                boolean isSpace = s.indexOf(' ') >= 0 && s.charAt(0) != '"';
                if (isSpace) {
                    cmdLine.set(i, '"' + s + '"');
                }
            }
*/
            //
            for (int r = 0; r < 3; r++) {
                checkPsExeSvc();
                //
                int pid;
                pid = TS.runProgramGetExitCode(120000, cmdLine.toArray(new String[cmdLine.size()]));
                if (pid > 6) {
                    if (pid >= 1000000000) { //1073741000
                        // error
                        long error = ((long) -pid);
                        TS.LOGGER.error(String.format("Session #" + id + ", process is unable to start correctly: 0x%X", error));
                        dumpCommandLine(cmd);
                    } else {
                        if (TS.LOGGER.isDebugEnabled()) {
                            TS.LOGGER.debug("Session #" + id + " started process ID=[" + pid + "] " + cmdLine);
                        }
                        addTermProcess(cmd[0], pid);
                        return pid;
                    }
                } else {
                    TS.LOGGER.error("Session #" + id + " process start error, code=" + pid + ", " + getCmdLine(cmd));
                    dumpCommandLine(cmd);
                }
            }
            return 0;
        } finally {
            startingProcessesCount.decrementAndGet();
        }
    }

    int runProcessGetPID_psutils(boolean show, boolean cpu, String dir, String... cmd) throws IOException {
        startingProcessesCount.incrementAndGet();
        try {
            if (user != null) {
                try {
                    TS.assertProgramExitCode(0, TS.ICACLS, Paths.get(cmd[0]).getParent().toString(), "/grant", "\"" + user.name + "\":F", "/T");
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            ArrayList<String> cmdLine = new ArrayList<String>();
            preparePSUtilsCall(cmdLine);
            if (show) {
                cmdLine.add("-v");
            }
            if (cpu) {
                cmdLine.add("-c");
            }
            cmdLine.add(dir);
            Collections.addAll(cmdLine, cmd);
            if (TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Session #" + id + " starting process: " + cmdLine);
            }
            //
            //
            for (int r = 0; r < 3; r++) {
                checkPsExeSvc();
                //
                int pid = TS.runProgramGetExitCode(120000, cmdLine.toArray(new String[cmdLine.size()]));
                if (pid < 0) {
                    pid = -pid;
                    if (pid >= 1000000000) { //1073741000
                        // error
                        long error = ((long) -pid);
                        TS.LOGGER.error(String.format("Session #" + id + ", process is unable to start correctly: 0x%X", error));
                        dumpCommandLine(cmd);
                    } else {
                        if (TS.LOGGER.isDebugEnabled()) {
                            TS.LOGGER.debug("Session #" + id + " started process ID=[" + pid + "] " + cmdLine);
                        }
                        addTermProcess(cmd[0], pid);
                        return pid;
                    }
                } else {
                    TS.LOGGER.error("Session #" + id + " process start error, code=" + pid);
                    dumpCommandLine(cmd);
                }
            }
            return 0;
        } finally {
            startingProcessesCount.decrementAndGet();
        }
    }

    private void checkPsExeSvc() {
        PidSession psExeSvc = getPsExeSvc();
        if (psExeSvc != null) {
            try {
                if (TS.LOGGER.isDebugEnabled()) {
                    TS.LOGGER.debug("Running PSEXESVC (PID=" + psExeSvc.getPid() + ")");
                }
                Thread.sleep(2500);
                PidSession psExeSvc2 = getPsExeSvc();
                if (psExeSvc2 != null && psExeSvc2.getPid() == psExeSvc.getPid()) {
                    TS.LOGGER.warn("Assuming PSEXESVC (PID=" + psExeSvc.getPid() + ") hung, killing it");
                    boolean isKilled = PSUtils.terminateApp(psExeSvc.getPid(), 0);
                    TS.LOGGER.warn("PSEXESVC (PID=" + psExeSvc.getPid() + ") kill res=" + isKilled);
                }
            } catch (Exception e) {
                TS.LOGGER.error("PSEXESVC (PID=" + psExeSvc.getPid() + ") workout error", e);
            }
        }
    }

    private PidSession getPsExeSvc() {
        PidSession psExeSvc = null;
        String[] processes = PSUtils.checkProcess2(TS.PSEXESVC, false, false);
        if (processes.length == 1) {
            psExeSvc = new PidSession(processes[0]);
        }
        return psExeSvc;
    }

    int terminateProcess(int pid, boolean show) throws IOException {
        if (!TS.USE_NJ4X_USER) return terminateProcess_psutil(pid, show);

        ArrayList<String> cmdLine = new ArrayList<String>();
        preparePSExecCall(cmdLine);
        cmdLine.add(System.getProperty("user.dir"));
        cmdLine.add("taskkill.exe");
        cmdLine.add("/PID");
        cmdLine.add("" + pid);
        cmdLine.add("/T");
        cmdLine.add("/F");
        if (TS.LOGGER.isDebugEnabled()) {
            TS.LOGGER.debug("Session #" + id + " terminating process: " + cmdLine);
        }
        String[] cmd = cmdLine.toArray(new String[cmdLine.size()]);
        int exit = ts.runProgramGetExitCode(30000, cmd);
        TS.LOGGER.info("Session #" + id + " terminating process: " + pid + " exit=" + exit);
        return 1;
    }

    int terminateProcess_psutil(int pid, boolean show) throws IOException {
        ArrayList<String> cmdLine = new ArrayList<String>();
        cmdLine.add(TS.PSEXEC);
        cmdLine.add("-i");
        cmdLine.add("" + this.id);
        if (user != null && this.id > 1 && user.id > 0) {
            cmdLine.add("-u");
            cmdLine.add(user.name);
            cmdLine.add("-p");
            cmdLine.add(user.password);
        }
        cmdLine.add("-w");
        cmdLine.add(System.getProperty("user.dir"));
        cmdLine.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "javaw.exe");
        cmdLine.add("-cp");
        cmdLine.add(System.getProperty("java.class.path"));
        cmdLine.add("com.jfx.ts.io.PSUtils");
        if (show) {
            cmdLine.add("-v");
        }
        cmdLine.add(System.getProperty("user.dir"));
        cmdLine.add("taskkill.exe");
        cmdLine.add("/PID");
        cmdLine.add("" + pid);
        if (TS.LOGGER.isDebugEnabled()) {
            TS.LOGGER.debug("Session #" + id + " terminating process: " + cmdLine);
        }
        String[] cmd = cmdLine.toArray(new String[cmdLine.size()]);
        int exit = ts.runProgramGetExitCode(30000, cmd);
        TS.LOGGER.info("Session #" + id + " terminating process: " + pid + " exit=" + exit);
        if (exit < 0) {
            exit *= -1;
            if (TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Session #" + id + " taskkill process ID=[" + exit + ']');
            }
            return exit;
        } else {
            TS.LOGGER.error("Session #" + id + " taskkill start error, code=" + exit);
            dumpCommandLine(cmd);
        }
        return 0;
    }

    private void dumpCommandLine(String[] cmd) {
        StringBuilder sb = getCmdLine(cmd);
        TS.LOGGER.error("Command line: " + sb);
    }

    private StringBuilder getCmdLine(String[] cmd) {
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
        return sb;
    }

    public int getHWND(int pid) {
        ArrayList<String> cmdLine = new ArrayList<String>();
        preparePSUtilsCall(cmdLine);
        cmdLine.add("GetHWND");
        cmdLine.add(String.valueOf(pid));
        if (TS.LOGGER.isDebugEnabled()) {
            TS.LOGGER.debug("Session #" + id + " getHWND, pid=" + pid + " cmd=" + cmdLine);
        }
        String[] cmd = cmdLine.toArray(new String[cmdLine.size()]);
        int exit = 0;
        try {
            exit = ts.runProgramGetExitCode(5000, cmd);
            if (exit <= 0) {
                if (TS.LOGGER.isDebugEnabled()) {
                    TS.LOGGER.debug("Session #" + id + " getHWND [OK], pid=" + pid + ", hWnd=" + exit);
                }
                return -exit;
            } else {
                TS.LOGGER.error("Session #" + id + " getHWND [NOK, " + exit + "], pid=" + pid);
                dumpCommandLine(cmd);
            }
        } catch (IOException e) {
            TS.LOGGER.error("Session #" + id + " getHWND [NOK, " + exit + "], pid=" + pid, e);
            dumpCommandLine(cmd);
        }
        //
        return 0;
    }

    public boolean toggleWindowVisibilityByPID(int pid) {
        if (user != null && this.id > 1 && user.id > 0) {
            ArrayList<String> cmdLine = new ArrayList<String>();
            preparePSUtilsCall(cmdLine);
            cmdLine.add("ToggleWindowVisibilityByPID");
            cmdLine.add(String.valueOf(pid));
            if (TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Session #" + id + " ToggleWindowVisibilityByPID, pid=" + pid + " cmd=" + cmdLine);
            }
            String[] cmd = cmdLine.toArray(new String[cmdLine.size()]);
            int exit = 0;
            try {
                exit = ts.runProgramGetExitCode(10000, cmd);
                if (exit <= 0) {
                    if (TS.LOGGER.isDebugEnabled()) {
                        TS.LOGGER.debug("Session #" + id + " ToggleWindowVisibilityByPID [OK], pid=" + pid + ", res=" + exit);
                    }
                    return exit != 0;
                } else {
                    TS.LOGGER.error("Session #" + id + " ToggleWindowVisibilityByPID [NOK, " + exit + "], pid=" + pid);
                    dumpCommandLine(cmd);
                }
            } catch (IOException e) {
                TS.LOGGER.error("Session #" + id + " ToggleWindowVisibilityByPID [NOK, " + exit + "], pid=" + pid, e);
                dumpCommandLine(cmd);
            }
            //
            return false;
        } else {
            return PSUtils.toggleWindowVisibilityByPID(pid);
        }
    }

    public boolean swShowHideByPID(boolean show, int pid) {
        if (user != null && this.id > 1 && user.id > 0) {
            ArrayList<String> cmdLine = new ArrayList<String>();
            preparePSUtilsCall(cmdLine);
            cmdLine.add("SWShowHideByPID");
            cmdLine.add(String.valueOf(show));
            cmdLine.add(String.valueOf(pid));
            if (TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Session #" + id + " SWShowHideByPID, pid=" + pid + " show=" + show + " cmd=" + cmdLine);
            }
            String[] cmd = cmdLine.toArray(new String[cmdLine.size()]);
            int exit = 0;
            try {
                exit = ts.runProgramGetExitCode(10000, cmd);
                if (exit <= 0) {
                    if (TS.LOGGER.isDebugEnabled()) {
                        TS.LOGGER.debug("Session #" + id + " SWShowHideByPID [OK], pid=" + pid + ", show=" + show + ", res=" + exit);
                    }
                    return exit != 0;
                } else {
                    TS.LOGGER.error("Session #" + id + " SWShowHideByPID [NOK, " + exit + "], pid=" + pid);
                    dumpCommandLine(cmd);
                }
            } catch (IOException e) {
                TS.LOGGER.error("Session #" + id + " SWShowHideByPID [NOK, " + exit + "], pid=" + pid, e);
                dumpCommandLine(cmd);
            }
            //
            return false;
        } else {
            return PSUtils.swShowHideByPID(show, pid);
        }
    }

    public void preparePSUtilsCall(ArrayList<String> cmdLine) {
        TsSystemUser nj4xUser = TS.USE_NJ4X_USER ? ts.sessionManager.getTsUserCreateTemplateIfNeeded(0) : null;
        cmdLine.add(TS.PSEXEC);
        cmdLine.add("-i");
        cmdLine.add("" + this.id);
        //noinspection ConstantConditions
        if (nj4xUser == null) {
            if (user != null) {
                cmdLine.add("-u");
                cmdLine.add(user.name);
                cmdLine.add("-p");
                cmdLine.add(user.password);
            }
        } else {
            cmdLine.add("-u");
            cmdLine.add(nj4xUser.name);
            cmdLine.add("-p");
            cmdLine.add(nj4xUser.password);
        }
        cmdLine.add("-w");
        cmdLine.add(System.getProperty("user.dir"));
        cmdLine.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "javaw.exe");//javaw.exe is important!!
        cmdLine.add("-cp");
        cmdLine.add(System.getProperty("java.class.path"));
        cmdLine.add("com.jfx.ts.io.PSUtils");
    }

    public void preparePSExecCall(ArrayList<String> cmdLine) {
        cmdLine.add(TS.PSEXEC);
        cmdLine.add("-d");
        if (user != null && this.user.id > 0) {
            cmdLine.add("-i");
            cmdLine.add("" + this.id);
            TsSystemUser nj4xUser = TS.USE_NJ4X_USER ? ts.sessionManager.getTsUserCreateTemplateIfNeeded(0) : null;
            //noinspection ConstantConditions
            if (nj4xUser == null) {
                cmdLine.add("-u");
                cmdLine.add(user.name);
                cmdLine.add("-p");
                cmdLine.add(user.password);
            } else {
                cmdLine.add("-u");
                cmdLine.add(nj4xUser.name);
                cmdLine.add("-p");
                cmdLine.add(nj4xUser.password);
            }
        }
        cmdLine.add("-w");
    }

    public boolean isPIDOwner(int pid) {
        return termProcesses.containsValue(pid);
    }

    public int getPID(String termName) {
        Integer integer = termProcesses.get(termName);
        return integer == null ? -1 : integer;
    }

    public Long getPIDStartTime(int pid) {
        return pidStartTime.get(pid);
    }

    public boolean terminateApp(int pid, int tmoutMillis, boolean show) {
        if (!TS.USE_NJ4X_USER) return terminateApp_psutil(pid, tmoutMillis, show);
        if (user != null && this.id > 1 && user.id > 0) {
            if (show) {
                TS.LOGGER.info("Closing terminal:"
                        + " pid=" + pid
                        + " tmout=" + tmoutMillis
                        + " sess=" + this.id
                );
            }
            ArrayList<String> cmdLine = new ArrayList<String>();
            preparePSExecCall(cmdLine);
            cmdLine.add(System.getProperty("user.dir"));
            cmdLine.add("taskkill.exe");
            cmdLine.add("/PID");
            cmdLine.add("" + pid);
            cmdLine.add("/T");
            cmdLine.add("/F");
            cmdLine.remove("-d");
            if (TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Session #" + id + " terminating process: " + cmdLine);
            }
            String[] cmd = cmdLine.toArray(new String[cmdLine.size()]);
            int exit = 0;
            try {
                exit = ts.runProgramGetExitCode(tmoutMillis, cmd);
                if (exit == 0) {
                    if (TS.LOGGER.isDebugEnabled()) {
                        TS.LOGGER.debug("Session #" + id + " TerminateApp [OK] " + pid);
                    }
                    return true;
                } else {
                    TS.LOGGER.error("Session #" + id + " TerminateApp [NOK, " + exit + "] " + pid);
                    dumpCommandLine(cmd);
                }
            } catch (IOException e) {
                TS.LOGGER.error("Session #" + id + " TerminateApp [NOK, " + exit + "] " + pid, e);
                dumpCommandLine(cmd);
            }
            //
            return false;
        } else {
            return ts.terminateApp(pid, tmoutMillis, show);
        }
    }

    public boolean terminateApp_psutil(int pid, int tmoutMillis, boolean show) {
        if (user != null && this.id > 1 && user.id > 0) {
            if (show) {
                TS.LOGGER.info("Closing terminal:"
                        + " pid=" + pid
                        + " tmout=" + tmoutMillis
                        + " sess=" + this.id
                );
            }
//                boolean b = PSUtils.postMsg(true, Integer.parseInt(hWnd), 16, 0, 0);
            ArrayList<String> cmdLine = new ArrayList<String>();
            preparePSUtilsCall(cmdLine);
            if (show) {
                cmdLine.add("-v");
            }
            cmdLine.add("TerminateApp");
            cmdLine.add(String.valueOf(pid));
            cmdLine.add(String.valueOf(tmoutMillis));
            if (TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("Session #" + id + " terminating process: " + cmdLine);
            }
            String[] cmd = cmdLine.toArray(new String[cmdLine.size()]);
            int exit = 0;
            try {
                exit = ts.runProgramGetExitCode(tmoutMillis, cmd);
                if (exit < 0) {
                    if (TS.LOGGER.isDebugEnabled()) {
                        TS.LOGGER.debug("Session #" + id + " TerminateApp [OK] " + pid);
                    }
                    return true;
                } else {
                    TS.LOGGER.error("Session #" + id + " TerminateApp [NOK, " + exit + "] " + pid);
                    dumpCommandLine(cmd);
                }
            } catch (IOException e) {
                TS.LOGGER.error("Session #" + id + " TerminateApp [NOK, " + exit + "] " + pid, e);
                dumpCommandLine(cmd);
            }
            //
            return false;
        } else {
            return ts.terminateApp(pid, tmoutMillis, show);
        }
    }

    public void clearAllTermProcesses() {
        termProcesses.clear();
        pidStartTime.clear();
    }
}
