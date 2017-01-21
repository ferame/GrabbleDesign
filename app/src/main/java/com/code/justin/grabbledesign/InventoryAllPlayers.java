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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class InventoryAllPlayers extends AppCompatActivity {

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
    ArrayList<InventoryAllPlayers.OneLine> arrayOfoneLines = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("ACITVITY", "InventoryAllPlayers");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_all_players);

        Intent intent = getIntent();
        player = Integer.parseInt(intent.getStringExtra("userId"));

        // Create the adapter to convert the array to views
        LinesAdapter adapter = new LinesAdapter(this, arrayOfoneLines);

        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.inventoryListView);
        populateArray(adapter);
        listView.setAdapter(adapter);
    }

    public class LinesAdapter extends ArrayAdapter<InventoryAllPlayers.OneLine> {
        public LinesAdapter(Context context, ArrayList<InventoryAllPlayers.OneLine> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            InventoryAllPlayers.OneLine line = getItem(position);
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

    class PlayerWord{
        private String nickname;
        private int value;

        public PlayerWord(String newName, int newValue){
            this.nickname = newName;
            this.value = newValue;
        }

        public String getNickname() {
            return nickname;
        }
        public int getValue(){
            return value;
        }
        public void setNickname(String newNickname) {
            this.nickname = newNickname;
        }
        public void setValue(int newValue){
            this.value = newValue;
        }
    }

    class MyAllPlayersWordsComp implements Comparator<PlayerWord> {

        @Override
        public int compare(PlayerWord word1, PlayerWord word2) {
            if(word1.getValue() < word2.getValue()){
                return 1;
            } else {
                return -1;
            }
        }
    }

    private void populateArray(InventoryAllPlayers.LinesAdapter adapter){
        adapter.clear();

        HashMap<String,String> users = getUserHashMap();
        List<PlayerWord> allPlayersWords = new ArrayList<>();

        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS usedwords (id INTEGER, word VARCHAR(7), value INTEGER, PRIMARY KEY(id,word))");

        Cursor c = userData.rawQuery("SELECT * FROM usedwords", null);
        int idIndex = c.getColumnIndex("id");
        int valueIndex = c.getColumnIndex("value");

        c.moveToFirst();
        if(c.moveToFirst()) {
            do {
//                Log.i("id", c.getString(idIndex));
                String id = c.getString(idIndex);
                String value = c.getString(valueIndex);

//                Log.i("id", c.getString(idIndex));
//                Log.i("value", c.getString(valueIndex));

                String user = users.get(id);
                allPlayersWords.add(new PlayerWord(user,Integer.parseInt(value)));

            } while (c.moveToNext());
        }

        Collections.sort(allPlayersWords, new MyAllPlayersWordsComp());

        for (PlayerWord word:allPlayersWords){
            InventoryAllPlayers.OneLine line = new InventoryAllPlayers.OneLine(word.getNickname(), Integer.toString(word.getValue()));
            adapter.add(line);
        }
        c.close();
        userData.close();
    }

    private HashMap<String,String> getUserHashMap(){
        HashMap<String, String> users = new HashMap<>();
        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS accounts (nickname VARCHAR(20), email TEXT, password TEXT, id INTEGER PRIMARY KEY)");

        Cursor c = userData.rawQuery("SELECT * FROM accounts", null);
        int idIndex = c.getColumnIndex("id");
        int nicknameIndex = c.getColumnIndex("nickname");

        c.moveToFirst();
        if(c.moveToFirst()) {
            do {
                String id = c.getString(idIndex);
                String nickname = c.getString(nicknameIndex);
                users.put(id, nickname);
            } while (c.moveToNext());
        }
        userData.close();
        c.close();
        return users;
    }

    public void toMapsActivity(View view){
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("userId", Integer.toString(player));
        startActivity(intent);
        finish();
    }

    public void toInventoryActivity(View view){
        Intent intent = new Intent(getApplicationContext(), InventoryActivity.class);
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
}
