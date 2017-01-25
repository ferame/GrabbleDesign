package com.code.justin.grabbledesign;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class InventorySumScores extends AppCompatActivity {

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
    ArrayList<InventorySumScores.OneLine> arrayOfoneLines = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("ACITVITY", "InventorySumScores");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_sum_scores);

        Intent intent = getIntent();
        player = Integer.parseInt(intent.getStringExtra("userId"));

        // Create the adapter to convert the array to views
        InventorySumScores.LinesAdapter adapter = new InventorySumScores.LinesAdapter(this, arrayOfoneLines);

        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.inventoryListView);
        populateArray(adapter);
        listView.setAdapter(adapter);
    }

    public class LinesAdapter extends ArrayAdapter<InventorySumScores.OneLine> {
        public LinesAdapter(Context context, ArrayList<InventorySumScores.OneLine> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            InventorySumScores.OneLine line = getItem(position);
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

    class Player{
        private String name;
        private int id;
        private int score;

        public Player(String newName, int newId, int newScore){
            this.name = newName;
            this.id = newId;
            this.score = newScore;
        }

        public String getName() {
            return name;
        }
        public int getId(){
            return id;
        }
        public int getScore(){
            return score;
        }
        public void setName(String newName) {
            this.name = newName;
        }
        public void setId(int newId){
            this.id = newId;
        }
        public void setScore(int newScore){
            this.score = newScore;
        }

        public void addToScore(int additionalScore){
            this.score = score + additionalScore;
        }
    }

    class MyAllPlayersComp implements Comparator<Player> {

        @Override
        public int compare(Player player1, Player player2) {
            if(player1.getScore() < player2.getScore()){
                return 1;
            } else {
                return -1;
            }
        }
    }

    private void addScore(List<Player> allPlayers, int id, int value){
        for (Player player:allPlayers){
            if (id == player.getId()){
                player.addToScore(value);
                return;
            }
        }
    }

    private void populateArray(InventorySumScores.LinesAdapter adapter){
        adapter.clear();

        HashMap<String,String> users = getUserHashMap();

        List<Player> allPlayers = new ArrayList<>();

        for(Map.Entry<String, String> usersEntry : users.entrySet()){
            allPlayers.add(new Player(usersEntry.getValue(), Integer.parseInt(usersEntry.getKey()), 0));
        }

        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS usedwords (id INTEGER, word VARCHAR(7), value INTEGER, PRIMARY KEY(id,word))");

        Cursor c = userData.rawQuery("SELECT * FROM usedwords", null);
        int idIndex = c.getColumnIndex("id");
        int valueIndex = c.getColumnIndex("value");

        c.moveToFirst();
        if(c.moveToFirst()) {
            do {
                int id = Integer.parseInt(c.getString(idIndex));
                int value = Integer.parseInt(c.getString(valueIndex));

                addScore(allPlayers, id, value);
            } while (c.moveToNext());
        }

        Collections.sort(allPlayers, new MyAllPlayersComp());

        for (Player player:allPlayers){
            InventorySumScores.OneLine line = new InventorySumScores.OneLine(player.getName(), Integer.toString(player.getScore()));
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

    public void toInventoryAllPlayers(View view){
        Intent intent = new Intent(getApplicationContext(), InventoryAllPlayers.class);
        intent.putExtra("userId", Integer.toString(player));
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
    }
}

