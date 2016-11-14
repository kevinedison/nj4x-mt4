using System;
using System.Collections.Generic;

namespace nj4x.Metatrader
{
#pragma warning disable 1591
    ///<summary>
    ///Information about how the margin requirements for a symbol are calculated.
    ///</summary>
    public enum PriceCalculationMode
    {
        Forex,
        Futures,
        CFD,
        CFDIndex,
        CFDLeverage,
        ExchStocks,
        ExchFutures,
        ExchFuturesForts
    }

    ///<summary>
    ///Methods of swap calculation at position transfer
    ///</summary>
    public enum SwapMode
    {
        Disabled,
        Points,
        SymbolCurrency,
        MarginCurrency,
        DepositCurrency,
        CurrentInterest,
        OpenInterest,
        CurrentReopen,
        BidReopen
    }

    public enum ExpirationMode
    {
        GTC,
        Day,
        Specified,
        SpecifiedDay
    }

    public enum FillingMode
    {
        FillOrKill,
        ImmediateOrCancel,
        Return
    }

    public enum OrderMode
    {
        Market,
        Limit,
        Stop,
        StopLimit,
        SL,
        TP
    }

    ///<summary>
    ///There are several symbol trading modes.
    ///</summary>
    public enum TradeMode
    {
        ///<summary>
        ///Trade is disabled for the symbol
        ///</summary>
        Disabled,

        ///<summary>
        ///Allowed only long positions (MT5 only)
        ///</summary>
        LongOnly,

        ///<summary>
        ///Allowed only short positions (MT5 only)
        ///</summary>
        ShortOnly,

        ///<summary>
        ///Allowed only position close operations
        ///</summary>
        CloseOnly,

        ///<summary>
        ///No trade restrictions
        ///</summary>
        Full
    }

    ///<summary>
    ///Possible deal execution modes for a certain symbol
    ///</summary>
    public enum TradeExecutionMode
    {
        ///<summary>
        ///Execution by request
        ///</summary>
        Request,

        ///<summary>
        ///Instant execution
        ///</summary>
        Instant,

        ///<summary>
        ///Market execution
        ///</summary>
        Market,

        ///<summary>
        ///Exchange execution (MT5 only)
        ///</summary>
        Exchange
    }

    ///<summary>
    ///used for specifying weekdays
    ///</summary>
    public enum DayOfWeek
    {
        Sunday,
        Monday,
        Tuesday,
        Wednesday,
        Thursday,
        Friday,
        Saturday
    }
#pragma warning restore 1591

    /// <summary>
    /// Symbol market properties set.
    /// </summary>
    public class SymbolMarketInfo
    {
        ///<summary> Ask - best buy offer</summary>
        public double Ask;

        ///<summary> Bid - best sell offer</summary>
        public double Bid;
        internal SymbolMarketInfo(SDParser p)
        {
            Bid = p.popDouble();
            Ask = p.popDouble();
        }
    }

    /// <summary>
    /// Symbol properties set.
    /// </summary>
    public class SymbolInfo
    {
        ///<summary> Ask - best buy offer</summary>
        public double Ask;

        ///<summary> Maximal Ask of the day</summary>
        public double AskHigh;

        ///<summary> Minimal Ask of the day</summary>
        public double AskLow;

        ///<summary> Basic currency of a symbol</summary>
        public String BaseCurrency;

        ///<summary> Bid - best sell offer</summary>
        public double Bid;

        ///<summary> Maximal Bid of the day</summary>
        public double BidHigh;

        ///<summary> Minimal Bid of the day</summary>
        public double BidLow;

        ///<summary> Contract price calculation mode</summary>
        public PriceCalculationMode ContractPriceCalculationMode;

        ///<summary> Symbol's description</summary>
        public String Description;

        ///<summary> Digits after a decimal point</summary>
        public int Digits;

        ///<summary> allowed order expiration modes</summary>
        public ExpirationMode[] ExpirationModes;

        ///<summary> Date of the symbol trade end (usually used for futures)</summary>
        public DateTime ExpirationTime;

        ///<summary> allowed order filling modes</summary>
        public FillingMode[] FillingModes;

        ///<summary> Distance to freeze trade operations in points</summary>
        public int FreezeLevel;

        ///<summary>
        ///Initial margin means the amount in the margin currency required for opening an order with the volume of one lot.
        ///It is used for checking a client's assets when he or she enters the market.
        /// </summary>
        ///<summary> Initial margin for a symbol</summary>
        public double InitialMargin;

        ///<summary> Indication of a floating spread.</summary>
        public bool IsFloatingSpread;

        ///<summary> true if symbol is selected in Market Watch.</summary>
        public bool IsSelected;

        ///<summary> Price of the last deal</summary>
        public double Last;

        ///<summary> Maximal Last of the day</summary>
        public double LastHigh;

        ///<summary> Minimal Last of the day</summary>
        public double LastLow;

        ///<summary> Rate of margin charging on limit orders</summary>
        public double LimitMargin;

        ///<summary>
        ///Maximum allowed aggregate volume of an opened and pending orders in one direction (buy or sell) for the symbol.
        ///For example, with the limitation of 5 lots, you can have an open buy order with the volume of 5 lots and place a pending order Sell Limit with the volume of 5 lots. But in this case you cannot place a Buy Limit pending order (since the total volume in one direction will exceed the limitation) or place Sell Limit with the volume more than 5 lots.
        /// </summary>
        ///<summary> Maximum allowed aggregate volume of an opened and pending orders in one direction (buy or sell) for the symbol</summary>
        public double LimitVolume;

        ///<summary> Rate of margin charging on buy orders</summary>
        public double LongMargin;

        ///<summary> Buy order swap value</summary>
        public double LongSwap;

        ///<summary>
        ///If it is set, it sets the margin amount in the margin currency of the symbol, charged from one lot.
        ///It is used for checking a client's assets when his/her account state Changes.
        ///If the maintenance margin is equal to 0, the initial margin is used.
        /// </summary>
        ///<summary> The maintenance margin for a symbol.
        ///</summary>
        public double MaintenanceMargin;

        ///<summary> Margin currency of a symbol</summary>
        public String MarginCurrency;

        ///<summary> Maximal volume for a deal</summary>
        public double MaxVolume;

        ///<summary> Minimal volume for a deal</summary>
        public double MinVolume;

        ///<summary> allowed order types</summary>
        public OrderMode[] OrderModes;

        ///<summary> Path in the symbol tree</summary>
        public String Path;

        ///<summary> Symbol point value</summary>
        public double Point;

        ///<summary> Profit currency of a symbol</summary>
        public String ProfitCurrency;

        ///<summary> Average weighted price of the current session</summary>
        public double SessionAverageWeightedPrice;

        ///<summary> Number of Buy orders at the moment</summary>
        public long SessionBuyOrders;


        ///<summary> Current volume of Buy orders</summary>
        public double SessionBuyOrdersVolume;

        ///<summary> Close price of the current session</summary>
        public double SessionClosePrice;

        ///<summary> Number of deals in the current session</summary>
        public long SessionDeals;

        ///<summary> Summary open interest</summary>
        public double SessionInterest;

        ///<summary> Maximal price of the current session</summary>
        public double SessionMaxLimitPrice;

        ///<summary> Minimal price of the current session</summary>
        public double SessionMinLimitPrice;

        ///<summary> Open price of the current session</summary>
        public double SessionOpenPrice;

        ///<summary> Number of Sell orders at the moment</summary>
        public long SessionSellOrders;

        ///<summary> Current volume of Sell orders</summary>
        public double SessionSellOrdersVolume;

        ///<summary> Settlement price of the current session</summary>
        public double SessionSettlementPrice;

        ///<summary> Summary turnover of the current session</summary>
        public double SessionTurnover;

        ///<summary> Summary volume of current session deals</summary>
        public double SessionVolume;

        ///<summary> Rate of margin charging on sell orders</summary>
        public double ShortMargin;

        ///<summary> Sell order swap value</summary>
        public double ShortSwap;

        ///<summary> Spread value in points</summary>
        public int Spread;

        ///<summary> Date of the symbol trade beginning (usually used for futures)</summary>
        public DateTime StartTime;

        ///<summary> Minimal volume change step for deal execution</summary>
        public double StepVolume;

        ///<summary> Rate of margin charging on stop limit orders</summary>
        public double StopLimitMargin;

        ///<summary> Rate of margin charging on stop orders</summary>
        public double StopMargin;

        ///<summary> Minimal indention in points from the current close price to place Stop orders</summary>
        public int StopsLevel;

        ///<summary> Swap calculation model</summary>
        public SwapMode SwapMode;

        ///<summary> Weekday to charge 3 days swap rollover</summary>
        public DayOfWeek SwapRollover3Days;

        ///<summary> Time of the last quote</summary>
        public DateTime Time;

        ///<summary> Trade contract size</summary>
        public double TradeContractSize;

        ///<summary> Deal execution mode</summary>
        public TradeExecutionMode TradeExecutionMode;

        ///<summary> Order execution type</summary>
        public TradeMode TradeMode;

        ///<summary> minimal price change</summary>
        public double TradeTickSize;

        ///<summary> Value of calculated tick price for a profitable order</summary>
        public double TradeTickValue;

        ///<summary> Calculated tick price for a losing order</summary>
        public double TradeTickValueLoss;

        ///<summary> Calculated tick price for a profitable order</summary>
        public double TradeTickValueProfit;

        ///<summary> Volume of the last deal</summary>
        public long Volume;

        ///<summary> Maximal day volume</summary>
        public long VolumeHigh;

        ///<summary> Minimal day volume</summary>
        public long VolumeLow;

        internal SymbolInfo(MT4 conn, String data) : this(conn, new SDParser(data, '|'))
        {
        }

        internal SymbolInfo(MT4 conn, SDParser p)
        {
            //
            IsSelected = p.popBoolean();
            IsFloatingSpread = p.popBoolean();
            //
            SessionDeals = p.popLong();
            SessionBuyOrders = p.popLong();
            SessionSellOrders = p.popLong();
            Volume = p.popLong();
            VolumeHigh = p.popLong();
            VolumeLow = p.popLong();
            //
            Digits = p.popInt();
            Spread = p.popInt();
            StopsLevel = p.popInt();
            FreezeLevel = p.popInt();
            //
            ContractPriceCalculationMode = ((PriceCalculationMode[]) Enum.GetValues(typeof(PriceCalculationMode)))[p.popInt()];
            TradeMode = ((TradeMode[])Enum.GetValues(typeof(TradeMode)))[p.popInt()];
            SwapMode = ((SwapMode[])Enum.GetValues(typeof(SwapMode)))[p.popInt()];
            SwapRollover3Days = ((DayOfWeek[])Enum.GetValues(typeof(DayOfWeek)))[p.popInt()];
            TradeExecutionMode = ((TradeExecutionMode[])Enum.GetValues(typeof(TradeExecutionMode)))[p.popInt()];
            //
            Time = conn.ToDate(p.popDouble());
            StartTime = conn.ToDate(p.popDouble());
            ExpirationTime = conn.ToDate(p.popDouble());
            //
            ExpirationModes = ExpirationModeValues(p.popInt());
            FillingModes = FillingModeValues(p.popInt());
            OrderModes = OrderModeValues(p.popInt());
            //
            BaseCurrency = p.pop();
            ProfitCurrency = p.pop();
            MarginCurrency = p.pop();
            Description = p.pop();
            Path = p.pop();
            //
            Bid = p.popDouble();
            BidHigh = p.popDouble();
            BidLow = p.popDouble();
            Ask = p.popDouble();
            AskHigh = p.popDouble();
            AskLow = p.popDouble();
            Last = p.popDouble();
            LastHigh = p.popDouble();
            LastLow = p.popDouble();
            Point = p.popDouble();
            TradeTickValue = p.popDouble();
            TradeTickValueProfit = p.popDouble();
            TradeTickValueLoss = p.popDouble();
            TradeTickSize = p.popDouble();
            TradeContractSize = p.popDouble();
            MinVolume = p.popDouble();
            MaxVolume = p.popDouble();
            StepVolume = p.popDouble();
            LimitVolume = p.popDouble();
            LongSwap = p.popDouble();
            ShortSwap = p.popDouble();
            InitialMargin = p.popDouble();
            MaintenanceMargin = p.popDouble();
            LongMargin = p.popDouble();
            ShortMargin = p.popDouble();
            LimitMargin = p.popDouble();
            StopMargin = p.popDouble();
            StopLimitMargin = p.popDouble();
            SessionVolume = p.popDouble();
            SessionTurnover = p.popDouble();
            SessionInterest = p.popDouble();
            SessionBuyOrdersVolume = p.popDouble();
            SessionSellOrdersVolume = p.popDouble();
            SessionOpenPrice = p.popDouble();
            SessionClosePrice = p.popDouble();
            SessionAverageWeightedPrice = p.popDouble();
            SessionSettlementPrice = p.popDouble();
            SessionMinLimitPrice = p.popDouble();
            SessionMaxLimitPrice = p.popDouble();
        }

        private static ExpirationMode[] ExpirationModeValues(int flags)
        {
            var res = new List<ExpirationMode>();
            var masks = new[] {1, 2, 4, 8};
            var values = (ExpirationMode[]) Enum.GetValues(typeof (ExpirationMode));
            for (int i = 0; i < masks.Length; i++)
            {
                int mask = masks[i];
                if ((flags & mask) != 0)
                {
                    res.Add(values[i]);
                }
            }
            return res.ToArray();
        }

        private static FillingMode[] FillingModeValues(int flags)
        {
            var res = new List<FillingMode>();
            var masks = new[] {1, 2, 4};
            var values = (FillingMode[]) Enum.GetValues(typeof (FillingMode));
            for (int i = 0; i < masks.Length; i++)
            {
                int mask = masks[i];
                if ((flags & mask) != 0)
                {
                    res.Add(values[i]);
                }
            }
            return res.ToArray();
        }

        private static OrderMode[] OrderModeValues(int flags)
        {
            var res = new List<OrderMode>();
            var masks = new[] {1, 2, 4, 8, 16, 32};
            var values = (OrderMode[]) Enum.GetValues(typeof (OrderMode));
            for (int i = 0; i < masks.Length; i++)
            {
                int mask = masks[i];
                if ((flags & mask) != 0)
                {
                    res.Add(values[i]);
                }
            }
            return res.ToArray();
        }

/*
        ///<returns> Indication of a floating spread.</returns>
        public bool IsFloatingSpread()
        {
            return isFloatingSpread;
        }

        ///<returns> true if symbol is selected in Market Watch.</returns>
        public bool IsSelected()
        {
            return isSelected;
        }

        ///<returns> Bid - best sell offer</returns>
        public double getBid()
        {
            return Bid;
        }

        ///<returns> Maximal Bid of the day</returns>
        public double getBidHigh()
        {
            return BidHigh;
        }

        ///<returns> Minimal Bid of the day</returns>
        public double getBidLow()
        {
            return BidLow;
        }

        ///<returns> Ask - best buy offer</returns>
        public double getAsk()
        {
            return Ask;
        }

        ///<returns> Maximal Ask of the day</returns>
        public double getAskHigh()
        {
            return AskHigh;
        }

        ///<returns> Minimal Ask of the day</returns>
        public double getAskLow()
        {
            return AskLow;
        }

        ///<returns> Price of the last deal</returns>
        public double getLast()
        {
            return Last;
        }

        ///<returns> Maximal Last of the day</returns>
        public double getLastHigh()
        {
            return LastHigh;
        }

        ///<returns> Minimal Last of the day</returns>
        public double getLastLow()
        {
            return LastLow;
        }

        ///<returns> Symbol point value</returns>
        public double getPoint()
        {
            return Point;
        }

        ///<returns> Value of calculated tick price for a profitable order</returns>
        public double getTradeTickValue()
        {
            return TradeTickValue;
        }

        ///<returns> Calculated tick price for a profitable order</returns>
        public double getTradeTickValueProfit()
        {
            return TradeTickValueProfit;
        }

        ///<returns> Calculated tick price for a losing order</returns>
        public double getTradeTickValueLoss()
        {
            return TradeTickValueLoss;
        }

        ///<returns> minimal price change</returns>
        public double getTradeTickSize()
        {
            return TradeTickSize;
        }

        ///<returns> Trade contract size</returns>
        public double getTradeContractSize()
        {
            return TradeContractSize;
        }

        ///<returns> Minimal volume for a deal</returns>
        public double getMinVolume()
        {
            return MinVolume;
        }

        ///<returns> Maximal volume for a deal</returns>
        public double getMaxVolume()
        {
            return MaxVolume;
        }

        ///<returns> Minimal volume change step for deal execution</returns>
        public double getStepVolume()
        {
            return StepVolume;
        }

        ///<summary>
        ///Maximum allowed aggregate volume of an opened and pending orders in one direction (buy or sell) for the symbol.
        ///For example, with the limitation of 5 lots, you can have an open buy order with the volume of 5 lots and place a pending order Sell Limit with the volume of 5 lots. But in this case you cannot place a Buy Limit pending order (since the total volume in one direction will exceed the limitation) or place Sell Limit with the volume more than 5 lots.
        /// </summary>
        ///<returns> Maximum allowed aggregate volume of an opened and pending orders in one direction (buy or sell) for the symbol</returns>
        public double getLimitVolume()
        {
            return LimitVolume;
        }

        ///<returns> Buy order swap value</returns>
        public double getLongSwap()
        {
            return LongSwap;
        }

        ///<returns> Sell order swap value</returns>
        public double getShortSwap()
        {
            return ShortSwap;
        }

        ///<summary>
        ///Initial margin means the amount in the margin currency required for opening an order with the volume of one lot.
        ///It is used for checking a client's assets when he or she enters the market.
        /// </summary>
        ///<returns> Initial margin for a symbol</returns>
        public double getInitialMargin()
        {
            return InitialMargin;
        }

        ///<summary>
        ///If it is set, it sets the margin amount in the margin currency of the symbol, charged from one lot.
        ///It is used for checking a client's assets when his/her account state Changes.
        ///If the maintenance margin is equal to 0, the initial margin is used.
        /// </summary>
        ///<returns> The maintenance margin for a symbol.
        ///</returns>
        public double getMaintenanceMargin()
        {
            return MaintenanceMargin;
        }

        ///<returns> Rate of margin charging on buy orders</returns>
        public double getLongMargin()
        {
            return LongMargin;
        }

        ///<returns> Rate of margin charging on sell orders</returns>
        public double getShortMargin()
        {
            return ShortMargin;
        }

        ///<returns> Rate of margin charging on limit orders</returns>
        public double getLimitMargin()
        {
            return LimitMargin;
        }

        ///<returns> Rate of margin charging on stop orders</returns>
        public double getStopMargin()
        {
            return StopMargin;
        }

        ///<returns> Rate of margin charging on stop limit orders</returns>
        public double getStopLimitMargin()
        {
            return StopLimitMargin;
        }

        ///<returns> Summary volume of current session deals</returns>
        public double getSessionVolume()
        {
            return SessionVolume;
        }

        ///<returns> Summary turnover of the current session</returns>
        public double getSessionTurnover()
        {
            return SessionTurnover;
        }

        ///<returns> Summary open interest</returns>
        public double getSessionInterest()
        {
            return SessionInterest;
        }

        ///<returns> Current volume of Buy orders</returns>
        public double getSessionBuyOrdersVolume()
        {
            return SessionBuyOrdersVolume;
        }

        ///<returns> Current volume of Sell orders</returns>
        public double getSessionSellOrdersVolume()
        {
            return SessionSellOrdersVolume;
        }

        ///<returns> Open price of the current session</returns>
        public double getSessionOpenPrice()
        {
            return SessionOpenPrice;
        }

        ///<returns> Close price of the current session</returns>
        public double getSessionClosePrice()
        {
            return SessionClosePrice;
        }

        ///<returns> Average weighted price of the current session</returns>
        public double getSessionAverageWeightedPrice()
        {
            return SessionAverageWeightedPrice;
        }

        ///<returns> Settlement price of the current session</returns>
        public double getSessionSettlementPrice()
        {
            return SessionSettlementPrice;
        }

        ///<returns> Minimal price of the current session</returns>
        public double getSessionMinLimitPrice()
        {
            return SessionMinLimitPrice;
        }

        ///<returns> Maximal price of the current session</returns>
        public double getSessionMaxLimitPrice()
        {
            return SessionMaxLimitPrice;
        }

        ///<returns> Number of deals in the current session</returns>
        public long getSessionDeals()
        {
            return SessionDeals;
        }

        ///<returns> Number of Buy orders at the moment</returns>
        public long getSessionBuyOrders()
        {
            return SessionBuyOrders;
        }

        ///<returns> Number of Sell orders at the moment</returns>
        public long getSessionSellOrders()
        {
            return SessionSellOrders;
        }

        ///<returns> Volume of the last deal</returns>
        public long getVolume()
        {
            return Volume;
        }

        ///<returns> Maximal day volume</returns>
        public long getVolumeHigh()
        {
            return VolumeHigh;
        }

        ///<returns> Minimal day volume</returns>
        public long getVolumeLow()
        {
            return VolumeLow;
        }

        ///<returns> Digits after a decimal point</returns>
        public int getDigits()
        {
            return Digits;
        }

        ///<returns> Spread value in points</returns>
        public int getSpread()
        {
            return Spread;
        }

        ///<returns> Minimal indention in points from the current close price to place Stop orders</returns>
        public int getStopsLevel()
        {
            return StopsLevel;
        }

        ///<returns> Distance to freeze trade operations in points</returns>
        public int getFreezeLevel()
        {
            return FreezeLevel;
        }

        ///<returns> Contract price calculation mode</returns>
        public PriceCalculationMode getContractPriceCalculationMode()
        {
            return ContractPriceCalculationMode;
        }

        ///<returns> Order execution type</returns>
        public TradeMode getTradeMode()
        {
            return TradeMode;
        }

        ///<returns> Swap calculation model</returns>
        public SwapMode getSwapMode()
        {
            return SwapMode;
        }

        ///<returns> Weekday to charge 3 days swap rollover</returns>
        public DayOfWeek getSwapRollover3Days()
        {
            return SwapRollover3Days;
        }

        ///<returns> Deal execution mode</returns>
        public TradeExecutionMode getTradeExecutionMode()
        {
            return TradeExecutionMode;
        }

        ///<returns> Time of the last quote</returns>
        public DateTime getTime()
        {
            return Time;
        }

        ///<returns> Date of the symbol trade beginning (usually used for futures)</returns>
        public DateTime getStartTime()
        {
            return StartTime;
        }

        ///<returns> Date of the symbol trade end (usually used for futures)</returns>
        public DateTime getExpirationTime()
        {
            return ExpirationTime;
        }

        ///<returns> allowed order expiration modes</returns>
        public ExpirationMode[] getExpirationModes()
        {
            return ExpirationModes;
        }

        ///<returns> allowed order filling modes</returns>
        public FillingMode[] getFillingModes()
        {
            return FillingModes;
        }

        ///<returns> allowed order types</returns>
        public OrderMode[] getOrderModes()
        {
            return OrderModes;
        }
*/
    }
}