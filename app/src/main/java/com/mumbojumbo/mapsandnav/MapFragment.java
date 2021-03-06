package com.mumbojumbo.mapsandnav;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.mumbojumbo.mapsandnav.model.PolylineData;
import com.mumbojumbo.mapsandnav.viewmodels.LocationsViewModel;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mumbojumbo.mapsandnav.utils.Constants.DESTINATION_ADDRESS_KEY;
import static com.mumbojumbo.mapsandnav.utils.Constants.LAST_KNOWN_LOCATION_KEY;
import static com.mumbojumbo.mapsandnav.utils.Constants.MAPVIEW_BUNDLE_KEY;
import static com.mumbojumbo.mapsandnav.utils.Constants.PERMISSIONS_ACCESS_FINE_LOCATION;
import static com.mumbojumbo.mapsandnav.utils.Constants.ROUTES_REQUESTED_KEY;
import static com.mumbojumbo.mapsandnav.utils.Constants.USER_ENTERED_ADDRESS_KEY;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback
        ,View.OnClickListener
        , GoogleMap.OnPolylineClickListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "MapFragment";
    //private FusedLocationProviderClient mFusedLocationProvider;
    private GoogleMap mGoogleMap;
    //using this object we can set the boundary of the map displayed onscreen
    private LatLngBounds mMapBoundary;
    private Location mUsersLastKnownLocation;
    private LocationsViewModel mLocationViewModel;
    @BindView(R.id.et_search) EditText mSearchEditText;
    @BindView(R.id.floating_action_button) FloatingActionButton mFloatingActionButton;
    @BindView(R.id.floating_action_button_directions) FloatingActionButton mDirectionsFloatingActionButton;
    @BindView(R.id.floating_action_button_start_navigation) FloatingActionButton mNavigationFloatingActionButton;
    @BindView(R.id.mv_map_view) MapView mMapView;
    private Address mDestinationAddress;

    private GeoApiContext mGeoApiContext;
    private String mSearchedAddress;
    private Marker mMarker;
    private List<PolylineData> mPolyLineData = new ArrayList<>();
    private boolean restoreState;
    private boolean routesRequested;

    public MapFragment(){

    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment MapFragment.
     */

    public static MapFragment newInstance( ) {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.map_fragment,container,false);
        ButterKnife.bind(this,view);
        initiateGoogleMaps(savedInstanceState);
        mLocationViewModel = new ViewModelProvider(this) .get(LocationsViewModel.class);
        mFloatingActionButton.setOnClickListener(this);
        mDirectionsFloatingActionButton.setOnClickListener(this);
        mFloatingActionButton.setOnClickListener(this);
        mNavigationFloatingActionButton.setOnClickListener(this);
        return view;
    }

    private void initiateGoogleMaps(Bundle savedInstanceState){
        Bundle bundle = null;
        if(savedInstanceState!=null){
            bundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
            restoreState(savedInstanceState);
        }
        mMapView.onCreate(bundle);
        mMapView.getMapAsync(this);
        if(mGeoApiContext==null){

            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.GOOGLE_MAPS_API_KEY))
                    .build();
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        googleMap.setMyLocationEnabled(true);

        mGoogleMap.setOnPolylineClickListener(this);
        initSearchEditText();
        if(restoreState){
            //getlastKnownLocation();
            searchEnteredLocation();
            if(routesRequested){
                onClick(mDirectionsFloatingActionButton);
            }
        }else{
            mLocationViewModel.getDevicesLastKnownLocation(getActivity()).observe(this, new Observer<Location>(){
                    @Override
                    public void onChanged(@Nullable final Location location){
                        if(location !=null){
                            setLocation(location);
                        }
                        else{
                            Toast.makeText(getContext(),"Location could not be found!!!",Toast.LENGTH_LONG).show();
                        }
                    }
            });
        }
    }

    private void setLocation(Location location){
        mUsersLastKnownLocation = location;
        setCameraView(mUsersLastKnownLocation.getLatitude(),mUsersLastKnownLocation.getLongitude(),false );
    }

    @SuppressLint("RestrictedApi")
    private void setCameraView(double latitude, double longitude, boolean displayMarker){
        double bottomBoundary = latitude-.01;
        double leftBoundary = longitude-.01;
        double rightBoundary = longitude+.01;
        double topBoundary = latitude+.01;
        mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary,leftBoundary),
                new LatLng(topBoundary,rightBoundary)
         );
        if(displayMarker){
            //setSearchAddressInfo();
            mMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude,longitude)).title(mDestinationAddress.getAddressLine(0)));
            mMarker.showInfoWindow();
            mDirectionsFloatingActionButton.setVisibility(View.VISIBLE);
        }
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                mMapBoundary,0));
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.floating_action_button:
                resetMap();
                //mSearchedAddressInfo.setVisibility(View.INVISIBLE);
                mDestinationAddress=null;
                routesRequested=false;
                restoreState=false;
                setCameraView(mUsersLastKnownLocation.getLatitude(),
                        mUsersLastKnownLocation.getLongitude(),false);
                break;
            case R.id.floating_action_button_directions:
                mDirectionsFloatingActionButton.setVisibility(View.INVISIBLE);

                LatLng from = new LatLng(mUsersLastKnownLocation.getLatitude()
                        ,mUsersLastKnownLocation.getLongitude());
                LatLng to = new LatLng(mDestinationAddress.getLatitude(),mDestinationAddress.getLongitude());

                processDirections(from,to);
                break;
            case R.id.floating_action_button_start_navigation:
                launchGoogleMapsForNavigation();
                break;
        }
    }

    private void resetMap(){
        mSearchEditText.setText("");
        mGoogleMap.clear();
        if(mMarker!=null)
             mMarker.remove();

    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        for(PolylineData polylineData: mPolyLineData){
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.blue));
                polylineData.getPolyline().setZIndex(1);
                mMarker.setSnippet("Duration:"+polylineData.getLeg().duration.humanReadable);
                mMarker.showInfoWindow();
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.darkGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }


    private void initSearchEditText(){
        mSearchEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        mSearchEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
        mSearchEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (    actionId == EditorInfo.IME_ACTION_GO ||
                        actionId == EditorInfo.IME_ACTION_SEARCH) {
                     //execute search
                    mGoogleMap.clear();
                    //mSearchedAddressInfo.setVisibility(View.INVISIBLE);
                    //searchEnteredLocation();
                    searchRequestedAddress();
                }
                return false;
            }
        });
    }


    private void processDirections(LatLng from,LatLng to){
        mLocationViewModel.processDirections(getContext(),from,to).observe(this, new Observer<DirectionsResult>() {
            @Override
            public void onChanged(DirectionsResult directionsResult) {
                if(directionsResult==null||
                        directionsResult.routes==null||
                        directionsResult.routes.length==0){
                    Log.d(TAG,"No routes!!!");
                    displayNoPossibleRoutesDialog();
                    return;
                }
                Log.d(TAG, "processing Direction: routes:"+directionsResult.routes[0].toString());
                Log.d(TAG, "processing Direction: duration:"+directionsResult.routes[0].legs[0].duration);
                Log.d(TAG, "processing Direction: duration:"+directionsResult.routes[0].legs[0].distance);
                Log.d(TAG, "processing Direction: waypoints:"+directionsResult.geocodedWaypoints[0].toString());
                addRouteLines(directionsResult);
            }
        });

    }

    private void searchRequestedAddress(){
        String searchString  = mSearchEditText.getText().toString();
        if(searchString==null||searchString.isEmpty()||searchString.trim().isEmpty()) {
            Toast.makeText(getActivity(), "Please enter an Address", Toast.LENGTH_LONG).show();
            return;
        }
        mLocationViewModel.searchAddress(getActivity(),searchString).observe(this,new Observer<Address>(){

            @Override
            public void onChanged(Address address) {
                if(address!=null){
                    mDestinationAddress = address;
                    setCameraView(mDestinationAddress.getLatitude(),mDestinationAddress.getLongitude(),true);
                }
            }
        });

    }

    private void searchEnteredLocation(){
        Log.d(TAG,"searchEnteredLocation:");
         String searchString  = mSearchEditText.getText().toString();
        Geocoder geoCoder = new Geocoder(getActivity());
        List<Address> addressList = new ArrayList<>();

        try {
            addressList=geoCoder.getFromLocationName(searchString,1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(addressList.size()>0){
            Log.d(TAG,"addresse found");
            Address address = addressList.get(0);
            double latitude = address.getLatitude();
            double longitude = address.getLongitude();
            mDestinationAddress = address;
            //Toast.makeText(getActivity(), address.getAddressLine(0),Toast.LENGTH_LONG).show();
            setCameraView(latitude,longitude,true);
        }
    }

    //Refactor this method.
    private void addRouteLines(final DirectionsResult result ){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @SuppressLint("RestrictedApi")
            @Override
            public void run() {
                Log.d(TAG,"addRouteLines->run(): routes length:"+result.routes.length);
                mGoogleMap.clear();

                if(mPolyLineData.size()>0){
                    mPolyLineData.clear();
                }
                mNavigationFloatingActionButton.setVisibility(View.VISIBLE);

                if(mDestinationAddress!=null){
                    mMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mDestinationAddress.getLatitude(),
                                    mDestinationAddress.getLongitude())).title(mDestinationAddress
                                    .getAddressLine(0)));
                    mMarker.showInfoWindow();
                }
                addPolyLinesToMap(result.routes);
            }
        });
    }


    private void addPolyLinesToMap(DirectionsRoute[] routes){
        double minTripDuration= Double.MAX_VALUE;
        for(DirectionsRoute route:routes){
            Log.d(TAG,"addRouteLines->run():"+route.legs[0].toString());
            List<com.google.maps.model.LatLng> path = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
            List<LatLng> decodedPath = new ArrayList<>();
            for(com.google.maps.model.LatLng coords:path){
                decodedPath.add(new LatLng(coords.lat,coords.lng));
            }
            Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(decodedPath));
            polyline.setColor(ContextCompat.getColor(getActivity(),R.color.darkGrey));
            polyline.setClickable(true);
            mPolyLineData.add(new PolylineData(polyline,route.legs[0]));
            calculateMapBoundary(polyline.getPoints());
            if(minTripDuration> route.legs[0].duration.inSeconds){
                minTripDuration = route.legs[0].duration.inSeconds;
                onPolylineClick(polyline);
                mMarker.setSnippet("Duration:"+ route.legs[0].duration.humanReadable);
                mMarker.showInfoWindow();
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        mMapBoundary,75));
            }
        }
    }

    //method to calculate the method boundaries depending on the latlng bounds of the polyline
    private void  calculateMapBoundary(List<LatLng> latlngRoutePath){
        if(mGoogleMap==null||latlngRoutePath==null||latlngRoutePath.isEmpty()) return;
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : latlngRoutePath)
            boundsBuilder.include(latLngPoint);

        int routePadding = 50;
        mMapBoundary = boundsBuilder.build();
    }




    //method to render alrtdialog in case no routes are possible.
    private void displayNoPossibleRoutesDialog(){
        new Handler(Looper.getMainLooper()).post(new Runnable(){

            @Override
            public void run() {
                Log.d(TAG,"Rendering AlertDialog");
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                dialogBuilder.setMessage(getString(R.string.no_possible_routes)).setTitle("Ooops!");
                dialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG,"User clicked Cancel");
                    }
                });
                dialogBuilder.create();
                dialogBuilder.show();
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        if(mDestinationAddress!=null)
            bundle.putParcelable(DESTINATION_ADDRESS_KEY,mDestinationAddress);
        if(mSearchEditText.getText()!=null && !mSearchEditText.getText().toString().isEmpty())
            bundle.putString(USER_ENTERED_ADDRESS_KEY,mSearchEditText.getText().toString());
        if(mPolyLineData.size()>0) {
            //bundle.putParcelableArrayList("PolyLineData",mPolyLineData);
            bundle.putBoolean(ROUTES_REQUESTED_KEY,true);
        }
        if(mUsersLastKnownLocation!=null){
            bundle.putParcelable(LAST_KNOWN_LOCATION_KEY,mUsersLastKnownLocation);
        }

    }
    private void restoreState(Bundle savedInstanceState){
        Address address = savedInstanceState.getParcelable(DESTINATION_ADDRESS_KEY);
        mDestinationAddress = address;

        mUsersLastKnownLocation = savedInstanceState.getParcelable(LAST_KNOWN_LOCATION_KEY);
        if(mDestinationAddress!=null) restoreState=true;
        String text = savedInstanceState.getString(USER_ENTERED_ADDRESS_KEY);
        mSearchEditText.setText(text);
        boolean routesRequested = savedInstanceState.getBoolean(ROUTES_REQUESTED_KEY);
        this.routesRequested = routesRequested;
    }
    private void launchGoogleMapsForNavigation(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Open Google Maps?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        String latitude = String.valueOf(mMarker.getPosition().latitude);
                        String longitude = String.valueOf(mMarker.getPosition().longitude);
                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");

                        try{
                            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(mapIntent);
                            }
                        }catch (NullPointerException e){
                            Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage() );
                            Toast.makeText(getActivity(), "Couldn't open map", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

}
