package com.jfx.strategy;

import java.util.List;

/**
 * Changes in trader's position: new, modified, deleted or closed orders.
 */
public interface PositionChangeInfo {
    List<OrderInfo> getDeletedOrders();

    List<OrderInfo> getClosedOrders();

    List<OrderInfo> getNewOrders();

    List<OrderInfo> getModifiedOrders();
}
