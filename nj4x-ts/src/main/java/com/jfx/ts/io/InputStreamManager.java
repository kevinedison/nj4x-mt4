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

package com.jfx.ts.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings({"LocalVariableNamingConvention"})
public class InputStreamManager {
    private InputStreamManager.InputStreamReadableByteChannel inputStreamReadableByteChannel;
    private int bufferSize;

    public int getBufferSize() {
        return bufferSize;
    }

    public InputStreamManager(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public InputStreamManager(InputStream is) {
        this(16386);
        inputStreamReadableByteChannel = new InputStreamManager.InputStreamReadableByteChannel(is);
    }

    public ReadableByteChannel getReadableByteChannel() throws IOException {
        return inputStreamReadableByteChannel;
    }

    public InputStream getInputStream() throws IOException {
        return inputStreamReadableByteChannel.getIs();
    }

    public void close() throws IOException {
        inputStreamReadableByteChannel.close();
    }

    String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList getLines(String encoding) throws IOException {
        InputStreamReader isr = new InputStreamReader(getInputStream(), encoding);
        try {
            char[] b = new char[16384];
            char[] line = null;
            ArrayList lines = new ArrayList();
            char NL = '\n';
            char CR = '\r';
            while (true) {
                int rc = isr.read(b);
                if (rc > 0) {
                    int indexFrom = 0;
                    for (int i = 0; i < rc; ++i) {
                        if (b[i] == NL || b[i] == CR) {
                            int lineLength = i - indexFrom;
                            if (lineLength + (line != null ? line.length : 0) > 0) {
                                String strLine;
                                if (line != null) {
                                    char[] newLine = new char[
                                            lineLength +
                                                    (line != null ? line.length : 0)
                                            ];
                                    System.arraycopy(line, 0, newLine, 0, line.length);
                                    System.arraycopy(b, indexFrom, newLine, line.length, lineLength);
                                    strLine = new String(newLine);
                                    line = null;
                                } else {
                                    strLine = new String(b, indexFrom, lineLength);
                                }
                                lines.add(strLine);
                            }
                            //
                            if (i + 1 < rc && (b[i + 1] == NL || b[i + 1] == CR)) {
                                indexFrom = i + 2;
                                i++;
                            } else {
                                indexFrom = i + 1;
                            }
                        }
                    }
                    if (indexFrom < rc) {
                        int lineLength = rc - indexFrom;
                        line = new char[lineLength];
                        System.arraycopy(b, indexFrom, line, 0, lineLength);
                    }
                }
                if (rc < b.length) {
                    break;
                }
            }
            if (line != null) {
                lines.add(new String(line));
            }
            return lines;
        } finally {
            isr.close();
        }
    }

    public byte[] getBytes() throws IOException {
        InputStreamManager.GetBytesListener lsnr = new InputStreamManager.GetBytesListener();
        readBytes(lsnr);
        return lsnr.getBytesArray();
    }

    public byte[] getBytesFixLF() throws IOException {
        InputStreamManager.GetBytesListenerLF lsnr = new InputStreamManager.GetBytesListenerLF();
        readBytes(lsnr);
        return lsnr.getBytesArray();
    }

    public char[] getChars(String encoding) throws IOException {
        InputStreamReader isr = new InputStreamReader(getInputStream(), encoding);
        try {
            char[] b = new char[16384];
            char[] res = null;
            while (true) {
                int rc = isr.read(b);
                if (rc > 0) {
                    if (res == null) {
                        res = new char[rc];
                        System.arraycopy(b, 0, res, 0, rc);
                    } else {
                        char[] newRes = new char[res.length + rc];
                        System.arraycopy(res, 0, newRes, 0, res.length);
                        System.arraycopy(b, 0, newRes, res.length, rc);
                        res = newRes;
                    }
                }
                if (rc < b.length) {
                    break;
                }
            }
            return res;
        } finally {
            isr.close();
        }
    }

    public void readASCIILines(LineListener lsnr) throws IOException {
        readASCIILines(lsnr, 0, Integer.MAX_VALUE);
    }

    public void readASCIILines(LineListener lsnr, int startLine, int endLine) throws IOException {
        ReadableByteChannel readableByteChannel = getReadableByteChannel();
        try {
            _readASCIILines(readableByteChannel, lsnr, startLine, endLine);
        } finally {
            try {
                readableByteChannel.close();
            } catch (IOException ignore) {
            }
            close();
        }
    }

    private void _readASCIILines(ReadableByteChannel udrChannel, LineListener lsnr, int startLine, int endLine) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
        boolean produceBytes = lsnr instanceof BytesLineListener;
        BytesLineListener bll = null;
        ByteBuffer bb = null;
        StringBuffer sb = produceBytes ? null : new StringBuffer();
        if (produceBytes) {
            bll = (BytesLineListener) lsnr;
            bb = ByteBuffer.allocate(bll.getMaxLineSize());
        }
        int line = 0;
        while (true) {
            int rd = udrChannel.read(byteBuffer);
            if (rd <= 0) {
                break;
            }
            if (byteBuffer.position() != 0) {
                byteBuffer.flip();
            }
            while (byteBuffer.position() < byteBuffer.limit()) {
                byte __b = byteBuffer.get();
                char c = (char) __b;
                if (c == '\n' || c == '\r') {
                    line++;
                    if (line >= startLine) {
                        if (produceBytes) {
                            bb.flip();
                            bll.onLine(line, bb);
                        } else {
                            lsnr.onLine(line, sb);
                        }
                    }
                    if (produceBytes) {
                        bb.clear();
                    } else {
                        sb.delete(0, sb.length());
                    }
                    if (line >= endLine) {
                        break;
                    }

                    while (byteBuffer.position() < byteBuffer.limit()) {
                        byte _b = byteBuffer.get();
                        if (produceBytes) {
                            if (_b != '\n' && _b != '\r') {
                                bb.put(_b);
                                break;
                            }
                        } else {
                            c = (char) _b;
                            if (c != '\n' && c != '\r') {
                                sb.append(c);
                                break;
                            }
                        }
                    }
                } else {
                    if (produceBytes) {
                        bb.put(__b);
                    } else {
                        sb.append(c);
                    }
                }
            }
            byteBuffer.clear();
        }
        if (produceBytes) {
            bb.flip();
            if (bb.remaining() > 0) {
                bll.onLine(++line, bb);
            }
        } else {
            if (sb.length() > 0) {
                lsnr.onLine(++line, sb);
            }
        }
        if (lsnr instanceof ExtendedLineListener) {
            ((ExtendedLineListener) lsnr).onFileEnd(line);
        }
    }

    private byte[] buffer;
    private boolean eof;
    int bufferLength;

    public synchronized void readCommand(CommandListener lsnr) throws IOException {
        buffer = buffer == null ? new byte[1024] : buffer;
        StringBuffer fullCommand = new StringBuffer();

        InputStream inputStream = inputStreamReadableByteChannel.getIs();
        while (true) {
            if (!eof && bufferLength <= 0) {
                bufferLength = inputStream.read(buffer);
                if (bufferLength < 0) {
                    eof = true;
                }
            } else if (eof) {
                throw new IOException("Try to read command after EOF.");
            }

            int i = 0;
            boolean commandFinished = false;
            for (; i < bufferLength; i++) {
                if (commandFinished = lsnr.isCommandFinished(buffer, i, fullCommand)) {
                    break;
                }
            }

            int len = bufferLength - i - 1;
            if (len > 0) {
                i++;
                for (int p = 0; i < bufferLength; i++, p++) {
                    buffer[p] = buffer[i];
                }
                bufferLength = len;
            } else {
                if (bufferLength > 0) {
                    fullCommand.append(new String(buffer, 0, bufferLength));
                }
                bufferLength = 0;
                if (eof) {
                    lsnr.onEof();
                }
            }

            if (commandFinished) {
                return;
            }
        }
    }

    public void readBytes(BytesListener lsnr) throws IOException {
        readBytes(lsnr, 0, Integer.MAX_VALUE);
    }

    public void readBytes(BytesListener lsnr, int startOffset, int endOffset) throws IOException {
        ReadableByteChannel readableByteChannel = getReadableByteChannel();
        try {
            _readBytes(readableByteChannel, lsnr, startOffset, endOffset);
        } finally {
            try {
                readableByteChannel.close();
            } catch (IOException ignore) {
            }
            close();
        }
    }

    private void _readBytes(ReadableByteChannel udrChannel, BytesListener lsnr, int startOffset, int endOffset) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
        int pos = 0;
        boolean started = false;
        while (true) {
            int rd = udrChannel.read(byteBuffer);
            if (rd <= 0) {
                break;
            }
            pos += rd;
            if (pos >= startOffset) {
                byte[] buffer = byteBuffer.array();
                if (!started) {
                    int startBufferOffset = rd - pos + startOffset;
                    if (startBufferOffset > 0) {
                        buffer = new byte[rd - startBufferOffset];
                        System.arraycopy(byteBuffer.array(), startBufferOffset, buffer, 0, rd = buffer.length);
                    }
                    started = true;
                }
                if (pos > endOffset) {
                    int extraBytes = pos - endOffset;
                    byte[] _buffer = new byte[rd - extraBytes];
                    System.arraycopy(buffer, 0, _buffer, 0, rd = _buffer.length);
                    buffer = _buffer;
                }
                lsnr.onBuffer(buffer, rd);
                if (pos > endOffset) {
                    break;
                }
            }
            byteBuffer.position(0);
            byteBuffer.limit(byteBuffer.capacity());
        }
    }

    public void zipIt(String toZipFileName, String zipEntryName, String underThePathInZip) throws IOException {
        if (underThePathInZip == null || underThePathInZip.length() == 0) {
            underThePathInZip = ".";
        }
        ZipOutputStream zos = null;
        final IOException[] excp = new IOException[1];
        try {
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(toZipFileName)));
            final ZipOutputStream _zos = zos;
            final ZipEntry ze = new ZipEntry(
                    underThePathInZip + '/' + zipEntryName
            );
            zos.putNextEntry(ze);
            readBytes(new BytesListener() {
                public void onBuffer(byte[] buffer, int limit) {
                    try {
                        _zos.write(buffer, 0, limit);
                    } catch (IOException e) {
                        excp[0] = e;
                    }
                }
            });
            zos.closeEntry();
            zos.close();
            if (excp[0] != null) {
                deleteFile(toZipFileName);
                throw excp[0];
            }
        } catch (IOException e) {
            if (zos != null) {
                zos.close();
            }
            deleteFile(toZipFileName);
            throw e;
        }
    }

    public void gzipIt(String toZipFileName, String underThePath) throws IOException {
        GZIPOutputStream zos = null;
        final IOException[] excp = new IOException[1];
        try {
            zos = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(
                    underThePath == null
                            ? toZipFileName
                            : underThePath + '/' + new File(toZipFileName).getName()
            ), 1024 * 1024 * 4));
            final GZIPOutputStream _zos = zos;
            readBytes(new BytesListener() {
                public void onBuffer(byte[] buffer, int limit) {
                    try {
                        _zos.write(buffer, 0, limit);
                    } catch (IOException e) {
                        excp[0] = e;
                    }
                }
            });
            zos.close();
            if (excp[0] != null) {
                deleteFile(toZipFileName);
                throw excp[0];
            }
        } catch (IOException e) {
            if (zos != null) {
                zos.close();
            }
            deleteFile(toZipFileName);
            throw e;
        }
    }

    protected void deleteFile(String fileName) {
        try {
            new File(fileName).delete();
        } catch (Exception ignore) {
        }
    }

    protected static class InputStreamReadableByteChannel implements ReadableByteChannel {
        //        int offset;
        private InputStream is;
//        private int available;

        public InputStreamReadableByteChannel(InputStream is) {
            this.is = is;
//            if (is != null) {
//                try {
//                    available = this.is.available();
//                } catch (IOException e) {
//                    available = -1;
//                }
//            }
//            offset = 0;
        }

        public InputStream getIs() {
            return is;
        }

//        public int getAvailable() {
//            if (is != null) {
//                try {
//                    return this.is.available();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return -1;
//                }
//            }
//            return 0;
//        }

        public int read(ByteBuffer dst) throws IOException {
            int dstSize = dst.limit() - dst.position();
            int _available = is.available();
//            int ava = getAvailable();
//            if (ava == 0 || is.available() > 0 && offset < ava) {
            if (_available >= 0) {
                int max = Math.min(dstSize, _available == 0 ? dstSize : _available);
                byte[] res = new byte[max];
                int rc = is.read(res, 0, res.length);
                do {
                    if (rc == -1) {
                        return -1;
                    } else if (rc == 0) {
                        try {
                            Thread.sleep(1000); // it is a user input stream then
                        } catch (InterruptedException e) {
                        }
                    } else {
//                        offset += rc;
                        dst.put(res, 0, rc);
                        break;
                    }
                } while (true);
                return rc;
            }
            return -1;
        }

        public void close() throws IOException {
            if (isOpen()) {
                try {
                    is.close();
                } finally {
                    is = null;
                }
            }
        }

        public boolean isOpen() {
            return is != null;
        }
    }

    private static class GetBytesListener implements BytesListener {
        private byte[] bytesArray;

        public byte[] getBytesArray() {
            return bytesArray;
        }

        public void onBuffer(byte[] buffer, int limit) {
            if (bytesArray == null) {
                bytesArray = new byte[limit];
                System.arraycopy(buffer, 0, bytesArray, 0, limit);
            } else {
                byte[] _bytesArray = new byte[bytesArray.length + limit];
                System.arraycopy(bytesArray, 0, _bytesArray, 0, bytesArray.length);
                System.arraycopy(buffer, 0, _bytesArray, bytesArray.length, limit);
                bytesArray = _bytesArray;
            }
        }
    }

    private static class GetBytesListenerLF implements BytesListener {
        private byte[] bytesArray;

        public byte[] getBytesArray() {
            return bytesArray;
        }

        public void onBuffer(byte[] buffer, int limit) {
            int newLength = 0;
            for (int j = 0; j < limit; ++j) {
                byte _byte = buffer[j];
                if (_byte != 0x0D) {
                    newLength++;
                }
            }
            //
            if (bytesArray == null) {
                bytesArray = new byte[newLength];
                //
                newLength = 0;
                for (int j = 0; j < limit; ++j) {
                    byte _byte = buffer[j];
                    if (_byte != 0x0D) {
                        bytesArray[newLength++] = _byte;
                    }
                }
                //System.arraycopy(buffer, 0, bytesArray, 0, limit);
            } else {
                byte[] _bytesArray = new byte[bytesArray.length + newLength];
                System.arraycopy(bytesArray, 0, _bytesArray, 0, bytesArray.length);
                //
                newLength = 0;
                for (int j = 0; j < limit; ++j) {
                    byte _byte = buffer[j];
                    if (_byte != 0x0D) {
                        _bytesArray[bytesArray.length + newLength++] = _byte;
                    }
                }
                //System.arraycopy(buffer, 0, _bytesArray, bytesArray.length, limit);
                bytesArray = _bytesArray;
            }
        }
    }
}
