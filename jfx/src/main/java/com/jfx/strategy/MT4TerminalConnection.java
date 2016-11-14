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

package com.jfx.strategy;

import com.jfx.MT4;
import com.jfx.io.Log4JUtil;
import com.jfx.net.Config;
import com.jfx.net.InprocessSocket;
import com.jfx.net.UbbArgsMapper;
import com.jfx.net.UnsafeByteBuffer;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * User: roman
 * Date: 24/9/2009
 * Time: 11:25:04
 */
public class MT4TerminalConnection extends BasicStrategyRunner {
    private static final Logger LOGGER = Logger.getLogger(BasicStrategyRunner.class);
    private static final String RESCMD = "R";//ESCMD";
    static long udpNo = 0;

    static {
        //LOGGER.setLevel(org.apache.log4j.Level.DEBUG);
    }

    long sent = 0;
    long rcvd = 0;
    private Socket socket;
    private long commandNo = 0;
    private InputStream bis;
    private OutputStream bos;
    private int id;
    private DatagramSocket udpSocket;
    private boolean isNoUDPAllowed;
    private boolean noMultilineConnection;
    private boolean isDisconnected = false;
    private int nextState = 0;

    public MT4TerminalConnection(String clientName) {
        setFullClientName(clientName);
        isNoUDPAllowed = isOrdersProcessingChannel();
    }

    public boolean isNoUDPAllowed() {
        return isNoUDPAllowed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUDPPort(int udpPort) throws SocketException {
        if (!isNoUDPAllowed) {
            udpSocket = new DatagramSocket();
            udpSocket.connect(socket.getInetAddress(), udpPort);
//            udpSocket.setSoTimeout(30000);
        }
    }

    public void close() {
        if (socket != null || udpSocket != null) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ignore) {
                } finally {
                    socket = null;
                }
            }
            if (udpSocket != null) {
                try {
                    udpSocket.close();
                } catch (Exception ignore) {
                } finally {
                    udpSocket = null;
                }
            }
            //
            super.close();
        }
    }

    private void sendToClient(UnsafeByteBuffer uni) throws IOException {
        int size = uni.remaining();
        bos.write(uni.array(), uni.position(), size);
        bos.flush();
        sent += size;
        //
        if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
            LOGGER.debug("MT4TerminalConnection: SENT TO " + clientName + ": " + uni.toString());
        }
    }

    private void sendToClient(String command) throws IOException {
//        byte[] b = (command + '\n').getBytes();
        byte[] b;
        if (noMultilineConnection) {
            b = (command + '\n').getBytes(Config.CHARSET);
        } else {
            b = (MT4.ARG_BEGIN + command + MT4.ARG_END).getBytes(Config.CHARSET);
        }
        OutputStream os = socket.getOutputStream();
        os.write(b);
        os.flush();
        sent += b.length;
        //
        if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
            LOGGER.debug("MT4TerminalConnection: SENT TO " + clientName + ": " + command);
        }
    }

/*
    private void sendToClient(DTO.Uni uni) throws IOException {
        int size = uni.getSerializedSize();
        bos.write(size & 0x000000ff);
        bos.write((size & 0x0000ff00) >> 8);
        bos.write((size & 0x00ff0000) >> 16);
        uni.writeTo(bos);
        bos.flush();
        sent += size;
        //
        if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
            LOGGER.debug("MT4TerminalConnection: SENT TO " + clientName + ": " + uni.toString());
        }
    }
*/

    protected UnsafeByteBuffer getCommand(UbbArgsMapper command) {
        return command.commandNo(commandNo++).build();
    }

/*
    protected DTO.Uni getCommand(UniArgsMapper command) {
        return command.commandNo(commandNo++).build();
    }
*/

    protected String getCommand(StringBuilder command) {
        try {
            return "" + commandNo + ' ' + command;
        } finally {
            commandNo++;
        }
    }

    public void setNoMultilineConnection(boolean noMultilineConnection) {
        this.noMultilineConnection = noMultilineConnection;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        try {
            if (this.socket instanceof InprocessSocket) {
                bis = this.socket.getInputStream();
            } else {
                bis = new BufferedInputStream(this.socket.getInputStream(), 10240);
                bos = new BufferedOutputStream(this.socket.getOutputStream(), 10240);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        return super.isConnected() && !isDisconnected && (socket == null || socket.isConnected()) && (udpSocket == null || udpSocket.isConnected());
    }

    public synchronized String sendCommandGetResult(UbbArgsMapper command) {
        StringBuilder sb = new StringBuilder();
        String line;
        int state = nextState; // 0 = GETCMD awaited, 1 = RESCMD awaited
        UnsafeByteBuffer uni = getCommand(command);
        if (uni.getInt(3) == _IDLE
                && strategy.isTickListenerStrategy()
                ) {
            nextState = 1;
        }
        int resCount = 0;
        while (true) {
            try {
                int b;
                //java is udp client
                if (udpSocket != null) {
                    if (isDisconnected) {
                        if (isPendingClose) {
                            return "R0@2";
                        }
                        throw new MT4DisconnectException("MT4TerminalConnection: Client " + clientName + " [i/o=" + rcvd + "/" + sent + "] disconnected");
                    }
                    //
                    final UDPClientPacket p = new UDPClientPacket();
                    if (state == 0) {
                        p.send(uni);
                    }
                    if (uni.getInt(3) == _IDLE) {
                        return null;
                    }
                    //
                    p.recieve();
                    if (nextState == 1) {
                        nextState = 0;
                    }
                    return p.getString();
                }
                //
                if (state == 1) {
                    b = bis.read();
                    if (b < 0) {
                        if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
                            LOGGER.info("MT4TerminalConnection: Client " + clientName + " [i/o=" + rcvd + "/" + sent + "] disconnected");
                        }
                        if (isPendingClose) {
                            return "R0@2";
                        }
                        throw new MT4DisconnectException("MT4TerminalConnection: Client " + clientName + " [i/o=" + rcvd + "/" + sent + "] disconnected");
                    }
                } else {
//                    UnsafeByteBuffer uni = getCommand(command);
                    sendToClient(uni);
                    if (uni.getInt(3) == _IDLE) {
                        return null;
                    }
                    state = 1;
                    continue;
                }
                //
//                if (b == '\n' || b == '\r') {
                if (b == MT4.ARG_END && resCount == 1 || noMultilineConnection && (b == '\n' || b == '\r')) {
                    line = sb.toString();
                    sb.setLength(0);
                    //
                    rcvd += line.length();
                    //
                    if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                        LOGGER.debug("MT4TerminalConnection: GOT FROM " + clientName + ": " + line);
                    }
                    //
                    if (line.startsWith(RESCMD)) {
                        if (nextState == 1) {
                            nextState = 0;
                        }
                        return line.substring(RESCMD.length());
                    } else {
                        LOGGER.error("MT4TerminalConnection: Unrecognized client (" + clientName + ") request: [" + line + "]");
                        sendToClient(getCommand(new StringBuilder("-2 UNRECOGNIZED REQ: " + line)));
                        state = 1;
                    }
                } else if (b != 0) {
                    if (resCount > 0 || noMultilineConnection) {
                        sb.append((char) b);
                    }
                    if (b == MT4.ARG_BEGIN) {
                        resCount++;
                    } else if (b == MT4.ARG_END) {
                        resCount--;
                    }
                }
            } catch (IOException e) {
                isDisconnected = true;
                if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
                    LOGGER.info("MT4TerminalConnection: Client " + clientName + " [i/o=" + rcvd + "/" + sent + "] disconnected (" + e + ")");
                }
                if (isPendingClose) {
                    return "R0@2";
                }
                throw new MT4DisconnectException("MT4TerminalConnection: Client " + clientName + " [i/o=" + rcvd + "/" + sent + "] disconnected (" + e + ")");
            }
        }
    }

    public synchronized String sendCommandGetResult(StringBuilder command) {
//        StringBuilder sb = new StringBuilder();
        ByteArrayOutputStream bab = new ByteArrayOutputStream();
        String line;
        int state = nextState; // 0 = GETCMD awaited, 1 = RESCMD awaited
        if (command == IDLE
                && strategy.isTickListenerStrategy()
                ) {
            nextState = 1;
        }
        int resCount = 0;
        while (true) {
            try {
                int b;
                //java is udp client
                if (udpSocket != null) {
                    if (isDisconnected) {
                        if (isPendingClose) {
                            return "R0@2";
                        }
                        throw new MT4DisconnectException("MT4TerminalConnection: Client " + clientName + " [i/o=" + rcvd + "/" + sent + "] disconnected");
                    }
                    //
                    final UDPClientPacket p = new UDPClientPacket();
                    if (state == 0) {
                        p.send(getCommand(command));
                    }
                    if (command == IDLE) {
                        return null;
                    }
                    //
                    p.recieve();
                    //
                    if (nextState == 1) {
                        nextState = 0;
                    }
                    return p.getString();
                }
                //
                if (state == 1) {
                    b = bis.read();
                    if (b < 0) {
                        if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
                            LOGGER.info("MT4TerminalConnection: Client " + clientName + " [i/o=" + rcvd + "/" + sent + "] disconnected");
                        }
                        if (isPendingClose) {
                            return "R0@2";
                        }
                        throw new MT4DisconnectException("MT4TerminalConnection: Client " + clientName + " [i/o=" + rcvd + "/" + sent + "] disconnected");
                    }
                } else {
                    sendToClient(getCommand(command));
                    if (command == IDLE) {
                        return null;
                    }
                    state = 1;
                    continue;
                }
                //
//                if (b == '\n' || b == '\r') {
                if (b == MT4.ARG_END && resCount == 1 || noMultilineConnection && (b == '\n' || b == '\r')) {
//                    line = sb.toString();
//                    sb.setLength(0);
                    //
                    line = bab.toString(Config.CHARSET_NAME);
                    bab.reset();
                    //
                    rcvd += line.length();
                    //
                    if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                        LOGGER.debug("MT4TerminalConnection: GOT FROM " + clientName + ": " + line);
                    }
                    //
                    if (line.startsWith(RESCMD)) {
                        if (nextState == 1) {
                            nextState = 0;
                        }
                        return line.substring(RESCMD.length());
                    } else {
                        LOGGER.error("MT4TerminalConnection: Unrecognized client (" + clientName + ") request: [" + line + "]");
                        sendToClient(getCommand(new StringBuilder("-2 UNRECOGNIZED REQ: " + line)));
                        state = 1;
                    }
                } else if (b != 0) {
                    if (resCount > 0 || noMultilineConnection) {
//                        sb.append((char) b);
                        bab.write(b);
                    }
                    if (b == MT4.ARG_BEGIN) {
                        resCount++;
                    } else if (b == MT4.ARG_END) {
                        resCount--;
                    }
                }
            } catch (IOException e) {
                nextState = 0;
                isDisconnected = true;
                if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
                    LOGGER.info("MT4TerminalConnection: Client " + clientName + " [i/o=" + rcvd + "/" + sent + "] disconnected (" + e + ")");
                }
                if (isPendingClose) {
                    return "R0@2";
                }
                throw new MT4DisconnectException("MT4TerminalConnection: Client " + clientName + " [i/o=" + rcvd + "/" + sent + "] disconnected (" + e + ")");
            }
        }
    }

    public class UDPClientPacket {
        byte[] data;
        private ByteBuffer bb;
        private long no;

        public UDPClientPacket() {
            data = new byte[10240];
            synchronized (MT4TerminalConnection.class) {
                no = udpNo++;
            }
        }

        public void send(UnsafeByteBuffer command) throws IOException {
            send(command.array(), command.position(), command.remaining());
        }

/*
        public void send(DTO.Uni command) throws IOException {
            send(command.toByteArray());
        }
*/

        public void send(String cmd) throws IOException {
            send(cmd.getBytes(Config.CHARSET));
        }

        private void send(byte[] bytes) throws IOException {
            send(bytes, 0, bytes.length);
        }

        private void send(byte[] bytes, int offset, int length) throws IOException {
            DatagramPacket p = new DatagramPacket(bytes, offset, length);
            if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                LOGGER.debug("" + no + "->UDP [id=" + id + "] [" + new String(p.getData(), p.getOffset(), p.getLength()) + "] ...");
            }
            udpSocket.send(p);
            sent += p.getLength();
        }

        public void recieve() throws IOException {
            DatagramPacket p = new DatagramPacket(data, 0, data.length);
//            udpSocket.setSoTimeout(60000);
            udpSocket.receive(p);
            rcvd += p.getLength();
            if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                LOGGER.debug("" + no + "<-UDP [id=" + id + "] [" + new String(p.getData(), p.getOffset(), p.getLength()) + "]");
            }
            bb = ByteBuffer.wrap(p.getData(), p.getOffset(), p.getLength());
        }

        public ByteBuffer getBb() {
            return bb;
        }

        public String getString() {
            return bb == null ? null : new String(bb.array(), bb.position(), bb.remaining());
        }
    }

}
