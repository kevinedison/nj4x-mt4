package com.jfx.strategy;

/**
 * Trading events handler interface.
 */
public interface PositionListener {
    /**
     * Informs about initial trader's position.
     * @param initialPositionInfo active orders at listener startup.
     */
    public void onInit(PositionInfo initialPositionInfo);

    /**
     * It is invoked on changes in trader's position.
     * @param currentPositionInfo current active orders
     * @param changes position changes: set of new/deleted/closed/modified orders
     */
    public void onChange(PositionInfo currentPositionInfo, PositionChangeInfo changes);
}
