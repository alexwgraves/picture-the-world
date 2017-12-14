package com.alex_graves.picturetheworld;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by agraves on 12/13/17.
 *
 * Holds images that a user has taken.
 */

class UserImageItem implements ListItem {
    private String imageName;
    private String itemName;
    private String credit;
    String place;

    UserImageItem(String timestamp, String credit, String place) {
        imageName = "img_" + timestamp + ".jpg";
        itemName = "item_" + timestamp;
        this.credit = credit;
        this.place = place;
    }

    UserImageItem(Parcel in) {
        imageName = in.readString();
        itemName = in.readString();
        credit = in.readString();
        place = in.readString();
    }

    @Override
    public int getListItemType() {
        return ListItem.USER_IMAGE;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageName);
        dest.writeString(itemName);
        dest.writeString(credit);
        dest.writeString(place);
    }

    public static final Parcelable.Creator<UserImageItem> CREATOR = new Parcelable.Creator<UserImageItem>() {

        public UserImageItem createFromParcel(Parcel in) {
            return new UserImageItem(in);
        }

        public UserImageItem[] newArray(int size) {
            return new UserImageItem[size];
        }
    };

    String getCredit() {
        return credit;
    }

    String getImageName() {
        return imageName;
    }

    String getPlace() {
        return place;
    }
}
