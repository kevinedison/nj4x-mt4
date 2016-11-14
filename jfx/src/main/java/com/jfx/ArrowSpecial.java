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

package com.jfx;

/**
 * Predefined Arrow codes enumeration. Arrows code constants.
 */
public class ArrowSpecial {

    /**
     * Upwards arrow with tip rightwards
     */
    public final static ArrowSpecial SYMBOL_UP_RIGHT = new ArrowSpecial(1);
    public final static int _SYMBOL_UP_RIGHT = 1;
    /**
     * Downwards arrow with tip rightwards
     */
    public final static ArrowSpecial SYMBOL_DOWN_RIGHT = new ArrowSpecial(2);
    public final static int _SYMBOL_DOWN_RIGHT = 2;
    /**
     * Left pointing triangle
     */
    public final static ArrowSpecial SYMBOL_TRIANGLE = new ArrowSpecial(3);
    public final static int _SYMBOL_TRIANGLE = 3;
    /**
     * En Dash symbol
     */
    public final static ArrowSpecial SYMBOL_DASH = new ArrowSpecial(4);
    public final static int _SYMBOL_DASH = 4;
    /**
     * Left sided price label.
     */
    public final static ArrowSpecial SYMBOL_LEFTPRICE = new ArrowSpecial(5);
    public final static int _SYMBOL_LEFTPRICE = 5;
    /**
     * Right sided price label.
     */
    public final static ArrowSpecial SYMBOL_RIGHTPRICE = new ArrowSpecial(6);
    public final static int _SYMBOL_RIGHTPRICE = 6;
    public int val;
    private ArrowSpecial(int val) {
        this.val = val;
    }
    public static ArrowSpecial getArrowSpecial(int val) {
        switch (val) {

            case 1: return SYMBOL_UP_RIGHT;
            case 2: return SYMBOL_DOWN_RIGHT;
            case 3: return SYMBOL_TRIANGLE;
            case 4: return SYMBOL_DASH;
            case 5: return SYMBOL_LEFTPRICE;
            case 6: return SYMBOL_RIGHTPRICE;
            default: return null;
        }
    }
}
