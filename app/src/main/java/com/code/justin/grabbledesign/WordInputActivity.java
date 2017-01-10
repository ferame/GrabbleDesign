package com.code.justin.grabbledesign;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.phenotype.Flag;

public class WordInputActivity extends AppCompatActivity implements View.OnClickListener{

    private int Player;

    private Letter[] lettersParam = new Letter[26];

    Button[] buttons = new Button[28];

    class Letter{
        int count;

        private void setCount(int newCount){
            count = newCount;
        }

        private int getCount(){
            return count;
        }

        Character letter;

        private void setLetter(Character newLetter){
            letter = newLetter;
        }

        private Character getLetter(){
            return letter;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_word_input);
        Intent intent = getIntent();
        Player = Integer.parseInt(intent.getStringExtra("userId"));

        Log.i("Populate", "Starts");
        populateLettersParam();
        Log.i("Populate", "Ends");

//        Button[] buttons = new Button[28];
        Integer i = 0;
        for(char alphabet = 'A'; alphabet <= 'Z';alphabet++) {
            String buttonID = alphabet + "letter";

            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = ((Button) findViewById(resID));
            buttons[i].setOnClickListener(this);
            i++;
        }

        Log.i("Button Count info", "Starts");
        setLetterButtonCounts();
        Log.i("Button Count info", "Ends");

        Log.i("Button Colouring", "Starts");
        setLetterButtonColours();
        Log.i("Button Colouring", "Ends");
//        for (int i = 0; i < buttons.length; i++) {
//            {
//                String buttonID = "sound" + (i + 1);
//
//                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
//                buttons[i] = ((Button) findViewById(resID));
//                buttons[i].setOnClickListener(this);
//            }
//
////            Button one = (Button) findViewById(R.id.oneButton);
////            one.setOnClickListener(this); // calling onClick() method
////            Button two = (Button) findViewById(R.id.twoButton);
////            two.setOnClickListener(this);
////            Button three = (Button) findViewById(R.id.threeButton);
////            three.setOnClickListener(this);
//
//        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.Aletter:
                Log.i("Keyboard", "A pressed");
                int letterCount = getLetterCount('A');
                if (letterCount == -1){
                    Log.i("Error", "Letter count invalid");
                }else if (letterCount == 0){
                    Toast.makeText(getApplicationContext(), "No A's remaining", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "A pressed", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.Bletter:
                Log.i("Keyboard", "B pressed");
                break;

            case R.id.Cletter:
                Log.i("Keyboard", "C pressed");

                break;

            case R.id.Dletter:
                // do your code
                break;

            case R.id.Eletter:
                // do your code
                break;

            case R.id.Fletter:
                // do your code
                break;

            case R.id.Gletter:
                // do your code
                break;

            case R.id.Hletter:
                // do your code
                break;

            case R.id.Iletter:
                // do your code
                break;

            case R.id.Jletter:
                // do your code
                break;

            case R.id.Kletter:
                // do your code
                break;

            case R.id.Lletter:
                // do your code
                break;

            case R.id.Mletter:
                // do your code
                break;

            case R.id.Nletter:
                // do your code
                break;

            case R.id.Oletter:
                // do your code
                break;

            case R.id.Pletter:
                // do your code
                break;

            case R.id.Qletter:
                // do your code
                break;

            case R.id.Rletter:
                // do your code
                break;

            case R.id.Sletter:
                // do your code
                break;

            case R.id.Tletter:
                // do your code
                break;

            case R.id.Uletter:
                // do your code
                break;

            case R.id.Vletter:
                // do your code
                break;

            case R.id.Wletter:
                // do your code
                break;

            case R.id.Xletter:
                // do your code
                break;

            case R.id.Yletter:
                // do your code
                break;

            case R.id.Zletter:
                // do your code
                break;

            case R.id.backSpace1:
                break;

            case R.id.backSpace2:
                break;

            default:
                break;
        }
    }

//    new function
    private int getLetterCount(char letter){
        for (int i = 0; i < 26; i++){
            if (lettersParam[i].getLetter() == letter){
                return lettersParam[i].getCount();
            }
        }
        return -1;
    }

    private int getLetterIndex(char letter){
        for (int i = 0; i < 26; i++){
            if (lettersParam[i].getLetter() == letter){
                return i;
            }
        }
        return -1;
    }

    private void populateLettersParam(){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        String tableName = "inventory";
        String selectStringPlacemark = "SELECT * FROM " + tableName + " WHERE " + "id" + " =?";

        Cursor c = userData.rawQuery(selectStringPlacemark, new String[] {Integer.toString(Player)});
        int letterIndex = c.getColumnIndex("letter");
        int amountIndex = c.getColumnIndex("amount");
        c.moveToFirst();
        Integer count = 0;
        while (c != null){
            Log.i("getting letter", "ok?");
            Character letter = c.getString(letterIndex).charAt(0);
            Log.i("Found letter", letter.toString());
            Integer amount = Integer.parseInt(c.getString(amountIndex));
            lettersParam[count] = new Letter();
            lettersParam[count].setLetter(letter);
            lettersParam[count].setCount(amount);

            count++;

            if (count != 26) {
                c.moveToNext();
            } else {
                break;
            }
        }
        c.close();
        userData.close();
    }

    private void setLetterButtonCounts(){
        for (int i = 0; i < 26; i++){
            Character letter = lettersParam[i].getLetter();
            Integer count = lettersParam[i].getCount();

            String toShow = letter + count.toString();
            SpannableStringBuilder showSpan = new SpannableStringBuilder(toShow);
            showSpan.setSpan(new SuperscriptSpan(), 1, showSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            showSpan.setSpan(new RelativeSizeSpan(0.7f), 1, showSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            buttons[i].setText(showSpan);
        }
    }

    private void setLetterButtonColours(){
        for (int i = 0; i < 26; i++){
            Integer count = lettersParam[i].getCount();

            if (count > 3){
                buttons[i].setBackgroundResource(R.drawable.keyboard_button_border);
            } else if ( count > 0){
                buttons[i].setBackgroundResource(R.drawable.keyboard_button_border_low);
            } else {
                buttons[i].setBackgroundResource(R.drawable.keyboard_button_border_disabled);
            }
        }
    }
}
