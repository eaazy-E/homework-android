package com.eazeup.eazehomework;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.eazeup.eazehomework.model.GifItem;
import com.eazeup.eazehomework.service.GiphyResponse;
import com.eazeup.eazehomework.service.GiphyServiceManager;
import com.eazeup.eazehomework.view.GifAdapter;
import com.eazeup.eazehomework.view.GridAutofitLayoutManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mGifsView;
    private GifAdapter mGifAdapter;
    private FloatingActionButton mFab;

    private static final int COL_WIDTH = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_trending);
        }
        mGifsView = (RecyclerView) findViewById(R.id.main_gifs);
        mFab = (FloatingActionButton) findViewById(R.id.main_search);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText searchBox = new EditText(MainActivity.this);
                searchBox.setHint(R.string.search_hint);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.search_title)
                        .setView(searchBox)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                GiphyServiceManager.get().getSearch("cannabis", new Callback<GiphyResponse>() {
                                    @Override
                                    public void onResponse(Call<GiphyResponse> call, Response<GiphyResponse> response) {
                                        if (response.isSuccessful()) {
                                            // Digest the relevant items from the raw response
                                            // into a list to populate the adapter
                                            List<GifItem> gifs = new ArrayList<>();
                                            GiphyResponse.Data[] giphyItems = response.body().data;
                                            for (int i = 0; i < giphyItems.length; i++) {
                                                GiphyResponse.Data curGiphy = giphyItems[i];
                                                if (!TextUtils.isEmpty(curGiphy.images.fixedHeightSmallStill.url)) {
                                                    gifs.add(new GifItem(curGiphy.images.original.url,
                                                            curGiphy.images.fixedHeightSmallStill.url,
                                                            curGiphy.bitlyGifUrl, curGiphy.id));
                                                }
                                            }
                                            // Create the adapter
                                            mGifAdapter = new GifAdapter(MainActivity.this, gifs);
                                            // Set up the list
                                            mGifsView.setLayoutManager(new GridAutofitLayoutManager(MainActivity.this, COL_WIDTH));
                                            mGifsView.setAdapter(mGifAdapter);
                                        } else {
                                            Log.e(TAG, "Unsuccessful: " + response.code());
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<GiphyResponse> call, Throwable t) {
                                        Log.e(TAG, "Failure: " + t.getMessage());
                                    }
                                });
                            }
                        }).show();
            }
        });
        getTrendingGifs();
    }

    private void getTrendingGifs() {
        GiphyServiceManager.get().getTrending(new Callback<GiphyResponse>() {
            @Override
            public void onResponse(Call<GiphyResponse> call, Response<GiphyResponse> response) {
                if (response.isSuccessful()) {
                    // Digest the relevant items from the raw response
                    // into a list to populate the adapter
                    List<GifItem> gifs = new ArrayList<>();
                    GiphyResponse.Data[] giphyItems = response.body().data;
                    for (int i = 0; i < giphyItems.length; i++) {
                        GiphyResponse.Data curGiphy = giphyItems[i];
                        if (!TextUtils.isEmpty(curGiphy.images.fixedHeightSmallStill.url)) {
                            gifs.add(new GifItem(curGiphy.images.original.url,
                                    curGiphy.images.fixedHeightSmallStill.url,
                                    curGiphy.bitlyGifUrl, curGiphy.id));
                        }
                    }
                    // Create the adapter
                    mGifAdapter = new GifAdapter(MainActivity.this, gifs);
                    // Set up the list
                    mGifsView.setLayoutManager(new GridAutofitLayoutManager(MainActivity.this, COL_WIDTH));
                    mGifsView.setAdapter(mGifAdapter);
                } else {
                    Log.e(TAG, "Unsuccessful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GiphyResponse> call, Throwable t) {
                Log.e(TAG, "Failure: " + t.getMessage());
            }
        });
    }
}
