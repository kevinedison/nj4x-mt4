using System;
using System.Collections.Generic;
using System.Globalization;

namespace nj4x.Metatrader
{
    /// <summary>
    ///     This structure stores information about the prices, volumes and spread.
    /// </summary>
    public class Rate
    {
        /// <summary>
        ///     Close price
        /// </summary>
        public double Close;

        /// <summary>
        ///     The highest price of the period
        /// </summary>
        public double High;

        /// <summary>
        ///     The lowest price of the period
        /// </summary>
        public double Low;

        /// <summary>
        ///     Open price
        /// </summary>
        public double Open;

        /// <summary>
        ///     Trade volume
        /// </summary>
        public long RealVolume;

        /// <summary>
        ///     Spread
        /// </summary>
        public int Spread;

        /// <summary>
        ///     Tick volume
        /// </summary>
        public long TickVolume;

        /// <summary>
        ///     Period start time
        /// </summary>
        public DateTime Time;

        internal Rate(double bid)
        {
            Time = DateTime.Now;
            Open = High = Low = Close = bid;
            TickVolume = 0;
            Spread = 0;
            RealVolume = 0;
        }

        internal Rate(string encodedRate, MT4 utils)
        {
            var p = new SDParser(encodedRate, '|');
            Time = utils.ToDate(int.Parse(p.pop(), CultureInfo.InvariantCulture));
            Open = double.Parse(p.pop(), CultureInfo.InvariantCulture);
            High = double.Parse(p.pop(), CultureInfo.InvariantCulture);
            Low = double.Parse(p.pop(), CultureInfo.InvariantCulture);
            Close = double.Parse(p.pop(), CultureInfo.InvariantCulture);
            TickVolume = long.Parse(p.pop());
            Spread = int.Parse(p.pop(), CultureInfo.InvariantCulture);
            RealVolume = long.Parse(p.pop());
        }

        internal static List<Rate> Decode(string ratesEncoded, MT4 utils, out double bid, out double ask)
        {
            var bp = new DDParser(ratesEncoded, MT4.ArgStartC, MT4.ArgEndC);
            var sz = int.Parse(bp.pop(), CultureInfo.InvariantCulture);
            var rates = new List<Rate>(sz);
            for (var i = 0; i < sz; i++)
            {
                var oi = new Rate(bp.pop(), utils);
                rates.Add(oi);
            }
            var tail = bp.tail();
            if (string.IsNullOrEmpty(tail))
            {
                bid = ask = 0;
            }
            else
            {
                var p = new SDParser(tail, '|');
                bid = p.popDouble();
                ask = p.popDouble();
            }

            return rates;
        }
    }

    /// <summary>
    ///     This structure stores OHLC information, time plus current bid/ask prices.
    /// </summary>
    public class Bar
    {
        /// <summary>
        ///     Current Ask price.
        /// </summary>
        public double Ask;

        /// <summary>
        ///     Current Bid price.
        /// </summary>
        public double Bid;

        /// <summary>
        ///     Close price
        /// </summary>
        public double Close;

        /// <summary>
        ///     The highest price of the period
        /// </summary>
        public double High;

        /// <summary>
        ///     The lowest price of the period
        /// </summary>
        public double Low;

        /// <summary>
        ///     Open price
        /// </summary>
        public double Open;

        /// <summary>
        ///     Period start time
        /// </summary>
        public DateTime Time;

        internal Bar(Rate r, double bid, double ask)
        {
            Time = r.Time;
            Open = r.Open;
            High = r.High;
            Low = r.Low;
            Close = r.Close;
            Bid = bid;
            Ask = ask;
        }
    }
}