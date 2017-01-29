package com.blacknebula.familyside.mobile.services;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.blacknebula.familyside.mobile.FamilySideApplication;
import com.blacknebula.familyside.mobile.util.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class SFLocationListener implements LocationListener {

    private LocationChangeCallback locationChangeCallback;

    public SFLocationListener(LocationChangeCallback locationChangeCallback) {
        this.locationChangeCallback = locationChangeCallback;
    }

    @Override
    public void onLocationChanged(Location loc) {
        Logger.info(Logger.Type.FAMILY_SIDE, "Location changed: Lat: %s Lng: %s", loc.getLatitude(), loc.getLongitude());
        String longitude = "Longitude: " + loc.getLongitude();
        Log.v(TAG, longitude);
        String latitude = "Latitude: " + loc.getLatitude();
        Log.v(TAG, latitude);

        /*------- To get city name from coordinates -------- */
        String cityName = null;
        Geocoder gcd = new Geocoder(FamilySideApplication.getAppContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                cityName = addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        locationChangeCallback.onLocationChange(loc.getLatitude(), loc.getLongitude());
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}