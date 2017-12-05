package com.alex_graves.picturetheworld;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private final String access_token = "3597152346.7d0b94c.49d17c5cd0fe4a65a9b34a1bdd1c151a";

    private static final int INTERNET_PERMISSION = 1;
    private static final int CAMERA_PERMISSION = 2;

    @BindView(R.id.connect)
    Button connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // handle permissions
        int internet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (internet != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
        }

        if (camera != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocations();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case INTERNET_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,
                            "You should grant internet access to use Picture the World fully!",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
            case CAMERA_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,
                            "You should grant camera access to use Picture the World fully!",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void getLocations() {
        Map<String, String> options = new HashMap<>();
        options.put("lat", "39.9583583");
        options.put("lng", "-75.1953933");
        options.put("access_token", access_token);
        InstagramService.getService().getLocations(options).enqueue(new Callback<InstagramService.GetLocationsResponse>() {
            @Override
            public void onResponse(Call<InstagramService.GetLocationsResponse> call,
                                   Response<InstagramService.GetLocationsResponse> response) {
                Log.d("response", call.request().url().toString());
                ArrayList<LocationItem> data = response.body().data;
                for (LocationItem d : data) {
                    Log.d("name", d.name);
                }
            }

            @Override
            public void onFailure(Call<InstagramService.GetLocationsResponse> call, Throwable t) {
                Log.d("error", t.toString());
            }
        });
    }
}
