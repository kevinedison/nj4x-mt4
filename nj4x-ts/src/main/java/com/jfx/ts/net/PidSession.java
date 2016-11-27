package com.jfx.ts.net;

import com.jfx.ts.io.PSUtils;

/**
 * 这个应该是一个终端的描述
 * Created by roman on 21.10.2016.
 */
class PidSession {
    private String process;
    private int sessionId;
    private int pid;

    public PidSession(String process) {
        this.process = process;
        invoke();
    }

    public int getSessionId() {
        return sessionId;
    }

    public int getPid() {
        return pid;
    }
    //貌似是计算pid的
    public PidSession invoke() {
        try {
            String _sessionId = process.substring(process.indexOf('\u0001') + 1);
            int ix = _sessionId.indexOf(':');
            //noinspection UnusedDeclaration
            String _pid;
            if (ix > 0) {
                //noinspection UnusedAssignment
                _pid = _sessionId.substring(ix + 1);
                _sessionId = _sessionId.substring(0, ix);
            } else {
                //noinspection UnusedAssignment
                _pid = "0";
            }
            pid = Integer.parseInt(_pid);
            sessionId = PSUtils.parseSessionId(_sessionId, TS.P_USE_MSTSC);
        } catch (NumberFormatException e) {
            String err = "Invalid PSUtils(_x64).dll detected! process=[" + process + "]";
            throw new RuntimeException(err);
        }
        return this;
    }
}
