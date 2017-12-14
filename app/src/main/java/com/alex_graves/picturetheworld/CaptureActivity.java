package com.alex_graves.picturetheworld;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CaptureActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int LOCATION_PERMISSION = 2;
    String photoPath;
    String currentImageName;

    private PlaceDetectionClient placeClient;
    private ArrayList<ListItem> placeItems = new ArrayList<>();

    private double currentLat = 39.9583583;
    private double currentLng = -75.1953933;
    private String capturePlace = "";

    @BindView(R.id.cancel)
    Button cancel;
    @BindView(R.id.take_photo)
    Button takePhoto;
    @BindView(R.id.save)
    Button save;
    @BindView(R.id.photo)
    ImageView photo;
    @BindView(R.id.item_credit)
    TextView credit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        ButterKnife.bind(this);

        // set up place API and get user's most likely place
        placeClient = Places.getPlaceDetectionClient(this, null);
        getUserPlace();

        Intent intent = getIntent();
        ArrayList<ListItem> receivedPlaceItems = intent.getParcelableArrayListExtra(getString(R.string.place_list_item));
        currentLat = intent.getDoubleExtra(getString(R.string.current_lat), currentLat);
        currentLng = intent.getDoubleExtra(getString(R.string.current_lng), currentLng);

        if (receivedPlaceItems != null) {
            placeItems = receivedPlaceItems;
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent main = new Intent(CaptureActivity.this, MainActivity.class);
                startActivity(main);
            }
        });

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (photo.getDrawable() != null && !credit.getText().toString().isEmpty()) {
                    saveImageFile();
                } else if (photo.getDrawable() == null) {
                    Toast.makeText(CaptureActivity.this, "Don't forget to take your photo!", Toast.LENGTH_LONG).show();
                } else if (credit.getText().toString().isEmpty()) {
                    Toast.makeText(CaptureActivity.this, "Make sure to credit yourself!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu._menu_take_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home_view) {
            goToMain();
            return true;
        }

        if (item.getItemId() == R.id.list_view) {
            goToList();
            return true;
        }

        if (item.getItemId() == R.id.map_view) {
            goToMap();
            return true;
        }

        if (item.getItemId() == R.id.see_photos) {
            seePhotos();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void goToMain() {
        Intent home = new Intent(CaptureActivity.this, MainActivity.class);
        home.putParcelableArrayListExtra(getString(R.string.place_list_item), placeItems);
        home.putExtra(getString(R.string.current_lat), currentLat);
        home.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(home);
    }

    void goToList() {
        Intent list = new Intent(CaptureActivity.this, ListActivity.class);
        list.putParcelableArrayListExtra(getString(R.string.place_list_item), placeItems);
        list.putExtra(getString(R.string.current_lat), currentLat);
        list.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(list);
    }

    void goToMap() {
        Intent map = new Intent(CaptureActivity.this, MapsActivity.class);
        map.putParcelableArrayListExtra(getString(R.string.place_list_item), placeItems);
        map.putExtra(getString(R.string.current_lat), currentLat);
        map.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(map);
    }

    void seePhotos() {
        Intent photos = new Intent(CaptureActivity.this, UserPhotosActivity.class);
        photos.putParcelableArrayListExtra(getString(R.string.place_list_item), placeItems);
        photos.putExtra(getString(R.string.current_lat), currentLat);
        photos.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(photos);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // just continue
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            }
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(timeStamp, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        photoPath = image.getAbsolutePath();
        currentImageName = timeStamp;
        return image;
    }

    private void saveImageFile() {
        final String imageName = currentImageName;
        File file = new File(photoPath); // file path on Android system
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        RedisService.getService().postImage("img_" + imageName, reqFile).enqueue(new Callback<RedisService.SetResponse>() {
            @Override
            public void onResponse(Call<RedisService.SetResponse> call, Response<RedisService.SetResponse> response) {

                RedisService.getService().makeUserImageItem("item_" + imageName,
                        new UserImageItem(imageName, credit.getText().toString(), capturePlace))
                        .enqueue(new Callback<RedisService.SetResponse>() {
                    @Override
                    public void onResponse(Call<RedisService.SetResponse> call, Response<RedisService.SetResponse> response) {
                        Intent userPhotos = new Intent(CaptureActivity.this, UserPhotosActivity.class);
                        startActivity(userPhotos);
                    }

                    @Override
                    public void onFailure(Call<RedisService.SetResponse> call, Throwable t) {
                        Toast.makeText(CaptureActivity.this, t.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<RedisService.SetResponse> call, Throwable t) {
                Toast.makeText(CaptureActivity.this, t.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Picasso.with(this).load(new File(photoPath)).into(photo);
        }
    }

    void getUserPlace() {
        int location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (location == PackageManager.PERMISSION_GRANTED) {
            Task<PlaceLikelihoodBufferResponse> placeResult = placeClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Float> likelihoods = new HashMap<>();
                        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                            likelihoods.put(placeLikelihood.getPlace().getName().toString(),
                                    placeLikelihood.getLikelihood());
                        }
                        likelyPlaces.release();

                        // find most likely place
                        String mostLikely = "";
                        float highest = 0.f;
                        for (String name : likelihoods.keySet()) {
                            if (likelihoods.get(name) > highest) {
                                highest = likelihoods.get(name);
                                mostLikely = name;
                            }
                        }
                        capturePlace = mostLikely;
                    } else {
                        Toast.makeText(CaptureActivity.this,
                                "We can't find your current place.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
    }
}
