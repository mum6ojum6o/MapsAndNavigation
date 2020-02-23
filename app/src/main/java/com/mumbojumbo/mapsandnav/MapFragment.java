package com.mumbojumbo.mapsandnav;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static com.mumbojumbo.mapsandnav.utils.Constants.MAPVIEW_BUNDLE_KEY;
import static com.mumbojumbo.mapsandnav.utils.Constants.PERMISSIONS_ACCESS_FINE_LOCATION;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback
        ,View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "MapFragment";
    private FusedLocationProviderClient mFusedLocationProvider;
    private GoogleMap mGoogleMap;
    //using this object we can set the boundary of the map displayed onscreen
    private LatLngBounds mMapBoundary;
    private Location mUsersLastKnownLocation;
    private boolean mLocationPermissionGranted;
    private EditText mSearchEditText;

    private String mParam1;
    private String mParam2;
    private FloatingActionButton mFloatingActionButton;
    private FloatingActionButton mDirectionsFloatingActionButton;
    private MapView mMapView;
    private Address mDestinationAddress;
    private OnFragmentInteractionListener mListener;
    private GeoApiContext mGeoApiContext;
    public MapFragment(OnFragmentInteractionListener aContext) {
        // Required empty public constructor
        this.mListener = aContext;
    }
    public MapFragment(){

    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */

    public static MapFragment newInstance(String param1, String param2,OnFragmentInteractionListener listener) {
        MapFragment fragment = new MapFragment(listener);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(getActivity());
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
        View view = inflater.inflate(R.layout.map_fragment,container,false);
        mMapView = view.findViewById(R.id.mv_map_view);
        mFloatingActionButton = view.findViewById(R.id.floating_action_button);
        mDirectionsFloatingActionButton = view.findViewById(R.id.floating_action_button_directions);
        mSearchEditText = view.findViewById(R.id.et_search);
        initiateGoogleMaps(savedInstanceState);
        mFloatingActionButton.setOnClickListener(this);
        mDirectionsFloatingActionButton.setOnClickListener(this);
        return view;
    }

    private void initiateGoogleMaps(Bundle savedInstanceState){
        Bundle bundle = null;
        if(savedInstanceState!=null){
            bundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(bundle);
        mMapView.getMapAsync(this);
        if(mGeoApiContext==null){
            Log.d(TAG,"maps_key:"+getString(R.string.GOOGLE_MAPS_API_KEY));
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.GOOGLE_MAPS_API_KEY))
                    .build();
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        googleMap.setMyLocationEnabled(true);
        getlastKnownLocation();
        initSearchEditText();
    }

    private void getlastKnownLocation(){
        Log.d(TAG,"Inside getLastKnownLocation ");

        mFusedLocationProvider.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    Location location = task.getResult();
                    mUsersLastKnownLocation = location;
                    Log.d(TAG, "getLastKnownLocation -> onComplete: Latitude:"+location.getLatitude());
                    Log.d(TAG, "getLastKnownLocation -> onComplete: Longiude:"+location.getLongitude());
                    setCameraView(mUsersLastKnownLocation.getLatitude(),mUsersLastKnownLocation.getLongitude(),false );

                }
            }
        });
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
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude,longitude)).title("Marker"));
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
        }
    }

    private void resetMap(){
        mSearchEditText.setText("");
        mGoogleMap.clear();

    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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
                    searchEnteredLocation();
                }
                return false;
            }
        });
    }


    private void processDirections(LatLng from,LatLng to){
        Log.d(TAG," process Directions: processing Directions");
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
        directions.alternatives(true);
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(to.latitude,to.longitude);
        directions.origin(new com.google.maps.model.LatLng(
                from.latitude,
                from.longitude
        ));
        Log.d(TAG,"calculating Directions to destination"+to.latitude +" "+ to.longitude);
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "processing Direction: routes:"+result.routes[0].toString());
                Log.d(TAG, "processing Direction: duration:"+result.routes[0].legs[0].duration);
                Log.d(TAG, "processing Direction: duration:"+result.routes[0].legs[0].distance);
                Log.d(TAG, "processing Direction: waypoints:"+result.geocodedWaypoints[0].toString());
                addRouteLines(result);

            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG,"calculate Directions: Unable to get Directions"+e.getMessage());
            }
        });
    }

    private void searchEnteredLocation(){
        Log.d(TAG,"searchEnteredLocation:");
        String searchString = mSearchEditText.getText().toString();
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
    private void addRouteLines(final DirectionsResult result ){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"addRouteLines->run(): routes length:"+result.routes.length);
                mGoogleMap.clear();
                if(mDestinationAddress!=null){
                    mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mDestinationAddress.getLatitude(),
                                    mDestinationAddress.getLongitude())).title("Marker"));

                }
                for(DirectionsRoute route:result.routes){
                    Log.d(TAG,"addRouteLines->run():"+route.legs[0].toString());
                    List<com.google.maps.model.LatLng> path = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
                    List<LatLng> decodedPath = new ArrayList<>();
                    for(com.google.maps.model.LatLng coords:path){
                        decodedPath.add(new LatLng(coords.lat,coords.lng));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(decodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(),R.color.darkGrey));
                    polyline.setClickable(true);
                    calculateMapBoundary();
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                            mMapBoundary,0));
                }

            }
        });
    }
    //TODO - calculcate the boundary to calculate better boundary.
    private void  calculateMapBoundary(){
        if(mUsersLastKnownLocation==null||mDestinationAddress==null)
            return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(mUsersLastKnownLocation.getLatitude(),
                mUsersLastKnownLocation.getLongitude()-1))
        .include(new LatLng(mDestinationAddress.getLatitude(),
                mDestinationAddress.getLongitude()+1));
        mMapBoundary = builder.build();

    }
}
