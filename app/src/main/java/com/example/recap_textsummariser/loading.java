package com.example.recap_textsummariser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class loading extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        getSupportActionBar().hide();
        Intent l=getIntent();
        String summ1=l.getStringExtra("summ1");
        String summ2=l.getStringExtra("summ2");

        Intent inext=new Intent(loading.this,Summary.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                inext.putExtra("summ1",summ1);
                inext.putExtra("summ2",summ2);

                startActivity(inext);
                finish();
            }
        },10000);
    }
}