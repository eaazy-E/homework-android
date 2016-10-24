package com.eazeup.eazehomework;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.eazeup.eazehomework.fragments.GifGridFragment;

public class MainActivity extends AppCompatActivity implements GifGridFragment.OnSearchCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String SEARCH_TYPE = "search_type";
    public static final String SEARCH_QUERY = "search_query";
    public static final String EXTRA_TRENDING = "trending";
    public static final String EXTRA_SEARCH = "search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GifGridFragment fragment = GifGridFragment.newInstance(this);
        Bundle args = new Bundle();
        args.putString(SEARCH_TYPE, EXTRA_TRENDING);
        fragment.setArguments(args);
        String simpleName = GifGridFragment.class.getSimpleName();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                fragment, simpleName).addToBackStack(simpleName).commit();
    }

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
}
