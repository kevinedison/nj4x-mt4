/*
 * Copyright (c) 2008-2014 by Gerasimenko Roman.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistribution of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 * 3. The name "JFX" must not be used to endorse or promote
 *     products derived from this software without prior written
 *     permission.
 *     For written permission, please contact roman.gerasimenko@gmail.com
 *
 * 4. Products derived from this software may not be called "JFX",
 *     nor may "JFX" appear in their name, without prior written
 *     permission of Gerasimenko Roman.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE JFX CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 */

package com.jfx.ts.jmx.mx4j;

import com.jfx.ts.jmx.JMXServer;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Method;

public class JMXServerImpl extends JMXServer {
    //
    private javax.management.MBeanServer server;
    private String configXml;
    private static final Class[] PARAMETER_TYPES = new Class[0];
    private static final Object[] ARGS = new Object[0];

    public JMXServerImpl() {
        configXml = System.getProperty(MX4J_CONFIG_XML);
        if (configXml == null) {
            throw new RuntimeException("System property " + MX4J_CONFIG_XML + " is not defined");
        }
        initMBeanServer();

    }

    private void initMBeanServer() {
        if (server == null) {
            if (new File(configXml).exists()) {
                try {
                    try {
                        Method m = Class.forName("java.lang.management.ManagementFactory").getMethod("getPlatformMBeanServer", PARAMETER_TYPES);
                        server = (MBeanServer) m.invoke(null, ARGS);
                    } catch (Throwable e) {
                        server = MBeanServerFactory.createMBeanServer();
                    }
//                    server = ManagementFactory.getPlatformMBeanServer();
                    Class cConfLoader = Class.forName("mx4j.tools.config.ConfigurationLoader");
                    Method mStartup = cConfLoader.getDeclaredMethod("startup", Reader.class);
                    Object loader = cConfLoader.newInstance();
//                    ConfigurationLoader loader = new ConfigurationLoader();
                    server.registerMBean(loader, ObjectName.getInstance("config:service=loader"));
                    Reader reader = null;
                    try {
                        reader = new BufferedReader(new FileReader(configXml));
                        mStartup.invoke(loader, reader);
//                        loader.startup(reader);
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Error instantiating JMXServerImpl MBeanServer", e);
                }
            } else {
//            throw new RuntimeException("System property " + MX4J_CONFIG_XML + "(" + configXml + ") does not point to an existing config file");
                System.err.println("ALERT: MBean config File does not exist [" + configXml + "] [" + MX4J_CONFIG_XML + ']');
            }
        }
    }

    public MBeanServer getMBeanServer() {
        if (server == null) {
            synchronized (this) {
                if (server == null) {
                    initMBeanServer();
                }
            }
        }
        return server;
    }
}
