package com.jfx.ts.io;

import java.io.*;
import java.net.URL;

/**
 * Created by roman on 13.08.2015.
 */
public class LibrariesUtil {
    public static boolean IS_OK = false;
    public static boolean isWindows;
    public static boolean isX64;
    public static String LIBS_DIR;

    static {
        String osName = System.getProperty("os.name");
        isWindows = osName.toLowerCase().contains("windows");
        String arch = System.getProperty("os.arch");
        isX64 = arch.contains("amd64") || arch.contains("x64");
        LIBS_DIR = System.getProperty("program_data_dir", "C:\\ProgramData\\nj4x") + "\\bin";
//        LIBS_DIR = System.getProperty("user.dir");
    }

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
        String libFileName = LIBS_DIR + File.separator + dllFileName;
        //
        URL nativeLibraryUrl = LibrariesUtil.class.getResource(dllFileName);
        if (nativeLibraryUrl != null) {
            // native library found within JAR, extract and load
//                    final File libFile = File.createTempFile(dllFileName, ".lib");
//                    libFile.deleteOnExit(); // just in case, does not work, ShutdownHook does not work as well
            final File libFile = new File(libFileName);
            //
            final InputStream in = nativeLibraryUrl.openStream();
            final OutputStream out = new BufferedOutputStream(new FileOutputStream(libFile));
            //
            int len;
            byte[] buffer = new byte[160000];
            while ((len = in.read(buffer)) > -1)
                out.write(buffer, 0, len);
            out.close();
            in.close();
            //
            System.load(libFile.getAbsolutePath());
            IS_OK = true;
        } // nativeLibraryUrl exists
    }
}
