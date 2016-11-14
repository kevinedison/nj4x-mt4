package com.jfx.ts.net.ws.dto;

/**
 * Client session expired exception.
 * User: roman
 * Date: 26/06/2014
 * Time: 19:26
 */
public class Nj4xSessionExpiredException extends RuntimeException {
    public Nj4xSessionExpiredException() {
    }

    public Nj4xSessionExpiredException(String s) {
        super(s);
    }
}
