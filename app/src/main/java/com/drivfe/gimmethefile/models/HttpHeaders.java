package com.drivfe.gimmethefile.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class HttpHeaders implements Serializable {
    @SerializedName("Accept-Language")
    String acceptLanguage;
    @SerializedName("User-Agent")
    String userAgent;
    @SerializedName("Accept-Encoding")
    String acceptEncoding;
    @SerializedName("Accept-Charset")
    String acceptCharset;
    @SerializedName("Accept")
    String accept;

    public Map<String, String> toMap() {
        Map<String, String> headers = new HashMap<>(5);
        headers.put("Accept-Language", acceptLanguage);
        headers.put("User-Agent", userAgent);
        headers.put("Accept-Encoding", acceptEncoding);
        headers.put("Accept-Charset", acceptCharset);
        headers.put("Accept", accept);

        return headers;
    }
}
