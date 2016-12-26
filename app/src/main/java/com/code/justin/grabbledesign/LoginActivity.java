package com.code.justin.grabbledesign;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import static android.app.PendingIntent.getActivity;

public class LoginActivity extends AppCompatActivity {

    public class SubmittedUser {

        private boolean allowed;
        private int id;

        public SubmittedUser(boolean allowed, int id) {
            this.allowed = allowed;
            this.id = id;
        }

        public boolean getAllowed() {
            return allowed;
        }

        public void setAllowed(boolean allowed) {
            this.allowed = allowed;
        }

        public int getId() {
            return this.id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }


    SubmittedUser tryingToAccess = new SubmittedUser(false, -1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Database Initialisation
        try{
            SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
            userData.execSQL("CREATE TABLE IF NOT EXISTS accounts (nickname VARCHAR(20), email VARCHAR(20), password VARCHAR(20), id INTEGER PRIMARY KEY)");
            userData.execSQL("DELETE FROM accounts");
            userData.execSQL("INSERT INTO accounts (nickname, email, password) VALUES ('ferame', 'alisauskas.j@gmail.com', 'somepass')");
            userData.execSQL("INSERT INTO accounts (nickname, email, password) VALUES ('darakan', 'justinas.ali@zebra.lt', 'somepass2')");

            Cursor c = userData.rawQuery("SELECT * FROM accounts", null);
            int nameIndex = c.getColumnIndex("nickname");
            int emailIndex = c.getColumnIndex("email");
            int passwordIndex = c.getColumnIndex("password");
            int idIndex = c.getColumnIndex("id");
            c.moveToFirst();
            while (c != null){
                Log.i("nickname", c.getString(nameIndex));
                Log.i("email", c.getString(emailIndex));
                Log.i("password", c.getString(passwordIndex));
                Log.i("id", c.getString(idIndex));
                c.moveToNext();
            }
            c.close();
            userData.close();
        }
        catch (Exception e){
//            e.printStackTrace();
        }

    }

    public void toMapsActivity (View view){
        AutoCompleteTextView emailNicknameInput = (AutoCompleteTextView)findViewById(R.id.email);
        String emailNickname = emailNicknameInput.getText().toString();
        Log.i("Got email/nickname", emailNickname);
        EditText passwordInput = (EditText)findViewById(R.id.password);
        String password = passwordInput.getText().toString();
        Log.i("Got password", password);

        //Check if this exists in database
        boolean AllowEntrance = hasObject("accounts", emailNickname, password);

        if (AllowEntrance){
            int id = tryingToAccess.getId();
            Log.i("FOUND USER, HIS ID IS: ", Integer.toString(id));
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            intent.putExtra("userId", Integer.toString(id));
            startActivity(intent);
        }else{
            Toast.makeText(getApplicationContext(), "Wrong Email or Password", Toast.LENGTH_LONG).show();
        }
    }

    public void toCreateAccActivity(View view){
        Intent intent = new Intent(getApplicationContext(), CreateAccActivity.class);
        startActivity(intent);
    }

    /*public boolean hasObjectOTHER(String tableName, String emailNickname, String password) {
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);

        String selectStringEmail = "SELECT * FROM " + tableName + " WHERE " + emailNickname +
                " = " +"email" + " AND " + password + " = " + "password";

        String selectStringNickname = "SELECT * FROM " + tableName + " WHERE " + emailNickname +
                " = " +"nickname" + " AND " + password + " = " + "password";

        boolean hasObject = false;
        Cursor cursor = userData.rawQuery(selectStringEmail, null);
        if (cursor != null){
            if(cursor.getCount() > 0){
                hasObject = true;
            }
        } else {
            cursor = userData.rawQuery(selectStringNickname, null);
            if (cursor != null){
                if (cursor.getCount() > 0){
                    hasObject = true;
                }
            }
        }

        if(cursor != null){
            cursor.close();
        }
        userData.close();
        return hasObject;
    }*/

    public boolean hasObject(String tableName, String emailNickname, String password) {
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);

        String selectStringEmail = "SELECT * FROM " + tableName + " WHERE " + "email" + " =?"+ " AND " + "password" + "=?";
        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        Cursor cursor = userData.rawQuery(selectStringEmail, new String[] {emailNickname, password});
        boolean hasObject = false;

        if(cursor.moveToFirst()){
            hasObject = true;
            Log.i("found", String.valueOf(hasObject));
            Log.i("email", cursor.getString(cursor.getColumnIndex("email")));
            Log.i("password", cursor.getString(cursor.getColumnIndex("password")));
            Log.i("id found", cursor.getString(cursor.getColumnIndex("id")));
            tryingToAccess.setAllowed(true);
            Log.i("IS ALLOWED AFTER?", Boolean.toString(tryingToAccess.getAllowed()));
            Integer id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));
            tryingToAccess.setId(id);
            Log.i("IT'S ID AFTER?", Integer.toString(id));
        }

        if(!hasObject){
            String selectStringNickname = "SELECT * FROM " + tableName + " WHERE " + "nickname" + " =?"+ " AND " + "password" + "=?";
            //cursor.close();
            cursor = userData.rawQuery(selectStringNickname, new String[] {emailNickname, password});
            if(cursor.moveToFirst()){
                hasObject = true;
                Log.i("found", String.valueOf(hasObject));
                Log.i("nickname", cursor.getString(cursor.getColumnIndex("nickname")));
                Log.i("password", cursor.getString(cursor.getColumnIndex("password")));
                Log.i("id found", cursor.getString(cursor.getColumnIndex("id")));
                tryingToAccess.setAllowed(true);
                Log.i("IS ALLOWED AFTER?", Boolean.toString(tryingToAccess.getAllowed()));
                Integer id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));
                tryingToAccess.setId(id);
                Log.i("IT'S ID AFTER?", Integer.toString(id));
            }
        }

        cursor.close();          // Dont forget to close your cursor
        userData.close();              //AND your Database!
        return hasObject;
    }
}
