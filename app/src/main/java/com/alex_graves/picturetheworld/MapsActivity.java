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

    private ArrayList<ListItem> items = new ArrayList<>();
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

        // get lists if returning from other activities
        Intent intent = getIntent();
        ArrayList<ListItem> receivedItems = intent.getParcelableArrayListExtra(getString(R.string.place_list_item));
        ArrayList<Bitmap> receivedImages = intent.getParcelableArrayListExtra(getString(R.string.place_images));
        ArrayList<String> receivedCredits = intent.getStringArrayListExtra(getString(R.string.place_credits));

        if (receivedItems != null) {
            items = receivedItems;
        }
        if (receivedImages != null) {
            placeImages = receivedImages;
        }
        if (receivedCredits != null) {
            placeCredits = receivedCredits;
        }
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
        LatLng point = new LatLng(39.952089, -75.193597);
        if (items.size() > 0 && items.get(0).getListItemType() == ListItem.PLACE) {
            PlaceListItem place = (PlaceListItem) items.get(0);
            point = new LatLng(place.getLat(), place.getLng());
        }

        for (ListItem item : items) {
            if (item.getListItemType() == ListItem.PLACE) {
                PlaceListItem place = (PlaceListItem) item;
                LatLng coords = new LatLng(place.getLat(), place.getLng());
                mMap.addMarker(new MarkerOptions().position(coords).title(place.getName()));
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
    }
}
