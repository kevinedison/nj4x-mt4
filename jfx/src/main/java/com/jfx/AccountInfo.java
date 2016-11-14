package com.jfx;

/**
 * Account's information.
 */
public class AccountInfo {
    private MT4 conn;
    //
    public long login;
    public TradeMode tradeMode;
    public long leverage;
    public int limitOrders;
    public StopOutMode stopOutMode;
    public boolean isTradeAllowed, isTradeExpert;
    //
    public String name, server, currency, company;
    //
    public double balance, credit, profit, equity, margin, freeMargin, marginLevel, marginSoCall, marginSoLevel;

    AccountInfo(MT4 conn, String data) {
        this.conn = conn;
        SDParser p = new SDParser(data, '|');
        //
        //long
        login = p.popLong();
        tradeMode = TradeMode.values()[p.popInt()];
        leverage = p.popLong();
        limitOrders = p.popInt();
        stopOutMode = StopOutMode.values()[p.popInt()];
        isTradeAllowed = p.popBoolean();
        isTradeExpert = p.popBoolean();
        //
        name = p.pop();
        server = p.pop();
        currency = p.pop();
        company = p.pop();
        //
        balance = p.popDouble();
        credit = p.popDouble();
        profit = p.popDouble();
        equity = p.popDouble();
        margin = p.popDouble();
        freeMargin = p.popDouble();
        marginLevel = p.popDouble();
        marginSoCall = p.popDouble();
        marginSoLevel = p.popDouble();
    }

    /**
     * @return Account number
     */
    public long getLogin() {
        return login;
    }

    /**
     * @return Account trade mode
     */
    public TradeMode getTradeMode() {
        return tradeMode;
    }

    /**
     * @return Account leverage
     */
    public long getLeverage() {
        return leverage;
    }

    /**
     * @return Maximum allowed number of active pending orders (0-unlimited)
     */
    public int getLimitOrders() {
        return limitOrders;
    }

    /**
     * @return Mode for setting the minimal allowed margin
     */
    public StopOutMode getStopOutMode() {
        return stopOutMode;
    }

    /**
     * @return 'true' if trade is allowed for the current account
     */
    public boolean isTradeAllowed() {
        return isTradeAllowed;
    }

    /**
     * @return 'true' if trade is allowed for an Expert Advisor
     */
    public boolean isTradeExpert() {
        return isTradeExpert;
    }

    /**
     * @return Client name
     */
    public String getName() {
        return name;
    }

    /**
     * @return Trade server name
     */
    public String getServer() {
        return server;
    }

    /**
     * @return Account currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @return Name of a company that serves the account
     */
    public String getCompany() {
        return company;
    }

    /**
     * @return Account balance in the deposit currency
     */
    public double getBalance() {
        return balance;
    }

    /**
     * @return Account credit in the deposit currency
     */
    public double getCredit() {
        return credit;
    }

    /**
     * @return Current profit of an account in the deposit currency
     */
    public double getProfit() {
        return profit;
    }

    /**
     * @return Account equity in the deposit currency
     */
    public double getEquity() {
        return equity;
    }

    /**
     * @return Account margin used in the deposit currency
     */
    public double getMargin() {
        return margin;
    }

    /**
     * @return Free margin of an account in the deposit currency
     */
    public double getFreeMargin() {
        return freeMargin;
    }

    /**
     * @return Account margin level in percents
     */
    public double getMarginLevel() {
        return marginLevel;
    }

    /**
     * @return Margin call level. Depending on the set ACCOUNT_MARGIN_SO_MODE is expressed in percents or in the deposit currency
     */
    public double getMarginSoCall() {
        return marginSoCall;
    }

    /**
     * @return Margin stop out level. Depending on the set ACCOUNT_MARGIN_SO_MODE is expressed in percents or in the deposit currency
     */
    public double getMarginSoLevel() {
        return marginSoLevel;
    }

    /**
     * Account's trading mode
     */
    public enum TradeMode {
        DEMO, CONTEST, REAL
    }

    /**
     * StopOut mode
     */
    public enum StopOutMode {
        PERCENT, MONEY
    }

}
