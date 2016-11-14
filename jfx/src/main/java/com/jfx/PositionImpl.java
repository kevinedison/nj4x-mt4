package com.jfx;

import com.jfx.strategy.OrderInfo;
import com.jfx.strategy.PositionChangeInfo;
import com.jfx.strategy.PositionInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Trader's position.
 */
class PositionImpl implements PositionInfo {
    private int tCount, hCount;
    public HashMap<Long, OrderInfo> liveOrders;
    public HashMap<Long, OrderInfo> historicalOrders;

    public PositionImpl(String positionEncoded, MT4 utils) {
        DDParser bp = new DDParser(positionEncoded, MT4.ARG_BEGIN, MT4.ARG_END);
        SDParser p = new SDParser(bp.pop(), '|');
        tCount = Integer.parseInt(p.pop());
        hCount = Integer.parseInt(p.pop());
        //
        int sz = Integer.parseInt(bp.pop());
        liveOrders = new HashMap<Long, OrderInfo>((int) (sz * 1.2));
        for (int i = 0; i < sz; i++) {
            OrderImpl oi = new OrderImpl(bp.pop(), utils);
            liveOrders.put(oi.ticket, oi);
        }
        sz = Integer.parseInt(bp.pop());
        historicalOrders = new HashMap<Long, OrderInfo>((int) (sz * 1.2));
        for (int i = 0; i < sz; i++) {
            OrderImpl oi = new OrderImpl(bp.pop(), utils);
            historicalOrders.put(oi.ticket, oi);
        }
    }

    @Override
    public PositionChangeInfo mergePosition(PositionInfo _newPositionInfo) {
        PositionImpl newPositionInfo = (PositionImpl) _newPositionInfo;
        tCount = newPositionInfo.tCount;
        hCount = newPositionInfo.hCount;
        //
        PositionChangeImpl pc = new PositionChangeImpl();
        for (OrderInfo o : newPositionInfo.liveOrders.values()) {
            OrderInfo orderInfo = liveOrders.get(o.ticket());
            if (orderInfo == null) {
                pc.newOrders.add(o);
                liveOrders.put(o.ticket(), o);
            } else if (o.isModified()) {
                pc.modifiedOrders.add(orderInfo.merge(o));
            }
        }
        for (OrderInfo o : newPositionInfo.historicalOrders.values()) {
            OrderInfo orderInfo = liveOrders.remove(o.ticket());
            if (orderInfo == null) {
                // ?? we've missed some order ??
                pc.addClosedOrDeletedOrder(o);
                historicalOrders.put(o.ticket(), o);
            } else {
                pc.addClosedOrDeletedOrder(orderInfo.merge(o));
                historicalOrders.put(orderInfo.ticket(), orderInfo);
            }
        }
        //
        if (newPositionInfo.liveOrders.size() != liveOrders.size()) {
            ArrayList<OrderInfo> lostOrders = new ArrayList<OrderInfo>();
            for (OrderInfo o : liveOrders.values()) {
                if (!newPositionInfo.liveOrders.containsKey(o.ticket())) {
                    lostOrders.add(o);
                }
            }
            for (OrderInfo o : lostOrders) {
                liveOrders.remove(o.ticket());
                pc.addClosedOrDeletedOrder(o);
            }
        }
        //
        return pc;
    }

    public int getTCount() {
        return tCount;
    }

    public int getHCount() {
        return hCount;
    }

    @Override
    public Map<Long, OrderInfo> liveOrders() {
        return liveOrders;
    }

    @Override
    public Map<Long, OrderInfo> historicalOrders() {
        return historicalOrders;
    }

    @Override
    public Map<Integer, OrderInfo> getLiveOrders() {
        Map<Integer, OrderInfo> r = new HashMap<>();
        for (Map.Entry<Long, OrderInfo> e : liveOrders.entrySet()) {
            r.put(e.getKey().intValue(), e.getValue());
        }
        return r;
    }

    @Override
    public Map<Integer, OrderInfo> getHistoricalOrders() {
        Map<Integer, OrderInfo> r = new HashMap<>();
        for (Map.Entry<Long, OrderInfo> e : historicalOrders.entrySet()) {
            r.put(e.getKey().intValue(), e.getValue());
        }
        return r;
    }
}
