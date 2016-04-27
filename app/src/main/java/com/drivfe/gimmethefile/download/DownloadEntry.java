package com.drivfe.gimmethefile.download;

import com.drivfe.gimmethefile.models.MediaFileBucket;
import com.drivfe.gimmethefile.models.MediaFileFormat;
import com.drivfe.gimmethefile.utilities.HelperUtils;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

public class DownloadEntry implements Serializable {
    private URL url;
    private File dest;
    private MediaFileBucket bucket;
    private MediaFileFormat format;

    private long progress = 0;
    private long length = -1;

    public DownloadEntry(MediaFileBucket bkt, MediaFileFormat frm) {
        bucket = bkt;
        format = frm;
        dest = HelperUtils.getPathWithFilename(bucket.title + "." + format.extension);
        try {
            url = new URL(format.url);
        } catch (MalformedURLException e) {
            Timber.e(e, "DownloadEntry");
        }
    }

    public boolean isFinished() {
        return progress == length && progress > 0;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getLength() {
        return length;
    }

    public long getProgress() {
        return progress;
    }

    public File getDest() {
        return dest;
    }

    public MediaFileFormat getFormat() {
        return format;
    }

    public MediaFileBucket getBucket() {
        return bucket;
    }

    public URL getUrl() {
        return url;
    }
}
