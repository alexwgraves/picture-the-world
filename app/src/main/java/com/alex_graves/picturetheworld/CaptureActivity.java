package com.alex_graves.picturetheworld;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CaptureActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String photoPath;
    String currentImageName;

    private double currentLat = 39.9583583;
    private double currentLng = -75.1953933;

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
                        new UserImageItem(imageName, credit.getText().toString(), new LatLng(currentLat, currentLng)))
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
}
