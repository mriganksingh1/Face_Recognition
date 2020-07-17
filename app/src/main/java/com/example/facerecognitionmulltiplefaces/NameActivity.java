package com.example.facerecognitionmulltiplefaces;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NameActivity extends AppCompatActivity {
    EditText name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);
        name = findViewById(R.id.name);
        findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(name.getText().toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(),"Please enter a name",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent intent = new Intent(NameActivity.this,Training.class);
                    intent.putExtra("name",name.getText().toString());
                    intent.putExtra("reset",0);
                    intent.putExtra("recognize",0);
                    startActivity(intent);
                }
            }
        });
        findViewById(R.id.resetButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NameActivity.this,Training.class);
                intent.putExtra("name","");
                intent.putExtra("reset",1);
                intent.putExtra("recognize",0);
                startActivity(intent);
            }
        });
    }
}
