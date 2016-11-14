package com.jfx.strategy;

import java.io.IOException;

/**
 * It can be thrown by Strategy.connect when mt4 terminal can not establish socket connection to the mt4 broker or connection times out.
 * User: roman
 * Date: 31/03/14
 * Time: 14:23
 */
public class NJ4XInvalidUserNameOrPasswordException extends IOException {
    public NJ4XInvalidUserNameOrPasswordException(String message) {
        super(message);
    }
}
