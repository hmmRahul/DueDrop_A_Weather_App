package com.hmmrahul.duedrop.network;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeoLocation {

    static double latitude = 0;
    static double longitude = 0;

    public static void getlonglat(final String location, final Context context) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List addressList = geocoder.getFromLocationName(location, 2);
            if (addressList != null && addressList.size() > 0) {
                Address address = (Address) addressList.get(0);
                latitude = address.getLatitude();
                longitude = address.getLongitude();
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }


    public static double getLatitude() {
        return latitude;
    }

    public static double getLongitude() {
        return longitude;
    }
}
