package com.jfx;

public class DemoAccount {
    public final static Broker MT_4_SERVER = new Broker("demo1-amsterdam.fxpro.com");
    public final static String MT_4_USER = "6796186";
    public final static String MT_4_PASSWORD = "dpkk4hg";

//    public static final String JFX_ACTIVATION_KEY = System.getProperty("nj4x_activation_key", "155857917");
    public static final String JFX_ACTIVATION_KEY = System.getProperty("nj4x_activation_key", "1879909835");
    public static final String NJ4X_MT5_ACTIVATION_KEY = System.getProperty("nj4x_mt5_activation_key", "325261026");

//    public static final Broker MT_4_SERVER = /**/new Broker("InstaForex-Demo.com");
//    public static final String MT_4_USER = /**/"62589107";
//    public static final String MT_4_PASSWORD = /**/"oin5vsv";

    static {
        System.setProperty("nj4x_activation_key", JFX_ACTIVATION_KEY);
        System.setProperty("nj4x_mt5_activation_key", NJ4X_MT5_ACTIVATION_KEY);
//        System.setProperty("jfx_server_host", "192.168.0.204");
//        System.setProperty("jfx_server_port", "17342"); // for extended debug info
    }
}
