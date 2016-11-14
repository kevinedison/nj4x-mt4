package com.jfx.strategy;

import com.jfx.Broker;
import com.jfx.DemoAccount;
import com.jfx.MT4;
import com.jfx.TickInfo;
import com.jfx.strategy.Strategy.TickListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class PG extends JFrame {
    static {
        String jfxActivationKey = DemoAccount.JFX_ACTIVATION_KEY;
    }

    private JPanel PricePane = null;
    private JPanel FileHeadPane = null;
    private JPanel TablePane = null;
    private JLabel PriceHead = null;
    private JTable Jtable = null;
    private JScrollPane JScrollPane = null;
    private DefaultTableModel DefaultTableModel = null;
    private Vector head = new Vector();
//    private priceTables priceTables = new priceTables();

    // private

    public PG() {
        super("PriceGoods");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(this);
        setContentPane(contentPane());
        setVisible(true);
    }

    public static void main(String[] args) {

        PG priceGoods = new PG();
        /*try {
			priceGoods.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
    }

    public JPanel contentPane() {
        if (PricePane == null) {
            PricePane = new JPanel(new BorderLayout());
            PricePane.setBackground(Color.yellow);
            PricePane.add(FileHeadPane(), BorderLayout.NORTH);
            PricePane.add(TablePane(), BorderLayout.CENTER);

        }
        return PricePane;
    }

    public JPanel FileHeadPane() {
        if (FileHeadPane == null) {
            FileHeadPane = new JPanel(new FlowLayout());
            FileHeadPane.add(PriceHeadLabel());
            FileHeadPane.setBackground(Color.WHITE);
        }
        return FileHeadPane;
    }

    public JLabel PriceHeadLabel() {
        if (PriceHead == null) {
            PriceHead = new JLabel("Price Goods");
            // PriceHead.setFont();
            PriceHead.setBackground(Color.red);
        }
        return PriceHead;
    }

    // ===========================
    public JPanel TablePane() {
        if (TablePane == null) {
            TablePane = new JPanel(new BorderLayout());
            TablePane.add(jScrollPane(), BorderLayout.CENTER);
            TablePane.setBackground(Color.gray);
        }
        return TablePane;
    }


    public JScrollPane jScrollPane() {
        if (JScrollPane == null) {
            JScrollPane = new JScrollPane();
            JScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            JScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            //JScrollPane.add(Jtable());
            JScrollPane.setViewportView(Jtable());
            JScrollPane.setBackground(Color.YELLOW);
        }
        return JScrollPane;

    }

    public JTable Jtable() {
        if (Jtable == null) {

            Jtable = new JTable();
            // Jtable.setPreferredScrollableViewportSize(new Dimension(300,
            // 200));
            Jtable.setModel(tableModel());
            //Jtable.setPreferredSize(new Dimension(300, 200));
            Jtable.setBackground(Color.red);
        }
        return Jtable;
    }

    public DefaultTableModel tableModel() {
        head.add("Symbol");
        head.add("Ask");
        head.add("Bid");
        head.add("Time");

        final Vector tab_data = new Vector();
        //	PriceGoods priceGoods = new PriceGoods();
	/*	try {
			tab_data=DataPrice();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */


        if (DefaultTableModel == null) {
            DefaultTableModel = new DefaultTableModel(tab_data, head);

//            System.setProperty("jfx_activation_key", "291127292");
            Strategy strategy = new Strategy();
            try {
                strategy.connect(Account.MT_4_Addres, Account.MT_4_Prot, Account.MT_4_SERVER, Account.MT_4_USER, Account.MT_4_PASSWORD);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            System.out.println("=========================>>>>>>>>>>.");
            Strategy.Terminal terminal;
            terminal = strategy.addTerminal(Strategy.TerminalType.TICK_WORKER);
            for (final String symbol : strategy.getSymbols()) {

                terminal.addTickListener("GOLD", new TickListener() {
                    @Override
                    public void onTick(TickInfo tick, MT4 connection) {

                        //  System.out.println("" + System.currentTimeMillis() + "> " + tick + " " + connection.accountBalance());
                        HashMap<Integer, Double> activeOrdersProfitLoss = tick.orderPlMap;
                        Date time = tick.time;
                        double ask = tick.ask;
                        double bid = tick.bid;
                        Vector row = new Vector();
                        row.add("GOLD");
                        row.add(tick.ask);
                        row.add(tick.bid);
                        row.add(tick.time);
                        tab_data.add(row);
                        // double high =tick.hashCode();
                        //System.out.println("time:"+ time + "ask:" + ask + "bid:" + bid );
                    }
                });
            }
            try {
                terminal.connect();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return DefaultTableModel;
    }

    ///-----------------------------------------
    private static class Account {
        public static final Broker MT_4_SERVER = new Broker(
                "demo1-amsterdam.fxpro.com");
        public static final String MT_4_USER = "6796186";
        public static final String MT_4_PASSWORD = "dpkk4hg";
        public static final String MT_4_Addres = "127.0.0.1";
        public static final int MT_4_Prot = 7788;
    }

/*
	public void run() throws IOException{
		System.setProperty("jfx_activation_key", "291127292");
		Strategy strategy=new Strategy();
		strategy.connect(Account.MT_4_Addres, Account.MT_4_Prot, Account.MT_4_SERVER, Account.MT_4_USER, Account.MT_4_PASSWORD);

		System.out.println("=========================>>>>>>>>>>.");
		Strategy.Terminal terminal;
		terminal = strategy.addTerminal(Strategy.TerminalType.TICK_WORKER);
		for(final String symbol:strategy.getSymbols()){

			terminal.addTickListener("GOLD",new TickListener() {
				@Override
				public void onTick(TickInfo tick, MT4 connection) {

					//  System.out.println("" + System.currentTimeMillis() + "> " + tick + " " + connection.accountBalance());
	            	HashMap<Integer,Double> activeOrdersProfitLoss = tick.orderPlMap;
	            	    Date time = tick.time;
	                    double ask = tick.ask;
	                    double bid = tick.bid;
	                   // double high =tick.hashCode();
	            	//System.out.println("time:"+ time + "ask:" + ask + "bid:" + bid );
				}
			});
		}
		 terminal.connect();

	}

	*/

}
