package com.impervious.chatapp.Model;

public class Users {

    private String uid;
    private String name;
    private String imgUrl;
    private String status;
    private String search;


    public Users(String uid, String name, String imgUrl, String status, String search) {
        this.uid = uid;
        this.name = name;
        this.imgUrl = imgUrl;
        this.status = status;
        this.search = search;
    }


    public Users() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

}
