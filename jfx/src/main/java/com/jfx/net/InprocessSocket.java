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

package com.jfx.net;

import com.jfx.strategy.MT4InprocessConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * User: roman
 * Date: 12.05.12
 * Time: 7:20
 */
public class InprocessSocket extends Socket {
    public static final int BUFSZ = 10240;
    public static final int MAX_PREPARE_STATE = Integer.MAX_VALUE;
    private boolean isClosed;
    private InprocessInputStream in;
    private InprocessOutputStream out;
    private MT4InprocessConnection mt4InprocessConnection;

    @Override
    public synchronized void close() throws IOException {
        if (!isClosed) {
            isClosed = true;
            MT4InprocessConnection inprocessConnection = mt4InprocessConnection;
            if (inprocessConnection != null) {
                inprocessConnection.close();
            }
            super.close();
        }
    }

    int state;

    public InprocessSocket() {
        in = new InprocessInputStream();
        out = new InprocessOutputStream();
        state = 0;
    }

    public void write2(char[] s, int len) {//used by mt4if.dll
//commentme System.out.println("\nwrite2: " + mt4InprocessConnection + ", state==" + state + ", s=" + new String(s, 0, Math.min(len, s.length)) + " s.length=" + s.length + " len=" + len );
        if (mt4InprocessConnection == null || state < MAX_PREPARE_STATE) {
            state++;
            in.write(s, len);
        } else {
            mt4InprocessConnection.setRes(new String(s, 0, len));//todo UTF-8 decode
        }
    }

    public void write(String s) {
        try {
//commentme System.out.println("\nwrite: " + mt4InprocessConnection + ", state==" + state + ", s=" + s + " s.length=" + s.length());
            if (mt4InprocessConnection == null || state < MAX_PREPARE_STATE) {
                state++;
                in.write(s.toCharArray(), -1);
            } else {
                mt4InprocessConnection.setRes(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int cmno = 0;
    long start;

    public String read() {
        if (mt4InprocessConnection == null || state < MAX_PREPARE_STATE) {
            String read = out.read();
//commentme System.out.println("\nread: " + mt4InprocessConnection + ", state==" + state + ", s=" + read + ", len=" + read.length());
            if (read.equals("\u0001START\u0002")) {
                state = MAX_PREPARE_STATE;
//commentme System.out.println("\nread: " + mt4InprocessConnection + ", state==" + state);
            }
            return read;
        } else {
            String cmd = mt4InprocessConnection.getCmd();
//commentme System.out.println("\nread: " + mt4InprocessConnection + ", state==" + state + ", cmd=" + cmd+ ", len=" + cmd.length());
            return cmd;
        }
    }

    public int read3(byte[] s, int bufSize) {
//commentme System.out.println("\nread3: " + mt4InprocessConnection + ", state==" + state);
        if (mt4InprocessConnection == null || state < MAX_PREPARE_STATE) {
            return out.read(s, bufSize);
        } else {
            return mt4InprocessConnection.getCmd(s, bufSize);
        }
    }

    public int read2(char[] s, int len) {
//commentme System.out.println("\nread2: " + mt4InprocessConnection + ", state==" + state + ", s=" + new String(s, 0, len) + ", len=" + len);
        if (mt4InprocessConnection == null || state < MAX_PREPARE_STATE) {
            int sz = out.read(s, len);
            if (sz == 7 && "START".equals(new String(s, 1, 5))) {
                state = MAX_PREPARE_STATE;
//commentme System.out.println("\nread2: " + mt4InprocessConnection + ", state==" + state);
            }
            return sz;
        } else {
            return mt4InprocessConnection.getCmd(s, len);
        }
    }

    public void setConnection(MT4InprocessConnection mt4InprocessConnection) {
        this.mt4InprocessConnection = mt4InprocessConnection;
    }

    @SuppressWarnings("ThrowFromFinallyBlock")
    public class InprocessInputStream extends InputStream {
        private final char[] buf;
        private char[] rbuf;
        private int pos, rpos;

        private InprocessInputStream() {
            buf = new char[BUFSZ];
            rbuf = new char[BUFSZ];
            pos = -1;
            rpos = -1;
        }

        public void write(char[] data, int len) {
            try {
                int dataPos = 0;
                synchronized (buf) {
                    int length = len < 0 ? data.length : len;
                    while (!isClosed && dataPos < length) {
                        while (pos == BUFSZ - 1) {
                            try {
                                buf.wait(100);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        int avail_wr = BUFSZ - pos - 1;
                        int avail_rd = length - dataPos;
                        int min = avail_rd > avail_wr ? avail_wr : avail_rd;
                        if (min > 0) {
                            System.arraycopy(data, dataPos, buf, pos + 1, min);
//commentme System.out.println("" + this + " <IN> [" + new String(buf, pos + 1, min) + "]");
                            pos += min;
                            buf.notify();
                            break;
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
                throw new RuntimeException(t);
            }
        }

        @Override
        public int read() throws IOException {
            try {
                if (!isClosed && rpos >= 0) {
                    return rbuf[rpos--];
                }
                synchronized (buf) {
                    while (!isClosed && pos < 0) {
                        try {
                            buf.wait(100);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    if (!isClosed && pos >= 0) {
                        try {
                            while (pos >= 0) rbuf[++rpos] = buf[pos--];
                            return rbuf[rpos--];
                        } finally {
                            buf.notify();
                        }
                    }
                }
                return -1;
            } catch (Throwable t) {
                t.printStackTrace();
                throw new RuntimeException(t);
            } finally {
                if (isClosed) {
                    throw new IOException("In-process socket closed");
                }
            }
        }
    }

    @SuppressWarnings("ThrowFromFinallyBlock")
    public class InprocessOutputStream extends OutputStream {
        private final byte[] buf;
        private int pos;

        private InprocessOutputStream() {
            buf = new byte[BUFSZ];
            pos = 0;
        }

        @Override
        public void write(byte[] data, int off, int len) throws IOException {
            try {
                int dataPos = off;
                synchronized (buf) {
                    while (!isClosed && dataPos - off < len) {
                        while (pos == BUFSZ) {
                            try {
                                buf.wait();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        int avail_wr = BUFSZ - pos;
                        int avail_rd = len - dataPos + off;
                        int min = avail_rd > avail_wr ? avail_wr : avail_rd;
                        System.arraycopy(data, dataPos, buf, pos, min);
                        pos += min;
                        dataPos += min;
//commentme System.out.println("" + this + " <OUT> [" + new String(buf, 0, pos) + "]");
                        buf.notify();
                    }
                }
            } finally {
                if (isClosed) {
                    throw new IOException("In-process socket closed");
                }
            }
        }

        @Override
        public void write(int b) throws IOException {
            try {
                synchronized (buf) {
                    try {
                        while (!isClosed && pos == BUFSZ) {
                            try {
                                buf.wait();
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        if (!isClosed && pos < BUFSZ) {
                            buf[pos++] = (byte) b;
                        }
                    } finally {
                        buf.notify();
                    }
                }
            } finally {
                if (isClosed) {
                    throw new IOException("In-process socket closed");
                }
            }
        }

        public String read() {
            synchronized (buf) {
                while (pos == 0) {
                    try {
                        buf.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                String s = new String(buf, 0, pos);
                pos = 0;
//commentme System.out.println("" + this + " <READ> [" + s + "]");
                return s;
            }
        }

        public boolean isAvailable() {
            synchronized (buf) {
                return pos > 0;
            }
        }

        public int read(char[] s, int len) {
            synchronized (buf) {
                while (pos == 0) {
                    try {
                        buf.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                int sz = pos > len ? len : pos;
                for (int i = 0; i < sz; ++i) s[i] = (char) buf[i];
                pos -= sz;
//commentme System.out.println("" + this + " <READ-c> [" + new String(s, 0, sz) + "]");
                return sz;
            }
        }

        public int read(byte[] s, int bufSize) {
            synchronized (buf) {
                while (pos == 0) {
                    try {
                        buf.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                int sz = pos > bufSize ? bufSize : pos;
                System.arraycopy(buf, 0, s, 0, sz);
                pos -= sz;
//    System.out.println("" + this + " <READ-b> [" + new String(s, 0, sz) + "]");
                return sz;
            }
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return in;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return out;
    }
}
