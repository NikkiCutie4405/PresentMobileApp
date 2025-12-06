package com.matibag.presentlast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

public class compose extends Activity {
    Button Cancel;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compose);
        Cancel = findViewById(R.id.btnCancel);

        Cancel.setOnClickListener(view -> finish());
    }
}