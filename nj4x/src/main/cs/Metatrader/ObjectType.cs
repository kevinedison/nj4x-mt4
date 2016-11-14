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
    /// <summary>Object type identifier constants used with ObjectCreate(), ObjectsDeleteAll() and ObjectType() functions</summary>
    /// 
    public enum ObjectType
    {
        /// <summary>Vertical line</summary>
        /// <remarks>Uses time part of first coordinate.</remarks>
        OBJ_VLINE = 0,

        /// <summary>Horizontal line</summary>
        /// <remarks>Uses price part of first coordinate.</remarks>
        OBJ_HLINE = 1,

        /// <summary>Trend line</summary>
        /// <remarks>Uses 2 coordinates.</remarks>
        OBJ_TREND = 2,

        /// <summary>Trend by angle</summary>
        /// <remarks>Uses 1 coordinate. To set angle of line use ObjectSet() function.</remarks>
        OBJ_TRENDBYANGLE = 3,

        /// <summary>Regression</summary>
        /// <remarks>Uses time parts of first two coordinates.</remarks>
        OBJ_REGRESSION = 4,

        /// <summary>Channel</summary>
        /// <remarks>Uses 3 coordinates.</remarks>
        OBJ_CHANNEL = 5,

        /// <summary>Standard deviation channel</summary>
        /// <remarks>Uses time parts of first two coordinates.</remarks>
        OBJ_STDDEVCHANNEL = 6,

        /// <summary>Gann line</summary>
        /// <remarks>Uses 2 coordinate, but price part of second coordinate ignored.</remarks>
        OBJ_GANNLINE = 7,

        /// <summary>Gann fan</summary>
        /// <remarks>Uses 2 coordinate, but price part of second coordinate ignored.</remarks>
        OBJ_GANNFAN = 8,

        /// <summary>Gann grid</summary>
        /// <remarks>Uses 2 coordinate, but price part of second coordinate ignored.</remarks>
        OBJ_GANNGRID = 9,

        /// <summary>Fibonacci retracement</summary>
        /// <remarks>Uses 2 coordinates.</remarks>
        OBJ_FIBO = 10,

        /// <summary>Fibonacci time zones</summary>
        /// <remarks>Uses 2 coordinates.</remarks>
        OBJ_FIBOTIMES = 11,

        /// <summary>Fibonacci fan</summary>
        /// <remarks>Uses 2 coordinates.</remarks>
        OBJ_FIBOFAN = 12,

        /// <summary>Fibonacci arcs</summary>
        /// <remarks>Uses 2 coordinates.</remarks>
        OBJ_FIBOARC = 13,

        /// <summary>Fibonacci expansions</summary>
        /// <remarks>Uses 3 coordinates.</remarks>
        OBJ_EXPANSION = 14,

        /// <summary>Fibonacci channel</summary>
        /// <remarks>Uses 3 coordinates.</remarks>
        OBJ_FIBOCHANNEL = 15,

        /// <summary>Rectangle</summary>
        /// <remarks>Uses 2 coordinates.</remarks>
        OBJ_RECTANGLE = 16,

        /// <summary>Triangle</summary>
        /// <remarks>Uses 3 coordinates.</remarks>
        OBJ_TRIANGLE = 17,

        /// <summary>Ellipse</summary>
        /// <remarks>Uses 2 coordinates.</remarks>
        OBJ_ELLIPSE = 18,

        /// <summary>Andrews pitchfork</summary>
        /// <remarks>Uses 3 coordinates.</remarks>
        OBJ_PITCHFORK = 19,

        /// <summary>Cycles</summary>
        /// <remarks>Uses 2 coordinates.</remarks>
        OBJ_CYCLES = 20,

        /// <summary>Text</summary>
        /// <remarks>Uses 1 coordinate.</remarks>
        OBJ_TEXT = 21,

        /// <summary>Arrows</summary>
        /// <remarks>Uses 1 coordinate.</remarks>
        OBJ_ARROW = 22,

        /// <summary>Text label</summary>
        /// <remarks>Uses 1 coordinate in pixels.</remarks>
        OBJ_LABEL = 23
    }
}