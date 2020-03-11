package edu.msu.carro228.pickup;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import edu.msu.carro228.pickup.Utility.PrefrencesUtility;

public class SetLocActivity extends FragmentActivity implements OnMapReadyCallback {

    /**
     * Constant string for marker title
     */
    private static final String MARKER_TITLE = "Game Location";

    /**
     * Firebase
     */
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    /**
     * reference to database
     */
    DatabaseReference myRef = database.getReference("pickup_games");

    /**
     * The google map for showing/selecting game location
     */
    private GoogleMap map;

    /**
     * The marker on the map that shows the pending location
     */
    private Marker marker;

    /**
     * LocationManager
     */
    private LocationManager locationManager = null;

    /**
     * active listener for location services
     */
    private ActiveListener activeListener = new ActiveListener();

    /**
     * Handles post new location behavior
     */
    private Handler postNew = new Handler();

    /**
     * Current coordinates in Latitude and Longitude
     */
    private LatLng coordinate = new LatLng(0, 0);

    private String gameName;

    private String gameType;

    private String description;

    private String difficulty;

    private String cost;

    private int maxPlayers;

    private int currentPlayers;

    private String date;

    private String time;

    /**
     * Are the current coordinates valid
     */
    private boolean valid = false;


    /**
     * Called when this application is no longer the foreground application.
     */
    @Override
    protected void onPause() {
        unregisterListeners();
        super.onPause();
    }

    /**
     * Called when this application becomes foreground again.
     */
    @Override
    protected void onResume() {
        super.onResume();
        registerListeners();
    }

    //TODO: Convert to use locationUtility
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_loc);

        // Get the map
        ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment)).getMapAsync(this);

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Intent intent = getIntent();
        gameName = intent.getStringExtra("GAME_NAME");
        gameType = intent.getStringExtra("GAME_TYPE");
        cost = intent.getStringExtra("COST");
        description = intent.getStringExtra("DESCRIPTION");
        maxPlayers = intent.getIntExtra("MAX_PLAYERS", 1);
        currentPlayers = intent.getIntExtra("CURRENT_PLAYERS",1);
        time = intent.getStringExtra(Game.START);
        date = intent.getStringExtra(Game.DATE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        marker = map.addMarker(new MarkerOptions().position(coordinate).title(MARKER_TITLE).visible(valid));
        map.moveCamera(CameraUpdateFactory.newLatLng(coordinate));
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                update(latLng.latitude, latLng.longitude);
            }
        });
    }

    public void onConfirm(View view) {
        Game game = new Game(gameType, difficulty, gameName, maxPlayers,description, cost, coordinate.latitude, coordinate.longitude, currentPlayers, time, date);
        String uid = myRef.push().getKey();
        myRef.child(uid).setValue(game);
        Set<String> mine = PrefrencesUtility.readMine(this);
        mine.add(uid);
        PrefrencesUtility.writeMine(this, mine);
        if (currentPlayers == 1){
            Set<String> joined = PrefrencesUtility.readJoined(this);
            joined.add(uid);
            PrefrencesUtility.writeJoined(this, joined);
        }
        unregisterListeners();
        Intent intent = new Intent(this, LandingActivity.class);
        startActivity(intent);
    }

    public void onPlaceAtMe(View view){
        registerListeners();
    }

    public void onPlace(View view){
        EditText location = (EditText)findViewById(R.id.gameAddressEditText);
        final String address = location.getText().toString().trim();
        newAddress(address);
    }

    /**
     * update the marker to reflect the coordinates
     * @param lat
     * @param lon
     */
    public void update(double lat, double lon){
        coordinate = new LatLng(lat, lon);
        if (marker != null){
            marker.setPosition(coordinate);
            marker.setVisible(valid);
        }
    }

    /**
     * Register a location listener and update marker
     */
    private void registerListeners() {
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
                if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            Location location = locationManager.getLastKnownLocation(bestAvailable);
            onLocation(location);
        }
    }

    /**
     * On acquisition of initial location data
     * @param location
     */
    private void onLocation(Location location) {
        if (location == null) {
            return;
        }
        valid = true;
        update(location.getLatitude(), location.getLongitude());
    }

    /**
     * Find the address
     * @param address
     */
    private void newAddress(final String address) {
        if(address.equals("")) {
            // Don't do anything if the address is blank
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                lookupAddress(address);
            }
        }).start();
    }

    /**
     * Look up the provided address. This works in a thread!
     * @param address Address we are looking up
     */
    private void lookupAddress(final String address) {
        Geocoder geocoder = new Geocoder(this, Locale.US);
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
        postNew.post(new Runnable() {
            @Override
            public void run() {
                newLocation(address, fException, fLocations);
            }
        });
    }

    /**
     * Validate address and update location
     * @param address
     * @param exception
     * @param locations
     */
    private void newLocation(String address, boolean exception, List<Address> locations) {

        if(exception) {
            Toast.makeText(this, R.string.location_exception, Toast.LENGTH_SHORT).show();
        } else {
            if(locations == null || locations.size() == 0) {
                Toast.makeText(this, R.string.location_not_found_msg, Toast.LENGTH_SHORT).show();
                return;
            }

            EditText location = (EditText)findViewById(R.id.gameAddressEditText);
            location.setText("");

            // We have a valid new location
            Address a = locations.get(0);
            update(a.getLatitude(), a.getLongitude());
        }
    }

    /**
     * Unregister listeners
     */
    private void unregisterListeners() {
        locationManager.removeUpdates(activeListener);
    }

    //TODO: probably need to implement listener for upgradding to better provider when available
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