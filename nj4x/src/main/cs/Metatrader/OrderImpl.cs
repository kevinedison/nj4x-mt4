using System;
using System.Diagnostics;
using System.Globalization;

namespace nj4x.Metatrader
{
    internal class OrderImpl : IOrderInfo
    {
        private const int MaskType = 2;
        private const int MaskOtime = 4;
        private const int MaskCtime = 8;
        private const int MaskExp = 16;
        private const int MaskLots = 32;
        private const int MaskOprice = 64;
        private const int MaskCprice = 128;
        private const int MaskSl = 256;
        private const int MaskTp = 512;
        private const int MaskProfit = 1024;
        private const int MaskCommission = 2048;
        private const int MaskSwap = 4096;
        public readonly long TicketNumber;
        //
        public double ClosePrice;
        public DateTime CloseTime;
        public String Comment;
        public double Commission;
        public DateTime Expiration;
        public double Lots;
        public int Magic;
        public double OpenPrice;
        public DateTime OpenTime;
        public double Profit;
        public double Sl;
        public double Swap;
        public String Symbol;
        public double Tp;
        public TradeOperation Type;
        private long _diffBitMap;
        //
        internal long _next;
        internal long _prev;
        private long _closedBy;
        private long _closedHedgeOf;
        internal bool _wasLiveOrder;
        //
        public double LotsClosed { get; set; }
        public double LotsBeforeClose { get; set; }
        //

        public OrderImpl(String encodedOrderInfo, MT4 utils)
        {
            var p = new SDParser(encodedOrderInfo, '|');
            if (p.peek().StartsWith("C"))
            {
                // order change info
                _diffBitMap = long.Parse(p.pop().Substring(1), CultureInfo.InvariantCulture);
                TicketNumber = long.Parse(p.pop(), CultureInfo.InvariantCulture);
                if (IsModified())
                {
                    if (IsTradeOperationChanged())
                    {
                        Type = ((TradeOperation) int.Parse(p.pop(), CultureInfo.InvariantCulture));
                    }
                    else
                    {
                        p.pop();
                    }
                    if (IsOpenTimeChanged())
                    {
                        OpenTime = utils.ToDate(int.Parse(p.pop(), CultureInfo.InvariantCulture));
                    }
                    else
                    {
                        p.pop();
                    }
                    if (IsCloseTimeChanged())
                    {
                        CloseTime = utils.ToDate(int.Parse(p.pop(), CultureInfo.InvariantCulture));
                    }
                    else
                    {
                        p.pop();
                    }
                    if (IsExpirationTimeChanged())
                    {
                        Expiration = utils.ToDate(int.Parse(p.pop(), CultureInfo.InvariantCulture));
                    }
                    else
                    {
                        p.pop();
                    }
                    if (IsLotsChanged())
                    {
                        Lots = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                    }
                    else
                    {
                        p.pop();
                    }
                    if (IsOpenPriceChanged())
                    {
                        OpenPrice = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                    }
                    else
                    {
                        p.pop();
                    }
                    if (IsClosePriceChanged())
                    {
                        ClosePrice = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                    }
                    else
                    {
                        p.pop();
                    }
                    if (IsStopLossChanged())
                    {
                        Sl = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                    }
                    else
                    {
                        p.pop();
                    }
                    if (IsTakeProfitChanged())
                    {
                        Tp = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                    }
                    else
                    {
                        p.pop();
                    }
                    if (IsProfitChanged())
                    {
                        Profit = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                    }
                    else
                    {
                        p.pop();
                    }
                    if (IsCommissionChanged())
                    {
                        Commission = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                    }
                    else
                    {
                        p.pop();
                    }
                    if (IsSwapChanged())
                    {
                        Swap = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                    }
                    else
                    {
                        p.pop();
                    }
                    if (IsCloseTimeChanged())
                    {
                        string pop = p.pop();
                        Comment = pop ?? Comment;
                    }
                    else
                    {
                        p.pop();
                    }
                }
            }
            else
            {
                TicketNumber = long.Parse(p.pop());
                Type = (TradeOperation) int.Parse(p.pop(), CultureInfo.InvariantCulture);
                OpenTime = utils.ToDate(int.Parse(p.pop(), CultureInfo.InvariantCulture));
                CloseTime = utils.ToDate(int.Parse(p.pop(), CultureInfo.InvariantCulture));
                Magic = int.Parse(p.pop(), CultureInfo.InvariantCulture);
                Expiration = utils.ToDate(int.Parse(p.pop(), CultureInfo.InvariantCulture));
                Lots = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                OpenPrice = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                ClosePrice = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                Sl = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                Tp = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                Profit = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                Commission = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                Swap = double.Parse(p.pop(), CultureInfo.InvariantCulture);
                Symbol = p.pop();
                Comment = p.pop();
            }
        }

        #region IOrderInfo Members

        public long Ticket()
        {
            return TicketNumber;
        }

        public int GetTicket()
        {
            return (int) TicketNumber;
        }

        public TradeOperation GetTradeOperation()
        {
            return Type;
        }


        public DateTime GetOpenTime()
        {
            return OpenTime;
        }


        public DateTime GetCloseTime()
        {
            return CloseTime;
        }


        public int GetMagic()
        {
            return Magic;
        }


        public DateTime GetExpiration()
        {
            return Expiration;
        }


        public double GetLots()
        {
            return Lots;
        }


        public double GetOpenPrice()
        {
            return OpenPrice;
        }


        public double GetClosePrice()
        {
            return ClosePrice;
        }


        public double GetStopLoss()
        {
            return Sl;
        }


        public double GetTakeProfit()
        {
            return Tp;
        }


        public double GetProfit()
        {
            return Profit;
        }


        public double GetCommission()
        {
            return Commission;
        }


        public double GetSwap()
        {
            return Swap;
        }


        public String GetSymbol()
        {
            return Symbol;
        }


        public String GetComment()
        {
            return Comment;
        }


        public bool IsTradeOperationChanged()
        {
            return (MaskType & _diffBitMap) > 0;
        }


        public bool IsOpenTimeChanged()
        {
            return (MaskOtime & _diffBitMap) > 0;
        }


        public bool IsCloseTimeChanged()
        {
            return (MaskCtime & _diffBitMap) > 0;
        }


        public bool IsExpirationTimeChanged()
        {
            return (MaskExp & _diffBitMap) > 0;
        }


        public bool IsLotsChanged()
        {
            return (MaskLots & _diffBitMap) > 0;
        }


        public bool IsOpenPriceChanged()
        {
            return (MaskOprice & _diffBitMap) > 0;
        }


        public bool IsClosePriceChanged()
        {
            return (MaskCprice & _diffBitMap) > 0;
        }


        public bool IsStopLossChanged()
        {
            return (MaskSl & _diffBitMap) > 0;
        }


        public bool IsTakeProfitChanged()
        {
            return (MaskTp & _diffBitMap) > 0;
        }


        public bool IsProfitChanged()
        {
            return (MaskProfit & _diffBitMap) > 0;
        }


        public bool IsCommissionChanged()
        {
            return (MaskCommission & _diffBitMap) > 0;
        }


        public bool IsSwapChanged()
        {
            return (MaskSwap & _diffBitMap) > 0;
        }


        public bool IsModified()
        {
            return _diffBitMap > 0;
        }

        public IOrderInfo Merge(IOrderInfo orderInfo)
        {
            var from = ((OrderImpl) orderInfo);
            _diffBitMap = from._diffBitMap;
            if (IsTradeOperationChanged())
            {
                Type = from.Type;
            }
            if (IsOpenTimeChanged())
            {
                OpenTime = from.OpenTime;
            }
            if (IsCloseTimeChanged())
            {
                CloseTime = from.CloseTime;
                Comment = from.Comment;
            }
            if (IsExpirationTimeChanged())
            {
                Expiration = from.Expiration;
            }
            if (IsLotsChanged())
            {
                Lots = from.Lots;
            }
            if (IsOpenPriceChanged())
            {
                OpenPrice = from.OpenPrice;
            }
            if (IsClosePriceChanged())
            {
                ClosePrice = from.ClosePrice;
            }
            if (IsStopLossChanged())
            {
                Sl = from.Sl;
            }
            if (IsTakeProfitChanged())
            {
                Tp = from.Tp;
            }
            if (IsProfitChanged())
            {
                Profit = from.Profit;
            }
            if (IsCommissionChanged())
            {
                Commission = from.Commission;
            }
            if (IsSwapChanged())
            {
                Swap = from.Swap;
            }
            _str = null;
            return this;
        }

        public void SetNext(IOrderInfo nextP)
        {
            _next = nextP.Ticket();
            nextP.SetWasEverLiveOrder(_wasLiveOrder);
            //
            var orderImpl = nextP as OrderImpl;
            Debug.Assert(orderImpl != null, "orderImpl != null");
            orderImpl._prev = Ticket();
            _str = null;
        }

        public void SetPrev(IOrderInfo prev)
        {
            _prev = prev.Ticket();
            _wasLiveOrder = prev.WasEverLiveOrder();
            //
            var orderImpl = prev as OrderImpl;
            Debug.Assert(orderImpl != null, "orderImpl != null");
            orderImpl._next = Ticket();
            _str = null;
        }

        public void SetClosedBy(IOrderInfo o)
        {
            _closedBy = o.Ticket();
            var orderImpl = o as OrderImpl;
            Debug.Assert(orderImpl != null, "orderImpl != null");
            orderImpl._closedHedgeOf = Ticket();
            _str = null;
        }

        public void SetClosedHedgeOf(IOrderInfo order)
        {
            _closedHedgeOf = order.Ticket();
            var orderImpl = order as OrderImpl;
            Debug.Assert(orderImpl != null, "orderImpl != null");
            orderImpl._closedBy = Ticket();
            _str = null;
        }

        public long GetPrev()
        {
            return _prev;
        }

        public long GetNext()
        {
            return _next;
        }

        public long GetClosedBy()
        {
            return _closedBy;
        }

        public long GetClosedHedgeOf()
        {
            return _closedHedgeOf;
        }

        public bool WasEverLiveOrder()
        {
            return _wasLiveOrder;
        }

        public void SetWasEverLiveOrder(bool f)
        {
            _wasLiveOrder = f;
        }

        #endregion

        private string _str;
        public override String ToString()
        {
            if (_str == null)
            {
                if (Type == TradeOperation.OP_BUY || Type == TradeOperation.OP_SELL)
                {
                    _str = "#" + TicketNumber
                           + (_wasLiveOrder ? "" : "*")
                           + " -> " + Type.ToString().Substring(3) + " " + Symbol
                           + (CloseTime != MT4.NotDefined
                              && LotsBeforeClose > 0 && LotsClosed > 0
                              && (Math.Abs(LotsClosed - Lots) > 0 || Math.Abs(LotsBeforeClose - Lots) > 0)
                               ? $" ({LotsClosed} of {LotsBeforeClose})"
                               : " " + Lots)
                           + " @ " + OpenPrice + " at " + OpenTime + " profit=" +
                           Profit
                           + (CloseTime == MT4.NotDefined ? "" : " closed @ " + ClosePrice + " at " + CloseTime)
                           + (_prev > 0 ? " prev=" + _prev : "")
                           + (_next > 0 ? " next=" + _next : "")
                           + (_closedBy > 0 ? " cBy=" + _closedBy : "")
                           + (_closedHedgeOf > 0 ? " cHdg=" + _closedHedgeOf : "")
                           + (Magic != 0 ? " m=" + Magic : "")
                           + " " + Comment
                        ;
                }
                else
                {
                    _str = "#" + TicketNumber + " -> " + Type + " " + Symbol + " @ " + OpenPrice + ", SL=" + Sl + ", TP=" + Tp;
                }
            }
            //
            return _str;
        }

        public bool Equals(OrderImpl other)
        {
            if (ReferenceEquals(null, other)) return false;
            if (ReferenceEquals(this, other)) return true;
            return other.TicketNumber == TicketNumber;
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != typeof (OrderImpl)) return false;
            return Equals((OrderImpl) obj);
        }

        public override int GetHashCode()
        {
            return (int) TicketNumber;
        }
    }
}