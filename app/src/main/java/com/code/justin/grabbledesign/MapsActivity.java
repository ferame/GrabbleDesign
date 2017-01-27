package com.code.justin.grabbledesign;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "Maps Activity";
    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;

    private KmlLayer letterLayer;

    private GoogleApiClient client;

    private ArrayList<Marker> allMarkers = new ArrayList<>();

    private int player;

    Toast currentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        Intent intent = getIntent();
        player = Integer.parseInt(intent.getStringExtra("userId"));
        Log.i("MAPS ACTIVITY Player:", String.valueOf(player));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onMapReady", "starts");
        mMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            String style = getSelectedStyle();
            Log.i("STYLE MAPS", style);
            Resources res = this.getResources();
            int styleID = res.getIdentifier(style, "raw", this.getPackageName());
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, styleID));
            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }

        setMarkerClickListener(mMap);

        DownloadTask task = new DownloadTask();
        try {
            Log.i(TAG,"load the layer of markers");
            String dayOfWeek = new SimpleDateFormat("EEEE").format(new Date());
            task.execute("http://www.inf.ed.ac.uk/teaching/courses/selp/coursework/" + dayOfWeek.toLowerCase() + ".kml");
        } catch (Exception e) {
            e.printStackTrace();
        }


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "onLocationChanged");
                findNearbyLetters(location);
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
            if(getCurrentBoolSetting("powerSaving")) {
                Log.i("PowerSaving", "ENABLED");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 8000, 0, locationListener);
            } else {
                Log.i("PowerSaving", "FALSE");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }

            Log.i("Button to center", "start");
            mMap.setMyLocationEnabled(true);
            Log.i("Button to center", "end");

            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
            }

        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {
                if(getCurrentBoolSetting("powerSaving")) {
                    Log.i("PowerSaving", "ENABLED");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 8000, 0, locationListener);
                } else {
                    Log.i("PowerSaving", "FALSE");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }

                Log.i("Button to center", "start");
                mMap.setMyLocationEnabled(true);
                Log.i("Button to center", "end");

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                }
            }
        }
        Log.d("onMapReady", "ends");
    }

    public class DownloadTask extends AsyncTask<String, Void, KmlLayer> {

        @Override
        protected KmlLayer doInBackground(String... urls) {
            Log.i(TAG,"doInBackground");
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
            //Log.i("Date check", checkDate().toString());
            if (checkDate() && markerMapExists()){
                letterLayer = kmlLayer;
                buildMarkerMap();
            } else {
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
    }

    private boolean markerMapExists(){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        String tableName = "placemarks";
        String selectStringPlacemark = "SELECT * FROM " + tableName + " WHERE " + "id" + " =?";

        Cursor c = userData.rawQuery(selectStringPlacemark, new String[] {Integer.toString(player)});
        if (!c.moveToFirst()){
            return false;
        }else {
            return true;
        }
    }

    private void buildMarkerMap(){
        Log.i("buildMarkerMap", "started");
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        String tableName = "placemarks";
        String selectStringPlacemark = "SELECT * FROM " + tableName + " WHERE " + "id" + " =?";

        Cursor c = userData.rawQuery(selectStringPlacemark, new String[] {Integer.toString(player)});
        int placemarkIdIndex = c.getColumnIndex("placemarkID");
        int pointLatIndex = c.getColumnIndex("pointLat");
        int pointLngIndex = c.getColumnIndex("pointLng");
        int superLetterIndex = c.getColumnIndex("superLetter");
        int letterIndex = c.getColumnIndex("letter");
        int collectedIndex = c.getColumnIndex("collected");
//        c.moveToFirst();
        Integer count = 1;
        if (c.moveToFirst()){
            do {
//        while (c != null){
                String placemarkId = c.getString(placemarkIdIndex);
                Double pointLat = Double.parseDouble(c.getString(pointLatIndex));
                Double pointLng = Double.parseDouble(c.getString(pointLngIndex));
                String superLetter = c.getString(superLetterIndex);
                String letter = c.getString(letterIndex);
                String collected = c.getString(collectedIndex);

                Marker newMarker = mMap.addMarker(
                        new MarkerOptions()
                                .position(new LatLng(pointLat, pointLng))
                                .title(letter)
                                .visible(false));

                newMarker.setTag(placemarkId);

                if (collected.equalsIgnoreCase("true")) {
                    newMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                }

                allMarkers.add(newMarker);
//                count++;
//                if (count != 1000) {
//                    c.moveToNext();
//                } else {
//                    break;
//                }
            } while (c.moveToNext());
        }else {
            Log.i("Caught bug", "Here");
        }
        c.close();
        userData.close();
        Log.i("buildMarkerMap", "ended");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    {
                        if(getCurrentBoolSetting("powerSaving")) {
                            Log.i("PowerSaving", "ENABLED");
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 8000, 0, locationListener);
                        } else {
                            Log.i("PowerSaving", "FALSE");
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                        }
                        Log.i("Button to center", "start");
                        mMap.setMyLocationEnabled(true);
                        Log.i("Button to center", "end");

                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastKnownLocation != null) {
                            LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                        }
                    }

                }

            }

        }

    }


    public void findNearbyLetters(Location userLocation) {
        int setDistance = getSetDistance();

        Log.i(TAG, "findNearbyLetters");

        boolean superLetterEnabled = isBooleanSettingOn("superLetter");
        String superLetterId = "";
        if (superLetterEnabled) {
            superLetterId = getSuperLetterId().replaceAll("\\D+","");
        }

        boolean autoCollectEnabled = isBooleanSettingOn("autoCollect");

        if(letterLayer != null){
            Log.i(TAG,"gets letterLayer");
            if (letterLayer.getPlacemarks() != null){
                Log.i(TAG,"checksPlacemarks");
                for (Marker point : allMarkers){
                    Location pointLoc = new Location("LetterLoc");
                    pointLoc.setLongitude(point.getPosition().longitude);
                    pointLoc.setLatitude(point.getPosition().latitude);

                    if (userLocation.distanceTo(pointLoc) < setDistance){
                        if (point.getTag().toString().replaceAll("\\D+","").equalsIgnoreCase(superLetterId)){
                            Log.i("found letter", "which is super");
                            if (superLetterEnabled){
                                Log.i("in settings the", "superletter was enabled");
                                Log.i("the superletter was", superLetterId);
                                Log.i("the point was", point.getTag().toString().replaceAll("\\D+",""));
                                if (point.getTag().toString().replaceAll("\\D+","").equalsIgnoreCase(superLetterId)){
                                    Log.i("and even", "point matched");
                                    if (wasPressedPreviously(superLetterId)){
                                        point.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                    } else {
                                        point.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                                    }
                                }
                            }
                            else {
                                if (wasPressedPreviously(superLetterId)){
                                    point.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                } else {
                                    point.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                }
                            }
                        }
                        point.setVisible(true);
                        if (autoCollectEnabled){
                            if (userLocation.distanceTo(pointLoc) <= 10){
                                markerInProximityOrClicked(point);
                            }
                        }
                    }else{
                        point.setVisible(false);
                    }
                }
            }
        }
    }

    private void createPlacemarkDB(KmlLayer kmlLayer){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS placemarks (id INTEGER, placemarkID VARCHAR(20), pointLat REAL, pointLng REAL, superLetter boolean, letter VARCHAR(1), collected boolean, PRIMARY KEY(id, placemarkID))");
        int superLetterIs = generateRandomNum(1,1001);
        userData.execSQL("DELETE FROM placemarks WHERE id = '" + Integer.toString(player) + "'");
        letterLayer = kmlLayer;
        for (KmlPlacemark point : kmlLayer.getPlacemarks()) {
            String pointLetter = point.getProperty("description");
            String pointID = point.getProperty("name");
            String toMod = point.getGeometry().toString();
            Double pointLat = Double.parseDouble(toMod.split("\\(")[1].split(",")[0]);
            Double pointLng = Double.parseDouble(toMod.split(",")[1].split("\\)")[0]);
            if (pointID.replaceAll("\\D+","").equalsIgnoreCase(Integer.toString(superLetterIs))) {
                userData.execSQL("INSERT INTO placemarks (id, placemarkID, pointLat, pointLng, superLetter, letter, collected) VALUES ('" + Integer.toString(player) + "', '" + pointID + "', '" + pointLat + "', '" + pointLng + "', '" + "true" + "', '" + pointLetter + "', '" + "false" + "')");
            } else {
                userData.execSQL("INSERT INTO placemarks (id, placemarkID, pointLat, pointLng, superLetter, letter, collected) VALUES ('" + Integer.toString(player) + "', '" + pointID + "', '" + pointLat + "', '" + pointLng + "', '" + "false" + "', '" + pointLetter + "', '" + "false" + "')");
            }
        }
        userData.close();
    }

    private boolean isBooleanSettingOn(String settingType){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS settings (id INTEGER PRIMARY KEY, nightMode boolean, powerSaving boolean, autoCollect boolean, superLetter boolean, visibilityRad INTEGER, overlay INTEGER, lastUse varchar(20))");

        String selectString = "SELECT * FROM settings WHERE " + "id" + " =?";
        Cursor cursor = userData.rawQuery(selectString, new String[]{Integer.toString(player)});

        int settingIndex = cursor.getColumnIndex(settingType);

        cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            boolean setting;
            Log.i("Did", "getCurrentBoolSetting");
            Log.i(settingType, cursor.getString(settingIndex));
            if (cursor.getString(settingIndex).equalsIgnoreCase("1")){
                setting = true;
                Log.i("setting", "1");
            }else {
                setting = false;
                Log.i("setting", "0");
            }
            cursor.close();
            userData.close();
            return setting;
        }
        cursor.close();
        userData.close();

        return false;
    }

    private String getSuperLetterId(){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        String tableName = "placemarks";
        String selectStringPlacemark = "SELECT * FROM " + tableName + " WHERE " + "id" + " =?"+ " AND " + "superLetter" + "=?";

        String superLetterId = "null";

        Cursor cursor = userData.rawQuery(selectStringPlacemark, new String[] {Integer.toString(player), "true"});

        if(cursor.moveToFirst()){
            cursor.moveToFirst();
            superLetterId = cursor.getString(cursor.getColumnIndex("placemarkID"));
        }
        cursor.close();
        userData.close();
        return superLetterId;
    }

    private boolean wasPressedPreviously(String letterId){
        letterId = "Point " + letterId;
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        String tableName = "placemarks";
        String selectStringPlacemark = "SELECT * FROM " + tableName + " WHERE " + "id" + " =?"+ " AND " + "placemarkID" + "=?";

        Cursor cursor = userData.rawQuery(selectStringPlacemark, new String[] {Integer.toString(player), letterId});

        Boolean pressedBefore = false;

        if(cursor.moveToFirst()){
            pressedBefore = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("collected")));
        }

        cursor.close();
        userData.close();
        return pressedBefore;
    }

    private int generateRandomNum(int low, int high){
        Random r = new Random();
        int result = r.nextInt(high-low) + low;
        return result;
    }

    private boolean wasPressed(String tag, String letter){
        Log.i("MarkerPress letter:", letter);
        Log.i("MarkerPress placeID:", tag);

        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        String tableName = "placemarks";
        String selectStringPlacemark = "SELECT * FROM " + tableName + " WHERE " + "id" + " =?"+ " AND " + "placemarkID" + "=?";

        Cursor cursor = userData.rawQuery(selectStringPlacemark, new String[] {Integer.toString(player), tag});

        Boolean pressedBefore;

        if(cursor.moveToFirst()){

            Log.i("id", cursor.getString(cursor.getColumnIndex("id")));
            String markerID = cursor.getString(cursor.getColumnIndex("id"));

            Log.i("placemarkID", cursor.getString(cursor.getColumnIndex("placemarkID")));
            String markerPlacemarkID = cursor.getString(cursor.getColumnIndex("placemarkID"));

            Log.i("pointLat", cursor.getString(cursor.getColumnIndex("pointLat")));
            String markerPointLat = cursor.getString(cursor.getColumnIndex("pointLat"));

            Log.i("pointLng", cursor.getString(cursor.getColumnIndex("pointLng")));
            String markerPointLng = cursor.getString(cursor.getColumnIndex("pointLng"));

            Log.i("superLetter", cursor.getString(cursor.getColumnIndex("superLetter")));
            String markerSuperLetter = cursor.getString(cursor.getColumnIndex("superLetter"));

            Log.i("letter", cursor.getString(cursor.getColumnIndex("letter")));
            String markerLetter = cursor.getString(cursor.getColumnIndex("letter"));

//            Log.i("collected", cursor.getString(cursor.getColumnIndex("collected")));
//            String markerCollected = cursor.getString(cursor.getColumnIndex("collected"));

            pressedBefore = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("collected")));

            if (!pressedBefore){
                showToast(letter + " collected");
                Log.i("ANNOUNCEMENT", "First press of the marker");
                ContentValues newPlacemarkObject = new ContentValues();
                newPlacemarkObject.put("id", markerID);
                newPlacemarkObject.put("placemarkID", markerPlacemarkID);
                newPlacemarkObject.put("pointLat", markerPointLat);
                newPlacemarkObject.put("pointLng", markerPointLng);
                newPlacemarkObject.put("superLetter", markerSuperLetter);
                newPlacemarkObject.put("letter", markerLetter);
                newPlacemarkObject.put("collected", "true");

                userData.update(tableName, newPlacemarkObject, "placemarkID='"+markerPlacemarkID + "' AND id='" + Integer.toString(player) + "'", null);
            } else {
                Log.i("ANNOUNCEMENT", "Was pressed before");
            }
        } else {
            pressedBefore = true;
        }
        cursor.close();
        userData.close();
        return pressedBefore;
    }

    private void showToast(String text)
    {
        if(currentToast == null)
        {
            currentToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        }

        currentToast.setText(text);
        currentToast.setDuration(Toast.LENGTH_LONG);
        currentToast.show();
    }


    private void markerInProximityOrClicked(Marker marker){
        String placemarkID = marker.getId();
        String letter = marker.getTitle();
        String tag = (String) marker.getTag();
        Log.i("Marker id is", placemarkID);
        Log.i("Marker title is", letter);
        Log.i("Marker tag is", tag);

        //Making DB check if the marker was pressed before, if true - pressed before or marker not found in DB, if false - the press was first
        Boolean markerPressed = wasPressed(tag, letter);
        if (!markerPressed){
            addToInventory(letter);
            Log.i("onClick", "MARKER PRESSED");
            Log.i("onClick marker tag", tag.replaceAll("\\D+",""));
            Log.i("onClick superLetter tag", getSuperLetterId().replaceAll("\\D+",""));
            Log.i("onClick comparison", String.valueOf(getSuperLetterId().replaceAll("\\D+","").equalsIgnoreCase(tag.replaceAll("\\D+",""))));
            if (isBooleanSettingOn("superLetter")) {
                String superLetterId = getSuperLetterId().replaceAll("\\D+","");
                if (superLetterId.equalsIgnoreCase(tag.replaceAll("\\D+",""))){
                    String randomLetter1 = generateRandomLetter();
                    Log.i("LETTER GENER", randomLetter1);
                    addToInventory(randomLetter1);
                    String randomLetter2 = generateRandomLetter();
                    Log.i("LETTER GENER", randomLetter2);
                    addToInventory(randomLetter2);
                }
            }
        }
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
    }


    private void setMarkerClickListener(GoogleMap map) {
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                markerInProximityOrClicked(marker);
                return true;
            }
        });
    }

    private String generateRandomLetter(){
        Random r = new Random();
        char c = (char) (r.nextInt(26) + 'a');
        return Character.toString(c).toUpperCase();
    }

    private void addToInventory(String letter){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        String tableName = "inventory";
        String selectStringLetter = "SELECT * FROM " + tableName + " WHERE " + "id" + " =?"+ " AND " + "letter" + "=?";

        Cursor cursor = userData.rawQuery(selectStringLetter, new String[] {Integer.toString(player), letter});

        if(cursor.moveToFirst()){

            Log.i("id", cursor.getString(cursor.getColumnIndex("id")));
            String cursorId = cursor.getString(cursor.getColumnIndex("id"));

            Log.i("letter", cursor.getString(cursor.getColumnIndex("letter")));
            String cursorLetter = cursor.getString(cursor.getColumnIndex("letter"));

            Log.i("amount", cursor.getString(cursor.getColumnIndex("amount")));
            Integer cursorAmount = cursor.getInt(cursor.getColumnIndex("amount"));

            ContentValues newPlacemarkObject = new ContentValues();
            newPlacemarkObject.put("id", cursorId);
            newPlacemarkObject.put("letter", cursorLetter);
            newPlacemarkObject.put("amount", cursorAmount+1);
            Log.i("newObject", newPlacemarkObject.toString());

            userData.update(tableName, newPlacemarkObject, "letter ='" + cursorLetter + "'" + " and id = '" + cursorId + "'", null);

        } else {
            Log.i("Error!!!", "addToInventory failed");
        }
        cursor.close();
        userData.close();
    }

    private Boolean checkDate(){
        Boolean sameDay = false;
        Log.i("Get Date", getDate());
        String date = getDate();
        Log.i("Last Date", getLastDate());
        String lastDate = getLastDate();
        Log.i("Date comparison","Started");
        if (date.equalsIgnoreCase(lastDate)){
            Log.i("Date check","passed");
            sameDay = true;
        } else {
            Log.i("Date check","failed");
            setDate();
        }
        Log.i("Date comparison","Ended");
        return sameDay;
    }

    public String getDate(){
        Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        String date = year + "-" + month + "-" + day;
        return date;
    }

    private String getLastDate(){
        //CHANGE TO VERSION WITH DATABASE
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        String selectString = "SELECT * FROM settings WHERE " + "id" + " =?";
        Cursor cursor = userData.rawQuery(selectString, new String[]{Integer.toString(player)});
        int settingIndex = cursor.getColumnIndex("lastUse");

        cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            String date;
            Log.i("getLastDate", "Succeeded");
            Log.i("lastUse", cursor.getString(settingIndex));
            date = cursor.getString(settingIndex);
            cursor.close();
            userData.close();
            return date;
        } else {
            Log.i("Did", "getCurrentBoolSetting");
            cursor.close();
            userData.close();
            return "8888-88-88";
        }
    }

    private void setDate(){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        String tableName = "settings";
        String selectStringLetter = "SELECT * FROM " + tableName + " WHERE " + "id" + " =?";

        Cursor cursor = userData.rawQuery(selectStringLetter, new String[] {Integer.toString(player)});

        if(cursor.moveToFirst()){
            cursor.moveToFirst();
            String cursorId = cursor.getString(cursor.getColumnIndex("id"));
            String cursorNightMode = cursor.getString(cursor.getColumnIndex("nightMode"));
            String cursorPowerSaving = cursor.getString(cursor.getColumnIndex("powerSaving"));
            String cursorAutoCollect = cursor.getString(cursor.getColumnIndex("autoCollect"));
            String cursorSuperLetter = cursor.getString(cursor.getColumnIndex("superLetter"));
            String cursorVisibilityRad = cursor.getString(cursor.getColumnIndex("visibilityRad"));
            String cursorOverlay = cursor.getString(cursor.getColumnIndex("overlay"));
//            String cursorLastUse = cursor.getString(cursor.getColumnIndex("lastUse"));

            ContentValues newObject = new ContentValues();
            newObject.put("id", cursorId);
            newObject.put("nightMode", cursorNightMode);
            newObject.put("powerSaving", cursorPowerSaving);
            newObject.put("autoCollect", cursorAutoCollect);
            newObject.put("superLetter", cursorSuperLetter);
            newObject.put("visibilityRad", cursorVisibilityRad);
            newObject.put("overlay", cursorOverlay);
            newObject.put("lastUse", getDate());
            Log.i("newObject", newObject.toString());
            userData.update(tableName, newObject, "id = '" + cursorId + "'", null);
        } else {
            Log.i("Error!!!", "setDate failed");
        }
        cursor.close();
        userData.close();
    }

    private String getSelectedStyle(){
        String style;
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS settings (id INTEGER PRIMARY KEY, nightMode boolean, powerSaving boolean, autoCollect boolean, superLetter boolean, visibilityRad INTEGER, overlay INTEGER, lastUse varchar(20))");

        String selectString = "SELECT * FROM settings WHERE " + "id" + " =?";
        Cursor cursor = userData.rawQuery(selectString, new String[]{Integer.toString(player)});

        int nightModeIndex = cursor.getColumnIndex("nightMode");
        int overlayIndex = cursor.getColumnIndex("overlay");

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            String nightMode = cursor.getString(nightModeIndex);
            if (nightMode.equalsIgnoreCase("1")){
                style = "nightmode_json";
            }else{
                int overlay = Integer.parseInt(cursor.getString(overlayIndex));
                style = "style" + overlay + "_json";
            }
        } else {
            Log.e("Style Selection", "crashed");
            style = "null";
        }
        cursor.close();
        userData.close();
        return style;
    }

    private int getSetDistance(){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS settings (id INTEGER PRIMARY KEY, nightMode boolean, powerSaving boolean, autoCollect boolean, superLetter boolean, visibilityRad INTEGER, overlay INTEGER, lastUse varchar(20))");

        String selectString = "SELECT * FROM settings WHERE " + "id" + " =?";
        Cursor cursor = userData.rawQuery(selectString, new String[]{Integer.toString(player)});

        int settingIndex = cursor.getColumnIndex("visibilityRad");

        cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            int intValue = Integer.parseInt(cursor.getString(settingIndex));
            cursor.close();
            userData.close();
            Log.i("getSetDistance", "succeeded");
            return intValue;
        }
        cursor.close();
        userData.close();
        Log.i("getSetDistance", "failed");
        return -1;
    }

    private boolean getCurrentBoolSetting(String settingType) {

        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS settings (id INTEGER PRIMARY KEY, nightMode boolean, powerSaving boolean, autoCollect boolean, superLetter boolean, visibilityRad INTEGER, overlay INTEGER, lastUse varchar(20))");

        String selectString = "SELECT * FROM settings WHERE " + "id" + " =?";
        Cursor cursor = userData.rawQuery(selectString, new String[]{Integer.toString(player)});

        int settingIndex = cursor.getColumnIndex(settingType);

        cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            boolean setting;
            Log.i(settingType, cursor.getString(settingIndex));
            if (cursor.getString(settingIndex).equalsIgnoreCase("1")){
                setting = true;
            }else {
                setting = false;
            }
            cursor.close();
            userData.close();
            return setting;
        }
        cursor.close();
        userData.close();

        return false;
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
        intent.putExtra("userId", Integer.toString(player));
        startActivity(intent);
    }

    public void toSettingsActivity(View view){
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        intent.putExtra("userId", Integer.toString(player));
        startActivity(intent);
    }

    public void toWordInputActivity(View view){
        Intent intent = new Intent(getApplicationContext(), WordInputActivity.class);
        intent.putExtra("userId", Integer.toString(player));
        startActivity(intent);
    }
}
