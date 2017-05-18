package com.myproject.mobileplayer.bean;

import java.io.Serializable;

/**
 * Created by lxj on 2017/3/7.
 * Cursor数据对应的实体bean
 */

public class MediaItem implements Serializable{
    public String name;
    public long size;
    public long duration;
    public String path;
    public String artist;//路径


}
