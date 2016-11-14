package com.jfx.ts.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
* Created by roman on 26-Mar-15.
*/
public class LogsFileVisitor implements FileVisitor<Path> {
    private Path startPath;
    private final ZipOutputStream zos;
    private boolean packDLLs;

    public LogsFileVisitor(Path startPath, ZipOutputStream zos) {
        this.startPath = startPath;
        this.zos = zos;
    }

    public LogsFileVisitor(Path startPath, ZipOutputStream zos, boolean packDLLs) {
        this.startPath = startPath;
        this.zos = zos;
        this.packDLLs = packDLLs;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
//        String dirName = dir.getName(dir.getNameCount() - 1).toString();
//        return dirName.startsWith("zero_term") ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String fileName = file.getName(file.getNameCount() - 1).toString();
        String fnLowerCase = fileName.toLowerCase();
        String dnLowerCase = file.getNameCount() > 2 ? file.getName(file.getNameCount() - 2).toString().toLowerCase() : "";
        if (fnLowerCase.contains(".log")
                || fnLowerCase.endsWith(".srv") && dnLowerCase.equals("config")
                || fnLowerCase.endsWith(".ini") && dnLowerCase.equals("config")
                || fnLowerCase.endsWith(".chr") && dnLowerCase.equals("default")
                || fnLowerCase.endsWith(".xml")
                || packDLLs && fnLowerCase.startsWith("mt") && fnLowerCase.endsWith(".dll")
                || packDLLs && (fnLowerCase.startsWith("jfx") || fnLowerCase.startsWith("wnds")) && (fnLowerCase.endsWith(".ex4") || fnLowerCase.endsWith(".ex5"))
                ) {
            addToZipFile(file.toString(), getZipEntryPath(file), zos);
        }
        return FileVisitResult.CONTINUE;
    }

    private String getZipEntryPath(Path file) {
        StringBuilder sb = new StringBuilder();
        int nameCount = file.getNameCount();
        for (int i = startPath.getNameCount() - 1; i < nameCount; i++) {
            if (sb.length() > 0) {
                sb.append('/');
            }
            sb.append(file.getName(i));
        }
        return sb.toString();
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//        String dirName = dir.getName(dir.getNameCount() - 1).toString();
//        packDLLs = !(packDLLs && (dirName.startsWith("zero_term") || dirName.startsWith("custom_term")));
        return FileVisitResult.CONTINUE;
    }

    private void addToZipFile(String fileName, String path, ZipOutputStream zos) throws FileNotFoundException, IOException {
        File file = new File(fileName);

        if (TS.LOGGER.isDebugEnabled()) {
            TS.LOGGER.debug("Writing '" + file + "' (" + file.length() + " bytes) to zip file");
        }

        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(path);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[10240];
//        int bytesWritten = 0;
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
//            bytesWritten += length;
        }

        zos.closeEntry();
        fis.close();

//        if (TS.LOGGER.isDebugEnabled()) {
//            TS.LOGGER.debug("Written '" + file + "' (" + bytesWritten + " bytes) to zip file");
//        }
    }
}
