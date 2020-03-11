package edu.msu.carro228.pickup.Utility;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import edu.msu.carro228.pickup.LocationUser;

/**
 * A collection of functions for interfacing with location services
 */
public class LocationUtility  {

    /**
     * Location Manager
     */
    private LocationManager locationManager;

    /**
     * Active listener
     */
    private ActiveListener activeListener = new ActiveListener();

    /**
     * The parent context
     */
    private Context context;

    /**
     * The parent
     */
    private LocationUser user;

    /**
     * last known device location
     */
    private Location lastLocation;

    /**
     * Handlers for post thread behavior
     */
    private Handler postGetAddress = new Handler();
    private Handler postFindLocation = new Handler();
    private Handler postFineAddress = new Handler();

    /**
     * Constructor
     * @param context
     * @param user
     */
    public LocationUtility(Context context, LocationUser user){
        this.context = context;
        this.user = user;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * get the current location and call onLocation in user
     * @param reregister reregister listener if true
     */
    public void getLocation(boolean reregister){
        if (reregister){
            registerListeners();
        }
        if (lastLocation == null) {
            return;
        }
        user.onLocation(lastLocation);
    }

    /**
     * get the current location address and call onAddress in user
     * @param reregister reregister listener if true
     */
    public void getAddress(boolean reregister){
        if (reregister){
            registerListeners();
        }
        if (lastLocation == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.US);
                boolean exception = false;
                List<Address> locations;
                try{
                    locations = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
                }catch (IOException ex){
                    locations = null;
                    exception = true;
                }

                final boolean fException = exception;
                final List<Address> fLocations = locations;
                postGetAddress.post(new Runnable() {
                    @Override
                    public void run() {
                        user.onAddress(fException, fLocations);
                    }
                });
            }
        }).start();
    }

    /**
     * Find an address for a given LatLng
     * @param coordinate
     */
    public void findAddress(final LatLng coordinate){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.US);
                boolean exception = false;
                List<Address> locations;
                try{
                    locations = geocoder.getFromLocation(coordinate.latitude, coordinate.longitude, 1);
                }catch (IOException ex){
                    locations = null;
                    exception = true;
                }

                final boolean fException = exception;
                final List<Address> fLocations = locations;
                postFineAddress.post(new Runnable() {
                    @Override
                    public void run() {
                        user.onFindAddress(coordinate, fException, fLocations);
                    }
                });
            }
        }).start();
    }

    /**
     * Find a coordinate for a given address
     * @param address
     */
    public void findLocation(final String address) {
        if(address.equals("")) {
            // Don't do anything if the address is blank
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.US);
                boolean exception = false;
                List<Address> locations;
                try {
                    locations = geocoder.getFromLocationName(address, 1);
                } catch(IOException ex) {
                    // Failed due to I/O exception
                    locations = null;
                    exception = true;
                }

                final boolean fException = exception;
                final List<Address> fLocations = locations;
                postFindLocation.post(new Runnable() {
                    @Override
                    public void run() {
                        user.onFindLocation(address, fException, fLocations);
                    }
                });
            }
        }).start();
    }

    /**
     * Register a location listener and update marker
     */
    public void registerListeners() {
        unregisterListeners();

        // Create a Criteria object
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);

        String bestAvailable = locationManager.getBestProvider(criteria, true);

        if (bestAvailable != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
            }
            locationManager.requestLocationUpdates(bestAvailable, 500, 1, activeListener);
            lastLocation = locationManager.getLastKnownLocation(bestAvailable);
        }
    }

    /**
     * Unregister listeners
     */
    public void unregisterListeners() {
        locationManager.removeUpdates(activeListener);
    }

    private class ActiveListener implements LocationListener {


        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {
            registerListeners();
        }
    };
}
