using System;
using System.Globalization;

namespace nj4x.Metatrader
{
    internal class SDParser
    {
        private readonly char delimiter;
        private readonly String src;
        private int ix;
        private String v;

        public SDParser(String src, char delimiter)
        {
            this.src = src;
            this.delimiter = delimiter;
            ix = src.IndexOf(delimiter);
            ix = ix < 0 ? src.Length : ix;
            v = src.Substring(0, ix);
        }

        public String peek()
        {
            return v;
        }

        public bool popBoolean()
        {
            String result = pop();
            return result.Length > 1 ? bool.Parse(result) : (int.Parse(result) != 0);
        }

        public int popInt()
        {
            String result = pop();
            return int.Parse(result);
        }

        public long popLong()
        {
            String result = pop();
            return long.Parse(result);
        }

        public double popDouble()
        {
            String result = pop();
            return double.Parse(result, CultureInfo.InvariantCulture);
        }

        public String pop()
        {
            try
            {
                return v;
            }
            finally
            {
                if (ix < src.Length)
                {
                    int ix2 = src.IndexOf(delimiter, ix + 1);
                    ix2 = ix2 < 0 ? src.Length : ix2;
                    v = src.Substring(ix + 1, ix2 - ix - 1);
                    ix = ix2;
                } else
                {
                    v = null;
                }
            }
        }
    }
}