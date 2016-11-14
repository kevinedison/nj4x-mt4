using System.IO;
using System.Text;

namespace nj4x.Metatrader
{
    public static class BasE91
    {
        private static readonly char[] enctab =
        {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '!', '#', '$',
            '%', '&', '(', ')', '*', '+', ',', '.', '/', ':', ';', '<', '=',
            '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~', '"'
        };

        private static readonly char[] dectab =
        {
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 62, (char) 90, (char) 63, (char) 64, (char) 65, (char) 66, (char) 91, (char) 67, (char) 68,
            (char) 69, (char) 70, (char) 71, (char) 91, (char) 72, (char) 73,
            (char) 52, (char) 53, (char) 54, (char) 55, (char) 56, (char) 57, (char) 58, (char) 59, (char) 60, (char) 61,
            (char) 74, (char) 75, (char) 76, (char) 77, (char) 78, (char) 79,
            (char) 80, (char) 0, (char) 1, (char) 2, (char) 3, (char) 4, (char) 5, (char) 6, (char) 7, (char) 8,
            (char) 9, (char) 10, (char) 11, (char) 12, (char) 13, (char) 14,
            (char) 15, (char) 16, (char) 17, (char) 18, (char) 19, (char) 20, (char) 21, (char) 22, (char) 23, (char) 24,
            (char) 25, (char) 81, (char) 91, (char) 82, (char) 83, (char) 84,
            (char) 85, (char) 26, (char) 27, (char) 28, (char) 29, (char) 30, (char) 31, (char) 32, (char) 33, (char) 34,
            (char) 35, (char) 36, (char) 37, (char) 38, (char) 39, (char) 40,
            (char) 41, (char) 42, (char) 43, (char) 44, (char) 45, (char) 46, (char) 47, (char) 48, (char) 49, (char) 50,
            (char) 51, (char) 86, (char) 87, (char) 88, (char) 89, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91,
            (char) 91, (char) 91, (char) 91, (char) 91, (char) 91, (char) 91
        };

        public static string BasE91Encode(this byte[] ib)
        {
            var ob = new StringBuilder();
            var len = ib.Length;
            //
            ulong queue = 0;
            int nbits = 0;
            int i = 0;

            while (len-- > 0)
            {
                queue |= (ulong) ib[i++] << nbits;
                nbits += 8;
                if (nbits > 13)
                {   /* enough bits in queue */
                    uint val = (uint) (queue & 8191);

                    if (val > 88)
                    {
                        queue >>= 13;
                        nbits -= 13;
                    }
                    else
                    {   /* we can take 14 bits */
                        val = (uint) (queue & 16383);
                        queue >>= 14;
                        nbits -= 14;
                    }
                    ob.Append(enctab[val % 91]);
                    ob.Append(enctab[val / 91]);
                }
            }
            if (nbits!=0)
            {
                ob.Append(enctab[queue % 91]);
                if (nbits > 7 || queue > 90)
                    ob.Append(enctab[queue / 91]);
            }

            return ob.ToString();
        }

        public static byte[] BasE91Decode(this string ib)
        {
            var ob = new MemoryStream();
            int i = 0;
            int len = ib.Length;
            ulong queue = 0;
            int nbits = 0;
            long val = -1;

            while (len-- > 0)
            {
                char d = dectab[ib[i++]];
                if (d == 91)
                    continue;   /* ignore non-alphabet chars */
                if (val == -1)
                    val = d; /* start next value */
                else
                {
                    val += d * 91;
                    queue |= (ulong) val << nbits;
                    nbits += (val & 8191) > 88 ? 13 : 14;
                    do
                    {
                        ob.WriteByte((byte)(queue&255));
                        queue >>= 8;
                        nbits -= 8;
                    } while (nbits > 7);
                    val = -1;    /* mark value complete */
                }
            }
            if (val != -1)
                ob.WriteByte((byte)((queue | ((ulong)val << nbits)) & 255));
            //
            return ob.ToArray();
        }
    }
}