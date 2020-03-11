package edu.msu.carro228.pickup;

import android.location.Address;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface LocationUser {
    void onLocation(Location coordinate);
    void onAddress(boolean exception, List<Address> locations);
    void onFindLocation(String address, boolean exception, List<Address> locations);
    void onFindAddress(LatLng coordinate, boolean exception, List<Address> locations);
}
