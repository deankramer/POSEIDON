package com.mikel.poseidon;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikel.poseidon.utility.ExplicitIntentGenerator;

import org.poseidon_project.context.IContextReasoner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.R.attr.y;
import static android.widget.Toast.makeText;
import static com.mikel.poseidon.R.id.ok_button;
import static com.mikel.poseidon.R.id.yourdate;

public class GetWeight extends AppCompatActivity {


    Button okbtn, date_button;
    EditText inputWeight;
    TextView yourDate;
    DBHelper myDB;

    //DatePicker variables
    static final int DIALOG_ID = 0;
    int year_x, month_x, day_x;
    String date, year, month, day, date_final, newDate;
    double newWeight;

    Date date_f;

    private PopupWindow popupWindow;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;
    private IContextReasoner mContextService;
    private Context mContext;
    private boolean mBound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_weight);

        mContext = getApplicationContext();

        //callback to home button
        ImageButton home_button = (ImageButton) findViewById(R.id.homebutton);
        home_button.setOnClickListener(view -> {

            Intent home_intent = new Intent(GetWeight.this, MainActivity.class);
            startActivity(home_intent);
        });


        //create db
        myDB = new DBHelper(this);

        //create view objects
        yourDate = (TextView)findViewById(yourdate);
        okbtn = (Button) findViewById(ok_button);
        inputWeight = (EditText) findViewById(R.id.editTextWeight);

        //set initial date of calendar picker
        final Calendar cal = Calendar.getInstance();
        year_x = cal.get(Calendar.YEAR);
        month_x = cal.get(Calendar.MONTH);
        day_x = cal.get(Calendar.DAY_OF_MONTH);

        showDialogOnSelectDateClick();

        //When I press OK button get newWeight and newDate + show popup window
        okbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //Check if user has entered weight
                if(inputWeight.getText().toString().matches("")){

                    showWeightDateErrorMessage();

                }else{

                    newWeight = Double.parseDouble(inputWeight.getText().toString());
                    newDate = date_final;

                    //If date is not entered, show a message and DO NOT add data to DB
                    if(newDate != null && newWeight > 0) {

                        // Find last weight entered to compare to the new one
                        double theLastWeight = getLastWeight();
                        double weightDiference = newWeight - theLastWeight;

                        //Insert new data
                        AddData(newWeight, newDate);


                        //Pop up feedback window
                        weightFeedback(weightDiference);

                    }else {

                             /*Toast dateToast = Toast.makeText(GetWeight.this, "Enter date", Toast.LENGTH_SHORT);
                             dateToast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0 , 100);
                             dateToast.show();*/

                        //Show date error pop up window
                        showDateErrorMessage();
                    }

                }

            }

        });


        Intent serviceIntent = new Intent(IContextReasoner.class.getName());

        serviceIntent = ExplicitIntentGenerator
                .createExplicitFromImplicitIntent(mContext, serviceIntent);

        if (serviceIntent != null) {
            bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        } else {
            Log.e("POSEIDON", "Context Reasoner not installed!");
        }

    }

    private void sendReasonerData(double newWeight) {
        Intent intent = new Intent();
        try {
            intent.setAction("org.poseidon_project.context.EXTERNAL_CONTEXT_UPDATE");
            intent.putExtra("context_name", "Weight");
            intent.putExtra("context_value_type","double");
            intent.putExtra("context_value",newWeight);
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e("StopService", e.getMessage());
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mContextService  = IContextReasoner.Stub.asInterface(iBinder);
            mBound = ! mBound;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mContextService = null;
            mBound = ! mBound;
        }
    };


    public void AddData(double newWeight, String newDate) {

        boolean insertData = myDB.addData(newWeight,newDate);
        sendReasonerData(newWeight);

        if(insertData==true){
            makeText(this, "Data Successfully Inserted!", Toast.LENGTH_SHORT).show();
        }else{
            makeText(this, "Something went wrong :(.", Toast.LENGTH_LONG).show();
        }
    }


    //===============================================
    //                   Give feedback
    //===============================================
    public void weightFeedback(double substraction){

        if (substraction > -1 && substraction <1){

            //more or less the same pop up window
            layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.popup, null);
            relativeLayout = (RelativeLayout) findViewById(R.id.activity_get_weight);

            popupWindow = new PopupWindow(container, dpToPx(250), dpToPx(250), true); //true allows us to close window by tapping outside
            popupWindow.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, dpToPx(60), dpToPx(120));
            //shut popup outside window
            container.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popupWindow.dismiss();
                    return false;
                }
            });

            //System.out.println("more or less the same pop up window");

        }else if (substraction >= 1 && substraction < 2 ){

            //increased between 1 and 2Kg
            layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.popup_1to2, null);
            relativeLayout = (RelativeLayout) findViewById(R.id.activity_get_weight);

            popupWindow = new PopupWindow(container, dpToPx(250), dpToPx(250), true); //true allows us to close window by tapping outside
            popupWindow.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, dpToPx(60), dpToPx(120));

            //shut popup outside window
            container.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popupWindow.dismiss();
                    return false;
                }
            });
            //System.out.println("increased between 1 and 2Kg");

        }else if (substraction >= 2){

            //increased in more than 2Kg
            layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.popup_2plus, null);
            relativeLayout = (RelativeLayout) findViewById(R.id.activity_get_weight);

            popupWindow = new PopupWindow(container, dpToPx(250), dpToPx(250), true); //true allows us to close window by tapping outside
            popupWindow.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, dpToPx(60), dpToPx(120));
            //popupWindow = new PopupWindow(container, 500, 500, true); //true allows us to close window by tapping outside
            //popupWindow.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, 125, 300);

            //shut popup outside window
            container.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popupWindow.dismiss();
                    return false;
                }
            });
            //System.out.println("increased in more than 2Kg");

        }else if (substraction < -1){

            //reduced in more than 1Kg -- Good
            layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.popup_good, null);
            relativeLayout = (RelativeLayout) findViewById(R.id.activity_get_weight);

            popupWindow = new PopupWindow(container, dpToPx(250), dpToPx(250), true); //true allows us to close window by tapping outside
            popupWindow.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, dpToPx(60), dpToPx(120));

            //shut popup outside window
            container.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popupWindow.dismiss();
                    return false;
                }
            });
            //System.out.println("reduced in more than 1Kg");

        }

    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    //===============================================
    //                   Date Picker
    //===============================================

    public void showDialogOnSelectDateClick(){

        yourDate.setOnClickListener(new View.OnClickListener() {
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

            date = year + "-" + month + "-"+ day;

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            try {
                date_f = formatter.parse(date);
                date_final = formatter.format(date_f);


                System.out.println(date_f);
                System.out.println(date_final);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            yourDate.setText(date_final);

            /*Intent pass_date = new Intent(GetWeight.this, DBHelper.class);
            pass_date.putExtra("Date", date_final);
            startActivity(pass_date);*/

            //Toast.makeText(GetWeight.this, (CharSequence) date_f, Toast.LENGTH_SHORT).show();
        }
    };



    //===============================================
    //                  Get last weight
    //===============================================

    private double getLastWeight() {

        Cursor alldata;
        ArrayList<Double> yVals;
        alldata= myDB.getListContents();

        int count = alldata.getCount();
        double[] weights = new double[count];

        yVals = new ArrayList<Double>();

        //get dates and weight from the database and populate arrays
        for (int m = 0; m < count; m++) {
            alldata.moveToNext();
            weights[m] = alldata.getDouble(2);

            yVals.add(weights[m]);


        }

        double lastWeight = yVals.get(yVals.size() - 1);

        return lastWeight;

    }


    //======================================================
    //             Show error if date is not entered
    //======================================================

    public void showDateErrorMessage(){

        layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.dateerror, null);
        relativeLayout = (RelativeLayout) findViewById(R.id.activity_get_weight);

        popupWindow = new PopupWindow(container, dpToPx(250), dpToPx(250), true); //true allows us to close window by tapping outside
        popupWindow.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, dpToPx(60), dpToPx(120));

        //shut popup outside window
        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                popupWindow.dismiss();
                return false;
            }
        });

    }


    public void showWeightDateErrorMessage(){

        layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.dateweighterror, null);
        relativeLayout = (RelativeLayout) findViewById(R.id.activity_get_weight);

        popupWindow = new PopupWindow(container, dpToPx(250), dpToPx(170), true); //true allows us to close window by tapping outside
        popupWindow.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, dpToPx(60), dpToPx(120));

        //shut popup outside window
        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                popupWindow.dismiss();
                return false;
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        myDB.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



}

