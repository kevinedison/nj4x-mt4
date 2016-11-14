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
 * The MessageBox function flags specify the contents and behavior of the dialog box. This value can be a combination of flags from the following groups of flags. To indicate the buttons displayed in the message box, specify one of the following values.
 */
public class MessageBoxFlag {

    /**
     * The message box contains one push button: OK. This is the default.
     */
    public final static MessageBoxFlag MB_OK = new MessageBoxFlag(0);
    public final static int _MB_OK = 0;
    /**
     * The message box contains two push buttons: OK and Cancel.
     */
    public final static MessageBoxFlag MB_OKCANCEL = new MessageBoxFlag(1);
    public final static int _MB_OKCANCEL = 1;
    /**
     * The message box contains three push buttons: Abort, Retry, and Ignore.
     */
    public final static MessageBoxFlag MB_ABORTRETRYIGNORE = new MessageBoxFlag(2);
    public final static int _MB_ABORTRETRYIGNORE = 2;
    /**
     * The message box contains three push buttons: Yes, No, and Cancel.
     */
    public final static MessageBoxFlag MB_YESNOCANCEL = new MessageBoxFlag(3);
    public final static int _MB_YESNOCANCEL = 3;
    /**
     * The message box contains two push buttons: Yes and No.
     */
    public final static MessageBoxFlag MB_YESNO = new MessageBoxFlag(4);
    public final static int _MB_YESNO = 4;
    /**
     * The message box contains two push buttons: Retry and Cancel.
     */
    public final static MessageBoxFlag MB_RETRYCANCEL = new MessageBoxFlag(5);
    public final static int _MB_RETRYCANCEL = 5;
    /**
     * Windows 2000: The message box contains three push buttons: Cancel, Try Again, Continue. Use this message box type instead of MB_ABORTRETRYIGNORE.
     */
    public final static MessageBoxFlag MB_CANCELTRYCONTINUE = new MessageBoxFlag(6);
    public final static int _MB_CANCELTRYCONTINUE = 6;
    /**
     * A stop-sign icon appears in the message box.
     */
    public final static MessageBoxFlag MB_ICONSTOP = new MessageBoxFlag(16);
    public final static int _MB_ICONSTOP = 16;
    /**
     * A question-mark icon appears in the message box.
     */
    public final static MessageBoxFlag MB_ICONQUESTION = new MessageBoxFlag(32);
    public final static int _MB_ICONQUESTION = 32;
    /**
     * An exclamation-point icon appears in the message box.
     */
    public final static MessageBoxFlag MB_ICONEXCLAMATION = new MessageBoxFlag(48);
    public final static int _MB_ICONEXCLAMATION = 48;
    /**
     * An icon consisting of a lowercase letter i in a circle appears in the message box.
     */
    public final static MessageBoxFlag MB_ICONINFORMATION = new MessageBoxFlag(64);
    public final static int _MB_ICONINFORMATION = 64;
    /**
     * The second button is the default button.
     */
    public final static MessageBoxFlag MB_DEFBUTTON2 = new MessageBoxFlag(256);
    public final static int _MB_DEFBUTTON2 = 256;
    /**
     * The third button is the default button.
     */
    public final static MessageBoxFlag MB_DEFBUTTON3 = new MessageBoxFlag(512);
    public final static int _MB_DEFBUTTON3 = 512;
    /**
     * The fourth button is the default button.
     */
    public final static MessageBoxFlag MB_DEFBUTTON4 = new MessageBoxFlag(768);
    public final static int _MB_DEFBUTTON4 = 768;
    public int val;
    private MessageBoxFlag(int val) {
        this.val = val;
    }
    public static MessageBoxFlag getMessageBoxFlag(int val) {
        switch (val) {

            case 0: return MB_OK;
            case 1: return MB_OKCANCEL;
            case 2: return MB_ABORTRETRYIGNORE;
            case 3: return MB_YESNOCANCEL;
            case 4: return MB_YESNO;
            case 5: return MB_RETRYCANCEL;
            case 6: return MB_CANCELTRYCONTINUE;
            case 16: return MB_ICONSTOP;
            case 32: return MB_ICONQUESTION;
            case 48: return MB_ICONEXCLAMATION;
            case 64: return MB_ICONINFORMATION;
            case 256: return MB_DEFBUTTON2;
            case 512: return MB_DEFBUTTON3;
            case 768: return MB_DEFBUTTON4;
            default: return null;
        }
    }
}
