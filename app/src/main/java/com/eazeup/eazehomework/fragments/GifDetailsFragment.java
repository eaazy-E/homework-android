package com.eazeup.eazehomework.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
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
    private FloatingActionButton mShareButton;
    private GiphyResponse.Data mData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set up the layout
        View rootView = inflater.from(getContext()).inflate(R.layout.gif_display_details, container, false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_details);
        }
        // Find the views
        mGifContainer = (LinearLayout) rootView.findViewById(R.id.gif_details_container);
        mSourceText = (TextView) rootView.findViewById(R.id.gif_details_source);
        mShareButton = (FloatingActionButton) rootView.findViewById(R.id.gif_details_share);
        // Set up the share button
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareImage();
            }
        });
        // Get the id of the GIF of which we're showing details
        Bundle args = getArguments();
        String id = args.getString(MainActivity.EXTRA_ID);
        // Show the gif details
        doGetDetails(id);
        return rootView;
    }

    /**
     * Get the details of the original gif
     * @param id The gif id
     */
    private void doGetDetails(String id) {
        GiphyServiceManager.get().getGifById(id, new Callback<GiphyDetailResponse>() {
            @Override
            public void onResponse(Call<GiphyDetailResponse> call, Response<GiphyDetailResponse> response) {
                if (response.isSuccessful()) {
                    mData = response.body().data;
                    // Calculate the screen width so that we can dynamically size the webview
                    Display display = getActivity().getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int screenWidth = size.x;
                    // Get the original height and width from the HTTP response
                    int gifOriginalWidth = Integer.parseInt(mData.images.original.width);
                    int gifOriginalHeight = Integer.parseInt(mData.images.original.height);
                    // Enlarge the webview to fit the screen width
                    // Future optimization: use animated gif supporting image library with native resize
                    int newWidth = screenWidth;
                    int newHeight = (screenWidth * gifOriginalHeight) / gifOriginalWidth;
                    // Load the gif into the webview
                    WebView gifWebView = new WebView(getContext());
                    StringBuffer htmlData = new StringBuffer().append("<html><body><img src=\'")
                            .append(mData.images.original.url)
                            .append("\' width=100%\' /></body></html>");
                    gifWebView.loadData(htmlData.toString(), "text/html", "UTF-8");
                    // Set the height and width and add it into the layout
                    gifWebView.setLayoutParams(new LinearLayout.LayoutParams(newWidth, newHeight));
                    gifWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                    mGifContainer.addView(gifWebView, 0);
                    // Get the domain from the source URL
                    String[] sourceArr = mData.source.split("/");
                    if (sourceArr != null && sourceArr.length >= 3) {
                        // Use domain only if possible
                        String domain = sourceArr[2];
                        if (domain.startsWith("www.")) {
                            domain = domain.substring(4);
                        }
                        mSourceText.setText(String.format(getString(R.string.gif_source), domain));
                    } else {
                        // Otherwise, use the full string
                        if (!TextUtils.isEmpty(mData.source)) {
                            mSourceText.setText(String.format(getString(R.string.gif_source), mData.source));
                        }
                    }
                } else {
                    Log.e(TAG, "Gif details call unsuccessful, status = " + response.code());
                    showGenericErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<GiphyDetailResponse> call, Throwable t) {
                Log.e(TAG, "Call to get gif details failed: " + t.getMessage());
                showGenericErrorDialog();
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

    private void shareImage() {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.gif_share_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, mData.bitlyGifUrl);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.gif_share_url_desc)));
    }

}
