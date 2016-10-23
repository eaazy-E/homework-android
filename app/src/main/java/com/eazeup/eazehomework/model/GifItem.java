package com.eazeup.eazehomework.model;

/**
 * Item with which to populate the adapter of images
 */
public class GifItem {

    private String mImageUrl;
    private String mShareUrl;
    private String mId;

    public GifItem(String imageUrl, String shareUrl, String id) {
        mImageUrl = imageUrl;
        mShareUrl = shareUrl;
        mId = id;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getShareUrl() {
        return mShareUrl;
    }

    public String getId() {
        return mId;
    }
}
