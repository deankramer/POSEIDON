package com.mikel.poseidon;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import static android.R.attr.data;


public class ViewSummary extends AppCompatActivity {

    DBHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_summary);

        //callback to home button
        ImageButton home_button = (ImageButton) findViewById(R.id.homebutton);
        home_button.setOnClickListener(new View.OnClickListener() {

            // The code in this method will be executed when the preferences button is clicked on.
            @Override
            public void onClick(View view) {

                Intent home_intent = new Intent(ViewSummary.this, MainActivity.class);
                startActivity(home_intent);
            }
        });

        myDB = new DBHelper(this);

        //populate an ArrayList<String> from the database and then view it
        //ArrayList<String> theList = new ArrayList<>();
        Cursor data = myDB.getListContents();
        /*if(data.getCount() == 0){
            Toast.makeText(this, "There are no contents in this list!",Toast.LENGTH_SHORT).show();
        }else{
            while(data.moveToNext()){
                theList.add(data.getString(2));
                ListAdapter listAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,theList);
                listView.setAdapter(listAdapter);
            }
        }*/

        startManagingCursor(data);

        String[] fromFieldNames = new String[]
                {DBHelper.DATE, DBHelper.WEIGHT};
        int[] toViewIDs = new int[]
                {R.id.date_item, R.id.weight_item};

        SimpleCursorAdapter myCursorAdapter = new SimpleCursorAdapter(

                this,                           //Context
                R.layout.item_layout, //Row layout template
                data,                           //Cursor named 'data' gets all the content on DB
                fromFieldNames,                 //DB column names
                toViewIDs);                     //view IDs to put information in


        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(myCursorAdapter);

    }








    /*@Override
    public void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);

       String weight = newIntent.getStringExtra("Weight");

        Log.e("ViewSummary","weight " +weight);

        ArrayList<String> weight_list = new ArrayList<String>();
        weight_list.add(weight);
        weight_list.add(weight);
        weight_list.add(weight);


        // Create an {@link ArrayAdapter}, whose data source is a list of Strings. The
        // adapter knows how to create layouts for each item in the list, using the
        // simple_list_item_1.xml layout resource defined in the Android framework.
        // This list item layout contains a single {@link TextView}, which the adapter will set to
        // display a single word.
        ArrayAdapter<String> weightItemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, weight_list);

        // Find the {@link ListView} object in the view hierarchy of the {@link Activity}.
        // There should be a {@link ListView} with the view ID called list, which is declared in the
        // activity_numbers.xml layout file.
        ListView listView = (ListView) findViewById(R.id.activity_view_summary);

        // Make the {@link ListView} use the {@link ArrayAdapter} we created above, so that the
        // {@link ListView} will display list items for each word in the list of words.
        // Do this by calling the setAdapter method on the {@link ListView} object and pass in
        // 1 argument, which is the {@link ArrayAdapter} with the variable name itemsAdapter.
        listView.setAdapter(weightItemsAdapter);
    }*/



}




