package com.mikel.poseidon;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import uk.ac.mdx.cs.ie.acontextlib.IContextReceiver;
import uk.ac.mdx.cs.ie.acontextlib.hardware.StepCounter;

import static android.R.attr.name;
import static android.R.attr.value;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.mikel.poseidon.R.id.steps_counting;
import static com.mikel.poseidon.R.id.textView;

/**
 * Created by mikel on 02/12/2016.
 */

public class StepService extends Service {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    StepCounter sCounter;
    //private final Random mGenerator = new Random();
    DBHelper myDB = new DBHelper(this);
    Calendar cal;
    //TextView textViewSteps;
    public static final String BROADCAST_INTENT = "com.mikel.poseidon.TOTAL_STEPS";
    private Context mContext;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class LocalBinder extends Binder {
        StepService getService() {
            // Return this instance of LocalService so clients can call public methods
            return StepService.this;
        }
    }

    ArrayList<Long> Value = new ArrayList<Long>();
    long step;

    public long getSteps(){
        mContext = getApplicationContext();
        sCounter = new StepCounter(mContext);
        sCounter.start();

        sCounter.addContextReceiver(new IContextReceiver() {
            @Override
            public void newContextValue(String name, long value) {
                step += value;
                //Value.add(step);
                //Value.add(step+1);*/
                broadcastSteps();


            }

            @Override
            public void newContextValue(String name, double value) {

            }
            @Override
            public void newContextValue(String name, boolean value) {

            }

            @Override
            public void newContextValue(String name, String value) {

            }

            @Override
            public void newContextValue(String name, Object value) {

            }

            @Override
            public void newContextValues(Map<String, String> values) {

            }
        });

        //long steps_counted = setSteps();

       /* System.out.println("step "+steps_counted);
        System.out.println("Array size "+ Value.size());
        System.out.println("Array size "+ Value);*/
        return 0;

    }
    /** method for clients */


    public void broadcastSteps() {

        Intent intent = new Intent();
        try {
            intent.setAction(BROADCAST_INTENT);
            intent.putExtra("steps",step);
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e("StopService", e.getMessage());
        }
    }

   /* public long setSteps(){

        if (Value.size() > 0) {

            for (int i = 0; i < Value.size(); i++) {

                step = step + Value.get(i);


            }
        } else {
            step = 0;
        }

        return step;


    }*/

   public void stopCounting (){


       try {
           sCounter.stop();
       } catch (Exception e) {
           Log.e("StopService", e.getMessage());
       }
   }

    @Override
    public void onDestroy() {


        try {
            if(sCounter.isRunning()) {
                sCounter.stop();
            }

        } catch (Exception e) {
            Log.e("StopService", e.getMessage());
        }

        super.onDestroy();
    }










}
