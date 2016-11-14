// Copyright (c) 2008-2014 by Gerasimenko Roman
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 
// 1. Redistribution of source code must retain the above copyright
//     notice, this list of conditions and the following disclaimer.
// 
// 2. Redistribution in binary form must reproduce the above copyright
//     notice, this list of conditions and the following disclaimer in
//     the documentation and/or other materials provided with the
//     distribution.
// 
// 3. The name "NJ4X" must not be used to endorse or promote
//     products derived from this software without prior written
//     permission.
//     For written permission, please contact roman.gerasimenko@nj4x.com
// 
// 4. Products derived from this software may not be called "NJ4X",
//     nor may "NJ4X" appear in their name, without prior written
//     permission of Gerasimenko Roman.
// 
// THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
// OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED.  IN NO EVENT SHALL THE JFX CONTRIBUTORS
// BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
// USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.

namespace nj4x.Metatrader
{
    /// <summary>The MessageBox function flags specify the contents and behavior of the dialog box</summary>
    /// <remarks>This value can be a combination of flags from the following groups of flags. To indicate the buttons displayed in the message box, specify one of the following values.</remarks>
    public enum MessageBoxFlag
    {
        /// <summary>The message box contains one push button: OK</summary>
        /// <remarks>This is the default.</remarks>
        MB_OK = 0,

        /// <summary>The message box contains two push buttons: OK and Cancel</summary>
        /// 
        MB_OKCANCEL = 1,

        /// <summary>The message box contains three push buttons: Abort, Retry, and Ignore</summary>
        /// 
        MB_ABORTRETRYIGNORE = 2,

        /// <summary>The message box contains three push buttons: Yes, No, and Cancel</summary>
        /// 
        MB_YESNOCANCEL = 3,

        /// <summary>The message box contains two push buttons: Yes and No</summary>
        /// 
        MB_YESNO = 4,

        /// <summary>The message box contains two push buttons: Retry and Cancel</summary>
        /// 
        MB_RETRYCANCEL = 5,

        /// <summary>Windows 2000: The message box contains three push buttons: Cancel, Try Again, Continue</summary>
        /// <remarks>Use this message box type instead of MB_ABORTRETRYIGNORE.</remarks>
        MB_CANCELTRYCONTINUE = 6,

        /// <summary>A stop-sign icon appears in the message box</summary>
        /// 
        MB_ICONSTOP = 16,

        /// <summary>A question-mark icon appears in the message box</summary>
        /// 
        MB_ICONQUESTION = 32,

        /// <summary>An exclamation-point icon appears in the message box</summary>
        /// 
        MB_ICONEXCLAMATION = 48,

        /// <summary>An icon consisting of a lowercase letter i in a circle appears in the message box</summary>
        /// 
        MB_ICONINFORMATION = 64,

        /// <summary>The second button is the default button</summary>
        /// 
        MB_DEFBUTTON2 = 256,

        /// <summary>The third button is the default button</summary>
        /// 
        MB_DEFBUTTON3 = 512,

        /// <summary>The fourth button is the default button</summary>
        /// 
        MB_DEFBUTTON4 = 768
    }
}