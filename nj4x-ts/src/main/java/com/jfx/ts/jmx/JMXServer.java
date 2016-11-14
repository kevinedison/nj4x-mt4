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

package com.jfx.ts.jmx;

import org.w3c.dom.Element;

import javax.management.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Hashtable;

public class JMXServer {
    //
    public static final String JAVAX_MANAGEMENT_BUILDER_INITIAL = "javax.management.builder.initial";

    static {
        if (System.getProperty(JAVAX_MANAGEMENT_BUILDER_INITIAL) == null) {
            System.setProperty(JAVAX_MANAGEMENT_BUILDER_INITIAL, "mx4j.server.MX4JMBeanServerBuilder");
        }
    }

    public static final String JMX_IMPL_CLASS = "jmx.impl.class";
    // mx4j implementation
    public static final String MX4J_IMPL_CLASS = "com.jfx.ts.jmx.mx4j.JMXServerImpl";
    public static final String MX4J_CONFIG_XML = "mx4j.config.xml";

    private static JMXServer ourInstance;

    private JMXServer instance;
    public static final String GET_INSTANCE = "getInstance";
    private static final DummyMBeanServer DUMMY_M_BEAN_SERVER = new DummyMBeanServer();
    private static final Object[] INITARGS = new Object[0];

    public static JMXServer getInstance() throws RuntimeException {
        if (ourInstance == null) {
            synchronized (JMXServer.class) {
                if (ourInstance == null/* && System.getProperty(JMX_IMPL_CLASS) != null*/) {
                    ourInstance = new JMXServer(System.getProperty(JMX_IMPL_CLASS));
                }
            }
        }
        return ourInstance;
    }

    public JMXServer() throws RuntimeException {
    }

    private JMXServer(String implClassName) throws RuntimeException {
        if (implClassName != null && implClassName.length() > 0) {
            try {
                Class c_mx4j = Class.forName(implClassName);
                Constructor m_getInstance = c_mx4j.getDeclaredConstructors()[0];
                m_getInstance.setAccessible(true);
                instance = (JMXServer) m_getInstance.newInstance(INITARGS);
            } catch (Throwable e) {
                throw e instanceof RuntimeException ? ((RuntimeException) e) : new RuntimeException(e);
            }
        }
    }

    protected StringBuffer getMBeanBody(ArrayList imports, String mBeanName, Element mbean) {
        return null;
    }

    Hashtable mbeans = new Hashtable();

    public DynamicMBean getMBean(String name) {
        if (name == null) {
            return null;
        } else {
            try {
                ObjectName beanName = new ObjectName(name);
                return getMBean(beanName);
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public DynamicMBean getMBean(ObjectName beanName) {
        return beanName == null ? null : (DynamicMBean) mbeans.get(beanName);
    }

    public MBeanServer getMBeanServer() {
        return instance == null ? DUMMY_M_BEAN_SERVER : instance.getMBeanServer();
    }

    public static MBeanAttributeInfo[] mergeAttrInfo(MBeanAttributeInfo[] i1, MBeanAttributeInfo[] i2) {
        if (i1 == null) {
            return i2;
        } else if (i2 == null) {
            return i1;
        } else {
            MBeanAttributeInfo[] im = new MBeanAttributeInfo[i1.length + i2.length];
            System.arraycopy(i1, 0, im, 0, i1.length);
            System.arraycopy(i2, 0, im, i1.length, i2.length);
            return im;
        }
    }

    public static MBeanOperationInfo[] mergeOpInfo(MBeanOperationInfo[] i1, MBeanOperationInfo[] i2) {
        if (i1 == null) {
            return i2;
        } else if (i2 == null) {
            return i1;
        } else {
            MBeanOperationInfo[] im = new MBeanOperationInfo[i1.length + i2.length];
            System.arraycopy(i1, 0, im, 0, i1.length);
            System.arraycopy(i2, 0, im, i1.length, i2.length);
            return im;
        }
    }
}
