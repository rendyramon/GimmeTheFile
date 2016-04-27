package com.drivfe.gimmethefile.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MediaFileFormat implements Serializable {
    public String url;
    @SerializedName("ext")
    public String extension;
    public String format;
    public String protocol;
    public String width;
    public String height;
    public Long filesize;
    @SerializedName("http_headers")
    public HttpHeaders headers;
}
