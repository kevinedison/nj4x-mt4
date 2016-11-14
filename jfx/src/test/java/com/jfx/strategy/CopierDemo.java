package com.jfx.strategy;

import com.jfx.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is one-to-one MT4 Account copier application, NJ4X API usage sample.
 * </p>
 * What it does ...
 * Detects new orders made by a Master account and copies them to the Copier account
 * </p>
 * <p/>
 * Detects Master orders cancellation and cancels respective Copier account orders
 * </p>
 * <p/>
 * Detects closed Master orders and closes respective Copier account orders
 * </p>
 * <p/>
 * </p>
 * You can easily extend its functionality to ...
 * <p/>
 * Handle one-to-many Master/Slave accounts
 * </p>
 * <p/>
 * Handle many-to-many Master/Slave accounts
 * </p>
 * <p/>
 * Detect pending orders modifications and reflect them in Copier account orders
 * </p>
 * <p/>
 * Detect partially closed Master orders and close partially respective Copier orders
 * </p>
 * <p/>
 * Create Copier Orders using proportional volumes
 * </p>
 * <p/>
 * Map different symbols for different brokers
 * </p>
 * <p/>
 * Apply custom order copy filtering algorithms
 * </p>
 * <p/>
 * Build GUI/WEB applications on top of that
 * </p>
 */
public class CopierDemo {

    public static final String JFX_ACTIVATION_KEY = System.getProperty("jfx_activation_key", "235961853");
    public static final String NJ4X_MT5_ACTIVATION_KEY = System.getProperty("nj4x_mt5_activation_key", "3010675205");

    static {
        System.setProperty("nj4x_activation_key", JFX_ACTIVATION_KEY);
        System.setProperty("nj4x_mt5_activation_key", NJ4X_MT5_ACTIVATION_KEY);
    }

    /**
     * NJ4X Terminal Server host IP address
     */
    public static final String TerminalHost = "localhost";

    /**
     * NJ4X Terminal Server port number
     */
    public static final int TerminalPort = 7788;

    /**
     * Master account broker name
     */
    public static final String
            MasterBroker = "MIGBank-Demo";

    /**
     * Slave Account Broker Name
     */
    public static final String
            CopierBroker = "MIGBank-Demo";

    /**
     * Master Account number
     */
    public static final String
            MasterAccount = "1218296449";

    /**
     * Master Account password
     */
    public static final String
            MasterPassword = "os1brmp";

    /**
     * Slave Account number
     */
    public static final String
            CopierAccount = "1218296450";

    /**
     * Slave Account password
     */
    public static final String
            CopierPassword = "sya8ltw";

    /**
     * CopierDemo application entry point.
     */
    public static void main(String[] args) {
        try {
            Master master = new Master(MasterAccount, MasterPassword, MasterBroker);
            Copier slave = new Copier(CopierAccount, CopierPassword, CopierBroker);
            //
            master.AddSlave(slave);
            //
            //noinspection InfiniteLoopStatement
            while (true) {
                Thread.sleep(100000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * Copier class implements MT4 Copier Account Logic,
 * i.e. synchronizes trading operations between Master and Copier accounts
 */
@SuppressWarnings("deprecation")
class Copier extends Strategy {
    private static final Logger LOGGER = Logger.getLogger(Copier.class);
    private final String acc;
    private final String broker;
    private final Hashtable<Integer, Integer> _ordersMap;
    private final String password;
    private Master master;

    /**
     * When constructed, tries to connect to MT4 Server immediately
     */
    public Copier(String acc, String password, String broker) throws IOException {
        this.acc = acc;
        this.password = password;
        this.broker = broker;
        _ordersMap = new Hashtable<>();
        connect(CopierDemo.TerminalHost, CopierDemo.TerminalPort, new Broker(this.broker), this.acc, this.password);
        info("Slave connected");
    }

    private void info(String msg) {
        LOGGER.info(acc + "@" + broker + "> " + msg);
    }

    /**
     * *Loads current Copier account orders at creation.
     */
    @Override
    synchronized void init(String symbol, int period) throws ErrUnknownSymbol, IOException {
        super.init(symbol, period);
        int ordersTotal = ordersTotal();
        for (int i = 0; i < ordersTotal; i++) {
            OrderInfo order = orderGet(i, SelectionType.SELECT_BY_POS, SelectionPool.MODE_TRADES);
            if (order != null) {
                if (order.getMagic() != 0) {
                    _ordersMap.put(
                            order.getMagic(), // Master's order ticket
                            order.getTicket()
                    );
                    info(String.format("Master order %d is mapped to %s", order.getMagic(), "" + order));
                } else {
                    info(String.format("Custom order %s left un-managed", "" + order));
                }
            }
        }
    }

    void setMaster(Object master) throws MT4Exception {
        this.master = (Master) master;
        info(String.format("Attached to %s", this.master.Acc + "@" + this.master.Broker));
        //
        PositionInfo positionInfo = this.master.PositionInfo;
        for (OrderInfo masterOrder : positionInfo.historicalOrders().values()) {
            MasterOrderClosedOrDeleted(masterOrder);
        }
    }

    void MasterOrderClosedOrDeleted(OrderInfo masterOrder) throws ErrUnknownSymbol, ErrNoOrderSelected {
        if (_ordersMap.containsKey(masterOrder.getTicket())) {
            int orderTicket = _ordersMap.get(masterOrder.getTicket());
            OrderInfo order = orderGet(orderTicket, SelectionType.SELECT_BY_TICKET, SelectionPool.MODE_TRADES);
            if (order != null) {
                if (CloseOrder(order)) {
                    _ordersMap.remove(masterOrder.getTicket());
                }
            } else {
                info(String.format("Order %d not found", orderTicket));
                _ordersMap.remove(masterOrder.getTicket());
            }
        }
    }

    private boolean CloseOrder(OrderInfo order) throws ErrUnknownSymbol, ErrNoOrderSelected {
        TradeOperation orderType = order.getType();
        switch (orderType.val) {
            case TradeOperation._OP_BUY:
            case TradeOperation._OP_SELL:
                try {
                    if (orderClose(order.getTicket(), orderLots(),
                            marketInfo(symbol(),
                                    orderType.val == TradeOperation._OP_BUY
                                            ? MarketInfo.MODE_BID
                                            : MarketInfo.MODE_ASK),
                            5, 0)) {
                        info(String.format("Order %s has been closed", order.toString()));
                        return true;
                    }
                } catch (MT4Exception err) {
                    err.printStackTrace();
                }
                info(String.format("Order %s to be closed; todo", order.toString()));
                return false;
            default:
                try {
                    if (orderDelete(order.getTicket(), Color.Black.val)) {
                        info(String.format("Order %s has been deleted", order.toString()));
                        return true;
                    }
                } catch (MT4Exception err) {
                    err.printStackTrace();
                }
                info(String.format("Can not delete order %s", order.toString()));
                return false;
        }
/*
        try {
        } catch (MT4Exception err) {
            info(String.format("Looks like order %s has been deleted manually", order.toString()));
            return true;
        }
*/
    }

    void MasterOrderCreated(OrderInfo masterOrder) {
        try {
            Date exp = masterOrder.getExpiration();
            int ticket = orderSend(
                    masterOrder.getSymbol(),
                    masterOrder.getType(),
                    masterOrder.getLots(),
                    masterOrder.getOpenPrice(), // todo: adjust price to current Bid/Ask if OrderType==OP_BUY/SELL
                    5,
                    masterOrder.getSl(),
                    masterOrder.getTp(),
                    master.Acc + "@" + master.Broker,
                    masterOrder.getTicket(),
                    exp == null || exp.getTime() == 0 ? null : exp,
                    0
            );
            if (ticket != 0) {
                _ordersMap.put(masterOrder.getTicket(), ticket);
                info(String.format("Master order %s is mapped to %d", masterOrder.toString(), ticket));
            }
        } catch (MT4Exception err) {
            err.printStackTrace();
        }
    }

    /**
     * It is called by Master account on its position change.
     */
    public void OnChange(PositionChangeInfo masterPositionChanges) throws MT4Exception {
        PositionChangeInfo changes = masterPositionChanges;
        for (OrderInfo o : changes.getNewOrders()) {
            LOGGER.info("NEW: " + o);
            MasterOrderCreated(o);
        }
        for (OrderInfo o : changes.getModifiedOrders()) {
            LOGGER.info("MODIFIED: " + o);
        }
        for (OrderInfo o : changes.getClosedOrders()) {
            LOGGER.info("CLOSED: " + o);
            MasterOrderClosedOrDeleted(o);
        }
        for (OrderInfo o : changes.getDeletedOrders()) {
            LOGGER.info("DELETED: " + o);
            MasterOrderClosedOrDeleted(o);
        }
    }
}

/**
 * This class implements Master MT4 account logic,
 * i.e. detects changes to the master account and informs registered Copier accounts to reflect those changes.
 */
class Master extends Strategy {
    private final static ExecutorService threadPool = Executors.newCachedThreadPool();
    private static final Logger LOGGER = Logger.getLogger(Master.class);
    //
    final String Acc;
    final String Broker;
    final String Passwd;
    final ArrayList<Copier> Slaves;

    PositionInfo PositionInfo;

    /**
     * *When constructed, tries to connect to MT4 Server immediately
     */
    public Master(String acc, String passwd, String broker) throws IOException {
        Acc = acc;
        Passwd = passwd;
        Broker = broker;
        //
        Slaves = new ArrayList<>();
        //
        setPositionListener(
                new PositionListener() {
                    @Override
                    public void onInit(PositionInfo info) {
                        PositionInfo = info;
                    }

                    @Override
                    public void onChange(PositionInfo info, final PositionChangeInfo changes) {
                        for (final Copier c : Slaves) {
                            threadPool.submit(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        c.OnChange(changes);
                                    } catch (MT4Exception err) {
                                        err.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                }
        );
        connect(CopierDemo.TerminalHost, CopierDemo.TerminalPort, new Broker(Broker), Acc, Passwd);
        //
        info("Master connected");
    }

    private void info(String msg) {
        LOGGER.info(Acc + "@" + Broker + "> " + msg);
    }

    /**
     * *Attaches Copier account to this Master.
     */
    public void AddSlave(final Copier copier) {
        Slaves.add(copier);
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    copier.setMaster(Master.this);
                } catch (MT4Exception err) {
                    err.printStackTrace();
                }
            }
        });
    }
}
