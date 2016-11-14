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
    /// <summary>Timeframes where object may be shown</summary>
    /// <remarks>Used in ObjectSet() function to set OBJPROP_TIMEFRAMES property.</remarks>
    public enum ObjectVisibility
    {
        /// <summary>Object shown is only on 1-minute charts</summary>
        /// 
        OBJ_PERIOD_M1 = 1,

        /// <summary>Object shown is only on 5-minute charts</summary>
        /// 
        OBJ_PERIOD_M5 = 2,

        /// <summary>Object shown is only on 15-minute charts</summary>
        /// 
        OBJ_PERIOD_M15 = 4,

        /// <summary>Object shown is only on 30-minute charts</summary>
        /// 
        OBJ_PERIOD_M30 = 8,

        /// <summary>Object shown is only on 1-hour charts</summary>
        /// 
        OBJ_PERIOD_H1 = 16,

        /// <summary>Object shown is only on 4-hour charts</summary>
        /// 
        OBJ_PERIOD_H4 = 32,

        /// <summary>Object shown is only on daily charts</summary>
        /// 
        OBJ_PERIOD_D1 = 64,

        /// <summary>Object shown is only on weekly charts</summary>
        /// 
        OBJ_PERIOD_W1 = 128,

        /// <summary>Object shown is only on monthly charts</summary>
        /// 
        OBJ_PERIOD_MN1 = 256,

        /// <summary>Object shown is on all timeframes</summary>
        /// 
        OBJ_ALL_PERIODS = 511,

        /// <summary>Hidden object on all timeframes</summary>
        /// 
        OBJ_HIDDEN = -1
    }
}