package com.code.justin.grabbledesign;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// some guidance https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
public class InventoryActivity extends AppCompatActivity {

    private int player;

    public class OneLine {
        public String word;
        public String value;

        public OneLine(String word, String value) {
            this.word = word;
            this.value = value;
        }
    }

    // Construct the data source
    ArrayList<OneLine> arrayOfoneLines = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("ACITVITY", "InventoryActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        Intent intent = getIntent();
        player = Integer.parseInt(intent.getStringExtra("userId"));

        // Create the adapter to convert the array to views
        LinesAdapter adapter = new LinesAdapter(this, arrayOfoneLines);

        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.inventoryListView);
        populateArray(adapter);
        listView.setAdapter(adapter);
    }

    public class LinesAdapter extends ArrayAdapter<OneLine> {
        public LinesAdapter(Context context, ArrayList<OneLine> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            OneLine line = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.inventory_one_line, parent, false);
            }
            // Lookup view for data population
            TextView lineWord = (TextView) convertView.findViewById(R.id.lineWord);
            TextView lineValue = (TextView) convertView.findViewById(R.id.lineValue);
            // Populate the data into the template view using the data object
            lineWord.setText(line.word);
            lineValue.setText(line.value);
            // Return the completed view to render on screen
            return convertView;
        }
    }

    class Word{

        private String word;
        private int value;

        public Word(String newWord, int newValue){
            this.word = newWord;
            this.value = newValue;
        }

        public String getWord() {
            return word;
        }
        public void setWord(String newWord) {
            this.word = newWord;
        }
        public int getValue() {
            return value;
        }
        public void setValue(int newValue) {
            this.value = newValue;
        }
    }

    class MyWordsComp implements Comparator<Word> {

        @Override
        public int compare(Word word1, Word word2) {
            if(word1.getValue() < word2.getValue()){
                return 1;
            } else {
                return -1;
            }
        }
    }

    private void populateArray(LinesAdapter adapter){
        adapter.clear();

        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS usedwords (id INTEGER, word VARCHAR(7), value INTEGER, PRIMARY KEY(id,word))");

        Cursor c = userData.rawQuery("SELECT * FROM usedwords WHERE id = " + Integer.toString(player), null);
        int wordIndex = c.getColumnIndex("word");
        int valueIndex = c.getColumnIndex("value");

        List<Word> allWords = new ArrayList<>();

        c.moveToFirst();
        if(c.moveToFirst()) {
            do {
                String word = c.getString(wordIndex);
                int value = Integer.parseInt(c.getString(valueIndex));
                allWords.add(new Word(word, value));
            } while (c.moveToNext());
        }

        Collections.sort(allWords, new MyWordsComp());

        for (Word word:allWords){
            OneLine line = new OneLine(word.getWord(), Integer.toString(word.getValue()));
            adapter.add(line);
        }
        userData.close();
        c.close();
    }

    public void toMapsActivity(View view){
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("userId", Integer.toString(player));
        startActivity(intent);
        finish();
    }

    public void toInventoryAllPlayers(View view){
        Intent intent = new Intent(getApplicationContext(), InventoryAllPlayers.class);
        intent.putExtra("userId", Integer.toString(player));
        startActivity(intent);
        finish();
    }

    public void toInventorySumScores(View view){
        Intent intent = new Intent(getApplicationContext(), InventorySumScores.class);
        intent.putExtra("userId", Integer.toString(player));
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
    }
}
