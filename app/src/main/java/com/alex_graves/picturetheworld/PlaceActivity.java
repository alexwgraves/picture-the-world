package com.alex_graves.picturetheworld;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaceActivity extends AppCompatActivity {
    @BindView(R.id.no_photos)
    TextView noPhotos;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    private ArrayList<ListItem> items = new ArrayList<>();
    private ArrayList<ListItem> placeItems = new ArrayList<>();
    RecyclerAdapter adapter;

    private double currentLat = 39.9583583;
    private double currentLng = -75.1953933;

    private GeoDataClient geoDataClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        ButterKnife.bind(this);

        // get place id and other info from previous activity
        Intent intent = getIntent();
        String id = intent.getStringExtra(getString(R.string.place_id));
        String name = intent.getStringExtra(getString(R.string.place_item_name));
        ArrayList<ListItem> receivedPlaceItems = intent.getParcelableArrayListExtra(getString(R.string.place_list_item));
        currentLat = intent.getDoubleExtra(getString(R.string.current_lat), currentLat);
        currentLng = intent.getDoubleExtra(getString(R.string.current_lng), currentLng);

        if (receivedPlaceItems != null) {
            placeItems = receivedPlaceItems;
        }

        // set up api
        geoDataClient = Places.getGeoDataClient(this, null);
        getPhotos(id);

        setTitle(name);

        // set up recycler
        adapter = new RecyclerAdapter(items);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        if (items.isEmpty()) {
            noPhotos.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu._menu_place, menu);
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

        if (item.getItemId() == R.id.take_photo) {
            takePhoto();
            return true;
        }

        if (item.getItemId() == R.id.see_photos) {
            seePhotos();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void goToMain() {
        Intent home = new Intent(PlaceActivity.this, MainActivity.class);
        home.putParcelableArrayListExtra(getString(R.string.place_list_item), placeItems);
        home.putExtra(getString(R.string.current_lat), currentLat);
        home.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(home);
    }

    void goToList() {
        // return to the list of places
        finish();
    }

    void goToMap() {
        Intent map = new Intent(PlaceActivity.this, MapsActivity.class);
        map.putParcelableArrayListExtra(getString(R.string.place_list_item), placeItems);
        map.putExtra(getString(R.string.current_lat), currentLat);
        map.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(map);
    }

    void takePhoto() {
        Intent capture = new Intent(PlaceActivity.this, CaptureActivity.class);
        capture.putParcelableArrayListExtra(getString(R.string.place_list_item), placeItems);
        capture.putExtra(getString(R.string.current_lat), currentLat);
        capture.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(capture);
    }

    void seePhotos() {
        Intent photos = new Intent(PlaceActivity.this, UserPhotosActivity.class);
        photos.putParcelableArrayListExtra(getString(R.string.place_list_item), placeItems);
        photos.putExtra(getString(R.string.current_lat), currentLat);
        photos.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(photos);
    }

    private void getPhotos(String id) {
        final String placeId = id;
        // get place image
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = geoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                PlacePhotoMetadataResponse photos = task.getResult();
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();

                // gather at most ten photos for this place
                int size = Math.min(10, photoMetadataBuffer.getCount());
                for (int i = 0; i < size; i++) {
                    // get the photo and its related attribution
                    PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(i);
                    CharSequence attribution = photoMetadata.getAttributions();
                    String attrib = "";
                    if (attribution.toString().lastIndexOf('<') != -1) {
                        attrib = attribution.subSequence(attribution.toString().indexOf('>') + 1,
                                attribution.toString().lastIndexOf('<')).toString();
                    }
                    final String credit = attrib;
                    Log.d("credit", credit);

                    // get full bitmap for the photo
                    Task<PlacePhotoResponse> photoResponse = geoDataClient.getPhoto(photoMetadata);
                    photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                            PlacePhotoResponse photo = task.getResult();
                            Bitmap bitmap = photo.getBitmap();
                            ImageItem item = new ImageItem(placeId, bitmap, credit);
                            noPhotos.setVisibility(View.GONE);
                            items.add(item);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }

                // free buffer
                photoMetadataBuffer.release();
            }
        });
    }
}
