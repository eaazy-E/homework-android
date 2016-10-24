package com.eazeup.eazehomework.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.eazeup.eazehomework.MainActivity;
import com.eazeup.eazehomework.R;
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

public class GifGridFragment extends Fragment {

    private static String TAG = GifGridFragment.class.getSimpleName();

    private Context mContext;
    private RecyclerView mGifsView;
    private GifAdapter mGifAdapter;
    private FloatingActionButton mFab;
    private OnSearchCallback mCallback;

    private static final int COL_WIDTH = 300;

    public static GifGridFragment newInstance(OnSearchCallback callback) {
        GifGridFragment fragment = new GifGridFragment();
        fragment.mCallback = callback;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getContext();
        View rootView = inflater.from(mContext).inflate(R.layout.gif_display_grid, container, false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        // By default, initialize the action bar title to "trending"
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_trending);
        }

        // Find the views
        mGifsView = (RecyclerView) rootView.findViewById(R.id.main_gifs);
        mFab = (FloatingActionButton) rootView.findViewById(R.id.main_search);

        // Setup the FAB for performing giphy search
        setupSearchButton();

        // Get the fragment's arguments so that we know whether to show trending gifs,
        // or do a search
        Bundle args = getArguments();
        String searchType = args.getString(MainActivity.SEARCH_TYPE);
        if (searchType.equals(MainActivity.EXTRA_SEARCH)) {
            String searchKey = args.getString(MainActivity.SEARCH_QUERY);
            // We are doing a search
            if (!TextUtils.isEmpty(searchKey)) {
                if (actionBar != null) {
                    // Update the action bar title
                    actionBar.setTitle(searchKey);
                }
                // Perform the search and display the gifs
                doSearch(searchKey);
            } else {
                // Search string is null, show trending
                doGetTrending();
            }
        } else {
            // Show trending gifs
            doGetTrending();
        }
        return rootView;
    }

    /**
     * Setup the FAB for initiating a search
     */
    private void setupSearchButton() {
        if (mFab != null) {
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle)
                            .setTitle(R.string.search_title)
                            .setView(R.layout.dialog_search_box)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Dialog dialog = (Dialog) dialogInterface;
                                    // Set an EditText for entering the search query
                                    EditText searchInput = (EditText) dialog.findViewById(R.id.dialog_search_edittext);
                                    String searchKey = searchInput.getText().toString();
                                    if (!TextUtils.isEmpty(searchKey)) {
                                        // User entered a search string, perform search
                                        mCallback.onSearch(searchKey);
                                    } else {
                                        // User did not enter a search string,
                                        // simply dismiss the dialog.
                                        dialogInterface.dismiss();
                                    }
                                }
                            }).show();
                }
            });
        }
    }

    /**
     * Show the gifs returned from a network call in the grid.
     * @param giphyItems An array of gifs returned from a giphy API call.
     */
    protected void showGiphyResults(GiphyResponse.Data[] giphyItems) {
        // Digest the relevant items from the raw response
        // into a list to populate the adapter
        List<GifItem> gifs = new ArrayList<>();
        for (int i = 0; i < giphyItems.length; i++) {
            GiphyResponse.Data curGiphy = giphyItems[i];
            if (!TextUtils.isEmpty(curGiphy.images.fixedHeightSmallStill.url)) {
                gifs.add(new GifItem(curGiphy.images.original.url,
                        curGiphy.images.fixedHeightSmallStill.url,
                        curGiphy.bitlyGifUrl, curGiphy.id));
            }
        }
        // Create the adapter
        mGifAdapter = new GifAdapter(mContext, gifs);
        // Set up the list
        mGifsView.setLayoutManager(new GridAutofitLayoutManager(mContext, COL_WIDTH));
        mGifsView.setAdapter(mGifAdapter);
    }

    /**
     * Call the GET trending giphy endpoint and display the results
     */
    private void doGetTrending() {
        GiphyServiceManager.get().getTrending(new Callback<GiphyResponse>() {
            @Override
            public void onResponse(Call<GiphyResponse> call, Response<GiphyResponse> response) {
                if (response.isSuccessful()) {
                    showGiphyResults(response.body().data);
                } else {
                    Log.e(TAG, "Get trending gifs unsuccessful, error code = " + response.code());
                    showGenericErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<GiphyResponse> call, Throwable t) {
                Log.e(TAG, "Giphy get trending failed: " + t.getMessage());
                showGenericErrorDialog();
            }
        });
    }

    /**
     * Call the GET search giphy endpoint and display the results.
     * @param searchKey The search query string
     */
    private void doSearch(String searchKey) {
        GiphyServiceManager.get().getSearch(searchKey, new Callback<GiphyResponse>() {
            @Override
            public void onResponse(Call<GiphyResponse> call, Response<GiphyResponse> response) {
                if (response.isSuccessful()) {
                    showGiphyResults(response.body().data);
                } else {
                    Log.e(TAG, "Search unsuccessful, error code = " + response.code());
                    showGenericErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<GiphyResponse> call, Throwable t) {
                Log.e(TAG, "Giphy search failed: " + t.getMessage());
                showGenericErrorDialog();
            }
        });
    }

    /**
     * Utility method to show a generic error dialog
     */
    protected void showGenericErrorDialog() {
        new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle)
                .setMessage(R.string.error_generic)
                .show();
    }

    /**
     * Callback for the Activity to implement on search
     */
    public interface OnSearchCallback {
        void onSearch(String searchKey);
    }

}
