package com.code.justin.grabbledesign;

import android.content.ContentValues;
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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private int player;

    Toast currentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Intent intent = getIntent();
        player = Integer.parseInt(intent.getStringExtra("userId"));

        setBooleanOptions();
        setIntegerOptions();

        setSwitchesListeners();
        setSliderListener();
        setLayoutListeners();
        //Function to check which layout is selected at the moment and set the img_clicked on the right layout
        //Visibility radius
    }

    private void setBooleanOptions(){
        Switch nightModeSwitch = (Switch) findViewById(R.id.nightMode);
        Log.i("NightMode",Boolean.toString(getCurrentBoolSetting("nightMode")));
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

    private void setSwitchesListeners(){
        Switch nightModeSwitch = (Switch)  findViewById(R.id.nightMode);
        nightModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("NightMode Switch State=", ""+isChecked);
                updateUserSettings("nightMode", getBinary(isChecked));
            }
        });

        Switch powerSavingSwitch = (Switch)  findViewById(R.id.powerSaving);
        powerSavingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("PowerSave Switch State=", ""+isChecked);
                updateUserSettings("powerSaving", getBinary(isChecked));
            }
        });

        Switch autoCollectSwitch = (Switch)  findViewById(R.id.autoCollect);
        autoCollectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("autoColl Switch State=", ""+isChecked);
                updateUserSettings("autoCollect", getBinary(isChecked));
            }
        });

        Switch superLetterSwitch = (Switch)  findViewById(R.id.superLetter);
        superLetterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("superLett Switch State=", ""+isChecked);
                updateUserSettings("superLetter", getBinary(isChecked));
            }
        });
    }

    private int getBinary(Boolean isChecked){
        if (isChecked){
            return 1;
        } else {
            return 0;
        }
    }
    private void updateUserSettings(String setting, int newValue){
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
            String cursorLastUse = cursor.getString(cursor.getColumnIndex("lastUse"));

            ContentValues newObject = new ContentValues();
            newObject.put("id", cursorId);

            if (setting.equalsIgnoreCase("nightMode")){
                newObject.put("nightMode", newValue);
            }else{
                newObject.put("nightMode", cursorNightMode);
            }

            if (setting.equalsIgnoreCase("powerSaving")){
                newObject.put("powerSaving", newValue);
            }else{
                newObject.put("powerSaving", cursorPowerSaving);
            }

            if (setting.equalsIgnoreCase("autoCollect")){
                newObject.put("autoCollect", newValue);
            }else{
                newObject.put("autoCollect", cursorAutoCollect);
            }

            if (setting.equalsIgnoreCase("superLetter")){
                newObject.put("superLetter", newValue);
            }else{
                newObject.put("superLetter", cursorSuperLetter);
            }

            if (setting.equalsIgnoreCase("visibilityRad")){
                newObject.put("visibilityRad", newValue);
            }else{
                newObject.put("visibilityRad", cursorVisibilityRad);
            }

            if (setting.equalsIgnoreCase("overlay")){
                newObject.put("overlay", newValue);
            }else{
                newObject.put("overlay", cursorOverlay);
            }

            newObject.put("lastUse", cursorLastUse);
            Log.i("newObject", newObject.toString());

            userData.update(tableName, newObject, "id = '" + cursorId + "'", null);

        } else {
            Log.i("Error!!!", "updateUserSetting failed");
        }
        cursor.close();
        userData.close();
    }

    private void setSliderListener(){
        final SeekBar radiusSlider=(SeekBar) findViewById(R.id.radiusSlider);
        radiusSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                int position = seekBar.getProgress();
                Log.i("Visibility radius", Integer.toString(position+20));
                updateUserSettings("visibilityRad", position+20);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                showToast("Visibility radius set to " + String.valueOf(progress+20) + "m");
                //Log.i("Visibility radius", String.valueOf(progress)+"m");
            }
        });
    }

    private void showToast(String text)
    {
        if(currentToast == null)
        {
            currentToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        }

        currentToast.setText(text);
        currentToast.setDuration(Toast.LENGTH_LONG);
        currentToast.show();
    }

    private void setLayoutListeners(){
        ImageView img1 = (ImageView) findViewById(R.id.img1);
        ImageView img2 = (ImageView) findViewById(R.id.img2);
        ImageView img3 = (ImageView) findViewById(R.id.img3);

        img1.setOnClickListener(layoutClick);
        img2.setOnClickListener(layoutClick);
        img3.setOnClickListener(layoutClick);
    }

    View.OnClickListener layoutClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.img1:
                    Log.i("Image pressed:", "first");
                    updateUserSettings("overlay", 1);
                    setSelectedLayout();
                    break;

                case R.id.img2:
                    Log.i("Image pressed:", "second");
                    updateUserSettings("overlay", 2);
                    setSelectedLayout();
                    break;

                case R.id.img3:
                    Log.i("Image pressed:", "third");
                    updateUserSettings("overlay", 3);
                    setSelectedLayout();
                    break;
            }
        }
    };

    private void setSlider(){
        int visibilityRad = getCurrentIntSetting("visibilityRad");
        SeekBar radiusSlider = (SeekBar) findViewById(R.id.radiusSlider);
        radiusSlider.setProgress(visibilityRad);
    }

    private void setSelectedLayout(){
        setImageViewsToDefault();

        int overlayID = getCurrentIntSetting("overlay");
        String imageID = "img" + overlayID;
        int resID = getResources().getIdentifier(imageID, "id", getPackageName());
        ImageView img = (ImageView) findViewById(resID);
        Log.i("the image id is", imageID);
        String imageIDclicked = "img" + overlayID + "small_clicked";
        int resIDclicked = getResources().getIdentifier(imageIDclicked, "drawable", getPackageName());
        img.setImageResource(resIDclicked);
    }

    private void setImageViewsToDefault(){
        ImageView img1= (ImageView) findViewById(R.id.img1);
        img1.setImageResource(R.drawable.img1small);
        ImageView img2= (ImageView) findViewById(R.id.img2);
        img2.setImageResource(R.drawable.img2small);
        ImageView img3= (ImageView) findViewById(R.id.img3);
        img3.setImageResource(R.drawable.img3small);
    }

    private boolean getCurrentBoolSetting(String settingType) {

        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS settings (id INTEGER PRIMARY KEY, nightMode boolean, powerSaving boolean, autoCollect boolean, superLetter boolean, visibilityRad INTEGER, overlay INTEGER)");

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

    private int getCurrentIntSetting(String settingType) {

        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS settings (id INTEGER PRIMARY KEY, nightMode boolean, powerSaving boolean, autoCollect boolean, superLetter boolean, visibilityRad INTEGER, overlay INTEGER)");

        String selectString = "SELECT * FROM settings WHERE " + "id" + " =?";
        Cursor cursor = userData.rawQuery(selectString, new String[]{Integer.toString(player)});

        int settingIndex = cursor.getColumnIndex(settingType);

        cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            int intValue = Integer.parseInt(cursor.getString(settingIndex));
            cursor.close();
            userData.close();
            return intValue;
        }
        cursor.close();
        userData.close();

        return -1;
    }

    public void toChangePassActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), ChangePassActivity.class);
        intent.putExtra("userId", Integer.toString(player));
        startActivity(intent);
    }

    public void toMapsActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("userId", Integer.toString(player));
        startActivity(intent);
    }

}
