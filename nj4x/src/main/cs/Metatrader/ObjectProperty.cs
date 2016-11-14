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
    /// <summary>Object value index used with ObjectGet() and ObjectSet() functions</summary>
    /// 
    public enum ObjectProperty
    {
        /// <summary>datetime; Datetime value to set/get first coordinate time part</summary>
        /// 
// ReSharper disable InconsistentNaming
        OBJPROP_TIME1 = 0,

        /// <summary>double; Double value to set/get first coordinate price part</summary>
        /// 
        OBJPROP_PRICE1 = 1,

        /// <summary>datetime; Datetime value to set/get second coordinate time part</summary>
        /// 
        OBJPROP_TIME2 = 2,

        /// <summary>double; Double value to set/get second coordinate price part</summary>
        /// 
        OBJPROP_PRICE2 = 3,

        /// <summary>datetime; Datetime value to set/get third coordinate time part</summary>
        /// 
        OBJPROP_TIME3 = 4,

        /// <summary>double; Double value to set/get third coordinate price part</summary>
        /// 
        OBJPROP_PRICE3 = 5,

        /// <summary>color; Color value to set/get object color</summary>
        /// 
        OBJPROP_COLOR = 6,

        /// <summary>int/DrawingStyle; Value is one of STYLE_SOLID, STYLE_DASH, STYLE_DOT, STYLE_DASHDOT, STYLE_DASHDOTDOT constants to set/get object line style</summary>
        /// 
        OBJPROP_STYLE = 7,

        /// <summary>Integer value to set/get object line width</summary>
        /// <remarks>Can be from 1 to 5.</remarks>
        OBJPROP_WIDTH = 8,

        /// <summary>bool; Boolean value to set/get background drawing flag for object</summary>
        /// 
        OBJPROP_BACK = 9,

        /// <summary>bool; Boolean value to set/get ray flag of object</summary>
        /// 
        OBJPROP_RAY = 10,

        /// <summary>bool; Boolean value to set/get ellipse flag for fibo arcs</summary>
        /// 
        OBJPROP_ELLIPSE = 11,

        /// <summary>double; Double value to set/get scale object property</summary>
        /// 
        OBJPROP_SCALE = 12,

        /// <summary>double; Double value to set/get angle object property in degrees</summary>
        /// 
        OBJPROP_ANGLE = 13,

        /// <summary>int/ArrowCodes; Integer value or arrow enumeration to set/get arrow code object property</summary>
        /// 
        OBJPROP_ARROWCODE = 14,

        /// <summary>int; Value can be one or combination (bitwise addition) of object visibility constants to set/get timeframe object property</summary>
        /// 
        OBJPROP_TIMEFRAMES = 15,

        /// <summary>double; Double value to set/get deviation property for Standard deviation objects</summary>
        /// 
        OBJPROP_DEVIATION = 16,

        /// <summary>int; Integer value to set/get font size for text objects</summary>
        /// 
        OBJPROP_FONTSIZE = 100,

        /// <summary>int; Integer value to set/get anchor corner property for label objects</summary>
        /// <remarks>Must be from 0-3.</remarks>
        OBJPROP_CORNER = 101,

        /// <summary>int; Integer value to set/get anchor X distance object property in pixels</summary>
        /// 
        OBJPROP_XDISTANCE = 102,

        /// <summary>int; Integer value is to set/get anchor Y distance object property in pixels</summary>
        /// 
        OBJPROP_YDISTANCE = 103,

        /// <summary>int; Integer value to set/get Fibonacci object level count</summary>
        /// <remarks>Can be from 0 to 32.</remarks>
        OBJPROP_FIBOLEVELS = 200,

        /// <summary>color; Color value to set/get object level line color</summary>
        /// 
        OBJPROP_LEVELCOLOR = 201,

        /// <summary>int/DrawingStyle; Value is one of STYLE_SOLID, STYLE_DASH, STYLE_DOT, STYLE_DASHDOT, STYLE_DASHDOTDOT constants to set/get object level line style</summary>
        /// 
        OBJPROP_LEVELSTYLE = 202,

        /// <summary>int; Integer value to set/get object level line width</summary>
        /// <remarks>Can be from 1 to 5.</remarks>
        OBJPROP_LEVELWIDTH = 203,

        /// <summary>int, 210+n n in [0..31]; Fibonacci object level index, where n is level index to set/get</summary>
        /// <remarks>Can be from 0 to 31.</remarks>
        OBJPROP_FIRSTLEVEL = 210
// ReSharper restore InconsistentNaming
    }
}