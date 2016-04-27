package com.drivfe.gimmethefile.models;

import java.io.Serializable;
import java.util.ArrayList;

public class MediaFileBucket implements Serializable {
    public String id;
    public String title;
    public String thumbnail;
    public String extractor;
    public String description;
    public String uploader;
    public String view_count;
    public String fps;
    public Object duration;
    public ArrayList<MediaFileFormat> formats = new ArrayList<>(1);
}
