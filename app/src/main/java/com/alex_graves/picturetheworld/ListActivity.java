package com.alex_graves.picturetheworld;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListActivity extends AppCompatActivity {
    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    private ArrayList<ListItem> items = new ArrayList<>();
    private ArrayList<Bitmap> placeImages = new ArrayList<>();
    private ArrayList<String> placeCredits = new ArrayList<>();
    private RecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        items = intent.getParcelableArrayListExtra(getString(R.string.place_list_item));
        placeImages = intent.getParcelableArrayListExtra(getString(R.string.place_images));
        placeCredits = intent.getStringArrayListExtra(getString(R.string.place_credits));

        for (int i = 0; i < items.size(); i++) {
            ListItem item = items.get(i);
            if (item.getListItemType() == ListItem.PLACE) {
                PlaceListItem place = (PlaceListItem) item;
                place.addImage(placeImages.get(i));
                place.addCredit(placeCredits.get(i));
                items.set(i, item);
            }
        }

        // set up recycler
        adapter = new RecyclerAdapter(items);
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
        home.putParcelableArrayListExtra(getString(R.string.place_images), placeImages);
        home.putStringArrayListExtra(getString(R.string.place_credits), placeCredits);
        startActivity(home);
    }

    void goToMap() {
        Intent map = new Intent(ListActivity.this, MapsActivity.class);
        map.putParcelableArrayListExtra(getString(R.string.place_list_item), items);
        map.putParcelableArrayListExtra(getString(R.string.place_images), placeImages);
        map.putStringArrayListExtra(getString(R.string.place_credits), placeCredits);
        startActivity(map);
    }
}
