package com.example.wordle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddWord extends AppCompatActivity {

    Button addNewWord;

    Button goBack;
    EditText inputText;

    DatabaseReference databaseReference;

    View.OnClickListener addWordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String value = inputText.getText().toString();

            if(value.length() == 5 && value.matches("[a-zA-Z]+")){
                DatabaseReference reference = databaseReference.child("words").push();
                value = value.toUpperCase();
                reference.setValue(value);
            }
            else{
                Toast.makeText(getApplicationContext(), "Invalid Word", Toast.LENGTH_LONG).show();
            }

        }
    };

    View.OnClickListener goBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //opens DB activity
            Intent intent = new Intent(AddWord.this, MainActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        inputText = findViewById(R.id.wordInput);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        addNewWord = findViewById(R.id.addNewWord);

        addNewWord.setOnClickListener(addWordListener);

        goBack = findViewById(R.id.goBack);
        goBack.setOnClickListener(goBackListener);

    }
}