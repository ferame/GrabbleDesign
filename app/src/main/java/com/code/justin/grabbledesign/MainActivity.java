package com.code.justin.grabbledesign;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toChangePassActivity(View view){
        Intent intent = new Intent(getApplicationContext(), ChangePassActivity.class);
        startActivity(intent);
    }

    public void toCreateAccountActivity(View view){
        Intent intent = new Intent(getApplicationContext(), CreateAccountActivity.class);
        startActivity(intent);
    }

    public void toWordInputActivity(View view){
        Intent intent = new Intent(getApplicationContext(), WordInputActivity.class);
        startActivity(intent);
    }

    public void toLoginActivity(View view){
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void toMapsActivity(View view){
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
    }

    public void toRecoveryActivity(View view){
        Intent intent = new Intent(getApplicationContext(), RecoveryActivity.class);
        startActivity(intent);
    }

    public void toInventoryActivity(View view){
        Intent intent = new Intent(getApplicationContext(), InventoryActivity.class);
        startActivity(intent);
    }

    public void toSettingsActivity(View view){
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }
}
