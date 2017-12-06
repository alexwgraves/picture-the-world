package com.alex_graves.picturetheworld;

import java.util.ArrayList;

/**
 * Created by agraves on 12/4/17.
 *
 * Stories a media item from Instagram's API.
 */

class MediaItem {
    String id;
    User user;
    Images images;
    String created_time;
    Caption caption;
    String link;
    String type;
    Location location;
}

class User {
    String full_name;
    String username;
}

class Images {
    Image thumbnail;
    Image standard_resolution;
}

class Image {
    String url;
}

class Caption {
    String text;
    String created_time;
}

class Location {
    double latitude;
    double longitude;
    String name;
    int id;
}