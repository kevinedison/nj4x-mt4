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

package com.jfx.net;

import com.jfx.io.CachedThreadFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: roman
 * Date: 12.05.12
 * Time: 8:04
 */
public class InprocessServer {
    public final static ExecutorService executorService;
    public final static ScheduledExecutorService scheduleService;
    private static final AtomicInteger sNo = new AtomicInteger(0);
    private static final AtomicInteger tNo = new AtomicInteger(0);
    static {
        ThreadFactory scheduleThreadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("NJ4X-CRON-" + sNo.incrementAndGet());
                return t;
            }
        };
        executorService = java.util.concurrent.Executors.newCachedThreadPool(new CachedThreadFactory("NJ4X-"));
        scheduleService = java.util.concurrent.Executors.newScheduledThreadPool(16, scheduleThreadFactory);
        map = null;
    }

    static ConcurrentHashMap<String, InprocessSocket> map;
    public static InprocessSocket connect(String id) {
        synchronized (InprocessServer.class) {
            if (map == null) {
                try {
                    map = new ConcurrentHashMap<String, InprocessSocket>();
                    System.setOut(new PrintStream(new FileOutputStream("logs/java.out"), true));
                    System.setErr(new PrintStream(new FileOutputStream("logs/java.err"), true));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Connecting [" + id + ']');
        InprocessSocket socket = new InprocessSocket();
        InprocessSocket inprocessSocket = map.put(id, socket);
        if (inprocessSocket != null) {
            try {
                inprocessSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        executorService.submit(new Greeter(socket, 0));
        return socket;
    }

}
