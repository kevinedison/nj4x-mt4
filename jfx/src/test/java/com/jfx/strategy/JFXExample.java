package com.jfx.strategy;

import com.jfx.ErrUnknownSymbol;
import com.jfx.MarketInfo;
import com.jfx.net.JFXServer;

/**
 * Created by roman on 27.06.2016.
 */
@SuppressWarnings("SpellCheckingInspection")
public class JFXExample extends Strategy {
    static {
        System.setProperty("nj4x_activation_key", "139080696");
    }

    @Override
    public void coordinate() {
        try {
            System.out.println(marketInfo_MODE_TIME(getSymbol()) + ": "+ marketInfo(getSymbol(), MarketInfo.MODE_BID));
        } catch (ErrUnknownSymbol errUnknownSymbol) {
            errUnknownSymbol.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        JFXServer.getInstance();
        Thread.sleep(100000);
    }
}
