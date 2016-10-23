package com.eazeup.eazehomework.model;

import android.support.annotation.NonNull;

/**
 * Item with which to populate the adapter of images
 */
public class GifItem {

    private final String mImageUrl;
    private final String mImageUrlSmall;
    private final String mShareUrl;
    private final String mId;

    public GifItem(@NonNull String imageUrl, @NonNull String imageUrlSmall, @NonNull String shareUrl,
                   @NonNull String id) {
        mImageUrl = imageUrl;
        mImageUrlSmall = imageUrlSmall;
        mShareUrl = shareUrl;
        mId = id;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getImageUrlSmall() {
        return mImageUrlSmall;
    }

    public String getShareUrl() {
        return mShareUrl;
    }

    public String getId() {
        return mId;
    }
}
