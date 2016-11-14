using System.Collections;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using NLog;

namespace nj4x.Metatrader
{
    internal class PositionImpl : IPositionInfoEnabled
    {
        private static readonly Logger Logger = LogManager.GetCurrentClassLogger();
        private int _hCount;
        private List<IOrderInfo> _notResolved;
        private int _tCount;
        private string _str;

        public override string ToString()
        {
            return _str;
        }

        private string AsString()
        {
            return $"Pos(t={_tCount} h={_hCount} toResolve={_notResolved?.Count})";
        }

        public PositionImpl(string positionEncoded, MT4 utils)
        {
            var bp = new DDParser(positionEncoded, MT4.ArgStartC, MT4.ArgEndC);
            var p = new SDParser(bp.pop(), '|');
            _tCount = int.Parse(p.pop(), CultureInfo.InvariantCulture);
            _hCount = int.Parse(p.pop(), CultureInfo.InvariantCulture);
            _str = AsString();
            //
            var sz = int.Parse(bp.pop(), CultureInfo.InvariantCulture);
            LiveOrders = new Dictionary<long, IOrderInfo>((int) (sz*1.2));
            for (var i = 0; i < sz; i++)
            {
                var oi = new OrderImpl(bp.pop(), utils);
                LiveOrders.Add(oi.TicketNumber, oi);
            }
            sz = int.Parse(bp.pop(), CultureInfo.InvariantCulture);
            HistoricalOrders = new Dictionary<long, IOrderInfo>((int) (sz*1.2));
            for (var i = 0; i < sz; i++)
            {
                var oi = new OrderImpl(bp.pop(), utils);
                HistoricalOrders.Add(oi.TicketNumber, oi);
            }
        }

        public int GetTCount()
        {
            return _tCount;
        }

        public int GetHCount()
        {
            return _hCount;
        }

        #region IPositionInfo Members

        public IPositionChangeInfo MergePosition(IPositionInfo positionInfo)
        {
            if (positionInfo == null)
            {
                PositionChangeImpl.AdjustPartiallyClosedOrders(
                    LiveOrders.Values.ToList(),
                    HistoricalOrders.Values.ToList(),
                    out _notResolved
                    );
                //
                // todo: load some more history if _notResolved > 0
                _notResolved.Clear();
                return new PositionChangeImpl();
            }
            var newPositionInfo = (PositionImpl) positionInfo;
            _tCount = newPositionInfo._tCount;
            _hCount = newPositionInfo._hCount;
            //
            Dictionary<long, IOrderInfo> liveOrders = new Dictionary<long, IOrderInfo>(LiveOrders);
            Dictionary<long, IOrderInfo> historicalOrders = new Dictionary<long, IOrderInfo>(HistoricalOrders);
            //
            var pc = new PositionChangeImpl();
            var newLiveOrders = newPositionInfo.LiveOrders;
            foreach (var o in newLiveOrders.Values)
            {
                var ticket = o.Ticket();
                IOrderInfo orderInfo;
                if (liveOrders.TryGetValue(o.Ticket(), out orderInfo))
                {
                    if (o.IsModified())
                    {
                        pc.ModifiedOrders.Add(orderInfo.Merge(o));
                    }
                }
                else
                {
                    pc.AddNewOrder(o);
                    liveOrders.Add(ticket, o);
                }
            }
            foreach (var o in newPositionInfo.HistoricalOrders.Values)
            {
//                object ticket = new Integer(o.GetTicket());
                var ticket = o.Ticket();
                //IOrderInfo orderInfo = liveOrders.Remove(ticket);
                IOrderInfo orderInfo;
                if (liveOrders.TryGetValue(ticket, out orderInfo))
                {
                    liveOrders.Remove(ticket);
                    pc.AddClosedOrDeletedOrder(orderInfo.Merge(o));
                    historicalOrders.Add(ticket, orderInfo);
                }
                else
                {
                    IOrderInfo ho;
                    if (historicalOrders.TryGetValue(ticket, out ho))
                    {
                        ho.Merge(o);
                    }
                    else
                    {
                        // ?? we've missed some order ??
                        pc.AddClosedOrDeletedOrder(o);
                        historicalOrders.Add(ticket, o);
                    }
                }
            }
            //
            foreach (var o in
                from IOrderInfo o in liveOrders.Values
                where !newLiveOrders.ContainsKey(o.Ticket())
                select o)
            {
                liveOrders.Remove(o.Ticket());
                pc.AddClosedOrDeletedOrder(o);
            }
            //
            LiveOrders = liveOrders;
            HistoricalOrders = historicalOrders;
            //
            if (_notResolved != null)
            {
                foreach (var o in _notResolved)
                {
                    Logger.Warn("PARTIALLY CLOSED order   to resolve: " + o);
                    pc.AddClosedOrDeletedOrder(o);
                }
            }
            _notResolved = pc.AdjustPartiallyClosedOrders();
            _str = AsString();
            //
            return pc;
        }

        public Hashtable GetLiveOrders()
        {
            return new Hashtable(LiveOrders);
        }

        public Dictionary<long, IOrderInfo> LiveOrders { get; private set; }

        public Hashtable GetHistoricalOrders()
        {
            return new Hashtable(HistoricalOrders);
        }

        public Dictionary<long, IOrderInfo> HistoricalOrders { get; private set; }

        #endregion
    }
}