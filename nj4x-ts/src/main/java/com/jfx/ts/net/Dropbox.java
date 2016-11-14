package com.jfx.ts.net;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.users.FullAccount;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by roman on 02-Apr-15.
 */
public class Dropbox {
    private static Dropbox instance = new Dropbox();

    public static Dropbox getInstance() {
        return instance;
    }

    private DbxClientV2 dbxClient;

    private Dropbox() {
        try {
            DbxRequestConfig dbxRequestConfig = new DbxRequestConfig("nj4x-ts-dbx", Locale.getDefault().toString());
            dbxClient = new DbxClientV2(dbxRequestConfig, "BRtpG4RuVtAAAAAAAAAABYbnHhg2-zYYFd65PdLZu0VlivWhZ-2Lzee6AmH-O1YJ");
            TS.LOGGER.info("Dropbox connection status: OK, userId=" + dbxClient.users().getCurrentAccount().getName());
        } catch (Exception e) {
            TS.LOGGER.error("Dropbox connection error", e);
        }
//        System.out.println("Dropbox Account Name: "
//                + dbxClient.getAccountInfo().displayName);
    }

    public long size() throws DbxException {
        long dropboxSize = 0;
        if (dbxClient != null) {
            FullAccount dbxAccountInfo = dbxClient.users().getCurrentAccount();
            dropboxSize = Long.MAX_VALUE;//dbxAccountInfo.quota.total;
            TS.LOGGER.info("Dropbox userId=" + dbxAccountInfo.getName().getDisplayName() + ", size=" + dropboxSize);
        }
        return dropboxSize;
    }

    public void upload(String fileName) throws DbxException, IOException {
        File file = new File(fileName);
        upload(file);
    }

    public void upload(File file) throws DbxException, IOException {
        if (dbxClient != null) {
            try (InputStream fis = new BufferedInputStream(new FileInputStream(file))) {
                TS.LOGGER.info("Uploading file " + file.getAbsolutePath() + ", size=" + file.length());
//                DbxEntry.File uploadedFile = dbxClient.uploadFile("/" + file.getName(), DbxWriteMode.add(), file.length(), fis);
                FileMetadata uploadedFile = dbxClient
                        .files().uploadBuilder("/" + file.getName())
                        .uploadAndFinish(fis);
                //, DbxWriteMode.add(), file.length(), fis);
                TS.LOGGER.info("File " + file.getAbsolutePath() + " has been uploaded");
//                String sharedUrl = dbxClient.createShareableUrl("/" + file.getName());
//                System.out.println("Uploaded: " + uploadedFile.toString() + " URL " + sharedUrl);
            }
        }
    }

    public void mkdir(String folderName) throws DbxException {
        if (dbxClient != null) {
//            dbxClient.createFolder("/" + folderName);
            dbxClient.files().createFolder("/" + folderName);
            TS.LOGGER.info("Dir " + folderName + " has been created.");
        }
    }

/*
    public List<DbxEntry> ls(String folderPath) throws DbxException {
        if (dbxClient == null) {
            return new ArrayList<>();
        } else {
//            DbxEntry.WithChildren listing = dbxClient.getMetadataWithChildren(folderPath);
            ListFolderResult listing = dbxClient.files().listFolder(folderPath);
            return listing.children;
        }
    }
*/

/*
    public void download(String dbxFilePath, String localFilePath) throws DbxException, IOException {
        if (dbxClient != null) {
            try (FileOutputStream outputStream = new FileOutputStream(localFilePath)) {
                DbxEntry.File downloadedFile = dbxClient.getFile("/" + dbxFilePath, null, outputStream);
                TS.LOGGER.info("File " + dbxFilePath + " has been downloaded to " + localFilePath);
//                System.out.println("Metadata: " + downloadedFile.toString());
            }
        }
    }
*/

    public static void main(String[] args) throws IOException, DbxException {
        Dropbox dropbox = Dropbox.getInstance();
        System.out.println("Dropbox Size: " + dropbox.size() + " GB");
        dropbox.upload("C:\\projects\\tmp_projects\\DropboxJavaApi\\src\\happy.png");
        try {
            dropbox.mkdir("tutorial");
        } catch (DbxException e) {
            e.printStackTrace();
        }
//        for (DbxEntry e : dropbox.ls("/")) {
//            System.out.println(e.toString());
//        }
//        dropbox.download("happy.png", "c:\\tmp\\happy.png");
    }
}
