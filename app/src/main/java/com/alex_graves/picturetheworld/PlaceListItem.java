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

class PlaceListItem implements ListItem, Parcelable {
    private String id;
    private String name;
    private String description;
    private String type;
    private double lat;
    private double lng;

    PlaceListItem(String id, String name, String description, String type, LatLng coords) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        lat = coords.latitude;
        lng = coords.longitude;
    }

    PlaceListItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        type = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
    }

    @Override
    public int getListItemType() {
        return ListItem.PLACE;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(type);
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

    String getID() {
        return id;
    }

    String getName() {
        return name;
    }

    String getDescription() {
        return description;
    }

    String getType() {
        return type;
    }

    double getLat() {
        return lat;
    }

    double getLng() {
        return lng;
    }
}
