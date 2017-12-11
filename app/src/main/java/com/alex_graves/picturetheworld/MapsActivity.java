package com.alex_graves.picturetheworld;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    private ArrayList<PlaceListItem> items = new ArrayList<>();
    private ArrayList<Bitmap> placeImages = new ArrayList<>();
    private ArrayList<String> placeCredits = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        map.getMapAsync(this);

        Intent intent = getIntent();
        items = intent.getParcelableArrayListExtra(getString(R.string.place_list_item));
        placeImages = intent.getParcelableArrayListExtra(getString(R.string.place_images));
        placeCredits = intent.getStringArrayListExtra(getString(R.string.place_credits));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu._menu_map, menu);
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

        return super.onOptionsItemSelected(item);
    }

    void goToMain() {
        Intent home = new Intent(MapsActivity.this, MainActivity.class);
        home.putParcelableArrayListExtra(getString(R.string.place_list_item), items);
        home.putParcelableArrayListExtra(getString(R.string.place_images), placeImages);
        home.putStringArrayListExtra(getString(R.string.place_credits), placeCredits);
        startActivity(home);
    }

    void goToList() {
        Intent list = new Intent(MapsActivity.this, ListActivity.class);
        list.putParcelableArrayListExtra(getString(R.string.place_list_item), items);
        list.putParcelableArrayListExtra(getString(R.string.place_images), placeImages);
        list.putStringArrayListExtra(getString(R.string.place_credits), placeCredits);
        startActivity(list);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker at Penn and move the camera
        LatLng coords = new LatLng(39.952089, -75.193597);

        for (PlaceListItem item : items) {
            coords = new LatLng(item.getLat(), item.getLng());
            mMap.addMarker(new MarkerOptions().position(coords).title(item.getName()));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(coords));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
    }
}
