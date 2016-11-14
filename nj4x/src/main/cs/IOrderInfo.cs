using System;
using nj4x.Metatrader;

namespace nj4x
{
    /// <summary>
    /// Provides information about a trading order.
    /// </summary>
    public interface IOrderInfo
    {
        /// <summary>
        /// Order's ticket number.
        /// </summary>
        /// <returns>Ticket number.</returns>
        long Ticket();

        /// <summary>
        /// Order's ticket number.
        /// </summary>
        /// <returns>Ticket number.</returns>
        [Obsolete("ticket's data type should be 'long', please use Ticket() method instead.")]
        int GetTicket();

        /// <summary>
        /// Orders operation type.
        /// </summary>
        /// <returns>Orders operation type.</returns>
        TradeOperation GetTradeOperation();

        /// <summary>
        /// The time order was opened at.
        /// </summary>
        /// <returns>Order's open date.</returns>
        DateTime GetOpenTime();

        /// <summary>
        /// The time order was closed at.
        /// </summary>
        /// <returns>Order's close date or <b>MT4.NotDefined</b> if order is active.</returns>
        DateTime GetCloseTime();

        /// <summary>
        /// User-defined magic number.
        /// </summary>
        /// <returns>Order's magic number.</returns>
        int GetMagic();

        /// <summary>
        /// User-defined expiration date.
        /// </summary>
        /// <returns>Order's expiration date or <b>MT4.NotDefined</b> if not set.</returns>
        DateTime GetExpiration();

        /// <summary>
        /// Amount of lots for the order.
        /// </summary>
        /// <returns>Order's Lots amount.</returns>
        double GetLots();

        /// <summary>
        /// The price order was opened at.
        /// </summary>
        /// <returns>Order's open price.</returns>
        double GetOpenPrice();

        /// <summary>
        /// The price order was closed at.
        /// </summary>
        /// <returns>Order's close price.</returns>
        double GetClosePrice();

        /// <summary>
        /// Stop loss level.
        /// </summary>
        /// <returns>Order's stop loss price.</returns>
        double GetStopLoss();

        /// <summary>
        /// Take profit level.
        /// </summary>
        /// <returns>Order's take profit price.</returns>
        double GetTakeProfit();

        /// <summary>
        /// Order's net profit value (without swaps or commissions). For open positions, it is the current unrealized profit. 
        /// For closed orders, it is the fixed profit. 
        /// </summary>
        /// <returns>Order's profit.</returns>
        double GetProfit();

        /// <summary>
        /// Calculated commission value for the order.
        /// </summary>
        /// <returns>Order's commission.</returns>
        double GetCommission();

        /// <summary>
        /// Swap value for the order.
        /// </summary>
        /// <returns>Order's swap value.</returns>
        double GetSwap();

        /// <summary>
        /// Order's symbol.
        /// </summary>
        /// <returns>Order's symbol.</returns>
        String GetSymbol();

        /// <summary>
        /// User-defined order comments.
        /// </summary>
        /// <returns>Order's comment.</returns>
        String GetComment();

        /// <summary>
        /// Indicates whether the order has been modified.
        /// </summary>
        /// <returns><b>true</b> in case the order was modified.</returns>
        bool IsModified();

        /// <summary>
        /// Indicates whether order's type has been changed (e.g. BUY_LIMIT -> BUY)
        /// </summary>
        /// <returns><b>true</b> in case the order type was modified.</returns>
        bool IsTradeOperationChanged();

        /// <summary>
        /// Indicates whether order open time has been changed.
        /// </summary>
        /// <returns><b>true</b> in case the order open time was modified.</returns>
        bool IsOpenTimeChanged();

        /// <summary>
        /// Indicates whether order close time has been changed.
        /// </summary>
        /// <returns><b>true</b> in case the order close time was modified.</returns>
        bool IsCloseTimeChanged();

        /// <summary>
        /// Indicates whether order expiration date has been changed.
        /// </summary>
        /// <returns><b>true</b> in case the order expiration date was modified.</returns>
        bool IsExpirationTimeChanged();

        /// <summary>
        /// Indicates whether order lots value has been changed.
        /// </summary>
        /// <returns><b>true</b> in case the order lots were modified.</returns>
        bool IsLotsChanged();

        /// <summary>
        /// Indicates whether order open price has been changed.
        /// </summary>
        /// <returns><b>true</b> in case the order open price was modified.</returns>
        bool IsOpenPriceChanged();

        /// <summary>
        /// Indicates whether order close price has been changed.
        /// </summary>
        /// <returns><b>true</b> in case the order close price was modified.</returns>
        bool IsClosePriceChanged();

        /// <summary>
        /// Indicates whether order's S/L level has been changed.
        /// </summary>
        /// <returns><b>true</b> in case the stop loss price was modified.</returns>
        bool IsStopLossChanged();

        /// <summary>
        /// Indicates whether order's T/P level has been changed.
        /// </summary>
        /// <returns><b>true</b> in case the take profit price was modified.</returns>
        bool IsTakeProfitChanged();

        /// <summary>
        /// Indicates whether order's profit has been changed.
        /// </summary>
        /// <returns><b>true</b> in case the order's profit was modified.</returns>
        bool IsProfitChanged();

        /// <summary>
        /// Indicates whether order's commission has been changed.
        /// </summary>
        /// <returns><b>true</b> in case the order;s commission was modified.</returns>
        bool IsCommissionChanged();

        /// <summary>
        /// Indicates whether order's swap has been changed.
        /// </summary>
        /// <returns><b>true</b> in case the order's swap was modified.</returns>
        bool IsSwapChanged();

        /// <summary>
        /// Merges an existing order ino with a new state of the same order.
        /// </summary>
        /// <param name="orderInfo">new order state information</param>
        /// <returns>Itself</returns>
        IOrderInfo Merge(IOrderInfo orderInfo);

        /// <summary>
        ///     Set successor order for partially closed order
        /// </summary>
        /// <param name="nextP"></param>
        void SetNext(IOrderInfo nextP);

        /// <summary>
        ///     Set predecessor (partially closed) order
        /// </summary>
        /// <param name="prev"></param>
        void SetPrev(IOrderInfo prev);

        /// <summary>
        ///     Set the order this one was partially closed by.
        /// </summary>
        /// <param name="o"></param>
        void SetClosedBy(IOrderInfo o);

        /// <summary>
        ///     Set the order this one was used to close (partially).
        /// </summary>
        /// <param name="order"></param>
        void SetClosedHedgeOf(IOrderInfo order);

        /// <summary>
        ///     Get predecessor (partially closed) order
        /// </summary>
        /// <returns></returns>
        long GetPrev();

        /// <summary>
        ///     Get successor order number of this partially closed order
        /// </summary>
        /// <returns></returns>
        long GetNext();

        /// <summary>
        ///     Get order ticket this order was partially closed by
        /// </summary>
        /// <returns></returns>
        long GetClosedBy();

        /// <summary>
        ///     Get the order this one was used to close (partially).
        /// </summary>
        long GetClosedHedgeOf();

        /// <summary>
        ///     Flag indicating this is order was not opened AND closed by the system automatically as a result of CloseBy operation.
        /// </summary>
        /// <returns></returns>
        bool WasEverLiveOrder();

        /// <summary>
        ///     Set a flag indicating this is order was not opened AND closed by the system automatically as a result of CloseBy operation.
        /// </summary>
        void SetWasEverLiveOrder(bool f);

        /// <summary>
        ///     Calculated amount of closed lots
        /// </summary>
        double LotsClosed { get; set; }

        /// <summary>
        ///     Calculated amount of lots <see cref="LotsClosed"/> were deducted from.
        /// </summary>
        double LotsBeforeClose { get; set; }
    }
}