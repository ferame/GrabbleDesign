package com.code.justin.grabbledesign;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "Maps Activity";
    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;

    private KmlLayer letterLayer;

    private GoogleApiClient client;

    private ArrayList<Marker> allMarkers = new ArrayList<>();
    private ArrayList<Marker> usedMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        mMap.setMyLocationEnabled(true);
        DownloadTask task = new DownloadTask();
        KmlLayer returnedLayer = null;
        try {
            Log.i(TAG,"load the layer of markers");
            String dayOfWeek = new SimpleDateFormat("EEEE").format(new Date());
            task.execute("http://www.inf.ed.ac.uk/teaching/courses/selp/coursework/" + dayOfWeek.toLowerCase() + ".kml");
            //usedMarkers = new int[];
        } catch (Exception e) {
            e.printStackTrace();
        }


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "onLocationChanged");
                findNearbyLetters(location);
                //centerMapOnLocation(location, "Your Location");
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                }
            }
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, KmlLayer> {

        @Override
        protected KmlLayer doInBackground(String... urls) {
            Log.i(TAG,"doInBackground");
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                try {
                    KmlLayer layer = new KmlLayer(mMap, in, getApplicationContext());
                    return layer;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(KmlLayer kmlLayer) {
            Log.i(TAG,"onPostExecute");
            super.onPostExecute(kmlLayer);
            try {
                letterLayer = kmlLayer;
                for (KmlPlacemark point : kmlLayer.getPlacemarks()) {
                    String pointLetter = point.getProperty("description");
                    String pointID = point.getProperty("name");
                    String toMod = point.getGeometry().toString();
                    Double pointLat = Double.parseDouble(toMod.split("\\(")[1].split(",")[0]);
                    Double pointLng = Double.parseDouble(toMod.split(",")[1].split("\\)")[0]);
                    Marker newMarker = mMap.addMarker(
                            new MarkerOptions()
                            .position(new LatLng(pointLat,pointLng))
                            .title(pointLetter)
                            .visible(false));
                    newMarker.setTag(pointID);
                    allMarkers.add(newMarker);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            letterLayer = kmlLayer;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    {

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    }

                }

            }

        }

    }

    public void findNearbyLetters(Location userLocation) {
        Log.i(TAG, "findNearbyLetters");

        if(letterLayer != null){
            if (letterLayer.getPlacemarks() != null){
                Log.i(TAG,"checksPlacemarks");
                for (Marker point : allMarkers){
                    Location pointLoc = new Location("LetterLoc");
                    pointLoc.setLongitude(point.getPosition().longitude);
                    pointLoc.setLatitude(point.getPosition().latitude);
                    if (userLocation.distanceTo(pointLoc) < 50){
                        point.setVisible(true);
                    }else{
                        point.setVisible(false);
                    }
                }
            }
        }
    }

    /*public static double checkDistance(double lat1, double lat2, double lon1, double lon2) {

        final int R = 6371; // Radius of the earth
        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        return Math.sqrt(distance);
    }*/

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }


    public void toInventoryActivity(View view){
        Intent intent = new Intent(getApplicationContext(), InventoryActivity.class);
        startActivity(intent);
    }

    public void toSettingsActivity(View view){
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    public void toWordInputActivity(View view){
        Intent intent = new Intent(getApplicationContext(), WordInputActivity.class);
        startActivity(intent);
    }
}
