package com.mikel.poseidon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class ChooseManAuto extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_man_auto);

        //callback to home button
        ImageButton home_button = (ImageButton) findViewById(R.id.homebutton);
        home_button.setOnClickListener(view -> {

            Intent home_intent = new Intent(ChooseManAuto.this, MainActivity.class);
            startActivity(home_intent);
        });

        // Manually
        Button getManually = (Button) findViewById(R.id.manually);

        getManually.setOnClickListener(view -> {
            Intent stepsRecordIntent = new Intent(ChooseManAuto.this, GetWeight.class);
            startActivity(stepsRecordIntent);
        });

        // Automatically
        Button getAutomatically= (Button) findViewById(R.id.automatically);

        getAutomatically.setOnClickListener(view -> {
            try{
            Intent stepsRecordIntent = new Intent(ChooseManAuto.this, GetWeightFitbit.class);
            startActivity(stepsRecordIntent);
            }
            catch (Exception e){
                Log.e("Fitbit: ", "Not connected to Fitbit");
                Toast.makeText(this, "No connected to Bitbit", Toast.LENGTH_SHORT).show();
            }

        });





    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
