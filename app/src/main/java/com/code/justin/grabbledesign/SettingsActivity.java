package com.code.justin.grabbledesign;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    private int player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Intent intent = getIntent();
        player = Integer.parseInt(intent.getStringExtra("userId"));

        setBooleanOptions();
        setIntegerOptions();

        //Function to get current player settings
        //getCurrentSettings();
        //Check if Auto letter collection is enabled, if yes - set the switch to TRUE, else, set switch to false
        //Check if daily super letter is enabled, if yes - set the switch to TRUE, else, set switch to false
        //Night mode as another layout that you can select in the HorizontalScrollView of layouts
        //Function to check which layout is selected at the moment and set the img_clicked on the right layout

        //Visibility radius
    }

    private void setBooleanOptions(){
        Switch nightModeSwitch = (Switch) findViewById(R.id.nightMode);
        nightModeSwitch.setChecked(getCurrentBoolSetting("nightMode"));

        Switch powerSavingSwitch = (Switch) findViewById(R.id.powerSaving);
        powerSavingSwitch.setChecked(getCurrentBoolSetting("powerSaving"));

        Switch autoCollectSwitch = (Switch) findViewById(R.id.autoCollect);
        autoCollectSwitch.setChecked(getCurrentBoolSetting("autoCollect"));

        Switch superLetterSwitch = (Switch) findViewById(R.id.superLetter);
        superLetterSwitch.setChecked(getCurrentBoolSetting("superLetter"));
    }

    private void setIntegerOptions(){
        setSlider();
        setSelectedLayout();
    }

    private void setSlider(){
        int visibilityRad = getCurrentIntSetting("visibilityRad");
        SeekBar radiusSlider = (SeekBar) findViewById(R.id.radiusSlider);
        radiusSlider.setProgress(visibilityRad);
    }

    private void setSelectedLayout(){
        int overlayID = getCurrentIntSetting("overlay");
        String imageID = "img" + overlayID;
        int resID = getResources().getIdentifier(imageID, "id", getPackageName());
        ImageView img = (ImageView) findViewById(resID);
        Log.i("the image id is", imageID);
        String imageIDclicked = "img" + overlayID + "small_clicked";
        int resIDclicked = getResources().getIdentifier(imageIDclicked, "drawable", getPackageName());
        img.setImageResource(resIDclicked);
    }

    private boolean getCurrentBoolSetting(String settingType) {

        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS settings (id INTEGER PRIMARY KEY, nightMode boolean, powerSaving boolean, autoCollect boolean, superLetter boolean, visibilityRad INTEGER, overlay INTEGER)");

        String selectString = "SELECT * FROM settings WHERE " + "id" + " =?";
        Cursor cursor = userData.rawQuery(selectString, new String[]{Integer.toString(player)});

        int settingIndex = cursor.getColumnIndex(settingType);

        cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            boolean setting = Boolean.parseBoolean(cursor.getString(settingIndex));
            return setting;
        }
        cursor.close();
        userData.close();

        return false;
    }

    private int getCurrentIntSetting(String settingType) {

        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS settings (id INTEGER PRIMARY KEY, nightMode boolean, powerSaving boolean, autoCollect boolean, superLetter boolean, visibilityRad INTEGER, overlay INTEGER)");

        String selectString = "SELECT * FROM settings WHERE " + "id" + " =?";
        Cursor cursor = userData.rawQuery(selectString, new String[]{Integer.toString(player)});

        int settingIndex = cursor.getColumnIndex(settingType);

        cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            int setting = Integer.parseInt(cursor.getString(settingIndex));
            return setting;
        }
        cursor.close();
        userData.close();

        return -1;
    }

    public void toChangePassActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), ChangePassActivity.class);
        startActivity(intent);
    }

    public void toMapsActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
    }


}
