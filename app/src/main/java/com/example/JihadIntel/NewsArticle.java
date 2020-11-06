package com.example.JihadIntel;

import com.google.firebase.Timestamp;
import java.util.Date;

public class NewsArticle {
    private String Id;
    private String title;
    private String desc;
    private String time;
    private Timestamp timestamp;
    private String image_url;

    public Timestamp getTimestamp() {return timestamp;}

    public void setTimestamp(Timestamp timestamp) {this.timestamp = timestamp;}

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
