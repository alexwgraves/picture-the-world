package com.alex_graves.picturetheworld;

import android.os.Parcelable;

/**
 * Created by agraves on 12/11/17.
 *
 * Interface for any items that use RecyclerAdapater.
 */

interface ListItem extends Parcelable {
    int PLACE = 1;
    int IMAGE = 2;

    int getListItemType();
}
