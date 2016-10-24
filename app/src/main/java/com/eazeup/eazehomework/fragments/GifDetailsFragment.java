package com.eazeup.eazehomework.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
                    gifWebView.loadUrl(mData.images.original.url);
                    // Set the height and width and add it into the layout
                    gifWebView.setLayoutParams(new LinearLayout.LayoutParams(newWidth, newHeight));
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
                        mSourceText.setText(String.format(getString(R.string.gif_source), mData.source));
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
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/jpeg");
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
                try {
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
                startActivity(Intent.createChooser(shareIntent, "Share Image"));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.e(TAG, "Compress to bitmap failed, share URL");
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                shareIntent.putExtra(Intent.EXTRA_TEXT, mData.bitlyGifUrl);

                startActivity(Intent.createChooser(shareIntent, "Share GIF"));
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // Empty implementation
            }
        };
        if (mData != null) {
            // Future optimization: stop in onPause()
            Picasso.with(getContext()).load(mData.images.original.url).into(target);
        }
    }

}
