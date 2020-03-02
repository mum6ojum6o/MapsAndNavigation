package com.mumbojumbo.mapsandnav.viewmodels;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.mumbojumbo.mapsandnav.R;

import java.io.IOException;
import java.util.List;

public class LocationsViewModel extends ViewModel implements OnCompleteListener<Location>,PendingResult.Callback<DirectionsResult> {
    private static final String TAG = "LocationsViewModel";
    private MutableLiveData<Location> mLocation;
    private FusedLocationProviderClient mFusedLocationProvider;
    private MutableLiveData<Address> mRequestedAddress;
    private MutableLiveData<DirectionsResult> mDirectionsResult;
    public MutableLiveData<Location> getDevicesLastKnownLocation(Context context){
        mFusedLocationProvider =  LocationServices.getFusedLocationProviderClient(context);
        if(mLocation==null){
            mLocation = new MutableLiveData<>();
            mFusedLocationProvider.getLastLocation().addOnCompleteListener(this);
        }
        return mLocation;
    }

    public LiveData<Address> searchAddress(Context context,String searchQuery){
        if(mRequestedAddress==null) {
            mRequestedAddress = new MutableLiveData<>();
        }
        Geocoder geocoder = new Geocoder(context);
        try {
            List<Address> address = geocoder.getFromLocationName(searchQuery,1);
            if(address.size()>0) {
                mRequestedAddress.setValue(address.get(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mRequestedAddress;
    }

    public LiveData<DirectionsResult> processDirections(Context context, LatLng from, LatLng to){
        if(mDirectionsResult==null){
            mDirectionsResult = new MutableLiveData<>();
        }
        Log.d(TAG,"apiKey:"+context.getString(R.string.GOOGLE_MAPS_API_KEY));
        GeoApiContext geoApiContext = new GeoApiContext.Builder().apiKey(
                context.getString(R.string.GOOGLE_MAPS_API_KEY)).build();
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);
        directions.alternatives(true);
        com.google.maps.model.LatLng destination =
                new com.google.maps.model.LatLng(to.latitude,to.longitude);
        directions.origin(new com.google.maps.model.LatLng(
                from.latitude,
                from.longitude
        ));
        Log.d(TAG,"calculating Directions to destination"+to.latitude +" "+ to.longitude);
        directions.destination(destination).setCallback(this);
        return mDirectionsResult;
    }





    @Override
    public void onComplete(@NonNull Task<Location> task) {
        if(task.isSuccessful()){
            mLocation.setValue(task.getResult());
        }else{
            mLocation.setValue(new Location(""));
        }
    }


    /********************************************************
     * Callback method to fetch the DirectionsResult
     * Call initiated by processDirections()->directions.destination(destination).setCallback(this);
     * @param result
     ********************************************************/
    @Override
    public void onResult(DirectionsResult result) {
        if( result.routes==null|| result.routes.length==0){
//                    Toast.makeText(getContext(),"Sorry! No route possible by Land!", Toast.LENGTH_LONG).show();
            //displayNoPossibleRoutesDialog();
            mDirectionsResult.postValue(null);
            return;
        }
        Log.d(TAG, "processing Direction: routes:"+result.routes[0].toString());
        Log.d(TAG, "processing Direction: duration:"+result.routes[0].legs[0].duration);
        Log.d(TAG, "processing Direction: duration:"+result.routes[0].legs[0].distance);
        Log.d(TAG, "processing Direction: waypoints:"+result.geocodedWaypoints[0].toString());
        //addRouteLines(result);

        mDirectionsResult.postValue(result);
    }

    @Override
    public void onFailure(Throwable e) {
        Log.e(TAG,"calculate Directions: Unable to get Directions"+e.getMessage());
    }
}
