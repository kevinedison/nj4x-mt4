package com.jfx.strategy;

import java.io.IOException;

/**
 * It can be thrown by Strategy.connect when Terminal Server can not create mt4 terminal process.
 * User: roman
 * Date: 31/03/14
 * Time: 14:23
 */
public class NJ4XMaxNumberOfTerminalsExceededException extends IOException {
    public NJ4XMaxNumberOfTerminalsExceededException(String message) {
        super(message);
    }
}
