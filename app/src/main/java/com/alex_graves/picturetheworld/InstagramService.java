package com.alex_graves.picturetheworld;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by agraves on 12/4/17.
 *
 * Creates a service to query the Instagram API based on nearby places.
 */

class InstagramService {
    private static Service service;
    private final static String URL = "https://api.instagram.com/v1/";

    interface Service {
        // get nearby recent media
        @GET("media/{media-id}")
        Call<GetMediaResponse> getMedia(@Path("media-id") String id, @Query("access_token") String token);

        // get nearby recent media
        @GET("media/search")
        Call<GetNearbyMediaResponse> getNearbyMedia(@QueryMap Map<String, String> options);

        // get nearby locations
        @GET("locations/search")
        Call<GetLocationsResponse> getLocations(@QueryMap Map<String, String> options);

        // get a location's recent media
        @GET("locations/{location-id}/media/recent")
        Call<GetLocationMediaResponse> getLocationMedia(@Path("location-id") String id, @Query("access_token") String token);
    }

    class GetMediaResponse {
        @SerializedName("data")
        MediaItem data;
    }

    class GetNearbyMediaResponse {
        @SerializedName("data")
        ArrayList<MediaItem> data;
    }

    class GetLocationsResponse {
        @SerializedName("data")
        ArrayList<LocationItem> data;
    }

    class GetLocationMediaResponse {
        @SerializedName("data")
        ArrayList<MediaItem> data;
    }

    static Service getService() {
        if (service == null) {
            GsonBuilder gsonBuilder = new GsonBuilder().setLenient();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                    .build();

            service = retrofit.create(Service.class);
        }
        return service;
    }

}
