package com.example.kks.carpool.LoginSignup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.kks.carpool.R;

public class Empty extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);

        Intent intent = new Intent(Empty.this, Login.class);
        startActivity(intent);
        finish();
    }
}
