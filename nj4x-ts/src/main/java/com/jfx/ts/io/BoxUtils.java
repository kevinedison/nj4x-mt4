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

package com.jfx.ts.io;

import java.io.*;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
//这个类应该是做操作系统操作的类
public class BoxUtils {
    public static boolean isX64;
    public static boolean isWindows;

    static {
        String osName = System.getProperty("os.name");
        isWindows = osName.toLowerCase().contains("windows");
        String arch = System.getProperty("os.arch");
        if (arch.contains("amd64") || arch.contains("x64")) {
            if (isWindows && !loadEmbeddedLibrary("box_utils_x64")) {
                System.loadLibrary("box_utils_x64");
            }
            isX64 = true;
        } else {
            if (isWindows && !loadEmbeddedLibrary("box_utils")) {
                System.loadLibrary("box_utils");
            }
            isX64 = false;
        }
    }

    private static boolean loadEmbeddedLibrary(final String dllFileName) {
        boolean usingEmbedded = false;
        URL nativeLibraryUrl = BoxUtils.class.getResource(dllFileName + ".dll");
        if (nativeLibraryUrl != null) {
            // native library found within JAR, extract and load
            try {
                final File libFile = File.createTempFile(dllFileName, ".lib");

                final InputStream in = nativeLibraryUrl.openStream();
                final OutputStream out = new BufferedOutputStream(new FileOutputStream(libFile));

                int len = 0;
                byte[] buffer = new byte[8192];
                while ((len = in.read(buffer)) > -1)
                    out.write(buffer, 0, len);
                out.close();
                in.close();

                System.load(libFile.getAbsolutePath());

                usingEmbedded = true;

                Files.walkFileTree(libFile.getParentFile().toPath(), new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes basicFileAttributes) throws IOException {
                        if (!file.equals(libFile.toPath())) {
                            String fileName = file.getName(file.getNameCount() - 1).toString();
                            if (fileName.startsWith(dllFileName) && fileName.endsWith(".lib")) {
                                Files.delete(file);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                });

            } catch (IOException ignore) {
                // mission failed, do nothing
            }
        } // nativeLibraryUrl exists

        return usingEmbedded;
    }

    private static native long boxid();

    private static native long getBinaryType(String binPath);

    public static long BOXID = boxid();

    public static void main(String[] args) {
        long x = getBinaryType(args[0]);
        System.out.println("bin type=" + x);
    }
}


