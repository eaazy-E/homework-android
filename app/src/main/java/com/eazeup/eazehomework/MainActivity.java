package com.eazeup.eazehomework;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.eazeup.eazehomework.service.GiphyResponse;
import com.eazeup.eazehomework.service.GiphyServiceManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.hello);

        GiphyServiceManager.get().getTrending(new Callback<GiphyResponse>() {
            @Override
            public void onResponse(Call<GiphyResponse> call, Response<GiphyResponse> response) {
                if (response.isSuccessful()) {
                    textView.setText(response.body().data[0].url);
                } else {
                    textView.setText("Unsuccessful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GiphyResponse> call, Throwable t) {
                textView.setText("Failed: " + t.getMessage());
            }
        });
    }
}
