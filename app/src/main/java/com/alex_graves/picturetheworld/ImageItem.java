package com.alex_graves.picturetheworld;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by agraves on 12/11/17.
 *
 * Used to store images when displaying photos of a given place.
 */

class ImageItem implements ListItem, Parcelable {
    private String url;
    private String description;
    private String credit;

    ImageItem(String url, String description, String credit) {
        this.url = url;
        this.description = description;
        this.credit = credit;
    }

    ImageItem(Parcel in) {
        url = in.readString();
        description = in.readString();
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
        dest.writeString(url);
        dest.writeString(description);
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

    String getURL() {
        return url;
    }

    String getDescription() {
        return description;
    }

    String getCredit() {
        return credit;
    }
}
