package com.eazeup.eazehomework.service;

import com.google.gson.annotations.SerializedName;

public class GiphyResponse {

    public Data[] data;
    public Pagination pagination;
    public Meta meta;

    public static class Data {
        public String type;
        public String id;
        public String url;
        @SerializedName("bitly_gif_url")
        public String bitlyGifUrl;
        public Images images;
    }

    public static class Images {
        public GifOriginal original;
        @SerializedName("fixed_height_small_still")
        public GifOriginalFixedHeightSmallStill fixedHeightSmallStill;
    }

    public static class GifOriginal {
        public String url;
        public String width;
        public String height;
        public String size;
        public String mp4;
        @SerializedName("mp4_size")
        public String mp4Size;
        public String webp;
        @SerializedName("webp_size")
        public String webpSize;
    }

    public static class GifOriginalFixedHeightSmallStill {
        public String url;
        public String width;
        public String height;
    }

    public static class Pagination {
        public int count;
        public int offset;
    }

    public static class Meta {
        public int status;
        public String msg;
    }
}
