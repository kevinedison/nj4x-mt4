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

import javax.management.*;
import javax.management.loading.ClassLoaderRepository;
import java.io.ObjectInputStream;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: roman
 * Date: 8/5/2009
 * Time: 13:24:22
 */
public class DummyMBeanServer implements MBeanServer {
    public ObjectInstance createMBean(String string, ObjectName objectName) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
        return null;
    }

    public ObjectInstance createMBean(String string, ObjectName objectName, ObjectName objectName1) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
        return null;
    }

    public ObjectInstance createMBean(String string, ObjectName objectName, Object[] objects, String[] strings) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
        return null;
    }

    public ObjectInstance createMBean(String string, ObjectName objectName, ObjectName objectName1, Object[] objects, String[] strings) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
        return null;
    }

    public ObjectInstance registerMBean(Object object, ObjectName objectName) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        return null;
    }

    public void unregisterMBean(ObjectName objectName) throws InstanceNotFoundException, MBeanRegistrationException {

    }

    public ObjectInstance getObjectInstance(ObjectName objectName) throws InstanceNotFoundException {
        return null;
    }

    public Set queryMBeans(ObjectName objectName, QueryExp queryExp) {
        return null;
    }

    public Set queryNames(ObjectName objectName, QueryExp queryExp) {
        return null;
    }

    public boolean isRegistered(ObjectName objectName) {
        return false;
    }

    public Integer getMBeanCount() {
        return null;
    }

    public Object getAttribute(ObjectName objectName, String string) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
        return null;
    }

    public AttributeList getAttributes(ObjectName objectName, String[] strings) throws InstanceNotFoundException, ReflectionException {
        return null;
    }

    public void setAttribute(ObjectName objectName, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {

    }

    public AttributeList setAttributes(ObjectName objectName, AttributeList attributeList) throws InstanceNotFoundException, ReflectionException {
        return null;
    }

    public Object invoke(ObjectName objectName, String string, Object[] objects, String[] strings) throws InstanceNotFoundException, MBeanException, ReflectionException {
        return null;
    }

    public String getDefaultDomain() {
        return null;
    }

    public String[] getDomains() {
        return new String[0];
    }

    public void addNotificationListener(ObjectName objectName, NotificationListener notificationListener, NotificationFilter notificationFilter, Object object) throws InstanceNotFoundException {

    }

    public void addNotificationListener(ObjectName objectName, ObjectName objectName1, NotificationFilter notificationFilter, Object object) throws InstanceNotFoundException {

    }

    public void removeNotificationListener(ObjectName objectName, ObjectName objectName1) throws InstanceNotFoundException, ListenerNotFoundException {

    }

    public void removeNotificationListener(ObjectName objectName, ObjectName objectName1, NotificationFilter notificationFilter, Object object) throws InstanceNotFoundException, ListenerNotFoundException {

    }

    public void removeNotificationListener(ObjectName objectName, NotificationListener notificationListener) throws InstanceNotFoundException, ListenerNotFoundException {

    }

    public void removeNotificationListener(ObjectName objectName, NotificationListener notificationListener, NotificationFilter notificationFilter, Object object) throws InstanceNotFoundException, ListenerNotFoundException {

    }

    public MBeanInfo getMBeanInfo(ObjectName objectName) throws InstanceNotFoundException, IntrospectionException, ReflectionException {
        return null;
    }

    public boolean isInstanceOf(ObjectName objectName, String string) throws InstanceNotFoundException {
        return false;
    }

    public Object instantiate(String string) throws ReflectionException, MBeanException {
        return null;
    }

    public Object instantiate(String string, ObjectName objectName) throws ReflectionException, MBeanException, InstanceNotFoundException {
        return null;
    }

    public Object instantiate(String string, Object[] objects, String[] strings) throws ReflectionException, MBeanException {
        return null;
    }

    public Object instantiate(String string, ObjectName objectName, Object[] objects, String[] strings) throws ReflectionException, MBeanException, InstanceNotFoundException {
        return null;
    }


    public ObjectInputStream deserialize(ObjectName objectName, byte[] bytes) throws InstanceNotFoundException, OperationsException {
        return null;
    }


    public ObjectInputStream deserialize(String string, byte[] bytes) throws OperationsException, ReflectionException {
        return null;
    }


    public ObjectInputStream deserialize(String string, ObjectName objectName, byte[] bytes) throws InstanceNotFoundException, OperationsException, ReflectionException {
        return null;
    }

    public ClassLoader getClassLoaderFor(ObjectName objectName) throws InstanceNotFoundException {
        return null;
    }

    public ClassLoader getClassLoader(ObjectName objectName) throws InstanceNotFoundException {
        return null;
    }

    public ClassLoaderRepository getClassLoaderRepository() {
        return null;
    }
}
