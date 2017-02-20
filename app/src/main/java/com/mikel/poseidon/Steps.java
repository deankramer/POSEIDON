package com.mikel.poseidon;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.R.attr.value;
import static android.R.attr.x;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;
import static com.github.mikephil.charting.charts.Chart.LOG_TAG;
import static com.mikel.poseidon.R.id.calories;
import static com.mikel.poseidon.R.id.steps_counting;
import static com.mikel.poseidon.R.id.stop;
import static com.mikel.poseidon.R.id.tv_chr;
import static com.mikel.poseidon.R.id.weight;

public class Steps extends AppCompatActivity {

    StepService mService = new StepService();
    DBHelper myDB;

    boolean mBound = false;
    Calendar cal;

    ArrayList<Long> stepArray;
    Cursor allsteps;
    private TextView mStepsTextView, caloriesText, tvChron;

    long number;
    private BroadcastReceiver mStepsReceiver;
    private Context mContext;

    private Context context;


    //Instance of Chronometer
    private com.mikel.poseidon.utility.Chronometer mChrono;

    //Thread for chronometer
    private Thread mThreadChrono;

    /**
     * Key for getting saved start time of Chronometer class
     * this is used for onResume/onPause/etc.
     */
    public static final String START_TIME = "START_TIME";
    /**
     * Same story, but to tell whether the Chronometer was running or not
     */
    public static final String CHRONO_WAS_RUNNING = "CHRONO_WAS_RUNNING";
    /**
     * Same story, but if chronometer was stopped, we dont want to lose the stop time shows in
     * the tv_timer
     */
    public static final String TV_TIMER_TEXT = "TV_TIMER_TEXT";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);
        mContext = getApplicationContext();
        context = this;

        //create db
        myDB = new DBHelper(this);

        allsteps= myDB.getListContentsSteps();

        //callback to home button
        ImageButton home_button = (ImageButton) findViewById(R.id.homebutton);
        home_button.setOnClickListener(new View.OnClickListener() {

            // The code in this method will be executed when the preferences button is clicked on.
            @Override
            public void onClick(View view) {

                Intent home_intent = new Intent(Steps.this, MainActivity.class);
                startActivity(home_intent);
            }
        });

        mStepsTextView = (TextView) findViewById(steps_counting);

        caloriesText = (TextView) findViewById(calories);

        tvChron = (TextView)findViewById(tv_chr);

        //chronometer = (Chronometer) findViewById(R.id.chronometer);
        //chronometer.setBase(SystemClock.elapsedRealtime());




    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInstance(); //load chrono instance

    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBReceiver();

        Intent intent = new Intent(this, StepService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unbindService(mConnection);
        mContext.unregisterReceiver(mStepsReceiver);

        saveInstance(); //save chrono instance

    }




    /// METHODS
    public void setupBReceiver(){
        mStepsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final long steps = intent.getLongExtra("steps", 0);
                number = steps;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStepsTextView.setText(String.valueOf(steps));
                    }
                });
            }
        };

        mContext.registerReceiver(mStepsReceiver, new IntentFilter(StepService.BROADCAST_INTENT));

    }


    //===================
    //          Service
    //===================


    public void onStartService(View v) {
        super.onStart();
        // Bind to LocalService
        mService.getSteps();

        //start chronometer
        startChrono();

        Toast.makeText(this, "COUNTING YOUR STEPS", Toast.LENGTH_LONG).show();
    }


    public void onStopService(View v) {

        // Unbind from the service
        if (mBound) {
            //String currentStartTime = getCurrentStartTime();
            //CON LA DATE EN TIPO STRING METIÉNDOLO DESDE getCurrentTime(); myDB.addDataSteps(currentStartTime, number);
            myDB.addDataSteps(number);
            mService.stopCounting();

            //stop chronometer
            stopChrono();
            caloriesText.setText(String.valueOf(getCalories(getLastWeight())));

            Toast.makeText(this, "Step counter STOPPED", Toast.LENGTH_LONG).show();
        } else {

            Toast.makeText(this, "Currently stopped", Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            StepService.LocalBinder binder = (StepService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;


        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

            mBound = false;
        }
    };

    //==============================
    // Calories calculation methods
    //==============================

    //private double getCalories (int walkTime, double weight){
    private double getCalories (double weight){

        double walkTime = getMinutes(tvChron.getText().toString());

        double met = 3.5;

        double caloriesMin;

        caloriesMin = (met * 3.5 * weight)/200;

        //[(MET value) x 3.5 x (weight in kg)]/200

        return round(caloriesMin * walkTime, 0);
    }


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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    //=================
    //   CHRONOMETER
    //=================
    private void startChrono(){
        if(mChrono == null) {
            //instantiate the chronometer
            mChrono = new com.mikel.poseidon.utility.Chronometer(context);
            //run the chronometer on a separate thread
            mThreadChrono = new Thread((Runnable) mChrono);
            mThreadChrono.start();

            //start the chronometer!
            mChrono.start();

        }
    }

    private void stopChrono(){
        //if the chronometer had been instantiated before...
        if(mChrono != null) {
            //stop the chronometer
            mChrono.stop();
            //stop the thread
            mThreadChrono.interrupt();
            mThreadChrono = null;
            //kill the chrono class
            mChrono = null;
        }

    }

    public void updateTimerText(final String timeAsText) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvChron.setText(timeAsText);
            }
        });
    }



    /**
     * If the application goes to background or orientation change or any other possibility that
     * will pause the application, we save some instance values, to resume back from last state
     */
    private void saveInstance() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        //TODO move tags to a static class
        if(mChrono != null && mChrono.isRunning()) {
            editor.putBoolean(CHRONO_WAS_RUNNING, mChrono.isRunning());
            editor.putLong(START_TIME, mChrono.getStartTime());
        } else {
            editor.putBoolean(CHRONO_WAS_RUNNING, false);
            editor.putLong(START_TIME, 0); //0 means chronometer was not active! a redundant check!
        }


        //Same story for timer text
        editor.putString(TV_TIMER_TEXT, tvChron.getText().toString());

        editor.commit();
    }


    /**
     * Load the shared preferences to resume last known state of the application
     */
    private void loadInstance() {

        SharedPreferences pref = getPreferences(MODE_PRIVATE);

        //if chronometer was running
        if(pref.getBoolean(CHRONO_WAS_RUNNING, false)) {
            //get the last start time from the bundle
            long lastStartTime = pref.getLong(START_TIME, 0);
            //if the last start time is not 0
            if(lastStartTime != 0) { //because 0 means value was not saved correctly!

                if(mChrono == null) { //make sure we dont create new instance and thread!

                    if(mThreadChrono != null) { //if thread exists...first interrupt and nullify it!
                        mThreadChrono.interrupt();
                        mThreadChrono = null;
                    }

                    //start chronometer with old saved time
                    mChrono = new com.mikel.poseidon.utility.Chronometer(context, lastStartTime);
                    mThreadChrono = new Thread(mChrono);
                    mThreadChrono.start();
                    mChrono.start();
                }
            }
        }



        String oldTvTimerText = pref.getString(TV_TIMER_TEXT, "");
        if(!oldTvTimerText.isEmpty()){
            tvChron.setText(oldTvTimerText);
        }
    }


    /**
     * Turns a period of time into the number of minutes represented
     * (eg. 06:30:15 returns 360.25)
     * @param hourFormat The string containing the hour format "HH:MM:SS"
     * @return The number of minutes represented, or -1 if the date could not be processed.
     */
    public static double getMinutes(String hourFormat) {

        double minutes = 0;
        String[] split = hourFormat.split(":");

        try {

            minutes += Double.parseDouble(split[0])*60;
            minutes += Double.parseDouble(split[1]);
            minutes += Double.parseDouble(split[2])/60;

            Log.e("MINUTES", String.valueOf(minutes));
            return minutes;

        } catch (Exception e) {
            return -1;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopChrono();
    }
}





