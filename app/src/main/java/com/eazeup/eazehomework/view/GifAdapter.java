package com.eazeup.eazehomework.view;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;

import com.eazeup.eazehomework.R;
import com.eazeup.eazehomework.model.GifItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GifAdapter extends RecyclerView.Adapter<GifAdapter.ViewHolder> {

    private List<GifItem> mGifs;
    private Context mContext;

    public GifAdapter(Context context, List<GifItem> gifs) {
        mContext = context;
        mGifs = gifs;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        if (position < mGifs.size()) {
            Picasso.with(mContext).load(mGifs.get(position).getImageUrlSmall()).into(viewHolder.image);
            viewHolder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    WebView gifWebView = new WebView(mContext);
                    gifWebView.loadUrl(mGifs.get(position).getImageUrl());
                    new AlertDialog.Builder(mContext).setView(gifWebView).show();
                }
            });
        } else {
            viewHolder.image.setImageResource(android.R.drawable.gallery_thumb);
        }
    }

    @Override
    public int getItemCount() {
        return mGifs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.item_grid_img);
        }
    }
}
