package com.code.justin.grabbledesign;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAccActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_acc);
    }

    public void toLoginActivity(View view){
        accountCreationStart();
    }

    private void accountCreationStart(){
        EditText nicknameInput = (EditText)findViewById(R.id.nickname);
        String nickname = nicknameInput.getText().toString();

        EditText emailInput = (EditText)findViewById(R.id.email);
        String email = emailInput.getText().toString();

        EditText passwordInput = (EditText)findViewById(R.id.password);
        String password = passwordInput.getText().toString();

        Boolean isSuccessful = createAcc(nickname, email, password);

        if (isSuccessful){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        } else {
            Context context = getApplicationContext();
            Toast.makeText(context,"Failed to create account", Toast.LENGTH_LONG).show();
        }
    }

    private boolean createAcc(String nicknameInput, String emailInput, String passwordInput){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS accounts (nickname VARCHAR(20), email TEXT, password TEXT, id INTEGER PRIMARY KEY)");
        if (checkInputs(nicknameInput, emailInput, passwordInput)){
            String encryptedPass = helperFunctions.md5(passwordInput);
            userData.execSQL("INSERT INTO accounts (nickname, email, password) VALUES ('" + nicknameInput + "', '" +  emailInput + "', '" + encryptedPass +"')");
            createUserInventoryTable(findUserId(nicknameInput));
            createUserSettingsTable(findUserId(nicknameInput));
            Log.i("user creation", "done, should be.");
        } else {
            userData.close();
            return false;
        }

        userData.close();
        return true;
    }

    public boolean checkInputs(String nicknameInput, String emailInput, String passwordInput){
        if (checkNickname(nicknameInput)){
            if (checkEmail(emailInput)){
                if (checkPassword(passwordInput)){
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean checkNickname(String nicknameInput){
        if (isNicknamic(nicknameInput)){
            if (!checkIsDataAlreadyInDB("accounts", "nickname", nicknameInput)){
                return true;
            }else {
                Context context = getApplicationContext();
                Toast lengthToast = Toast.makeText(context, "Nickname is already in use", Toast.LENGTH_LONG);
                lengthToast.show();
                return false;
            }
        }else{
            if (nicknameInput.length() > 0 && nicknameInput.length() <= 20) {
                Context context = getApplicationContext();
                Toast lengthToast = Toast.makeText(context, "Nickname can be at most 20 characters long.", Toast.LENGTH_LONG);
                lengthToast.show();
                return false;
            } else if (nicknameInput.isEmpty()) {
                Context context = getApplicationContext();
                Toast lengthToast = Toast.makeText(context, "Nickname has to be at least 1 character long.", Toast.LENGTH_LONG);
                lengthToast.show();
                return false;
            } else {
                    Context context = getApplicationContext();
                    Toast charTypeToast = Toast.makeText(context, "Nickname can contain only letters and numbers", Toast.LENGTH_LONG);
                    charTypeToast.show();
                    return false;
            }
        }
    }

    public boolean checkEmail(String emailInput){
        if (isEmailic(emailInput)){
            if (!checkIsDataAlreadyInDB("accounts", "email", emailInput)){
                return true;
            }else {
                Context context = getApplicationContext();
                Toast lengthToast = Toast.makeText(context, "Email is already in use", Toast.LENGTH_LONG);
                lengthToast.show();
                return false;
            }
        }else{
            Context context = getApplicationContext();
            Toast charTypeToast = Toast.makeText(context, "That doesn't look like a valid email address", Toast.LENGTH_LONG);
            charTypeToast.show();
            return false;
        }
    }

    public boolean checkPassword(String passwordInput){
        if (isPasswordic(passwordInput)){
            return true;
        }else {
            if (passwordInput.length() > 20) {
                Context context = getApplicationContext();
                Toast charTypeToast = Toast.makeText(context, "Password can be at most 20 characters long", Toast.LENGTH_LONG);
                charTypeToast.show();
                return false;
            } else {
                Context context = getApplicationContext();
                Toast charTypeToast = Toast.makeText(context, "Password has to be at least 1 character long", Toast.LENGTH_LONG);
                charTypeToast.show();
                return false;
            }
        }
    }

    public static boolean isNicknamic(String s){
        if (s == null || s.isEmpty()){
            return false;
        }
        if (s.length() > 20 ){
            return false;
        }
        String pattern= "^[a-zA-Z0-9]*$";
        return s.matches(pattern);
    }

    public static boolean isEmailic(String s){
        if (s == null){
            return false;
        }
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = s;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    public static boolean isPasswordic(String s){
        if (s != null && s.length() > 0 && s.length() <= 20 && !s.isEmpty()){
            return true;
        } else {
            return false;
        }
    }

    public boolean checkIsDataAlreadyInDB(String TableName, String dbfield, String fieldValue) {
        Log.i("On table", TableName);
        Log.i("Checking dbfield", dbfield);
        Log.i("With value", fieldValue);

        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);

        Cursor cursor = userData.rawQuery("select 1 from " + TableName + " where " + dbfield + "=?",
                new String[] { fieldValue });
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public void createUserInventoryTable(Integer idInput){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS inventory (id INTEGER, letter VARCHAR(1), amount INTEGER, PRIMARY KEY(id,letter))");

        for(char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
//            System.out.println(alphabet);
            userData.execSQL("INSERT INTO inventory (id, letter, amount) VALUES (" + idInput + ", '" +  alphabet + "', " + 0 +")");
        }
        userData.close();
    }

    public void createUserSettingsTable(Integer idInput){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS settings (id INTEGER PRIMARY KEY, nightMode boolean, powerSaving boolean, autoCollect boolean, superLetter boolean, visibilityRad INTEGER, overlay INTEGER, lastUse varchar(20))");
        userData.execSQL("INSERT INTO settings (id, nightMode, powerSaving, autoCollect, superLetter, visibilityRad, overlay, lastUse) VALUES (" + idInput + ", " + 0 + ", " + 0 + ", " + 0 + ", " + 1 + ", " + 50 +", " + 1 + ", " + "9999-99-99" + ")");
        userData.close();
    }

    public Integer findUserId(String inputNickname){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        String selectStringId = "SELECT * FROM " + "accounts" + " WHERE " + "nickname" + " =?";

        Cursor cursor = userData.rawQuery(selectStringId, new String[] {inputNickname});

        if(cursor.moveToFirst()){
            Log.i("Found userID", cursor.getString(cursor.getColumnIndex("id")));
            Integer id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));
            return id;
        }
        return null;
    }
}
