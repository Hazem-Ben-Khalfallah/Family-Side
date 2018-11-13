package com.blacknebula.familyside.mobile.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import com.blacknebula.familyside.mobile.R;
import com.blacknebula.familyside.mobile.util.Logger;
import com.blacknebula.familyside.mobile.util.ViewUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Konstantin on 22.12.2014.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String MAPS = "Building";
    private static final int REQUEST_ACCESS_LOCATION_STATE = 1;
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final float MIN_ZOOM = 15f;
    private final String CURRENT_USER_NAME = "You";
    protected int res;
    private View containerView;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker marker;
    private LocationRequest mLocationRequest;

    public static MapsFragment newInstance(int resId) {
        MapsFragment contentFragment = new MapsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Integer.class.getName(), resId);
        contentFragment.setArguments(bundle);
        return contentFragment;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.containerView = view.findViewById(R.id.container);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getArguments().getInt(Integer.class.getName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);


        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000L)        // 10 seconds, in milliseconds
                .setFastestInterval(1000L); // 1 second, in milliseconds
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkGPS();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    private boolean checkGPS() {
        final LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            ViewUtils.openDialog(getActivity(), R.string.gps_network_not_enabled, R.string.open_location_settings, new ViewUtils.onClickListener() {
                @Override
                public void onPositiveClick() {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }

                @Override
                public void onNegativeClick() {
                    checkGPS();
                }

                @Override
                public void onCancel() {
                    checkGPS();
                }

            });
        }
        return gpsEnabled;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_LOCATION_STATE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Logger.info(Logger.Type.FAMILY_SIDE, "permission was granted, yay! retry location detection");
                mGoogleApiClient.connect();
            } else {
                Logger.warn(Logger.Type.FAMILY_SIDE, "permission denied, boo! Disable location detection");
            }
        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Logger.info(Logger.Type.FAMILY_SIDE, "connection success *********");

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_LOCATION_STATE);
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Logger.info(Logger.Type.FAMILY_SIDE, String.format("Last location detected: Lat: %s Lng: %s", mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            addMarkerOnMap(mLastLocation.getLatitude(), mLastLocation.getLongitude(), CURRENT_USER_NAME);
        } else {
            Logger.warn(Logger.Type.FAMILY_SIDE, "Last location not detected.");
        }

        Logger.info(Logger.Type.FAMILY_SIDE, "Requesting current location");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Logger.error(Logger.Type.FAMILY_SIDE, "Location services connection failed with code %s", connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(final Location loc) {
        if (marker == null) {
            Logger.info(Logger.Type.FAMILY_SIDE, "Add a new marker on map");
            addMarkerOnMap(loc.getLatitude(), loc.getLongitude(), CURRENT_USER_NAME);
        } else {
            Logger.info(Logger.Type.FAMILY_SIDE, "Update position of marker");
            final LatLng toPosition = new LatLng(loc.getLatitude(), loc.getLongitude());
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            Projection proj = mMap.getProjection();
            Point startPoint = proj.toScreenLocation(marker.getPosition());
            final LatLng startLatLng = proj.fromScreenLocation(startPoint);
            final long duration = 500; //ms

            final Interpolator interpolator = new LinearInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed
                            / duration);
                    double lng = t * loc.getLongitude() + (1 - t)
                            * startLatLng.longitude;
                    double lat = t * loc.getLatitude() + (1 - t)
                            * startLatLng.latitude;
                    marker.setPosition(new LatLng(lat, lng));

                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    } else {
                        marker.setVisible(true);
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(toPosition));
                }
            });
        }
    }

    private void addMarkerOnMap(double lat, double lang, String name) {
        LatLng position = new LatLng(lat, lang);
        marker = mMap.addMarker(new MarkerOptions().position(position).title(name));
        //Move the camera to the user's location and zoom in!
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, MIN_ZOOM));
    }


}
