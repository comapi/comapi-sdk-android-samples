package com.dotdigital.deeplinksample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SecondaryActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary);
        Button btn = findViewById(R.id.button_close);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}