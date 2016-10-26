package com.eazeup.eazehomework.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
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
import com.eazeup.eazehomework.view.EndlessRecyclerViewScrollListener;
import com.eazeup.eazehomework.view.GifAdapter;
import com.eazeup.eazehomework.view.GridAutofitLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
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
    private String mSearchString;
    private EndlessRecyclerViewScrollListener mScrollListener;
    private SwipeRefreshLayout mSwipeContainer;

    private static final int COL_WIDTH = 300;

    public static GifGridFragment newInstance() {
        GifGridFragment fragment = new GifGridFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getContext();
        mCallback = (OnSearchCallback) getActivity();
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
        mSwipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.main_swipe_container);

        // Setup the FAB for performing giphy search
        setupSearchButton();

        // Initialize the empty grid
        setupGrid();

        // Set up the swipe refresh
        setupSwipeRefresh();

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
     * Set up the swipe-to-refresh functionality
     */
    private void setupSwipeRefresh() {
        if (mSwipeContainer != null) {
            mSwipeContainer.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),
                    getResources().getColor(R.color.colorPrimaryDark));
            mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mGifAdapter.clear();
                    if (TextUtils.isEmpty(mSearchString)) {
                        doGetTrending();
                    } else {
                        doSearch(mSearchString);
                    }
                }
            });
        }
    }

    /**
     * Initial call to set up the grid of gifs
     */
    private void setupGrid() {
        mGifAdapter = new GifAdapter(mContext, mCallback, Collections.EMPTY_LIST);
        GridLayoutManager gridLayoutManager = new GridAutofitLayoutManager(mContext, COL_WIDTH);
        mGifsView.setLayoutManager(gridLayoutManager);
        mGifsView.setAdapter(mGifAdapter);
        mScrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!TextUtils.isEmpty(mSearchString)) {
                    doSearch(mSearchString, page);
                } else {
                    doGetTrending(page);
                }
            }
        };
        mGifsView.addOnScrollListener(mScrollListener);
    }

    /**
     * Show the gifs returned from a network call in the grid.
     *
     * @param giphyItems An array of gifs returned from a giphy API call.
     */
    private void showGiphyResults(GiphyResponse.Data[] giphyItems) {
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
        // Set up the adapter
        if (mGifAdapter == null) {
            mGifAdapter = new GifAdapter(mContext, mCallback, gifs);
        } else {
            mGifAdapter.addItems(gifs);
        }
    }

    /**
     * Call the GET trending giphy endpoint from offset 0 and display the results
     */
    private void doGetTrending() {
        this.doGetTrending(0);
    }

    /**
     * Call the GET trending giphy endpoint and display the results
     *
     * @param offset results offset
     */
    private void doGetTrending(int offset) {
        GiphyServiceManager.get().getTrending(offset, new Callback<GiphyResponse>() {
            @Override
            public void onResponse(Call<GiphyResponse> call, Response<GiphyResponse> response) {
                mSwipeContainer.setRefreshing(false);
                if (response.isSuccessful()) {
                    showGiphyResults(response.body().data);
                } else {
                    Log.e(TAG, "Get trending gifs unsuccessful, error code = " + response.code());
                    showGenericErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<GiphyResponse> call, Throwable t) {
                mSwipeContainer.setRefreshing(false);
                Log.e(TAG, "Giphy get trending failed: " + t.getMessage());
                showGenericErrorDialog();
            }
        });
    }

    /**
     * Call the GET search giphy endpoint from offset 0 and display the results.
     *
     * @param searchKey The search query string
     */
    private void doSearch(String searchKey) {
        mSearchString = searchKey;
        this.doSearch(mSearchString, 0);
    }

    /**
     * Call the GET search giphy endpoint and display the results.
     *
     * @param searchKey The search query string
     * @param offset    The results page offset
     */
    private void doSearch(String searchKey, int offset) {
        mSearchString = searchKey;
        GiphyServiceManager.get().getSearch(mSearchString, offset, new Callback<GiphyResponse>() {
            @Override
            public void onResponse(Call<GiphyResponse> call, Response<GiphyResponse> response) {
                mSwipeContainer.setRefreshing(false);
                if (response.isSuccessful()) {
                    showGiphyResults(response.body().data);
                } else {
                    Log.e(TAG, "Search unsuccessful, error code = " + response.code());
                    showGenericErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<GiphyResponse> call, Throwable t) {
                mSwipeContainer.setRefreshing(false);
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
     * Callbacks for the Activity to implement on search and on gif click
     */
    public interface OnSearchCallback {
        void onSearch(String searchKey);

        void onDetails(String id);
    }

}
