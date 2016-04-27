package com.drivfe.gimmethefile;

public class Config {
    public static final String BASE_URL = BuildConfig.BASE_URL;

    public static String DOWNLOAD_FOLDER = ""; // set in GimmeApplication

    // Download service
    public static final String ACTION_START_DOWNLOAD_SERVICE = "com.drivfe.gimmethefile.action.startdownloadservice";
    public static final String ACTION_STOP_DOWNLOAD_SERVICE = "com.drivfe.gimmethefile.action.stopdownloadservice";
    public static final int NOTIFICATION_DOWNLOAD_ID = 100;
    public static final int NOTIFICATION_DOWNLOAD_FINISHED_ID = 101;
    public static final int NOTIFICATION_DOWNLOAD_ERROR_ID = 102;
}