package com.code.justin.grabbledesign;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordInputActivity extends AppCompatActivity implements View.OnClickListener {

    private int Player;

    private Letter[] lettersParam = new Letter[26];

    Button[] buttons = new Button[29];

    class Letter {
        int count;

        private void setCount(int newCount) {
            count = newCount;
        }

        private int getCount() {
            return count;
        }

        Character letter;

        private void setLetter(Character newLetter) {
            letter = newLetter;
        }

        private Character getLetter() {
            return letter;
        }
    }

    String dictionary;

    Map<Character, Object> letterValues = new HashMap<Character, Object>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_input);
        Intent intent = getIntent();
        Player = Integer.parseInt(intent.getStringExtra("userId"));

        setLetterValues();
        Log.i("Populate", "Starts");
        populateLettersParam();
        Log.i("Populate", "Ends");

//        Button[] buttons = new Button[28];
        Integer i = 0;
        for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
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

        //Setting up submit button
        buttons[28] = ((Button) findViewById(R.id.submitWord));
        buttons[28].setOnClickListener(this);

        Log.i("Button Count info", "Starts");
        setLetterButtonCountsWITHLETTERSCORES();
//        WITHLETTERSCORES
//        setLetterButtonCounts();
        Log.i("Button Count info", "Ends");

        Log.i("Button Colouring", "Starts");
        setLetterButtonColours();
        Log.i("Button Colouring", "Ends");

        dictionary = readRawTextFile();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.Aletter:
                Log.i("Keyboard", "A pressed");
                onLetterPress('A');
                break;

            case R.id.Bletter:
                Log.i("Keyboard", "B pressed");
                onLetterPress('B');
                break;

            case R.id.Cletter:
                Log.i("Keyboard", "C pressed");
                onLetterPress('C');
                break;

            case R.id.Dletter:
                onLetterPress('D');
                break;

            case R.id.Eletter:
                onLetterPress('E');
                break;

            case R.id.Fletter:
                onLetterPress('F');
                break;

            case R.id.Gletter:
                onLetterPress('G');
                break;

            case R.id.Hletter:
                onLetterPress('H');
                break;

            case R.id.Iletter:
                onLetterPress('I');
                break;

            case R.id.Jletter:
                onLetterPress('J');
                break;

            case R.id.Kletter:
                onLetterPress('K');
                break;

            case R.id.Lletter:
                onLetterPress('L');
                break;

            case R.id.Mletter:
                onLetterPress('M');
                break;

            case R.id.Nletter:
                onLetterPress('N');
                break;

            case R.id.Oletter:
                onLetterPress('O');
                break;

            case R.id.Pletter:
                onLetterPress('P');
                break;

            case R.id.Qletter:
                onLetterPress('Q');
                break;

            case R.id.Rletter:
                onLetterPress('R');
                break;

            case R.id.Sletter:
                onLetterPress('S');
                break;

            case R.id.Tletter:
                onLetterPress('T');
                break;

            case R.id.Uletter:
                onLetterPress('U');
                break;

            case R.id.Vletter:
                onLetterPress('V');
                break;

            case R.id.Wletter:
                onLetterPress('W');
                break;

            case R.id.Xletter:
                onLetterPress('X');
                break;

            case R.id.Yletter:
                onLetterPress('Y');
                break;

            case R.id.Zletter:
                onLetterPress('Z');
                break;

            case R.id.backSpace1:
                Log.i("Keyboard", "backspace pressed");
                backspaceAction();
                break;

            case R.id.backSpace2:
                Log.i("Keyboard", "backspace pressed");
                backspaceAction();
                break;

            case R.id.submitWord:
                Log.i("Keyboard", "submit pressed");
                submitAction();
                break;

            default:
                break;
        }
    }

    private int getLetterCount(char letter) {
        for (int i = 0; i < 26; i++) {
            if (lettersParam[i].getLetter() == letter) {
                return lettersParam[i].getCount();
            }
        }
        return -1;
    }

    private int getLetterIndex(char letter) {
        for (int i = 0; i < 26; i++) {
            if (lettersParam[i].getLetter() == letter) {
                return i;
            }
        }
        return -1;
    }

    private void populateLettersParam() {
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        String tableName = "inventory";
        String selectStringPlacemark = "SELECT * FROM " + tableName + " WHERE " + "id" + " =?";

        Cursor c = userData.rawQuery(selectStringPlacemark, new String[]{Integer.toString(Player)});
        int letterIndex = c.getColumnIndex("letter");
        int amountIndex = c.getColumnIndex("amount");
        c.moveToFirst();
        Integer count = 0;
        while (c != null) {
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

//    private void setLetterButtonCounts() {
//        for (int i = 0; i < 26; i++) {
//            Character letter = lettersParam[i].getLetter();
//            Integer count = lettersParam[i].getCount();
//
//            String toShow = letter + count.toString();
//            SpannableStringBuilder showSpan = new SpannableStringBuilder(toShow);
//            showSpan.setSpan(new SuperscriptSpan(), 1, showSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            showSpan.setSpan(new RelativeSizeSpan(0.7f), 1, showSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//            buttons[i].setText(showSpan);
//        }
//    }

    private void setLetterButtonCountsWITHLETTERSCORES() {
        for (int i = 0; i < 26; i++) {
            Character letter = lettersParam[i].getLetter();
            Integer count = lettersParam[i].getCount();

            String toShow = letter + letterValues.get(letter).toString();
            SpannableStringBuilder showSpan = new SpannableStringBuilder(toShow);
            showSpan.setSpan(new SuperscriptSpan(), 1, showSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            showSpan.setSpan(new RelativeSizeSpan(0.7f), 1, showSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            buttons[i].setText(showSpan);
        }
    }

    private void setLetterButtonColours() {
        for (int i = 0; i < 26; i++) {
            Integer count = lettersParam[i].getCount();

            if (count > 3) {
                buttons[i].setBackgroundResource(R.drawable.keyboard_button_border);
            } else if (count > 0) {
                buttons[i].setBackgroundResource(R.drawable.keyboard_button_border_low);
            } else {
                buttons[i].setBackgroundResource(R.drawable.keyboard_button_border_disabled);
            }
        }
    }

    private void onLetterPress(Character letter) {
        int letterCount = getLetterCount(letter);
        int letterIndex = getLetterPosition(letter);
        if (letterCount == -1) {
            Log.i("Error", "Letter count invalid");
        } else if (letterCount == 0) {
            Toast.makeText(getApplicationContext(), "No " + letter + "'s remaining", Toast.LENGTH_SHORT).show();
        } else {
            Boolean fullWord = checkIfSeven();
            if (fullWord) {
                Toast.makeText(getApplicationContext(), "Already 7 letters", Toast.LENGTH_LONG).show();
            } else {
                addLetter(letter.toString());
                reduceLetterCount(letterIndex);
                updateLetterButtonColour(letterIndex);
//                WITHLETTERSCORES
//                updateLetterCountIndex(letterIndex);
            }
        }
    }

    private boolean checkIfSeven() {
        TextView textInput = (TextView) findViewById(R.id.textViewInput);
        if (textInput.getText().length() == 7) {
            return true;
        } else {
            return false;
        }
    }

    private void addLetter(String letter) {
        TextView textInput = (TextView) findViewById(R.id.textViewInput);
        String newText = textInput.getText() + letter;
        textInput.setText(newText);
    }

    private void increaseLetterCount(Integer letterIndex) {
        lettersParam[letterIndex].setCount(lettersParam[letterIndex].getCount() + 1);
    }

    private void reduceLetterCount(Integer letterIndex) {
        lettersParam[letterIndex].setCount(lettersParam[letterIndex].getCount() - 1);
    }

    private void updateLetterButtonColour(Integer letterIndex) {
        Integer count = lettersParam[letterIndex].getCount();

        if (count > 3) {
            buttons[letterIndex].setBackgroundResource(R.drawable.keyboard_button_border);
        } else if (count > 0) {
            buttons[letterIndex].setBackgroundResource(R.drawable.keyboard_button_border_low);
        } else {
            buttons[letterIndex].setBackgroundResource(R.drawable.keyboard_button_border_disabled);
        }
    }
//  WITHLETTERSCORES
    private void updateLetterCountIndex(Integer letterIndex) {
        Character letter = lettersParam[letterIndex].getLetter();
        Integer count = lettersParam[letterIndex].getCount();

        String toShow = letter + count.toString();
        SpannableStringBuilder showSpan = new SpannableStringBuilder(toShow);
        showSpan.setSpan(new SuperscriptSpan(), 1, showSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        showSpan.setSpan(new RelativeSizeSpan(0.7f), 1, showSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        buttons[letterIndex].setText(showSpan);
    }

    private void backspaceAction() {
        TextView textInput = (TextView) findViewById(R.id.textViewInput);
        String startText = textInput.getText().toString();
        if (startText.length() > 0) {
            Character lastLetter = startText.charAt(startText.length() - 1);
            Integer letterIndex = getLetterPosition(lastLetter);
            Log.i("got position " + letterIndex.toString(), "for" + lastLetter);
            increaseLetterCount(letterIndex);
            updateLetterButtonColour(letterIndex);
//            WITHLETTERSCORES
//            updateLetterCountIndex(letterIndex);
            textInput.setText(removeLastLetter(startText));
        } else {
            Toast.makeText(getApplicationContext(), "Nothing to delete", Toast.LENGTH_SHORT).show();
        }
    }

    private int getLetterPosition(Character letter) {
        letter = Character.toUpperCase(letter);
        int temp = (int) letter;
        int temp_integer = 65; //for upper case
        if (temp <= 90 & temp >= 65) {
            return temp - temp_integer;
        } else {
            return -1;
        }
    }

    private String removeLastLetter(String text) {
        if (text.length() > 0) {
            return text.substring(0, text.length() - 1);
        } else {
            return "";
        }
    }

    private void submitAction() {
        TextView textInput = (TextView) findViewById(R.id.textViewInput);
        String word = textInput.getText().toString();
        if (word.length() == 7) {
            if (dictionary.contains(word)) {
                if (isWordAlreadyUsed(word)) {
                    Toast.makeText(getApplicationContext(), "Word already used", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Nice", Toast.LENGTH_LONG).show();
                    addWordToUsedWords(word);
                    updateLetterCountInDB(word);
                    textInput.setText("");
                }
            } else {
                Toast.makeText(getApplicationContext(), "That's not a word", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "It must be 7 letter word", Toast.LENGTH_SHORT).show();
        }
    }

    private String readRawTextFile()
    {
        Log.i("readRawTextFile", "started");
        Resources resources = getResources();

        InputStream inputStream = resources.openRawResource(R.raw.grabbledictionary);
        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);

        StringBuilder wordList = new StringBuilder();
        String line;
        try {
            while (( line = buffreader.readLine()) != null) {
                wordList.append(line.toUpperCase());
                wordList.append(",");
            }
        } catch (IOException e) {
            return null;
        }
        Log.i("readRawTextFile", "ended");
        return wordList.toString();
    }

    private boolean isWordAlreadyUsed(String word){
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS usedwords (id INTEGER, word VARCHAR(7), value INTEGER, PRIMARY KEY(id,word))");

        String selectString = "SELECT * FROM usedwords WHERE " + "id" + " =?"+ " AND " + "word" + "=?";
        Cursor cursor = userData.rawQuery(selectString, new String[] {Integer.toString(Player), word});

        Boolean wordExists;

        if(cursor.moveToFirst()){
            wordExists = true;
        } else {
            wordExists = false;
        }

        cursor.close();
        userData.close();
        return wordExists;
    }

    private void addWordToUsedWords(String word){
        Log.i("addWordToUsedWords", "start");
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        Log.i("addWordToUsedWords", "after first line");
        userData.execSQL("CREATE TABLE IF NOT EXISTS usedwords (id INTEGER, word VARCHAR(7), value INTEGER, PRIMARY KEY(id,word))");
        Log.i("addWordToUsedWords", "after second line");
        userData.execSQL("INSERT INTO usedwords (id, word, value) VALUES (" + Integer.toString(Player) + ", '" + word +"', " + getWordValue(word)+ ")");
        Log.i("addWordToUsedWords", "middle");
        Log.i("Player words", "start");
        Cursor c = userData.rawQuery("SELECT * FROM usedwords WHERE id = " + Integer.toString(Player), null);
        int idIndex = c.getColumnIndex("id");
        int wordIndex = c.getColumnIndex("word");
        int valueIndex = c.getColumnIndex("value");
        c.moveToFirst();
        if(c.moveToFirst()) {
            do {
                Log.i("id", c.getString(idIndex));
                Log.i("word", c.getString(wordIndex));
                Log.i("value", c.getString(valueIndex));
            } while (c.moveToNext());
        }
        c.close();
        Log.i("Player words", "end");
        Log.i("addWordToUsedWords", "end");
        userData.close();
    }

    private Integer getWordValue(String word){
        Integer sum = 0;
        for (char letter : word.toCharArray()){
            sum += Integer.parseInt(letterValues.get(letter).toString());
        }
        return sum;
    }

    private void updateLetterCountInDB(String word){
        Log.i("updateLetterCountInDB", "starts");
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        String tableName = "inventory";
        String selectStringLetter = "SELECT * FROM inventory WHERE " + "id" + " =?"+ " AND " + "letter" + "=?";
        for (char letter : word.toCharArray()){
            Cursor cursor = userData.rawQuery(selectStringLetter, new String[] {Integer.toString(Player), Character.toString(letter)});
            if(cursor.moveToFirst()){
                String cursorId = cursor.getString(cursor.getColumnIndex("id"));

                String cursorLetter = cursor.getString(cursor.getColumnIndex("letter"));

                Integer newAmount = lettersParam[getLetterPosition(letter)].getCount();

                ContentValues newInventoryObject = new ContentValues();
                newInventoryObject.put("id", cursorId);
                newInventoryObject.put("letter", cursorLetter);
                newInventoryObject.put("amount", newAmount);
                Log.i("newObject", newInventoryObject.toString());

                userData.update(tableName, newInventoryObject, "letter ='" + cursorLetter + "'" + " and id = '" + cursorId + "'", null);
            } else {
                Log.i("Error!!!", "updateLetterCountInDB failed");
            }
            cursor.close();
        }
        userData.close();
        Log.i("updateLetterCountInDB", "ends");
    }

    private void setLetterValues(){
        letterValues.put('A', 3);
        letterValues.put('B', 20);
        letterValues.put('C', 13);
        letterValues.put('D', 10);
        letterValues.put('E', 1);
        letterValues.put('F', 15);
        letterValues.put('G', 18);
        letterValues.put('H', 9);
        letterValues.put('I', 5);
        letterValues.put('J', 25);
        letterValues.put('K', 22);
        letterValues.put('L', 11);
        letterValues.put('M', 14);
        letterValues.put('N', 6);
        letterValues.put('O', 4);
        letterValues.put('P', 19);
        letterValues.put('Q', 24);
        letterValues.put('R', 8);
        letterValues.put('S', 7);
        letterValues.put('T', 2);
        letterValues.put('U', 12);
        letterValues.put('V', 21);
        letterValues.put('W', 17);
        letterValues.put('X', 23);
        letterValues.put('Y', 16);
        letterValues.put('Z', 26);
    }
}
