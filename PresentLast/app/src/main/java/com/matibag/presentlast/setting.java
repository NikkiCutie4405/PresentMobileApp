package com.matibag.presentlast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class setting extends Activity {

    Button PROFILE,LOGOUT;
    TextView back;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        PROFILE = findViewById(R.id.Profile);
        LOGOUT = findViewById(R.id.logout);
        back=findViewById(R.id.btnBack);
        back.setOnClickListener(view -> {
            finish();

        });
        PROFILE.setOnClickListener(view -> {
            Intent callMainT = new Intent(setting.this, ProfilePage.class);
            startActivity(callMainT);

        });
        LOGOUT.setOnClickListener(view -> {
            Intent callMainT = new Intent(setting.this, Login.class);
            startActivity(callMainT);
            finish();
        });
    }
}
