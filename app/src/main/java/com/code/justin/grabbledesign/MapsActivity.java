package com.code.justin.grabbledesign;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

    private int Player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        Intent intent = getIntent();
        Player = Integer.parseInt(intent.getStringExtra("userId"));
        Log.i("MAPS ACTIVITY PLAYER:", String.valueOf(Player));
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

        setMarkerClickListener(mMap);

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

            createPlacemarkDB(kmlLayer);

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

    private void createPlacemarkDB(KmlLayer kmlLayer){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS placemarks (id INTEGER, placemarkID VARCHAR(20), superLetter boolean, letter VARCHAR(1), collected boolean, PRIMARY KEY(id, placemarkID))");
        userData.execSQL("DELETE FROM placemarks");
        letterLayer = kmlLayer;
        for (KmlPlacemark point : kmlLayer.getPlacemarks()) {
            String pointLetter = point.getProperty("description");
            String pointID = point.getProperty("name");
            Log.i("PlacemarkID", pointID);
            userData.execSQL("INSERT INTO placemarks (id, placemarkID, superLetter, letter, collected) VALUES ('" + Integer.toString(Player) + "', '" + pointID + "', '" + "false" + "', '" + pointLetter + "', '" + "false" + "')");
        }
        userData.close();
    }

    private boolean wasPressed(String tag, String letter){
        Log.i("MarkerPress", "letter:" + letter);
        Log.i("MarkerPress", "placeID:" + tag);
        Boolean pressed = false;
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        String tableName = "placemarks";
        String selectStringPlacemark = "SELECT * FROM " + tableName + " WHERE " + "id" + " =?"+ " AND " + "placemarkID" + "=?";

        Cursor cursor = userData.rawQuery(selectStringPlacemark, new String[] {Integer.toString(Player), tag});
        boolean hasObject = false;

        if(cursor.moveToFirst()){
            hasObject = true;
            Log.i("foundPress", String.valueOf(hasObject));
            Log.i("id", cursor.getString(cursor.getColumnIndex("id")));
            Log.i("letter", cursor.getString(cursor.getColumnIndex("placemarkID")));
            Log.i("superLetter", cursor.getString(cursor.getColumnIndex("superLetter")));
            Log.i("letter", cursor.getString(cursor.getColumnIndex("letter")));
            Log.i("collected", cursor.getString(cursor.getColumnIndex("collected")));
        }

        cursor.close();
        userData.close();
        return pressed;
    }

    private void setMarkerClickListener(GoogleMap map) {
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i("Marker Clicked", "HELLO MUH FRIEND");
                String placemarkID = marker.getId();
                String letter = marker.getTitle();
                String tag = (String) marker.getTag();
                Log.i("Marker id is: ", placemarkID);
                Log.i("Marker title is: ", letter);
                Log.i("Marker tag is: ", tag);

                //Making DB check
                wasPressed(tag, letter);
                //String tag = marker.getTag();
                return true;
            }
        });
    }

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
