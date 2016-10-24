package com.eazeup.eazehomework.fragments;

import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eazeup.eazehomework.MainActivity;
import com.eazeup.eazehomework.R;
import com.eazeup.eazehomework.service.GiphyDetailResponse;
import com.eazeup.eazehomework.service.GiphyResponse;
import com.eazeup.eazehomework.service.GiphyServiceManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GifDetailsFragment extends Fragment {

    private static final String TAG = GifGridFragment.class.getSimpleName();

    private LinearLayout mGifContainer;
    private TextView mSourceText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.from(getContext()).inflate(R.layout.gif_display_details, container, false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_details);
        }
        mGifContainer = (LinearLayout) rootView.findViewById(R.id.gif_details_container);
        mSourceText = (TextView) rootView.findViewById(R.id.gif_details_source);
        Bundle args = getArguments();
        String id = args.getString(MainActivity.EXTRA_ID);
        doGetDetails(id);
        return rootView;
    }

    private void doGetDetails(String id) {
        GiphyServiceManager.get().getGifById(id, new Callback<GiphyDetailResponse>() {
            @Override
            public void onResponse(Call<GiphyDetailResponse> call, Response<GiphyDetailResponse> response) {
                if (response.isSuccessful()) {
                    GiphyResponse.Data data = response.body().data;
                    // Calculate the screen width so that we can dynamically size the webview
                    Display display = getActivity().getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int screenWidth = size.x;
                    // Get the original height and width from the HTTP response
                    int gifOriginalWidth = Integer.parseInt(data.images.original.width);
                    int gifOriginalHeight = Integer.parseInt(data.images.original.height);
                    // Enlarge the webview to fit the screen width
                    // Future optimization: use animated gif supporting image library with native resize
                    int newWidth = screenWidth;
                    int newHeight = (screenWidth * gifOriginalHeight) / gifOriginalWidth;
                    // Load the gif into the webview
                    WebView gifWebView = new WebView(getContext());
                    gifWebView.loadUrl(data.images.original.url);
                    // Set the height and width and add it into the layout
                    gifWebView.setLayoutParams(new LinearLayout.LayoutParams(newWidth, newHeight));
                    mGifContainer.addView(gifWebView, 0);
                    // Get the domain from the source URL
                    String[] sourceArr = data.source.split("/");
                    if (sourceArr != null && sourceArr.length >= 3) {
                        // Use domain only if possible
                        String domain = sourceArr[2];
                        if (domain.startsWith("www.")) {
                            domain = domain.substring(4);
                        }
                        mSourceText.setText(String.format(getString(R.string.gif_source), domain));
                    } else {
                        // Otherwise, use the full string
                        mSourceText.setText(String.format(getString(R.string.gif_source), data.source));
                    }
                } else {
                    Log.e(TAG, "Gif details call unsuccessful, status = " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GiphyDetailResponse> call, Throwable t) {
                Log.e(TAG, "Call to get gif details failed: " + t.getMessage());
            }
        });
    }

    /**
     * Utility method to show a generic error dialog
     */
    protected void showGenericErrorDialog() {
        new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle)
                .setMessage(R.string.error_generic)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().onBackPressed();
                    }
                })
                .show();
    }

}
