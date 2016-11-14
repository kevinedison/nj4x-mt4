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
 * The MessageBox() function return codes. If a message box has a Cancel button, the function returns the IDCANCEL value if either the ESC key is pressed or the Cancel button is selected. If the message box has no Cancel button, pressing ESC has no effect.
 */
public class MessageBoxID {

    /**
     * OK button was selected.
     */
    public final static MessageBoxID IDOK = new MessageBoxID(1);
    public final static int _IDOK = 1;
    /**
     * Cancel button was selected.
     */
    public final static MessageBoxID IDCANCEL  = new MessageBoxID(2);
    public final static int _IDCANCEL  = 2;
    /**
     * Abort button was selected.
     */
    public final static MessageBoxID IDABORT  = new MessageBoxID(3);
    public final static int _IDABORT  = 3;
    /**
     * Retry button was selected.
     */
    public final static MessageBoxID IDRETRY  = new MessageBoxID(4);
    public final static int _IDRETRY  = 4;
    /**
     * Ignore button was selected.
     */
    public final static MessageBoxID IDIGNORE  = new MessageBoxID(5);
    public final static int _IDIGNORE  = 5;
    /**
     * Yes button was selected.
     */
    public final static MessageBoxID IDYES  = new MessageBoxID(6);
    public final static int _IDYES  = 6;
    /**
     * No button was selected.
     */
    public final static MessageBoxID IDNO  = new MessageBoxID(7);
    public final static int _IDNO  = 7;
    /**
     * Try Again button was selected.
     */
    public final static MessageBoxID IDTRYAGAIN  = new MessageBoxID(10);
    public final static int _IDTRYAGAIN  = 10;
    /**
     * Continue button was selected.
     */
    public final static MessageBoxID IDCONTINUE  = new MessageBoxID(11);
    public final static int _IDCONTINUE  = 11;
    public int val;
    private MessageBoxID(int val) {
        this.val = val;
    }
    public static MessageBoxID getMessageBoxID(int val) {
        switch (val) {

            case 1: return IDOK;
            case 2: return IDCANCEL ;
            case 3: return IDABORT ;
            case 4: return IDRETRY ;
            case 5: return IDIGNORE ;
            case 6: return IDYES ;
            case 7: return IDNO ;
            case 10: return IDTRYAGAIN ;
            case 11: return IDCONTINUE ;
            default: return null;
        }
    }
}
