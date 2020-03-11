package edu.msu.carro228.pickup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.msu.carro228.pickup.Utility.LocationUtility;
import edu.msu.carro228.pickup.Utility.PrefrencesUtility;

public class GamesActivity extends AppCompatActivity implements LocationUser{

    /**
     * ListView in layout
     */
    private ListView list;

    private Spinner spinner;

    private Location location;

    private ValueEventListener listener;

    private LocationUtility locationUtility;

    private Set<String> mine;
    private Set<String> joined;

    /**
     * Database
     */
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    /**
     * Reference to specific database
     */
    DatabaseReference myRef = database.getReference(Game.TABLE);


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
        setContentView(R.layout.activity_games);
        mine = PrefrencesUtility.readMine(this);
        joined = PrefrencesUtility.readJoined(this);
        locationUtility = new LocationUtility(this, this);
        list = (ListView) findViewById(R.id.gamesListView);
        spinner = (Spinner) findViewById(R.id.radiusSpinner);
        spinner.setAdapter(new RadiusAdapter());
        locationUtility.getLocation(true);
        locationUtility.getAddress(false);
    }

    /**
     * button handler for search
     * @param view
     */
    public void onSearch (View view){
        locationUtility.getLocation(true);
    }

    /**
     * button handler for going back
     * @param view
     */
    public void onBack(View view){
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);

    }


    // ============================ LocationUser interface ==========================================

    @Override
    public void onLocation(final Location location) {
        this.location = location;
        final float [] result = new float [1];
        if(listener != null){
            myRef.removeEventListener(listener);
        }

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<DataSnapshot> temp = new ArrayList<>();
                for (DataSnapshot snap: dataSnapshot.getChildren()){
                    try {
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(), (double) snap.child(Game.LATITUDE).getValue(), (double) snap.child(Game.LONGITUDE).getValue(), result);
                        if (result[0] < (int)spinner.getSelectedItem()) {
                            if(joined.contains(snap.getKey().toString())){
                                continue;
                            }
                            temp.add(snap);
                        }
                    }catch (Exception e){
                        Log.e("Malformed Entry", e.toString());
                        //temp.add(snap);
                    }
                }
                list.setAdapter(new GamesAdapter(temp));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        myRef.addValueEventListener(listener);
    }

    @Override
    public void onAddress(boolean exception, List<Address> locations) {
        ((TextView) findViewById(R.id.currentPositionTextView)).setText(locations.get(0).getAddressLine(0));
    }

    @Override
    public void onFindLocation(String address, boolean exception, List<Address> locations) {
        if(exception) {
            Toast.makeText(this, R.string.location_exception, Toast.LENGTH_SHORT).show();
        } else {
            if(locations == null || locations.size() == 0) {
                Toast.makeText(this, R.string.location_not_found_msg, Toast.LENGTH_SHORT).show();
                return;
            }

            //TODO: Do something
            //locations.get(0);
        }
    }



    @Override
    public void onFindAddress(LatLng coordinate, boolean exception, List<Address> locations) {
        return;
    }

    // =============================================================================================

    /**
     * Adapter for list view of games
     */
    public static class GamesAdapter extends BaseAdapter {

        private ArrayList<DataSnapshot> data;


        public GamesAdapter(ArrayList<DataSnapshot> data){
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return data.get(i).getKey().hashCode();
        }

        @Override
        public View getView(int i, View view, final ViewGroup viewGroup) {
            Button x = new Button(viewGroup.getContext());
            x.setText((String) data.get(i).child(Game.NAME).getValue());
            final int index = i;
            x.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(viewGroup.getContext(), GameActivity.class);
                    intent.putExtra("UID", data.get(index).getKey());
                    viewGroup.getContext().startActivity(intent);
                }
            });
            return x;
        }
    }

    /**
     * Adapter for radius spinner
     */
    public static class RadiusAdapter extends BaseAdapter
    {
        private int [] radii = new int [] {100, 500, 1000};


        @Override
        public int getCount() {
            return radii.length;
        }

        @Override
        public Object getItem(int i) {
            return radii[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView text = new TextView(viewGroup.getContext());
            text.setText(radii[i] + " m");
            text.setTextSize(20);
            return text;
        }
    }}
