package com.eazeup.eazehomework;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.eazeup.eazehomework.fragments.GifDetailsFragment;
import com.eazeup.eazehomework.fragments.GifGridFragment;

public class MainActivity extends AppCompatActivity implements GifGridFragment.OnSearchCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String SEARCH_TYPE = "search_type";
    public static final String SEARCH_QUERY = "search_query";
    public static final String EXTRA_DETAILS = "search_details";
    public static final String EXTRA_TRENDING = "trending";
    public static final String EXTRA_SEARCH = "search";
    public static final String EXTRA_ID = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GifGridFragment fragment = GifGridFragment.newInstance(this);
        Bundle args = new Bundle();
        // Initially show trending gifs
        args.putString(SEARCH_TYPE, EXTRA_TRENDING);
        fragment.setArguments(args);
        String simpleName = GifGridFragment.class.getSimpleName();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                fragment, simpleName).addToBackStack(simpleName).commit();
    }

    // Implementation of gif search callback
    @Override
    public void onSearch(String searchKey) {
        GifGridFragment fragment = GifGridFragment.newInstance(this);
        Bundle args = new Bundle();
        args.putString(SEARCH_TYPE, EXTRA_SEARCH);
        args.putString(SEARCH_QUERY, searchKey);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment,
                searchKey).addToBackStack(searchKey).commit();
    }

    // Implementation of gif view details callback
    @Override
    public void onDetails(String id) {
        GifDetailsFragment fragment = new GifDetailsFragment();
        Bundle args = new Bundle();
        args.putString(SEARCH_TYPE, EXTRA_DETAILS);
        args.putString(EXTRA_ID, id);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, id)
                .addToBackStack(id).commit();
    }
}
