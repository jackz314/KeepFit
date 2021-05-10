package com.jackz314.keepfit.models;

public class SearchResult {
    private boolean isUser;
    private User user;
    private Media media;

    public SearchResult(User user) {
        this.isUser = true;
        this.user = user;
    }

    public SearchResult(Media media) {
        this.isUser = false;
        this.media = media;
    }

    public boolean isUser() {
        return isUser;
    }

    public User getUser() {
        return user;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "isUser=" + isUser +
                ", user=" + user +
                ", media=" + media +
                '}';
    }
}
