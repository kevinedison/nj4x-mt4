package com.jfx.strategy;

import com.jfx.net.InprocessSocket;
import com.jfx.net.UbbArgsMapper;
import com.jfx.net.UnsafeByteBuffer;

import java.net.Socket;
import java.util.concurrent.Semaphore;

/**
 * Created with IntelliJ IDEA.
 * User: roman
 * Date: 14.05.12
 * Time: 15:29
 */
public class MT4InprocessConnection extends MT4TerminalConnection {
    private Semaphore cmdReadySemaphore;
    private Semaphore resReadySemaphore;
    private Object cmd;
    private String res;

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void close() {
        super.close();
        cmdReadySemaphore.release();
        resReadySemaphore.release();
    }

    @Override
    public void setSocket(Socket socket) {
        super.setSocket(socket);
        ((InprocessSocket) socket).setConnection(this);
    }

    public boolean isStarted() {
        return strategy != null;
    }

    public MT4InprocessConnection(String clientName) {
        super(clientName);
        this.cmdReadySemaphore = new Semaphore(0);
        this.resReadySemaphore = new Semaphore(0);
    }

    @Override
    public synchronized String sendCommandGetResult(UbbArgsMapper command) {
        cmd = command.build();
        cmdReadySemaphore.release();//4 - cmd is here, releasing semaphore

//commentme System.out.println("\n sendCommandGetResult: cmdReadySemaphore.release(), command==" + command);

        if (((UnsafeByteBuffer) cmd).getInt(3) == -1) {
            resReadySemaphore.acquireUninterruptibly();
            return null;
        }
        //
        resReadySemaphore.acquireUninterruptibly();//6- waiting for res
        if (!isAlive) {
            throw new MT4DisconnectException("-Closed-");
        }

//commentme System.out.println("\n sendCommandGetResult: resReadySemaphore.acquireUninterruptibly(), command==" + command);

        return res;
    }

    @Override
    public String sendCommandGetResult(StringBuilder command) {
        cmd = command;
//commentme System.out.println("\nsendCommandGetResult: cmdReadySemaphore.release(), command==" + command);
        cmdReadySemaphore.release();//4 - cmd is here, releasing semaphore
        if (cmd == IDLE) {
            resReadySemaphore.acquireUninterruptibly();
            return null;
        }
        //
        resReadySemaphore.acquireUninterruptibly();//6- waiting for res
        if (!isAlive) {
            throw new MT4DisconnectException("-Closed-");
        }
        //commentme System.out.println("\n  sendCommandGetResult: resReadySemaphore.acquireUninterruptibly(), command==" + command + " res=" + res);
        return res;
    }

    public int getCmd(byte[] s, int bufSize) {
        cmdReadySemaphore.acquireUninterruptibly();//3 - wait until cmd arrives
        if (!isAlive) {
            throw new MT4DisconnectException("-Closed-");
        }
//commentme System.out.println("\n getCmd: BB cmdReadySemaphore.acquireUninterruptibly()");
        UnsafeByteBuffer bb = (UnsafeByteBuffer) cmd;
        int r = bb.remaining();
        bb.get(s, 0, r > bufSize ? bufSize : r);
        if (((UnsafeByteBuffer) cmd).getInt(3) == -1) {
            resReadySemaphore.release();
        }
        return r;
    }

    public int getCmd(char[] s, int len) {
        cmdReadySemaphore.acquireUninterruptibly();//3 - wait until cmd arrives
        if (!isAlive) {
            throw new MT4DisconnectException("-Closed-");
        }
        StringBuilder sb = (StringBuilder) cmd;
//commentme System.out.println("\n getCmd: cmdReadySemaphore.acquireUninterruptibly(): [" + cmd + ']');
        int l2 = sb.length();
        int sz = len > l2 ? l2 : len;
        sb.getChars(0, sz, s, 1);
        //s[0] = MT4.ARG_BEGIN;
        if (cmd == IDLE) {
            resReadySemaphore.release();
        }
        return sz + 2;
    }

    public String getCmd() {
        cmdReadySemaphore.acquireUninterruptibly();//3 - wait until cmd arrives
        if (!isAlive) {
            throw new MT4DisconnectException("-Closed-");
        }
        String s;
        StringBuilder sb = (StringBuilder) cmd;
        s = sb.toString();
        if (cmd == IDLE) {
            resReadySemaphore.release();
        }
        return s;
    }

    public void setRes(String res) {
        this.res = res;
        resReadySemaphore.release();//7 - res is ready
//commentme System.out.println("\n setRes: resReadySemaphore.release() res=" + this.res);
    }

}
