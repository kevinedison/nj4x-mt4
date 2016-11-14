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
public class ArrowCodes {

    /**
     * Thumb up symbol.
     */
    public final static ArrowCodes SYMBOL_THUMBSUP = new ArrowCodes(67);
    public final static int _SYMBOL_THUMBSUP = 67;
    /**
     * Thumb down symbol.
     */
    public final static ArrowCodes SYMBOL_THUMBSDOWN = new ArrowCodes(68);
    public final static int _SYMBOL_THUMBSDOWN = 68;
    /**
     * Arrow up symbol.
     */
    public final static ArrowCodes SYMBOL_ARROWUP = new ArrowCodes(241);
    public final static int _SYMBOL_ARROWUP = 241;
    /**
     * Arrow down symbol.
     */
    public final static ArrowCodes SYMBOL_ARROWDOWN = new ArrowCodes(242);
    public final static int _SYMBOL_ARROWDOWN = 242;
    /**
     * Stop sign symbol
     */
    public final static ArrowCodes SYMBOL_STOPSIGN = new ArrowCodes(251);
    public final static int _SYMBOL_STOPSIGN = 251;
    /**
     * Check sign symbol.
     */
    public final static ArrowCodes SYMBOL_CHECKSIGN = new ArrowCodes(252);
    public final static int _SYMBOL_CHECKSIGN = 252;
    public int val;
    private ArrowCodes(int val) {
        this.val = val;
    }
    public static ArrowCodes getArrowCodes(int val) {
        switch (val) {

            case 67: return SYMBOL_THUMBSUP;
            case 68: return SYMBOL_THUMBSDOWN;
            case 241: return SYMBOL_ARROWUP;
            case 242: return SYMBOL_ARROWDOWN;
            case 251: return SYMBOL_STOPSIGN;
            case 252: return SYMBOL_CHECKSIGN;
            default: return null;
        }
    }
}
