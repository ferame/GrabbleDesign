package com.code.justin.grabbledesign;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePassActivity extends AppCompatActivity {

    private int player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);
        Intent intent = getIntent();
        player = Integer.parseInt(intent.getStringExtra("userId"));
    }

    public void confirmButtonClicked(View view){
        EditText passInput = (EditText)findViewById(R.id.password);
        String pass = passInput.getText().toString();
        Log.i("Got pass", pass);
        EditText passwordInput = (EditText)findViewById(R.id.confirmpassword);
        String passRepeat = passwordInput.getText().toString();
        Log.i("Got passRepeat", passRepeat);

        if (pass.equals(passRepeat)){
            if (pass.length() > 20){
                Toast.makeText(getApplicationContext(), "Password can be up to 20 characters long", Toast.LENGTH_LONG).show();
            } else if (pass.length() == 0){
                Toast.makeText(getApplicationContext(), "Password field is empty", Toast.LENGTH_LONG).show();
            } else {
                setNewPass(pass);
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("userId", Integer.toString(player));
                startActivity(intent);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_LONG).show();
        }
    }

    private void setNewPass(String newPassword){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        String tableName = "accounts";
        String selectStringPlacemark = "SELECT * FROM " + tableName + " WHERE " + "id" + " =?";

        Cursor cursor = userData.rawQuery(selectStringPlacemark, new String[] {Integer.toString(player)});

        if(cursor.moveToFirst()) {

            Log.i("nickname", cursor.getString(cursor.getColumnIndex("nickname")));
            String nickname = cursor.getString(cursor.getColumnIndex("nickname"));

            Log.i("email", cursor.getString(cursor.getColumnIndex("email")));
            String email = cursor.getString(cursor.getColumnIndex("email"));

            Log.i("password", cursor.getString(cursor.getColumnIndex("password")));
            String password = cursor.getString(cursor.getColumnIndex("password"));

            Log.i("id", cursor.getString(cursor.getColumnIndex("id")));
            String id = cursor.getString(cursor.getColumnIndex("id"));

            ContentValues newPlacemarkObject = new ContentValues();
            newPlacemarkObject.put("nickname", nickname);
            newPlacemarkObject.put("email", email);
            newPlacemarkObject.put("password", newPassword);
            newPlacemarkObject.put("id", id);

            userData.update(tableName, newPlacemarkObject, "id='" + player + "'", null);
        }
        cursor.close();
        userData.close();
    }
}
