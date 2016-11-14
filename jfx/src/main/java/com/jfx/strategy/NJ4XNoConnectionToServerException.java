package com.jfx.strategy;

import java.io.IOException;

/**
 * It can be thrown by Strategy.connect when Terminal Server can not establish socket connection to the mt4 broker.
 * User: roman
 * Date: 31/03/14
 * Time: 14:23
 */
public class NJ4XNoConnectionToServerException extends IOException {
    public NJ4XNoConnectionToServerException(String message) {
        super(message);
    }
}
