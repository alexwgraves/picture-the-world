package com.alex_graves.picturetheworld;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by agraves on 12/11/17.
 *
 * Holds a place item for displaying in a list of places.
 */

class PlaceListItem implements Parcelable {
    private Bitmap image;
    private String credit;
    private String name;
    private String description;
    private double lat;
    private double lng;

    PlaceListItem(String name, String description, LatLng coords) {
        this.name = name;
        this.description = description;
        lat = coords.latitude;
        lng = coords.longitude;
    }

    PlaceListItem(Parcel in) {
        this.name = in.readString();
        this.description = in.readString();
        this.lat = in.readDouble();
        this.lng = in.readDouble();
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }

    public static final Parcelable.Creator<PlaceListItem> CREATOR = new Parcelable.Creator<PlaceListItem>() {

        public PlaceListItem createFromParcel(Parcel in) {
            return new PlaceListItem(in);
        }

        public PlaceListItem[] newArray(int size) {
            return new PlaceListItem[size];
        }
    };

    String getName() {
        return name;
    }

    String getDescription() {
        return description;
    }

    Bitmap getImage() {
        return image;
    }

    String getCredit() {
        return credit;
    }

    double getLat() {
        return lat;
    }

    double getLng() {
        return lng;
    }

    void addImage(Bitmap image) {
        this.image = image;
    }

    void addCredit(String credit) {
        this.credit = credit;
    }
}
