package com.alex_graves.picturetheworld;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListActivity extends AppCompatActivity {
    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    private ArrayList<ListItem> items = new ArrayList<>();
    private RecyclerAdapter adapter;

    private double currentLat = 39.9583583;
    private double currentLng = -75.1953933;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);

        // get lists if returning from other activities
        Intent intent = getIntent();
        ArrayList<ListItem> receivedItems = intent.getParcelableArrayListExtra(getString(R.string.place_list_item));
        currentLat = intent.getDoubleExtra(getString(R.string.current_lat), currentLat);
        currentLng = intent.getDoubleExtra(getString(R.string.current_lng), currentLng);

        if (receivedItems != null) {
            items = receivedItems;
        }

        // set up recycler
        adapter = new RecyclerAdapter(items, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu._menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home_view) {
            goToMain();
            return true;
        }

        if (item.getItemId() == R.id.map_view) {
            goToMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void goToMain() {
        Intent home = new Intent(ListActivity.this, MainActivity.class);
        home.putParcelableArrayListExtra(getString(R.string.place_list_item), items);
        home.putExtra(getString(R.string.current_lat), currentLat);
        home.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(home);
    }

    void goToMap() {
        Intent map = new Intent(ListActivity.this, MapsActivity.class);
        map.putParcelableArrayListExtra(getString(R.string.place_list_item), items);
        map.putExtra(getString(R.string.current_lat), currentLat);
        map.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(map);
    }

    ArrayList<ListItem> getItems() {
        return items;
    }

    double getCurrentLat() {
        return currentLat;
    }

    double getCurrentLng() {
        return currentLng;
    }
}
