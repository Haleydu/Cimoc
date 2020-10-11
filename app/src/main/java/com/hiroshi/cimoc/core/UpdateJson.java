package com.hiroshi.cimoc.core;

public class UpdateJson {

    private String versionName;
    private int versionCode;
    private String content;
    private String md5;
    private String url;

    public String getVersionName() {
        return versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMd5() {
        return md5;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

}

