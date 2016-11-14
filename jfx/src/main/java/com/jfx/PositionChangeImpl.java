package com.jfx;

import com.jfx.strategy.OrderInfo;
import com.jfx.strategy.PositionChangeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Changes in trader's position: new, modified, deleted or closed orders.
 */
class PositionChangeImpl implements PositionChangeInfo {
    public ArrayList<OrderInfo> deletedOrders;
    public ArrayList<OrderInfo> closedOrders;
    public ArrayList<OrderInfo> newOrders;
    public ArrayList<OrderInfo> modifiedOrders;

    public PositionChangeImpl() {
        deletedOrders = new ArrayList<OrderInfo>();
        closedOrders = new ArrayList<OrderInfo>();
        newOrders = new ArrayList<OrderInfo>();
        modifiedOrders = new ArrayList<OrderInfo>();
    }

    @Override
    public List<OrderInfo> getDeletedOrders() {
        return deletedOrders;
    }

    @Override
    public List<OrderInfo> getClosedOrders() {
        return closedOrders;
    }

    @Override
    public List<OrderInfo> getNewOrders() {
        return newOrders;
    }

    @Override
    public List<OrderInfo> getModifiedOrders() {
        return modifiedOrders;
    }

    private boolean isClosed(OrderInfo o) {
        return o.getType() == TradeOperation.OP_BUY || o.getType() == TradeOperation.OP_SELL;
    }

    void addClosedOrDeletedOrder(OrderInfo o) {
        if (isClosed(o)) {
            closedOrders.add(o);
        } else {
            deletedOrders.add(o);
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Changes: ");
        if (deletedOrders != null && deletedOrders.size() > 0) {
            s.append('\n').append("DELETED: ").append(deletedOrders);
        }
        if (closedOrders != null && closedOrders.size() > 0) {
            s.append('\n').append("CLOSED: ").append(closedOrders);
        }
        if (modifiedOrders != null && modifiedOrders.size() > 0) {
            s.append('\n').append("MODIFIED: ").append(modifiedOrders);
        }
        if (newOrders != null && newOrders.size() > 0) {
            s.append('\n').append("NEW: ").append(newOrders);
        }
        return s.toString();
    }
}
