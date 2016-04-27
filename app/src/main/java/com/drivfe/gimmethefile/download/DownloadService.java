package com.drivfe.gimmethefile.download;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.drivfe.gimmethefile.Config;
import com.drivfe.gimmethefile.R;
import com.drivfe.gimmethefile.activities.DownloadActivity;
import com.drivfe.gimmethefile.errors.BaseException;
import com.drivfe.gimmethefile.listeners.DownloadListener;
import com.drivfe.gimmethefile.models.MediaFileBucket;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class DownloadService extends Service implements DownloadListener {
    private static final String EXTRA_ENTRY = "EXTRA_ENTRY";
    public static Boolean isRunning = false;
    private final IBinder mBinder = new DownloadBinder();

    private MediaFileBucket mBucket;
    private DownloadEntry mEntry;
    private DownloadManager mDownloadManager = DownloadManager.getInstance();

    private NotificationCompat.Builder mDownloadNotification;
    private NotificationManager mNotificationManager;
    private Timer mTimer;

    private int mDownloadProgress = 0;

    private boolean newEntry = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Config.ACTION_START_DOWNLOAD_SERVICE)) {
            Timber.i("Starting");
            isRunning = true;

            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mEntry = (DownloadEntry) intent.getSerializableExtra(EXTRA_ENTRY);
            mBucket = mEntry.getBucket();

            startDownload();
        }

        if (intent.getAction().equals(Config.ACTION_STOP_DOWNLOAD_SERVICE)) {
            Timber.i("Stopping");
            mDownloadManager.stopDownload();
        }

        return START_STICKY;
    }

    private void updateNotification() {
        Timber.d("updateNotification");
        if (mDownloadManager.isPaused())
            mDownloadNotification.setContentInfo("Paused");
        else
            mDownloadNotification.setContentInfo(mDownloadProgress+"%");

        mDownloadNotification.setProgress(100, mDownloadProgress, false);
        mNotificationManager.notify(Config.NOTIFICATION_DOWNLOAD_ID, mDownloadNotification.build());
    }

    private void createErrorNotification(BaseException exc) {
        Intent notificationIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder finishedNotification = new NotificationCompat.Builder(this)
                .setContentTitle("Gimme The File")
                .setTicker("Download has failed")
                .setContentText("Your download has cancelled due to an error")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        mNotificationManager.notify(Config.NOTIFICATION_DOWNLOAD_ERROR_ID, finishedNotification.build());
    }

    private void createFinishedNotification(File file) {
        Intent notificationIntent = new Intent();
        notificationIntent.setAction(Intent.ACTION_VIEW);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.setData(Uri.fromFile(file));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder finishedNotification = new NotificationCompat.Builder(this)
                .setContentTitle(mBucket.title)
                .setTicker("Download has finished!")
                .setContentText("Download finished. Click here to open.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        mNotificationManager.notify(Config.NOTIFICATION_DOWNLOAD_FINISHED_ID, finishedNotification.build());
    }

    public void startDownload() {
        Timber.d("startDownload");
        mDownloadManager.addDownload(mEntry);
        mDownloadManager.registerListener(this);
        mDownloadManager.startDownload();
    }

    public void cancelDownload() {
        mDownloadManager.stopDownload();
    }

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        isRunning = false;
        mDownloadManager.unregisterListener(this);
    }

    @Override
    public void onDownloadStart() {
        Timber.d("onDownloadStart");

        // Starting notification

        Intent notificationIntent = new Intent(this, DownloadActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent cancelIntent = new Intent(this, DownloadService.class);
        cancelIntent.setAction(Config.ACTION_STOP_DOWNLOAD_SERVICE);
        PendingIntent cancelPendingIntent = PendingIntent.getService(this, 0, cancelIntent, 0);

        mDownloadNotification = new NotificationCompat.Builder(this)
                .setContentTitle("Gimme The File")
                .setTicker("Starting download...")
                .setContentText(mBucket.title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPendingIntent)
                .setOngoing(true);

        mNotificationManager.notify(Config.NOTIFICATION_DOWNLOAD_ID, mDownloadNotification.build());
        startForeground(Config.NOTIFICATION_DOWNLOAD_ID, mDownloadNotification.build());

        mTimer = new Timer("notification timer");
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateNotification();
            }
        }, 0, 1200);
    }

    @Override
    public void onDownloadCancelled() {
        Timber.d("onDownloadCancelled");

        mTimer.cancel();
        mNotificationManager.cancel(Config.NOTIFICATION_DOWNLOAD_ID);
        if (newEntry) {
            startDownload();
        }
        else
            stopSelf();
    }

    @Override
    public void onDownloadProgress(long progress, long total) {
        mDownloadProgress = (int) ((float) progress / (float) total*100f);
    }

    @Override
    public void onDownloadFinished(File file) {
        Timber.d("onDownloadFinished");

        if (mTimer!=null)
            mTimer.cancel();
        mNotificationManager.cancel(Config.NOTIFICATION_DOWNLOAD_ID);
        createFinishedNotification(file);
        stopSelf();
    }

    @Override
    public void onDownloadError(BaseException exc) {
        Timber.d("onDownloadError");

        if (mTimer != null)
            mTimer.cancel();
        mNotificationManager.cancel(Config.NOTIFICATION_DOWNLOAD_ID);
        createErrorNotification(exc);
        stopSelf();
    }

    @Override
    public void onDownloadPaused() {
        // TODO: handle pauses
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Timber.d("onRebind");

        // Wont work, Intent has no extras in onRebind for some reason.
        DownloadEntry entry = (DownloadEntry) intent.getSerializableExtra(EXTRA_ENTRY);
        if (!mEntry.getUrl().toString().equals(entry.getUrl().toString())) {
            Timber.d("A different entry has been passed by DownloadActivity. Restarting download of the new file");
            mEntry = entry;
            newEntry = true;
            cancelDownload();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static Intent newIntent(Context context, DownloadEntry entry) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(EXTRA_ENTRY, entry);
        return intent;
    }
}
