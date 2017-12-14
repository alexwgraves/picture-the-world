package com.alex_graves.picturetheworld;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by agraves on 12/11/17.
 *
 * Used to store images when displaying photos of a given place.
 */

class ImageItem implements ListItem, Parcelable {
    private String id;
    private Bitmap image;
    private String credit;

    ImageItem(String id, Bitmap image, String credit) {
        this.id = id;
        this.image = image;
        this.credit = credit;
    }

    ImageItem(Parcel in) {
        id = in.readString();
        credit = in.readString();
    }

    @Override
    public int getListItemType() {
        return ListItem.IMAGE;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(credit);
    }

    public static final Parcelable.Creator<ImageItem> CREATOR = new Parcelable.Creator<ImageItem>() {

        public ImageItem createFromParcel(Parcel in) {
            return new ImageItem(in);
        }

        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };

    Bitmap getImage() {
        return image;
    }

    String getCredit() {
        return credit;
    }
}
