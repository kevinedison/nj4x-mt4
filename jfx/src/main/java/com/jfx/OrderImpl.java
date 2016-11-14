package com.jfx;

import com.jfx.strategy.OrderInfo;

import java.util.Date;

/**
 * Provides information about an order.
 * User: roman
 * Date: 27/09/13
 * Time: 09:47
 */
class OrderImpl implements OrderInfo {
    public long ticket;
    public TradeOperation type;
    public java.util.Date openTime, closeTime;
    public int magic;
    public java.util.Date expiration;
    public double lots, openPrice, closePrice, sl, tp, profit, commission, swap;
    public String symbol, comment;

    @Override
    public long ticket() {
        return ticket;
    }

    @Override
    public int getTicket() {
        return (int) ticket;
    }

    @Override
    public TradeOperation getType() {
        return type;
    }

    @Override
    public Date getOpenTime() {
        return openTime;
    }

    @Override
    public Date getCloseTime() {
        return closeTime;
    }

    @Override
    public int getMagic() {
        return magic;
    }

    @Override
    public Date getExpiration() {
        return expiration;
    }

    @Override
    public double getLots() {
        return lots;
    }

    @Override
    public double getOpenPrice() {
        return openPrice;
    }

    @Override
    public double getClosePrice() {
        return closePrice;
    }

    @Override
    public double getSl() {
        return sl;
    }

    @Override
    public double getTp() {
        return tp;
    }

    @Override
    public double getProfit() {
        return profit;
    }

    @Override
    public double getCommission() {
        return commission;
    }

    @Override
    public double getSwap() {
        return swap;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        if (type == TradeOperation.OP_BUY || type == TradeOperation.OP_SELL) {
            return "#" + ticket + " -> " + type + " " + symbol + " @ " + openPrice + " at " + openTime + " profit=" + profit
                    + (closeTime.getTime() == 0 ? "" : " closed at " + closeTime)
                    ;
        } else {
            return "#" + ticket + " -> " + type + " " + symbol + " @ " + openPrice + ", SL=" + sl + ", TP=" + tp;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderInfo)) return false;

        OrderImpl orderInfo = (OrderImpl) o;

        if (ticket != orderInfo.ticket) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) ticket;
    }

    private long diffBitMap;
    private final static int MASK_TYPE = 2;
    private final static int MASK_OTIME = 4;
    private final static int MASK_CTIME = 8;
    private final static int MASK_EXP = 16;
    private final static int MASK_LOTS = 32;
    private final static int MASK_OPRICE = 64;
    private final static int MASK_CPRICE = 128;
    private final static int MASK_SL = 256;
    private final static int MASK_TP = 512;
    private final static int MASK_PROFIT = 1024;
    private final static int MASK_COMMISSION = 2048;
    private final static int MASK_SWAP = 4096;

    @Override
    public boolean isTypeChanged() {
        return (MASK_TYPE & diffBitMap) > 0;
    }

    @Override
    public boolean isOpenTimeChanged() {
        return (MASK_OTIME & diffBitMap) > 0;
    }

    @Override
    public boolean isCloseTimeChanged() {
        return (MASK_CTIME & diffBitMap) > 0;
    }

    @Override
    public boolean isExpirationTimeChanged() {
        return (MASK_EXP & diffBitMap) > 0;
    }

    @Override
    public boolean isLotsChanged() {
        return (MASK_LOTS & diffBitMap) > 0;
    }

    @Override
    public boolean isOpenPriceChanged() {
        return (MASK_OPRICE & diffBitMap) > 0;
    }

    @Override
    public boolean isClosePriceChanged() {
        return (MASK_CPRICE & diffBitMap) > 0;
    }

    @Override
    public boolean isStopLossChanged() {
        return (MASK_SL & diffBitMap) > 0;
    }

    @Override
    public boolean isTakeProfitChanged() {
        return (MASK_TP & diffBitMap) > 0;
    }

    @Override
    public boolean isProfitChanged() {
        return (MASK_PROFIT & diffBitMap) > 0;
    }

    @Override
    public boolean isCommissionChanged() {
        return (MASK_COMMISSION & diffBitMap) > 0;
    }

    @Override
    public boolean isSwapChanged() {
        return (MASK_SWAP & diffBitMap) > 0;
    }

    @Override
    public boolean isModified() {
        return diffBitMap > 0;
    }

    public OrderImpl(String encodedOrderInfo, MT4 utils) {
        SDParser p = new SDParser(encodedOrderInfo, '|');
        if (p.peek().startsWith("C")) { // order change info
            diffBitMap = Long.parseLong(p.pop().substring(1));
            ticket = Long.parseLong(p.pop());
            if (isModified()) {
                if (isTypeChanged()) {
                    type = TradeOperation.getTradeOperation(Integer.parseInt(p.pop()));
                } else {
                    p.pop();
                }
                if (isOpenTimeChanged()) {
                    openTime = utils.toDate(Integer.parseInt(p.pop()));
                } else {
                    p.pop();
                }
                if (isCloseTimeChanged()) {
                    closeTime = utils.toDate(Integer.parseInt(p.pop()));
                } else {
                    p.pop();
                }
                if (isExpirationTimeChanged()) {
                    expiration = utils.toDate(Integer.parseInt(p.pop()));
                } else {
                    p.pop();
                }
                if (isLotsChanged()) {
                    lots = Double.parseDouble(p.pop());
                } else {
                    p.pop();
                }
                if (isOpenPriceChanged()) {
                    openPrice = Double.parseDouble(p.pop());
                } else {
                    p.pop();
                }
                if (isClosePriceChanged()) {
                    closePrice = Double.parseDouble(p.pop());
                } else {
                    p.pop();
                }
                if (isStopLossChanged()) {
                    sl = Double.parseDouble(p.pop());
                } else {
                    p.pop();
                }
                if (isTakeProfitChanged()) {
                    tp = Double.parseDouble(p.pop());
                } else {
                    p.pop();
                }
                if (isProfitChanged()) {
                    profit = Double.parseDouble(p.pop());
                } else {
                    p.pop();
                }
                if (isCommissionChanged()) {
                    commission = Double.parseDouble(p.pop());
                } else {
                    p.pop();
                }
                if (isSwapChanged()) {
                    swap = Double.parseDouble(p.pop());
                } else {
                    p.pop();
                }
                if (isCloseTimeChanged()) {
                    String pop = p.pop();
                    comment = pop == null ? comment : pop;
                } else {
                    p.pop();
                }
            }
        } else {
            ticket = Long.parseLong(p.pop());
            type = TradeOperation.getTradeOperation(Integer.parseInt(p.pop()));
            openTime = utils.toDate(Integer.parseInt(p.pop()));
            closeTime = utils.toDate(Integer.parseInt(p.pop()));
            magic = Integer.parseInt(p.pop());
            expiration = utils.toDate(Integer.parseInt(p.pop()));
            lots = Double.parseDouble(p.pop());
            openPrice = Double.parseDouble(p.pop());
            closePrice = Double.parseDouble(p.pop());
            sl = Double.parseDouble(p.pop());
            tp = Double.parseDouble(p.pop());
            profit = Double.parseDouble(p.pop());
            commission = Double.parseDouble(p.pop());
            swap = Double.parseDouble(p.pop());
            symbol = p.pop();
            comment = p.pop();
        }
    }

    @Override
    public OrderInfo merge(OrderInfo _from) {
        OrderImpl from = ((OrderImpl) _from);
        diffBitMap = from.diffBitMap;
        if (isTypeChanged()) {
            type = from.type;
        }
        if (isOpenTimeChanged()) {
            openTime = from.openTime;
        }
        if (isCloseTimeChanged()) {
            closeTime = from.closeTime;
            comment = from.comment;
        }
        if (isExpirationTimeChanged()) {
            expiration = from.expiration;
        }
        if (isLotsChanged()) {
            lots = from.lots;
        }
        if (isOpenPriceChanged()) {
            openPrice = from.openPrice;
        }
        if (isClosePriceChanged()) {
            closePrice = from.closePrice;
        }
        if (isStopLossChanged()) {
            sl = from.sl;
        }
        if (isTakeProfitChanged()) {
            tp = from.tp;
        }
        if (isProfitChanged()) {
            profit = from.profit;
        }
        if (isCommissionChanged()) {
            commission = from.commission;
        }
        if (isSwapChanged()) {
            swap = from.swap;
        }
        return this;
    }
}
