using System;

namespace nj4x.Metatrader
{
    internal class DDParser
    {
        private readonly char end;
        private readonly String src;
        private readonly char start;
        private int ixEnd;
        private int ixStart;
        private String v;

        public DDParser(String src, char start, char end)
        {
            this.src = src;
            this.start = start;
            this.end = end;
            ixStart = src.IndexOf(start);
            ixEnd = src.IndexOf(end, ixStart + 1);
            v = src.Substring(ixStart + 1, ixEnd - ixStart - 1);
        }

        public String peek()
        {
            return v;
        }

        public String tail()
        {
            return ixEnd + 1 < src.Length ? src.Substring(ixEnd + 1) : null;
        }

        public String pop()
        {
            try
            {
                return v;
            }
            finally
            {
                if (ixEnd < src.Length)
                {
                    ixStart = src.IndexOf(start, ixEnd + 1);
                    if (ixStart > 0)
                    {
                        ixEnd = src.IndexOf(end, ixStart + 1);
                        v = src.Substring(ixStart + 1, ixEnd - ixStart - 1);
                    }
                }
            }
        }
    }
}