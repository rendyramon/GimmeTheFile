package com.drivfe.gimmethefile.listeners;

import com.drivfe.gimmethefile.errors.BaseException;

import java.io.File;

public interface DownloadListener {
    void onDownloadStart();
    void onDownloadCancelled();
    void onDownloadProgress(long progress, long total);
    void onDownloadFinished(File file);
    void onDownloadError(BaseException exc);
    void onDownloadPaused();
}