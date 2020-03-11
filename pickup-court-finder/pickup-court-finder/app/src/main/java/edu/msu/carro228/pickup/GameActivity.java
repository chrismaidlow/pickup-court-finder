package edu.msu.carro228.pickup;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Set;

import edu.msu.carro228.pickup.Utility.LocationUtility;
import edu.msu.carro228.pickup.Utility.PrefrencesUtility;

public class GameActivity extends FragmentActivity implements OnMapReadyCallback, LocationUser {

    /**
     * Title for currentmarker
     */
    private final String CURRENT = "Current Location";

    /**
     * Title for destination marker
     */
    private final String DESTINATION = "Game Location";

    /**
     * Ammount to zoom map
     */
    private final float ZOOM = 12f;

    /**
     * Firebase
     */
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    /**
     * reference to database
     */
    DatabaseReference myRef = database.getReference(Game.TABLE);

    /**
     * ID of the game to be displayed
     */
    private String uid;

    /**
     * last known location of device
     */
    private LatLng currentCoordinate;

    /**
     * marker for currentCoordinate
     */
    private Marker currentMarker;

    /**
     * location of destination
     */
    private LatLng destinationCoordinate;

    /**
     * Marker for destination
     */
    private Marker destinationMarker;

    /**
     * Location services
     */
    private LocationUtility locationUtility;

    /**
     * The google map for showing/selecting game location
     */
    private GoogleMap map;

    /**
     * Values for total number of players
     */
    private long currentPlayers;
    private long totalPlayers;

    TextView playersInGame;
    TextView joinTheGame;
    TextView fullOrNot;
    Button addToGame;


    /**
     * Called when this application is no longer the foreground application.
     */
    @Override
    protected void onPause() {
        locationUtility.unregisterListeners();
        super.onPause();
    }

    /**
     * Called when this application becomes foreground again.
     */
    @Override
    protected void onResume() {
        super.onResume();
        locationUtility.registerListeners();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Get uid
        uid = getIntent().getStringExtra("UID");

        // Get the map
        ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment)).getMapAsync(this);

        // Initialize location
        locationUtility = new LocationUtility(this, this);
        locationUtility.getLocation(true);

        // Access firebase for game details
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                destinationCoordinate = new LatLng((double) dataSnapshot.child(uid).child(Game.LATITUDE).getValue(), (double) dataSnapshot.child(uid).child(Game.LONGITUDE).getValue());
                if (destinationMarker == null && map != null){
                    destinationMarker = map.addMarker(new MarkerOptions().position(destinationCoordinate).title(DESTINATION));
                    map.moveCamera((CameraUpdateFactory.newLatLngZoom(destinationCoordinate, ZOOM)));
                }

                ((TextView) findViewById(R.id.titleTextView)).setText((String) dataSnapshot.child(uid).child(Game.NAME).getValue());
                ((TextView) findViewById(R.id.typeTextView)).setText((String) dataSnapshot.child(uid).child(Game.TYPE).getValue());
                ((TextView) findViewById(R.id.descriptionTextView)).setText((String) dataSnapshot.child(uid).child(Game.DESCRIPTION).getValue());
                totalPlayers = (long) dataSnapshot.child(uid).child(Game.MAX_PLAYERS).getValue();
                currentPlayers = (long) dataSnapshot.child(uid).child(Game.CURRENT_PLAYERS).getValue();
                fullOrNot =  findViewById(R.id.fullOrNot);
                playersInGame =  findViewById(R.id.playersInGame);
                joinTheGame =  findViewById(R.id.joinTheGame);
                addToGame = findViewById(R.id.addToGame);

                playersInGame.setText("Current Players: " + currentPlayers + " Max Players: " + totalPlayers);
                if(currentPlayers >= totalPlayers){
                    fullOrNot.setText("Game Full");
                    fullOrNot.setTextColor(Color.RED);
                    joinTheGame.setText("You can't join, but you can spectate");
                    addToGame.setEnabled(false);
                    //Set the text to be full instead of "good to join"
                    //Game is "full" but you can still spectate!
                }else{
                    fullOrNot.setText("Game Open to Join!");
                    fullOrNot.setTextColor(Color.parseColor("#00b159"));
                    //Set the text to be "good to join"
                    //enable the buttons to confirm joining
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    /**
     * Handler for confirm button
     * @param view
     */
    public void onConfirmAttendance(View view){
        // try to start a navigation app on device
        currentPlayers = currentPlayers + 1;
        myRef.child(uid).child(Game.CURRENT_PLAYERS).setValue(currentPlayers);
        Set<String> joined = PrefrencesUtility.readJoined(this);
        joined.add(uid);
        PrefrencesUtility.writeJoined(this, joined);
        playersInGame.setText("Current Players: " + currentPlayers + " Max Players: " + totalPlayers);
        joinTheGame.setText("Goodluck and have fun!");
        addToGame.setEnabled(false);
    }

    /**
     * Handler for navigate button
     * @param view
     */
    public void onNavigate(View view){
        // try to start a navigation app on device
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=" +
                        currentCoordinate.latitude + "," +
                        currentCoordinate.longitude +
                        "&daddr=" + destinationCoordinate.latitude + "," +
                        destinationCoordinate.longitude));
        locationUtility.unregisterListeners();
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (currentCoordinate != null) {
            currentMarker = map.addMarker(new MarkerOptions().position(currentCoordinate).title(CURRENT));
        }
        if (destinationCoordinate != null){
            destinationMarker = map.addMarker(new MarkerOptions().position(destinationCoordinate).title(DESTINATION));
            map.moveCamera((CameraUpdateFactory.newLatLngZoom(destinationCoordinate, ZOOM)));
        }
    }

    @Override
    public void onLocation(Location coordinate) {
        currentCoordinate = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
        if (currentMarker == null && map != null){
            currentMarker = map.addMarker(new MarkerOptions().position(currentCoordinate).title(CURRENT));
        }
    }

    @Override
    public void onAddress(boolean exception, List<Address> locations) {

    }

    @Override
    public void onFindLocation(String address, boolean exception, List<Address> locations) {

    }

    @Override
    public void onFindAddress(LatLng coordinate, boolean exception, List<Address> locations) {

    }
}
