using System;

namespace nj4x.Metatrader
{
#pragma warning disable 1591

    ///<summary>
    ///Account's trading mode
    ///</summary>
    public enum AccountTradeMode
    {
        Demo,
        Contest,
        Real
    }

    ///<summary>
    /// Stop Out Mode
    ///</summary>
    public enum StopOutMode
    {
        Percent,
        Money
    }
#pragma warning restore 1591

    /// <summary>
    /// Account properties set.
    /// </summary>
    public class AccountInfo
    {
        //
        /// <summary>
        /// Account balance in the deposit currency
        /// </summary>
        public double Balance;

        /// <summary>
        /// Name of a company that serves the account
        /// </summary>
        public String Company;

        /// <summary>
        /// Account credit in the deposit currency
        /// </summary>
        public double Credit;

        /// <summary>
        /// Account currency
        /// </summary>
        public String Currency;

        /// <summary>
        /// Account equity in the deposit currency
        /// </summary>
        public double Equity;

        /// <summary>
        /// Free margin of an account in the deposit currency
        /// </summary>
        public double FreeMargin;

        /// <summary>
        /// 'true' if trade is allowed for the current account
        /// </summary>
        public bool IsTradeAllowed;

        /// <summary>
        /// 'true' if trade is allowed for an Expert Advisor
        /// </summary>
        public bool IsTradeExpert;

        /// <summary>
        /// Account leverage
        /// </summary>
        public long Leverage;

        /// <summary>
        /// Maximum allowed number of active pending orders (0-unlimited)
        /// </summary>
        public int LimitOrders;

        /// <summary>
        /// Account number
        /// </summary>
        public long Login;

        /// <summary>
        /// Account margin used in the deposit currency
        /// </summary>
        public double Margin;

        /// <summary>
        /// Account margin level in percents
        /// </summary>
        public double MarginLevel;

        /// <summary>
        /// Margin call level. Depending on the set ACCOUNT_MARGIN_SO_MODE is expressed in percents or in the deposit currency
        /// </summary>
        public double MarginSoCall;

        /// <summary>
        /// Margin stop out level. Depending on the set ACCOUNT_MARGIN_SO_MODE is expressed in percents or in the deposit currency
        /// </summary>
        public double MarginSoLevel;

        /// <summary>
        /// Client name
        /// </summary>
        public String Name;

        /// <summary>
        /// Current profit of an account in the deposit currency
        /// </summary>
        public double Profit;

        /// <summary>
        /// Trade server name
        /// </summary>
        public String Server;

        /// <summary>
        /// Mode for setting the minimal allowed margin
        /// </summary>
        public StopOutMode StopOutMode;

        /// <summary>
        /// Account trade mode
        /// </summary>
        public AccountTradeMode TradeMode;

        private MT4 _conn;

        internal AccountInfo(MT4 conn, String data)
        {
            _conn = conn;
            //
            var p = new SDParser(data, '|');
            //
            Login = p.popLong();
            TradeMode = ((AccountTradeMode[]) Enum.GetValues(typeof (AccountTradeMode)))[p.popInt()];
            Leverage = p.popLong();
            LimitOrders = p.popInt();
            StopOutMode = ((StopOutMode[]) Enum.GetValues(typeof (StopOutMode)))[p.popInt()];
            IsTradeAllowed = p.popBoolean();
            IsTradeExpert = p.popBoolean();
            //
            Name = p.pop();
            Server = p.pop();
            Currency = p.pop();
            Company = p.pop();
            //
            Balance = p.popDouble();
            Credit = p.popDouble();
            Profit = p.popDouble();
            Equity = p.popDouble();
            Margin = p.popDouble();
            FreeMargin = p.popDouble();
            MarginLevel = p.popDouble();
            MarginSoCall = p.popDouble();
            MarginSoLevel = p.popDouble();
        }
    }
}