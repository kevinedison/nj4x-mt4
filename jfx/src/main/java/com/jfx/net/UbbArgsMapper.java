/*
 * Metatrader Java (JFX) / .Net (NJ4X) library
 * Copyright (c) 2008-2014 by Gerasimenko Roman.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistribution of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The names "JFX" or "NJ4X" must not be used to endorse or
 * promote products derived from this software without prior
 * written permission. For written permission, please contact
 * roman.gerasimenko@nj4x.com
 *
 * 4. Products derived from this software may not be called "JFX" or
 * "NJ4X", nor may "JFX" or "NJ4X" appear in their name,
 * without prior written permission of Gerasimenko Roman.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE JFX CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package com.jfx.net;

/**
 * User: roman
 * Date: 26/11/13
 * Time: 19:56
 */
public class UbbArgsMapper {
    private final int id;
    private long commandNo;
    private int pos;
    private int intPos, longPos, stringPos, doublePos;
    private int[] ints, longs, doubles, strings;
    private int[] intVs;
    private long[] longVs;
    private double[] doubleVs;
    private String[] stringVs;

    public UbbArgsMapper(int id) {
        this.id = id;
        pos = intPos = stringPos = doublePos = 0;
        ints = new int[16];
        intVs = new int[16];
        longs = new int[16];
        longVs = new long[16];
        doubles = new int[16];
        doubleVs = new double[16];
        strings = new int[16];
        stringVs = new String[16];
    }

    public UbbArgsMapper commandNo(long v) {
        commandNo = v;
        return this;
    }

    public UbbArgsMapper append(int v) {
        addInt(v);
        return this;
    }

    public UbbArgsMapper append(long v) {
        addLong(v);
        return this;
    }

    public UbbArgsMapper append(String v) {
        addString(v);
        return this;
    }

    public UbbArgsMapper append(double v) {
        addDouble(v);
        return this;
    }

    public UbbArgsMapper append(char ignored) {
        return this;
    }

    public void addInt(int v) {
        intVs[intPos] = v;
        ints[intPos++] = pos++;
    }

    public void addLong(long v) {
        longVs[longPos] = v;
        longs[longPos++] = pos++;
    }

    public void addDouble(double v) {
        doubleVs[doublePos] = v;
        doubles[doublePos++] = pos++;
    }

    public void addString(String v) {
        stringVs[stringPos] = v;
        strings[stringPos++] = pos++;
    }

    @Override
    public String toString() {
        return "UBB: id=" + id + " cmd_no=" + commandNo;
    }

    public UnsafeByteBuffer build() {
        UnsafeByteBuffer b = UnsafeByteBuffer.allocate(8192);
        //
        b.put((byte) 0);
        b.put((byte) 0);
        b.put((byte) 0);
        //
        b.putInt(this.id);
        b.putLong(this.commandNo);
        //
        b.put((byte) intPos);
        b.put((byte) longPos);
        b.put((byte) doublePos);
        b.put((byte) stringPos);
        //
        for (int j = 0; j < intPos; j++) {
            b.put((byte) ints[j]);
            b.putInt(intVs[j]);
        }
        for (int j = 0; j < longPos; j++) {
            b.put((byte) longs[j]);
            b.putLong(longVs[j]);
        }
        for (int j = 0; j < doublePos; j++) {
            b.put((byte) doubles[j]);
            b.putDouble(doubleVs[j]);
        }
        for (int j = 0; j < stringPos; j++) {
            b.put((byte) strings[j]);
            byte[] bytes = stringVs[j].getBytes();
            b.putByteArray(bytes, 0, bytes.length);
        }
        //
        int size = b.position() - 3;
        b.put(0, (byte) (size & 0x000000ff));
        b.put(1, (byte) ((size & 0x0000ff00) >> 8));
        b.put(2, (byte) ((size & 0x00ff0000) >> 16));
        //
        return b.flip();
    }
}
