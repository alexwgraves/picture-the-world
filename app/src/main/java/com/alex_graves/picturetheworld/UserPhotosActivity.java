package com.alex_graves.picturetheworld;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.places.Places;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserPhotosActivity extends AppCompatActivity {
    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    private ArrayList<ListItem> items = new ArrayList<>();
    private ArrayList<ListItem> placeItems = new ArrayList<>();
    RecyclerAdapter adapter;

    private double currentLat = 39.9583583;
    private double currentLng = -75.1953933;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_photos);
        ButterKnife.bind(this);

        // get place id and other info from previous activity
        Intent intent = getIntent();
        ArrayList<ListItem> receivedPlaceItems = intent.getParcelableArrayListExtra(getString(R.string.place_list_item));
        currentLat = intent.getDoubleExtra(getString(R.string.current_lat), currentLat);
        currentLng = intent.getDoubleExtra(getString(R.string.current_lng), currentLng);

        if (receivedPlaceItems != null) {
            placeItems = receivedPlaceItems;
        }

        // set up recycler
        adapter = new RecyclerAdapter(items);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        getItemKeys();
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

        return super.onOptionsItemSelected(item);
    }

    void goToMain() {
        Intent home = new Intent(UserPhotosActivity.this, MainActivity.class);
        home.putParcelableArrayListExtra(getString(R.string.place_list_item), placeItems);
        home.putExtra(getString(R.string.current_lat), currentLat);
        home.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(home);
    }

    void goToList() {
        Intent list = new Intent(UserPhotosActivity.this, ListActivity.class);
        list.putParcelableArrayListExtra(getString(R.string.place_list_item), placeItems);
        list.putExtra(getString(R.string.current_lat), currentLat);
        list.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(list);
    }

    void goToMap() {
        Intent map = new Intent(UserPhotosActivity.this, MapsActivity.class);
        map.putParcelableArrayListExtra(getString(R.string.place_list_item), placeItems);
        map.putExtra(getString(R.string.current_lat), currentLat);
        map.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(map);
    }

    void getItemKeys() {
        RedisService.getService().allKeys("item_*").enqueue(new Callback<RedisService.KeysResponse>() {
            @Override
            public void onResponse(Call<RedisService.KeysResponse> call, Response<RedisService.KeysResponse> response) {
                for (String key : response.body().keys) {
                    getUserImageItem(key);
                }
            }

            @Override
            public void onFailure(Call<RedisService.KeysResponse> call, Throwable t) {
                Toast.makeText(UserPhotosActivity.this, t.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    void getUserImageItem(String key) {
        RedisService.getService().getUserImageItem(key).enqueue(new Callback<RedisService.GetResponse>() {
            @Override
            public void onResponse(Call<RedisService.GetResponse> call, Response<RedisService.GetResponse> response) {
                if (response.body() != null) {
                    UserImageItem item = response.body().item;
                    items.add(item);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<RedisService.GetResponse> call, Throwable t) {
                Toast.makeText(UserPhotosActivity.this, t.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
