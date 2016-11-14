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
    /// <summary>Drawing shape style enumeration for SetIndexStyle() function</summary>
    /// 
    public enum DrawingShape
    {
        /// <summary>Drawing line</summary>
        /// 
        DRAW_LINE = 0,

        /// <summary>Drawing sections</summary>
        /// 
        DRAW_SECTION = 1,

        /// <summary>Drawing histogram</summary>
        /// 
        DRAW_HISTOGRAM = 2,

        /// <summary>Drawing arrows (symbols)</summary>
        /// 
        DRAW_ARROW = 3,

        /// <summary>Drawing sections between even and odd indicator buffers</summary>
        /// 
        DRAW_ZIGZAG = 4,

        /// <summary>No drawing</summary>
        /// 
        DRAW_NONE = 12
    }
}