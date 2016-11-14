package com.jfx.ts.net;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class GDriveAccess {
    //
    public static final String P_SRV = "g_drive_srv";
    public static final String P_ZERO_TERM = "g_drive_zero_term";
    public static final String P_ZERO_TERM_MT5 = "g_drive_zero_term_mt5";
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME =
            "NJ4X-TS";
    /**
     * Directory to store user credentials for this application.
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            TS.JFX_HOME_CONFIG, ".credentials/google-drive");
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();
    /**
     * Global instance of the scopes required by this quickstart.
     */
    private static final List<String> SCOPES =
            Arrays.asList(DriveScopes.DRIVE_READONLY, DriveScopes.DRIVE_METADATA_READONLY);
    private static final GDriveAccess instance = new GDriveAccess();
    //
    private Drive drive;
    /**
     * Global instance of the HTTP transport.
     */
    private HttpTransport httpTransport;
    private User owner;
    private File root;
    private List<GDriveFile> topFolders;
    private ConcurrentHashMap<String, DownloadSetup> downloads = new ConcurrentHashMap<>();
    private boolean checking;
    private long lastSyncTime = -1;

    private GDriveAccess() {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            showError("Google transport initialization error", e);
        }
        //
        TS.scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                int nextSyncInSeconds = nextSyncInSeconds();
                if (nextSyncInSeconds > 0) {
                    TS.scheduledExecutorService.schedule(this, 1/*Math.min(nextSyncInSeconds, 5)*/, TimeUnit.SECONDS);
                    return;
                }
                try {
                    checking = true;
                    boolean somethingDone = false;
                    for (DownloadSetup ds : downloads.values()) {
                        somethingDone |= ds.run();
                    }
                    if (somethingDone) {
                        TS.LOGGER.info("Google Drive: All Downloads Complete!");
                    }
                    checking = false;
                } finally {
                    lastSyncTime = System.currentTimeMillis();
                    TS.scheduledExecutorService.schedule(this, 1/*Math.max(nextSyncInSeconds(), 5)*/, TimeUnit.SECONDS);
                }
            }
        }, 5, TimeUnit.SECONDS);
        //
        if (GDriveAccess.hasStoredAuthentications())
            initDrive();
    }

    public static GDriveAccess getInstance() {
        return instance;
    }

    private static void showError(String msg, Exception error) {
        String message = msg + " " + error;
        TS.LOGGER.error(message, error);
//        JOptionPane.showMessageDialog(null, message, "Error Message", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean hasStoredAuthentications() {
        return new java.io.File(DATA_STORE_DIR + "/authenticated").exists();
    }

    public static void main(String[] args) throws IOException {
        GDriveAccess.getInstance().initDrive();
    }

    public static String formatTime(Date date) {
        //2012-06-04T12:00:00
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(date);
    }

    public static void newConfig(String pSrv, String id) {
        TS.removeConfigurationValues(pSrv + "_last_time");
        TS.setConfigurationValue(pSrv, id);
    }

    public static void removeConfig(String pSrv) {
        TS.removeConfigurationValues(pSrv + "_last_time");
        TS.removeConfigurationValues(pSrv);
    }

    public int nextSyncInSeconds() {
        int updateInterval = getUpdateInterval();
        long i = updateInterval - (System.currentTimeMillis() - lastSyncTime) / 1000;
        return isValid() && lastSyncTime > 0 ?
                (checking ? 0 :
                        i > 0 ? (int) i : -1)
                : -1;
    }

    private int getUpdateInterval() {
        return Integer.parseInt(TS.getConfigurationValue("cloud_update_interval", "60"));
    }

    private List<GDriveFile> _listFolders(HashMap<String, List<File>> service, String parentFolderId, String prefix) {
        List<GDriveFile> res = new ArrayList<>();
        List<File> files = service.get(parentFolderId);
        if (files != null && files.size() != 0) {
            for (File file : files) {
                String path = prefix + file.getName();
                res.add(new GDriveFile(path, file));
                res.addAll(_listFolders(service, file.getId(), path + "/"));
            }
        }
        return res;
    }

    public User getOwner() {
        return owner;
    }

    public boolean isValid() {
        return drive != null && root != null && owner != null;
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     */
    private Credential authorize() {
        try {
            // Load client secrets.
            InputStream in =
                    GDriveAccess.class.getResourceAsStream("/client_secret.json");
            GoogleClientSecrets clientSecrets =
                    GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow =
                    new GoogleAuthorizationCodeFlow.Builder(
                            httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                            .setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR))
                            .setAccessType("offline")
                            .build();
            return new AuthorizationCodeInstalledApp(
                    flow, new LocalServerReceiver()).authorize("user");
        } catch (IOException e1) {
            String msg = "Google Drive Authorization error: ";
            showError(msg, e1);
            return null;
        }
    }

    public void disconnectAndClearCredentials() {
        //noinspection ConstantConditions
        for (java.io.File f : DATA_STORE_DIR.listFiles()) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
        TS.LOGGER.info("Google Drive: disconnected, credentials have been removed.");
        drive = null;
        topFolders = null;
    }

    /**
     * Build and return an authorized Drive client service.
     *
     * @return an authorized Drive client service
     */
    public Drive initDrive() {
        if (drive == null) {
            Credential credential = authorize();
            drive = credential == null ? null : new Drive.Builder(
                    httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            if (drive != null) {
                try {
                    topFolders = null;
                    root = null;
                    owner = null;
                    //
                    root = drive.files().get("root").setFields("id,name,owners").execute();
                    owner = root.getOwners().get(0);
                    TS.LOGGER.info("Google Drive: connected to " + owner.getEmailAddress());
                    try {
                        new FileOutputStream(DATA_STORE_DIR + "/authenticated").close();
                    } catch (IOException ignore) {
                    }
                } catch (Exception e) {
                    showError("Google Drive access error", e);
                }
            }
        }
        return drive;
    }

    public List<GDriveFile> listFolders() {
        if (topFolders == null && isValid()) {
            try {
                // Build a new authorized API client service.
                String rootId = root.getId();
                FileList result = drive.files().list()
                        .setQ("mimeType = 'application/vnd.google-apps.folder'")
                        .setFields("files(id, name, parents, trashed)")
                        .setPageSize(1000)
                        .execute();
                List<File> files = result.getFiles();
                HashMap<String, List<File>> folders = new HashMap<>();
                for (File folder : files) {
                    List<String> parents = folder.getParents();
                    if (parents != null && !folder.getTrashed()) {
                        String key = parents.get(0);
                        List<File> v = folders.get(key);
                        if (v == null) {
                            folders.put(key, v = new ArrayList<>());
                        }
                        v.add(folder);
                    }
                }
                if (folders.size() == 0) {
                    TS.LOGGER.error("No folders found at connected Google Drive (" + owner.getEmailAddress() + ")");
//                    JOptionPane.showMessageDialog(null,
//                            "No folders found at connected Google Drive (" + owner.getEmailAddress() + ")",
//                            owner.getDisplayName(),
//                            JOptionPane.INFORMATION_MESSAGE);
                }
                //
                topFolders = _listFolders(folders, rootId, "");
                topFolders.add(0, new GDriveFile("<not set>", null));
            } catch (IOException e) {
                showError("Google Drive Browse error: ", e);
            }
        }
        //
        return topFolders;
    }

    public synchronized void setupDownloadFolder(String gDriveProperty, java.io.File localDir, String... validSuffix) {
        downloads.put(gDriveProperty, new DownloadSetup(gDriveProperty, localDir, validSuffix));
    }

    private boolean checkSuffix(File file, String[] validSuffix) {
        String fn = file.getName().toLowerCase();
        for (String s : validSuffix) {
            if (fn.endsWith(s.toLowerCase())) return true;
        }
        return false;
    }

//    private String getLastConfigTime(String pSrv) {
//        return TS.getConfigurationValue(pSrv + "_last_time", formatTime(new Date(0)));
//    }

    private void setLastConfigTime(String pSrv) {
        TS.setConfigurationValue(pSrv + "_last_time", formatTime(new Date()));
    }

    private class DownloadSetup {
        String gDriveProperty;
        java.io.File localDir;
        String[] validSuffix;
        private boolean isRunning;

        public DownloadSetup(String gDriveProperty, java.io.File localDir, String[] validSuffix) {
            this.gDriveProperty = gDriveProperty;
            this.localDir = localDir;
            this.localDir.mkdirs();
            this.validSuffix = validSuffix;
        }

        @Override
        public String toString() {
            return "" + hashCode() + " " + gDriveProperty + "(" + localDir + ")";
        }

        public boolean run() {
            if (!isValid() || isRunning) {
                if (TS.LOGGER.isTraceEnabled()) {
                    TS.LOGGER.trace("Skip GDrive Download job (" + this + "): valid?=" + isValid() + ", running?=" + isRunning);
                }
                return false;
            }
            isRunning = true;
            boolean somethingDone = false;
            try {
                String driveFolderId = TS.getConfigurationValue(gDriveProperty, null);
                if (driveFolderId != null) {
//                    String modifiedTime = getLastConfigTime(gDriveProperty);
                    Drive.Files.List list = drive.files().list()
                            .setQ("'" + driveFolderId + "' in parents")
//                            .setQ("'" + driveFolderId + "' in parents and modifiedTime >= '" + modifiedTime + "'")
                            .setFields("nextPageToken,files(id,name,modifiedTime,size,mimeType,trashed)")
                            .setPageSize(100);
                    FileList result = list.execute();
                    while (true) {
                        List<File> files = result.getFiles();
                        if (files != null && files.size() != 0) {
                            for (File file : files) {
                                if (downloads.get(gDriveProperty) != this) {
                                    if (TS.LOGGER.isDebugEnabled()) {
                                        TS.LOGGER.debug("Exit GDrive Download job (" + this + "): new setup detected");
                                    }
                                    return somethingDone; // cancel job on new setup
                                }
                                if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                                    if (TS.LOGGER.isTraceEnabled()) {
                                        TS.LOGGER.trace("" + this + " :: skip folder: " + file.getName());
                                    }
                                    continue; // folder
                                }
                                if (file.getTrashed()) {
                                    if (TS.LOGGER.isDebugEnabled()) {
                                        TS.LOGGER.debug("" + this + " :: skip trashed file: " + file.getName());
                                    }
                                    continue; // removed
                                }
                                if (validSuffix == null
                                        || checkSuffix(file, validSuffix)) {
                                    java.io.File localFile = new java.io.File(localDir, file.getName());
                                    //
                                    if (localFile.exists()
                                            && localFile.lastModified() > file.getModifiedTime().getValue()
                                            && localFile.length() == file.getSize()
                                            ) {
                                        if (TS.LOGGER.isTraceEnabled()) {
                                            TS.LOGGER.trace("" + this + " :: skip existing file: " + file.getName());
                                        }
                                        continue;
                                    }
                                    //
                                    try {
                                        String tmpFileName = System.getProperty("java.io.tmpdir") + "/" + file.getName();
                                        TS.LOGGER.debug("" + this + " :: downloading file: " + file.getName()
                                                + " (" + fileInfo(file) + ") to " + localDir.getAbsolutePath() + " via " + tmpFileName);
                                        OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFileName));
                                        drive.files().get(file.getId()).executeMediaAndDownloadTo(out);
                                        out.close();
                                        Files.move(
                                                Paths.get(tmpFileName),
                                                localFile.toPath(),
                                                StandardCopyOption.REPLACE_EXISTING
                                        );
                                        somethingDone = true;
                                        TS.LOGGER.info("Google Drive: " + localDir.getAbsolutePath() + "\\" + file.getName() + "(" + fileInfo(file) + ") downloaded successfully");
                                    } catch (Throwable e) {
                                        TS.LOGGER.error("" + this + " :: file download error: " + file.getName(), e);
                                        //showError("Google Drive File (" + file.getName() + ") Download error", e);
                                    }
                                } else {
                                    if (TS.LOGGER.isTraceEnabled()) {
                                        TS.LOGGER.trace("" + this + " :: skip invalid suffix: " + file.getName());
                                    }
                                }
                            }
                            setLastConfigTime(gDriveProperty);
                        } else {
                            if (TS.LOGGER.isDebugEnabled()) {
                                TS.LOGGER.debug("Exit GDrive Download job (" + this + "): no more files");
                            }
                            break;
                        }
                        //
                        String nextPageToken = result.getNextPageToken();
                        if (nextPageToken == null) {
                            break;
                        }
                        result = list.setPageToken(nextPageToken).execute();
                    }
                } else {
                    if (TS.LOGGER.isTraceEnabled()) {
                        TS.LOGGER.trace("Skip GDrive Download job (" + this + "): not configured");
                    }
                }
            } catch (Exception e) {
                showError("Google Drive Folder Download error", e);
            } finally {
                isRunning = false;
            }
            return somethingDone;
        }

        private String fileInfo(File file) {
            return file.getId() + ", time=" + file.getModifiedTime() + ", sz=" + file.getSize();
        }
    }

    public class GDriveFile {
        String display;
        File driveFile;

        public GDriveFile(String display, File gDriveFile) {
            this.display = display;
            this.driveFile = gDriveFile;
        }

        public File getDriveFile() {
            return driveFile;
        }

        @Override
        public String toString() {
            return display;
        }
    }
}
