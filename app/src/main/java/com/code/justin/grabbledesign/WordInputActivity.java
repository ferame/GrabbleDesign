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

import com.google.android.gms.drive.query.internal.InFilter;
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
        //Setting up backspaces
        buttons[26] = ((Button) findViewById(R.id.backSpace1));
        buttons[26].setOnClickListener(this);

        buttons[27] = ((Button) findViewById(R.id.backSpace2));
        buttons[27].setOnClickListener(this);


        Log.i("Button Count info", "Starts");
        setLetterButtonCounts();
        Log.i("Button Count info", "Ends");

        Log.i("Button Colouring", "Starts");
        setLetterButtonColours();
        Log.i("Button Colouring", "Ends");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.Aletter:
                Log.i("Keyboard", "A pressed");
                onLetterPress('A', 0);
                break;

            case R.id.Bletter:
                Log.i("Keyboard", "B pressed");
                onLetterPress('B', 1);
                break;

            case R.id.Cletter:
                Log.i("Keyboard", "C pressed");
                onLetterPress('C', 2);
                break;

            case R.id.Dletter:
                onLetterPress('D', 3);
                break;

            case R.id.Eletter:
                onLetterPress('E', 4);
                break;

            case R.id.Fletter:
                onLetterPress('F', 5);
                break;

            case R.id.Gletter:
                onLetterPress('G', 6);
                break;

            case R.id.Hletter:
                onLetterPress('H', 7);
                break;

            case R.id.Iletter:
                onLetterPress('I', 8);
                break;

            case R.id.Jletter:
                onLetterPress('J', 9);
                break;

            case R.id.Kletter:
                onLetterPress('K', 10);
                break;

            case R.id.Lletter:
                onLetterPress('L', 11);
                break;

            case R.id.Mletter:
                onLetterPress('M', 12);
                break;

            case R.id.Nletter:
                onLetterPress('N', 13);
                break;

            case R.id.Oletter:
                onLetterPress('O', 14);
                break;

            case R.id.Pletter:
                onLetterPress('P', 15);
                break;

            case R.id.Qletter:
                onLetterPress('Q', 16);
                break;

            case R.id.Rletter:
                onLetterPress('R', 17);
                break;

            case R.id.Sletter:
                onLetterPress('S', 18);
                break;

            case R.id.Tletter:
                onLetterPress('T', 19);
                break;

            case R.id.Uletter:
                onLetterPress('U', 20);
                break;

            case R.id.Vletter:
                onLetterPress('V', 21);
                break;

            case R.id.Wletter:
                onLetterPress('W', 22);
                break;

            case R.id.Xletter:
                onLetterPress('X', 23);
                break;

            case R.id.Yletter:
                onLetterPress('Y', 24);
                break;

            case R.id.Zletter:
                onLetterPress('Z', 25);
                break;

            case R.id.backSpace1:
                Log.i("Keyboard", "backspace pressed");
                backspaceAction();
                break;

            case R.id.backSpace2:
                Log.i("Keyboard", "backspace pressed");
                backspaceAction();
                break;

            default:
                break;
        }
    }

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

    private void onLetterPress(Character letter, Integer letterIndex){
        int letterCount = getLetterCount(letter);
        if (letterCount == -1){
            Log.i("Error", "Letter count invalid");
        }else if (letterCount == 0){
            Toast.makeText(getApplicationContext(), "No "+ letter + "'s remaining", Toast.LENGTH_SHORT).show();
        }else{
            Boolean fullWord = checkIfSeven();
            if (fullWord){
                Toast.makeText(getApplicationContext(), "Already 7 letters", Toast.LENGTH_LONG).show();
            } else {
                addLetter(letter.toString());
                reduceLetterCount(letterIndex);
                updateLetterButtonColour(letterIndex);
                updateLetterCountIndex(letterIndex);
            }
        }
    }

    private boolean checkIfSeven(){
        TextView textInput = (TextView)findViewById(R.id.textViewInput);
        if (textInput.getText().length() == 7){
            return true;
        }else {
            return false;
        }
    }

    private void addLetter(String letter){
        TextView textInput = (TextView)findViewById(R.id.textViewInput);
        textInput.setText(textInput.getText() + letter);
    }

    private void increaseLetterCount(Integer letterIndex){
        lettersParam[letterIndex].setCount(lettersParam[letterIndex].getCount() + 1);
    }

    private void reduceLetterCount(Integer letterIndex){
        lettersParam[letterIndex].setCount(lettersParam[letterIndex].getCount() - 1);
    }

    private void updateLetterButtonColour(Integer letterIndex){
        Integer count = lettersParam[letterIndex].getCount();

        if (count > 3){
            buttons[letterIndex].setBackgroundResource(R.drawable.keyboard_button_border);
        } else if ( count > 0){
            buttons[letterIndex].setBackgroundResource(R.drawable.keyboard_button_border_low);
        } else {
            buttons[letterIndex].setBackgroundResource(R.drawable.keyboard_button_border_disabled);
        }
    }

    private void updateLetterCountIndex(Integer letterIndex){
        Character letter = lettersParam[letterIndex].getLetter();
        Integer count = lettersParam[letterIndex].getCount();

        String toShow = letter + count.toString();
        SpannableStringBuilder showSpan = new SpannableStringBuilder(toShow);
        showSpan.setSpan(new SuperscriptSpan(), 1, showSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        showSpan.setSpan(new RelativeSizeSpan(0.7f), 1, showSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        buttons[letterIndex].setText(showSpan);
    }

    private void backspaceAction(){
        TextView textInput = (TextView)findViewById(R.id.textViewInput);
        String startText = textInput.getText().toString();
        if (startText.length() > 0){
            Character lastLetter = startText.charAt(startText.length()-1);
            Integer letterIndex = getLetterPosition(lastLetter);
            Log.i("got position "+ letterIndex.toString(), "for" + lastLetter);
            increaseLetterCount(letterIndex);
            updateLetterButtonColour(letterIndex);
            updateLetterCountIndex(letterIndex);
            textInput.setText(removeLastLetter(startText));
        } else {
            Toast.makeText(getApplicationContext(), "Nothing to delete", Toast.LENGTH_SHORT).show();
        }
    }

    private int getLetterPosition(Character letter){
        letter = Character.toUpperCase(letter);
        int temp = (int)letter;
        int temp_integer = 65; //for upper case
        if(temp<=90 & temp>=65){
            return temp-temp_integer;
        }else {
            return -1;
        }
    }

    private String removeLastLetter(String text){
        if (text.length() > 0){
            return text.substring(0, text.length() - 1);
        }else {
            return "";
        }
    }
}
