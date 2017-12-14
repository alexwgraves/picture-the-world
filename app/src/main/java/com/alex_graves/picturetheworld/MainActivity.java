package com.alex_graves.picturetheworld;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final String INSTAGRAM_ACCESS_TOKEN = "3597152346.7d0b94c.49d17c5cd0fe4a65a9b34a1bdd1c151a";

    private static final int INTERNET_PERMISSION = 1;
    private static final int CAMERA_PERMISSION = 2;
    private static final int LOCATION_PERMISSION = 3;

    private GoogleApiClient googleApiClient;
    private GeoDataClient geoDataClient;
    private PlaceDetectionClient placeClient;
    private FusedLocationProviderClient locationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private double currentLat = 39.9583583;
    private double currentLng = -75.1953933;
    private ArrayList<String> placeIds = new ArrayList<>();
    private ArrayList<ListItem> items = new ArrayList<>();

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
        int location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (internet != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
        }

        if (camera != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }

        if (location != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }

        // set up places stuff
        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        geoDataClient = Places.getGeoDataClient(this, null);
        placeClient = Places.getPlaceDetectionClient(this, null);
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    currentLat = location.getLatitude();
                    currentLng = location.getLongitude();
                }
            }
        };

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNearbyPlacesInformation();
                Toast.makeText(MainActivity.this,
                        "Places found!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // get user location
        startLocationUpdates();

        // get intent data if necessary
        Intent intent = getIntent();
        ArrayList<ListItem> receivedPlaces = intent.getParcelableArrayListExtra(getString(R.string.place_list_item));
        double lat = intent.getDoubleExtra(getString(R.string.current_lat), currentLat);
        double lng = intent.getDoubleExtra(getString(R.string.current_lng), currentLng);

        // update member variables if necessary
        if (receivedPlaces != null) {
            items = receivedPlaces;
        }

        // if location changed or has never been checked, get user's place
        if (placeIds.isEmpty() || lat != currentLat || lng != currentLng) {
            getUserPlace();
        }

        // update location if necessary
        if (lat != currentLat) {
            currentLat = lat;
        }
        if (lng != currentLng) {
            currentLng = lng;
        }

        Log.d("lat", Double.toString(currentLat));
        Log.d("lng", Double.toString(currentLng));
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
                return;
            }
            case LOCATION_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,
                            "You should grant location access to use Picture the World fully!",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu._menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.map_view) {
            goToMap();
            return true;
        }

        if (item.getItemId() == R.id.list_view) {
            goToList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("error", connectionResult.getErrorMessage());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    private void stopLocationUpdates() {
        locationClient.removeLocationUpdates(locationCallback);
    }

    void startLocationUpdates() {
        int location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (location == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
    }

    void goToMap() {
        Intent map = new Intent(MainActivity.this, MapsActivity.class);
        map.putParcelableArrayListExtra(getString(R.string.place_list_item), items);
        map.putExtra(getString(R.string.current_lat), currentLat);
        map.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(map);
    }

    void goToList() {
        Intent list = new Intent(MainActivity.this, ListActivity.class);
        list.putParcelableArrayListExtra(getString(R.string.place_list_item), items);
        list.putExtra(getString(R.string.current_lat), currentLat);
        list.putExtra(getString(R.string.current_lng), currentLng);
        startActivity(list);
    }

    void getNearbyPlacesInformation() {
        // clear the list; if the user clicks this again, they want to refresh the place list
        items = new ArrayList<>();
        for (String id : placeIds) {
            getPlaceInformation(id);
        }
    }

    void getMedia() {
        String id = "1515686174910420163_3597152346";
        InstagramService.getService().getMedia(id,
                INSTAGRAM_ACCESS_TOKEN).enqueue(new Callback<InstagramService.GetMediaResponse>() {
            @Override
            public void onResponse(Call<InstagramService.GetMediaResponse> call,
                                   Response<InstagramService.GetMediaResponse> response) {
                Log.d("response", call.request().url().toString());
                MediaItem data = response.body().data;
                Log.d("id", data.id);
            }

            @Override
            public void onFailure(Call<InstagramService.GetMediaResponse> call, Throwable t) {
                Log.d("error", t.toString());
            }
        });
    }

    void getNearbyMedia() {
        Map<String, String> options = new HashMap<>();
        options.put("lat", "39.9583583");
        options.put("lng", "-75.1953933");
        options.put("access_token", INSTAGRAM_ACCESS_TOKEN);
        InstagramService.getService().getNearbyMedia(options).enqueue(new Callback<InstagramService.GetNearbyMediaResponse>() {
            @Override
            public void onResponse(Call<InstagramService.GetNearbyMediaResponse> call,
                                   Response<InstagramService.GetNearbyMediaResponse> response) {
                Log.d("response", call.request().url().toString());
                ArrayList<MediaItem> data = response.body().data;
                for (MediaItem d : data) {
                    Log.d("id", d.id);
                }
            }

            @Override
            public void onFailure(Call<InstagramService.GetNearbyMediaResponse> call, Throwable t) {
                Log.d("error", t.toString());
            }
        });
    }

    void getLocations() {
        Map<String, String> options = new HashMap<>();
        options.put("lat", "39.9583583");
        options.put("lng", "-75.1953933");
        options.put("access_token", INSTAGRAM_ACCESS_TOKEN);
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

    void getLocationMedia() {
        String id = "214228753";
        InstagramService.getService().getLocationMedia(id,
                INSTAGRAM_ACCESS_TOKEN).enqueue(new Callback<InstagramService.GetLocationMediaResponse>() {
            @Override
            public void onResponse(Call<InstagramService.GetLocationMediaResponse> call,
                                   Response<InstagramService.GetLocationMediaResponse> response) {
                Log.d("response", call.request().url().toString());
                ArrayList<MediaItem> data = response.body().data;
                for (MediaItem d : data) {
                    Log.d("id", d.id);
                }
            }

            @Override
            public void onFailure(Call<InstagramService.GetLocationMediaResponse> call, Throwable t) {
                Log.d("error", t.toString());
            }
        });
    }

    void getUserPlace() {
        int location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (location == PackageManager.PERMISSION_GRANTED) {
            Task<PlaceLikelihoodBufferResponse> placeResult = placeClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                            placeIds.add(placeLikelihood.getPlace().getId());
                            Log.d("places", String.format("Place '%s' has likelihood: %g",
                                    placeLikelihood.getPlace().getName(),
                                    placeLikelihood.getLikelihood()));
                        }
                        likelyPlaces.release();
                    } else {
                        Toast.makeText(MainActivity.this,
                                "We can't find your current place. Try searching instead!",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
    }

    void getUserLocation() {
        int location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (location == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location == null) {
                        Toast.makeText(MainActivity.this,
                                "We can't find your location. Try searching for places instead!",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    Log.d("lat", Double.toString(location.getLatitude()));
                    Log.d("lng", Double.toString(location.getLongitude()));
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
    }

    private void getPlaceInformation(String id) {
        final String placeId = id;

        Places.GeoDataApi.getPlaceById(googleApiClient, placeId).setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(PlaceBuffer places) {
                if (places.getStatus().isSuccess() && places.getCount() > 0) {
                    final Place place = places.get(0);
                    Log.i("place", "Place found: " + place.getName());
                    // TODO parse place types
                    List<Integer> types = place.getPlaceTypes();
                    PlaceListItem item = new PlaceListItem(placeId, place.getName().toString(),
                            place.getAddress().toString(), "", place.getLatLng());
                    items.add(item);
                }
                places.release();
            }
        });
    }
}
