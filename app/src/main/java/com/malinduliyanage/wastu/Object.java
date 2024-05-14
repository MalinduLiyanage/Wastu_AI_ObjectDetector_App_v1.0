package com.malinduliyanage.wastu;

public class Object {

    private String  imgPath;
    private String path;
    private String timestamp;
    public Object(String imgPath, String path, String timestamp) {
        this.imgPath = imgPath;
        this.path = path;
        this.timestamp = timestamp;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
