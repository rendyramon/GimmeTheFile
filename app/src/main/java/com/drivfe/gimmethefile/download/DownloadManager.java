package com.drivfe.gimmethefile.download;

import android.os.AsyncTask;

import com.drivfe.gimmethefile.errors.BaseException;
import com.drivfe.gimmethefile.errors.OtherException;
import com.drivfe.gimmethefile.errors.UnauthorizedException;
import com.drivfe.gimmethefile.listeners.DownloadListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class DownloadManager {
    private static DownloadManager instance = null;

    private DownloadEntry mEntry;
    private ArrayList<DownloadListener> mListeners = new ArrayList<>(2);
    private DownloadListenerDelegate mListenerDelegate = new DownloadListenerDelegate(mListeners);
    private FileDownloadTask mTask;

    public static DownloadManager getInstance() {
        if (instance == null)
            instance = new DownloadManager();

        return instance;
    }

    private DownloadManager() {
    }

    public void addDownload(DownloadEntry entry) {
        mEntry = entry;
    }

    public void registerListener(DownloadListener listener) {
        mListeners.add(listener);
        Timber.d(mListeners.size() + " listeners");
    }

    public void unregisterListener(DownloadListener listener) {
        mListeners.remove(listener);
        Timber.d(mListeners.size() + " listeners");
    }

    public DownloadEntry getEntry() {
        return mEntry;
    }

    public void startDownload() {
        if (mTask != null)
            mTask.cancel(true);
        mTask = new FileDownloadTask(mEntry);
        mTask.execute();
    }

    public void pauseDownload() {
        mTask.pause();
    }

    public void stopDownload() {
        mTask.cancel(true);
    }

    public boolean isPaused() {
        return mTask.isPaused();
    }

    private class FileDownloadTask extends AsyncTask<Void, Void, Boolean> {
        private final int BUFFER_SIZE = 1024 * 8;
        private long mContentLength, mDownloaded;
        private File mTempFile;
        private BaseException exc = null;
        private DownloadEntry mEntry;
        private boolean mPaused;
        private boolean mFinished;

        public FileDownloadTask(DownloadEntry entry) {
            mEntry = entry;
            mTempFile = new File(mEntry.getDest().getAbsolutePath() + ".unfinished");
            mDownloaded = 0;
            mFinished = false;
        }

        public boolean isFinished() {
            return mFinished;
        }

        public boolean isPaused() {
            return mPaused;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(mEntry.getUrl())
                    .headers(Headers.of(mEntry.getFormat().headers.toMap()))
                    .build();

            try {
                Response response = client.newCall(request).execute();

                int statusCode = response.code();
                if (statusCode == 403) {
                    throw new UnauthorizedException("The app is not authorized to access this link",
                            "Some websites, like Youtube, have videos " +
                                    "that are restricted to a certain country, age group or only logged in accounts." +
                                    "This app does not currently support any methods to bypass these restrictions."
                            , 403);
                }

                InputStream inputStream = response.body().byteStream();
                BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(mTempFile, false), BUFFER_SIZE);

                byte[] buff = new byte[BUFFER_SIZE];
                mContentLength = response.body().contentLength();

                mListenerDelegate.onDownloadStart();

                while (true) {
                    int readAmount = inputStream.read(buff);
                    if (readAmount == -1) {
                        break;
                    }

                    fileOutputStream.write(buff, 0, readAmount);
                    mDownloaded += readAmount;
                    publishProgress();

                    if (isCancelled()) {
                        return false;
                    }
                }

                response.body().close();
                fileOutputStream.close();
                inputStream.close();
                return mDownloaded == mContentLength;
            } catch (IOException e) {
                exc = new OtherException("There was a problem accessing the website");
            } catch (UnauthorizedException e) {
                exc = e;
            }

            return false;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            mListenerDelegate.onDownloadProgress(mDownloaded, mContentLength);
        }

        @Override
        protected void onPostExecute(Boolean finished) {
            super.onPostExecute(finished);
            Timber.i("onPostExecute: download finished: " + finished);
            Timber.i("onPostExecute: downloaded: " + mDownloaded + "/" + mContentLength);
            Timber.i(mTempFile.length() + " bytes");

            mEntry.setProgress(mDownloaded);
            mEntry.setLength(mContentLength);
            mFinished = true;

            if (exc != null) {
                mListenerDelegate.onDownloadError(exc);
                mTempFile.delete();
            } else {
                mTempFile.renameTo(mEntry.getDest());
                mListenerDelegate.onDownloadFinished(mEntry.getDest());
            }
        }

        @Override
        protected void onCancelled(Boolean pause) {
            super.onCancelled(pause);
            mFinished = true;

            mEntry.setProgress(mDownloaded);
            mEntry.setLength(mContentLength);

            if (mPaused) {
                Timber.d("onCancelled: Download paused");
                Timber.d("onCancelled: downloaded: " + mDownloaded + "/" + mContentLength);
                mListenerDelegate.onDownloadPaused();
            } else {
                Timber.d("onCancelled: Download cancelled");
                mTempFile.delete();
                mListenerDelegate.onDownloadCancelled();
            }
        }

        public void pause() {
            // TODO: handle pauses
            mPaused = true;
            this.cancel(true);
        }
    }

    private class DownloadListenerDelegate implements DownloadListener {
        private List<DownloadListener> mListeners;

        public DownloadListenerDelegate(List<DownloadListener> listener) {
            mListeners = listener;
        }

        @Override
        public void onDownloadStart() {
            for (DownloadListener l : mListeners)
                l.onDownloadStart();
        }

        @Override
        public void onDownloadCancelled() {
            for (DownloadListener l : mListeners)
                l.onDownloadCancelled();
        }

        @Override
        public void onDownloadProgress(long progress, long total) {
            for (DownloadListener l : mListeners)
                l.onDownloadProgress(progress, total);
        }

        @Override
        public void onDownloadFinished(File file) {
            for (DownloadListener l : mListeners)
                l.onDownloadFinished(file);
        }

        @Override
        public void onDownloadError(BaseException exc) {
            for (DownloadListener l : mListeners)
                l.onDownloadError(exc);
        }

        @Override
        public void onDownloadPaused() {
            for (DownloadListener l : mListeners)
                l.onDownloadPaused();
        }
    }
}