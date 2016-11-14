using System;
using System.Collections;
using System.Text;

namespace nj4x.Metatrader
{
    /// <summary>
    /// Tick information.
    /// </summary>
    public class TickInfo
    {
        /// <summary>
        /// ASK price
        /// </summary>
        public double Ask;

        /// <summary>
        /// BID price.
        /// </summary>
        public double Bid;

        /// <summary>
        /// Long->Double dictionary of profits by order tickets.
        /// </summary>
        public Hashtable /*<Long, Double>*/ OrderPlMap;

        /// <summary>
        /// Time of the tick.
        /// </summary>
        public DateTime Time;

        /// <summary>
        /// Constructs empty tick info.
        /// </summary>
        public TickInfo()
        {
            Time = DateTime.MinValue;
        }

        internal TickInfo(DateTime time, double bid, double ask, Hashtable orderPlMap)
        {
            Time = time;
            Bid = bid;
            Ask = ask;
            OrderPlMap = orderPlMap;
        }

        /// <summary>
        /// Returns a <see cref="T:System.String"/> that represents the current <see cref="T:System.Object"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="T:System.String"/> that represents the current <see cref="T:System.Object"/>.
        /// </returns>
        /// <filterpriority>2</filterpriority>
        public override string ToString()
        {
            return "bid=" + Bid + ", ask=" + Ask + ", orders=" + DumpOrderPlMap(OrderPlMap);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="orderPlMap"></param>
        /// <returns></returns>
        public static string DumpOrderPlMap(Hashtable orderPlMap)
        {
            StringBuilder sb = new StringBuilder();
            foreach (var t in orderPlMap.Keys)
            {
                if (sb.Length > 0) sb.Append(", ");
                sb.Append(t).Append(':').Append(orderPlMap[t]);
            }
            return sb.ToString();
        }
    }

    /// <summary>
    /// Symbol's tick information.
    /// </summary>
    public class Tick
    {
        /// <summary>
        /// Symbol name.
        /// </summary>
        public string Symbol;

        /// <summary>
        /// Time of the tick.
        /// </summary>
        public DateTime Time;

        /// <summary>
        /// ASK price
        /// </summary>
        public double Ask;

        /// <summary>
        /// BID price.
        /// </summary>
        public double Bid;


        internal Tick(string symbol, DateTime time, double bid, double ask)
        {
            Symbol = symbol;
            Time = time;
            Bid = bid;
            Ask = ask;
        }

        /// <summary>
        /// Returns a <see cref="T:System.String"/> that represents the this tick.
        /// </summary>
        /// <returns>
        /// Returns a <see cref="T:System.String"/> that represents the this tick.
        /// </returns>
        /// <filterpriority>2</filterpriority>
        public override string ToString()
        {
            return $"symbol={Symbol} bid={Bid} ask={Ask} time={Time}";
        }
    }

    /// <summary>
    /// Symbol's tick information.
    /// </summary>
    public class BulkTick
    {
        /// <summary>
        /// Symbol name.
        /// </summary>
        public string Symbol;

        /// <summary>
        /// Time of the tick.
        /// </summary>
        public DateTime Time;

        /// <summary>
        /// ASK price
        /// </summary>
        public double Ask;

        /// <summary>
        /// BID price.
        /// </summary>
        public double Bid;

        /// <summary>
        /// Long->Double dictionary of profits by order tickets.
        /// </summary>
        public Hashtable /*<Long, Double>*/ OrderPlMap;

        internal BulkTick(string symbol, DateTime time, double bid, double ask, Hashtable orderPlMap)
        {
            Symbol = symbol;
            Time = time;
            Bid = bid;
            Ask = ask;
            OrderPlMap = orderPlMap;
        }

        /// <summary>
        /// Returns a <see cref="T:System.String"/> that represents the this tick.
        /// </summary>
        /// <returns>
        /// Returns a <see cref="T:System.String"/> that represents the this tick.
        /// </returns>
        /// <filterpriority>2</filterpriority>
        public override string ToString()
        {
            return $"symbol={Symbol} bid={Bid} ask={Ask} time={Time} orders={TickInfo.DumpOrderPlMap(OrderPlMap)}";
        }
    }
}