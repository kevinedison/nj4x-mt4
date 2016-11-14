/*
 * Metatrader Java (JFX) / .Net (NJ4X) library
 * Copyright (c) 2008-2014 by Gerasimenko Roman.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistribution of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The names "JFX" or "NJ4X" must not be used to endorse or
 * promote products derived from this software without prior
 * written permission. For written permission, please contact
 * roman.gerasimenko@nj4x.com
 *
 * 4. Products derived from this software may not be called "JFX" or
 * "NJ4X", nor may "JFX" or "NJ4X" appear in their name,
 * without prior written permission of Gerasimenko Roman.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE JFX CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package com.jfx.io;

import java.lang.reflect.Method;
import java.util.Enumeration;

/**
 * Apache Log4j helper.
 */
public class Log4JUtil {
    private static boolean isConfigured = false;
    private static int req = 0;
    public static boolean isConfigured() {
        try {
            if (!isConfigured && (req++ < 100 || req % 10 == 1)) {
                Object logger = callStaticMethod("org.apache.log4j.Logger", "getRootLogger");
                Object appenders = callObjectMethod(logger, "getAllAppenders");
                isConfigured = ((Enumeration)appenders).hasMoreElements();
                //
                if (!isConfigured) {
                    Object repo = callStaticMethod("org.apache.log4j.LogManager", "getLoggerRepository");
                    Enumeration currentCategories = (Enumeration) callObjectMethod(repo, "getCurrentCategories");
                    while (currentCategories.hasMoreElements()) {
                        logger = currentCategories.nextElement();
                        String name = (String) callObjectMethod(logger, "getName");
                        if (!name.startsWith("com.jfx")) {
                            return (isConfigured = true);
                        }
                    }
                }
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
            isConfigured = true;
        }
        //
        return isConfigured;
    }

    public static class Logger {
        Object log4jLogger;
    }

    public static Object callObjectMethod(Object o, String methodName, Object... params) throws RuntimeException {
        return callMethod(o, o.getClass().getName(), methodName, params);
    }

    public static Object callStaticMethod(String className, String methodName, Object... params) throws RuntimeException {
        return callMethod(null, className, methodName, params);
    }

    public static Object callMethod(Object o, String className, String methodName, Object... params) throws RuntimeException {
        try {
            Class aClass = Class.forName(className);
            Class[] paramClasses = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                paramClasses[i] = param.getClass();
            }
            Method aClassMethod = aClass.getMethod(methodName, paramClasses);
            return aClassMethod.invoke(o, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
