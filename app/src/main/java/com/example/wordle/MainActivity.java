package com.example.wordle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GridLayout grid;

    int currentRow;

    String solution;

    Button submitButton;
    Button restartButton;
    Button clearButton;

    Button newActivity;

    Drawable defaultEditTextBackground;

    FirebaseDatabase database;
    DatabaseReference myRef;

    ArrayList<String> solutions;




    HashMap<Character, Integer> letters;

    View.OnClickListener submitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(checkSubmission()){
                Toast.makeText(getApplicationContext(), "You won!", Toast.LENGTH_LONG).show();
            }
            else{
                currentRow++;
                revealNextRow();
            }
        }
    };

    View.OnClickListener restartListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for(int i = 0; i < grid.getChildCount(); i++){
                View text = grid.getChildAt(i);

                if (text instanceof EditText) {
                    EditText editText = (EditText) text;
                    editText.setBackground(defaultEditTextBackground);
                    editText.setText("");
                }
            }
            letters.clear();
            //todo create new solution from db
            currentRow = 1;
            revealNextRow();
        }
    };

    View.OnClickListener clearListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for(int i = 0; i < grid.getChildCount(); i++){
                View text = grid.getChildAt(i);

                if (text instanceof EditText) {
                    EditText editText = (EditText) text;
                    editText.setBackground(defaultEditTextBackground);
                    editText.setText("");
                }
            }
            currentRow = 1;
            revealNextRow();
        }
    };

    View.OnClickListener newActivityListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //opens DB activity
            Intent intent = new Intent(MainActivity.this, AddWord.class);
            startActivity(intent);
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        solutions = new ArrayList<>();
        solution = "TESTS";

        myRef = FirebaseDatabase.getInstance().getReference();

        myRef.child("words").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    DataSnapshot snapshot = task.getResult();

                    for(DataSnapshot temp : snapshot.getChildren()){
                        Log.d("firebase", "imma kill myself cause: " + temp.getValue(String.class));
                        solutions.add(temp.getValue(String.class));
                    }

                    Collections.shuffle(solutions);

                    solution = solutions.get(0);
                }
            }
        });

        letters = new HashMap<>();

        //make hashmap to track what letters are in our solution
        for(int i = 0; i < solution.length(); i++){
            char letter = solution.substring(i, i+1).toCharArray()[0];
            if(letters.containsKey(letter)){
                letters.put(letter, letters.get(letter) + 1);
            }
            else{
                letters.put(letter, 1);
            }
        }

        currentRow = 1;

        setContentView(R.layout.activity_main);
        grid = findViewById(R.id.wordleGrid);
        revealNextRow();

        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(submitListener);

        restartButton = findViewById(R.id.restartButton);
        restartButton.setOnClickListener(restartListener);

        clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(clearListener);

        newActivity = findViewById(R.id.newActivity);
        newActivity.setOnClickListener(newActivityListener);

        defaultEditTextBackground = grid.getChildAt(0).getBackground();

    }


    public void revealNextRow(){
        for(int i = 0; i < grid.getChildCount(); i++){
            View text = grid.getChildAt(i);

            //i + 1 to account for index offset
            //currentRow * 5 is the 5 cells of the next allowed row
            if(i + 1 > currentRow * 5){
                text.setVisibility(View.INVISIBLE);

            }
            else{
                text.setVisibility(View.VISIBLE);
            }
        }
    }

    public boolean checkSubmission(){
        int correctGuesses = 0;
        HashMap<Character, Integer> correctLetters = new HashMap<>(letters);
        HashMap<Character, Integer> guessLetters = new HashMap<>();
        for(int i = 0; i < grid.getChildCount(); i++){
            View text = grid.getChildAt(i);

            //i + 1 to account for index offset
            //currentRow * 5 is the 5 cells of the next allowed row
            if(i + 1 <= currentRow * 5){
                // Check if the current view is an EditText
                if (text instanceof EditText) {
                    EditText editText = (EditText) text;

                    String stringLetter = editText.getText().toString();
                    char letter = stringLetter.toCharArray()[0];

                    //decrement count of letter in hashmap
                    if(guessLetters.containsKey(letter)){
                        guessLetters.put(letter, guessLetters.get(letter) + 1);
                    }
                    else{
                        guessLetters.put(letter, 1);
                    }

                    //check if correct letter + placement
                    if(stringLetter.equals(solution.substring(i % 5, i % 5 + 1))){
                        text.setBackgroundColor(getResources().getColor(R.color.green));
                        //if we are on the latest entry row
                        if(i + 1 > (currentRow - 1) * 5){
                            correctGuesses++;
                        }
                    }
                    else if(!correctLetters.containsKey(letter) || (guessLetters.get(letter) != null && correctLetters.get(letter) != null && guessLetters.get(letter) > correctLetters.get(letter))){
                        text.setBackgroundColor(getResources().getColor(R.color.grey));
                        Log.i("GridColors", "Assigning letter: " + letter + " grey because guessLetters: " + guessLetters.get(letter) + " correctLetters: " + correctLetters.get(letter));
                    }
                    else{
                        Log.i("GridColors", "Assigning letter: " + letter + " yellow because guessLetters: " + guessLetters.get(letter) + " correctLetters: " + correctLetters.get(letter));
                        text.setBackgroundColor(getResources().getColor(R.color.yellow));
                    }
                }
            }
        }

        return correctGuesses == 5;

    }


}