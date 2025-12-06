package com.matibag.presentlast;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class ProfilePage extends Activity {
    TextView btnBack;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(view -> finish());

    }
}
