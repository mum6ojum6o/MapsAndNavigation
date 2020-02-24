package com.mumbojumbo.mapsandnav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import static com.mumbojumbo.mapsandnav.utils.Constants.ERROR_DIALOG_MSG;
import static com.mumbojumbo.mapsandnav.utils.Constants.PERMISSIONS_ACCESS_FINE_LOCATION;
import static com.mumbojumbo.mapsandnav.utils.Constants.PERMISSIONS_ENABLE_GPS;

public class MainActivity extends AppCompatActivity implements MapFragment.OnFragmentInteractionListener {
    private static final String TAG="MainActivity";
    private boolean mLocationPermissionGranted;
    private MapFragment mMapFragment;
    private boolean mFragmentResotred;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if(savedInstanceState!=null) mFragmentResotred=true;
        mMapFragment =  MapFragment.newInstance(null,null,this);

    }
    @Override
    protected void onResume(){
        super.onResume();

        if(checkMapServices() && mLocationPermissionGranted){
            Log.d(TAG,"All Services and Permissions accounted for. Resuming App");
            initMapFragment();
        }else{
            Log.d(TAG,"Permission not accounted for.");
            getLocationPermission();
            if(mLocationPermissionGranted && !mFragmentResotred) {
                initMapFragment();
            }
        }
    }

    private void initMapFragment(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,mMapFragment)
                .commit();
    }

    private boolean checkMapServices(){
        if(areServicesOk()){
           if(areMapsEnabled()){
               return true;
           }
        }
        return false;
    }

    //determine whether user can use google play services on the device.
    private boolean areServicesOk(){
        Log.d(TAG,"areServicesOk: Checking if google play services are configured and working...");
        int googlePlayServicesAvailable = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(MainActivity.this);
        if(googlePlayServicesAvailable == ConnectionResult.SUCCESS){
            Log.d(TAG,"areServicesOk: Google Play Services are running ");
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(googlePlayServicesAvailable)){
            Log.d(TAG,"areServicesOk: an Error occured but could be resolved!!");
            Dialog dialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(MainActivity.this,googlePlayServicesAvailable,ERROR_DIALOG_MSG);
            dialog.show();
        }else{
            Toast.makeText(this,"Unable to make map requests",Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private boolean areMapsEnabled(){
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            buildNoGPSAlertMessage();
            return false;
        }
        return true;
    }
    private void buildNoGPSAlertMessage(){
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(R.string.request_gps_message_in_dialog)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent enableGpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent,PERMISSIONS_ACCESS_FINE_LOCATION);
                    }
                });
        final AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    private void getLocationPermission(){
        /* Request Location permissions, to obtain location of the device.
         */
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionGranted = true;
            //resume next activity
        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);
        }
    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode){
            case PERMISSIONS_ENABLE_GPS:{
                if(mLocationPermissionGranted){
                    //resume
                }else{
                    getLocationPermission();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        mLocationPermissionGranted = true;
        switch (requestCode){
            case PERMISSIONS_ACCESS_FINE_LOCATION:{
                if (grantResults.length > 0 && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
//TODO Internet Connectivity check
//TODO Config change handling
// TODO Navigation

