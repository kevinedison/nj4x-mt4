package com.jfx.ts.io;

import java.io.*;
import java.net.URL;

/**
 * Created by roman on 13.08.2015.
 */
public class LibrariesUtil {
    /**
     * The constant IS_OK.
     */
    public static boolean IS_OK = false;
    /**
     * The constant isWindows.
     */
    public static boolean isWindows;  //true
    /**
     * The constant isX64.
     */
    public static boolean isX64;  // true
    /**
     * The constant LIBS_DIR.
     */
    public static String LIBS_DIR;  //     C:\ProgramData\nj4x\bin

    static {
        String osName = System.getProperty("os.name");
        isWindows = osName.toLowerCase().contains("windows");
        String arch = System.getProperty("os.arch");
        isX64 = arch.contains("amd64") || arch.contains("x64");
        LIBS_DIR = System.getProperty("program_data_dir", "C:\\ProgramData\\nj4x") + "\\bin";
//        LIBS_DIR = System.getProperty("user.dir");
    }

    /**
     * 这个不知道什么意思，把target下的dll的内容写到programData文件夹下的DLL？如果shi复制的话也不需要这么费劲啊
     *
     * @exception IOException the io exception
     */
    public static void initEmbeddedLibraries() throws IOException {
        String dllFileName;
        if (isX64) {
            dllFileName = "PSUtils_x64.dll";
        } else {
            dllFileName = "PSUtils.dll";
        }
        File _libFileDir = new File(LIBS_DIR);
        if (!_libFileDir.exists()) {
            _libFileDir.mkdirs();
        }
        String libFileName = LIBS_DIR + File.separator + dllFileName;   //     C:\ProgramData\nj4x\bin\PSUtils_x64.dll
        //
        URL nativeLibraryUrl = LibrariesUtil.class.getResource(dllFileName);   // file:/E:/CodeWorkSpace/Java/nj4x-src-2.6.2/nj4x-ts/target/classes/com/jfx/ts/io/PSUtils_x64.dll  这个shijava调用dll的URL吧， 这个机制还不是很清楚，要找资料学习下，但是这个URL和libFileName的路径不一样
        if (nativeLibraryUrl != null) {
            // native library found within JAR, extract and load
//                    final File libFile = File.createTempFile(dllFileName, ".lib");
//                    libFile.deleteOnExit(); // just in case, does not work, ShutdownHook does not work as well
            final File libFile = new File(libFileName);   //     C:\ProgramData\nj4x\bin\PSUtils_x64.dll
            //
            final InputStream in = nativeLibraryUrl.openStream();
            final OutputStream out = new BufferedOutputStream(new FileOutputStream(libFile));
            //
            int len;
            byte[] buffer = new byte[160000];
            while ((len = in.read(buffer)) > -1)
                out.write(buffer, 0, len);  //写入dll文件
            out.close();
            in.close();
            //
            System.load(libFile.getAbsolutePath()); //加载PSUtil_X64.dll这个dll
            IS_OK = true;
        } // nativeLibraryUrl exists
    }
}
