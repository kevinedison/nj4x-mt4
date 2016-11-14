using System;
using System.Collections.Generic;
using System.Linq;
using NLog;

// ReSharper disable CompareOfFloatsByEqualityOperator
namespace nj4x.Metatrader
{
    internal class PositionChangeImpl : IPositionChangeInfo
    {
        private static readonly Logger Logger = LogManager.GetCurrentClassLogger();
        //
        public List<IOrderInfo> ClosedOrders;
        public List<IOrderInfo> DeletedOrders;
        public List<IOrderInfo> ModifiedOrders;
        public List<IOrderInfo> NewOrders;
        private string _str;

        public PositionChangeImpl()
        {
            DeletedOrders = new List<IOrderInfo>();
            ClosedOrders = new List<IOrderInfo>();
            NewOrders = new List<IOrderInfo>();
            ModifiedOrders = new List<IOrderInfo>();
        }

        public override string ToString()
        {
            return _str;
        }

        private string AsString()
        {
            return $"PosChange(C={ClosedOrders?.Count} D={DeletedOrders?.Count} M={ModifiedOrders?.Count} N={NewOrders?.Count})";
        }

        private bool isClosed(IOrderInfo o)
        {
            return o.GetTradeOperation() == TradeOperation.OP_BUY || o.GetTradeOperation() == TradeOperation.OP_SELL;
        }

        internal void AddClosedOrDeletedOrder(IOrderInfo o)
        {
            if (isClosed(o))
            {
                ClosedOrders.Add(o);
            }
            else
            {
                DeletedOrders.Add(o);
            }
        }

        internal void AddNewOrder(IOrderInfo o)
        {
            o.SetWasEverLiveOrder(true);
            NewOrders.Add(o);
        }

        public List<IOrderInfo> AdjustPartiallyClosedOrders()
        {
            List<IOrderInfo> toResolve;
            AdjustPartiallyClosedOrders(NewOrders, ClosedOrders, out toResolve);
            foreach (var orderInfo in toResolve)
            {
                Logger.Warn("PARTIALLY CLOSED order not resolved: " + orderInfo);
                ClosedOrders.Remove(orderInfo);
            }
            _str = AsString();
            return toResolve;
        }

        private class PartiallyClosedOrdersAdjuster
        {
            private readonly List<IOrderInfo> _live;
            private readonly List<IOrderInfo> _history;
            private readonly List<IOrderInfo> _toResolve;
            private readonly List<Stack<IOrderInfo>> _buySide;
            private readonly List<Stack<IOrderInfo>> _sellSide;
            private readonly Dictionary<long, bool> _buyAtTop;
            private readonly Dictionary<long, bool> _sellAtTop;
            private readonly Dictionary<long, long> _hedgedBy;
            private readonly Dictionary<long, long> _closedBy;
            private readonly Dictionary<long, IOrderInfo> _orders;

            public PartiallyClosedOrdersAdjuster(List<IOrderInfo> live, List<IOrderInfo> history)
            {
                _live = live;
                _history = history;
                _toResolve = new List<IOrderInfo>();
                _history.Sort((o1, o2) =>
                {
                    try
                    {
                        var cmp = o1.GetCloseTime().CompareTo(o2.GetCloseTime());
                        if (cmp == 0)
                        {
                            cmp = string.Compare(o1.GetSymbol(), o2.GetSymbol(), StringComparison.Ordinal);
                            if (cmp == 0)
                            {
                                cmp = o1.GetTradeOperation() - o2.GetTradeOperation();
                                if (cmp == 0)
                                {
                                    var d = o1.GetOpenPrice() - o2.GetOpenPrice();
                                    cmp = d == 0 ? 0 : (d > 0 ? 1 : -1);
                                    if (cmp == 0)
                                    {
                                        var t = o1.GetOpenTime() - o2.GetOpenTime();
                                        cmp = t.TotalSeconds == 0 ? 0 : (t.TotalSeconds > 0 ? 1 : -1);
                                        if (cmp == 0)
                                        {
                                            cmp = o1.GetMagic() - o2.GetMagic();
                                            if (cmp == 0)
                                            {
                                                var t1 = o1.Ticket();
                                                var t2 = o2.Ticket();
                                                cmp = t1 > t2 ? -1 : (t1 < t2 ? 1 : 0); // reverse order for stack
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        return cmp;
                    }
                    catch (Exception e)
                    {
                        Console.WriteLine(e);
                        return 0;
                    }
                });
                _live.Sort((o1, o2) => o1.Ticket() > o2.Ticket() ? 1 : -1);
                //
                var notBuySellOrders = new List<IOrderInfo>();
                _orders = _live.ToDictionary(info => info.Ticket());
                _buySide = new List<Stack<IOrderInfo>>();
                _sellSide = new List<Stack<IOrderInfo>>();
                _buyAtTop = new Dictionary<long, bool>();
                _sellAtTop = new Dictionary<long, bool>();
                _hedgedBy = new Dictionary<long, long>();
                _closedBy = new Dictionary<long, long>();
                OComparator lastOrderCmpInfo = null;
                var closeHedgeBy = @"close hedge by #";
                foreach (var o in history)
                {
                    _orders[o.Ticket()] = o;
                    var tradeOperation = o.GetTradeOperation();
                    if (tradeOperation != TradeOperation.OP_BUY && tradeOperation != TradeOperation.OP_SELL)
                    {
                        notBuySellOrders.Add(o);
                        continue;
                    }
                    //
                    if (o.GetComment().StartsWith(closeHedgeBy))
                    {
                        long t;
                        if (long.TryParse(o.GetComment().Substring(closeHedgeBy.Length), out t))
                        {
                            _hedgedBy[o.Ticket()] = t;
                            _closedBy[t] = o.Ticket();
                        }
                        else
                        {
                            Logger.Warn($"Not expected, not numeric ticket in comment: {o.GetComment()}");
                        }
                    }
                    //
                    var side = tradeOperation == TradeOperation.OP_BUY ? _buySide : _sellSide;
                    var top = tradeOperation == TradeOperation.OP_BUY ? _buyAtTop : _sellAtTop;
                    //
                    if (lastOrderCmpInfo == null || !lastOrderCmpInfo.IsSameOrder(o))
                    {
                        lastOrderCmpInfo = new OComparator(o);
                        var stack = new Stack<IOrderInfo>();
                        stack.Push(o);
                        side.Add(stack);
                    }
                    else
                    {
                        var stack = side[side.Count - 1];
                        top.Remove(stack.Peek().Ticket());
                        stack.Push(o);
                    }
                    top[o.Ticket()] = true;
                }
                //
                history.Clear();//rebuilding historical orders
                history.AddRange(notBuySellOrders);
            }

            public List<IOrderInfo> Adjust()
            {
                bool hedgesFound;
                do
                {
                    hedgesFound = false;
                    var bIx = 0;
                    while (bIx < _buySide.Count)
                    {
                        if (CheckHedges(_buySide[bIx]))
                        {
                            hedgesFound = true;
                            continue; // continue walking BUY side
                        }
                        //
                        bIx++;
                    }
                } while (hedgesFound);
                //
                // flatten buy&sell sides
                var history = _buySide.SelectMany(infos => infos.ToArray()).ToList();
                history.AddRange(_sellSide.SelectMany(infos => infos.ToArray()));
                history.Sort((o1, o2) => o1.Ticket() > o2.Ticket() ? 1 : -1);
                //
                // process from#/to# orders
                foreach (var orderInfo in history)
                {
                    var prevTicket = PrevTicketFromComment(orderInfo);
                    var nextTicket = NextTicketFromComment(orderInfo);
                    IOrderInfo prev, next;
                    _orders.TryGetValue(prevTicket, out prev);
                    _orders.TryGetValue(nextTicket, out next);
                    if (prev != null)
                    {
                        orderInfo.SetPrev(prev);
                    }
                    if (next != null)
                    {
                        orderInfo.SetNext(next);
                    }
                    if (prevTicket != 0 && prev == null 
                        || nextTicket != 0 && next == null)
                    {
                        OrderImpl oi = orderInfo as OrderImpl;
                        if (oi != null)
                        {
                            if (oi._prev == 0 && prevTicket != 0)
                            {
                                oi._prev = prevTicket;
                            }
                            if (oi._next == 0 && nextTicket != 0)
                            {
                                oi._next = nextTicket;
                            }
                        }
                    }
                }
                _toResolve.AddRange(history.Where(
                    order => order.GetComment().Equals(@"partial close") && order.GetPrev() == 0));
                _history.AddRange(history.Where(
                    order => !order.GetComment().Equals(@"partial close") || order.GetPrev() != 0));
                //
                foreach (var orderInfo in _live)
                {
                    OrderImpl oi = orderInfo as OrderImpl;
                    if (oi != null && oi._prev == 0)
                    {
                        oi._prev = PrevTicketFromComment(orderInfo);
                    }
                }
                //
                foreach (var order in _history)
                {
                    order.LotsBeforeClose = order.LotsClosed = Lots(order);
                    var o = order;
                    IOrderInfo next;
                    while (_orders.TryGetValue(o.GetNext(), out next))
                    {
                        order.LotsBeforeClose += Lots(next);
                        o = next;
                    }
                }
                //
                return _toResolve;
            }

            private double Lots(IOrderInfo order)
            {
                double lots = 0;
                if (order.GetLots() == 0)
                {
                    IOrderInfo cb;
                    if (_orders.TryGetValue(order.GetClosedHedgeOf(), out cb))
                    {
                        lots = cb.GetLots();
                    }
                }
                else
                {
                    lots = order.GetLots();
                }
                return lots;
            }

            private long PrevTicketFromComment(IOrderInfo o)
            {
                var fromComment = @"from #";
                long t;
                if (o.GetComment().StartsWith(fromComment) &&
                    long.TryParse(o.GetComment().Substring(fromComment.Length), out t))
                    return t;
                return 0;
            }

            private long NextTicketFromComment(IOrderInfo o)
            {
                var toComment = @"to #";
                long t;
                if (o.GetComment().StartsWith(toComment) &&
                    long.TryParse(o.GetComment().Substring(toComment.Length), out t))
                    return t;
                return 0;
            }

            private bool CheckHedges(Stack<IOrderInfo> bStack)
            {
                Stack<IOrderInfo> cbStack = null;
                Stack<IOrderInfo> hdgStack = null;
                long tmpTicket;
                if (_closedBy.TryGetValue(bStack.Peek().Ticket(), out tmpTicket))
                {
                    cbStack = bStack;
                    if (_sellAtTop.ContainsKey(tmpTicket))
                    {
                        hdgStack = _sellSide.FirstOrDefault(sellStack => sellStack.Peek().Ticket() == tmpTicket);
                    }
                }
                if (_hedgedBy.TryGetValue(bStack.Peek().Ticket(), out tmpTicket))
                {
                    hdgStack = bStack;
                    if (_sellAtTop.ContainsKey(tmpTicket))
                    {
                        cbStack = _sellSide.FirstOrDefault(sellStack => sellStack.Peek().Ticket() == tmpTicket);
                    }
                }
                if (cbStack != null && hdgStack != null)
                {
                    var cbOrder = cbStack.Pop();
                    var hdgOrder = hdgStack.Pop();
                    //
                    _history.Add(hdgOrder);
                    _history.Add(cbOrder);
                    //
                    IOrderInfo cbNext = null;
                    IOrderInfo hdgNext = null;
                    //
                    Dictionary<long, bool> cbTop = cbOrder.GetTradeOperation() == TradeOperation.OP_BUY ? _buyAtTop : _sellAtTop;
                    Dictionary<long, bool> hdgTop = hdgOrder.GetTradeOperation() == TradeOperation.OP_BUY ? _buyAtTop : _sellAtTop;
                    cbTop.Remove(cbOrder.Ticket());
                    hdgTop.Remove(hdgOrder.Ticket());
                    if (cbStack.Count > 0) cbTop[(cbNext = cbStack.Peek()).Ticket()] = true;
                    if (hdgStack.Count > 0) hdgTop[(hdgNext = hdgStack.Peek()).Ticket()] = true;
                    //
                    cbOrder.SetClosedBy(hdgOrder);
                    if (cbNext == null && hdgNext == null)
                    {
                        //check live orders for next
                        var cbC = new OComparator(cbOrder);
                        var hdgC = new OComparator(hdgOrder);
                        cbNext =
                            _live.FirstOrDefault(
                                order => order.Ticket() > cbOrder.Ticket() && cbC.IsSameLiveOrder(order));
                        hdgNext =
                            _live.FirstOrDefault(
                                order => order.Ticket() > hdgOrder.Ticket() && hdgC.IsSameLiveOrder(order));
                    }
                    var next = cbNext == null
                        ? hdgNext
                        : (hdgNext == null
                            ? cbNext
                            : (cbNext.Ticket() > hdgNext.Ticket() ? hdgNext : cbNext)); // use minimal ticket number for next order
                    if (next != null)
                    {
                        if (ReferenceEquals(next, cbNext))
                        {
                            cbOrder.SetNext(next);
                        }
                        if (ReferenceEquals(next, hdgNext))
                        {
                            hdgOrder.SetNext(next);
                        }
                    }
                    //
                    // remove empty stack from a side
                    if (cbStack.Count == 0)
                    {
                        (cbOrder.GetTradeOperation() == TradeOperation.OP_BUY ? _buySide : _sellSide).Remove(cbStack);
                    }
                    if (hdgStack.Count == 0)
                    {
                        (hdgOrder.GetTradeOperation() == TradeOperation.OP_BUY ? _buySide : _sellSide).Remove(hdgStack);
                    }
                    //
                    return true;
                }
                return false;
            }
        }

        private class OComparator
        {
            private readonly DateTime _closeTime;
            private readonly string _symbol;
            private readonly TradeOperation _tradeOp;
            private readonly double _price;
            private readonly DateTime _time;
            private readonly int _magic;

            public OComparator(IOrderInfo o)
            {
                _closeTime = o.GetCloseTime();
                _symbol = o.GetSymbol();
                _tradeOp = o.GetTradeOperation();
                _price = o.GetOpenPrice();
                _time = o.GetOpenTime();
                _magic = o.GetMagic();
            }

            public bool IsSameOrder(IOrderInfo o)
            {
                return _closeTime == o.GetCloseTime()
                    && _symbol == o.GetSymbol()
                    && _tradeOp == o.GetTradeOperation()
                    && _price == o.GetOpenPrice()
                    && _time == o.GetOpenTime()
                    && _magic == o.GetMagic()
                ;
            }

            public bool IsSameLiveOrder(IOrderInfo o)
            {
                return _symbol == o.GetSymbol()
                    && _tradeOp == o.GetTradeOperation()
                    && _price == o.GetOpenPrice()
                    && _time == o.GetOpenTime()
                    && _magic == o.GetMagic()
                ;
            }
        }
        
        internal static void AdjustPartiallyClosedOrders(List<IOrderInfo> live, List<IOrderInfo> history,
            out List<IOrderInfo> toResolve)
        {
            toResolve = new PartiallyClosedOrdersAdjuster(live, history).Adjust();
        }

        #region IPositionChangeInfo Members

        public List<IOrderInfo> GetDeletedOrders()
        {
            return DeletedOrders;
        }


        public List<IOrderInfo> GetClosedOrders()
        {
            return ClosedOrders;
        }


        public List<IOrderInfo> GetNewOrders()
        {
            return NewOrders;
        }


        public List<IOrderInfo> GetModifiedOrders()
        {
            return ModifiedOrders;
        }

        #endregion
    }
}