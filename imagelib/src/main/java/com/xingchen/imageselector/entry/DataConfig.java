package com.xingchen.imageselector.entry;

import java.io.Serializable;
import java.util.ArrayList;

public class DataConfig implements Serializable {
    private int position;
    private ArrayList<MediaData> dataSources;

    public DataConfig(int position, ArrayList<MediaData> dataSources) {
        this.position = position;
        this.dataSources = dataSources;
    }
}
