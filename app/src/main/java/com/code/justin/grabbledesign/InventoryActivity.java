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

    private void populateArray(LinesAdapter adapter){
        adapter.clear();

        SQLiteDatabase userData = this.openOrCreateDatabase("userDatabase", MODE_PRIVATE, null);
        userData.execSQL("CREATE TABLE IF NOT EXISTS usedwords (id INTEGER, word VARCHAR(7), value INTEGER, PRIMARY KEY(id,word))");

        Cursor c = userData.rawQuery("SELECT * FROM usedwords WHERE id = " + Integer.toString(player), null);
        int wordIndex = c.getColumnIndex("word");
        int valueIndex = c.getColumnIndex("value");

        c.moveToFirst();
        if(c.moveToFirst()) {
            do {
//                Log.i("id", c.getString(idIndex));
                String word = c.getString(wordIndex);
                String value = c.getString(valueIndex);

                Log.i("word", c.getString(wordIndex));
                Log.i("value", c.getString(valueIndex));

                OneLine line = new OneLine(word, value);
                adapter.add(line);

            } while (c.moveToNext());
        }
        c.close();
    }

    public void toMapsActivity(View view){
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("userId", Integer.toString(player));
        startActivity(intent);
    }
}
