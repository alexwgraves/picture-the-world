package com.alex_graves.picturetheworld;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by agraves on 12/11/17.
 *
 * Holds a place item for displaying in a list of places.
 */

class PlaceListItem implements Parcelable {
    private Bitmap image;
    private String name;
    private String description;

    PlaceListItem(String name, String description) {
        this.name = name;
        this.description = description;
    }

    PlaceListItem(Parcel in) {
        this.name = in.readString();
        this.description = in.readString();
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
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

    void addImage(Bitmap image) {
        this.image = image;
    }
}
