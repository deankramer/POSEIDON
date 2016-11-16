package com.mikel.poseidon;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.R.attr.format;
import static com.mikel.poseidon.R.id.ok_button;

public class GetWeight extends AppCompatActivity {


    Button okbtn, date_button;
    EditText inputWeight;
    DBHelper myDB;
    static final int DIALOG_ID = 0;
    int year_x, month_x, day_x;
    String date, year, month, day, newDateStr;

    Date date_f;
    String date_final;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_weight);


        //create db
        myDB = new DBHelper(this);

        //create view objects
        okbtn = (Button) findViewById(ok_button);
        inputWeight = (EditText) findViewById(R.id.editTextWeight_string);

        //set initial date of calendar picker
        final Calendar cal = Calendar.getInstance();
        year_x = cal.get(Calendar.YEAR);
        month_x = cal.get(Calendar.MONTH);
        day_x = cal.get(Calendar.DAY_OF_MONTH);

        //implement methods: add weight data, pick date
        AddData();
        showDialogOnSelectDateClick();


    }
    public void AddData() {

        okbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isInserted = myDB.insertData(inputWeight.getText().toString());

                /*Intent intent = new Intent(GetWeight.this, ViewSummary.class);
                intent.putExtra("Weight", weight);
                startActivity(intent);*/
                if(isInserted == true)
                    Toast.makeText(GetWeight.this,"Data Inserted",Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(GetWeight.this,"Data not Inserted",Toast.LENGTH_LONG).show();




            }

        });
    }


    public void showDialogOnSelectDateClick(){

        date_button = (Button) findViewById(R.id.datebtn);
        date_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(DIALOG_ID);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id){
        if (id == DIALOG_ID)
        return new DatePickerDialog(this, dpickerListener, year_x, month_x, day_x);
        return null;
    }

    private DatePickerDialog.OnDateSetListener dpickerListener
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            year_x=i;
            month_x=i1+1;
            day_x=i2;

            year = String.valueOf(year_x);
            month = String.valueOf(month_x);
            day = String.valueOf(day_x);

            date = year + "/" + month + "/"+ day;

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
            try {
                date_f = formatter.parse(date);
                date_final = formatter.format(date_f);

                System.out.println(date_f);
                System.out.println(date_final);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }





            //Toast.makeText(GetWeight.this, (CharSequence) date_f, Toast.LENGTH_SHORT).show();
        }
    };



}