package com.code.justin.grabbledesign;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ChangePassActivity extends AppCompatActivity {

    private int player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);
        Intent intent = getIntent();
        player = Integer.parseInt(intent.getStringExtra("userId"));
    }
}
